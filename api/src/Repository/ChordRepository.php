<?php

namespace App\Repository;

use App\Entity\Chord;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<TChordCHR>
 *
 * @method TChordCHR|null find($id, $lockMode = null, $lockVersion = null)
 * @method TChordCHR|null findOneBy(array $criteria, array $orderBy = null)
 * @method TChordCHR[]    findAll()
 * @method TChordCHR[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class ChordRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Chord::class);
    }

//    /**
//     * @return TChordCHR[] Returns an array of TChordCHR objects
//     */
//    public function findByExampleField($value): array
//    {
//        return $this->createQueryBuilder('t')
//            ->andWhere('t.exampleField = :val')
//            ->setParameter('val', $value)
//            ->orderBy('t.id', 'ASC')
//            ->setMaxResults(10)
//            ->getQuery()
//            ->getResult()
//        ;
//    }

//    public function findOneBySomeField($value): ?TChordCHR
//    {
//        return $this->createQueryBuilder('t')
//            ->andWhere('t.exampleField = :val')
//            ->setParameter('val', $value)
//            ->getQuery()
//            ->getOneOrNullResult()
//        ;
//    }
}
