<?php

namespace App\Entity;

use App\Repository\BeatRepository;
use Doctrine\ORM\Mapping as ORM;
use phpDocumentor\Reflection\Types\Boolean;
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

    #[ORM\Column(name: 'bea_is_rest', options: ['default' => false])]
    #[Groups(['music:partition', 'music:tablature'])]
    private bool $isRest = false;

    #[ORM\Column(name: 'bea_position')]
    #[Groups(['music:partition', 'music:tablature'])]
    private int $position = 0;

    #[ORM\Column(length: 2, nullable: true, name: 'bea_pitch_step')]
    #[Groups(['music:partition'])]
    private ?string $pitchStep = null;

    #[ORM\Column(nullable: true, name: 'bea_pitch_octave')]
    #[Groups(['music:partition'])]
    private ?int $pitchOctave = null;

    #[ORM\Column(nullable: true, name: 'bea_pitch_alter')]
    #[Groups(['music:partition'])]
    private ?float $pitchAlter = null;

    #[ORM\Column(name: 'bea_duration')]
    #[Groups(['music:partition', 'music:tablature'])]
    private ?int $duration;

    #[ORM\Column(length: 16, name: 'bea_type')]
    #[Groups(['music:partition', 'music:tablature'])]
    private ?string $type;

    #[ORM\Column(name: 'bea_dot')]
    #[Groups(['music:partition', 'music:tablature'])]
    private ?bool $dot = false;

    #[ORM\Column(nullable: true, name: 'bea_string')]
    #[Groups(['music:tablature'])]
    private ?int $string = null;

    #[ORM\Column(nullable: true, name: 'bea_fret')]
    #[Groups(['music:tablature'])]
    private ?int $fret = null;

    #[ORM\Column(length: 8, nullable: true, name: 'bea_harmony_text')]
    #[Groups(['music:partition', 'music:tablature'])]
    private ?string $harmonyText = null;

    #[ORM\Column(length: 8, nullable: true, name: 'bea_strum_direction')]
    #[Groups(['music:partition', 'music:tablature'])]
    private ?string $strumDirection = null;

    #[ORM\ManyToOne(inversedBy: 'beats')]
    #[ORM\JoinColumn(name: 'bea_mea_id', referencedColumnName: 'mea_id')]
    private ?Measure $measure = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getIsRest(): ?bool
    {
        return $this->isRest;
    }

    public function setIsRest(?bool $isRest): static
    {
        $this->isRest = $isRest;

        return $this;
    }

    public function getPosition(): ?int
    {
        return $this->position;
    }

    public function setPosition(?int $position): static
    {
        $this->position = $position;

        return $this;
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

    public function getHarmonyText(): ?String
    {
        return $this->harmonyText;
    }

    public function setHarmonyText(?String $harmonyText): static
    {
        $this->harmonyText = $harmonyText;

        return $this;
    }

    public function getStrumDirection(): ?String
    {
        return $this->strumDirection;
    }

    public function setStrumDirection(?String $strumDirection): static
    {
        $this->strumDirection = $strumDirection;

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
