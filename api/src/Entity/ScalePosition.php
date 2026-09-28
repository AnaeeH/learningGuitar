<?php

namespace App\Entity;

use App\Repository\ScalePositionRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: ScalePositionRepository::class)]
#[ORM\Table(name: 't_scale_position_spo')]
#[ORM\UniqueConstraint(
    name: 'uniq_scale_position_number',
    columns: ['spo_scl_id', 'spo_position_number']
)]
class ScalePosition
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'spo_id')]
    #[Groups(['scale:detail'])]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'positions')]
    #[ORM\JoinColumn(
        nullable: false,
        name: 'spo_scl_id',
        referencedColumnName: 'scl_id',
        onDelete: 'CASCADE'
    )]
    private ?Scale $scale = null;

    #[ORM\Column(name: 'spo_position_number')]
    #[Groups(['scale:detail'])]
    private ?int $positionNumber = null;

    #[ORM\Column(name: 'spo_start_fret')]
    #[Groups(['scale:detail'])]
    private ?int $startFret = null;

    /**
     * @var Collection<int, ScaleNote>
     */
    #[ORM\OneToMany(
        targetEntity: ScaleNote::class,
        mappedBy: 'position',
        cascade: ['persist', 'remove'],
        orphanRemoval: true
    )]
    #[Groups(['scale:detail'])]
    private Collection $notes;

    public function __construct()
    {
        $this->notes = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getScale(): ?Scale
    {
        return $this->scale;
    }

    public function setScale(?Scale $scale): static
    {
        $this->scale = $scale;
        return $this;
    }

    public function getPositionNumber(): ?int
    {
        return $this->positionNumber;
    }

    public function setPositionNumber(int $positionNumber): static
    {
        $this->positionNumber = $positionNumber;
        return $this;
    }

    public function getStartFret(): ?int
    {
        return $this->startFret;
    }

    public function setStartFret(int $startFret): static
    {
        $this->startFret = $startFret;
        return $this;
    }

    /**
     * @return Collection<int, ScaleNote>
     */
    public function getNotes(): Collection
    {
        return $this->notes;
    }

    public function addNote(ScaleNote $note): static
    {
        if (!$this->notes->contains($note)) {
            $this->notes->add($note);
            $note->setPosition($this);
        }
        return $this;
    }

    public function removeNote(ScaleNote $note): static
    {
        if ($this->notes->removeElement($note)) {
            if ($note->getPosition() === $this) {
                $note->setPosition(null);
            }
        }
        return $this;
    }
}