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

    public function findAllOrderedByTitle(): array
    {
        return $this->createQueryBuilder('m')
            ->orderBy('m.title', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findAllOrderedByLastPlayed(): array
    {
        return $this->createQueryBuilder('m')
            ->orderBy('m.last_played_at', 'DESC')
            ->getQuery()
            ->getResult();
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
