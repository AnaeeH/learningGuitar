<?php

namespace App\Entity;

use App\Repository\TNoteNTERepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TNoteNTERepository::class)]
class TNoteNTE
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 8)]
    private ?string $NTE_Name = null;

    #[ORM\Column(length: 64)]
    private ?string $NTE_Label = null;

    #[ORM\OneToMany(targetEntity: TChordCHR::class, mappedBy: 'CHR_NTE_Id')]
    private Collection $chords;

    public function __construct()
    {
        $this->chords = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNTEName(): ?string
    {
        return $this->NTE_Name;
    }

    public function setNTEName(string $NTE_Name): static
    {
        $this->NTE_Name = $NTE_Name;

        return $this;
    }

    public function getNTELabel(): ?string
    {
        return $this->NTE_Label;
    }

    public function setNTELabel(string $NTE_Label): static
    {
        $this->NTE_Label = $NTE_Label;

        return $this;
    }

    /**
     * @return Collection<int, TChordCHR>
     */
    public function getChords(): Collection
    {
        return $this->chords;
    }

    public function addChord(TChordCHR $chord): static
    {
        if (!$this->chords->contains($chord)) {
            $this->chords->add($chord);
            $chord->setCHRNTEId($this);
        }

        return $this;
    }

    public function removeChord(TChordCHR $chord): static
    {
        if ($this->chords->removeElement($chord)) {
            // set the owning side to null (unless already changed)
            if ($chord->getCHRNTEId() === $this) {
                $chord->setCHRNTEId(null);
            }
        }

        return $this;
    }
}
