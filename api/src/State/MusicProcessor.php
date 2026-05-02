<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Entity\Beat;
use App\Entity\Measure;
use App\Entity\Music;
use App\Entity\Video;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;



class MusicProcessor implements ProcessorInterface
{
    public function __construct(
        private EntityManagerInterface $em,
        private RequestStack $requestStack,
        private ParameterBagInterface $params
    ) {}

    public function process(
        mixed $data,
        Operation $operation,
        array $uriVariable = [],
        array $context = []
    ): Music {
        $request = $this->requestStack->getCurrentRequest();

        $music = new Music();

        $this->processVideo($request, $music);

        $xmlDir = $this->params->get('music_xml_directory');
        $audioDir = $this->params->get('music_audio_directory');
        $xmlFile = $request->files->get('xmlFile');
        $audioFile = $request->files->get('audioFile');

        if ($xmlFile !== null) {
            $originalName = $xmlFile->getClientOriginalName();
            $xmlFile->move($xmlDir, $originalName);
            $this->parseXML($xmlDir . '/' . $originalName, $music);
        }
        if ($audioFile !== null) {
            $originalName = $audioFile->getClientOriginalName();
            $audioFile->move($audioDir, $originalName);
            $music->setAudioPath($originalName);
        }

        $riff = $request->request->get('riff', 'false');
        $music->setRiff(filter_var($riff, FILTER_VALIDATE_BOOLEAN));

        $this->em->persist($music);
        $this->em->flush();

        if ($xmlFile !== null) {
            $oldPath = $xmlDir . '/' . $xmlFile->getClientOriginalName();
            $music->setXmlFileName(str_replace(' ', '', $music->getTitle()) . $music->getId() . '.musicxml');
            rename($oldPath, $xmlDir . '/' . $music->getXmlFileName());
        }

        return $music;
    }

    private function processVideo($request, Music $music): void
    {
        $videoRaw = $request->request->get('video');

        if ($videoRaw !== null) {
            $videoData = json_decode($videoRaw, true);
            $videoId = $videoData['videoId'] ?? null;
            $startSec = $videoData['startSec'] ?? 0;

            if ($videoId !== null && $startSec !== null) {
                $video = new Video();
                $video->setVideoId($videoId);
                $video->setStartSec((float) $startSec);
                $video->setMusic($music);
                $this->em->persist($video);
                $music->setVideo($video);
            }
        }
    }

    private function parseXML(string $path, Music $music): void
    {
        $xml = simplexml_load_file($path);

        $title = (string) $xml->work->{'work-title'};
        if (!empty($title)) {
            $music->setTitle($title);
        }

        $artist = (string) $xml->identification->{'creator'};
        if (!empty($artist)) {
            $music->setArtist($artist);
        }

        $firstMeasure = $xml->part->measure[0];
        foreach ($firstMeasure->direction as $direction) {
            $tempo = (int) $direction->sound['tempo'];
            if ($tempo > 0) {
                $music->setTempo($tempo);
                break;
            }
        }

        $beats = (string) $firstMeasure->attributes->time->beats;
        $beatType = (string) $firstMeasure->attributes->time->{'beat-type'};
        if (!empty($beats) && !empty($beatType)) {
            $music->setTimeSignature($beats . '/' . $beatType);
        }

        $fifths = (string) $firstMeasure->attributes->key->fifths;
        $mode = (string) $firstMeasure->attributes->key->mode;
        if (!empty($mode)) {
            $music->setKeySignature($fifths . ' ' . $mode);
        }

        $this->parseXMLMeasures($xml, $music);
    }

