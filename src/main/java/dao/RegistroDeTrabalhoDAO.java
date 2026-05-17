package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.ConnectionFactory;
import model.RegistroDeTrabalho;
import util.SQLiteDateUtil;
import util.SQLiteTimeUtil;

public class RegistroDeTrabalhoDAO {

    private static void mapearInstituicao(ResultSet rs, RegistroDeTrabalho r) throws SQLException {
        int idInst = rs.getInt("fk_instituicao_id_instituicao");
        if (!rs.wasNull()) {
            r.setFkInstituicaoIdInstituicao(idInst);
        }
    }

    private static RegistroDeTrabalho mapear(ResultSet rs) throws SQLException {
        RegistroDeTrabalho r = new RegistroDeTrabalho();
        r.setIdRegistro(rs.getInt("id_registro"));
        r.setFkPenaId(rs.getInt("fk_pena_id_pena"));
        r.setDataTrabalho(SQLiteDateUtil.getDate(rs, "data_trabalho"));
        r.setHorasCumpridas(rs.getDouble("horas_cumpridas"));
        r.setAtividades(rs.getString("atividades"));
        r.setHorarioInicio(SQLiteTimeUtil.getTime(rs, "horario_inicio"));
        r.setHorarioAlmoco(SQLiteTimeUtil.getTime(rs, "horario_almoco"));
        r.setHorarioVolta(SQLiteTimeUtil.getTime(rs, "horario_volta"));
        r.setHorarioSaida(SQLiteTimeUtil.getTime(rs, "horario_saida"));
        mapearInstituicao(rs, r);
        return r;
    }

    private static void definirInstituicao(PreparedStatement stmt, int index, Integer idInstituicao) throws SQLException {
        if (idInstituicao != null && idInstituicao > 0) {
            stmt.setInt(index, idInstituicao);
        } else {
            stmt.setNull(index, java.sql.Types.INTEGER);
        }
    }

