package common.commands;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code execute_script} – запрос на выполнение скрипта.
 * Содержит имя файла скрипта. Сервер, получив эту команду,
 * должен выполнить команды из указанного файла.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class ExecuteScriptCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String fileName;

    /**
     * Создаёт команду выполнения скрипта.
     *
     * @param fileName имя файла скрипта
     */
    public ExecuteScriptCommand(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Возвращает имя файла скрипта.
     *
     * @return имя файла
     */
    public String getFileName() {
        return fileName;
    }
}