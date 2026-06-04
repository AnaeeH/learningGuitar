<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\MusicRepository;

class MusicProvider implements ProviderInterface
{
    public function __construct(
        private MusicRepository $musicRepository
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): object|array|null
    {
        return match ($operation->getName()) {
            'get_title'       => $this->musicRepository->findAllOrderedByTitle(),
            'get_recent'      => $this->musicRepository->findAllOrderedByLastPlayed(),
            'get_last_played' => $this->musicRepository->findLastPlayed(),
            'get_chords'      => $this->musicRepository->findDistinctChordsByMusic($uriVariables['id']),
            default           => null
        };
    }
}
