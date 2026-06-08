package server.processing;

import common.Command;
import common.CommandResponse;
import server.manager.CommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Модуль обработки запросов.
 * Использует CachedThreadPool для обработки команд в отдельных потоках.
 * Каждая команда обрабатывается в новом потоке (new Thread).
 *
 * @author Kovalenko Vlad, 504673
 */
public class RequestProcessor {
    private static final Logger logger = LogManager.getLogger(RequestProcessor.class);

    private final CommandHandler commandHandler;
    private final server.processing.ResponseQueue responseQueue;
    private final ExecutorService processingPool;

    public RequestProcessor(CommandHandler commandHandler, server.processing.ResponseQueue responseQueue) {
        this.commandHandler = commandHandler;
        this.responseQueue = responseQueue;
        this.processingPool = Executors.newCachedThreadPool();
        logger.info("RequestProcessor инициализирован с CachedThreadPool");
    }

    /**
     * Обрабатывает запрос в отдельном потоке.
     *
     * @param command        команда
     * @param clientAddress  адрес клиента
     */
    public void processRequest(Command command, SocketAddress clientAddress) {
        processingPool.submit(() -> {
            logger.debug("Начало обработки команды {} от {}",
                    command.getClass().getSimpleName(), clientAddress);
            CommandResponse response = commandHandler.handle(command);
            logger.info("Команда {} обработана, результат: {}",
                    command.getClass().getSimpleName(),
                    response.isSuccess() ? "успех" : "ошибка");

            responseQueue.add(response, clientAddress);
            logger.debug("Ответ добавлен в очередь для {}", clientAddress);
        });
    }

    /**
     * Останавливает обработчик.
     */
    public void shutdown() {
        logger.info("Завершение RequestProcessor...");
        processingPool.shutdown();
        logger.info("RequestProcessor завершён");
    }
}