package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда получения информации о коллекции.
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class InfoCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор команды информации.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     */
    public InfoCommand(String username, String password) {
        super(username, password);
    }
}