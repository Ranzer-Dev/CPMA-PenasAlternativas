package database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe responsável por inicializar o banco de dados SQLite,
 * criando as tabelas necessárias se elas não existirem.
 */
public class DatabaseInitializer {

    /**
     * Inicializa o banco de dados criando todas as tabelas necessárias.
     * Este método deve ser chamado na inicialização da aplicação.
     */
    public static void initialize() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                System.err.println("Não foi possível conectar ao banco de dados para inicialização.");
                return;
            }

            // Criar tabelas principais
            executeScript(conn, "/script/penas-alternativas.sql");
            
            // Criar tabela de dados faciais
            executeScript(conn, "/script/dados-faciais.sql");
            
            // Inserir administrador inicial (se não existir)
            executeScript(conn, "/script/inserir-admin-inicial.sql");

            System.out.println("Banco de dados inicializado com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados:");
            e.printStackTrace();
        }
    }

    /**
     * Executa um script SQL a partir de um arquivo de recursos.
     */
    private static void executeScript(Connection conn, String scriptPath) throws SQLException {
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                System.err.println("Script não encontrado: " + scriptPath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder script = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    // Ignorar linhas de comentário e linhas vazias
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    
                    script.append(line).append("\n");
                    
                    // Executar quando encontrar ponto e vírgula (fim de comando SQL)
                    if (line.endsWith(";")) {
                        String sql = script.toString().trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException e) {
                                // Ignorar erros de "table already exists" e similares
                                if (!e.getMessage().contains("already exists") && 
                                    !e.getMessage().contains("duplicate column name")) {
                                    System.err.println("Erro ao executar SQL: " + sql.substring(0, Math.min(50, sql.length())));
                                    System.err.println("Mensagem: " + e.getMessage());
                                }
                            }
                        }
                        script.setLength(0);
                    }
                }
                
                // Executar qualquer SQL restante
                String remainingSql = script.toString().trim();
                if (!remainingSql.isEmpty()) {
                    try {
                        stmt.execute(remainingSql);
                    } catch (SQLException e) {
                        if (!e.getMessage().contains("already exists")) {
                            System.err.println("Erro ao executar SQL final: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler script: " + scriptPath);
            e.printStackTrace();
            throw new SQLException("Erro ao executar script: " + scriptPath, e);
        }
    }
}

