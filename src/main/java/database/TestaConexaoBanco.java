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

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT DB_NAME() AS CurrentDatabase")) {

                if (rs.next()) {
                    System.out.println("Conectado ao banco: " + rs.getString("CurrentDatabase"));
                } else {
                    System.out.println("Não foi possível obter o nome do banco.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro durante operação no banco:");
            e.printStackTrace();
        }
    }
}