    private function parseXMLMeasures(\SimpleXMLElement $xml, Music $music): void
    {
        $currentStaves = 1; 

        foreach ($xml->part->measure as $measureNode) {
            $measure = new Measure();
            $measure->setNumero((int) $measureNode['number']);
            $measure->setMusic($music);

            foreach ($measureNode->direction as $direction) {
                $measureTempo = (int) $direction->sound['tempo'];
                if ($measureTempo > 0 && $measureTempo !== $music->getTempo()) {
                    $measure->setTempo($measureTempo);
                    break;
                }
            }

            $timeNode = $measureNode->attributes->time ?? null;
            if ($timeNode !== null) {
                $measureBeats = (string) $timeNode->beats;
                $measureBeatType = (string) $timeNode->{'beat-type'};
                if (!empty($measureBeats) && !empty($measureBeatType)) {
                    $sig = $measureBeats . '/' . $measureBeatType;
                    if ($sig !== $music->getTimeSignature()) {
                        $measure->setTimeSignature($sig);
                    }
                }
            }

            $stavesNode = $measureNode->attributes->staves ?? null;
            if ($stavesNode !== null && (int) $stavesNode > 0) {
                $currentStaves = (int) $stavesNode;
            }

            foreach ($measureNode->barline as $barline) {
                $direction = (string) $barline->repeat['direction'];
                if ($direction === 'forward') {
                    $measure->setRepeatStart(true);
                } elseif ($direction === 'backward') {
                    $measure->setRepeatEnd(true);
                }
            }

            $this->em->persist($measure);

            $staves = (int) $measureNode->attributes->staves;
            $this->parseXMLNotes($measureNode, $measure, $currentStaves);
        }
    }

    private function parseXMLNotes(mixed $measureNode, Measure $measure, int $staves): void
    {
        $positionByVoice = [];
        $lastPositionByVoice = [];
        $pendingHarmonyByVoice = [];
        $pendingHarmony = null;
        $staff1Beats = []; // liste simple de tous les beats créés depuis staff 1

        foreach ($measureNode->children() as $child) {
            $nodeName = $child->getName();

            if ($nodeName === 'harmony') {
                $kindText = (string) $child->kind['text'];
                $root     = (string) $child->root->{'root-step'};
                $pendingHarmony = $kindText !== '' ? $root . $kindText : $root;
                continue;
            }

            if ($nodeName !== 'note') continue;

            $voice    = (int) $child->voice;
            $staff    = (int) $child->staff;
            $isChord  = isset($child->chord);
            $isRest   = isset($child->rest);
            $duration = (int) $child->duration;
            $hammerOn = isset($child->notations->{'hammer-on'});
            $pullOff  = isset($child->notations->{'pull-off'});
            $isGrace  = isset($child->grace);       

            if (!isset($positionByVoice[$voice])) {
                $positionByVoice[$voice]     = 0;
                $lastPositionByVoice[$voice] = 0;
            }

            $isTied = false;
            foreach ($child->tie as $tie) {
                if ((string) $tie['type'] === 'stop') {
                    $isTied = true;
                    break;
                }
            }
            
            if ($isGrace) {
                $isTied = true;
            }

            $hasTablature = isset($child->notations->technical->string);

            // -------------------------------------------------------
            // STAFF 1 — crée le beat avec les infos partition
            // -------------------------------------------------------
            if ($staves < 2 || $staff === 1) {
                $position = $isChord ? ($lastPositionByVoice[$voice] ?? 0) : $positionByVoice[$voice];

                $beat = new Beat();
                $beat->setMeasure($measure);
                $beat->setDuration($duration);
                $beat->setType((string) $child->type);
                $beat->setDot(isset($child->dot));
                $beat->setIsRest($isRest);
                $beat->setTied($isTied);
                $beat->setPosition($position);
                $beat->setHammerOn($hammerOn);
                $beat->setPullOff($pullOff);

                if (!$isRest) {
                    $beat->setPitchStep((string) $child->pitch->step);
                    $beat->setPitchOctave((int) $child->pitch->octave);
                    if (isset($child->pitch->alter)) {
                        $beat->setPitchAlter((float) $child->pitch->alter);
                    }
                    if ($staves < 2 && $hasTablature) {
                        $beat->setString((int) $child->notations->technical->string);
                        $beat->setFret((int) $child->notations->technical->fret);
                    }
                }

                $harmonyToApply = $pendingHarmonyByVoice[$voice] ?? $pendingHarmony ?? null;
                if ($harmonyToApply !== null && !$isChord) {
                    $beat->setHarmonyText($harmonyToApply);
                    $beat->setStrumDirection($this->extractStrumDirection($child));
                    unset($pendingHarmonyByVoice[$voice]);
                    unset($pendingHarmony);
                }

                $staff1Beats[] = $beat;

                if (!$isChord && !$isGrace) {
                    $lastPositionByVoice[$voice] = $positionByVoice[$voice];
                    $positionByVoice[$voice] += $duration;
                }
                continue;
            }

            // -------------------------------------------------------
            // STAFF 2 — cherche le beat correspondant et complète
            // -------------------------------------------------------
            if ($isGrace) continue;
            
            if (!$hasTablature) {
                // Pas d'infos tablature → rien à compléter
                if (!$isChord) {
                    $lastPositionByVoice[$voice] = $positionByVoice[$voice];
                    $positionByVoice[$voice] += $duration;
                }
                continue;
            }

            $position = $isChord ? ($lastPositionByVoice[$voice] ?? 0) : $positionByVoice[$voice];
            $string   = (int) $child->notations->technical->string;
            $fret     = (int) $child->notations->technical->fret;

            // Cherche dans staff1Beats un beat qui correspond :
            // même position, même durée, pas encore de tablature
            foreach ($staff1Beats as $existingBeat) {
                if (
                    $existingBeat->getPosition() === $position &&
                    $existingBeat->getDuration() === $duration &&
                    $existingBeat->getString() === null
                ) {
                    $existingBeat->setString($string);
                    $existingBeat->setFret($fret);
                    break;
                }
            }

            if (!$isChord) {
                $lastPositionByVoice[$voice] = $positionByVoice[$voice];
                $positionByVoice[$voice] += $duration;
            } else {
                $lastPositionByVoice[$voice] = $position;
            }
        }

        foreach ($staff1Beats as $beat) {
            $this->em->persist($beat);   
        }
    }

