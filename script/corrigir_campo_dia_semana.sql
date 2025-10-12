-- Script para corrigir o tamanho do campo dia_semana
-- O campo estava muito pequeno para armazenar "sexta-feira" (10 caracteres)

USE PENAS_ALTERNATIVAS;
GO

-- Aumenta o tamanho do campo dia_semana para 20 caracteres
ALTER TABLE disponibilidade_instituicao 
ALTER COLUMN dia_semana VARCHAR(20) NOT NULL;
GO

-- Verifica se a alteração foi aplicada
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'disponibilidade_instituicao' 
AND COLUMN_NAME = 'dia_semana';
GO
