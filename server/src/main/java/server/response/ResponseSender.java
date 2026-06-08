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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Модуль отправки ответов.
 * Использует FixedThreadPool для отправки ответов клиентам.
 *
 * @author Kovalenko Vlad, 504673
 */
public class ResponseSender {
    private static final Logger logger = LogManager.getLogger(ResponseSender.class);

    private final DatagramChannel channel;
    private final ResponseQueue responseQueue;
    private final ExecutorService sendingPool;
    private final ByteBuffer buffer = ByteBuffer.allocate(65507);
    private volatile boolean running = true;

    public ResponseSender(DatagramChannel channel, ResponseQueue responseQueue, int poolSize) {
        this.channel = channel;
        this.responseQueue = responseQueue;
        this.sendingPool = Executors.newFixedThreadPool(poolSize);
        logger.info("ResponseSender инициализирован с FixedThreadPool размером {}", poolSize);
    }

    /**
     * Запускает цикл отправки ответов.
     */
    public void start() {
        // Отдельный поток для чтения очереди и распределения задач
        Thread dispatcherThread = new Thread(() -> {
            logger.info("Dispatcher поток запущен");
            while (running) {
                try {
                    ResponseQueue.ResponseTask task = responseQueue.take();
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

    /**
     * Отправляет ответ клиенту.
     *
     * @param response      ответ сервера
     * @param clientAddress адрес клиента
     */
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

    /**
     * Останавливает отправщик.
     */
    public void shutdown() {
        logger.info("Завершение ResponseSender...");
        running = false;
        sendingPool.shutdown();
        try {
            if (!sendingPool.awaitTermination(5, TimeUnit.SECONDS)) {
                sendingPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            sendingPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ResponseSender завершён");
    }
}