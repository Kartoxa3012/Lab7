package server.connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Модуль приёма подключений (неблокирующий).
 * Отвечает за создание и настройку канала, привязку к порту.
 *
 * @author Kovalenko Vlad, 504673
 */
public class ConnectionAcceptor {
    private static final Logger logger = LogManager.getLogger(ConnectionAcceptor.class);
    private final int port;
    private DatagramChannel channel;
    private Selector selector;
    private boolean running;

    public ConnectionAcceptor(int port) {
        this.port = port;
        this.running = true;
    }

    /**
     * Запускает модуль: открывает канал, привязывается к порту, регистрирует в селекторе.
     *
     * @throws IOException если ошибка ввода-вывода
     */
    public void start() throws IOException {
        logger.info("Запуск модуля приёма подключений на порту {}", port);

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        logger.debug("Канал открыт, неблокирующий режим включён");
        logger.info("Сервер успешно запущен на порту {}", port);
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public Selector getSelector() {
        return selector;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        logger.info("Остановка модуля приёма подключений");
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }

    public void close() throws IOException {
        logger.debug("Закрытие ресурсов ConnectionAcceptor");
        if (selector != null) {
            selector.close();
        }
        if (channel != null) {
            channel.close();
        }
        logger.info("Ресурсы ConnectionAcceptor закрыты");
    }
}