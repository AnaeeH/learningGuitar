<?php

namespace App\Entity;

use App\Repository\ChordPositionRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: ChordPositionRepository::class)]
#[ORM\Table(name: 't_chord_position_cpo')]
#[ORM\UniqueConstraint(name: 'uniq_chord_string', columns: ['cpo_chr_id', 'cpo_string'])]
class ChordPosition
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'cpo_id')]
    #[Groups(['chord:detail'])]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'positions')]
    #[ORM\JoinColumn(
        nullable: false,
        name: 'cpo_chr_id',
        referencedColumnName: 'chr_id',
        onDelete: 'CASCADE'
    )]
    private ?Chord $chord = null;

    #[ORM\Column(name: 'cpo_string')]
    #[Groups(['chord:detail'])]
    private ?int $string = null;

    #[ORM\Column(name: 'cpo_fret')]
    #[Groups(['chord:detail'])]
    private ?int $fret = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getChord(): ?Chord
    {
        return $this->chord;
    }

    public function setChord(?Chord $chord): static
    {
        $this->chord = $chord;
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
}