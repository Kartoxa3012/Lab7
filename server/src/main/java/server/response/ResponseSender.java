package server.response;

import common.CommandResponse;
import common.util.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Модуль отправки ответов клиенту (неблокирующий).
 * Сериализует ответ и отправляет его по указанному адресу.
 *
 * @author Kovalenko Vlad, 504673
 */
public class ResponseSender {
    private final ByteBuffer buffer = ByteBuffer.allocate(65507);

    /**
     * Отправляет ответ клиенту.
     *
     * @param channel       канал для отправки
     * @param response      ответ сервера
     * @param clientAddress адрес клиента
     * @throws IOException если ошибка ввода-вывода
     */
    public void send(DatagramChannel channel, CommandResponse response, SocketAddress clientAddress) throws IOException {
        byte[] data = Serializer.serialize(response);
        buffer.clear();
        buffer.put(data);
        buffer.flip();
        channel.send(buffer, clientAddress);
    }
}