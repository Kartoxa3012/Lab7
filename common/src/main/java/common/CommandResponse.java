package common;

import java.io.Serializable;

/**
 * Класс ответа сервера на выполнение команды.
 * Содержит флаг успеха, текстовое сообщение и дополнительные данные.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Object data;

    /**
     * Конструктор ответа с данными.
     *
     * @param success флаг успеха выполнения команды
     * @param message текстовое сообщение
     * @param data    дополнительные данные (например, список элементов)
     */
    public CommandResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Конструктор ответа без дополнительных данных.
     *
     * @param success флаг успеха выполнения команды
     * @param message текстовое сообщение
     */
    public CommandResponse(boolean success, String message) {
        this(success, message, null);
    }

    /**
     * Возвращает флаг успеха выполнения команды.
     *
     * @return true, если команда выполнена успешно
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Возвращает текстовое сообщение ответа.
     *
     * @return сообщение
     */
    public String getMessage() {
        return message;
    }

    /**
     * Возвращает дополнительные данные ответа.
     *
     * @param <T> тип данных
     * @return данные (может быть null)
     */
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }
}