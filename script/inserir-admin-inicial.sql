-- Script para inserir administrador inicial no banco de dados
-- CPF: 12345678900
-- Senha: admin123 (hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)

-- Inserir administrador inicial
-- O nível de permissão 1 geralmente representa administrador com acesso total
-- INSERT OR IGNORE evita erro se o administrador já existir
INSERT OR IGNORE INTO Administrador (
    nome, 
    cpf, 
    senha, 
    nivel_permissao, 
    pergunta_secreta, 
    resposta_secreta
) VALUES (
    'Administrador',
    '12345678900',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    1,
    NULL,
    NULL
);

