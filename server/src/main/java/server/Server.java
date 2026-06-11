package server;

import common.Command;
import common.CommandResponse;
import server.connection.ConnectionAcceptor;
import server.manager.CollectionManager;
import server.manager.CommandHandler;
import server.processing.ResponseQueue;
import server.request.RequestReader;
import server.response.ResponseSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    private final int port;
    private ConnectionAcceptor connectionAcceptor;
    private RequestReader requestReader;
    private ResponseQueue responseQueue;
    private ResponseSender responseSender;
    private CollectionManager collectionManager;
    private CommandHandler commandHandler;
    private boolean running;

    // Многопоточность
    private final ExecutorService readerPool;
    private final ExecutorService senderPool;

    public Server(int port) {
        this.port = port;
        this.running = true;
        this.readerPool = Executors.newCachedThreadPool();
        this.senderPool = Executors.newFixedThreadPool(4);
        logger.info("Создание сервера: порт={}", port);
    }

    private void init() throws IOException {
        logger.info("Инициализация сервера...");

        collectionManager = new CollectionManager();
        collectionManager.loadFromDatabase();

        connectionAcceptor = new ConnectionAcceptor(port);
        connectionAcceptor.start();

        requestReader = new RequestReader();
        responseQueue = new ResponseQueue();

        commandHandler = new CommandHandler(collectionManager);

        responseSender = new ResponseSender(connectionAcceptor.getChannel(), responseQueue, senderPool);
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
                        //1 пункт
                        readerPool.submit(() -> {
                            try {
                                logger.debug("Чтение запроса в потоке: {}", Thread.currentThread().getName());

                                if (requestReader.read(connectionAcceptor.getChannel())) {
                                    Command command = requestReader.getCommand();
                                    SocketAddress clientAddress = requestReader.getClientAddress();

                                    // 2 пункт
                                    Thread handlerThread = new Thread(() -> {
                                        logger.debug("Обработка команды {} в потоке: {}",
                                                command.getClass().getSimpleName(), Thread.currentThread().getName());

                                        long startTime = System.currentTimeMillis();
                                        CommandResponse response = commandHandler.handle(command);
                                        long elapsedTime = System.currentTimeMillis() - startTime;

                                        logger.info("Команда {} обработана за {} мс",
                                                command.getClass().getSimpleName(), elapsedTime);


                                        responseQueue.add(response, clientAddress);
                                    });
                                    handlerThread.start();
                                }
                            } catch (IOException e) {
                                logger.error("Ошибка чтения запроса: {}", e.getMessage());
                            }
                        });
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

        readerPool.shutdown();
        senderPool.shutdown();

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