<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260315104916 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_music_mus DROP CONSTRAINT fk_4033c754c169aa27');
        $this->addSql('ALTER TABLE t_music_mus ADD CONSTRAINT FK_4033C754C169AA27 FOREIGN KEY (mus_vdo_id) REFERENCES t_video_vdo (vdo_id) ON DELETE CASCADE NOT DEFERRABLE');
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
        $this->addSql('ALTER TABLE t_music_mus DROP CONSTRAINT FK_4033C754C169AA27');
        $this->addSql('ALTER TABLE t_music_mus ADD CONSTRAINT fk_4033c754c169aa27 FOREIGN KEY (mus_vdo_id) REFERENCES t_video_vdo (vdo_id) NOT DEFERRABLE INITIALLY IMMEDIATE');
    }
}
