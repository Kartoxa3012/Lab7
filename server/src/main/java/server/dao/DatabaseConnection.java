package server.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);

    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 5432;
    private static final String DB_NAME = "studs";

    private static final String USER;
    private static final String PASSWORD;
    private static final String URL;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }

        String pgPassPath = Path.of(System.getProperty("user.home"), ".pgpass").toString();
        String[] credentials = readPgPass(pgPassPath);

        USER = credentials[0];
        PASSWORD = credentials[1];
        URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl=false";

        logger.info("Подключение к БД: {}", URL);
    }

    private static String[] readPgPass(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 5);
                if (parts.length == 5 && (parts[0].equals("*") || parts[0].equals(DB_HOST))) {
                    return new String[]{parts[3], parts[4]};
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка чтения .pgpass: {}", e.getMessage());
        }
        throw new RuntimeException("Не найдены учётные данные в .pgpass");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}