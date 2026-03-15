<?php

namespace App\Entity;

use App\Repository\VideoRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: VideoRepository::class)]
#[ORM\Table(name: 't_video_vdo')]
class Video
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'vdo_id')]
    private ?int $id = null;

    #[ORM\Column(length: 20, name: 'vdo_video_id')]
    #[Groups(['music:read', 'music:write'])]
    private ?string $videoId = null;

    #[ORM\Column(name: 'vdo_start_sec')]
    #[Groups(['music:read', 'music:write'])]
    private ?float $startSec = null;

    #[ORM\OneToOne(inversedBy: 'video')]
    #[ORM\JoinColumn(name: 'vdo_mus_id', referencedColumnName: 'mus_id')]
    private ?Music $music = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getVideoId(): ?string
    {
        return $this->videoId;
    }

    public function setVideoId(string $videoId): static
    {
        $this->videoId = $videoId;

        return $this;
    }

    public function getStartSec(): ?float
    {
        return $this->startSec;
    }

    public function setStartSec(float $startSec): static
    {
        $this->startSec = $startSec;

        return $this;
    }

    public function getMusic(): ?Music
    {
        return $this->music;
    }

    public function setMusic(Music $music): static
    {
        $this->music = $music;

        return $this;
    }
}
