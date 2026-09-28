<?php

namespace App\Entity;

use App\Repository\ChordRepository;
use Doctrine\ORM\Mapping as ORM;
use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\GetCollection;
use ApiPlatform\Metadata\Post;
use ApiPlatform\Metadata\Patch;
use ApiPlatform\Metadata\Delete;
use ApiPlatform\Doctrine\Orm\Filter\SearchFilter;
use ApiPlatform\Metadata\ApiFilter;
use Doctrine\Common\Collections\Collection;
use Doctrine\Common\Collections\ArrayCollection;
use Symfony\Component\Serializer\Annotation\Groups;

#[ApiFilter(SearchFilter::class, properties: [
    'note'    => 'exact',   // /api/chords?note=/api/notes/1
    'name'    => 'partial', // /api/chords?name=Am
    'isMajor' => 'exact',   // /api/chords?isMajor=true
])]
#[ApiResource(
    paginationEnabled: false,
    normalizationContext: ['groups' => ['chord:detail']],
    operations: [
        new GetCollection(uriTemplate: '/chords'),
        new Get(uriTemplate: '/chord/{id}'),
        new Post(uriTemplate: '/chord'),
        new Patch(uriTemplate: '/chord/patch/{id}'),
        new Delete(uriTemplate: '/chord/delete/{id}'),
    ]
)]
#[ORM\Entity(repositoryClass: ChordRepository::class)]
#[ORM\Table(name: 't_chord_chr')]
class Chord
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'chr_id')]
    #[Groups(['chord:detail'])]
    private ?int $id = null;

    #[ORM\Column(length: 32, name: 'chr_name')]
    #[Groups(['chord:detail'])]
    private ?string $name = null;

    #[ORM\Column(type: "boolean", name: 'chr_ismajor', options: ["default" => true])]
    #[Groups(['chord:detail'])]
    private bool $isMajor = true;

    #[ORM\ManyToOne(inversedBy: 'chords')]
    #[ORM\JoinColumn(nullable: false, name: 'chr_nte_id', referencedColumnName: 'nte_id')]
    #[Groups(['chord:detail'])]
    private ?Note $note = null;

    #[ORM\Column(nullable: true, name: 'chr_barre_fret')]
    #[Groups(['chord:detail'])]
    private ?int $barreFret = null;

    #[ORM\Column(nullable: true, name: 'chr_barre_from_string')]
    #[Groups(['chord:detail'])]
    private ?int $barreFromString = null;

    #[ORM\Column(nullable: true, name: 'chr_barre_to_string')]
    #[Groups(['chord:detail'])]
    private ?int $barreToString = null;

    #[ORM\Column(type: 'simple_array', nullable: true, name: 'chr_muted_strings')]
    #[Groups(['chord:detail'])]
    private ?array $mutedStrings = null;

    #[ORM\OneToMany(
        targetEntity: ChordPosition::class,
        mappedBy: 'chord',
        cascade: ['persist', 'remove'],
        orphanRemoval: true
    )]
    #[Groups(['chord:detail'])]
    private Collection $positions;

    public function __construct()
    {
        $this->positions = new ArrayCollection();
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

    public function getIsMajor(): bool
    {
        return $this->isMajor;
    }

    public function setIsMajor(bool $isMajor): static
    {
        $this->isMajor = $isMajor;
        return $this;
    }

    public function getNote(): ?Note
    {
        return $this->note;
    }

    public function setNote(?Note $note): static
    {
        $this->note = $note;
        return $this;
    }

    public function getBarreFret(): ?int
    {
        return $this->barreFret;
    }

    public function setBarreFret(?int $barreFret): static
    {
        $this->barreFret = $barreFret;
        return $this;
    }

    public function getBarreFromString(): ?int
    {
        return $this->barreFromString;
    }

    public function setBarreFromString(?int $barreFromString): static
    {
        $this->barreFromString = $barreFromString;
        return $this;
    }

    public function getBarreToString(): ?int
    {
        return $this->barreToString;
    }

    public function setBarreToString(?int $barreToString): static
    {
        $this->barreToString = $barreToString;
        return $this;
    }

    public function getMutedStrings(): ?array
    {
        return $this->mutedStrings;
    }

    public function setMutedStrings(?array $mutedStrings): static
    {
        $this->mutedStrings = $mutedStrings;
        return $this;
    }

    /**
     * @return Collection<int, ChordPosition>
     */
    public function getPositions(): Collection
    {
        return $this->positions;
    }

    public function addPosition(ChordPosition $position): static
    {
        if (!$this->positions->contains($position)) {
            $this->positions->add($position);
            $position->setChord($this);
        }
        return $this;
    }

    public function removePosition(ChordPosition $position): static
    {
        if ($this->positions->removeElement($position)) {
            if ($position->getChord() === $this) {
                $position->setChord(null);
            }
        }
        return $this;
    }
}