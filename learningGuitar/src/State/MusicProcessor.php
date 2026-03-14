<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
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

        $xmlDir = $this->params->get('kernel.project_dir') . '/public/files/xml';
        $audioDir = $this->params->get('kernel.project_dir') . '/public/files/audio';
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
    }
}
