-- Script para corrigir datas inválidas no banco de dados SQLite
-- Este script limpa valores de data que são números inválidos (restos da migração do SQL Server)

-- Atualizar data_nascimento: se for um número negativo ou inválido, definir como NULL
UPDATE Usuario 
SET data_nascimento = NULL 
WHERE data_nascimento IS NOT NULL 
  AND (data_nascimento GLOB '-*' OR data_nascimento NOT GLOB '*-*-*');

-- Verificar quantos registros foram corrigidos
SELECT 'Registros com data_nascimento corrigida:' AS resultado;
SELECT COUNT(*) AS total FROM Usuario WHERE data_nascimento IS NULL;

-- Verificar registros restantes
SELECT 'Registros com data_nascimento válida:' AS resultado;
SELECT id_usuario, nome, cpf, data_nascimento FROM Usuario WHERE data_nascimento IS NOT NULL;

