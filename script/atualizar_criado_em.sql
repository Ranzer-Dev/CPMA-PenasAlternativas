-- Script para adicionar coluna criadoEm nas tabelas existentes
-- Execute este script após a criação inicial do banco de dados

USE [PENAS_ALTERNATIVAS]
GO

-- Adicionar coluna criadoEm na tabela Usuario (substituindo data_cadastro)
ALTER TABLE [dbo].[Usuario] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Atualizar registros existentes com data atual
UPDATE [dbo].[Usuario] 
SET [criado_em] = ISNULL([data_cadastro], GETDATE())
GO

-- Remover coluna data_cadastro antiga
ALTER TABLE [dbo].[Usuario] 
DROP COLUMN [data_cadastro]
GO

-- Adicionar coluna criadoEm na tabela Instituicao
ALTER TABLE [dbo].[Instituicao] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Adicionar coluna criadoEm na tabela Pena
ALTER TABLE [dbo].[Pena] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Adicionar coluna criadoEm na tabela RegistroDeTrabalho
ALTER TABLE [dbo].[RegistroDeTrabalho] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Adicionar coluna criadoEm na tabela Administrador
ALTER TABLE [dbo].[Administrador] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Adicionar coluna criadoEm na tabela tipoDeInstituição
ALTER TABLE [dbo].[tipoDeInstituição] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

-- Adicionar coluna criadoEm na tabela disponibilidade_instituicao
ALTER TABLE [dbo].[disponibilidade_instituicao] 
ADD [criado_em] [datetime] NOT NULL DEFAULT GETDATE()
GO

PRINT 'Colunas criado_em adicionadas com sucesso em todas as tabelas!'
