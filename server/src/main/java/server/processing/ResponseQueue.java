package server.processing;

import common.CommandResponse;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;

/**
 * Очередь ответов для многопоточной отправки.
 *
 * @author Kovalenko Vlad, 504673
 */
public class ResponseQueue {
    private static final Logger logger = LogManager.getLogger(ResponseQueue.class);
    private final BlockingQueue<ResponseTask> queue = new LinkedBlockingQueue<>();

    /**
     * Добавляет ответ в очередь.
     *
     * @param response       ответ сервера
     * @param clientAddress  адрес клиента
     */
    public void add(CommandResponse response, SocketAddress clientAddress) {
        logger.debug("Добавлен ответ в очередь для {}", clientAddress);
        queue.offer(new ResponseTask(response, clientAddress));
    }

    /**
     * Извлекает ответ из очереди (блокируется, если очередь пуста).
     *
     * @return задача на отправку
     * @throws InterruptedException если поток прерван
     */
    public ResponseTask take() throws InterruptedException {
        logger.trace("Ожидание ответа из очереди...");
        ResponseTask task = queue.take();
        logger.debug("Взят ответ из очереди для {}", task.getClientAddress());
        return task;
    }

    /**
     * Класс, представляющий задачу на отправку.
     */
    public static class ResponseTask {
        private final CommandResponse response;
        private final SocketAddress clientAddress;

        public ResponseTask(CommandResponse response, SocketAddress clientAddress) {
            this.response = response;
            this.clientAddress = clientAddress;
        }

        public CommandResponse getResponse() { return response; }
        public SocketAddress getClientAddress() { return clientAddress; }
    }
}