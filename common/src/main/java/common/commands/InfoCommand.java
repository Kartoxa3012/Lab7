package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code info} – запрос на получение информации о коллекции.
 * Данная команда не содержит параметров. Сервер, получив эту команду,
 * должен вернуть информацию о коллекции (тип, дата инициализации, количество элементов).
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class InfoCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду запроса информации о коллекции.
     */
    public InfoCommand() {
        // пустой конструктор для десериализации
    }
}