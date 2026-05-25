package common.commands;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда {@code update} – запрос на обновление элемента коллекции по его идентификатору.
 * Содержит идентификатор {@code id} элемента для обновления и новый объект
 * {@link SpaceMarine} (без id и даты создания, так как эти поля копируются со старого элемента на сервере).
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 * @see SpaceMarine
 */
public class UpdateCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final SpaceMarine marine;

    /**
     * Создаёт команду обновления элемента.
     *
     * @param id     идентификатор элемента для обновления
     * @param marine новый объект {@link SpaceMarine} (id и creationDate будут скопированы со старого)
     */
    public UpdateCommand(int id, SpaceMarine marine) {
        this.id = id;
        this.marine = marine;
    }

    /**
     * Возвращает идентификатор элемента для обновления.
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Возвращает новый объект {@link SpaceMarine} для обновления.
     *
     * @return новый SpaceMarine (без id и даты)
     */
    public SpaceMarine getMarine() {
        return marine;
    }
}