package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import database.ConnectionFactory;
import model.LogAcessoApenado;

/**
 * DAO de auditoria das tentativas de acesso pelo totem.
 * Toda tentativa (sucesso ou falha) é registrada para evitar que
 * abusos passem despercebidos pelo administrador.
 */
public class LogAcessoApenadoDAO {

    private static final DateTimeFormatter SQLITE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void registrar(Integer idUsuario,
                                  Integer idCodigoAcesso,
                                  String metodo,
                                  boolean sucesso,
                                  String codigoTentado,
                                  String mensagem) {
        String sql = "INSERT INTO LogAcessoApenado "
                + "(fk_usuario_id_usuario, fk_codigo_acesso_id, metodo, sucesso, codigo_tentado, mensagem, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idUsuario != null) {
                stmt.setInt(1, idUsuario);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            if (idCodigoAcesso != null) {
                stmt.setInt(2, idCodigoAcesso);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, metodo);
            stmt.setInt(4, sucesso ? 1 : 0);
            stmt.setString(5, codigoTentado);
            stmt.setString(6, mensagem);
            stmt.setString(7, LocalDateTime.now().format(SQLITE_FMT));

            stmt.executeUpdate();
        } catch (SQLException e) {
            // Auditoria nunca pode quebrar o fluxo do usuário; apenas registra.
            System.err.println("Erro ao registrar log de acesso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<LogAcessoApenado> listarPorUsuario(int idUsuario, int limite) {
        List<LogAcessoApenado> lista = new ArrayList<>();
        String sql = "SELECT * FROM LogAcessoApenado "
                + "WHERE fk_usuario_id_usuario = ? "
                + "ORDER BY id_log DESC LIMIT ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, Math.max(1, limite));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private static LogAcessoApenado mapear(ResultSet rs) throws SQLException {
        LogAcessoApenado log = new LogAcessoApenado();
        log.setIdLog(rs.getInt("id_log"));
        int idUser = rs.getInt("fk_usuario_id_usuario");
        log.setFkUsuarioIdUsuario(rs.wasNull() ? null : idUser);
        int idCod = rs.getInt("fk_codigo_acesso_id");
        log.setFkCodigoAcessoId(rs.wasNull() ? null : idCod);
        log.setMetodo(rs.getString("metodo"));
        log.setSucesso(rs.getInt("sucesso") == 1);
        log.setCodigoTentado(rs.getString("codigo_tentado"));
        log.setMensagem(rs.getString("mensagem"));
        String dataHora = rs.getString("data_hora");
        if (dataHora != null && !dataHora.isBlank()) {
            try {
                log.setDataHora(LocalDateTime.parse(dataHora, SQLITE_FMT));
            } catch (Exception ex) {
                try {
                    log.setDataHora(LocalDateTime.parse(dataHora.replace(' ', 'T')));
                } catch (Exception ignored) {
                    log.setDataHora(null);
                }
            }
        }
        return log;
    }
}
