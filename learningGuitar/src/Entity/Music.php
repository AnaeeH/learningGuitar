<?php

namespace App\Entity;

use App\Repository\MusicRepository;
use Doctrine\ORM\Mapping as ORM;
use ApiPlatform\Metadata\ApiFilter;
use ApiPlatform\Doctrine\Orm\Filter\SearchFilter;
use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\GetCollection;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\Post;
use ApiPlatform\Metadata\Patch;
use App\State\MusicProcessor;
use Vich\UploaderBundle\Mapping\Attribute as Vich;
use Symfony\Component\Serializer\Attribute\Groups;
use Symfony\Component\HttpFoundation\File\File;

#[ApiFilter(SearchFilter::class, properties: [
    'title'    => 'ipartial', // /api/musics?title=SanFran
])]
#[ApiResource(
    paginationEnabled: false,
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
    private ?int $id = null;

    #[ORM\Column(length: 150, nullable: true, name: 'mus_title')]
    private ?string $title = null;

    #[ORM\Column(nullable: true, name: 'mus_tempo')]
    private ?int $tempo = null;

    #[ORM\Column(length: 8, nullable: true, name: 'mus_time_signature')]
    private ?string $time_signature = null;

    #[ORM\Column(length: 16, nullable: true, name: 'mus_key_signature')]
    private ?string $key_signature = null;

    #[ORM\Column(name: 'mus_favorite')]
    #[Groups(['music:favorite'])]
    private bool $favorite = false;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_comment')]
    #[Groups(['music:comment'])] 
    private ?string $comment = null;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_xmlpath')]
    private ?string $xmlPath = null;

    #[Vich\UploadableField(mapping: 'music_xml', fileNameProperty: 'xmlPath')]
    #[Groups(['music:write'])] 
    public ?File $xmlFile = null;

    #[ORM\Column(length: 255, nullable: true, name: 'mus_audiopath')]
    private ?string $audioPath = null;

    #[Vich\UploadableField(mapping: 'music_audio', fileNameProperty: 'audioPath')]
    #[Groups(['music:write'])] 
    public ?File $audioFile = null;


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

    public function getXmlPath(): ?string
    {
        return $this->xmlPath;
    }

    public function setXmlPath(?string $xmlPath): static
    {
        $this->xmlPath = $xmlPath;

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
}
