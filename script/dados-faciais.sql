-- Script para criação da tabela de dados faciais
-- Execute este script no banco PENAS_ALTERNATIVAS

USE [PENAS_ALTERNATIVAS]
GO

-- Criação da tabela DadosFaciais
CREATE TABLE [dbo].[DadosFaciais](
    [id_dados_faciais] [int] IDENTITY(1,1) NOT NULL,
    [fk_usuario_id_usuario] [int] NOT NULL,
    [imagem_rosto] [varbinary](max) NULL,
    [descritores_faciais] [text] NULL,
    [data_cadastro] [date] NOT NULL,
    [data_atualizacao] [date] NOT NULL,
    [ativo] [bit] NOT NULL DEFAULT 1,
 CONSTRAINT [PK_DadosFaciais] PRIMARY KEY CLUSTERED 
(
    [id_dados_faciais] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

-- Criação da chave estrangeira
ALTER TABLE [dbo].[DadosFaciais]  WITH CHECK ADD  CONSTRAINT [FK_DadosFaciais_Usuario] FOREIGN KEY([fk_usuario_id_usuario])
REFERENCES [dbo].[Usuario] ([id_usuario])
GO

ALTER TABLE [dbo].[DadosFaciais] CHECK CONSTRAINT [FK_DadosFaciais_Usuario]
GO

-- Criação de índices para melhor performance
CREATE NONCLUSTERED INDEX [IX_DadosFaciais_Usuario] ON [dbo].[DadosFaciais]
(
    [fk_usuario_id_usuario] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IX_DadosFaciais_Ativo] ON [dbo].[DadosFaciais]
(
    [ativo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- Inserção de dados de exemplo (opcional)
-- Descomente as linhas abaixo se quiser inserir dados de teste

/*
INSERT INTO [dbo].[DadosFaciais] 
([fk_usuario_id_usuario], [descritores_faciais], [data_cadastro], [data_atualizacao], [ativo])
VALUES 
(1, '[100,150,200,120,180,90,160,140,110,130]', GETDATE(), GETDATE(), 1),
(2, '[95,145,195,115,175,85,155,135,105,125]', GETDATE(), GETDATE(), 1)
GO
*/

-- Verificação da criação da tabela
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'DadosFaciais'
ORDER BY ORDINAL_POSITION
GO

-- Verificação das constraints
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_NAME = 'DadosFaciais'
GO

PRINT 'Tabela DadosFaciais criada com sucesso!'
GO
