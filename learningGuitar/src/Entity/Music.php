<?php

namespace App\Entity;

use App\Repository\MusicRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use ApiPlatform\Metadata\ApiFilter;
use ApiPlatform\Doctrine\Orm\Filter\SearchFilter;
use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\Delete;
use ApiPlatform\Metadata\GetCollection;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\Post;
use ApiPlatform\Metadata\Patch;
use App\State\MusicProcessor;
use App\State\MusicDeleteProcessor;
use Vich\UploaderBundle\Mapping\Attribute as Vich;
use Symfony\Component\Serializer\Attribute\Groups;
use Symfony\Component\HttpFoundation\File\File;

#[ApiFilter(SearchFilter::class, properties: [
    'title'    => 'ipartial', // /api/musics?title=SanFran
])]
#[ApiResource(
    paginationEnabled: false,
    normalizationContext: ['groups' => ['music:read']],
    operations: [
        new GetCollection(uriTemplate: '/musics'),
        new Get(uriTemplate: '/music/{id}'),
        new Post(
            uriTemplate: '/music',
            inputFormats: ['multipart' => ['multipart/form-data']],
            processor: MusicProcessor::class,
            deserialize: false,
            denormalizationContext: ['groups' => ['music:write']]
        ),
        new Patch(
            uriTemplate: '/music/{id}/favorite',
            denormalizationContext: ['groups' => ['music:favorite']]
        ),
        new Patch(
            uriTemplate: '/music/{id}/comment',
            denormalizationContext: ['groups' => ['music:comment']]
        ),
        new Delete(
            uriTemplate: '/music/{id}/delete',
            processor: MusicDeleteProcessor::class,
        )
    ]
)]
#[ORM\Entity(repositoryClass: MusicRepository::class)]
#[Vich\Uploadable]
#[ORM\Table(name: 't_music_mus')]
class Music
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'mus_id')]
    #[Groups(['music:read'])]
    private ?int $id = null;

    #[ORM\Column(length: 150, nullable: true, name: 'mus_title')]
    #[Groups(['music:read'])]
    private ?string $title = null;

    #[ORM\Column(nullable: true, name: 'mus_tempo')]
    #[Groups(['music:read'])]
    private ?int $tempo = null;

    #[ORM\Column(length: 8, nullable: true, name: 'mus_time_signature')]
    #[Groups(['music:read'])]
    private ?string $time_signature = null;

    #[ORM\Column(length: 16, nullable: true, name: 'mus_key_signature')]
    #[Groups(['music:read'])]
    private ?string $key_signature = null;

    #[ORM\Column(name: 'mus_favorite')]
    #[Groups(['music:favorite', 'music:read'])]
    private bool $favorite = false;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_comment')]
    #[Groups(['music:comment', 'music:read'])]
    private ?string $comment = null;

    private ?string $xmlFileName = null;

    #[Vich\UploadableField(mapping: 'music_xml', fileNameProperty: 'xmlFileName')]
    #[Groups(['music:write'])]
    public ?File $xmlFile = null;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_audiopath')]
    #[Groups(['music:read'])]
    private ?string $audioPath = null;

    #[Vich\UploadableField(mapping: 'music_audio', fileNameProperty: 'audioPath')]
    #[Groups(['music:write'])]
    public ?File $audioFile = null;

    /**
     * @var Collection<int, Measure>
     */
    #[ORM\OneToMany(
        targetEntity: Measure::class,
        mappedBy: 'music',
        cascade: ['remove'],
        orphanRemoval: true
    )]
    #[Groups(['music:read'])]
    private Collection $measures;

    #[ORM\OneToOne(mappedBy: 'music', cascade: ['persist', 'remove'])]
    #[Groups(['music:read', 'music:write'])]
    private ?Video $video = null;

    public function __construct()
    {
        $this->measures = new ArrayCollection();
    }


    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(?string $title): static
    {
        $this->title = $title;

        return $this;
    }

    public function getTempo(): ?int
    {
        return $this->tempo;
    }

    public function setTempo(?int $tempo): static
    {
        $this->tempo = $tempo;

        return $this;
    }

    public function getTimeSignature(): ?string
    {
        return $this->time_signature;
    }

    public function setTimeSignature(?string $time_signature): static
    {
        $this->time_signature = $time_signature;

        return $this;
    }

    public function getKeySignature(): ?string
    {
        return $this->key_signature;
    }

    public function setKeySignature(?string $key_signature): static
    {
        $this->key_signature = $key_signature;

        return $this;
    }

    public function getXmlFileName(): ?string
    {
        return $this->xmlFileName;
    }

    public function setXmlFileName(?string $xmlFileName): static
    {
        $this->xmlFileName = $xmlFileName;
        return $this;
    }

    public function getAudioPath(): ?string
    {
        return $this->audioPath;
    }

    public function setAudioPath(?string $audioPath): static
    {
        $this->audioPath = $audioPath;

        return $this;
    }

    public function isFavorite(): ?bool
    {
        return $this->favorite;
    }

    public function setFavorite(bool $favorite): static
    {
        $this->favorite = $favorite;

        return $this;
    }

    public function getComment(): ?string
    {
        return $this->comment;
    }

    public function setComment(?string $comment): static
    {
        $this->comment = $comment;

        return $this;
    }

    /**
     * @return Collection<int, Measure>
     */
    public function getMeasures(): Collection
    {
        return $this->measures;
    }

    public function addMeasure(Measure $measure): static
    {
        if (!$this->measures->contains($measure)) {
            $this->measures->add($measure);
            $measure->setMusic($this);
        }

        return $this;
    }

    public function removeMeasure(Measure $measure): static
    {
        if ($this->measures->removeElement($measure)) {
            // set the owning side to null (unless already changed)
            if ($measure->getMusic() === $this) {
                $measure->setMusic(null);
            }
        }

        return $this;
    }

    public function getVideo(): ?Video
    {
        return $this->video;
    }

    public function setVideo(?Video $video): static
    {
        $this->video = $video;

        return $this;
    }
}
