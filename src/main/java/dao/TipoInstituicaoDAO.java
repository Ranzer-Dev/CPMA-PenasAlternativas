package dao;

import database.ConnectionFactory;
import model.TipoInstituicao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoInstituicaoDAO {

    public static List<TipoInstituicao> buscarTodos() {
        List<TipoInstituicao> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipoDeInstituição";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TipoInstituicao tipo = new TipoInstituicao();
                tipo.setId(rs.getInt("id_tipo"));
                tipo.setTipo(rs.getString("tipo"));
                lista.add(tipo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean inserir(String tipo) {
        String sql = "INSERT INTO tipoDeInstituição (tipo) VALUES (?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static TipoInstituicao buscarPorId(int id) {
        String sql = "SELECT * FROM tipoDeInstituição WHERE id_tipo = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TipoInstituicao tipo = new TipoInstituicao();
                    tipo.setId(rs.getInt("id_tipo"));
                    tipo.setTipo(rs.getString("tipo"));
                    return tipo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
