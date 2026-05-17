-- Migração: múltiplas instituições por pena
-- Uso: sqlite3 penas_alternativas.db < script/migracao-multiplas-instituicoes.sql

CREATE TABLE IF NOT EXISTS pena_instituicao (
    fk_pena_id_pena INTEGER NOT NULL,
    fk_instituicao_id_instituicao INTEGER NOT NULL,
    PRIMARY KEY (fk_pena_id_pena, fk_instituicao_id_instituicao),
    FOREIGN KEY (fk_pena_id_pena) REFERENCES Pena(id_pena) ON DELETE CASCADE,
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

ALTER TABLE RegistroDeTrabalho ADD COLUMN fk_instituicao_id_instituicao INTEGER;

INSERT OR IGNORE INTO pena_instituicao (fk_pena_id_pena, fk_instituicao_id_instituicao)
SELECT id_pena, fk_instituicao_id_instituicao FROM Pena;

UPDATE RegistroDeTrabalho
SET fk_instituicao_id_instituicao = (
    SELECT fk_instituicao_id_instituicao FROM Pena WHERE Pena.id_pena = RegistroDeTrabalho.fk_pena_id_pena
)
WHERE fk_instituicao_id_instituicao IS NULL;

CREATE INDEX IF NOT EXISTS idx_pena_instituicao_pena ON pena_instituicao(fk_pena_id_pena);
CREATE INDEX IF NOT EXISTS idx_registro_instituicao ON RegistroDeTrabalho(fk_instituicao_id_instituicao);
