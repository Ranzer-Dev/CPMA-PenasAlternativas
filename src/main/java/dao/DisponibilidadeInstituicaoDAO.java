package dao;

import database.ConnectionFactory;
import model.DisponibilidadeInstituicao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadeInstituicaoDAO {

    public static boolean inserir(DisponibilidadeInstituicao disp) {
        String sql = """
            INSERT INTO disponibilidade_instituicao
                (dia_semana, hora_inicio_1, hora_fim_1, hora_inicio_2, hora_fim_2, fk_instituicao_id_instituicao)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, disp.getDiaSemana());
            stmt.setTime(2, Time.valueOf(disp.getHoraInicio1()));
            stmt.setTime(3, Time.valueOf(disp.getHoraFim1()));

            if (disp.getHoraInicio2() != null && disp.getHoraFim2() != null) {
                stmt.setTime(4, Time.valueOf(disp.getHoraInicio2()));
                stmt.setTime(5, Time.valueOf(disp.getHoraFim2()));
            } else {
                stmt.setNull(4, Types.TIME);
                stmt.setNull(5, Types.TIME);
            }

            stmt.setInt(6, disp.getFkInstituicaoId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<DisponibilidadeInstituicao> buscarPorInstituicaoId(int idInstituicao) {
        List<DisponibilidadeInstituicao> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM disponibilidade_instituicao
            WHERE fk_instituicao_id_instituicao = ?
        """;
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInstituicao);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DisponibilidadeInstituicao disp = new DisponibilidadeInstituicao();
                disp.setId(rs.getInt("id_disponibilidade"));
                disp.setDiaSemana(rs.getString("dia_semana"));
                disp.setHoraInicio1(rs.getTime("hora_inicio_1").toLocalTime());
                disp.setHoraFim1(rs.getTime("hora_fim_1").toLocalTime());

                Time hi2 = rs.getTime("hora_inicio_2");
                Time hf2 = rs.getTime("hora_fim_2");
                disp.setHoraInicio2(hi2 != null ? hi2.toLocalTime() : null);
                disp.setHoraFim2(hf2 != null ? hf2.toLocalTime() : null);
                disp.setFkInstituicaoId(idInstituicao);

                lista.add(disp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
