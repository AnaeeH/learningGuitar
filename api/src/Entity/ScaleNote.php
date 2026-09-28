<?php

namespace App\Entity;

use App\Repository\ScaleNoteRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: ScaleNoteRepository::class)]
#[ORM\Table(name: 't_scale_note_snt')]
#[ORM\UniqueConstraint(
    name: 'uniq_scale_position_string_fret',
    columns: ['snt_spo_id', 'snt_string', 'snt_fret']
)]
class ScaleNote
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'snt_id')]
    #[Groups(['scale:detail'])]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'notes')]
    #[ORM\JoinColumn(
        nullable: false,
        name: 'snt_spo_id',
        referencedColumnName: 'spo_id',
        onDelete: 'CASCADE'
    )]
    private ?ScalePosition $position = null;

    #[ORM\Column(name: 'snt_string')]
    #[Groups(['scale:detail'])]
    private ?int $string = null;

    #[ORM\Column(name: 'snt_fret')]
    #[Groups(['scale:detail'])]
    private ?int $fret = null;

    #[ORM\Column(name: 'snt_is_root', options: ['default' => false])]
    #[Groups(['scale:detail'])]
    private bool $isRoot = false;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getPosition(): ?ScalePosition
    {
        return $this->position;
    }

    public function setPosition(?ScalePosition $position): static
    {
        $this->position = $position;
        return $this;
    }

    public function getString(): ?int
    {
        return $this->string;
    }

    public function setString(int $string): static
    {
        $this->string = $string;
        return $this;
    }

    public function getFret(): ?int
    {
        return $this->fret;
    }

    public function setFret(int $fret): static
    {
        $this->fret = $fret;
        return $this;
    }

    public function isRoot(): bool
    {
        return $this->isRoot;
    }

    public function setIsRoot(bool $isRoot): static
    {
        $this->isRoot = $isRoot;
        return $this;
    }
}