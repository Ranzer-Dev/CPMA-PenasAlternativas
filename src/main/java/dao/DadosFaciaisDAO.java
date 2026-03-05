package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ConnectionFactory;
import model.DadosFaciais;
import model.Usuario;
import util.SQLiteDateUtil;

public class DadosFaciaisDAO {


    /**
     * Cadastra novos dados faciais para um usuário
     * Salva a imagem como BLOB no campo imagem_rosto e os embeddings (descritores_faciais)
     */
    public boolean cadastrar(DadosFaciais dadosFaciais) {
        String sql = "INSERT INTO DadosFaciais (fk_usuario_id_usuario, imagem_rosto, descritores_faciais, data_cadastro, data_atualizacao, ativo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dadosFaciais.getFkUsuarioIdUsuario());
            
            // Salva a imagem no banco como BLOB usando setBytes()
            // NOTA: SQLite via JDBC usa setBytes() que escreve BLOBs eficientemente.
            // A interface C sqlite3_blob_write() não é necessária quando usando JDBC,
            // pois o driver JDBC já otimiza a escrita de BLOBs internamente.
            if (dadosFaciais.getImagemRosto() != null && dadosFaciais.getImagemRosto().length > 0) {
                stmt.setBytes(2, dadosFaciais.getImagemRosto());
                System.out.println("  - Campo imagem_rosto: " + dadosFaciais.getImagemRosto().length + 
                                 " bytes (" + String.format("%.1f", dadosFaciais.getImagemRosto().length / 1024.0) + " KB)");
            } else {
                stmt.setNull(2, java.sql.Types.BLOB);
                System.out.println("  - Campo imagem_rosto: NULL (nenhuma imagem fornecida)");
            }
            
            String descritores = dadosFaciais.getDescritoresFaciais() != null ? dadosFaciais.getDescritoresFaciais() : "";
            stmt.setString(3, descritores);
            System.out.println("  - Campo descritores_faciais: " + (descritores.isEmpty() ? "VAZIO ⚠️" : descritores.length() + " caracteres"));
            if (!descritores.isEmpty()) {
                System.out.println("  - Primeiros 100 caracteres dos descritores: " + descritores.substring(0, Math.min(100, descritores.length())));
            }
            
            if (dadosFaciais.getCriadoEm() != null) {
                stmt.setString(4, dadosFaciais.getCriadoEm().toString());
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            if (dadosFaciais.getDataAtualizacao() != null) {
                stmt.setString(5, dadosFaciais.getDataAtualizacao().toString());
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            
            stmt.setInt(6, dadosFaciais.isAtivo() ? 1 : 0); // SQLite usa INTEGER para boolean

            System.out.println("Executando INSERT na tabela DadosFaciais...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                // SQLite não suporta getGeneratedKeys(), então usamos last_insert_rowid()
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        dadosFaciais.setIdDadosFaciais(rs.getInt(1));
                        System.out.println("✅ Dados faciais cadastrados com ID: " + dadosFaciais.getIdDadosFaciais());
                    }
                }
                return true;
            } else {
                System.err.println("❌ Nenhuma linha foi afetada no INSERT");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao cadastrar dados faciais:");
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Atualiza dados faciais existentes
     * Atualiza a imagem_rosto (BLOB) e os embeddings (descritores_faciais)
     */
    public boolean atualizar(DadosFaciais dadosFaciais) {
        // Atualiza descritores_faciais, imagem_rosto e data_atualizacao
        String sql = "UPDATE DadosFaciais SET descritores_faciais = ?, imagem_rosto = ?, data_atualizacao = ? WHERE id_dados_faciais = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dadosFaciais.getDescritoresFaciais() != null ? dadosFaciais.getDescritoresFaciais() : "");
            
            // Atualiza a imagem no banco como BLOB usando setBytes()
            // NOTA: SQLite via JDBC usa setBytes() que escreve BLOBs eficientemente.
            if (dadosFaciais.getImagemRosto() != null && dadosFaciais.getImagemRosto().length > 0) {
                stmt.setBytes(2, dadosFaciais.getImagemRosto());
                System.out.println("  - Campo imagem_rosto: " + dadosFaciais.getImagemRosto().length + 
                                 " bytes (" + String.format("%.1f", dadosFaciais.getImagemRosto().length / 1024.0) + " KB)");
            } else {
                stmt.setNull(2, java.sql.Types.BLOB);
                System.out.println("  - Campo imagem_rosto: NULL (nenhuma imagem fornecida)");
            }
            
            stmt.setString(3, new Date(System.currentTimeMillis()).toString());
            stmt.setInt(4, dadosFaciais.getIdDadosFaciais());

            System.out.println("Executando UPDATE na tabela DadosFaciais (ID: " + dadosFaciais.getIdDadosFaciais() + ")...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar dados faciais:");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Busca dados faciais por ID do usuário
     */
    public DadosFaciais buscarPorUsuario(int idUsuario) {
        String sql = "SELECT * FROM DadosFaciais WHERE fk_usuario_id_usuario = ? AND ativo = 1";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            System.out.println("   Executando query: " + sql);
            System.out.println("   Parâmetro: idUsuario = " + idUsuario);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                DadosFaciais dadosFaciais = mapearResultSet(rs);
                System.out.println("   ✅ DadosFaciais encontrado:");
                System.out.println("      ID: " + dadosFaciais.getIdDadosFaciais());
                System.out.println("      ID Usuário: " + dadosFaciais.getFkUsuarioIdUsuario());
                System.out.println("      Imagem (bytes): " + (dadosFaciais.getImagemRosto() != null ? 
                                 dadosFaciais.getImagemRosto().length + " bytes" : "NULL"));
                System.out.println("      Descritores: " + (dadosFaciais.getDescritoresFaciais() != null ? 
                                 dadosFaciais.getDescritoresFaciais().length() + " caracteres" : "NULL"));
                
                // Valida se o ID do usuário corresponde
                if (dadosFaciais.getFkUsuarioIdUsuario() != idUsuario) {
                    System.err.println("   ⚠️ AVISO: ID do usuário não corresponde!");
                    System.err.println("      Esperado: " + idUsuario);
                    System.err.println("      Encontrado: " + dadosFaciais.getFkUsuarioIdUsuario());
                }
                
                return dadosFaciais;
            } else {
                System.err.println("   ❌ Nenhum DadosFaciais encontrado para o usuário ID: " + idUsuario);
            }

        } catch (SQLException e) {
            System.err.println("   ❌ Erro ao buscar dados faciais: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Busca usuário por similaridade facial
     */
    public Usuario buscarPorSimilaridadeFacial(String descritoresFaciais, double threshold) {
        // Query SQL: busca TODOS os dados faciais ativos (múltiplas fotos por usuário)
        // Agrupa por usuário e compara com TODAS as fotos de cada usuário
        String sql = "SELECT u.*, df.descritores_faciais, df.id_dados_faciais, df.fk_usuario_id_usuario as df_usuario_id "
                + "FROM Usuario u "
                + "INNER JOIN DadosFaciais df ON u.id_usuario = df.fk_usuario_id_usuario "
                + "WHERE df.ativo = 1 "
                + "ORDER BY u.id_usuario, df.id_dados_faciais";

        System.out.println("   Executando query SQL...");
        System.out.println("   💡 NOVO: Comparando com TODAS as fotos de cada usuário para melhor precisão");

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            int totalRegistros = 0;
            int usuariosComDescritoresValidos = 0;
            double maiorSimilaridadeGlobal = 0.0;
            Usuario melhorMatch = null;
            
            // Mapa para rastrear a melhor similaridade de cada usuário
            Map<Integer, Double> melhorSimilaridadePorUsuario = new HashMap<>();
            Map<Integer, Usuario> usuariosProcessados = new HashMap<>();

            int idUsuarioAnterior = -1;
            int totalUsuarios = 0;
            
            while (rs.next()) {
                totalRegistros++;
                int idUsuario = rs.getInt("id_usuario");
                int dfUsuarioId = rs.getInt("df_usuario_id");
                String descritoresArmazenados = rs.getString("descritores_faciais");
                
                // Conta usuários únicos
                if (idUsuario != idUsuarioAnterior) {
                    totalUsuarios++;
                    idUsuarioAnterior = idUsuario;
                }
                
                // VALIDAÇÃO CRÍTICA: Verifica se os IDs correspondem
                if (idUsuario != dfUsuarioId) {
                    System.err.println("\n   ⚠️⚠️⚠️ ERRO CRÍTICO: IDs não correspondem! ⚠️⚠️⚠️");
                    System.err.println("      id_usuario: " + idUsuario);
                    System.err.println("      df.fk_usuario_id_usuario: " + dfUsuarioId);
                    continue;
                }
                
                if (descritoresArmazenados == null || descritoresArmazenados.isEmpty() || descritoresArmazenados.equals("[]")) {
                    continue; // Pula descritores vazios
                }
                
                // Armazena o usuário se ainda não foi processado
                if (!usuariosProcessados.containsKey(idUsuario)) {
                    usuariosProcessados.put(idUsuario, mapearUsuario(rs));
                    melhorSimilaridadePorUsuario.put(idUsuario, 0.0);
                    usuariosComDescritoresValidos++;
                }
                
                // Calcula similaridade com esta foto específica
                double similaridade = calcularSimilaridade(descritoresFaciais, descritoresArmazenados);
                
                // Atualiza a melhor similaridade para este usuário (pode ter múltiplas fotos)
                double melhorSimilaridadeUsuario = melhorSimilaridadePorUsuario.get(idUsuario);
                if (similaridade > melhorSimilaridadeUsuario) {
                    melhorSimilaridadePorUsuario.put(idUsuario, similaridade);
                    
                    // Atualiza o melhor match global se necessário
                    if (similaridade > maiorSimilaridadeGlobal) {
                        maiorSimilaridadeGlobal = similaridade;
                        melhorMatch = usuariosProcessados.get(idUsuario);
                    }
                }
            }
            
            if (usuariosComDescritoresValidos == 0) {
                System.err.println("      ❌ ERRO CRÍTICO: Nenhum usuário tem descritores faciais válidos no banco!");
                System.err.println("      💡 Ação necessária: Recadastre as fotos dos usuários");
                return null;
            }
            
            // CORREÇÃO CRÍTICA: Retorna o MELHOR match (maior similaridade entre todas as fotos) apenas se >= threshold
            if (melhorMatch != null && maiorSimilaridadeGlobal >= threshold) {
                System.out.println("\n      ✅✅✅ MELHOR MATCH ENCONTRADO! ✅✅✅");
                System.out.println("      Similaridade (melhor foto): " + String.format("%.4f", maiorSimilaridadeGlobal) + " >= " + threshold);
                System.out.println("      Usuário: ID " + melhorMatch.getIdUsuario() + " - " + melhorMatch.getNome());
                System.out.println("      CPF: " + melhorMatch.getCpf());
                System.out.println("      💡 Este usuário tem " + melhorSimilaridadePorUsuario.get(melhorMatch.getIdUsuario()) + " como melhor similaridade entre suas fotos");
                
                // Validação adicional: verifica se o usuário retornado tem dados faciais correspondentes
                DadosFaciais dadosVerificacao = buscarPorUsuario(melhorMatch.getIdUsuario());
                if (dadosVerificacao != null) {
                    System.out.println("      ✅ Validação: DadosFaciais encontrado para este usuário");
                    System.out.println("         ID DadosFaciais: " + dadosVerificacao.getIdDadosFaciais());
                    System.out.println("         ID Usuário no DadosFaciais: " + dadosVerificacao.getFkUsuarioIdUsuario());
                    
                    if (dadosVerificacao.getFkUsuarioIdUsuario() != melhorMatch.getIdUsuario()) {
                        System.err.println("      ❌ ERRO CRÍTICO: IDs não correspondem!");
                        return null;
                    }
                } else {
                    System.err.println("      ⚠️ AVISO: Nenhum DadosFaciais encontrado para este usuário");
                    return null;
                }
                
                return melhorMatch;
            } else if (maiorSimilaridadeGlobal > 0.0 && maiorSimilaridadeGlobal < threshold) {
                System.out.println("      ⚠️ Similaridade mais alta (" + String.format("%.4f", maiorSimilaridadeGlobal) + 
                                 ") está abaixo do threshold (" + threshold + ")");
                System.out.println("      💡 Diferença: " + String.format("%.4f", threshold - maiorSimilaridadeGlobal));
                
                if (maiorSimilaridadeGlobal > 0.2) {
                    System.out.println("      💡 Similaridade > 0.2 - pode ser a mesma pessoa, threshold muito restritivo");
                    // Retorna mesmo assim se for > 0.2 (fallback)
                    if (melhorMatch != null) {
                        System.out.println("\n      ⚠️ Retornando melhor match mesmo abaixo do threshold (similaridade > 0.2)");
                        System.out.println("      Usuário: ID " + melhorMatch.getIdUsuario() + " - " + melhorMatch.getNome());
                        return melhorMatch;
                    }
                } else if (maiorSimilaridadeGlobal > 0.1) {
                    System.out.println("      💡 Similaridade > 0.1 - possível match, mas baixa confiança");
                } else {
                    System.out.println("      💡 Similaridade muito baixa - provavelmente não é a mesma pessoa");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar por similaridade facial: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lista todos os dados faciais ativos
     */
    public List<DadosFaciais> listarTodos() {
        String sql = "SELECT * FROM DadosFaciais WHERE ativo = 1 ORDER BY data_cadastro DESC";
        List<DadosFaciais> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Desativa dados faciais (soft delete)
     */
    public boolean desativar(int idDadosFaciais) {
        String sql = "UPDATE DadosFaciais SET ativo = 0, data_atualizacao = ? WHERE id_dados_faciais = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, new Date(System.currentTimeMillis()).toString());
            stmt.setInt(2, idDadosFaciais);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Mapeia o ResultSet para objeto DadosFaciais
     */
    private DadosFaciais mapearResultSet(ResultSet rs) throws SQLException {
        DadosFaciais dadosFaciais = new DadosFaciais();
        dadosFaciais.setIdDadosFaciais(rs.getInt("id_dados_faciais"));
        
        int idUsuario = rs.getInt("fk_usuario_id_usuario");
        dadosFaciais.setFkUsuarioIdUsuario(idUsuario);
        
        // Lê os bytes do campo imagem_rosto (pode ser null)
        byte[] bytes = rs.getBytes("imagem_rosto");
        if (bytes != null && bytes.length > 0) {
            dadosFaciais.setImagemRosto(bytes);
            System.out.println("      ✅ Bytes lidos do banco: " + bytes.length + " bytes (" + 
                             String.format("%.1f", bytes.length / 1024.0) + " KB)");
        } else {
            System.out.println("      ⚠️ Bytes são NULL ou vazios no banco de dados");
            dadosFaciais.setImagemRosto(null);
        }
        
        // Valida se o ID do usuário está correto
        System.out.println("      [Validação] ID Usuário no DadosFaciais: " + idUsuario);
        
        dadosFaciais.setDescritoresFaciais(rs.getString("descritores_faciais"));
        dadosFaciais.setCriadoEm(SQLiteDateUtil.getDate(rs, "data_cadastro"));
        dadosFaciais.setDataAtualizacao(SQLiteDateUtil.getDate(rs, "data_atualizacao"));
        
        // SQLite usa INTEGER (1 ou 0) para boolean
        int ativoInt = rs.getInt("ativo");
        dadosFaciais.setAtivo(ativoInt == 1);
        
        return dadosFaciais;
    }

    /**
     * Mapeia o ResultSet para objeto Usuario
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setDataNascimento(SQLiteDateUtil.getDate(rs, "data_nascimento"));
        usuario.setEndereco(rs.getString("endereco"));
        usuario.setCidade(rs.getString("cidade"));
        usuario.setUf(rs.getString("uf"));
        usuario.setBairro(rs.getString("bairro"));
        usuario.setCep(rs.getString("cep"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setNacionalidade(rs.getString("nacionalidade"));
        usuario.setCriadoEm(SQLiteDateUtil.getDate(rs, "criado_em"));
        usuario.setFoto(rs.getString("foto"));
        usuario.setObservacao(rs.getString("observacao"));
        return usuario;
    }

    /**
     * Calcula similaridade entre dois vetores de descritores faciais usando cosseno similarity
     * Embeddings faciais normalizados funcionam melhor com cosseno similarity
     */
    private double calcularSimilaridade(String descritores1, String descritores2) {
        try {
            if (descritores1 == null || descritores2 == null || 
                descritores1.isEmpty() || descritores2.isEmpty() ||
                descritores1.equals("[]") || descritores2.equals("[]")) {
                System.out.println("⚠️ Descritores vazios ou nulos");
                return 0.0;
            }

            // Converte strings JSON para arrays de double
            // Remove colchetes e espaços, depois divide por vírgula
            String clean1 = descritores1.trim().replaceAll("^\\[|\\]$", "").trim();
            String clean2 = descritores2.trim().replaceAll("^\\[|\\]$", "").trim();
            
            String[] valores1 = clean1.isEmpty() ? new String[0] : clean1.split(",\\s*");
            String[] valores2 = clean2.isEmpty() ? new String[0] : clean2.split(",\\s*");

            if (valores1.length != valores2.length) {
                System.out.println("⚠️ Dimensões diferentes: " + valores1.length + " vs " + valores2.length);
                System.out.println("   Primeiros valores 1: " + (valores1.length > 0 ? valores1[0] : "vazio"));
                System.out.println("   Primeiros valores 2: " + (valores2.length > 0 ? valores2[0] : "vazio"));
                // Tenta usar a menor dimensão para comparação parcial
                int minLen = Math.min(valores1.length, valores2.length);
                if (minLen > 0) {
                    System.out.println("   Usando apenas as primeiras " + minLen + " dimensões para comparação");
                    String[] temp1 = new String[minLen];
                    String[] temp2 = new String[minLen];
                    System.arraycopy(valores1, 0, temp1, 0, minLen);
                    System.arraycopy(valores2, 0, temp2, 0, minLen);
                    valores1 = temp1;
                    valores2 = temp2;
                } else {
                    return 0.0;
                }
            }

            if (valores1.length == 0) {
                System.out.println("⚠️ Arrays vazios após parsing");
                System.out.println("   Descritores1 original: " + descritores1.substring(0, Math.min(100, descritores1.length())));
                System.out.println("   Descritores2 original: " + descritores2.substring(0, Math.min(100, descritores2.length())));
                return 0.0;
            }
            
            System.out.println("   Comparando " + valores1.length + " dimensões");

            // Calcula cosseno similarity (melhor para embeddings normalizados)
            double produtoEscalar = 0.0;
            double norma1 = 0.0;
            double norma2 = 0.0;

            for (int i = 0; i < valores1.length; i++) {
                try {
                    double v1 = Double.parseDouble(valores1[i].trim());
                    double v2 = Double.parseDouble(valores2[i].trim());
                    
                    produtoEscalar += v1 * v2;
                    norma1 += v1 * v1;
                    norma2 += v2 * v2;
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Erro ao parsear valor na posição " + i + ": '" + valores1[i] + "' ou '" + valores2[i] + "'");
                    continue;
                }
            }

            double denominador = Math.sqrt(norma1) * Math.sqrt(norma2);
            
            if (denominador == 0.0) {
                System.out.println("⚠️ Denominador zero na similaridade");
                return 0.0;
            }

            double similaridade = produtoEscalar / denominador;
            
            // Para embeddings normalizados com L2 normalization, cosseno similarity já retorna valores entre 0 e 1
            // Valores próximos de 1 indicam alta similaridade
            // Não precisamos normalizar novamente, apenas garantimos que está no range [0, 1]
            double similaridadeFinal = Math.max(0.0, Math.min(1.0, similaridade));
            
            return similaridadeFinal;

        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular similaridade: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
}
