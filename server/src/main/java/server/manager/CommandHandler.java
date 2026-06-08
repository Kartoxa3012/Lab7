package server.manager;

import common.*;
import common.commands.*;
import common.commands.LoginCommand;
import common.commands.RegisterCommand;
import common.model.*;
import server.dao.SpaceMarineDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Обработчик команд на сервере.
 * Проверяет авторизацию, выполняет команды через DAO и синхронизирует кэш.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandHandler {
    private static final Logger logger = LogManager.getLogger(CommandHandler.class);

    private final CollectionManager collectionManager;
    private final SpaceMarineDao spaceMarineDao = new SpaceMarineDao();
    private final AuthService authService = new AuthService();

    // Текущий авторизованный пользователь для каждого потока
    private final ThreadLocal<User> currentUser = new ThreadLocal<>();

    // Блокировки для синхронизации операций с ключами
    private final Object insertLock = new Object();
    private final Map<String, Object> keyLocks = new ConcurrentHashMap<>();

    public CommandHandler(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        logger.info("CommandHandler инициализирован");
    }

    /**
     * Основной метод обработки команды.
     *
     * @param command команда от клиента
     * @return ответ сервера
     */
    public CommandResponse handle(Command command) {
        logger.debug("Получена команда: {}", command.getClass().getSimpleName());

        // Обработка команд, не требующих авторизации
        if (command instanceof RegisterCommand) {
            return handleRegister((RegisterCommand) command);
        }
        if (command instanceof LoginCommand) {
            return handleLogin((LoginCommand) command);
        }

        // Для всех остальных команд требуется авторизация
        if (!(command instanceof AuthenticatedCommand)) {
            logger.warn("Попытка выполнения команды без авторизации: {}", command.getClass().getSimpleName());
            return new CommandResponse(false, "Требуется авторизация. Используйте login");
        }

        AuthenticatedCommand authCmd = (AuthenticatedCommand) command;

        // Проверяем или сохраняем пользователя
        User user = authService.login(authCmd.getUsername(), authCmd.getPasswordHash());
        if (user == null) {
            logger.warn("Неудачная авторизация для команды {}: пользователь {}",
                    command.getClass().getSimpleName(), authCmd.getUsername());
            return new CommandResponse(false, "Неверный логин или пароль");
        }
        currentUser.set(user);
        logger.debug("Пользователь {} авторизован для команды {}", user.getUsername(), command.getClass().getSimpleName());

        // Маршрутизация команд
        if (command instanceof InsertCommand) {
            return handleInsert((InsertCommand) command);
        } else if (command instanceof UpdateCommand) {
            return handleUpdate((UpdateCommand) command);
        } else if (command instanceof RemoveCommand) {
            return handleRemove((RemoveCommand) command);
        } else if (command instanceof RemoveGreaterCommand) {
            return handleRemoveGreater((RemoveGreaterCommand) command);
        } else if (command instanceof ReplaceIfGreaterCommand) {
            return handleReplaceIfGreater((ReplaceIfGreaterCommand) command);
        } else if (command instanceof ReplaceIfLowerCommand) {
            return handleReplaceIfLower((ReplaceIfLowerCommand) command);
        } else if (command instanceof ClearCommand) {
            return handleClear();
        } else if (command instanceof ShowCommand) {
            return handleShow();
        } else if (command instanceof InfoCommand) {
            return handleInfo();
        } else if (command instanceof FilterByCategoryCommand) {
            return handleFilterByCategory((FilterByCategoryCommand) command);
        } else if (command instanceof FilterContainsNameCommand) {
            return handleFilterContainsName((FilterContainsNameCommand) command);
        } else if (command instanceof PrintFieldDescendingHealthCommand) {
            return handlePrintHealth();
        } else if (command instanceof LogoutCommand) {
            return handleLogout();
        } else {
            logger.warn("Неизвестная команда: {}", command.getClass().getSimpleName());
            return new CommandResponse(false, "Неизвестная команда");
        }
    }

    // ==================== КОМАНДЫ БЕЗ АВТОРИЗАЦИИ ====================

    private CommandResponse handleRegister(RegisterCommand cmd) {
        logger.debug("Регистрация пользователя: {}", cmd.getUsername());

        User user = authService.register(cmd.getUsername(), cmd.getPasswordHash());
        if (user == null) {
            logger.warn("Регистрация не удалась: пользователь {} уже существует", cmd.getUsername());
            return new CommandResponse(false, "Пользователь с таким именем уже существует");
        }

        logger.info("Пользователь {} успешно зарегистрирован с id={}", user.getUsername(), user.getId());
        return new CommandResponse(true, "Регистрация успешна. Теперь вы можете войти (login)");
    }

    private CommandResponse handleLogin(LoginCommand cmd) {
        logger.debug("Вход пользователя: {}", cmd.getUsername());

        User user = authService.login(cmd.getUsername(), cmd.getPasswordHash());
        if (user == null) {
            logger.warn("Неудачная попытка входа для {}", cmd.getUsername());
            return new CommandResponse(false, "Неверный логин или пароль");
        }

        logger.info("Пользователь {} успешно вошёл в систему", user.getUsername());
        return new CommandResponse(true, "Добро пожаловать, " + user.getUsername() + "!");
    }

    private CommandResponse handleLogout() {
        User user = currentUser.get();
        if (user != null) {
            logger.info("Пользователь {} вышел из системы", user.getUsername());
            currentUser.remove();
        }
        return new CommandResponse(true, "Вы вышли из системы");
    }

    // ==================== КОМАНДЫ С АВТОРИЗАЦИЕЙ ====================

    /**
     * Получение блокировки для конкретного ключа.
     */
    private Object getKeyLock(String key) {
        return keyLocks.computeIfAbsent(key, k -> new Object());
    }

    /**
     * Обработка команды вставки элемента с синхронизацией.
     */
    private CommandResponse handleInsert(InsertCommand cmd) {
        User user = currentUser.get();
        String key = cmd.getKey();

        logger.debug("handleInsert: key='{}', user='{}'", key, user.getUsername());

        // Блокировка по конкретному ключу для предотвращения дубликатов
        synchronized (getKeyLock(key)) {
            // Двойная проверка: сначала в кэше
            if (collectionManager.containsKey(key)) {
                logger.warn("Попытка вставки с уже существующим ключом: key='{}', user='{}'", key, user.getUsername());
                return new CommandResponse(false, "Элемент с ключом '" + key + "' уже существует");
            }

            SpaceMarine marine = cmd.getMarine();
            marine.setKey(key);

            try {
                boolean success = spaceMarineDao.insert(marine, user.getId());
                if (success) {
                    collectionManager.addToCache(marine);
                    logger.info("Элемент вставлен: key='{}', id={}, user='{}'", key, marine.getId(), user.getUsername());
                    return new CommandResponse(true, "Элемент добавлен. Ключ: " + key + ", ID: " + marine.getId());
                } else {
                    logger.error("Ошибка вставки элемента: key='{}'", key);
                    return new CommandResponse(false, "Ошибка при вставке элемента");
                }
            } catch (SQLException e) {
                // Проверка на нарушение уникальности в БД (PostgreSQL error code 23505)
                if (e.getSQLState().equals("23505")) {
                    logger.warn("Нарушение уникальности ключа в БД: key='{}'", key);
                    return new CommandResponse(false, "Элемент с ключом '" + key + "' уже существует");
                }
                logger.error("Ошибка БД при вставке key='{}': {}", key, e.getMessage());
                return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
            }
        }
    }

    /**
     * Обработка команды обновления элемента с синхронизацией.
     */
    private CommandResponse handleUpdate(UpdateCommand cmd) {
        User user = currentUser.get();
        String key = cmd.getKey();

        logger.debug("handleUpdate: key='{}', user='{}'", key, user.getUsername());

        synchronized (getKeyLock(key)) {
            if (!collectionManager.containsKey(key)) {
                logger.warn("Попытка обновления несуществующего ключа: key='{}', user='{}'", key, user.getUsername());
                return new CommandResponse(false, "Элемент с ключом '" + key + "' не найден");
            }

            SpaceMarine marine = cmd.getMarine();
            marine.setKey(key);

            try {
                boolean success = spaceMarineDao.update(marine, user.getId());
                if (success) {
                    collectionManager.addToCache(marine);
                    logger.info("Элемент обновлён: key='{}', user='{}'", key, user.getUsername());
                    return new CommandResponse(true, "Элемент с ключом '" + key + "' обновлён");
                } else {
                    logger.warn("Элемент не обновлён: key='{}', возможно нет прав", key);
                    return new CommandResponse(false, "Элемент не найден или нет прав для обновления");
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при обновлении key='{}': {}", key, e.getMessage());
                return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
            }
        }
    }

    /**
     * Обработка команды удаления элемента с синхронизацией.
     */
    private CommandResponse handleRemove(RemoveCommand cmd) {
        User user = currentUser.get();
        String key = cmd.getKey();

        logger.debug("handleRemove: key='{}', user='{}'", key, user.getUsername());

        synchronized (getKeyLock(key)) {
            if (!collectionManager.containsKey(key)) {
                logger.warn("Попытка удаления несуществующего ключа: key='{}', user='{}'", key, user.getUsername());
                return new CommandResponse(false, "Элемент с ключом '" + key + "' не найден");
            }

            try {
                boolean success = spaceMarineDao.delete(key, user.getId());
                if (success) {
                    collectionManager.removeFromCache(key);
                    // Удаляем блокировку для ключа
                    keyLocks.remove(key);
                    logger.info("Элемент удалён: key='{}', user='{}'", key, user.getUsername());
                    return new CommandResponse(true, "Элемент с ключом '" + key + "' удалён");
                } else {
                    logger.warn("Элемент не удалён: key='{}', возможно нет прав", key);
                    return new CommandResponse(false, "Элемент не найден или нет прав для удаления");
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при удалении key='{}': {}", key, e.getMessage());
                return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
            }
        }
    }

    /**
     * Обработка команды удаления элементов, превышающих заданный.
     */
    private CommandResponse handleRemoveGreater(RemoveGreaterCommand cmd) {
        User user = currentUser.get();
        SpaceMarine reference = cmd.getReference();

        logger.debug("handleRemoveGreater: user='{}', reference health={}", user.getUsername(), reference.getHealth());

        List<SpaceMarine> allElements = collectionManager.getSortedValues();
        List<String> keysToRemove = allElements.stream()
                .filter(m -> m.compareTo(reference) > 0)
                .map(SpaceMarine::getKey)
                .collect(Collectors.toList());

        logger.debug("Найдено {} элементов для удаления", keysToRemove.size());

        int removed = 0;
        for (String key : keysToRemove) {
            synchronized (getKeyLock(key)) {
                try {
                    boolean success = spaceMarineDao.delete(key, user.getId());
                    if (success) {
                        collectionManager.removeFromCache(key);
                        keyLocks.remove(key);
                        removed++;
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка БД при удалении key='{}': {}", key, e.getMessage());
                }
            }
        }

        logger.info("Удалено {} элементов пользователем {}", removed, user.getUsername());
        return new CommandResponse(true, "Удалено элементов: " + removed);
    }

    /**
     * Обработка команды замены (если новое значение больше старого).
     */
    private CommandResponse handleReplaceIfGreater(ReplaceIfGreaterCommand cmd) {
        User user = currentUser.get();
        String key = cmd.getKey();

        logger.debug("handleReplaceIfGreater: key='{}', user='{}'", key, user.getUsername());

        synchronized (getKeyLock(key)) {
            if (!collectionManager.containsKey(key)) {
                logger.warn("Элемент с ключом '{}' не найден для replace_if_greater", key);
                return new CommandResponse(false, "Элемент с ключом '" + key + "' не найден");
            }

            SpaceMarine oldMarine = collectionManager.getFromCache(key);
            SpaceMarine newMarine = cmd.getNewMarine();
            newMarine.setKey(key);
            newMarine.setId(oldMarine.getId());
            newMarine.setCreationDate(oldMarine.getCreationDate());

            if (newMarine.compareTo(oldMarine) > 0) {
                try {
                    boolean success = spaceMarineDao.update(newMarine, user.getId());
                    if (success) {
                        collectionManager.addToCache(newMarine);
                        logger.info("Замена выполнена: key='{}', user='{}' (новое больше старого)", key, user.getUsername());
                        return new CommandResponse(true, "Значение заменено (новое больше старого)");
                    } else {
                        return new CommandResponse(false, "Не удалось обновить элемент");
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка БД при замене key='{}': {}", key, e.getMessage());
                    return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
                }
            } else {
                logger.debug("Замена не выполнена: новое значение не больше старого, key='{}'", key);
                return new CommandResponse(true, "Замена не выполнена: новое значение не больше старого");
            }
        }
    }

    /**
     * Обработка команды замены (если новое значение меньше старого).
     */
    private CommandResponse handleReplaceIfLower(ReplaceIfLowerCommand cmd) {
        User user = currentUser.get();
        String key = cmd.getKey();

        logger.debug("handleReplaceIfLower: key='{}', user='{}'", key, user.getUsername());

        synchronized (getKeyLock(key)) {
            if (!collectionManager.containsKey(key)) {
                logger.warn("Элемент с ключом '{}' не найден для replace_if_lower", key);
                return new CommandResponse(false, "Элемент с ключом '" + key + "' не найден");
            }

            SpaceMarine oldMarine = collectionManager.getFromCache(key);
            SpaceMarine newMarine = cmd.getNewMarine();
            newMarine.setKey(key);
            newMarine.setId(oldMarine.getId());
            newMarine.setCreationDate(oldMarine.getCreationDate());

            if (newMarine.compareTo(oldMarine) < 0) {
                try {
                    boolean success = spaceMarineDao.update(newMarine, user.getId());
                    if (success) {
                        collectionManager.addToCache(newMarine);
                        logger.info("Замена выполнена: key='{}', user='{}' (новое меньше старого)", key, user.getUsername());
                        return new CommandResponse(true, "Значение заменено (новое меньше старого)");
                    } else {
                        return new CommandResponse(false, "Не удалось обновить элемент");
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка БД при замене key='{}': {}", key, e.getMessage());
                    return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
                }
            } else {
                logger.debug("Замена не выполнена: новое значение не меньше старого, key='{}'", key);
                return new CommandResponse(true, "Замена не выполнена: новое значение не меньше старого");
            }
        }
    }

    /**
     * Обработка команды очистки (удаляет только элементы текущего пользователя).
     */
    private CommandResponse handleClear() {
        User user = currentUser.get();

        logger.debug("handleClear: user='{}'", user.getUsername());

        try {
            int deletedCount = spaceMarineDao.deleteAllByOwner(user.getId());
            collectionManager.loadFromDatabase(); // Перезагружаем кэш
            // Очищаем все блокировки
            keyLocks.clear();
            logger.info("Очищено {} элементов пользователя {}", deletedCount, user.getUsername());
            return new CommandResponse(true, "Удалено ваших элементов: " + deletedCount);
        } catch (SQLException e) {
            logger.error("Ошибка БД при очистке коллекции: {}", e.getMessage());
            return new CommandResponse(false, "Ошибка БД: " + e.getMessage());
        }
    }

    /**
     * Обработка команды показа всех элементов коллекции.
     */
    private CommandResponse handleShow() {
        logger.debug("handleShow: запрос от пользователя");

        List<SpaceMarine> sorted = collectionManager.getSortedValues();
        if (sorted.isEmpty()) {
            logger.debug("Коллекция пуста");
            return new CommandResponse(true, "Коллекция пуста");
        }

        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : sorted) {
            sb.append("Ключ: ").append(marine.getKey())
                    .append(", ID: ").append(marine.getId())
                    .append(", Имя: ").append(marine.getName())
                    .append(", Здоровье: ").append(marine.getHealth())
                    .append("\n");
        }

        logger.debug("Выведено {} элементов", sorted.size());
        return new CommandResponse(true, sb.toString());
    }

    /**
     * Обработка команды информации о коллекции.
     */
    private CommandResponse handleInfo() {
        logger.debug("handleInfo: запрос информации о коллекции");

        String info = "Тип коллекции: " + collectionManager.getCollection().getClass().getName() + "\n" +
                "Дата инициализации: " + collectionManager.getInitDate() + "\n" +
                "Количество элементов: " + collectionManager.size();

        logger.debug("Информация выдана: {} элементов", collectionManager.size());
        return new CommandResponse(true, info);
    }

    /**
     * Обработка команды фильтрации по категории.
     */
    private CommandResponse handleFilterByCategory(FilterByCategoryCommand cmd) {
        logger.debug("handleFilterByCategory: категория={}", cmd.getCategory());

        List<SpaceMarine> filtered = collectionManager.getSortedValues().stream()
                .filter(m -> cmd.getCategory().equals(m.getCategory()))
                .collect(Collectors.toList());

        logger.debug("Найдено {} элементов с категорией {}", filtered.size(), cmd.getCategory());

        if (filtered.isEmpty()) {
            return new CommandResponse(true, "Элементы с категорией " + cmd.getCategory() + " не найдены");
        }

        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : filtered) {
            sb.append("Ключ: ").append(marine.getKey())
                    .append(", ID: ").append(marine.getId())
                    .append(", Имя: ").append(marine.getName())
                    .append(", Категория: ").append(marine.getCategory())
                    .append("\n");
        }
        return new CommandResponse(true, sb.toString());
    }

    /**
     * Обработка команды фильтрации по подстроке в имени.
     */
    private CommandResponse handleFilterContainsName(FilterContainsNameCommand cmd) {
        String substring = cmd.getSubstring().toLowerCase();
        logger.debug("handleFilterContainsName: подстрока='{}'", substring);

        List<SpaceMarine> filtered = collectionManager.getSortedValues().stream()
                .filter(m -> m.getName().toLowerCase().contains(substring))
                .collect(Collectors.toList());

        logger.debug("Найдено {} элементов, содержащих '{}'", filtered.size(), substring);

        if (filtered.isEmpty()) {
            return new CommandResponse(true, "Элементы, содержащие '" + substring + "' в имени, не найдены");
        }

        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : filtered) {
            sb.append("Ключ: ").append(marine.getKey())
                    .append(", ID: ").append(marine.getId())
                    .append(", Имя: ").append(marine.getName())
                    .append("\n");
        }
        return new CommandResponse(true, sb.toString());
    }

    /**
     * Обработка команды вывода здоровья в порядке убывания.
     */
    private CommandResponse handlePrintHealth() {
        logger.debug("handlePrintHealth: запрос значений здоровья");

        if (collectionManager.size() == 0) {
            logger.debug("Коллекция пуста");
            return new CommandResponse(true, "Коллекция пуста");
        }

        List<Float> healthValues = collectionManager.getSortedValues().stream()
                .map(SpaceMarine::getHealth)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        logger.debug("Выведено {} значений здоровья", healthValues.size());

        StringBuilder sb = new StringBuilder();
        for (Float health : healthValues) {
            sb.append(String.format("%.2f", health)).append("\n");
        }
        return new CommandResponse(true, sb.toString());
    }
}