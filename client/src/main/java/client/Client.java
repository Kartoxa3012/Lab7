package client;

import client.util.InputManager;
import common.*;
import common.commands.*;
import common.commands.LoginCommand;
import common.commands.RegisterCommand;
import common.model.SpaceMarine;
import common.model.AstartesCategory;
import common.util.Serializer;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * UDP-клиент для взаимодействия с сервером.
 * Поддерживает авторизацию и отправку команд с логином/паролем.
 *
 * @author Kovalenko Vlad, 504673
 */
public class Client {
    private static final Set<String> activeScripts = new HashSet<>();

    private final String host;
    private final int port;
    private final int timeout;
    private final int maxRetries;
    private DatagramSocket socket;
    private final InputManager inputManager;
    private boolean running;
    private String currentUsername;
    private String currentPassword;
    private boolean authenticated;

    public Client(String host, int port, int timeout, int maxRetries) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.inputManager = new InputManager(new Scanner(System.in));
        this.running = true;
        this.authenticated = false;
    }

    public void start() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            System.out.println("Клиент запущен. Подключение к " + host + ":" + port);
            System.out.println("Сначала выполните register или login");
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

    private void processCommand(String line) {
        String[] parts = line.split("\\s+", 3);
        String commandName = parts[0].toLowerCase();
        String arg1 = parts.length > 1 ? parts[1] : null;
        String arg2 = parts.length > 2 ? parts[2] : null;

        try {
            switch (commandName) {
                case "help":
                    showHelp();
                    break;
                case "exit":
                    exit();
                    break;
                case "execute_script":
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите имя файла.");
                        return;
                    }
                    executeScript(arg1);
                    break;
                case "register":
                    if (arg1 == null || arg2 == null) {
                        System.out.println("Использование: register <логин> <пароль>");
                        return;
                    }
                    sendRegisterCommand(arg1, arg2);
                    break;
                case "login":
                    if (arg1 == null || arg2 == null) {
                        System.out.println("Использование: login <логин> <пароль>");
                        return;
                    }
                    sendLoginCommand(arg1, arg2);
                    break;
                case "logout":
                    sendLogoutCommand();
                    break;
                case "info":
                case "show":
                case "clear":
                case "print_field_descending_health":
                    if (!checkAuth()) return;
                    sendSimpleCommand(commandName);
                    break;
                case "insert":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: insert myKey");
                        return;
                    }
                    sendInsertCommand(arg1);
                    break;
                case "update":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: update myKey");
                        return;
                    }
                    sendUpdateCommand(arg1);
                    break;
                case "remove_key":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: remove_key myKey");
                        return;
                    }
                    sendRemoveCommand(arg1);
                    break;
                case "remove_greater":
                    if (!checkAuth()) return;
                    sendRemoveGreaterCommand();
                    break;
                case "replace_if_greater":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: replace_if_greater myKey");
                        return;
                    }
                    sendReplaceIfGreaterCommand(arg1);
                    break;
                case "replace_if_lowe":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите ключ. Пример: replace_if_lowe myKey");
                        return;
                    }
                    sendReplaceIfLowerCommand(arg1);
                    break;
                case "filter_by_category":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите категорию.");
                        return;
                    }
                    sendFilterByCategoryCommand(arg1);
                    break;
                case "filter_contains_name":
                    if (!checkAuth()) return;
                    if (arg1 == null) {
                        System.out.println("Ошибка: укажите подстроку.");
                        return;
                    }
                    sendFilterContainsNameCommand(arg1);
                    break;
                default:
                    System.out.println("Неизвестная команда. Введите help.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private boolean checkAuth() {
        if (!authenticated) {
            System.out.println("Необходимо авторизоваться. Используйте login <логин> <пароль>");
            return false;
        }
        return true;
    }

    private void showHelp() {
        System.out.println("Доступные команды:");
        System.out.println("  help - вывести справку");
        System.out.println("  register <логин> <пароль> - регистрация");
        System.out.println("  login <логин> <пароль> - авторизация");
        System.out.println("  logout - завершить сессию");
        System.out.println("  info - информация о коллекции");
        System.out.println("  show - все элементы коллекции");
        System.out.println("  insert <key> - добавить элемент");
        System.out.println("  update <key> - обновить элемент");
        System.out.println("  remove_key <key> - удалить элемент");
        System.out.println("  clear - очистить коллекцию (только свои элементы)");
        System.out.println("  remove_greater - удалить элементы, превышающие заданный");
        System.out.println("  replace_if_greater <key> - заменить, если новое больше");
        System.out.println("  replace_if_lowe <key> - заменить, если новое меньше");
        System.out.println("  filter_by_category <category> - фильтр по категории");
        System.out.println("  filter_contains_name <name> - фильтр по имени");
        System.out.println("  print_field_descending_health - здоровье по убыванию");
        System.out.println("  execute_script <file> - выполнить скрипт");
        System.out.println("  exit - завершить клиент");
    }

    private void exit() {
        System.out.println("Завершение клиента.");
        running = false;
    }

    private void executeScript(String fileName) {
        if (!authenticated) {
            System.out.println("Сначала выполните login");
            return;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Файл не найден: " + fileName);
            return;
        }
        if (!file.canRead()) {
            System.out.println("Нет прав на чтение файла: " + fileName);
            return;
        }

        String absolutePath;
        try {
            absolutePath = file.getCanonicalPath();
        } catch (IOException e) {
            absolutePath = file.getAbsolutePath();
        }

        if (activeScripts.contains(absolutePath)) {
            System.out.println("Обнаружена рекурсия! Скрипт " + fileName + " уже выполняется.");
            return;
        }

        activeScripts.add(absolutePath);

        try (Scanner fileScanner = new Scanner(file)) {
            inputManager.startScriptMode(fileScanner);
            int lineNum = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                lineNum++;
                if (line.isEmpty() || line.startsWith("#")) continue;
                System.out.println("[скрипт " + fileName + ":" + lineNum + "] " + line);
                processCommand(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка открытия файла: " + e.getMessage());
        } finally {
            inputManager.endScriptMode();
            activeScripts.remove(absolutePath);
        }
    }

    private void sendRegisterCommand(String username, String password) {
        RegisterCommand cmd = new RegisterCommand(username, password);
        sendAndReceive(cmd);
    }

    private void sendLoginCommand(String username, String password) {
        LoginCommand cmd = new LoginCommand(username, password);
        CommandResponse response = sendAndReceive(cmd);
        if (response != null && response.isSuccess()) {
            currentUsername = username;
            currentPassword = password;
            authenticated = true;
            System.out.println("Авторизация успешна. Добро пожаловать, " + username + "!");
        }
    }

    private void sendLogoutCommand() {
        if (!authenticated) {
            System.out.println("Вы не авторизованы");
            return;
        }
        LogoutCommand cmd = new LogoutCommand(currentUsername, currentPassword);
        sendAndReceive(cmd);
        authenticated = false;
        currentUsername = null;
        currentPassword = null;
        System.out.println("Вы вышли из системы");
    }

    private void sendSimpleCommand(String commandName) {
        Command cmd;
        switch (commandName) {
            case "info":
                cmd = new InfoCommand(currentUsername, currentPassword);
                break;
            case "show":
                cmd = new ShowCommand(currentUsername, currentPassword);
                break;
            case "clear":
                cmd = new ClearCommand(currentUsername, currentPassword);
                break;
            case "print_field_descending_health":
                cmd = new PrintFieldDescendingHealthCommand(currentUsername, currentPassword);
                break;
            default:
                return;
        }
        sendAndReceive(cmd);
    }

    private void sendInsertCommand(String key) {
        CheckKeyCommand checkCmd = new CheckKeyCommand(currentUsername, currentPassword, key);
        CommandResponse checkResponse = sendAndReceive(checkCmd);
        if (checkResponse == null || !checkResponse.isSuccess()) {
            return;
        }
        System.out.println("Введите данные нового элемента:");
        SpaceMarine marine = inputManager.readNewMarine();
        InsertCommand insertCmd = new InsertCommand(currentUsername, currentPassword, key, marine);
        sendAndReceive(insertCmd);
    }

    private void sendUpdateCommand(String key) {
        System.out.println("Введите новые данные элемента:");
        SpaceMarine marine = inputManager.readMarineForUpdate();
        UpdateCommand cmd = new UpdateCommand(currentUsername, currentPassword, key, marine);
        sendAndReceive(cmd);
    }

    private void sendRemoveCommand(String key) {
        RemoveCommand cmd = new RemoveCommand(currentUsername, currentPassword, key);
        sendAndReceive(cmd);
    }

    private void sendRemoveGreaterCommand() {
        System.out.println("Введите эталонный элемент:");
        SpaceMarine reference = inputManager.readNewMarine();
        RemoveGreaterCommand cmd = new RemoveGreaterCommand(currentUsername, currentPassword, reference);
        sendAndReceive(cmd);
    }

    private void sendReplaceIfGreaterCommand(String key) {
        System.out.println("Введите новый элемент:");
        SpaceMarine newMarine = inputManager.readMarineForUpdate();
        ReplaceIfGreaterCommand cmd = new ReplaceIfGreaterCommand(currentUsername, currentPassword, key, newMarine);
        sendAndReceive(cmd);
    }

    private void sendReplaceIfLowerCommand(String key) {
        System.out.println("Введите новый элемент:");
        SpaceMarine newMarine = inputManager.readMarineForUpdate();
        ReplaceIfLowerCommand cmd = new ReplaceIfLowerCommand(currentUsername, currentPassword, key, newMarine);
        sendAndReceive(cmd);
    }

    private void sendFilterByCategoryCommand(String categoryArg) {
        try {
            AstartesCategory category = AstartesCategory.valueOf(categoryArg.toUpperCase());
            FilterByCategoryCommand cmd = new FilterByCategoryCommand(currentUsername, currentPassword, category);
            sendAndReceive(cmd);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: неверная категория. Доступные: DREADNOUGHT, AGGRESSOR, APOTHECARY");
        }
    }

    private void sendFilterContainsNameCommand(String substring) {
        FilterContainsNameCommand cmd = new FilterContainsNameCommand(currentUsername, currentPassword, substring);
        sendAndReceive(cmd);
    }

    private CommandResponse sendAndReceive(Command command) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                byte[] sendData = Serializer.serialize(command);
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        InetAddress.getByName(host), port
                );
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[65507];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                byte[] actualData = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuffer, 0, actualData, 0, receivePacket.getLength());
                CommandResponse response = (CommandResponse) Serializer.deserialize(actualData);

                if (response.getMessage() != null && !response.getMessage().isEmpty()) {
                    System.out.println(response.getMessage());
                }
                return response;

            } catch (SocketTimeoutException e) {
                System.out.println("Попытка " + attempt + "/" + maxRetries + ": Сервер не отвечает.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка при обмене данными: " + e.getMessage());
                return null;
            }
        }
        System.out.println("Не удалось получить ответ от сервера после " + maxRetries + " попыток.");
        return null;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8091;
        int timeout = 10000;
        int maxRetries = 5;

        if (args.length >= 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт. Используется порт по умолчанию: " + port);
            }
        }

        System.out.println("Подключение к " + host + ":" + port);
        Client client = new Client(host, port, timeout, maxRetries);
        client.start();
    }
}