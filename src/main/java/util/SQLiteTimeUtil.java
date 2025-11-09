package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Utilitário para trabalhar com horários (TIME) no SQLite.
 * SQLite armazena horários como TEXT, então precisamos tratar valores NULL
 * e formatos inválidos corretamente.
 */
public class SQLiteTimeUtil {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Lê um horário do ResultSet de forma segura, tratando valores NULL e inválidos.
     * 
     * @param rs ResultSet
     * @param columnName Nome da coluna
     * @return Time ou null se o valor for NULL ou inválido
     * @throws SQLException
     */
    public static Time getTime(ResultSet rs, String columnName) throws SQLException {
        try {
            // Tenta ler como string primeiro (formato SQLite)
            String timeStr = rs.getString(columnName);
            
            if (timeStr == null || timeStr.trim().isEmpty()) {
                return null;
            }
            
            // Tenta fazer parse da string de horário
            try {
                // Remove frações de segundo se existirem
                if (timeStr.contains(".")) {
                    timeStr = timeStr.substring(0, timeStr.indexOf("."));
                }
                
                // Garante formato HH:mm:ss
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    timeStr = timeStr + ":00";
                }
                
                return Time.valueOf(timeStr);
            } catch (IllegalArgumentException e) {
                // Se não conseguir fazer parse, tenta com SimpleDateFormat
                try {
                    java.util.Date date = TIME_FORMAT.parse(timeStr);
                    return new Time(date.getTime());
                } catch (ParseException pe) {
                    System.err.println("Erro ao fazer parse do horário '" + timeStr + "': " + pe.getMessage());
                    return null;
                }
            }
            
        } catch (SQLException e) {
            // Se houver erro ao ler a coluna, retorna null
            return null;
        }
    }
    
    /**
     * Converte um Time para String no formato SQLite (HH:mm:ss).
     * 
     * @param time Time a ser convertido
     * @return String no formato HH:mm:ss ou null se time for null
     */
    public static String timeToString(Time time) {
        if (time == null) {
            return null;
        }
        return time.toString(); // Time.toString() já retorna HH:mm:ss
    }
}

