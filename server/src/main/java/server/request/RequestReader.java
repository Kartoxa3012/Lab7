package server.request;

import common.Command;
import common.util.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RequestReader {
    private static final Logger logger = LogManager.getLogger(RequestReader.class);

    private final ByteBuffer buffer = ByteBuffer.allocate(65507);
    private SocketAddress clientAddress;
    private Command command;

    public boolean read(DatagramChannel channel) throws IOException {
        buffer.clear();
        SocketAddress receivedAddress = channel.receive(buffer);

        if (receivedAddress == null) {
            return false;
        }

        buffer.flip();

        if (buffer.remaining() == 0) {
            logger.warn("Пустой пакет от {}", receivedAddress);
            return false;
        }

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        try {
            command = (Command) Serializer.deserialize(data);
            clientAddress = receivedAddress;  // ← СОХРАНЯЕМ АДРЕС
            logger.info("Получена команда: {} от {}", command.getClass().getSimpleName(), clientAddress);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка десериализации: {}", e.getMessage());
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