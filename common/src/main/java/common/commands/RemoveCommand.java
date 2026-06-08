package common.commands;
import common.AuthenticatedCommand;
import common.Command;


import java.io.Serializable;

/**
 * Команда удаления элемента из коллекции по ключу.
 * Требует авторизации. Содержит ключ удаляемого элемента.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class RemoveCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String key;

    /**
     * Конструктор команды удаления.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @param key      ключ удаляемого элемента
     */
    public RemoveCommand(String username, String password, String key) {
        super(username, password);
        this.key = key;
    }

    /**
     * Возвращает ключ удаляемого элемента.
     *
     * @return ключ элемента
     */
    public String getKey() { return key; }
}