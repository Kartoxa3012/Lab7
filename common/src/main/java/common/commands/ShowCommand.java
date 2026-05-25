package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code show} – запрос на получение всех элементов коллекции.
 * Данная команда не содержит параметров. Сервер, получив эту команду,
 * должен вернуть все элементы коллекции в отсортированном порядке.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class ShowCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду запроса всех элементов коллекции.
     */
    public ShowCommand() {
        // пустой конструктор для десериализации
    }
}