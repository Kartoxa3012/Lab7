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

import static java.lang.System.getenv;
import static java.nio.file.Path.of;

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
            logger.info("PostgreSQL JDBC Driver загружен");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL JDBC Driver не найден: {}", e.getMessage());
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }

        // Читаем учётные данные из .pgpass
        String[] credentials = loadCredentialsFromPgPass(DB_HOST, DB_PORT, DB_NAME);

        if (credentials[0].isEmpty() || credentials[1].isEmpty()) {
            logger.error("Не удалось загрузить учётные данные из .pgpass для {}:{}/{}", DB_HOST, DB_PORT, DB_NAME);
            throw new RuntimeException("Не найдены учётные данные в .pgpass");
        }

        USER = credentials[0];
        PASSWORD = credentials[1];
        URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl=false";

        logger.info("Учётные данные загружены из .pgpass для пользователя {}", USER);
    }

    public static Connection getConnection() throws SQLException {
        logger.debug("Подключение к БД: {}", URL);
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Подключение к БД установлено");
            return conn;
        } catch (SQLException e) {
            logger.error("Ошибка подключения к БД: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Загружает учётные данные из .pgpass файла.
     *
     * @param host     хост базы данных
     * @param port     порт
     * @param database имя базы данных
     * @return массив [username, password] или ["", ""] если не найдено
     */
    private static String[] loadCredentialsFromPgPass(String host, int port, String database) {
        String pgPassPath = firstNonBlank(
                getenv("PGPASSFILE"),
                Path.of(System.getProperty("user.home"), ".pgpass").toString()
        );

        try (BufferedReader reader = new BufferedReader(new FileReader(pgPassPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split(":", 5);
                if (parts.length != 5) {
                    continue;
                }

                if (!fieldMatches(parts[0], host)
                        || !fieldMatches(parts[1], String.valueOf(port))
                        || !fieldMatches(parts[2], database)) {
                    continue;
                }

                logger.debug("Найдены учётные данные для {}:{}/{}", host, port, database);
                return new String[]{parts[3], parts[4]};
            }
        } catch (IOException exception) {
            logger.error("Ошибка чтения .pgpass файла: {}", exception.getMessage());
        }

        return new String[]{"", ""};
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static boolean fieldMatches(String pattern, String value) {
        return pattern.equals("*") || pattern.equals(value);
    }
}