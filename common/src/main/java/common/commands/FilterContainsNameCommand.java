package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда фильтрации элементов по подстроке в имени.
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 */
public class FilterContainsNameCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String substring;

    /**
     * Конструктор команды фильтрации по имени.
     *
     * @param username  логин пользователя
     * @param password  пароль пользователя
     * @param substring подстрока для поиска
     */
    public FilterContainsNameCommand(String username, String password, String substring) {
        super(username, password);
        this.substring = substring;
    }

    /**
     * Возвращает подстроку для поиска.
     *
     * @return подстрока
     */
    public String getSubstring() { return substring; }
}