    // private function parseXMLNotes(mixed $measureNode, Measure $measure, int $staves): void
    // {
    //     $positionByVoice = [];
    //     $lastPositionByVoice = [];
    //     $pendingHarmonyByVoice = [];

    //     foreach ($measureNode->children() as $child) {
    //         $nodeName = $child->getName();

    //         if ($nodeName === 'harmony') {
    //             $kindText = (string) $child->kind['text'];
    //             $root     = (string) $child->root->{'root-step'};
    //             // On stocke l'harmonie en attente — elle sera attribuée à la prochaine note non-chord
    //             $lastHarmony = $kindText !== '' ? $root . $kindText : $root;
    //             // On l'associe à toutes les voix (on ne sait pas encore quelle voix la prendra)
    //             $pendingHarmony = $lastHarmony;
    //             continue;
    //         }

    //         if ($nodeName !== 'note') {
    //             continue;
    //         }

    //         $voice   = (int) $child->voice;
    //         $staff   = (int) $child->staff;
    //         $isChord = isset($child->chord);
    //         $isRest  = isset($child->rest);
    //         $duration = (int) $child->duration;

    //         // Initialiser la position de cette voix
    //         if (!isset($positionByVoice[$voice])) {
    //             $positionByVoice[$voice] = 0;
    //             $lastPositionByVoice[$voice] = 0;
    //         }

    //         // Ignorer staff 1 (partition classique) — on ne traite que la tablature (staff 2)
    //         // SAUF si staves == 1 (fichier sans double portée)
    //         if ($staves >= 2 && $staff === 1) {
    //             if (!$isChord) {
    //                 $lastPositionByVoice[$voice] = $positionByVoice[$voice];
    //                 $positionByVoice[$voice] += $duration;
    //             }
    //             // On consomme quand même l'harmonie en attente si c'est la première note de l'accord
    //             if (!$isChord && isset($pendingHarmony)) {
    //                 $pendingHarmonyByVoice[$voice] = $pendingHarmony;
    //                 unset($pendingHarmony);
    //             }
    //             continue;
    //         }

    //         $isTied = false;
    //         foreach ($child->tie as $tie) {
    //             if ((string) $tie['type'] === 'stop') {
    //                 $isTied = true;
    //                 break;
    //             }
    //         }


    //         dump([
    //                 'bnojour' => "frep",

    //             ]); die;
    //         // Pour staff 2 : on ignore les notes chord (doublons de la mélodie dans les accords plaqués)
    //         // SAUF si c'est une note avec fret/string info (tablature individuelle)
    //         $hasTablature = isset($child->notations->technical->string);

    //         if ($isChord && $hasTablature) {

    //             dump([
    //                 'voice' => $voice,
    //                 'staff' => $staff,
    //                 'fret' => (int) $child->notations->technical->fret,
    //                 'lastPosition' => $lastPositionByVoice[$voice] ?? 'non défini',
    //                 'currentPosition' => $positionByVoice[$voice] ?? 'non défini',
    //             ]); die;

