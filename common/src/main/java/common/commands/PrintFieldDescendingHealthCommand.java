package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда вывода значений здоровья всех элементов в порядке убывания.
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class PrintFieldDescendingHealthCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор команды вывода здоровья.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     */
    public PrintFieldDescendingHealthCommand(String username, String password) {
        super(username, password);
    }
}