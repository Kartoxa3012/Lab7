package server.manager;

import common.User;
import server.dao.UserDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * Сервис для авторизации и регистрации пользователей.
 *
 * @author Kovalenko Vlad, 504673
 */
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserDao userDao = new UserDao();

    public User register(String username, String password) {
        logger.debug("Регистрация пользователя: {}", username);
        try {
            User user = userDao.register(username, password);
            if (user == null) {
                logger.warn("Регистрация не удалась: пользователь {} уже существует", username);
            } else {
                logger.info("Пользователь {} успешно зарегистрирован", username);
            }
            return user;
        } catch (SQLException e) {
            logger.error("Ошибка БД при регистрации {}: {}", username, e.getMessage());
            return null;
        }
    }

    public User login(String username, String password) {
        logger.debug("Авторизация пользователя: {}", username);
        try {
            User user = userDao.login(username, password);
            if (user == null) {
                logger.warn("Неудачная авторизация: {}", username);
            } else {
                logger.info("Пользователь {} авторизован", username);
            }
            return user;
        } catch (SQLException e) {
            logger.error("Ошибка БД при авторизации {}: {}", username, e.getMessage());
            return null;
        }
    }
}