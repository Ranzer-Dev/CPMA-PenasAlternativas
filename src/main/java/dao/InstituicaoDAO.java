package dao;

import database.ConnectionFactory;
import model.Instituicao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class InstituicaoDAO {

    public static boolean inserir(Instituicao inst) {
        final String sql = """
            INSERT INTO Instituicao
                (nome, endereco, cidade, uf, bairro, cep, responsavel, responsavel2, telefone, telefone2, tipo)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherParams(st, inst);
            int affected = st.executeUpdate();

            if (affected > 0) {
                ResultSet keys = st.getGeneratedKeys();
                if (keys.next()) {
                    inst.setIdInstituicao(keys.getInt(1));
                }
            }

            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int inserirEPegarID(Instituicao inst) {
        String sql = """
        INSERT INTO instituicao (nome, endereco, cidade, uf, bairro, cep, responsavel, telefone, tipo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, inst.getNome());
            stmt.setString(2, inst.getEndereco());
            stmt.setString(3, inst.getCidade());
            stmt.setString(4, inst.getUf());
            stmt.setString(5, inst.getBairro());
            stmt.setString(6, inst.getCep());
            stmt.setString(7, inst.getResponsavel());
            stmt.setString(8, inst.getTelefone());
            stmt.setInt(9, inst.getTipo());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
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

            preencherParams(st, inst);
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
        return i;
    }

    private InstituicaoDAO() {}
}
