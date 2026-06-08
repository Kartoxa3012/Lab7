package server.processing;

import common.Command;
import common.CommandResponse;
import server.manager.CommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Модуль обработки полученных команд.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandProcessor {
    private static final Logger logger = LogManager.getLogger(CommandProcessor.class);
    private final CommandHandler commandHandler;

    public CommandProcessor(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public CommandResponse process(Command command) {
        String commandName = command.getClass().getSimpleName();
        logger.debug("Начало обработки команды: {}", commandName);

        CommandResponse response = commandHandler.handle(command);
        logger.info("Команда {} обработана , результат: {}",
                commandName, response.isSuccess() ? "успех" : "ошибка");

        return response;
    }
}