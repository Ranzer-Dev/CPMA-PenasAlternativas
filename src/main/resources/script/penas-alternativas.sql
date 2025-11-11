-- Script SQLite para criação do banco de dados PENAS_ALTERNATIVAS
-- Execute este script usando: sqlite3 penas_alternativas.db < penas-alternativas.sql

-- Tabela Administrador
CREATE TABLE IF NOT EXISTS Administrador (
    id_admin INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    cpf TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    nivel_permissao INTEGER NOT NULL,
    pergunta_secreta TEXT,
    resposta_secreta TEXT,
    criado_em TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Tabela tipoDeInstituição
CREATE TABLE IF NOT EXISTS tipoDeInstituição (
    id_tipo INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo TEXT,
    criado_em TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Tabela Instituicao
CREATE TABLE IF NOT EXISTS Instituicao (
    id_instituicao INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    endereco TEXT,
    cidade TEXT,
    uf TEXT,
    bairro TEXT,
    cep TEXT,
    responsavel TEXT,
    telefone TEXT,
    tipo INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tipo) REFERENCES tipoDeInstituição(id_tipo)
);

-- Tabela Usuario
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL,
    nome TEXT NOT NULL,
    cpf TEXT NOT NULL UNIQUE,
    data_nascimento TEXT,
    endereco TEXT,
    bairro TEXT,
    cidade TEXT,
    cep TEXT,
    uf TEXT,
    nacionalidade TEXT,
    criado_em TEXT NOT NULL DEFAULT (datetime('now')),
    foto TEXT,
    observacao TEXT,
    telefone TEXT,
    fk_administrador_id_admin INTEGER NOT NULL,
    FOREIGN KEY (fk_administrador_id_admin) REFERENCES Administrador(id_admin)
);

-- Tabela Pena
CREATE TABLE IF NOT EXISTS Pena (
    id_pena INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_pena TEXT NOT NULL,
    data_inicio TEXT NOT NULL,
    data_termino TEXT,
    descricao TEXT,
    dias_semana_e_horarios_disponivel TEXT,
    atividades_acordadas TEXT,
    horas_semanais INTEGER NOT NULL,
    tempo_pena INTEGER NOT NULL,
    horas_totais INTEGER NOT NULL,
    fk_usuario_id_usuario INTEGER NOT NULL,
    fk_instituicao_id_instituicao INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Tabela RegistroDeTrabalho
CREATE TABLE IF NOT EXISTS RegistroDeTrabalho (
    id_registro INTEGER PRIMARY KEY AUTOINCREMENT,
    data_trabalho TEXT NOT NULL,
    horas_cumpridas REAL NOT NULL,
    atividades TEXT,
    horario_inicio TEXT,
    horario_almoco TEXT,
    horario_volta TEXT,
    horario_saida TEXT,
    fk_pena_id_pena INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (fk_pena_id_pena) REFERENCES Pena(id_pena) ON DELETE CASCADE
);

-- Tabela disponibilidade_instituicao
CREATE TABLE IF NOT EXISTS disponibilidade_instituicao (
    id_disponibilidade INTEGER PRIMARY KEY AUTOINCREMENT,
    dia_semana TEXT,
    hora_inicio_1 TEXT,
    hora_fim_1 TEXT,
    hora_inicio_2 TEXT,
    hora_fim_2 TEXT,
    fk_instituicao_id_instituicao INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_usuario_cpf ON Usuario(cpf);
CREATE INDEX IF NOT EXISTS idx_administrador_cpf ON Administrador(cpf);
CREATE INDEX IF NOT EXISTS idx_pena_usuario ON Pena(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_pena_instituicao ON Pena(fk_instituicao_id_instituicao);
CREATE INDEX IF NOT EXISTS idx_registro_pena ON RegistroDeTrabalho(fk_pena_id_pena);
CREATE INDEX IF NOT EXISTS idx_disponibilidade_instituicao ON disponibilidade_instituicao(fk_instituicao_id_instituicao);
