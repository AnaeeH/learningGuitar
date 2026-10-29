<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20261029172554 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_slide BOOLEAN DEFAULT false NOT NULL');
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_palm_mute BOOLEAN DEFAULT false NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_slide');
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_palm_mute');
    }
}
