package server.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

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

    public void start() throws IOException {
        logger.info("Запуск модуля приёма подключений на порту {}", port);

        channel = DatagramChannel.open();
        channel.configureBlocking(false);

        try {
            channel.socket().bind(new InetSocketAddress(port));
            logger.debug("Успешная привязка к порту {}", port);
        } catch (BindException e) {
            logger.error("Порт {} уже занят", port);
            throw new BindException("Порт " + port + " уже занят");
        }

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        logger.info("ConnectionAcceptor запущен на порту {}", port);
    }

    public DatagramChannel getChannel() { return channel; }
    public Selector getSelector() { return selector; }
    public boolean isRunning() { return running; }

    public void stop() {
        logger.info("Остановка ConnectionAcceptor");
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }

    public void close() throws IOException {
        if (selector != null) selector.close();
        if (channel != null) channel.close();
        logger.info("ConnectionAcceptor закрыт");
    }
}