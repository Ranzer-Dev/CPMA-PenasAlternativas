package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import database.ConnectionFactory;
import model.CodigoAcessoApenado;
import util.CodigoAcessoUtil;

/**
 * DAO para o ciclo de vida dos códigos de acesso do apenado ao totem.
 */
public class CodigoAcessoApenadoDAO {

    private static final DateTimeFormatter SQLITE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Cancela códigos ATIVOS anteriores e gera um novo código numérico
     * de acesso para o usuário, garantindo unicidade entre os ATIVOS.
     *
     * @return o registro persistido (com ID e código preenchidos) ou {@code null}
     *         em caso de falha.
     */
    public static CodigoAcessoApenado gerarParaUsuario(int idUsuario, int idAdmin, int validadeHoras) {
        cancelarAtivosDoUsuario(idUsuario);

        String codigo = sortearCodigoUnico();
        if (codigo == null) {
            return null;
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expira = agora.plusHours(Math.max(1, validadeHoras));

        String sql = "INSERT INTO CodigoAcessoApenado "
                + "(fk_usuario_id_usuario, fk_admin_id_admin, codigo, data_geracao, data_expiracao, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ATIVO')";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idAdmin);
            stmt.setString(3, codigo);
            stmt.setString(4, agora.format(SQLITE_FMT));
            stmt.setString(5, expira.format(SQLITE_FMT));

            if (stmt.executeUpdate() == 0) {
                return null;
            }

            CodigoAcessoApenado registro = new CodigoAcessoApenado();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    registro.setIdCodigoAcesso(rs.getInt(1));
                }
            }
            registro.setFkUsuarioIdUsuario(idUsuario);
            registro.setFkAdminIdAdmin(idAdmin);
            registro.setCodigo(codigo);
            registro.setDataGeracao(agora);
            registro.setDataExpiracao(expira);
            registro.setStatus(CodigoAcessoApenado.STATUS_ATIVO);
            return registro;

        } catch (SQLException e) {
            System.err.println("Erro ao gerar código de acesso: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Localiza um código ATIVO informado pelo apenado no totem.
     * Antes da consulta, marca como EXPIRADO os que já passaram da
     * data de expiração para manter a integridade do estado.
     */
    public static CodigoAcessoApenado buscarAtivoPorCodigo(String codigoBruto) {
        String codigo = CodigoAcessoUtil.normalizar(codigoBruto);
        if (codigo.isEmpty()) {
            return null;
        }
        marcarExpirados();

        String sql = "SELECT * FROM CodigoAcessoApenado "
                + "WHERE codigo = ? AND status = 'ATIVO' "
                + "ORDER BY id_codigo_acesso DESC LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar código de acesso: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static CodigoAcessoApenado buscarAtivoPorUsuario(int idUsuario) {
        marcarExpirados();
        String sql = "SELECT * FROM CodigoAcessoApenado "
                + "WHERE fk_usuario_id_usuario = ? AND status = 'ATIVO' "
                + "ORDER BY id_codigo_acesso DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Marca um código como utilizado (uso único). Usa CAS (compare-and-swap)
     * para impedir uso concorrente por dois pontos diferentes.
     */
    public static boolean marcarComoUsado(int idCodigo) {
        String sql = "UPDATE CodigoAcessoApenado "
                + "SET status = 'USADO', data_uso = ? "
                + "WHERE id_codigo_acesso = ? AND status = 'ATIVO'";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, LocalDateTime.now().format(SQLITE_FMT));
            stmt.setInt(2, idCodigo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int cancelarAtivosDoUsuario(int idUsuario) {
        String sql = "UPDATE CodigoAcessoApenado "
                + "SET status = 'CANCELADO' "
                + "WHERE fk_usuario_id_usuario = ? AND status = 'ATIVO'";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Atualiza para EXPIRADO os códigos cuja janela de validade já passou.
     */
    public static void marcarExpirados() {
        String sql = "UPDATE CodigoAcessoApenado "
                + "SET status = 'EXPIRADO' "
                + "WHERE status = 'ATIVO' AND data_expiracao < ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, LocalDateTime.now().format(SQLITE_FMT));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String sortearCodigoUnico() {
        // 8 dígitos = 100 milhões de combinações. Mesmo com colisão pontual,
        // alguns retries são suficientes para encontrar um código livre entre os ATIVOS.
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            String candidato = CodigoAcessoUtil.gerarCodigo();
            if (!existeAtivo(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    private static boolean existeAtivo(String codigo) {
        String sql = "SELECT 1 FROM CodigoAcessoApenado WHERE codigo = ? AND status = 'ATIVO' LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private static CodigoAcessoApenado mapear(ResultSet rs) throws SQLException {
        CodigoAcessoApenado c = new CodigoAcessoApenado();
        c.setIdCodigoAcesso(rs.getInt("id_codigo_acesso"));
        c.setFkUsuarioIdUsuario(rs.getInt("fk_usuario_id_usuario"));
        c.setFkAdminIdAdmin(rs.getInt("fk_admin_id_admin"));
        c.setCodigo(rs.getString("codigo"));
        c.setDataGeracao(parse(rs.getString("data_geracao")));
        c.setDataExpiracao(parse(rs.getString("data_expiracao")));
        c.setDataUso(parse(rs.getString("data_uso")));
        c.setStatus(rs.getString("status"));
        return c;
    }

    private static LocalDateTime parse(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor, SQLITE_FMT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(valor.replace(' ', 'T'));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
