package common.commands;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда {@code insert} – запрос на добавление нового элемента в коллекцию.
 * Содержит ключ, по которому элемент будет храниться в коллекции,
 * и сам объект {@link SpaceMarine} (без id, так как id генерируется на сервере).
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 * @see SpaceMarine
 */
public class InsertCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final SpaceMarine marine;

    /**
     * Создаёт команду добавления элемента.
     *
     * @param key    ключ, под которым элемент будет храниться в коллекции (не может быть null или пустым)
     * @param marine объект {@link SpaceMarine} без id (id будет сгенерирован на сервере)
     */
    public InsertCommand(String key, SpaceMarine marine) {
        this.key = key;
        this.marine = marine;
    }

    /**
     * Возвращает ключ для добавления элемента.
     *
     * @return ключ
     */
    public String getKey() {
        return key;
    }

    /**
     * Возвращает объект {@link SpaceMarine} для добавления.
     *
     * @return объект SpaceMarine (без id)
     */
    public SpaceMarine getMarine() {
        return marine;
    }
}