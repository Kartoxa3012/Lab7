package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code filter_contains_name} – запрос на фильтрацию элементов коллекции по подстроке в имени.
 * Содержит подстроку, которую сервер должен искать в именах элементов коллекции (без учёта регистра).
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class FilterContainsNameCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String substring;

    /**
     * Создаёт команду фильтрации по подстроке в имени.
     *
     * @param substring подстрока для поиска (регистр не важен)
     */
    public FilterContainsNameCommand(String substring) {
        this.substring = substring;
    }

    /**
     * Возвращает подстроку для поиска.
     *
     * @return подстрока
     */
    public String getSubstring() {
        return substring;
    }
}