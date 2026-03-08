<?php

namespace App\Entity;

use App\Repository\NoteRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\GetCollection;

#[ApiResource(
    paginationEnabled: false,
    operations: [
        new GetCollection(uriTemplate: '/notes'),
        new Get(uriTemplate: '/notes/{id}'),
    ]
)]
#[ORM\Entity(repositoryClass: NoteRepository::class)]
#[ORM\Table(name: 't_note_nte')]
class Note
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'nte_id')]
    private ?int $id = null;

    #[ORM\Column(length: 8, name: 'nte_name')]
    private ?string $name = null;

    #[ORM\Column(length: 64, name: 'nte_label')]
    private ?string $label = null;

    #[ORM\OneToMany(targetEntity: Chord::class, mappedBy: 'note')]
    private Collection $chords;

    public function __construct()
    {
        $this->chords = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): static
    {
        $this->name = $name;
        return $this;
    }

    public function getLabel(): ?string
    {
        return $this->label;
    }

    public function setLabel(string $label): static
    {
        $this->label = $label;
        return $this;
    }

    public function getChords(): Collection
    {
        return $this->chords;
    }

    public function addChord(Chord $chord): static
    {
        if (!$this->chords->contains($chord)) {
            $this->chords->add($chord);
            $chord->setNote($this);
        }
        return $this;
    }

    public function removeChord(Chord $chord): static
    {
        if ($this->chords->removeElement($chord)) {
            if ($chord->getNote() === $this) {
                $chord->setNote(null);
            }
        }
        return $this;
    }
}
