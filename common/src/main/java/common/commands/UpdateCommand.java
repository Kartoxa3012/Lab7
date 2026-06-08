package common.commands;
import common.AuthenticatedCommand;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда обновления существующего элемента коллекции.
 * Требует авторизации. Содержит ключ и новый объект SpaceMarine.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 * @see SpaceMarine
 */
public class UpdateCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final SpaceMarine marine;

    /**
     * Конструктор команды обновления.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @param key      ключ обновляемого элемента
     * @param marine   новый объект SpaceMarine (id и дата будут скопированы со старого)
     */
    public UpdateCommand(String username, String password, String key, SpaceMarine marine) {
        super(username, password);
        this.key = key;
        this.marine = marine;
    }

    /**
     * Возвращает ключ обновляемого элемента.
     *
     * @return ключ элемента
     */
    public String getKey() { return key; }

    /**
     * Возвращает новый объект SpaceMarine.
     *
     * @return объект SpaceMarine
     */
    public SpaceMarine getMarine() { return marine; }
}