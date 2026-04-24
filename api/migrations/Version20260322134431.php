<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260322134431 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_is_rest BOOLEAN DEFAULT false NOT NULL');
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_position INT NOT NULL');
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_harmony_text VARCHAR(8) DEFAULT NULL');
        $this->addSql('ALTER TABLE t_beat_bea ADD bea_strum_direction VARCHAR(8) DEFAULT NULL');
        $this->addSql('ALTER INDEX idx_cf7b53d116922956 RENAME TO idx_beat_measure');
        $this->addSql('ALTER INDEX idx_a6a9a0a27d697a4d RENAME TO idx_measure_music');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE SCHEMA pgbouncer');
        $this->addSql('CREATE SCHEMA realtime');
        $this->addSql('CREATE SCHEMA extensions');
        $this->addSql('CREATE SCHEMA vault');
        $this->addSql('CREATE SCHEMA graphql_public');
        $this->addSql('CREATE SCHEMA graphql');
        $this->addSql('CREATE SCHEMA auth');
        $this->addSql('CREATE SCHEMA storage');
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_is_rest');
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_position');
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_harmony_text');
        $this->addSql('ALTER TABLE t_beat_bea DROP bea_strum_direction');
        $this->addSql('ALTER INDEX idx_beat_measure RENAME TO idx_cf7b53d116922956');
        $this->addSql('ALTER INDEX idx_measure_music RENAME TO idx_a6a9a0a27d697a4d');
    }
}
