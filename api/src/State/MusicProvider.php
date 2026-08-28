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
        $status = $context['filters']['status'] ?? null;

        return match ($operation->getName()) {
            'get_title'       => $this->musicRepository->findAllOrdered(
                'title',
                $context['filters']['favorite'] ?? null,
                $context['filters']['riff'] ?? null,
                $context['filters']['difficulty'] ?? null,
                $status
            ),
            'get_artist'       => $this->musicRepository->findAllOrdered(
                'artist',
                $context['filters']['favorite'] ?? null,
                $context['filters']['riff'] ?? null,
                $context['filters']['difficulty'] ?? null,
                $status
            ),
            'get_recent'      => $this->musicRepository->findAllOrdered(
                'recent',
                $context['filters']['favorite'] ?? null,
                $context['filters']['riff'] ?? null,
                $context['filters']['difficulty'] ?? null,
                $status
            ),
            'get_last_played' => $this->musicRepository->findLastPlayed(),
            'get_chords'      => $this->musicRepository->findDistinctChordsByMusic($uriVariables['id']),
            default           => null
        };
    }
}
