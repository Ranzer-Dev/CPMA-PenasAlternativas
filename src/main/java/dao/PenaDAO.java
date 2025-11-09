package dao;

import database.ConnectionFactory;
import model.Pena;
import util.SQLiteDateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenaDAO {

    public static int inserirPena(Pena pena) {
        String sql = "INSERT INTO Pena (tipo_pena, data_inicio, data_termino, descricao, dias_semana_e_horarios_disponivel, atividades_acordadas, horas_semanais, tempo_pena, horas_totais, fk_usuario_id_usuario, fk_instituicao_id_instituicao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pena.getTipoPena());
            if (pena.getDataInicio() != null) {
                stmt.setString(2, new java.sql.Date(pena.getDataInicio().getTime()).toString());
            } else {
                stmt.setNull(2, Types.DATE);
            }
            if (pena.getDataTermino() != null) {
                stmt.setString(3, new java.sql.Date(pena.getDataTermino().getTime()).toString());
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.setString(4, pena.getDescricao());
            stmt.setString(5, pena.getDiasSemanaEHorariosDisponivel());
            stmt.setString(6, pena.getAtividadesAcordadas());
            stmt.setInt(7, pena.getHorasSemanais());
            stmt.setDouble(8, pena.getTempoPena());
            stmt.setDouble(9, pena.getHorasTotais());
            stmt.setInt(10, pena.getFkUsuarioIdUsuario());
            stmt.setInt(11, pena.getFkInstituicaoIdInstituicao());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // SQLite não suporta getGeneratedKeys(), então usamos last_insert_rowid()
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir pena:");
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Pena> buscarTodasPenas() {
        List<Pena> lista = new ArrayList<>();
        String sql = "SELECT * FROM Pena";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static List<Pena> buscarPenasPorUsuario(int idUsuario) {
        List<Pena> penas = new ArrayList<>();
        String sql = "SELECT * FROM Pena WHERE fk_usuario_id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) penas.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return penas;
    }

    public static Pena buscarPorId(int id) {
        String sql = "SELECT * FROM Pena WHERE id_pena = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapear(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean atualizar(Pena pena) {
        String sql = "UPDATE Pena SET tipo_pena = ?, data_inicio = ?, data_termino = ?, descricao = ?, dias_semana_e_horarios_disponivel = ?, atividades_acordadas = ?, horas_semanais = ?, tempo_pena = ?, horas_totais = ?, fk_usuario_id_usuario = ?, fk_instituicao_id_instituicao = ? WHERE id_pena = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pena.getTipoPena());
            if (pena.getDataInicio() != null) {
                stmt.setString(2, new java.sql.Date(pena.getDataInicio().getTime()).toString());
            } else {
                stmt.setNull(2, Types.DATE);
            }
            if (pena.getDataTermino() != null) {
                stmt.setString(3, new java.sql.Date(pena.getDataTermino().getTime()).toString());
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.setString(4, pena.getDescricao());
            stmt.setString(5, pena.getDiasSemanaEHorariosDisponivel());
            stmt.setString(6, pena.getAtividadesAcordadas());
            stmt.setInt(7, pena.getHorasSemanais());
            stmt.setDouble(8, pena.getTempoPena());
            stmt.setDouble(9, pena.getHorasTotais());
            stmt.setInt(10, pena.getFkUsuarioIdUsuario());
            stmt.setInt(11, pena.getFkInstituicaoIdInstituicao());
            stmt.setInt(12, pena.getIdPena());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deletar(int id) {
        String sql = "DELETE FROM Pena WHERE id_pena = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Pena buscarPenaAtivaPorUsuario(int idUsuario) {
        String sql = """
            SELECT *
            FROM Pena
            WHERE fk_usuario_id_usuario = ?
            ORDER BY
                CASE WHEN data_termino IS NULL OR data_termino >= date('now', 'localtime') THEN 0 ELSE 1 END,
                data_inicio DESC
            LIMIT 1
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapear(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Conta o número total de penas cadastradas para um usuário.
     * 
     * @param idUsuario ID do usuário
     * @return Número total de penas cadastradas
     */
    public static int contarPenasPorUsuario(int idUsuario) {
        String sql = "SELECT COUNT(*) as total FROM Pena WHERE fk_usuario_id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Pena mapear(ResultSet rs) throws SQLException {
        Pena p = new Pena();
        p.setIdPena(rs.getInt("id_pena"));
        p.setTipoPena(rs.getString("tipo_pena"));
        p.setDataInicio(SQLiteDateUtil.getDate(rs, "data_inicio"));
        p.setDataTermino(SQLiteDateUtil.getDate(rs, "data_termino"));
        p.setDescricao(rs.getString("descricao"));
        p.setDiasSemanaEHorariosDisponivel(rs.getString("dias_semana_e_horarios_disponivel"));
        p.setAtividadesAcordadas(rs.getString("atividades_acordadas"));
        p.setHorasSemanais(rs.getInt("horas_semanais"));
        p.setTempoPena(rs.getInt("tempo_pena"));
        p.setHorasTotais(rs.getDouble("horas_totais"));
        p.setFkUsuarioIdUsuario(rs.getInt("fk_usuario_id_usuario"));
        p.setFkInstituicaoIdInstituicao(rs.getInt("fk_instituicao_id_instituicao"));
        return p;
    }
}
