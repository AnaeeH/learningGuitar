-- -----------------------------------------------------------------------------
-- TABLE : T_Note_NTE — The 12 musical notes
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_Note_NTE (
    NTE_Id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NTE_Name  VARCHAR(8)  NOT NULL UNIQUE,  -- Ex: A, C, D...
    NTE_Label VARCHAR(64) NOT NULL UNIQUE   -- Ex: la, do, ré...
);

-- -----------------------------------------------------------------------------
-- TABLE : T_Chord_CHR - The chords
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_Chord_CHR (
    CHR_Id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    CHR_Name    VARCHAR(32)  NOT NULL UNIQUE,  -- Ex: Am7, A
    CHR_Diagram VARCHAR(256) NOT NULL,         -- Path to the image ex: /img/chords/Dm7.png
    CHR_IsMajor BOOLEAN      NOT NULL DEFAULT TRUE,
    CHR_NTE_Id  INT          NOT NULL,
    CONSTRAINT FK_CHR_Note FOREIGN KEY (CHR_NTE_Id)
        REFERENCES T_Note_NTE(NTE_Id)
);

-- -----------------------------------------------------------------------------
-- DONNÉES : The 12 musical notes
-- -----------------------------------------------------------------------------
INSERT INTO T_Note_NTE (NTE_Name, NTE_Label) VALUES
    ('A', 'la'),
    ('B', 'si'),
    ('C', 'do'),
    ('D', 'ré'),
    ('E', 'mi'),
    ('F', 'fa'),
    ('G', 'sol');