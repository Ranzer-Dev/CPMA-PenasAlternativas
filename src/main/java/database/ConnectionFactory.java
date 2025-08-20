package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=penas_alternativas;"+
            "encrypt=true;trustServerCertificate=true;user=sa;password=dbo@123574896";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados:");
            e.printStackTrace();
            return null;
        }
    }
}
