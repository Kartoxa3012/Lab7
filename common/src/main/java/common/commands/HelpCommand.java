package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code help} – запрос на получение списка доступных команд.
 * Сервер, получив эту команду, должен вернуть список имён и описаний команд.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class HelpCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду запроса справки.
     */
    public HelpCommand() {
        // пустой конструктор
    }
}