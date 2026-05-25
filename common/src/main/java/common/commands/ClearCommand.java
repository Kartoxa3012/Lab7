package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code clear} – запрос на очистку коллекции.
 * Данная команда не содержит параметров, так как операция очистки
 * не требует дополнительных данных от клиента. Сервер, получив эту команду,
 * удаляет все элементы из коллекции.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class ClearCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду очистки коллекции.
     * Конструктор не принимает параметров, так как команда не требует данных.
     */
    public ClearCommand() {
        // пустой конструктор для десериализации
    }
}