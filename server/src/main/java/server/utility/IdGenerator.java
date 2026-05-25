package server.utility;

import server.manager.CollectionManager;
import common.model.SpaceMarine;

/**
 * Утилитарный класс для генерации уникальных идентификаторов объектов {@link SpaceMarine}.
 * <p>
 * Генерация основана на поиске максимального значения {@code id} среди всех элементов коллекции
 * и последующем увеличении его на 1. Если коллекция пуста, возвращается 1.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 * @see CollectionManager
 * @see SpaceMarine
 */
public class IdGenerator {

    /**
     * Генерирует новый уникальный идентификатор для элемента коллекции.
     *
     * @param manager менеджер коллекции, содержащий существующие элементы
     * @return новый уникальный идентификатор (целое положительное число)
     */
    public static Integer generateId(CollectionManager manager) {
        if (manager.size() == 0) return 1;
        return manager.getCollection().values().stream()
                .mapToInt(SpaceMarine::getId)
                .max()
                .getAsInt() + 1;
    }
}