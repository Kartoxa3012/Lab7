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

    public Server(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
        this.running = true;
    }

    /**
     * Инициализирует все модули сервера.
     */
    private void init() throws IOException {
        // Загрузка коллекции из файла
        collectionManager = new CollectionManager();
        collectionManager.loadFromFile(filePath);

        // Модуль приёма подключений
        connectionAcceptor = new ConnectionAcceptor(port);
        connectionAcceptor.start();

        // Остальные модули
        requestReader = new RequestReader();
        CommandHandler commandHandler = new CommandHandler(collectionManager);
        commandProcessor = new CommandProcessor(commandHandler);
        responseSender = new ResponseSender();
    }

    /**
     * Запускает сервер в неблокирующем режиме.
     */
    public void start() {
        try {
            init();
            System.out.println("Сервер запущен (неблокирующий режим). Ожидание запросов...");

            // Добавляем хук для сохранения при завершении
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Сохранение коллекции перед завершением...");
                collectionManager.saveToFile(filePath);
            }));

            Selector selector = connectionAcceptor.getSelector();

            while (running && connectionAcceptor.isRunning()) {
                selector.select(100); // таймаут 100 мс
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        if (requestReader.read(connectionAcceptor.getChannel())) {
                            // Обрабатываем команду
                            CommandResponse response = commandProcessor.process(requestReader.getCommand());
                            // Отправляем ответ
                            responseSender.send(connectionAcceptor.getChannel(), response, requestReader.getClientAddress());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Останавливает сервер.
     */
    public void stop() {
        running = false;
        if (connectionAcceptor != null) {
            connectionAcceptor.stop();
            try {
                connectionAcceptor.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String csvFile = System.getenv("SPACE_MARINES_DATA");
        if (csvFile == null || csvFile.trim().isEmpty()) {
            System.err.println("Переменная окружения SPACE_MARINES_DATA не установлена");
            System.exit(1);
        }

        int port = 8080;
        Server server = new Server(port, csvFile);
        server.start();
    }
}