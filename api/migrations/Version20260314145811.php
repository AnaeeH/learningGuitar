<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260314145811 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_beat_bea DROP CONSTRAINT fk_cf7b53d1494c47fd');
        $this->addSql('DROP INDEX idx_cf7b53d1494c47fd');
        $this->addSql('ALTER TABLE t_beat_bea RENAME COLUMN bea_measure_id TO bea_mea_id');
        $this->addSql('ALTER TABLE t_beat_bea ADD CONSTRAINT FK_CF7B53D116922956 FOREIGN KEY (bea_mea_id) REFERENCES t_measure_mea (mea_id) NOT DEFERRABLE');
        $this->addSql('CREATE INDEX IDX_CF7B53D116922956 ON t_beat_bea (bea_mea_id)');
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
        $this->addSql('ALTER TABLE t_beat_bea DROP CONSTRAINT FK_CF7B53D116922956');
        $this->addSql('DROP INDEX IDX_CF7B53D116922956');
        $this->addSql('ALTER TABLE t_beat_bea RENAME COLUMN bea_mea_id TO bea_measure_id');
        $this->addSql('ALTER TABLE t_beat_bea ADD CONSTRAINT fk_cf7b53d1494c47fd FOREIGN KEY (bea_measure_id) REFERENCES t_measure_mea (mea_id) NOT DEFERRABLE INITIALLY IMMEDIATE');
        $this->addSql('CREATE INDEX idx_cf7b53d1494c47fd ON t_beat_bea (bea_measure_id)');
    }
}
