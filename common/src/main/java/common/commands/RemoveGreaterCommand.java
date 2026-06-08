package common.commands;
import common.AuthenticatedCommand;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда удаления всех элементов, превышающих заданный.
 * Требует авторизации. Содержит эталонный объект SpaceMarine.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 * @see SpaceMarine
 */
public class RemoveGreaterCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final SpaceMarine reference;

    /**
     * Конструктор команды удаления больших элементов.
     *
     * @param username  логин пользователя
     * @param password  пароль пользователя
     * @param reference эталонный объект для сравнения
     */
    public RemoveGreaterCommand(String username, String password, SpaceMarine reference) {
        super(username, password);
        this.reference = reference;
    }

    /**
     * Возвращает эталонный объект для сравнения.
     *
     * @return эталонный SpaceMarine
     */
    public SpaceMarine getReference() { return reference; }
}