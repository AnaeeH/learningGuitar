<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Entity\Beat;
use App\Entity\Measure;
use App\Entity\Music;
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

        $xmlDir = $this->params->get('music_xml_directory');
        $audioDir = $this->params->get('music_audio_directory');
        $xmlFile = $request->files->get('xmlFile');
        $audioFile = $request->files->get('audioFile');

        if ($xmlFile !== null) {
            $originalName = $xmlFile->getClientOriginalName();
            $xmlFile->move($xmlDir, $originalName);
            $music->setXmlPath($originalName);
            $this->parseXML($xmlDir . '/' . $originalName, $music);
        }
        if ($audioFile !== null) {
            $originalName = $audioFile->getClientOriginalName();
            $audioFile->move($audioDir, $originalName);
            $music->setAudioPath($originalName);
        }

        $this->em->persist($music);
        $this->em->flush();

        return $music;
    }

    private function parseXML(string $path, Music $music): void
    {
        $xml = simplexml_load_file($path);

        $title = (string) $xml->work->{'work-title'};
        if (!empty($title)) {
            $music->setTitle($title);
        }

        $tempo = (int) $xml->part->measure->direction->{'sound'}['tempo'];
        if ($tempo > 0) {
            $music->setTempo($tempo);
        }

        $beats = (string) $xml->part->measure->attributes->time->beats;
        $beatType = (string) $xml->part->measure->attributes->time->{'beat-type'};
        if (!empty($beats) && !empty($beatType)) {
            $music->setTimeSignature($beats . '/' . $beatType);
        }

        $fifths = (string) $xml->part->measure->attributes->key->fifths;
        $mode = (string) $xml->part->measure->attributes->key->mode;
        if (!empty($mode)) {
            $music->setKeySignature($fifths . ' ' . $mode);
        }

        $this->parseXMLMeasures($xml, $music);
    }

    private function parseXMLMeasures(\SimpleXMLElement $xml, Music $music): void
    {
        foreach ($xml->part->measure as $measureNode) {
            $measure = new Measure();
            $measure->setNumero((int) $measureNode['number']);
            $measure->setMusic($music);

            $measureTempo = (int) $measureNode->direction->sound['tempo'];
            if ($measureTempo > 0 && $measureTempo !== $music->getTempo()) {
                $measure->setTempo($measureTempo);
            }

            $measureBeats = (string) $measureNode->attributes->time->beats;
            $measureBeatType = (string) $measureNode->attributes->time->{'beat-type'};
            if (!empty($measureBeats) && !empty($measureBeatType)) {
                $sig = $measureBeats . '/' . $measureBeatType;
                if ($sig !== $music->getTimeSignature()) {
                    $measure->setTimeSignature($sig);
                }
            }

            $this->em->persist($measure);

            $staves = (int) $measureNode->attributes->staves;
            $this->parseXMLNotes($measureNode, $measure, $staves);
        }
    }

    private function parseXMLNotes(mixed $measureNode, Measure $measure, int $staves): void
    {
        foreach ($measureNode->note as $noteNode) {
            if ($staves === 2 && (int) $noteNode->staff === 1) {
                continue;
            }

            $beat = new Beat();
            $beat->setMeasure($measure);

            if (!isset($noteNode->rest)) {
                $beat->setPitchStep((string) $noteNode->pitch->step);
                $beat->setPitchOctave((int) $noteNode->pitch->octave);

                if (isset($noteNode->pitch->alter)) {
                    $beat->setPitchAlter((float) $noteNode->pitch->alter);
                }

                if (isset($noteNode->notations->technical->string)) {
                    $beat->setString((int) $noteNode->notations->technical->string);
                    $beat->setFret((int) $noteNode->notations->technical->fret);
                }
            }
            $beat->setDuration((int) $noteNode->duration);
            $beat->setType((string) $noteNode->type);
            $beat->setDot(isset($noteNode->dot));

            $this->em->persist($beat);
        }
    }
}
