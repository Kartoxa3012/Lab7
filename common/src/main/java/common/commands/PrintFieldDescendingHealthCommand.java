package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code print_field_descending_health} – запрос на вывод значений здоровья
 * всех элементов коллекции в порядке убывания.
 * Данная команда не содержит параметров. Сервер, получив эту команду,
 * должен вернуть список значений {@code health} всех элементов,
 * отсортированный по убыванию.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class PrintFieldDescendingHealthCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду запроса значений здоровья в порядке убывания.
     */
    public PrintFieldDescendingHealthCommand() {
        // пустой конструктор для десериализации
    }
}