package common.commands;
import common.AuthenticatedCommand;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда замены элемента по ключу, если новое значение больше старого.
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 * @see SpaceMarine
 */
public class ReplaceIfGreaterCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final SpaceMarine newMarine;

    /**
     * Конструктор команды замены (если новое больше старого).
     *
     * @param username  логин пользователя
     * @param password  пароль пользователя
     * @param key       ключ элемента для замены
     * @param newMarine новый объект SpaceMarine
     */
    public ReplaceIfGreaterCommand(String username, String password, String key, SpaceMarine newMarine) {
        super(username, password);
        this.key = key;
        this.newMarine = newMarine;
    }

    /**
     * Возвращает ключ элемента для замены.
     *
     * @return ключ элемента
     */
    public String getKey() { return key; }

    /**
     * Возвращает новый объект SpaceMarine.
     *
     * @return новый SpaceMarine
     */
    public SpaceMarine getNewMarine() { return newMarine; }
}