package server;

import common.CommandResponse;
import server.connection.ConnectionAcceptor;
import server.manager.CollectionManager;
import server.manager.CommandHandler;
import server.processing.CommandProcessor;
import server.request.RequestReader;
import server.response.ResponseSender;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс сервера (неблокирующий).
 * Координирует работу всех модулей через Selector.
 *
 * @author Kovalenko Vlad, 504673
 */
public class Server {
    private final int port;
    private final String filePath;
    private ConnectionAcceptor connectionAcceptor;
    private RequestReader requestReader;
    private CommandProcessor commandProcessor;
    private ResponseSender responseSender;
    private CollectionManager collectionManager;
    private boolean running;
    private static final Logger logger = LogManager.getLogger(Server.class);


    public Server(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
        this.running = true;

        logger.info("Создание экземпляра сервера, порт: {}, файл: {}", port, filePath);
    }

    /**
     * Инициализирует все модули сервера.
     */
    private void init() throws IOException {
        logger.info("Инициализация сервера...");

        collectionManager = new CollectionManager();
        collectionManager.loadFromFile(filePath);
        logger.info("Загружено {} элементов из файла", collectionManager.size());

        connectionAcceptor = new ConnectionAcceptor(port);
        connectionAcceptor.start();
        logger.debug("ConnectionAcceptor запущен");

        requestReader = new RequestReader();
        logger.debug("RequestReader создан");

        CommandHandler commandHandler = new CommandHandler(collectionManager);
        commandProcessor = new CommandProcessor(commandHandler);
        logger.debug("CommandProcessor создан");

        responseSender = new ResponseSender();
        logger.debug("ResponseSender создан");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook активирован, сохранение коллекции...");
            collectionManager.saveToFile(filePath);
            logger.info("Коллекция сохранена, сервер завершает работу");
        }));

        logger.info("Инициализация сервера завершена успешно");
    }

    /**
     * Запускает сервер в неблокирующем режиме.
     */
    public void start() {
        logger.info("Запуск сервера...");
        try {
            init();
            java.nio.channels.Selector selector = connectionAcceptor.getSelector();
            logger.info("Сервер запущен на порту {}, ожидание запросов...", port);

            while (running && connectionAcceptor.isRunning()) {
                selector.select(100);
                var keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    var key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        if (requestReader.read(connectionAcceptor.getChannel())) {
                            logger.debug("Получен запрос, запуск обработки");
                            var response = commandProcessor.process(requestReader.getCommand());
                            responseSender.send(connectionAcceptor.getChannel(), response, requestReader.getClientAddress());
                            logger.debug("Обработка запроса завершена");
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Критическая ошибка сервера: {}", e.getMessage(), e);
        } finally {
            stop();
        }
    }

    public void stop() {
        logger.info("Остановка сервера...");
        running = false;
        if (connectionAcceptor != null) {
            connectionAcceptor.stop();
            try {
                connectionAcceptor.close();
            } catch (IOException e) {
                logger.error("Ошибка при закрытии ресурсов: {}", e.getMessage());
            }
        }
        logger.info("Сервер остановлен");
    }
}