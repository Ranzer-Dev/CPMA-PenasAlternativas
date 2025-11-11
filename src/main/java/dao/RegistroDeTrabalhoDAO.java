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

    public boolean inserir(RegistroDeTrabalho registro) {
        String sql = "INSERT INTO RegistroDeTrabalho (fk_pena_id_pena, data_trabalho, horas_cumpridas, atividades, horario_inicio, horario_almoco, horario_volta, horario_saida) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registro.getFkPenaId());
            if (registro.getDataTrabalho() != null) {
                stmt.setString(2, registro.getDataTrabalho().toString());
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }
            stmt.setDouble(3, registro.getHorasCumpridas());
            stmt.setString(4, registro.getAtividades());
            if (registro.getHorarioInicio() != null) {
                stmt.setString(5, SQLiteTimeUtil.timeToString(registro.getHorarioInicio()));
            } else {
                stmt.setNull(5, java.sql.Types.TIME);
            }
            if (registro.getHorarioAlmoco() != null) {
                stmt.setString(6, SQLiteTimeUtil.timeToString(registro.getHorarioAlmoco()));
            } else {
                stmt.setNull(6, java.sql.Types.TIME);
            }
            if (registro.getHorarioVolta() != null) {
                stmt.setString(7, SQLiteTimeUtil.timeToString(registro.getHorarioVolta()));
            } else {
                stmt.setNull(7, java.sql.Types.TIME);
            }
            if (registro.getHorarioSaida() != null) {
                stmt.setString(8, SQLiteTimeUtil.timeToString(registro.getHorarioSaida()));
            } else {
                stmt.setNull(8, java.sql.Types.TIME);
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
                data_trabalho  = ?, horas_cumpridas = ?, atividades = ?,
                horario_inicio = ?, horario_almoco  = ?,
                horario_volta  = ?, horario_saida   = ?
            WHERE id_registro = ?""";

        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, r.getFkPenaId());
            if (r.getDataTrabalho() != null) {
                st.setString(2, r.getDataTrabalho().toString());
            } else {
                st.setNull(2, java.sql.Types.DATE);
            }
            st.setDouble(3, r.getHorasCumpridas());
            st.setString(4, r.getAtividades());
            if (r.getHorarioInicio() != null) {
                st.setString(5, SQLiteTimeUtil.timeToString(r.getHorarioInicio()));
            } else {
                st.setNull(5, java.sql.Types.TIME);
            }
            if (r.getHorarioAlmoco() != null) {
                st.setString(6, SQLiteTimeUtil.timeToString(r.getHorarioAlmoco()));
            } else {
                st.setNull(6, java.sql.Types.TIME);
            }
            if (r.getHorarioVolta() != null) {
                st.setString(7, SQLiteTimeUtil.timeToString(r.getHorarioVolta()));
            } else {
                st.setNull(7, java.sql.Types.TIME);
            }
            if (r.getHorarioSaida() != null) {
                st.setString(8, SQLiteTimeUtil.timeToString(r.getHorarioSaida()));
            } else {
                st.setNull(8, java.sql.Types.TIME);
            }
            st.setInt(9, r.getIdRegistro());

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<RegistroDeTrabalho> buscarPorUsuarioEPena(int idUsuario, int idPena) {
        // Observação: a tabela RegistroDeTrabalho não possui coluna fk_usuario_id_usuario.
        // Filtramos pelos registros da pena informada (idPena). O parâmetro idUsuario é ignorado.
        List<RegistroDeTrabalho> lista = new ArrayList<>();
        final String sql = """
        SELECT *
          FROM RegistroDeTrabalho
         WHERE fk_pena_id_pena = ?
         ORDER BY data_trabalho
        """;

        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, idPena);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
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
                    lista.add(r);
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
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setIdRegistro(rs.getInt("id_registro"));
                registro.setFkPenaId(rs.getInt("fk_pena_id_pena"));
                registro.setDataTrabalho(SQLiteDateUtil.getDate(rs, "data_trabalho"));
                registro.setHorasCumpridas(rs.getDouble("horas_cumpridas"));
                registro.setAtividades(rs.getString("atividades"));
                registro.setHorarioInicio(SQLiteTimeUtil.getTime(rs, "horario_inicio"));
                registro.setHorarioAlmoco(SQLiteTimeUtil.getTime(rs, "horario_almoco"));
                registro.setHorarioVolta(SQLiteTimeUtil.getTime(rs, "horario_volta"));
                registro.setHorarioSaida(SQLiteTimeUtil.getTime(rs, "horario_saida"));
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
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setIdRegistro(rs.getInt("id_registro"));
                registro.setFkPenaId(rs.getInt("fk_pena_id_pena"));
                registro.setDataTrabalho(SQLiteDateUtil.getDate(rs, "data_trabalho"));
                registro.setHorasCumpridas(rs.getDouble("horas_cumpridas"));
                registro.setAtividades(rs.getString("atividades"));
                registro.setHorarioInicio(SQLiteTimeUtil.getTime(rs, "horario_inicio"));
                registro.setHorarioAlmoco(SQLiteTimeUtil.getTime(rs, "horario_almoco"));
                registro.setHorarioVolta(SQLiteTimeUtil.getTime(rs, "horario_volta"));
                registro.setHorarioSaida(SQLiteTimeUtil.getTime(rs, "horario_saida"));
                registros.add(registro);
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
                lista.add(r);
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
        
        String sql = "INSERT INTO RegistroDeTrabalho (fk_pena_id_pena, data_trabalho, horas_cumpridas, atividades, horario_inicio, horario_almoco, horario_volta, horario_saida) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (RegistroDeTrabalho registro : registros) {
                stmt.setInt(1, registro.getFkPenaId());
                if (registro.getDataTrabalho() != null) {
                    stmt.setString(2, registro.getDataTrabalho().toString());
                } else {
                    stmt.setNull(2, java.sql.Types.DATE);
                }
                stmt.setDouble(3, registro.getHorasCumpridas());
                stmt.setString(4, registro.getAtividades());
                if (registro.getHorarioInicio() != null) {
                    stmt.setString(5, SQLiteTimeUtil.timeToString(registro.getHorarioInicio()));
                } else {
                    stmt.setNull(5, java.sql.Types.TIME);
                }
                if (registro.getHorarioAlmoco() != null) {
                    stmt.setString(6, SQLiteTimeUtil.timeToString(registro.getHorarioAlmoco()));
                } else {
                    stmt.setNull(6, java.sql.Types.TIME);
                }
                if (registro.getHorarioVolta() != null) {
                    stmt.setString(7, SQLiteTimeUtil.timeToString(registro.getHorarioVolta()));
                } else {
                    stmt.setNull(7, java.sql.Types.TIME);
                }
                if (registro.getHorarioSaida() != null) {
                    stmt.setString(8, SQLiteTimeUtil.timeToString(registro.getHorarioSaida()));
                } else {
                    stmt.setNull(8, java.sql.Types.TIME);
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
