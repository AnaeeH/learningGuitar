<?php

namespace App\Command;

use App\Entity\Note;
use App\Entity\Scale;
use App\Entity\ScaleNote;
use App\Entity\ScalePosition;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:seed-scales',
    description: 'Seeds the database with a starter set of guitar scales',
)]
class SeedScalesCommand extends Command
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
            $existing = $this->em->getRepository(Scale::class)
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

            $scale = new Scale();
            $scale->setName($data['name']);
            $scale->setType($data['type']);
            $scale->setNote($notes[$data['note']]);

            foreach ($data['positions'] as $positionData) {
                $position = new ScalePosition();
                $position->setPositionNumber($positionData['number']);
                $position->setStartFret($positionData['startFret']);

                foreach ($positionData['notes'] as [$string, $fret, $isRoot]) {
                    $note = new ScaleNote();
                    $note->setString($string);
                    $note->setFret($fret);
                    $note->setIsRoot($isRoot);

                    $position->addNote($note);
                }

                $scale->addPosition($position);
            }

            $this->em->persist($scale);
            $io->text(sprintf(
                '  <info>+</info> %s created (%d positions)',
                $data['name'],
                count($data['positions'])
            ));
            $created++;
        }

        $this->em->flush();

        $io->success(sprintf('%d scale(s) created, %d skipped.', $created, $skipped));

        return Command::SUCCESS;
    }


    private function getChordCatalog(): array
    {
        return [
            [
                'name' => 'Pentatonique mineure',
                'type' => 'pentatonic',
                'note' => 'A',
                'positions' => [
                    [
                        'number' => 1,
                        'startFret' => 5,
                        'notes' => [
                            [6, 1, true],  [6, 4, false],
                            [5, 1, false], [5, 3, false],
                            [4, 1, false], [4, 3, true],
                            [3, 1, false], [3, 3, false],
                            [2, 1, false], [2, 4, false],
                            [1, 1, true],  [1, 4, false],
                        ],
                    ],
                    [
                        'number' => 2,
                        'startFret' => 7,
                        'notes' => [
                            [6, 2, false], [6, 4, false],
                            [5, 1, false], [5, 4, false],
                            [4, 1, true],  [4, 4, false],
                            [3, 1, false], [3, 3, false],
                            [2, 2, false], [2, 4, true],
                            [1, 2, false], [1, 4, false],
                        ],
                    ],
                    [
                        'number' => 3,
                        'startFret' => 9,
                        'notes' => [
                            [6, 2, false], [6, 4, false],
                            [5, 2, false], [5, 4, true],
                            [4, 2, false], [4, 4, false],
                            [3, 1, false], [3, 4, false],
                            [2, 2, true],  [2, 5, false],
                            [1, 2, false], [1, 4, false],
                        ],
                    ],
                    [
                        'number' => 4,
                        'startFret' => 12,
                        'notes' => [
                            [6, 1, false], [6, 4, false],
                            [5, 1, true],  [5, 4, false],
                            [4, 1, false], [4, 3, false],
                            [3, 1, false], [3, 3, true],
                            [2, 2, false], [2, 4, false],
                            [1, 1, false], [1, 4, false],
                        ],
                    ],
                    [
                        'number' => 5,
                        'startFret' => 14,
                        'notes' => [
                            [6, 2, false], [6, 4, true],
                            [5, 2, false], [5, 4, false],
                            [4, 1, false], [4, 4, false],
                            [3, 1, true],  [3, 4, false],
                            [2, 2, false], [2, 4, false],
                            [1, 2, false], [1, 4, true],
                        ],
                    ],
                ],
            ],
        ];
    }
}