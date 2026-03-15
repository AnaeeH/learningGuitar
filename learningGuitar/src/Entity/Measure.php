<?php

namespace App\Entity;

use App\Entity\Beat;
use App\Repository\MeasureRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: MeasureRepository::class)]
#[ORM\Table(name: 't_measure_mea')]
#[ORM\Index(columns: ['mea_mus_id'], name: 'idx_measure_music')]
class Measure
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'mea_id')]
    private ?int $id = null;

    #[ORM\Column(name: 'mea_numero')]
    #[Groups(['music:detail'])]
    private ?int $numero = null;

    #[ORM\Column(nullable: true, name: 'mea_tempo')]
    #[Groups(['music:detail'])]
    private ?int $tempo = null;

    #[ORM\Column(length: 32, nullable: true, name: 'mea_time_signature')]
    #[Groups(['music:detail'])]
    private ?string $timeSignature = null;

    #[ORM\ManyToOne(inversedBy: 'measures')]
    #[ORM\JoinColumn(name: 'mea_mus_id', referencedColumnName: 'mus_id')]
    private ?Music $music = null;

    /**
     * @var Collection<int, Beat>
     */
    #[ORM\OneToMany(
        targetEntity: Beat::class,
        mappedBy: 'measure',
        cascade: ['remove'],
        orphanRemoval: true
    )]
    #[Groups(['music:detail'])]
    private Collection $beats;

    public function __construct()
    {
        $this->beats = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNumero(): ?int
    {
        return $this->numero;
    }

    public function setNumero(int $numero): static
    {
        $this->numero = $numero;

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
        return $this->timeSignature;
    }

    public function setTimeSignature(?string $timeSignature): static
    {
        $this->timeSignature = $timeSignature;

        return $this;
    }

    public function getMusic(): ?Music
    {
        return $this->music;
    }

    public function setMusic(?Music $music): static
    {
        $this->music = $music;

        return $this;
    }

    /**
     * @return Collection<int, Beat>
     */
    public function getBeats(): Collection
    {
        return $this->beats;
    }

    public function addBeat(Beat $beat): static
    {
        if (!$this->beats->contains($beat)) {
            $this->beats->add($beat);
            $beat->setMeasure($this);
        }

        return $this;
    }

    public function removeBeat(Beat $beat): static
    {
        if ($this->beats->removeElement($beat)) {
            // set the owning side to null (unless already changed)
            if ($beat->getMeasure() === $this) {
                $beat->setMeasure(null);
            }
        }

        return $this;
    }
}
