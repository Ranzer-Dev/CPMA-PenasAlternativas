package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
            
            // Salva a imagem no banco como BLOB
            if (dadosFaciais.getImagemRosto() != null && dadosFaciais.getImagemRosto().length > 0) {
                stmt.setBytes(2, dadosFaciais.getImagemRosto());
                System.out.println("  - Campo imagem_rosto: " + dadosFaciais.getImagemRosto().length + " bytes (BLOB)");
            } else {
                stmt.setNull(2, java.sql.Types.BLOB);
                System.out.println("  - Campo imagem_rosto: NULL (nenhuma imagem fornecida)");
            }
            
            stmt.setString(3, dadosFaciais.getDescritoresFaciais() != null ? dadosFaciais.getDescritoresFaciais() : "");
            
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
            
            // Atualiza a imagem no banco como BLOB
            if (dadosFaciais.getImagemRosto() != null && dadosFaciais.getImagemRosto().length > 0) {
                stmt.setBytes(2, dadosFaciais.getImagemRosto());
                System.out.println("  - Campo imagem_rosto: " + dadosFaciais.getImagemRosto().length + " bytes (BLOB)");
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
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Busca usuário por similaridade facial
     */
    public Usuario buscarPorSimilaridadeFacial(String descritoresFaciais, double threshold) {
        String sql = "SELECT u.*, df.descritores_faciais FROM Usuario u "
                + "INNER JOIN DadosFaciais df ON u.id_usuario = df.fk_usuario_id_usuario "
                + "WHERE df.ativo = 1";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String descritoresArmazenados = rs.getString("descritores_faciais");
                double similaridade = calcularSimilaridade(descritoresFaciais, descritoresArmazenados);

                if (similaridade >= threshold) {
                    return mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
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
        dadosFaciais.setFkUsuarioIdUsuario(rs.getInt("fk_usuario_id_usuario"));
        
        // Lê os bytes do campo imagem_rosto (pode ser null)
        byte[] bytes = rs.getBytes("imagem_rosto");
        if (bytes != null && bytes.length > 0) {
            dadosFaciais.setImagemRosto(bytes);
            System.out.println("✅ Bytes lidos do banco: " + bytes.length + " bytes");
        } else {
            System.out.println("⚠️ Bytes são NULL ou vazios no banco de dados");
            dadosFaciais.setImagemRosto(null);
        }
        
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
     * Calcula similaridade entre dois vetores de descritores faciais
     * Implementação simplificada - pode ser melhorada com algoritmos mais
     * sofisticados
     */
    private double calcularSimilaridade(String descritores1, String descritores2) {
        try {
            // Converte strings JSON para arrays de double
            String[] valores1 = descritores1.replaceAll("[\\[\\]]", "").split(",");
            String[] valores2 = descritores2.replaceAll("[\\[\\]]", "").split(",");

            if (valores1.length != valores2.length) {
                return 0.0;
            }

            double somaQuadrados = 0.0;
            for (int i = 0; i < valores1.length; i++) {
                double diff = Double.parseDouble(valores1[i].trim()) - Double.parseDouble(valores2[i].trim());
                somaQuadrados += diff * diff;
            }

            double distanciaEuclidiana = Math.sqrt(somaQuadrados);
            // Converte distância para similaridade (0 = diferente, 1 = idêntico)
            return Math.max(0, 1 - (distanciaEuclidiana / 100.0));

        } catch (Exception e) {
            return 0.0;
        }
    }
}
