package database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    private static final String PROP_FILE_NAME = "db.properties";

    static {
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {

            if (inputStream == null) {
                throw new RuntimeException("Arquivo de propriedades '" + PROP_FILE_NAME + "' não encontrado. Certifique-se de que ele está na pasta 'src/main/resources'.");
            }

            // Carrega as propriedades do arquivo.
            properties.load(inputStream);

        } catch (IOException e) {
            // Este erro ocorre se houver um problema ao ler o arquivo, mesmo que ele tenha sido encontrado.
            throw new RuntimeException("Erro ao carregar o arquivo de propriedades.", e);
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUser() {
        return properties.getProperty("db.user");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }
}