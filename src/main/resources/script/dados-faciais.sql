-- Script SQLite para criação da tabela de dados faciais
-- Execute este script usando: sqlite3 penas_alternativas.db < dados-faciais.sql

-- Criação da tabela DadosFaciais
CREATE TABLE IF NOT EXISTS DadosFaciais (
    id_dados_faciais INTEGER PRIMARY KEY AUTOINCREMENT,
    fk_usuario_id_usuario INTEGER NOT NULL,
    imagem_rosto BLOB,
    descritores_faciais TEXT,
    data_cadastro TEXT NOT NULL DEFAULT (date('now')),
    data_atualizacao TEXT NOT NULL DEFAULT (date('now')),
    ativo INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario)
);

-- Criação de índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_dados_faciais_usuario ON DadosFaciais(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_dados_faciais_ativo ON DadosFaciais(ativo);
