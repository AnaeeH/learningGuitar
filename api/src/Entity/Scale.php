<?php

namespace App\Entity;

use App\Repository\ScaleRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
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
    'note' => 'exact',
    'name' => 'partial',
    'type' => 'exact',
])]
#[ApiResource(
    paginationEnabled: false,
    normalizationContext: ['groups' => ['scale:detail']],
    operations: [
        new GetCollection(uriTemplate: '/scales'),
        new Get(uriTemplate: '/scale/{id}'),
        new Post(uriTemplate: '/scale'),
        new Patch(uriTemplate: '/scale/patch/{id}'),
        new Delete(uriTemplate: '/scale/delete/{id}'),
    ]
)]
#[ORM\Entity(repositoryClass: ScaleRepository::class)]
#[ORM\Table(name: 't_scale_scl')]
class Scale
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'scl_id')]
    #[Groups(['scale:detail'])]
    private ?int $id = null;

    #[ORM\Column(length: 64, name: 'scl_name')]
    #[Groups(['scale:detail'])]
    private ?string $name = null;

    #[ORM\Column(length: 32, name: 'scl_type')]
    #[Groups(['scale:detail'])]
    private ?string $type = null;

    #[ORM\ManyToOne(inversedBy: 'scales')]
    #[ORM\JoinColumn(nullable: false, name: 'scl_nte_id', referencedColumnName: 'nte_id')]
    #[Groups(['scale:detail'])]
    private ?Note $note = null;

    /**
     * @var Collection<int, ScalePosition>
     */
    #[ORM\OneToMany(
        targetEntity: ScalePosition::class,
        mappedBy: 'scale',
        cascade: ['persist', 'remove'],
        orphanRemoval: true
    )]
    #[Groups(['scale:detail'])]
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

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): static
    {
        $this->type = $type;
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

    /**
     * @return Collection<int, ScalePosition>
     */
    public function getPositions(): Collection
    {
        return $this->positions;
    }

    public function addPosition(ScalePosition $position): static
    {
        if (!$this->positions->contains($position)) {
            $this->positions->add($position);
            $position->setScale($this);
        }
        return $this;
    }

    public function removePosition(ScalePosition $position): static
    {
        if ($this->positions->removeElement($position)) {
            if ($position->getScale() === $this) {
                $position->setScale(null);
            }
        }
        return $this;
    }
}