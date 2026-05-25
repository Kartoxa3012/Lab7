package server.manager;

import common.Command;
import common.commands.*;
import common.model.AstartesCategory;
import common.model.SpaceMarine;
import common.CommandResponse;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Обработчик команд на сервере.
 * Получает команду от клиента, выполняет соответствующую операцию над коллекцией
 * и возвращает результат в виде {@link CommandResponse}.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandHandler {
    private final CollectionManager collectionManager;
    private static final Logger logger = LogManager.getLogger(CommandHandler.class);

    public CommandHandler(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        logger.info("CommandHandler инициализирован");
    }

    /**
     * Основной метод обработки команды.
     * Определяет тип команды и вызывает соответствующий обработчик.
     *
     * @param command команда от клиента
     * @return ответ сервера
     */
    public CommandResponse handle(Command command) {
        String commandName = command.getClass().getSimpleName();
        logger.debug("Получена команда для обработки: {}", commandName);


        if (command instanceof ClearCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleClear();
        } else if (command instanceof FilterByCategoryCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleFilterByCategory((FilterByCategoryCommand) command);
        } else if (command instanceof FilterContainsNameCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleFilterContainsName((FilterContainsNameCommand) command);
        } else if (command instanceof InfoCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleInfo();
        } else if (command instanceof InsertCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleInsert((InsertCommand) command);
        } else if (command instanceof PrintFieldDescendingHealthCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handlePrintHealth();
        } else if (command instanceof RemoveCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleRemoveKey((RemoveCommand) command);
        } else if (command instanceof RemoveGreaterCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleRemoveGreater((RemoveGreaterCommand) command);
        } else if (command instanceof ReplaceIfGreaterCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleReplaceIfGreater((ReplaceIfGreaterCommand) command);
        } else if (command instanceof ReplaceIfLowerCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleReplaceIfLower((ReplaceIfLowerCommand) command);
        } else if (command instanceof ShowCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleShow();
        } else if (command instanceof UpdateCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleUpdate((UpdateCommand) command);
        } else if (command instanceof ServerSaveCommand) {
            logger.debug("Выполнение команды: {}", commandName);
            return handleServerSave();
        } else {
            logger.warn("Неизвестная команда: {}", commandName);
            return new CommandResponse(false, "Неизвестная команда");
        }
    }

    /**
     * Обработка команды clear (очистка коллекции).
     */
    private CommandResponse handleClear() {
        logger.debug("handleClear: очистка коллекции");
        collectionManager.clear();
        return new CommandResponse(true, "Коллекция очищена.");
    }

    /**
     * Обработка команды filter_by_category.
     */
    private CommandResponse handleFilterByCategory(FilterByCategoryCommand command) {
        AstartesCategory category = command.getCategory();
        logger.debug("handleFilterByCategory: категория = {}", category);

        List<SpaceMarine> filtered = collectionManager.sortedValues().stream()
                .filter(m -> command.getCategory().equals(m.getCategory()))
                .collect(Collectors.toList());

        logger.info("Фильтрация по категории {} ", category);

        if (filtered.isEmpty()) {
            return new CommandResponse(true, "Элементы с категорией " + command.getCategory() + " не найдены.");
        }
        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : filtered) {
            sb.append(marine).append("\n");
        }
        return new CommandResponse(true, sb.toString(), filtered);
    }

    /**
     * Обработка команды filter_contains_name.
     */
    private CommandResponse handleFilterContainsName(FilterContainsNameCommand command) {
        String substring = command.getSubstring().toLowerCase();
        logger.debug("handleFilterContainsName: подстрока = '{}'", substring);

        List<SpaceMarine> filtered = collectionManager.sortedValues().stream()
                .filter(m -> m.getName().toLowerCase().contains(substring))
                .collect(Collectors.toList());

        logger.info("Фильтрация по имени с подстрокой '{}': найдено {} элементов", substring, filtered.size());

        if (filtered.isEmpty()) {
            return new CommandResponse(true, "Элементы, содержащие '" + command.getSubstring() + "' в имени, не найдены.");
        }
        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : filtered) {
            sb.append(marine).append("\n");
        }
        return new CommandResponse(true, sb.toString(), filtered);
    }

    /**
     * Обработка команды info.
     */
    private CommandResponse handleInfo() {
        logger.debug("Запрос информации о коллекции");
        String info = "Тип коллекции: " + collectionManager.getCollection().getClass().getName() + "\n" +
                "Дата инициализации: " + collectionManager.getInitDate() + "\n" +
                "Количество элементов: " + collectionManager.size();

        logger.info("Выдана информация о коллекции");
        return new CommandResponse(true, info);
    }

    /**
     * Обработка команды insert.
     */
    private CommandResponse handleInsert(InsertCommand command) {
        String key = command.getKey();
        logger.debug("Ввод новых значений с ключем = '{}'", key);

        if (collectionManager.containsKey(key)) {
            logger.warn("Попытка вставки с уже существующим ключом: '{}'", key);
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' уже существует.");
        }
        SpaceMarine marine = command.getMarine();
        marine.setId(collectionManager.generateId());
        collectionManager.put(key, marine);
        logger.info("Вставлен новый элемент: ключ='{}', id={}, имя='{}'",
                key, marine.getId(), marine.getName());
        return new CommandResponse(true, "Элемент с ключом '" + key + "' успешно добавлен. ID = " + marine.getId());
    }

    /**
     * Обработка команды print_field_descending_health.
     */
    private CommandResponse handlePrintHealth() {
        logger.debug("Запрос значений здоровья");
        if (collectionManager.size() == 0) {
            logger.debug("Коллекция пуста");
            return new CommandResponse(true, "Коллекция пуста.");
        }
        List<Float> healthValues = collectionManager.sortedValues().stream()
                .map(SpaceMarine::getHealth)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        logger.info("Выведено {} значений здоровья", healthValues.size());

        StringBuilder sb = new StringBuilder();
        for (Float health : healthValues) {
            sb.append(String.format("%.2f", health)).append("\n");
        }
        return new CommandResponse(true, sb.toString(), healthValues);
    }

    /**
     * Обработка команды remove_key.
     */
    private CommandResponse handleRemoveKey(RemoveCommand command) {
        String key = command.getKey();
        logger.debug("Удалить по ключу = '{}'", key);

        if (!collectionManager.containsKey(key)) {
            logger.warn("Попытка удаления несуществующего ключа: '{}'", key);
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' не найден.");
        }
        SpaceMarine removed = collectionManager.remove(key);
        logger.info("Удалён элемент: ключ='{}', id={}, имя='{}'", key, removed.getId(), removed.getName());
        return new CommandResponse(true, "Элемент с ключом '" + key + "' успешно удалён.");
    }

    /**
     * Обработка команды remove_greater.
     */
    private CommandResponse handleRemoveGreater(RemoveGreaterCommand command) {
        SpaceMarine reference = command.getReference();
        logger.debug("Эталонный элемент с именем '{}'", reference.getName());

        reference.setId(collectionManager.generateId());

        List<String> keysToRemove = collectionManager.getCollection().entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(reference) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.debug("Найдено {} элементов для удаления", keysToRemove.size());

        for (String key : keysToRemove) {
            SpaceMarine removed = collectionManager.getByKey(key);
            logger.trace("Удаление элемента: ключ='{}', id={}, имя='{}'", key, removed.getId(), removed.getName());
        }
        logger.info("Удалено {} элементов, превышающих эталонный", keysToRemove.size());
        return new CommandResponse(true, "Удалено элементов: " + keysToRemove.size());
    }

    /**
     * Обработка команды replace_if_greater.
     */
    private CommandResponse handleReplaceIfGreater(ReplaceIfGreaterCommand command) {
        String key = command.getKey();
        logger.debug("Эталонный элемент с ключем = '{}' для замены", key);
        if (!collectionManager.containsKey(key)) {
            logger.debug("handleReplaceIfGreater: ключ = '{}'", key);
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' не найден.");
        }
        SpaceMarine oldMarine = collectionManager.getByKey(key);
        SpaceMarine newMarineData = command.getNewMarine();
        SpaceMarine newMarine = new SpaceMarine(
                oldMarine.getId(),
                newMarineData.getName(),
                newMarineData.getCoordinates(),
                oldMarine.getCreationDate(),
                newMarineData.getHealth(),
                newMarineData.getCategory(),
                newMarineData.getWeaponType(),
                newMarineData.getMeleeWeapon(),
                newMarineData.getChapter()
        );

        int comparison = newMarine.compareTo(oldMarine);
        logger.debug("Сравнение нового и старого: результат = {}", comparison);

        if (newMarine.compareTo(oldMarine) > 0) {
            collectionManager.put(key, newMarine);
            logger.info("Замена выполнена: ключ='{}', старое имя='{}', новое имя='{}', новое здоровье={}",key, oldMarine.getName(), newMarine.getName(), newMarine.getHealth());
            return new CommandResponse(true, "Значение заменено (новое больше старого).");
        } else {
            logger.debug("Замена не выполнена: новое значение ({}) не больше старого ({})", newMarine.getName(), oldMarine.getName());
            return new CommandResponse(true, "Замена не выполнена: новое значение не больше старого.");
        }
    }

    /**
     * Обработка команды replace_if_lowe.
     */
    private CommandResponse handleReplaceIfLower(ReplaceIfLowerCommand command) {
        String key = command.getKey();
        logger.debug("Замена меньшего эталонный элемент с ключем = '{}'", key);
        if (!collectionManager.containsKey(key)) {
            logger.warn("Элемент с ключом '{}' не найден", key);
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' не найден.");
        }
        SpaceMarine oldMarine = collectionManager.getByKey(key);
        SpaceMarine newMarineData = command.getNewMarine();
        SpaceMarine newMarine = new SpaceMarine(
                oldMarine.getId(),
                newMarineData.getName(),
                newMarineData.getCoordinates(),
                oldMarine.getCreationDate(),
                newMarineData.getHealth(),
                newMarineData.getCategory(),
                newMarineData.getWeaponType(),
                newMarineData.getMeleeWeapon(),
                newMarineData.getChapter()
        );

        int comparison = newMarine.compareTo(oldMarine);
        logger.debug("Сравнение нового и старого: результат = {}", comparison);

        if (newMarine.compareTo(oldMarine) < 0) {
            collectionManager.put(key, newMarine);
            logger.info("Замена выполнена: ключ='{}', старое имя='{}', новое имя='{}', новое здоровье={}",key, oldMarine.getName(), newMarine.getName(), newMarine.getHealth());
            return new CommandResponse(true, "Значение заменено (новое меньше старого).");
        } else {
            logger.debug("Замена не выполнена: новое значение ({}) не меньше старого ({})", newMarine.getName(), oldMarine.getName());
            return new CommandResponse(true, "Замена не выполнена: новое значение не меньше старого.");
        }
    }

    /**
     * Обработка команды show.
     */
    private CommandResponse handleShow() {
        int size = collectionManager.size();
        logger.debug("Вывести коллекцию");

        if (collectionManager.size() == 0) {
            logger.debug("Коллекция пуста");
            return new CommandResponse(true, "Коллекция пуста.");
        }
        List<SpaceMarine> sorted = collectionManager.sortedValues();
        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : sorted) {
            String key = collectionManager.findKeyById(marine.getId());
            sb.append("Ключ: ").append(key).append(", ").append(marine).append("\n");
        }
        logger.info("Выведено {} элементов коллекции", size);
        return new CommandResponse(true, sb.toString(), sorted);
    }

    /**
     * Обработка команды update.
     */
    private CommandResponse handleUpdate(UpdateCommand command) {
        int id = command.getId();
        logger.debug("Обновление значениев колекции с id = {}", id);
        String key = collectionManager.findKeyById(id);
        if (key == null) {
            logger.warn("Попытка обновления несуществующего элемента с id={}", id);
            return new CommandResponse(false, "Элемент с id=" + id + " не найден.");
        }
        SpaceMarine oldMarine = collectionManager.getByKey(key);
        SpaceMarine newMarineData = command.getMarine();

        logger.debug("Обновление элемента с ключом '{}', старые данные: {}", key, oldMarine.getName());

        SpaceMarine updatedMarine = new SpaceMarine(
                oldMarine.getId(),
                newMarineData.getName(),
                newMarineData.getCoordinates(),
                oldMarine.getCreationDate(),
                newMarineData.getHealth(),
                newMarineData.getCategory(),
                newMarineData.getWeaponType(),
                newMarineData.getMeleeWeapon(),
                newMarineData.getChapter()
        );
        collectionManager.put(key, updatedMarine);
        logger.info("Обновлён элемент id={}, новое имя='{}'", id, updatedMarine.getName());

        return new CommandResponse(true, "Элемент с id=" + id + " успешно обновлён.");
    }

    private CommandResponse handleServerSave() {
        logger.info("Получена команда на сохранение коллекции");
        String filePath = collectionManager.getFilePath();

        if (filePath == null || filePath.isEmpty()) {
            logger.error("Не указан путь к файлу для сохранения");
            return new CommandResponse(false, "Ошибка: путь к файлу не установлен.");
        }

        try {
            collectionManager.saveToFile(filePath);
            logger.info("Коллекция успешно сохранена в файл: {}", filePath);
            return new CommandResponse(true, "Коллекция сохранена по команде администратора.");
        } catch (Exception e) {
            logger.error("Ошибка при сохранении коллекции: {}", e.getMessage(), e);
            return new CommandResponse(false, "Ошибка сохранения: " + e.getMessage());
        }
    }
}