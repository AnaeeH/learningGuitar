<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260315124432 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE t_music_mus DROP CONSTRAINT fk_4033c754c169aa27');
        $this->addSql('DROP INDEX idx_4033c754c169aa27');
        $this->addSql('ALTER TABLE t_music_mus DROP mus_vdo_id');
        $this->addSql('ALTER TABLE t_video_vdo ADD vdo_mus_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE t_video_vdo ADD CONSTRAINT FK_2528368A0D60623 FOREIGN KEY (vdo_mus_id) REFERENCES t_music_mus (mus_id) NOT DEFERRABLE');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_2528368A0D60623 ON t_video_vdo (vdo_mus_id)');
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
        $this->addSql('ALTER TABLE t_music_mus ADD mus_vdo_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE t_music_mus ADD CONSTRAINT fk_4033c754c169aa27 FOREIGN KEY (mus_vdo_id) REFERENCES t_video_vdo (vdo_id) ON DELETE CASCADE NOT DEFERRABLE INITIALLY IMMEDIATE');
        $this->addSql('CREATE INDEX idx_4033c754c169aa27 ON t_music_mus (mus_vdo_id)');
        $this->addSql('ALTER TABLE t_video_vdo DROP CONSTRAINT FK_2528368A0D60623');
        $this->addSql('DROP INDEX UNIQ_2528368A0D60623');
        $this->addSql('ALTER TABLE t_video_vdo DROP vdo_mus_id');
    }
}
