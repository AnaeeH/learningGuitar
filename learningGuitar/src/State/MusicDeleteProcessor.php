<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Entity\Music;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;



class MusicDeleteProcessor implements ProcessorInterface
{
    public function __construct(
        private EntityManagerInterface $em,
        private ParameterBagInterface $params
    ) {}

    public function process(
        mixed $data,
        Operation $operation,
        array $uriVariable = [],
        array $context = []
    ): void {
        if (!$data instanceof Music) {
            return;
        }

        $this->deleteFile('music_audio_directory', $data->getAudioPath());
        $this->deleteFile('music_xml_directory', $data->getXmlPath());

        $this->em->remove($data);
        $this->em->flush();
    }

    private function deleteFile(string $paramKey, ?string $filename): void
    {
        if (!$filename) {
            return;
        }

        $directory = $this->params->get($paramKey);
        $filepath = $directory . '/' . $filename;

        if (file_exists($filepath)) {
            unlink($filepath);
        }
    }
}
