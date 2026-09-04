-- Códigos temporários para acesso do apenado ao totem (PostgreSQL)
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

-- Log de auditoria das tentativas de acesso pelo totem (PostgreSQL)
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

CREATE INDEX IF NOT EXISTS idx_codigo_acesso_codigo ON CodigoAcessoApenado(codigo);
CREATE INDEX IF NOT EXISTS idx_codigo_acesso_usuario ON CodigoAcessoApenado(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_codigo_acesso_status ON CodigoAcessoApenado(status);
CREATE INDEX IF NOT EXISTS idx_log_acesso_usuario ON LogAcessoApenado(fk_usuario_id_usuario);
CREATE INDEX IF NOT EXISTS idx_log_acesso_data ON LogAcessoApenado(data_hora);
