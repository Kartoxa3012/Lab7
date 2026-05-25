package server.processing;

import common.Command;
import common.CommandResponse;
import server.manager.CommandHandler;

/**
 * Модуль обработки полученных команд.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandProcessor {
    private final CommandHandler commandHandler;

    public CommandProcessor(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public CommandResponse process(Command command) {
        return commandHandler.handle(command);
    }
}