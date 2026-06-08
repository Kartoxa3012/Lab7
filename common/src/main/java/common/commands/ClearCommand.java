package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда очистки коллекции (удаляет только элементы текущего пользователя).
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class ClearCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор команды очистки.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     */
    public ClearCommand(String username, String password) {
        super(username, password);
    }
}