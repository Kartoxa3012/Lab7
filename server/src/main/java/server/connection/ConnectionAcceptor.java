package server.connection;

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
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Модуль приёма подключений запущен на порту " + port);
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
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }

    public void close() throws IOException {
        if (selector != null) {
            selector.close();
        }
        if (channel != null) {
            channel.close();
        }
    }
}