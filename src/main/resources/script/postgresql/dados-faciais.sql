-- Script PostgreSQL para criação da tabela de dados faciais

-- Criação da tabela DadosFaciais
CREATE TABLE IF NOT EXISTS DadosFaciais (
    id_dados_faciais SERIAL PRIMARY KEY,
    fk_usuario_id_usuario INTEGER NOT NULL,
    imagem_rosto BYTEA,
    descritores_faciais TEXT,
    data_cadastro TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'),
    data_atualizacao TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'),
    ativo INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario)
);

-- Criação de índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_dados_faciais_usuario ON DadosFaciais(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_dados_faciais_ativo ON DadosFaciais(ativo);
