package dao;

import database.ConnectionFactory;
import model.DisponibilidadeInstituicao;
import util.SQLiteTimeUtil;

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
            if (disp.getHoraInicio1() != null) {
                stmt.setString(2, SQLiteTimeUtil.timeToString(Time.valueOf(disp.getHoraInicio1())));
            } else {
                stmt.setNull(2, Types.TIME);
            }
            if (disp.getHoraFim1() != null) {
                stmt.setString(3, SQLiteTimeUtil.timeToString(Time.valueOf(disp.getHoraFim1())));
            } else {
                stmt.setNull(3, Types.TIME);
            }

            if (disp.getHoraInicio2() != null && disp.getHoraFim2() != null) {
                stmt.setString(4, SQLiteTimeUtil.timeToString(Time.valueOf(disp.getHoraInicio2())));
                stmt.setString(5, SQLiteTimeUtil.timeToString(Time.valueOf(disp.getHoraFim2())));
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
                
                Time hi1 = SQLiteTimeUtil.getTime(rs, "hora_inicio_1");
                Time hf1 = SQLiteTimeUtil.getTime(rs, "hora_fim_1");
                disp.setHoraInicio1(hi1 != null ? hi1.toLocalTime() : null);
                disp.setHoraFim1(hf1 != null ? hf1.toLocalTime() : null);

                Time hi2 = SQLiteTimeUtil.getTime(rs, "hora_inicio_2");
                Time hf2 = SQLiteTimeUtil.getTime(rs, "hora_fim_2");
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

    public static boolean removerPorInstituicaoId(int idInstituicao) {
        String sql = "DELETE FROM disponibilidade_instituicao WHERE fk_instituicao_id_instituicao = ?";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idInstituicao);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
