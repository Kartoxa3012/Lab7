package server.request;

import common.Command;
import common.util.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Модуль чтения запроса (неблокирующий).
 * Читает данные из канала и десериализует команду.
 *
 * @author Kovalenko Vlad, 504673
 */
public class RequestReader {
    private final ByteBuffer buffer = ByteBuffer.allocate(65507);
    private SocketAddress clientAddress;
    private Command command;

    /**
     * Читает запрос из канала.
     *
     * @param channel канал для чтения
     * @return true, если данные успешно прочитаны, false если данных нет
     * @throws IOException если ошибка ввода-вывода
     */
    public boolean read(DatagramChannel channel) throws IOException {
        buffer.clear();
        clientAddress = channel.receive(buffer);
        if (clientAddress == null) {
            return false;
        }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        try {
            command = (Command) Serializer.deserialize(data);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка десериализации команды: " + e.getMessage());
            return false;
        }
    }

    public SocketAddress getClientAddress() {
        return clientAddress;
    }

    public Command getCommand() {
        return command;
    }
}