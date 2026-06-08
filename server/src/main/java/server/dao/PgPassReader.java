package server.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.System.getenv;
import static java.nio.file.Path.of;

/**
 * Утилита для чтения учетных данных из файла .pgpass.
 *
 * @author Kovalenko Vlad, 504673
 */
public class PgPassReader {

    private static final String PGPASS = ".pgpass";

    /**
     * Загружает учетные данные для подключения к PostgreSQL из .pgpass файла.
     *
     * @param host     хост базы данных (например, localhost)
     * @param port     порт базы данных (обычно 5432)
     * @param database имя базы данных
     * @return массив [username, password] или пустой массив, если не найдено
     */
    public static Optional<String[]> loadCredentials(String host, int port, String database) {
        String pgPassPath = getPgPassPath();

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

                return Optional.of(new String[]{parts[3], parts[4]});
            }
        } catch (IOException exception) {
            System.err.println("Ошибка чтения .pgpass файла: " + exception.getMessage());
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Возвращает путь к .pgpass файлу.
     * Сначала проверяет переменную окружения PGPASSFILE,
     * затем использует стандартный путь ~/.pgpass.
     *
     * @return путь к .pgpass файлу
     */
    private static String getPgPassPath() {
        String pgPassEnv = getenv("PGPASSFILE");
        if (pgPassEnv != null && !pgPassEnv.isEmpty()) {
            return pgPassEnv;
        }
        return of(System.getProperty("user.home"), PGPASS).toString();
    }

    /**
     * Проверяет соответствие поля шаблону.
     * Поддерживает символ '*' (любое значение).
     *
     * @param pattern шаблон из .pgpass
     * @param value   фактическое значение
     * @return true если соответствует
     */
    private static boolean fieldMatches(String pattern, String value) {
        return pattern.equals("*") || pattern.equals(value);
    }
}