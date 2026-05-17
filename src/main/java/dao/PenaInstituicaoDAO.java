package dao;

import database.ConnectionFactory;
import model.Instituicao;
import model.Pena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PenaInstituicaoDAO {

    public static void salvarParaPena(int idPena, List<Integer> idsInstituicoes) {
        if (idsInstituicoes == null || idsInstituicoes.isEmpty()) {
            return;
        }
        removerPorPena(idPena);
        String sql = "INSERT OR IGNORE INTO pena_instituicao (fk_pena_id_pena, fk_instituicao_id_instituicao) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer idInst : idsInstituicoes) {
                if (idInst == null || idInst <= 0) {
                    continue;
                }
                stmt.setInt(1, idPena);
                stmt.setInt(2, idInst);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removerPorPena(int idPena) {
        String sql = "DELETE FROM pena_instituicao WHERE fk_pena_id_pena = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPena);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> buscarIdsPorPena(int idPena) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT fk_instituicao_id_instituicao FROM pena_instituicao WHERE fk_pena_id_pena = ? ORDER BY fk_instituicao_id_instituicao";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPena);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("fk_instituicao_id_instituicao"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (ids.isEmpty()) {
            Pena pena = PenaDAO.buscarPorId(idPena);
            if (pena != null && pena.getFkInstituicaoIdInstituicao() > 0) {
                ids.add(pena.getFkInstituicaoIdInstituicao());
            }
        }
        return ids;
    }

    public static List<Instituicao> buscarInstituicoesPorPena(int idPena) {
        List<Instituicao> lista = new ArrayList<>();
        for (int idInst : buscarIdsPorPena(idPena)) {
            Instituicao inst = InstituicaoDAO.buscarPorId(idInst);
            if (inst != null) {
                lista.add(inst);
            }
        }
        return lista;
    }

    public static String buscarNomesConcatenadosPorPena(int idPena) {
        return buscarInstituicoesPorPena(idPena).stream()
                .map(Instituicao::getNome)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.joining(", "));
    }

    public static boolean vinculada(int idPena, int idInstituicao) {
        return buscarIdsPorPena(idPena).contains(idInstituicao);
    }
}
