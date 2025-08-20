package dao;

import database.ConnectionFactory;
import model.AcordoDeTrabalho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcordoDeTrabalhoDAO {

    /* ---------- INSERT ---------- */
    public static int inserir(AcordoDeTrabalho a) {
        String sql = """
                INSERT INTO AcordoDeTrabalho
                  (dias_semana_e_horarios_disponivel,
                   atividades_acordadas,
                   fk_usuario_id_usuario,
                   fk_instituicao_id_instituicao)
                OUTPUT INSERTED.id_acordo_de_trabalho
                VALUES (?, ?, ?, ?)""";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setString(1, a.getDiasSemanaEHorarios());
            st.setString(2, a.getAtividadesAcordadas());
            st.setInt   (3, a.getIdUsuario());
            st.setInt   (4, a.getIdInstituicao());


            ResultSet rs = st.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* ---------- SELECT * ---------- */
    public static List<AcordoDeTrabalho> buscarTodos() {
        String sql = """
                SELECT a.*, u.nome AS usuario
                  FROM AcordoDeTrabalho a
                  JOIN Usuario u ON u.id_usuario = a.fk_usuario_id_usuario""";

        List<AcordoDeTrabalho> lista = new ArrayList<>();

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                AcordoDeTrabalho a = new AcordoDeTrabalho();
                a.setIdAcordo(rs.getInt("id_acordo_de_trabalho"));
                a.setDiasSemanaEHorarios(
                        rs.getString("dias_semana_e_horarios_disponivel"));
                a.setAtividadesAcordadas(rs.getString("atividades_acordadas"));
                a.setIdUsuario(rs.getInt("fk_usuario_id_usuario"));
                a.setNomeUsuario(rs.getString("usuario"));      // <- p/ ComboBox
                lista.add(a);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /* ---------- SELECT por ID ---------- */
    public static AcordoDeTrabalho buscarPorId(int id) {
        String sql = """
                SELECT a.*, u.nome AS usuario
                  FROM AcordoDeTrabalho a
                  JOIN Usuario u ON u.id_usuario = a.fk_usuario_id_usuario
                 WHERE id_acordo_de_trabalho = ?""";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                AcordoDeTrabalho a = new AcordoDeTrabalho();
                a.setIdAcordo(rs.getInt("id_acordo_de_trabalho"));
                a.setDiasSemanaEHorarios(
                        rs.getString("dias_semana_e_horarios_disponivel"));
                a.setAtividadesAcordadas(rs.getString("atividades_acordadas"));
                a.setIdUsuario(rs.getInt("fk_usuario_id_usuario"));
                a.setNomeUsuario(rs.getString("usuario"));
                return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
