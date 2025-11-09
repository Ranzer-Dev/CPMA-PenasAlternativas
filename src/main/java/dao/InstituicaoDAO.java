package dao;

import database.ConnectionFactory;
import model.Instituicao;
import util.SQLiteDateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class InstituicaoDAO {

    public static boolean inserir(Instituicao inst) {
        final String sql = """
            INSERT INTO Instituicao
                (nome, endereco, cidade, uf, bairro, cep, responsavel, telefone, tipo)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            preencherParams(st, inst);
            int affected = st.executeUpdate();

            if (affected > 0) {
                // SQLite não suporta getGeneratedKeys(), então usamos last_insert_rowid()
                try (Statement stmt2 = c.createStatement();
                     ResultSet rs = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        inst.setIdInstituicao(rs.getInt(1));
                    }
                }
            }

            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir instituição:");
            e.printStackTrace();
            return false;
        }
    }

    public static int inserirEPegarID(Instituicao inst) {
        String sql = """
            INSERT INTO Instituicao (nome, endereco, cidade, uf, bairro, cep, responsavel, telefone, tipo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inst.getNome());
            stmt.setString(2, inst.getEndereco() != null ? inst.getEndereco() : "");
            stmt.setString(3, inst.getCidade() != null ? inst.getCidade() : "");
            stmt.setString(4, inst.getUf() != null ? inst.getUf() : "");
            stmt.setString(5, inst.getBairro() != null ? inst.getBairro() : "");
            stmt.setString(6, inst.getCep() != null ? inst.getCep() : "");
            stmt.setString(7, inst.getResponsavel() != null ? inst.getResponsavel() : "");
            stmt.setString(8, inst.getTelefone() != null ? inst.getTelefone() : "");
            stmt.setInt(9, inst.getTipo());

            System.out.println("Tentando inserir instituição: " + inst.getNome() + ", tipo: " + inst.getTipo());

            int affected = stmt.executeUpdate();
            System.out.println("Linhas afetadas: " + affected);

            if (affected > 0) {
                // SQLite não suporta getGeneratedKeys(), então usamos last_insert_rowid()
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int idGerado = rs.getInt(1);
                        System.out.println("ID gerado: " + idGerado);
                        return idGerado;
                    }
                }
                
                System.err.println("Aviso: Não foi possível obter o ID gerado");
            } else {
                System.err.println("Erro: Nenhuma linha foi inserida");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir instituição:");
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println("Código SQL: " + e.getSQLState());
            System.err.println("Erro: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado ao inserir instituição:");
            e.printStackTrace();
        }
        return -1;
    }


    public static List<Instituicao> buscarTodasInstituicoes() {
        List<Instituicao> lista = new ArrayList<>();
        String sql = "SELECT * FROM Instituicao";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static Instituicao buscarPorId(int id) {
        final String sql = "SELECT * FROM Instituicao WHERE id_instituicao = ?";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean atualizar(Instituicao inst) {
        final String sql = """
            UPDATE Instituicao SET
                nome = ?, endereco = ?, cidade = ?, uf = ?, bairro = ?, cep = ?,
                responsavel = ?, telefone = ?, tipo = ?
            WHERE id_instituicao = ?
        """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setString(1, inst.getNome());
            st.setString(2, inst.getEndereco());
            st.setString(3, inst.getCidade());
            st.setString(4, inst.getUf());
            st.setString(5, inst.getBairro());
            st.setString(6, inst.getCep());
            st.setString(7, inst.getResponsavel());
            st.setString(8, inst.getTelefone());
            st.setInt(9, inst.getTipo());
            st.setInt(10, inst.getIdInstituicao());
            return st.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deletar(int id) {
        final String sql = "DELETE FROM Instituicao WHERE id_instituicao = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            return st.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String buscarNomePorId(int id) {
        final String sql = "SELECT nome FROM Instituicao WHERE id_instituicao = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void preencherParams(PreparedStatement st, Instituicao i) throws SQLException {
        st.setString(1, i.getNome());
        st.setString(2, i.getEndereco());
        st.setString(3, i.getCidade());
        st.setString(4, i.getUf());
        st.setString(5, i.getBairro());
        st.setString(6, i.getCep());
        st.setString(7, i.getResponsavel());
        st.setString(8, i.getTelefone());
        st.setInt(9, i.getTipo());
    }

    private static Instituicao mapear(ResultSet rs) throws SQLException {
        Instituicao i = new Instituicao();
        i.setIdInstituicao(rs.getInt("id_instituicao"));
        i.setNome(rs.getString("nome"));
        i.setEndereco(rs.getString("endereco"));
        i.setCidade(rs.getString("cidade"));
        i.setUf(rs.getString("uf"));
        i.setBairro(rs.getString("bairro"));
        i.setCep(rs.getString("cep"));
        i.setResponsavel(rs.getString("responsavel"));
        i.setTelefone(rs.getString("telefone"));
        i.setTipo(rs.getInt("tipo"));
        // Mapear criado_em se necessário
        java.sql.Date criadoEm = SQLiteDateUtil.getDate(rs, "criado_em");
        if (criadoEm != null) {
            i.setCriadoEm(new java.util.Date(criadoEm.getTime()));
        }
        return i;
    }

    private InstituicaoDAO() {}
}
