<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260308153328 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('DROP SEQUENCE graphql.seq_schema_version CASCADE');
        $this->addSql('ALTER TABLE t_chord_chr DROP CONSTRAINT fk_43fdeb0695c8ab7');
        $this->addSql('ALTER TABLE t_chord_chr ALTER CHR_Diagram TYPE TEXT');
        $this->addSql('ALTER TABLE t_chord_chr ADD CONSTRAINT FK_43FDEB0695C8AB7 FOREIGN KEY (CHR_NTE_Id) REFERENCES T_Note_NTE (id) NOT DEFERRABLE');
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
        $this->addSql('CREATE SEQUENCE graphql.seq_schema_version INCREMENT BY 1 MINVALUE 1 START 1');
        $this->addSql('ALTER TABLE T_Chord_CHR DROP CONSTRAINT FK_43FDEB0695C8AB7');
        $this->addSql('ALTER TABLE T_Chord_CHR ALTER chr_diagram TYPE VARCHAR(255)');
        $this->addSql('ALTER TABLE T_Chord_CHR ADD CONSTRAINT fk_43fdeb0695c8ab7 FOREIGN KEY (chr_nte_id) REFERENCES t_note_nte (nte_id) NOT DEFERRABLE INITIALLY IMMEDIATE');
    }
}
