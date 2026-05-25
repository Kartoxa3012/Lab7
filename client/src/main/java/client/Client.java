package client;

import client.util.InputManager;
import common.Command;
import common.commands.*;
import common.model.AstartesCategory;
import common.model.SpaceMarine;
import common.CommandResponse;
import common.util.Serializer;


import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * UDP-клиент для взаимодействия с сервером.
 * <p>
 * Читает команды из консоли, сериализует их, отправляет на сервер,
 * получает ответ и выводит результат пользователю.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 */
public class Client {
    private final String host;
    private final int port;
    private final int timeout;
    private final int maxRetries;
    private DatagramSocket socket;
    private final InputManager inputManager;
    private boolean running;

    /**
     * Конструктор клиента.
     *
     * @param host   адрес сервера
     * @param port   порт сервера
     * @param timeout таймаут ожидания ответа в миллисекундах
     * @param maxRetries максимальное количество попыток отправки
     */
    public Client(String host, int port, int timeout, int maxRetries) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.inputManager = new InputManager(new Scanner(System.in));
        this.running = true;
    }

    /**
     * Запускает клиент.
     */
    public void start() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            System.out.println("Клиент запущен. Подключение к " + host + ":" + port);
            System.out.println("Введите help для списка команд.");

            while (running) {
                System.out.print("> ");
                String line = inputManager.readString("", true);
                if (line.isEmpty()) continue;

                processCommand(line);
            }
        } catch (SocketException e) {
            System.err.println("Ошибка создания сокета: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * Обрабатывает введённую команду.
     *
     * @param line строка команды
     */
    private void processCommand(String line) {
        String[] parts = line.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : null;

        try {
            switch (commandName) {
                case "help":
                    showHelp();
                    break;
                case "exit":
                    exit();
                    break;
                case "info":
                case "show":
                case "clear":
                case "print_field_descending_health":
                    Command cmd = createSimpleCommand(commandName);
                    sendAndReceive(cmd);
                    break;
                case "insert":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: insert myKey");
                        return;
                    }
                    sendInsertCommand(argument);
                    break;
                case "update":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите id. Пример: update 5");
                        return;
                    }
                    sendUpdateCommand(argument);
                    break;
                case "remove_key":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: remove_key myKey");
                        return;
                    }
                    sendRemoveCommand(argument);
                    break;
                case "remove_greater":
                    sendRemoveGreaterCommand();
                    break;
                case "replace_if_greater":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: replace_if_greater myKey");
                        return;
                    }
                    sendReplaceIfGreaterCommand(argument);
                    break;
                case "replace_if_lowe":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: replace_if_lowe myKey");
                        return;
                    }
                    sendReplaceIfLowerCommand(argument);
                    break;
                case "filter_by_category":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите категорию. Доступные: DREADNOUGHT, AGGRESSOR, APOTHECARY");
                        return;
                    }
                    sendFilterByCategoryCommand(argument);
                    break;
                case "filter_contains_name":
                    if (argument == null) {
                        System.out.println("Ошибка: укажите подстроку.");
                        return;
                    }
                    sendFilterContainsNameCommand(argument);
                    break;
                default:
                    System.out.println("Неизвестная команда. Введите help.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Выводит список доступных команд.
     */
    private void showHelp() {
        System.out.println("Доступные команды:");
        System.out.println("  help - вывести справку");
        System.out.println("  info - информация о коллекции");
        System.out.println("  show - все элементы коллекции");
        System.out.println("  insert <key> - добавить элемент");
        System.out.println("  update <id> - обновить элемент");
        System.out.println("  remove_key <key> - удалить элемент");
        System.out.println("  clear - очистить коллекцию");
        System.out.println("  remove_greater - удалить элементы, превышающие заданный");
        System.out.println("  replace_if_greater <key> - заменить, если новое больше");
        System.out.println("  replace_if_lowe <key> - заменить, если новое меньше");
        System.out.println("  filter_by_category <category> - фильтр по категории");
        System.out.println("  filter_contains_name <name> - фильтр по имени");
        System.out.println("  print_field_descending_health - здоровье по убыванию");
        System.out.println("  exit - завершить клиент");
    }

    /**
     * Завершает работу клиента.
     */
    private void exit() {
        System.out.println("Завершение клиента.");
        running = false;
    }

    /**
     * Создаёт простую команду без аргументов.
     */
    private Command createSimpleCommand(String commandName) {
        switch (commandName) {
            case "info": return new InfoCommand();
            case "show": return new ShowCommand();
            case "clear": return new ClearCommand();
            case "print_field_descending_health": return new PrintFieldDescendingHealthCommand();
            default: throw new IllegalArgumentException("Неизвестная команда: " + commandName);
        }
    }

    /**
     * Отправляет команду insert.
     */
    private void sendInsertCommand(String key) {
        System.out.println("Введите данные нового элемента:");
        SpaceMarine marine = inputManager.readNewMarine();
        InsertCommand cmd = new InsertCommand(key, marine);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду update.
     */
    private void sendUpdateCommand(String idArg) {
        try {
            int id = Integer.parseInt(idArg);
            System.out.println("Введите новые данные элемента:");
            SpaceMarine marine = inputManager.readMarineForUpdate();
            UpdateCommand cmd = new UpdateCommand(id, marine);
            sendAndReceive(cmd);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: id должен быть целым числом.");
        }
    }

    /**
     * Отправляет команду remove_key.
     */
    private void sendRemoveCommand(String key) {
        RemoveCommand cmd = new RemoveCommand(key);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду remove_greater.
     */
    private void sendRemoveGreaterCommand() {
        System.out.println("Введите эталонный элемент:");
        SpaceMarine reference = inputManager.readNewMarine();
        RemoveGreaterCommand cmd = new RemoveGreaterCommand(reference);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду replace_if_greater.
     */
    private void sendReplaceIfGreaterCommand(String key) {
        System.out.println("Введите новый элемент:");
        SpaceMarine newMarine = inputManager.readMarineForUpdate();
        ReplaceIfGreaterCommand cmd = new ReplaceIfGreaterCommand(key, newMarine);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду replace_if_lowe.
     */
    private void sendReplaceIfLowerCommand(String key) {
        System.out.println("Введите новый элемент:");
        SpaceMarine newMarine = inputManager.readMarineForUpdate();
        ReplaceIfLowerCommand cmd = new ReplaceIfLowerCommand(key, newMarine);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду filter_by_category.
     */
    private void sendFilterByCategoryCommand(String categoryArg) {
        try {
            AstartesCategory category = AstartesCategory.valueOf(categoryArg.toUpperCase());
            FilterByCategoryCommand cmd = new FilterByCategoryCommand(category);
            sendAndReceive(cmd);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: неверная категория. Доступные: DREADNOUGHT, AGGRESSOR, APOTHECARY");
        }
    }

    /**
     * Отправляет команду filter_contains_name.
     */
    private void sendFilterContainsNameCommand(String substring) {
        FilterContainsNameCommand cmd = new FilterContainsNameCommand(substring);
        sendAndReceive(cmd);
    }

    /**
     * Отправляет команду на сервер и получает ответ.
     *
     * @param command команда для отправки
     */
    private void sendAndReceive(Command command) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Сериализуем команду
                byte[] sendData = Serializer.serialize(command);
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        InetAddress.getByName(host), port
                );
                socket.send(sendPacket);

                // Ждём ответ
                byte[] receiveBuffer = new byte[65507];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                // Десериализуем ответ
                byte[] actualData = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuffer, 0, actualData, 0, receivePacket.getLength());
                CommandResponse response = (CommandResponse) Serializer.deserialize(actualData);

                // Выводим результат
                System.out.println(response.getMessage());
                return;

            } catch (SocketTimeoutException e) {
                System.out.println("Попытка " + attempt + "/" + maxRetries + ": Сервер не отвечает.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка при обмене данными: " + e.getMessage());
                return;
            }
        }
        System.out.println("Не удалось получить ответ от сервера после " + maxRetries + " попыток.");
    }

    /**
     * Точка входа в клиентское приложение.
     *
     * @param args аргументы командной строки (host, port)
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        int timeout = 5000;
        int maxRetries = 3;

        if (args.length >= 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт. Используется порт по умолчанию: " + port);
            }
        }

        Client client = new Client(host, port, timeout, maxRetries);
        client.start();
    }
}