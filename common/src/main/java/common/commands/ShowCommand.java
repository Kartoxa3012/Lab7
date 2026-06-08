package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда отображения всех элементов коллекции.
 * Требует авторизации. Выводит все элементы (всех пользователей).
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class ShowCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор команды показа коллекции.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     */
    public ShowCommand(String username, String password) {
        super(username, password);
    }
}