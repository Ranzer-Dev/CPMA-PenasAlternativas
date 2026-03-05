-- Script para verificar se os descritores faciais estão sendo salvos corretamente

-- Verifica quantos usuários têm descritores faciais
SELECT 
    u.id_usuario,
    u.nome,
    u.cpf,
    CASE 
        WHEN df.descritores_faciais IS NULL THEN 'NULL'
        WHEN df.descritores_faciais = '' THEN 'VAZIO'
        WHEN df.descritores_faciais = '[]' THEN 'ARRAY VAZIO'
        ELSE 'OK'
    END as status_descritores,
    LENGTH(df.descritores_faciais) as tamanho_descritores,
    SUBSTR(df.descritores_faciais, 1, 100) as primeiros_100_chars,
    df.ativo as dados_faciais_ativos,
    df.data_cadastro,
    df.data_atualizacao
FROM Usuario u
LEFT JOIN DadosFaciais df ON u.id_usuario = df.fk_usuario_id_usuario
ORDER BY u.id_usuario;

-- Conta quantos têm descritores válidos
SELECT 
    COUNT(*) as total_usuarios,
    SUM(CASE WHEN df.descritores_faciais IS NOT NULL 
             AND df.descritores_faciais != '' 
             AND df.descritores_faciais != '[]' 
             AND df.ativo = 1 
        THEN 1 ELSE 0 END) as usuarios_com_descritores_validos,
    SUM(CASE WHEN df.descritores_faciais IS NULL OR df.descritores_faciais = '' OR df.descritores_faciais = '[]' 
        THEN 1 ELSE 0 END) as usuarios_sem_descritores
FROM Usuario u
LEFT JOIN DadosFaciais df ON u.id_usuario = df.fk_usuario_id_usuario;

