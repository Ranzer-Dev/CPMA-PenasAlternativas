package util;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Utilitário para trabalhar com datas no SQLite.
 * SQLite armazena datas como TEXT, então precisamos tratar valores NULL
 * e formatos inválidos corretamente.
 */
public class SQLiteDateUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Lê uma data do ResultSet de forma segura, tratando valores NULL e inválidos.
     * 
     * @param rs ResultSet
     * @param columnName Nome da coluna
     * @return Date ou null se o valor for NULL ou inválido
     * @throws SQLException
     */
    public static Date getDate(ResultSet rs, String columnName) throws SQLException {
        try {
            // Tenta ler como string primeiro (formato SQLite)
            String dateStr = rs.getString(columnName);
            
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }
            
            // Verifica se é um valor numérico inválido (migração de SQL Server)
            if (dateStr.matches("^-?\\d+$")) {
                // É um número, provavelmente um timestamp inválido
                // Tenta converter de timestamp se for positivo e válido
                try {
                    long timestamp = Long.parseLong(dateStr);
                    if (timestamp > 0 && timestamp < System.currentTimeMillis() + (100L * 365 * 24 * 60 * 60 * 1000)) {
                        return new Date(timestamp);
                    }
                } catch (NumberFormatException e) {
                    // Ignora e retorna null
                }
                return null;
            }
            
            // Tenta fazer parse da string de data
            try {
                // Tenta formato datetime primeiro
                if (dateStr.contains(" ")) {
                    return new Date(DATETIME_FORMAT.parse(dateStr).getTime());
                } else {
                    // Formato date apenas
                    return new Date(DATE_FORMAT.parse(dateStr).getTime());
                }
            } catch (ParseException e) {
                // Se não conseguir fazer parse, retorna null
                System.err.println("Erro ao fazer parse da data '" + dateStr + "': " + e.getMessage());
                return null;
            }
            
        } catch (SQLException e) {
            // Se houver erro ao ler a coluna, retorna null
            return null;
        }
    }
}

