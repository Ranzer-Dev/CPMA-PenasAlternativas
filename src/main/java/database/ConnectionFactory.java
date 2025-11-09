package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = DatabaseConfig.getDbUrl();
    private static final String USER = DatabaseConfig.getDbUser();
    private static final String PASSWORD = DatabaseConfig.getDbPassword();

    public static Connection getConnection() {
        try {
            // SQLite não precisa de usuário e senha
            if (URL != null && URL.startsWith("jdbc:sqlite:")) {
                return DriverManager.getConnection(URL);
            } else {
                // Para outros bancos de dados (compatibilidade)
                return DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados:");
            e.printStackTrace();
            return null;
        }
    }
}
