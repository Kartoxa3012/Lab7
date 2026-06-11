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
    }

    public void start() {
        Thread dispatcherThread = new Thread(() -> {
            logger.info("Dispatcher поток запущен");
            while (running) {
                try {
                    ResponseQueue.ResponseTask task = responseQueue.take();
                    sendingPool.submit(() -> send(task.getResponse(), task.getClientAddress()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        dispatcherThread.start();
    }

    private void send(CommandResponse response, SocketAddress clientAddress) {
        try {
            byte[] data = Serializer.serialize(response);
            logger.info("отправка: адрес={}, размер={} байт", clientAddress, data.length);
            buffer.clear();
            buffer.put(data);
            buffer.flip();
            int sent = channel.send(buffer, clientAddress);
            logger.info("отправлено: {} байт", sent);
        } catch (IOException e) {
            logger.error("Ошибка отправки ответа {}: {}", clientAddress, e.getMessage(), e);
        }
    }

    public void shutdown() {
        running = false;
        sendingPool.shutdown();
    }
}