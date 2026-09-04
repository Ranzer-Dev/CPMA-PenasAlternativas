-- Script PostgreSQL para criação do banco de dados PENAS_ALTERNATIVAS

-- Tabela Administrador
CREATE TABLE IF NOT EXISTS Administrador (
    id_admin SERIAL PRIMARY KEY,
    nome TEXT NOT NULL,
    cpf TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    nivel_permissao INTEGER NOT NULL,
    pergunta_secreta TEXT,
    resposta_secreta TEXT,
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS')
);

-- Tabela tipoDeInstituição
CREATE TABLE IF NOT EXISTS tipoDeInstituição (
    id_tipo SERIAL PRIMARY KEY,
    tipo TEXT,
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS')
);

-- Tabela Instituicao
CREATE TABLE IF NOT EXISTS Instituicao (
    id_instituicao SERIAL PRIMARY KEY,
    nome TEXT NOT NULL,
    endereco TEXT,
    cidade TEXT,
    uf TEXT,
    bairro TEXT,
    cep TEXT,
    responsavel TEXT,
    telefone TEXT,
    tipo INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    FOREIGN KEY (tipo) REFERENCES tipoDeInstituição(id_tipo)
);

-- Tabela Usuario
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario SERIAL PRIMARY KEY,
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
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    foto TEXT,
    observacao TEXT,
    telefone TEXT,
    fk_administrador_id_admin INTEGER NOT NULL,
    FOREIGN KEY (fk_administrador_id_admin) REFERENCES Administrador(id_admin)
);

-- Tabela Pena
CREATE TABLE IF NOT EXISTS Pena (
    id_pena SERIAL PRIMARY KEY,
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
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Tabela RegistroDeTrabalho
CREATE TABLE IF NOT EXISTS RegistroDeTrabalho (
    id_registro SERIAL PRIMARY KEY,
    data_trabalho TEXT NOT NULL,
    horas_cumpridas REAL NOT NULL,
    atividades TEXT,
    horario_inicio TEXT,
    horario_almoco TEXT,
    horario_volta TEXT,
    horario_saida TEXT,
    fk_pena_id_pena INTEGER NOT NULL,
    fk_instituicao_id_instituicao INTEGER,
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    FOREIGN KEY (fk_pena_id_pena) REFERENCES Pena(id_pena) ON DELETE CASCADE,
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Vínculo N:N entre pena e instituições
CREATE TABLE IF NOT EXISTS pena_instituicao (
    fk_pena_id_pena INTEGER NOT NULL,
    fk_instituicao_id_instituicao INTEGER NOT NULL,
    PRIMARY KEY (fk_pena_id_pena, fk_instituicao_id_instituicao),
    FOREIGN KEY (fk_pena_id_pena) REFERENCES Pena(id_pena) ON DELETE CASCADE,
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Tabela disponibilidade_instituicao
CREATE TABLE IF NOT EXISTS disponibilidade_instituicao (
    id_disponibilidade SERIAL PRIMARY KEY,
    dia_semana TEXT,
    hora_inicio_1 TEXT,
    hora_fim_1 TEXT,
    hora_inicio_2 TEXT,
    hora_fim_2 TEXT,
    fk_instituicao_id_instituicao INTEGER NOT NULL,
    criado_em TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    FOREIGN KEY (fk_instituicao_id_instituicao) REFERENCES Instituicao(id_instituicao)
);

-- Códigos temporários para acesso do apenado ao totem
CREATE TABLE IF NOT EXISTS CodigoAcessoApenado (
    id_codigo_acesso SERIAL PRIMARY KEY,
    fk_usuario_id_usuario INTEGER NOT NULL,
    fk_admin_id_admin INTEGER NOT NULL,
    codigo TEXT NOT NULL,
    data_geracao TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    data_expiracao TEXT NOT NULL,
    data_uso TEXT,
    status TEXT NOT NULL DEFAULT 'ATIVO',
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (fk_admin_id_admin) REFERENCES Administrador(id_admin)
);

-- Log de auditoria das tentativas de acesso pelo totem
CREATE TABLE IF NOT EXISTS LogAcessoApenado (
    id_log SERIAL PRIMARY KEY,
    fk_usuario_id_usuario INTEGER,
    fk_codigo_acesso_id INTEGER,
    metodo TEXT NOT NULL,
    sucesso INTEGER NOT NULL DEFAULT 0,
    codigo_tentado TEXT,
    mensagem TEXT,
    data_hora TEXT NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
    FOREIGN KEY (fk_usuario_id_usuario) REFERENCES Usuario(id_usuario) ON DELETE SET NULL,
    FOREIGN KEY (fk_codigo_acesso_id) REFERENCES CodigoAcessoApenado(id_codigo_acesso) ON DELETE SET NULL
);

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_usuario_cpf ON Usuario(cpf);
CREATE INDEX IF NOT EXISTS idx_administrador_cpf ON Administrador(cpf);
CREATE INDEX IF NOT EXISTS idx_pena_usuario ON Pena(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_pena_instituicao ON Pena(fk_instituicao_id_instituicao);
CREATE INDEX IF NOT EXISTS idx_registro_pena ON RegistroDeTrabalho(fk_pena_id_pena);
CREATE INDEX IF NOT EXISTS idx_pena_instituicao_pena ON pena_instituicao(fk_pena_id_pena);
CREATE INDEX IF NOT EXISTS idx_registro_instituicao ON RegistroDeTrabalho(fk_instituicao_id_instituicao);
CREATE INDEX IF NOT EXISTS idx_disponibilidade_instituicao ON disponibilidade_instituicao(fk_instituicao_id_instituicao);
CREATE INDEX IF NOT EXISTS idx_codigo_acesso_codigo ON CodigoAcessoApenado(codigo);
CREATE INDEX IF NOT EXISTS idx_codigo_acesso_usuario ON CodigoAcessoApenado(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_codigo_acesso_status ON CodigoAcessoApenado(status);
CREATE INDEX IF NOT EXISTS idx_log_acesso_usuario ON LogAcessoApenado(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_log_acesso_data ON LogAcessoApenado(data_hora);
