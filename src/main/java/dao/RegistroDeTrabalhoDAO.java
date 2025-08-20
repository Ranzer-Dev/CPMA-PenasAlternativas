package dao;

import model.RegistroDeTrabalho;
import database.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistroDeTrabalhoDAO {

    public boolean inserir(RegistroDeTrabalho registro) {
        String sql = "INSERT INTO RegistroDeTrabalho (fk_pena_id_pena, data_trabalho, horas_cumpridas, atividades, horario_inicio, horario_almoco, horario_volta, horario_saida) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registro.getFkPenaId());
            stmt.setDate(2, registro.getDataTrabalho());
            stmt.setDouble(3, registro.getHorasCumpridas());
            stmt.setString(4, registro.getAtividades());
            stmt.setTime(5, registro.getHorarioInicio());
            stmt.setTime(6, registro.getHorarioAlmoco());
            stmt.setTime(7, registro.getHorarioVolta());
            stmt.setTime(8, registro.getHorarioSaida());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizar(RegistroDeTrabalho r) {
        final String sql = """
            UPDATE RegistroDeTrabalho SET
                fk_usuario_id_usuario        = ?,
                data_trabalho  = ?, horas_cumpridas = ?, atividades = ?,
                horario_inicio = ?, horario_almoco  = ?,
                horario_volta  = ?, horario_saida   = ?
            WHERE id_registro = ?""";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt   (1,  r.getFkPenaId());
            st.setDate  (2,  r.getDataTrabalho());
            st.setDouble(3,  r.getHorasCumpridas());
            st.setString(4,  r.getAtividades());
            st.setTime  (5,  r.getHorarioInicio());
            st.setTime  (6,  r.getHorarioAlmoco());
            st.setTime  (7,  r.getHorarioVolta());
            st.setTime  (8, r.getHorarioSaida());
            st.setInt   (9, r.getIdRegistro());

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<RegistroDeTrabalho> buscarPorUsuarioEPena(int idUsuario, int idPena) {
        List<RegistroDeTrabalho> lista = new ArrayList<>();
        final String sql = """
        SELECT * FROM RegistroDeTrabalho
         WHERE fk_usuario_id_usuario = ?
           AND fk_pena_id_pena       = ?
         ORDER BY data_trabalho
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, idUsuario);
            st.setInt(2, idPena);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    RegistroDeTrabalho r = new RegistroDeTrabalho();
                    r.setIdRegistro       (rs.getInt("id_registro"));
                    r.setDataTrabalho     (rs.getDate("data_trabalho"));
                    r.setHorasCumpridas   (rs.getDouble("horas_cumpridas"));
                    // ... complete os demais campos se precisar
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    private static RegistroDeTrabalho mapear(ResultSet rs) throws SQLException {
        RegistroDeTrabalho r = new RegistroDeTrabalho();
        r.setIdRegistro       (rs.getInt   ("id_registro"));
        r.setFkPenaId         (rs.getInt   ("fk_pena_id_pena"));
        r.setDataTrabalho     (rs.getDate  ("data_trabalho"));
        r.setHorasCumpridas   (rs.getDouble("horas_cumpridas"));
        r.setAtividades       (rs.getString("atividades"));
        r.setHorarioInicio    (rs.getTime  ("horario_inicio"));
        r.setHorarioAlmoco    (rs.getTime  ("horario_almoco"));
        r.setHorarioVolta     (rs.getTime  ("horario_volta"));
        r.setHorarioSaida     (rs.getTime  ("horario_saida"));
        return r;
    }


    public boolean deletar(int idRegistro) {
        String sql = "DELETE FROM RegistroDeTrabalho WHERE id_registro = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRegistro);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RegistroDeTrabalho buscarPorId(int idRegistro) {
        String sql = "SELECT * FROM RegistroDeTrabalho WHERE id_registro = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRegistro);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setIdRegistro(rs.getInt("id_registro"));
                registro.setFkPenaId(rs.getInt("fk_pena_id_pena"));
                registro.setDataTrabalho(rs.getDate("data_trabalho"));
                registro.setHorasCumpridas(rs.getInt("horas_cumpridas"));
                registro.setAtividades(rs.getString("atividades"));
                registro.setHorarioInicio(rs.getTime("horario_inicio"));
                registro.setHorarioAlmoco(rs.getTime("horario_almoco"));
                registro.setHorarioVolta(rs.getTime("horario_volta"));
                registro.setHorarioSaida(rs.getTime("horario_saida"));
                return registro;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RegistroDeTrabalho> listarTodos() {
        List<RegistroDeTrabalho> registros = new ArrayList<>();
        String sql = "SELECT * FROM RegistroDeTrabalho";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setIdRegistro(rs.getInt("id_registro"));
                registro.setFkPenaId(rs.getInt("fk_pena_id_pena"));
                registro.setDataTrabalho(rs.getDate("data_trabalho"));
                registro.setHorasCumpridas(rs.getInt("horas_cumpridas"));
                registro.setAtividades(rs.getString("atividades"));
                registro.setHorarioInicio(rs.getTime("horario_inicio"));
                registro.setHorarioAlmoco(rs.getTime("horario_almoco"));
                registro.setHorarioVolta(rs.getTime("horario_volta"));
                registro.setHorarioSaida(rs.getTime("horario_saida"));
                registros.add(registro);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public static List<RegistroDeTrabalho> buscarPorUsuario(int idUsuario) {
        List<RegistroDeTrabalho> lista = new ArrayList<>();
        String sql = "SELECT * FROM RegistroDeTrabalho WHERE fk_usuario_id_usuario = ? ORDER BY data_trabalho";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RegistroDeTrabalho registro = new RegistroDeTrabalho();
                    registro.setIdRegistro(rs.getInt("id_registro"));
                    registro.setFkPenaId(rs.getInt("fk_pena_id_pena"));
                    registro.setDataTrabalho(rs.getDate("data_trabalho"));
                    registro.setHorasCumpridas(rs.getDouble("horas_cumpridas"));
                    registro.setAtividades(rs.getString("atividades"));
                    registro.setHorarioInicio(rs.getTime("horario_inicio"));
                    registro.setHorarioAlmoco(rs.getTime("horario_almoco"));
                    registro.setHorarioVolta(rs.getTime("horario_volta"));
                    registro.setHorarioSaida(rs.getTime("horario_saida"));
                    lista.add(registro);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

}
