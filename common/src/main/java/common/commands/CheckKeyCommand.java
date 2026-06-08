package common.commands;

import common.AuthenticatedCommand;


/**
 * Команда для проверки существования ключа на сервере.
 * Отправляется перед вводом данных для команды insert.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CheckKeyCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final String key;

    /**
     * Конструктор команды проверки ключа.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @param key      ключ для проверки
     */
    public CheckKeyCommand(String username, String password, String key) {
        super(username, password);
        this.key = key.trim();
    }

    /**
     * Возвращает ключ для проверки.
     *
     * @return ключ
     */
    public String getKey() {
        return key;
    }
}