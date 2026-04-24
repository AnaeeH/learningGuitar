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

    #[ORM\Column(type: 'text', name: 'chr_diagram')]
    #[Groups(['chord:detail'])]
    private ?string $diagram = null;

    #[ORM\Column(type: "boolean", name: 'chr_ismajor', options: ["default" => true])]
    #[Groups(['chord:detail'])]
    private bool $isMajor = true;

    #[ORM\ManyToOne(inversedBy: 'chords')]
    #[ORM\JoinColumn(nullable: false, name: 'chr_nte_id', referencedColumnName: 'nte_id')]
    #[Groups(['chord:detail'])]
    private ?Note $note = null;

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

    public function getDiagram(): ?string
    {
        return $this->diagram;
    }

    public function setDiagram(string $diagram): static
    {
        $this->diagram = $diagram;
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
}