    //             // Dans le fichier 1, les chords sur staff 2 sont des doublons → on ignore
    //             // Dans le fichier 2, les chords sur staff 2 peuvent avoir leur propre fret (ex mesure 24)
    //             // On les sauvegarde seulement s'ils ont des infos de tablature distinctes
    //             // chord avec tablature → on crée un Beat à la même position que la note précédente
    //             $beat = new Beat();
    //             $beat->setMeasure($measure);
    //             $beat->setDuration($duration);
    //             $beat->setType((string) $child->type);
    //             $beat->setDot(isset($child->dot));
    //             $beat->setIsRest(false);
    //             $beat->setTied($isTied);
    //             $beat->setPosition($lastPositionByVoice[$voice] ?? 0); // même position que la note parente

    //             if (isset($child->notations->technical->string)) {
    //                 $beat->setString((int) $child->notations->technical->string);
    //                 $beat->setFret((int) $child->notations->technical->fret);
    //             }
    //             $beat->setPitchStep((string) $child->pitch->step);
    //             $beat->setPitchOctave((int) $child->pitch->octave);
    //             if (isset($child->pitch->alter)) {
    //                 $beat->setPitchAlter((float) $child->pitch->alter);
    //             }

    //             $this->em->persist($beat);
    //             continue;
    //         }

    //         // Note normale (non-chord) sur staff 2
    //         $beat = new Beat();
    //         $beat->setMeasure($measure);
    //         $beat->setDuration($duration);
    //         $beat->setType((string) $child->type);
    //         $beat->setDot(isset($child->dot));
    //         $beat->setIsRest($isRest);
    //         $beat->setTied($isTied);
    //         $beat->setPosition($positionByVoice[$voice]);

    //         // Attribuer l'harmonie en attente (venant de <harmony> ou de la voix miroir staff 1)
    //         $harmonyToApply = $pendingHarmonyByVoice[$voice] ?? $pendingHarmony ?? null;
    //         if ($harmonyToApply !== null) {
    //             $beat->setHarmonyText($harmonyToApply);
    //             $beat->setStrumDirection($this->extractStrumDirection($child));
    //             unset($pendingHarmonyByVoice[$voice]);
    //             unset($pendingHarmony);
    //         }

    //         if (!$isRest) {
    //             $beat->setPitchStep((string) $child->pitch->step);
    //             $beat->setPitchOctave((int) $child->pitch->octave);
    //             if (isset($child->pitch->alter)) {
    //                 $beat->setPitchAlter((float) $child->pitch->alter);
    //             }
    //             if ($hasTablature) {
    //                 $beat->setString((int) $child->notations->technical->string);
    //                 $beat->setFret((int) $child->notations->technical->fret);
    //             }
    //         }

    //         $this->em->persist($beat);
    //         $lastPositionByVoice[$voice] = $positionByVoice[$voice];
    //         $positionByVoice[$voice] += $duration;
    //     }
    // }

    // if ($staves === 2 && (int) $noteNode->staff === 1) {
    //     continue;
    // }

    // $beat = new Beat();
    // $beat->setMeasure($measure);

    // if (!isset($noteNode->rest)) {
    //     $beat->setPitchStep((string) $noteNode->pitch->step);
    //     $beat->setPitchOctave((int) $noteNode->pitch->octave);

    //     if (isset($noteNode->pitch->alter)) {
    //         $beat->setPitchAlter((float) $noteNode->pitch->alter);
    //     }

    //     if (isset($noteNode->notations->technical->string)) {
    //         $beat->setString((int) $noteNode->notations->technical->string);
    //         $beat->setFret((int) $noteNode->notations->technical->fret);
    //     }
    // }
    // $beat->setDuration((int) $noteNode->duration);
    // $beat->setType((string) $noteNode->type);
    // $beat->setDot(isset($noteNode->dot));


    // $this->em->persist($beat);

    private function extractStrumDirection(\SimpleXMLElement $noteNode): ?string
    {
        if (!isset($noteNode->notations->technical)) {
            return null;
        }
        $technical = $noteNode->notations->technical;
        if (isset($technical->{'down-bow'})) return 'down';
        if (isset($technical->{'up-bow'}))   return 'up';
        return null;
    }
}
