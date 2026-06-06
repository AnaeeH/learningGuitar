<?php

namespace App\Entity;

use App\Repository\MusicRepository;
use App\Enum\MusicStatus;
use App\Enum\MusicDifficulty;
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
use ApiPlatform\Metadata\QueryParameter;
use App\State\MusicProcessor;
use App\State\MusicDeleteProcessor;
use App\State\MusicProvider;
use DateTime;
use Vich\UploaderBundle\Mapping\Attribute as Vich;
use Symfony\Component\Serializer\Attribute\Groups;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Constraints\Date;

#[ApiFilter(SearchFilter::class, properties: [
    'title'      => 'ipartial', // /api/musics?title=SanFran
    'favorite'   => 'exact',
    'riff'       => 'exact',
    'status'     => 'exact',
    'difficulty' => 'exact',
])]
#[ApiResource(
    paginationEnabled: true,
    paginationItemsPerPage: 20,
    normalizationContext: ['groups' => ['music:detail']],
    operations: [
        new GetCollection(
            uriTemplate: '/get/musics/title',
            name: 'get_title',
            provider: MusicProvider::class,
            normalizationContext: ['groups' => ['music:read']],
            parameters: [
                'status' => new QueryParameter(),
            ]
        ),
        new GetCollection(
            uriTemplate: '/get/musics/recent',
            name: 'get_recent',
            provider: MusicProvider::class,
            normalizationContext: ['groups' => ['music:read']],
            parameters: [
                'status' => new QueryParameter(),
            ]
        ),
        new Get(
            uriTemplate: '/get/music/last-played',
            name: 'get_last_played',
            provider: MusicProvider::class,
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Get(
            uriTemplate: '/get/music/{id}',
            normalizationContext: ['groups' => ['music:detail']]
        ),
        new Get(
            uriTemplate: '/get/music/{id}/chords',
            name: 'get_chords',
            provider: MusicProvider::class,
            normalizationContext: ['groups' => ['music:chords']]
        ),
        new Get(
            uriTemplate: '/get/music/{id}/partition',
            normalizationContext: ['groups' => ['music:detail', 'music:partition']]
        ),
        new Get(
            uriTemplate: '/get/music/{id}/tablature',
            normalizationContext: ['groups' => ['music:detail', 'music:tablature']]
        ),
        new Post(
            uriTemplate: '/post/music',
            inputFormats: ['multipart' => ['multipart/form-data']],
            processor: MusicProcessor::class,
            deserialize: false,
            denormalizationContext: ['groups' => ['music:write']]
        ),
        new Patch(
            uriTemplate: '/patch/music/{id}/favorite',
            denormalizationContext: ['groups' => ['music:favorite']],
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Patch(
            uriTemplate: '/patch/music/{id}/comment',
            denormalizationContext: ['groups' => ['music:comment']],
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Patch(
            uriTemplate: '/patch/music/{id}/progress',
            denormalizationContext: ['groups' => ['music:progress']],
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Patch(
            uriTemplate: '/patch/music/{id}/difficulty',
            denormalizationContext: ['groups' => ['music:difficulty']],
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Patch(
            uriTemplate: '/patch/music/{id}/last-played',
            denormalizationContext: ['groups' => ['music:last_played_at']],
            normalizationContext: ['groups' => ['music:read']]
        ),
        new Delete(
            uriTemplate: '/delete/music/{id}/delete',
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
    #[Groups(['music:read', 'music:detail'])]
    private ?int $id = null;

    #[ORM\Column(length: 150, nullable: true, name: 'mus_title')]
    #[Groups(['music:read', 'music:detail'])]
    private ?string $title = null;

    #[ORM\Column(length: 32, nullable: true, name: 'mus_artist')]
    #[Groups(['music:read', 'music:detail'])]
    private ?string $artist = null;

    #[ORM\Column(nullable: true, name: 'mus_tempo')]
    #[Groups(['music:detail'])]
    private ?int $tempo = null;

    #[ORM\Column(length: 8, nullable: true, name: 'mus_time_signature')]
    #[Groups(['music:detail'])]
    private ?string $time_signature = null;

    #[ORM\Column(length: 16, nullable: true, name: 'mus_key_signature')]
    #[Groups(['music:detail'])]
    private ?string $key_signature = null;

    #[ORM\Column(name: 'mus_favorite')]
    #[Groups(['music:read', 'music:favorite', 'music:detail'])]
    private bool $favorite = false;

    #[ORM\Column(name: 'mus_riff')]
    #[Groups(['music:write', 'music:detail'])]
    private bool $riff = false;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_comment')]
    #[Groups(['music:read', 'music:comment', 'music:detail'])]
    private ?string $comment = null;

    #[ORM\Column(type: 'smallint', nullable: false, name: 'mus_progress', options: ['default' => 0])]
    #[Assert\Range(min: 0, max: 100)]
    #[Groups(['music:read', 'music:progress', 'music:detail'])]
    private int $progress = 0;

    #[ORM\Column(type: 'string', length: 16, nullable: false, name: 'mus_difficulty', enumType: MusicDifficulty::class)]
    #[Groups(['music:read', 'music:detail', 'music:difficulty'])]
    private ?MusicDifficulty $difficulty = MusicDifficulty::EASY;

    private ?string $xmlFileName = null;

    #[Vich\UploadableField(mapping: 'music_xml', fileNameProperty: 'xmlFileName')]
    #[Groups(['music:write'])]
    public ?File $xmlFile = null;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_audiopath')]
    #[Groups(['music:detail'])]
    private ?string $audioPath = null;

    #[Vich\UploadableField(mapping: 'music_audio', fileNameProperty: 'audioPath')]
    #[Groups(['music:write'])]
    public ?File $audioFile = null;

    #[ORM\Column(nullable: true, name: 'mus_last_played_at')]
    #[Groups(['music:last_played_at'])]
    private ?DateTime $last_played_at;

    /**
     * @var Collection<int, Measure>
     */
    #[ORM\OneToMany(
        targetEntity: Measure::class,
        mappedBy: 'music',
        cascade: ['remove'],
        orphanRemoval: true
    )]
    #[Groups(['music:partition', 'music:tablature'])]
    private Collection $measures;

    #[ORM\OneToOne(mappedBy: 'music', cascade: ['persist', 'remove'])]
    #[Groups(['music:detail', 'music:write'])]
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

    public function getArtist(): ?string
    {
        return $this->artist;
    }

    public function setTitle(?string $title): static
    {
        $this->title = $title;

        return $this;
    }

    public function setArtist(?string $artist): static
    {
        $this->artist = $artist;

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

    public function getProgress(): ?int
    {
        return $this->progress;
    }

    public function setProgress(?int $progress): static
    {
        $this->progress = $progress;
        return $this;
    }

    public function getDifficulty(): ?MusicDifficulty
    {
        return $this->difficulty;
    }

    public function setDifficulty(?MusicDifficulty $difficulty): static
    {
        $this->difficulty = $difficulty;
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

    public function isRiff(): ?bool
    {
        return $this->riff;
    }

    public function setRiff(bool $riff): static
    {
        $this->riff = $riff;

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

    public function getLastPlayedAt(): ?DateTime
    {
        return $this->last_played_at;
    }

    public function setLastPlayedAt(?DateTime $last_played_at): static
    {
        $this->last_played_at = $last_played_at;

        return $this;
    }
}
