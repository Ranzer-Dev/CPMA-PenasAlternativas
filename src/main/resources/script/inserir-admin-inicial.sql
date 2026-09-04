-- Script para inserir administrador inicial no banco de dados (PostgreSQL)
-- CPF: 12345678900
-- Senha: admin123 (hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)

-- Inserir administrador inicial
-- O nível de permissão 1 geralmente representa administrador com acesso total
INSERT INTO Administrador (
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
    'Qual é a cor do seu cavalo branco?',
    'Branco'
) ON CONFLICT (cpf) DO NOTHING;
