package server.dao;

import common.User;
import common.PasswordUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * Data Access Object для работы с таблицей users.
 *
 * @author Kovalenko Vlad, 504673
 */
public class UserDao {
    private static final Logger logger = LogManager.getLogger(UserDao.class);

    /**
     * Регистрация нового пользователя.
     */
    public User register(String username, String password) throws SQLException {
        logger.debug("Попытка регистрации пользователя: {}", username);

        if (userExists(username)) {
            logger.warn("Пользователь с именем {} уже существует", username);
            return null;
        }

        String passwordHash = PasswordUtil.hashMD2(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getInt("id"), username);
                logger.info("Пользователь {} зарегистрирован с id={}", username, user.getId());
                return user;
            }
            logger.error("Ошибка регистрации пользователя {}", username);
            return null;
        }
    }

    /**
     * Авторизация пользователя.
     */
    public User login(String username, String password) throws SQLException {
        logger.debug("Попытка входа пользователя: {}", username);

        String passwordHash = PasswordUtil.hashMD2(password);
        String sql = "SELECT id, username FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("username"));
                logger.info("Пользователь {} успешно вошёл в систему", username);
                return user;
            }
            logger.warn("Неудачная попытка входа для пользователя {}", username);
            return null;
        }
    }

    /**
     * Проверка существования пользователя.
     */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            logger.trace("Пользователь {} существует: {}", username, exists);
            return exists;
        }
    }

    /**
     * Получение пользователя по id.
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT id, username FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"));
            }
            logger.warn("Пользователь с id={} не найден", id);
            return null;
        }
    }
}