    public boolean inserir(RegistroDeTrabalho registro) {
        String sql = "INSERT INTO RegistroDeTrabalho (fk_pena_id_pena, fk_instituicao_id_instituicao, data_trabalho, horas_cumpridas, atividades, horario_inicio, horario_almoco, horario_volta, horario_saida) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registro.getFkPenaId());
            definirInstituicao(stmt, 2, registro.getFkInstituicaoIdInstituicao());
            if (registro.getDataTrabalho() != null) {
                stmt.setString(3, registro.getDataTrabalho().toString());
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            stmt.setDouble(4, registro.getHorasCumpridas());
            stmt.setString(5, registro.getAtividades());
            if (registro.getHorarioInicio() != null) {
                stmt.setString(6, SQLiteTimeUtil.timeToString(registro.getHorarioInicio()));
            } else {
                stmt.setNull(6, java.sql.Types.TIME);
            }
            if (registro.getHorarioAlmoco() != null) {
                stmt.setString(7, SQLiteTimeUtil.timeToString(registro.getHorarioAlmoco()));
            } else {
                stmt.setNull(7, java.sql.Types.TIME);
            }
            if (registro.getHorarioVolta() != null) {
                stmt.setString(8, SQLiteTimeUtil.timeToString(registro.getHorarioVolta()));
            } else {
                stmt.setNull(8, java.sql.Types.TIME);
            }
            if (registro.getHorarioSaida() != null) {
                stmt.setString(9, SQLiteTimeUtil.timeToString(registro.getHorarioSaida()));
            } else {
                stmt.setNull(9, java.sql.Types.TIME);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizar(RegistroDeTrabalho r) {
        final String sql = """
            UPDATE RegistroDeTrabalho SET
                fk_pena_id_pena = ?,
                fk_instituicao_id_instituicao = ?,
                data_trabalho  = ?, horas_cumpridas = ?, atividades = ?,
                horario_inicio = ?, horario_almoco  = ?,
                horario_volta  = ?, horario_saida   = ?
            WHERE id_registro = ?""";

        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, r.getFkPenaId());
            definirInstituicao(st, 2, r.getFkInstituicaoIdInstituicao());
            if (r.getDataTrabalho() != null) {
                st.setString(3, r.getDataTrabalho().toString());
            } else {
                st.setNull(3, java.sql.Types.DATE);
            }
            st.setDouble(4, r.getHorasCumpridas());
            st.setString(5, r.getAtividades());
            if (r.getHorarioInicio() != null) {
                st.setString(6, SQLiteTimeUtil.timeToString(r.getHorarioInicio()));
            } else {
                st.setNull(6, java.sql.Types.TIME);
            }
            if (r.getHorarioAlmoco() != null) {
                st.setString(7, SQLiteTimeUtil.timeToString(r.getHorarioAlmoco()));
            } else {
                st.setNull(7, java.sql.Types.TIME);
            }
            if (r.getHorarioVolta() != null) {
                st.setString(8, SQLiteTimeUtil.timeToString(r.getHorarioVolta()));
            } else {
                st.setNull(8, java.sql.Types.TIME);
            }
            if (r.getHorarioSaida() != null) {
                st.setString(9, SQLiteTimeUtil.timeToString(r.getHorarioSaida()));
            } else {
                st.setNull(9, java.sql.Types.TIME);
            }
            st.setInt(10, r.getIdRegistro());

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<RegistroDeTrabalho> buscarPorUsuarioEPena(int idUsuario, int idPena) {
        // Segurança: retorna registros apenas quando a pena pertence ao usuário informado.
        List<RegistroDeTrabalho> lista = new ArrayList<>();
        final String sql = """
        SELECT rt.*
          FROM RegistroDeTrabalho rt
          JOIN Pena p ON p.id_pena = rt.fk_pena_id_pena
         WHERE rt.fk_pena_id_pena = ?
           AND p.fk_usuario_id_usuario = ?
         ORDER BY data_trabalho
        """;

        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, idPena);
            st.setInt(2, idUsuario);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean deletar(int idRegistro) {
        String sql = "DELETE FROM RegistroDeTrabalho WHERE id_registro = ?";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRegistro);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RegistroDeTrabalho buscarPorId(int idRegistro) {
        String sql = "SELECT * FROM RegistroDeTrabalho WHERE id_registro = ?";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRegistro);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RegistroDeTrabalho> listarTodos() {
        List<RegistroDeTrabalho> registros = new ArrayList<>();
        String sql = "SELECT * FROM RegistroDeTrabalho";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                registros.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    public static List<RegistroDeTrabalho> buscarPorUsuario(int idUsuario) {
        // Não há fk do usuário em RegistroDeTrabalho. Para obter por usuário seria necessário JOIN com Pena.
        // Mantemos o método por compatibilidade, retornando lista vazia.
        return new ArrayList<>();
    }

    /**
     * Busca a última data de trabalho cadastrada para uma pena.
     * Retorna null se não houver registros.
     */
    public static java.sql.Date buscarUltimaDataPorPena(int idPena) {
        String sql = "SELECT data_trabalho FROM RegistroDeTrabalho WHERE fk_pena_id_pena = ? ORDER BY data_trabalho DESC LIMIT 1";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idPena);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return SQLiteDateUtil.getDate(rs, "data_trabalho");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca registros de uma pena para um mês e ano específico.
     */
    public static List<RegistroDeTrabalho> buscarPorPenaEMes(int idPena, int mes, int ano) {
        List<RegistroDeTrabalho> lista = new ArrayList<>();
        String sql = "SELECT * FROM RegistroDeTrabalho WHERE fk_pena_id_pena = ? " +
                     "AND CAST(strftime('%m', data_trabalho) AS INTEGER) = ? AND CAST(strftime('%Y', data_trabalho) AS INTEGER) = ? " +
                     "ORDER BY data_trabalho";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPena);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);
            
            ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Insere múltiplos registros de uma vez (batch insert).
     */
    public boolean inserirBatch(List<RegistroDeTrabalho> registros) {
        if (registros == null || registros.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO RegistroDeTrabalho (fk_pena_id_pena, fk_instituicao_id_instituicao, data_trabalho, horas_cumpridas, atividades, horario_inicio, horario_almoco, horario_volta, horario_saida) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (RegistroDeTrabalho registro : registros) {
                stmt.setInt(1, registro.getFkPenaId());
                definirInstituicao(stmt, 2, registro.getFkInstituicaoIdInstituicao());
                if (registro.getDataTrabalho() != null) {
                    stmt.setString(3, registro.getDataTrabalho().toString());
                } else {
                    stmt.setNull(3, java.sql.Types.DATE);
                }
                stmt.setDouble(4, registro.getHorasCumpridas());
                stmt.setString(5, registro.getAtividades());
                if (registro.getHorarioInicio() != null) {
                    stmt.setString(6, SQLiteTimeUtil.timeToString(registro.getHorarioInicio()));
                } else {
                    stmt.setNull(6, java.sql.Types.TIME);
                }
                if (registro.getHorarioAlmoco() != null) {
                    stmt.setString(7, SQLiteTimeUtil.timeToString(registro.getHorarioAlmoco()));
                } else {
                    stmt.setNull(7, java.sql.Types.TIME);
                }
                if (registro.getHorarioVolta() != null) {
                    stmt.setString(8, SQLiteTimeUtil.timeToString(registro.getHorarioVolta()));
                } else {
                    stmt.setNull(8, java.sql.Types.TIME);
                }
                if (registro.getHorarioSaida() != null) {
                    stmt.setString(9, SQLiteTimeUtil.timeToString(registro.getHorarioSaida()));
                } else {
                    stmt.setNull(9, java.sql.Types.TIME);
                }
                stmt.addBatch();
            }
            
            int[] resultados = stmt.executeBatch();
            // Verifica se todos foram inseridos com sucesso
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
