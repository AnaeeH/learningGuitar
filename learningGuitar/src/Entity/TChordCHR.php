<?php

namespace App\Entity;

use App\Repository\TChordCHRRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TChordCHRRepository::class)]
class TChordCHR
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 32)]
    private ?string $CHR_Name = null;

    #[ORM\Column(length: 255)]
    private ?string $CHR_Diagram = null;

    #[ORM\Column(type: "boolean", options: ["default" => true])]
    private bool $CHR_IsMajor = true;

    #[ORM\ManyToOne(inversedBy: 'chords')]
    #[ORM\JoinColumn(nullable: false)]
    private ?TNoteNTE $CHR_NTE_Id = null;
    
    public function getId(): ?int
    {
        return $this->id;
    }

    public function getCHRName(): ?string
    {
        return $this->CHR_Name;
    }

    public function setCHRName(string $CHR_Name): static
    {
        $this->CHR_Name = $CHR_Name;

        return $this;
    }

    public function getCHRDiagram(): ?string
    {
        return $this->CHR_Diagram;
    }

    public function getCHRIsMajor(): ?bool
    {
        return $this->CHR_IsMajor;
    }

    public function setCHRDiagram(string $CHR_Diagram): static
    {
        $this->CHR_Diagram = $CHR_Diagram;

        return $this;
    }

    public function getCHRNTEId(): ?TNoteNTE
    {
        return $this->CHR_NTE_Id;
    }

    public function setCHRNTEId(?TNoteNTE $CHR_NTE_Id): static
    {
        $this->CHR_NTE_Id = $CHR_NTE_Id;

        return $this;
    }
}
