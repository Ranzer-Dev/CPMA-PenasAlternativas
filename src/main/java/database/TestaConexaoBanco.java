package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestaConexaoBanco {

    public static void main(String[] args) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                System.err.println("Falha ao obter conexão com o banco.");
                return;
            }

            String dbName = conn.getCatalog();
            String product = conn.getMetaData().getDatabaseProductName();
            System.out.println("Conectado ao banco com sucesso!");
            System.out.println("Engine: " + product);
            if (dbName != null) {
                System.out.println("Catálogo/Banco: " + dbName);
            }
        } catch (SQLException e) {
            System.err.println("Erro durante operação no banco:");
            e.printStackTrace();
        }
    }
}
