package common.commands;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда {@code remove_greater} – запрос на удаление из коллекции всех элементов,
 * превышающих заданный эталонный элемент.
 * Содержит эталонный объект {@link SpaceMarine} (без id, так как id генерируется на сервере).
 * Сервер должен удалить все элементы коллекции, которые в естественном порядке
 * сравнения оказываются больше переданного.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 * @see SpaceMarine
 */
public class RemoveGreaterCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final SpaceMarine reference;

    /**
     * Создаёт команду удаления элементов, превышающих эталонный.
     *
     * @param reference эталонный объект {@link SpaceMarine} (id не используется, только поля для сравнения)
     */
    public RemoveGreaterCommand(SpaceMarine reference) {
        this.reference = reference;
    }

    /**
     * Возвращает эталонный объект для сравнения.
     *
     * @return эталонный SpaceMarine
     */
    public SpaceMarine getReference() {
        return reference;
    }
}