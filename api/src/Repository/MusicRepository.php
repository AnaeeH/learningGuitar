<?php

namespace App\Repository;

use App\Entity\Beat;
use App\Entity\Music;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Music>
 */
class MusicRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Music::class);
    }

    public function findAllOrdered(
        string $orderBy = 'recent',
        ?bool $favorite = null,
        ?bool $riff = null,
        ?string $difficulty = null,
        ?string $status = null
    ): array {
        [$field, $direction] = match ($orderBy) {
            'title'  => ['m.title', 'ASC'],
            'artist' => ['m.artist', 'ASC'],
            default  => ['m.last_played_at', 'DESC'], // 'recent'
        };
        $queryBuilder = $this->createQueryBuilder('m')
            ->orderBy($field, $direction);

        if ($orderBy === 'artist') {
            $queryBuilder->addOrderBy('m.title', 'ASC');
        }

        if ($favorite !== null) {
            $queryBuilder->andWhere('m.favorite = :favorite')
                ->setParameter('favorite', $favorite);
        }
        if ($riff !== null) {
            $queryBuilder->andWhere('m.riff = :riff')
                ->setParameter('riff', $riff);
        }
        if ($difficulty !== null) {
            $queryBuilder->andWhere('m.difficulty = :difficulty')
                ->setParameter('difficulty', $difficulty);
        }

        if ($status !== null) {
            match ($status) {
                'to_learn' => $queryBuilder->andWhere('m.progress = 0'),
                'learnt'   => $queryBuilder->andWhere('m.progress = 100'),
                'learning' => $queryBuilder->andWhere('m.progress > 0')->andWhere('m.progress < 100'),
                default    => null
            };
        }

        return $queryBuilder->getQuery()->getResult();
    }

    public function findLastPlayed(): ?Music
    {
        return $this->createQueryBuilder('m')
            ->where('m.last_played_at IS NOT NULL')
            ->orderBy('m.last_played_at', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function findDistinctChordsByMusic(int $musicId): array
    {
        $result = $this->getEntityManager()
            ->createQueryBuilder()
            ->select('b.harmonyText, MIN(mea.numero) as firstMeasure, MIN(b.position) as firstPosition')
            ->from(Beat::class, 'b')
            ->join('b.measure', 'mea')
            ->join('mea.music', 'mus')
            ->where('mus.id = :musicId')
            ->groupBy('b.harmonyText')
            ->having('b.harmonyText IS NOT NULL')
            ->orderBy('firstMeasure', 'ASC')
            ->addOrderBy('firstPosition', 'ASC')
            ->setParameter('musicId', $musicId)
            ->getQuery()
            ->getResult();

        return array_column($result, 'harmonyText');
    }
}
