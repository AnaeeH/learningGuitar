<?php

namespace App\Entity;

use App\Repository\BeatRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: BeatRepository::class)]
#[ORM\Table(name: 't_beat_bea')]
#[ORM\Index(columns: ['bea_mea_id'], name: 'idx_beat_measure')]
class Beat
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'bea_id')]
    private ?int $id = null;

    #[ORM\Column(length: 2, nullable: true, name: 'bea_pitch_step')]
    #[Groups(['music:detail'])]
    private ?string $pitchStep = null;

    #[ORM\Column(nullable: true, name: 'bea_pitch_octave')]
    #[Groups(['music:detail'])]
    private ?int $pitchOctave = null;

    #[ORM\Column(nullable: true, name: 'bea_pitch_alter')]
    #[Groups(['music:detail'])]
    private ?float $pitchAlter = null;

    #[ORM\Column(name: 'bea_duration')]
    #[Groups(['music:detail'])]
    private ?int $duration;

    #[ORM\Column(length: 16, name: 'bea_type')]
    #[Groups(['music:detail'])]
    private ?string $type;

    #[ORM\Column(name: 'bea_dot')]
    #[Groups(['music:detail'])]
    private ?bool $dot = false;

    #[ORM\Column(nullable: true, name: 'bea_string')]
    #[Groups(['music:detail'])]
    private ?int $string = null;

    #[ORM\Column(nullable: true, name: 'bea_fret')]
    #[Groups(['music:detail'])]
    private ?int $fret = null;

    #[ORM\ManyToOne(inversedBy: 'beats')]
    #[ORM\JoinColumn(name: 'bea_mea_id', referencedColumnName: 'mea_id')]
    private ?Measure $measure = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getPitchStep(): ?string
    {
        return $this->pitchStep;
    }

    public function setPitchStep(?string $pitchStep): static
    {
        $this->pitchStep = $pitchStep;

        return $this;
    }

    public function getPitchOctave(): ?int
    {
        return $this->pitchOctave;
    }

    public function setPitchOctave(?int $pitchOctave): static
    {
        $this->pitchOctave = $pitchOctave;

        return $this;
    }

    public function getPitchAlter(): ?float
    {
        return $this->pitchAlter;
    }

    public function setPitchAlter(?float $pitchAlter): static
    {
        $this->pitchAlter = $pitchAlter;

        return $this;
    }

    public function getDuration(): int
    {
        return $this->duration;
    }

    public function setDuration(int $duration): static
    {
        $this->duration = $duration;

        return $this;
    }

    public function getType(): string
    {
        return $this->type;
    }

    public function setType(string $type): static
    {
        $this->type = $type;

        return $this;
    }

    public function isDot(): ?bool
    {
        return $this->dot;
    }

    public function setDot(?bool $dot): static
    {
        $this->dot = $dot;

        return $this;
    }

    public function getString(): ?int
    {
        return $this->string;
    }

    public function setString(?int $string): static
    {
        $this->string = $string;

        return $this;
    }

    public function getFret(): ?int
    {
        return $this->fret;
    }

    public function setFret(?int $fret): static
    {
        $this->fret = $fret;

        return $this;
    }

    public function getMeasure(): ?Measure
    {
        return $this->measure;
    }

    public function setMeasure(?Measure $measure): static
    {
        $this->measure = $measure;

        return $this;
    }
}
