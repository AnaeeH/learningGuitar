<?php

namespace App\Command;

use App\Entity\Chord;
use App\Entity\ChordPosition;
use App\Entity\Note;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:seed-chords',
    description: 'Seeds the database with a starter set of guitar chords',
)]
class SeedChordsCommand extends Command
{
    public function __construct(private readonly EntityManagerInterface $em)
    {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        $notes = [];
        foreach ($this->em->getRepository(Note::class)->findAll() as $n) {
            $notes[$n->getName()] = $n;
        }

        $created = 0;
        $skipped = 0;

        foreach ($this->getChordCatalog() as $data) {
            $existing = $this->em->getRepository(Chord::class)
                ->findOneBy(['name' => $data['name']]);

            if ($existing !== null) {
                $io->text(sprintf('  <comment>~</comment> %s already exists, skipped', $data['name']));
                $skipped++;
                continue;
            }

            if (!isset($notes[$data['note']])) {
                $io->error(sprintf('Note "%s" not found. Please seed notes first.', $data['note']));
                return Command::FAILURE;
            }

            $chord = new Chord();
            $chord->setName($data['name']);
            $chord->setIsMajor($data['isMajor']);
            $chord->setNote($notes[$data['note']]);
            $chord->setMutedStrings(!empty($data['muted']) ? $data['muted'] : null);

            if (isset($data['barre'])) {
                $chord->setBarreFret($data['barre']['fret']);
                $chord->setBarreFromString($data['barre']['from']);
                $chord->setBarreToString($data['barre']['to']);
            }

            foreach ($data['positions'] as [$string, $fret]) {
                $position = new ChordPosition();
                $position->setString($string);
                $position->setFret($fret);
                $chord->addPosition($position);
            }

            $this->em->persist($chord);
            $io->text(sprintf('  <info>+</info> %s created', $data['name']));
            $created++;
        }

        $this->em->flush();

        $io->success(sprintf('%d chord(s) created, %d skipped.', $created, $skipped));

        return Command::SUCCESS;
    }


    private function getChordCatalog(): array
    {
        return [
            // A 
            [
                'name' => 'A', 'note' => 'A', 'isMajor' => true,
                'muted' => [6],
                'positions' => [[4, 2], [3, 2], [2, 2]],
            ],

            // B 
            [
                'name' => 'B', 'note' => 'B', 'isMajor' => true,
                'muted' => [],
                'barre' => ['fret' => 1, 'from' => 1, 'to' => 6],
                'positions' => [[4, 3], [3, 3], [2, 3]],
            ],

            // C 
            [
                'name' => 'C', 'note' => 'C', 'isMajor' => true,
                'muted' => [6],
                'positions' => [[5, 3], [4, 2], [2, 1]],
            ],

            // D 
            [
                'name' => 'D', 'note' => 'D', 'isMajor' => true,
                'muted' => [5, 6],
                'positions' => [[3, 2], [1, 2], [2, 3]],
            ],

            // E 
            [
                'name' => 'E', 'note' => 'E', 'isMajor' => true,
                'muted' => [],
                'positions' => [[5, 2], [4, 2], [3, 1]],
            ],

            // F 
            [
                'name' => 'F', 'note' => 'F', 'isMajor' => true,
                'muted' => [],
                'barre' => ['fret' => 1, 'from' => 1, 'to' => 6],
                'positions' => [[5, 3], [4, 3], [3, 2]],
            ],

            // G 
            [
                'name' => 'G', 'note' => 'G', 'isMajor' => true,
                'muted' => [],
                'positions' => [[6, 3], [5, 2], [1, 3]],
            ],

            // Am
            [
                'name' => 'Am', 'note' => 'A', 'isMajor' => false,
                'muted' => [6],
                'positions' => [[4, 2], [3, 2], [2, 1]],
            ],

            // Dm
            [
                'name' => 'Dm', 'note' => 'D', 'isMajor' => false,
                'muted' => [5, 6],
                'positions' => [[3, 2], [2, 3], [1, 1]],
            ],

            // Em
            [
                'name' => 'Em', 'note' => 'E', 'isMajor' => false,
                'muted' => [],
                'positions' => [[5, 2], [4, 2]],
            ],

            // Fmaj7
            [
                'name' => 'Fmaj7', 'note' => 'F', 'isMajor' => true,
                'muted' => [5, 6],
                'positions' => [[4, 3], [3, 2], [2, 1]],
            ],
        ];
    }
}