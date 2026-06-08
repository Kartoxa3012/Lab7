package server;

import server.connection.ConnectionAcceptor;
import server.manager.CollectionManager;
import server.manager.CommandHandler;
import server.processing.RequestProcessor;
import server.processing.ResponseQueue;
import server.request.RequestReader;
import server.response.ResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Главный класс сервера.
 * Реализует многопоточную обработку запросов:
 * - Cached thread pool для приёма
 * - Создание нового потока для обработки каждой команды
 * - Fixed thread pool для отправки ответов
 *
 * @author Kovalenko Vlad, 504673
 */
public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    private final int port;
    private ConnectionAcceptor connectionAcceptor;
    private RequestReader requestReader;
    private RequestProcessor requestProcessor;
    private ResponseSender responseSender;
    private ResponseQueue responseQueue;
    private CollectionManager collectionManager;
    private boolean running;

    public Server(int port) {
        this.port = port;
        this.running = true;
        logger.info("Создание сервера: порт={}", port);
    }

    private void init() throws IOException {
        logger.info("Инициализация сервера...");

        // Загрузка коллекции из БД
        collectionManager = new CollectionManager();
        collectionManager.loadFromDatabase();

        // Модуль приёма подключений
        connectionAcceptor = new ConnectionAcceptor(port);
        connectionAcceptor.start();

        // Модуль чтения запроса
        requestReader = new RequestReader();

        // Очередь ответов
        responseQueue = new ResponseQueue();

        // Модуль обработки команд (создаёт новые потоки)
        CommandHandler commandHandler = new CommandHandler(collectionManager);
        requestProcessor = new RequestProcessor(commandHandler, responseQueue);

        // Модуль отправки ответов (FixedThreadPool)
        responseSender = new ResponseSender(connectionAcceptor.getChannel(), responseQueue, 4);
        responseSender.start();

        logger.info("Инициализация сервера завершена");
    }

    public void start() {
        logger.info("Запуск сервера...");
        try {
            init();
            logger.info("Сервер запущен на порту {}, ожидание запросов...", port);

            Selector selector = connectionAcceptor.getSelector();

            while (running && connectionAcceptor.isRunning()) {
                selector.select(100);
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        if (requestReader.read(connectionAcceptor.getChannel())) {
                            // Передаём запрос в обработчик (асинхронно)
                            requestProcessor.processRequest(requestReader.getCommand(), requestReader.getClientAddress());
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

        if (requestProcessor != null) {
            requestProcessor.shutdown();
        }
        if (responseSender != null) {
            responseSender.shutdown();
        }
        if (connectionAcceptor != null) {
            connectionAcceptor.stop();
            try {
                connectionAcceptor.close();
            } catch (IOException e) {
                logger.error("Ошибка закрытия ресурсов: {}", e.getMessage());
            }
        }
        logger.info("Сервер остановлен");
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется 8080");
            }
        }

        Server server = new Server(port);
        server.start();
    }
}