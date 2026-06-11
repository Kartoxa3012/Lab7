package server.response;

import common.CommandResponse;
import common.util.Serializer;
import server.processing.ResponseQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;

public class ResponseSender {
    private static final Logger logger = LogManager.getLogger(ResponseSender.class);

    private final DatagramChannel channel;
    private final ResponseQueue responseQueue;
    private final ExecutorService sendingPool;
    private final ByteBuffer buffer = ByteBuffer.allocate(65507);
    private volatile boolean running = true;

    public ResponseSender(DatagramChannel channel, ResponseQueue responseQueue, ExecutorService sendingPool) {
        this.channel = channel;
        this.responseQueue = responseQueue;
        this.sendingPool = sendingPool;
        logger.info("ResponseSender инициализирован с FixedThreadPool");
    }

    public void start() {
        Thread dispatcherThread = new Thread(() -> {
            logger.info("Dispatcher поток запущен");
            while (running) {
                try {
                    ResponseQueue.ResponseTask task = responseQueue.take();
                    // 3 пункт
                    sendingPool.submit(() -> send(task.getResponse(), task.getClientAddress()));
                } catch (InterruptedException e) {
                    logger.info("Dispatcher поток прерван");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            logger.info("Dispatcher поток завершён");
        });
        dispatcherThread.setDaemon(false);
        dispatcherThread.start();
    }

    private void send(CommandResponse response, SocketAddress clientAddress) {
        try {
            byte[] data = Serializer.serialize(response);
            buffer.clear();
            buffer.put(data);
            buffer.flip();
            channel.send(buffer, clientAddress);
            logger.debug("Ответ отправлен {}: {} байт", clientAddress, data.length);
        } catch (IOException e) {
            logger.error("Ошибка отправки ответа {}: {}", clientAddress, e.getMessage());
        }
    }

    public void shutdown() {
        logger.info("Завершение ResponseSender...");
        running = false;
    }
}