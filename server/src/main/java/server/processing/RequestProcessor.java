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
        // Cached thread pool для обработки команд
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
        // Создаём новый поток для обработки команды (требование: новый Thread)
        Thread handlerThread = new Thread(() -> {
            logger.debug("Начало обработки команды {} от {} в потоке {}",
                    command.getClass().getSimpleName(), clientAddress, Thread.currentThread().getName());

            long startTime = System.currentTimeMillis();
            CommandResponse response = commandHandler.handle(command);
            long elapsedTime = System.currentTimeMillis() - startTime;

            logger.info("Команда {} обработана за {} мс, результат: {}",
                    command.getClass().getSimpleName(), elapsedTime,
                    response.isSuccess() ? "успех" : "ошибка");

            // Добавляем ответ в очередь
            responseQueue.add(response, clientAddress);
            logger.debug("Ответ добавлен в очередь для {}", clientAddress);
        });

        handlerThread.setDaemon(false);
        handlerThread.start();
        logger.debug("Создан новый поток {} для обработки команды", handlerThread.getName());

        // Альтернативно можно использовать пул (раскомментировать для использования пула вместо new Thread)
        // processingPool.submit(() -> {
        //     CommandResponse response = commandHandler.handle(command);
        //     responseQueue.add(response, clientAddress);
        // });
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