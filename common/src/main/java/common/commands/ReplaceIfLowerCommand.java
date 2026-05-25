package common.commands;
import common.Command;
import common.model.SpaceMarine;

import java.io.Serializable;

/**
 * Команда {@code replace_if_lowe} – запрос на замену элемента по ключу,
 * если новое значение меньше старого согласно естественному порядку
 * {@link SpaceMarine#compareTo(SpaceMarine)}.
 * Содержит ключ элемента и новый объект {@link SpaceMarine} (без id и даты создания,
 * так как эти поля копируются со старого элемента на сервере).
 * @author Kovalenko Vlad, 504673
 * @see Command
 * @see SpaceMarine
 */
public class ReplaceIfLowerCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final SpaceMarine newMarine;

    /**
     * Создаёт команду замены элемента при условии, что новое значение меньше старого.
     *
     * @param key       ключ элемента для замены
     * @param newMarine новый объект {@link SpaceMarine} (id и creationDate будут скопированы со старого)
     */
    public ReplaceIfLowerCommand(String key, SpaceMarine newMarine) {
        this.key = key;
        this.newMarine = newMarine;
    }

    /**
     * Возвращает ключ элемента для замены.
     *
     * @return ключ
     */
    public String getKey() {
        return key;
    }

    /**
     * Возвращает новый объект {@link SpaceMarine} для замены.
     *
     * @return новый SpaceMarine (без id и даты)
     */
    public SpaceMarine getNewMarine() {
        return newMarine;
    }
}