package common.commands;
import common.Command;


import java.io.Serializable;

/**
 * Команда {@code remove_key} – запрос на удаление элемента из коллекции по ключу.
 * Содержит ключ элемента, который должен быть удалён из коллекции на сервере.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class RemoveCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String key;

    /**
     * Создаёт команду удаления элемента по ключу.
     *
     * @param key ключ элемента для удаления
     */
    public RemoveCommand(String key) {
        this.key = key;
    }

    /**
     * Возвращает ключ элемента для удаления.
     *
     * @return ключ
     */
    public String getKey() {
        return key;
    }
}