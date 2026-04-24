<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260412161016 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_music_mus ADD mus_artist VARCHAR(32) DEFAULT NULL');
        $this->addSql('ALTER TABLE t_music_mus ADD mus_riff BOOLEAN NOT NULL');
        $this->addSql('ALTER TABLE t_music_mus ADD mus_status VARCHAR(16) NOT NULL');
        $this->addSql('ALTER TABLE t_music_mus ADD mus_difficulty VARCHAR(16) NOT NULL');
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
        $this->addSql('ALTER TABLE t_music_mus DROP mus_artist');
        $this->addSql('ALTER TABLE t_music_mus DROP mus_riff');
        $this->addSql('ALTER TABLE t_music_mus DROP mus_status');
        $this->addSql('ALTER TABLE t_music_mus DROP mus_difficulty');
    }
}
