package common.commands;
import common.AuthenticatedCommand;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда добавления нового элемента в коллекцию.
 * Требует авторизации. Содержит ключ и объект SpaceMarine.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 * @see SpaceMarine
 */
public class InsertCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final SpaceMarine marine;

    /**
     * Конструктор команды вставки.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @param key      ключ, по которому элемент будет храниться в коллекции
     * @param marine   объект SpaceMarine (без id, генерируется на сервере)
     */
    public InsertCommand(String username, String password, String key, SpaceMarine marine) {
        super(username, password);
        this.key = key;
        this.marine = marine;
    }

    /**
     * Возвращает ключ для хранения элемента.
     *
     * @return ключ элемента
     */
    public String getKey() { return key; }

    /**
     * Возвращает объект SpaceMarine для добавления.
     *
     * @return объект SpaceMarine
     */
    public SpaceMarine getMarine() { return marine; }
}