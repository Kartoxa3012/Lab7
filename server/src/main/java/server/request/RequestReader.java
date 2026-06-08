package server.request;

import common.Command;
import common.util.Serializer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Модуль чтения запроса (неблокирующий).
 * Читает данные из канала и десериализует команду.
 *
 * @author Kovalenko Vlad, 504673
 */
public class RequestReader {
    private static final Logger logger = LogManager.getLogger(RequestReader.class);

    private final ByteBuffer buffer = ByteBuffer.allocate(65507);
    private SocketAddress clientAddress;
    private Command command;

    public boolean read(DatagramChannel channel) throws IOException {
        buffer.clear();
        clientAddress = channel.receive(buffer);

        if (clientAddress == null) {
            return false;
        }

        logger.debug("Получен запрос от {}", clientAddress);

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        try {
            command = (Command) Serializer.deserialize(data);
            logger.info("Получена команда: {} от {}", command.getClass().getSimpleName(), clientAddress);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка десериализации команды: {}", e.getMessage());
            return false;
        }
    }

    public SocketAddress getClientAddress() { return clientAddress; }
    public Command getCommand() { return command; }
}