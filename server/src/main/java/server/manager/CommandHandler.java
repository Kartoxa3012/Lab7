package server.manager;

import common.Command;
import common.commands.*;
import common.model.SpaceMarine;
import common.CommandResponse;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Обработчик команд на сервере.
 * Получает команду от клиента, выполняет соответствующую операцию над коллекцией
 * и возвращает результат в виде {@link CommandResponse}.
 *
 * @author Kovalenko Vlad, 504673
 */
public class CommandHandler {
    private final CollectionManager collectionManager;

    public CommandHandler(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Основной метод обработки команды.
     * Определяет тип команды и вызывает соответствующий обработчик.
     *
     * @param command команда от клиента
     * @return ответ сервера
     */
    public CommandResponse handle(Command command) {
        if (command instanceof ClearCommand) {
            return handleClear();
        } else if (command instanceof FilterByCategoryCommand) {
            return handleFilterByCategory((FilterByCategoryCommand) command);
        } else if (command instanceof FilterContainsNameCommand) {
            return handleFilterContainsName((FilterContainsNameCommand) command);
        } else if (command instanceof InfoCommand) {
            return handleInfo();
        } else if (command instanceof InsertCommand) {
            return handleInsert((InsertCommand) command);
        } else if (command instanceof PrintFieldDescendingHealthCommand) {
            return handlePrintHealth();
        } else if (command instanceof RemoveCommand) {
            return handleRemoveKey((RemoveCommand) command);
        } else if (command instanceof RemoveGreaterCommand) {
            return handleRemoveGreater((RemoveGreaterCommand) command);
        } else if (command instanceof ReplaceIfGreaterCommand) {
            return handleReplaceIfGreater((ReplaceIfGreaterCommand) command);
        } else if (command instanceof ReplaceIfLowerCommand) {
            return handleReplaceIfLower((ReplaceIfLowerCommand) command);
        } else if (command instanceof ShowCommand) {
            return handleShow();
        } else if (command instanceof UpdateCommand) {
            return handleUpdate((UpdateCommand) command);
        } else if (command instanceof ServerSaveCommand) {
            return handleServerSave();
        } else {
            return new CommandResponse(false, "Неизвестная команда");
        }
    }

    /**
     * Обработка команды clear (очистка коллекции).
     */
    private CommandResponse handleClear() {
        collectionManager.clear();
        return new CommandResponse(true, "Коллекция очищена.");
    }

    /**
     * Обработка команды filter_by_category.
     */
    private CommandResponse handleFilterByCategory(FilterByCategoryCommand command) {
        List<SpaceMarine> filtered = collectionManager.sortedValues().stream()
                .filter(m -> command.getCategory().equals(m.getCategory()))
                .collect(Collectors.toList());

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
        List<SpaceMarine> filtered = collectionManager.sortedValues().stream()
                .filter(m -> m.getName().toLowerCase().contains(substring))
                .collect(Collectors.toList());

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
        String info = "Тип коллекции: " + collectionManager.getCollection().getClass().getName() + "\n" +
                "Дата инициализации: " + collectionManager.getInitDate() + "\n" +
                "Количество элементов: " + collectionManager.size();
        return new CommandResponse(true, info);
    }

    /**
     * Обработка команды insert.
     */
    private CommandResponse handleInsert(InsertCommand command) {
        String key = command.getKey();
        if (collectionManager.containsKey(key)) {
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' уже существует.");
        }
        SpaceMarine marine = command.getMarine();
        marine.setId(collectionManager.generateId());
        collectionManager.put(key, marine);
        return new CommandResponse(true, "Элемент с ключом '" + key + "' успешно добавлен. ID = " + marine.getId());
    }

    /**
     * Обработка команды print_field_descending_health.
     */
    private CommandResponse handlePrintHealth() {
        if (collectionManager.size() == 0) {
            return new CommandResponse(true, "Коллекция пуста.");
        }
        List<Float> healthValues = collectionManager.sortedValues().stream()
                .map(SpaceMarine::getHealth)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

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
        if (!collectionManager.containsKey(key)) {
            return new CommandResponse(false, "Ошибка: элемент с ключом '" + key + "' не найден.");
        }
        collectionManager.remove(key);
        return new CommandResponse(true, "Элемент с ключом '" + key + "' успешно удалён.");
    }

    /**
     * Обработка команды remove_greater.
     */
    private CommandResponse handleRemoveGreater(RemoveGreaterCommand command) {
        SpaceMarine reference = command.getReference();
        // Присваиваем временный id для корректного сравнения
        reference.setId(collectionManager.generateId());

        List<String> keysToRemove = collectionManager.getCollection().entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(reference) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (String key : keysToRemove) {
            collectionManager.remove(key);
        }
        return new CommandResponse(true, "Удалено элементов: " + keysToRemove.size());
    }

    /**
     * Обработка команды replace_if_greater.
     */
    private CommandResponse handleReplaceIfGreater(ReplaceIfGreaterCommand command) {
        String key = command.getKey();
        if (!collectionManager.containsKey(key)) {
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
        if (newMarine.compareTo(oldMarine) > 0) {
            collectionManager.put(key, newMarine);
            return new CommandResponse(true, "Значение заменено (новое больше старого).");
        } else {
            return new CommandResponse(true, "Замена не выполнена: новое значение не больше старого.");
        }
    }

    /**
     * Обработка команды replace_if_lowe.
     */
    private CommandResponse handleReplaceIfLower(ReplaceIfLowerCommand command) {
        String key = command.getKey();
        if (!collectionManager.containsKey(key)) {
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
        if (newMarine.compareTo(oldMarine) < 0) {
            collectionManager.put(key, newMarine);
            return new CommandResponse(true, "Значение заменено (новое меньше старого).");
        } else {
            return new CommandResponse(true, "Замена не выполнена: новое значение не меньше старого.");
        }
    }

    /**
     * Обработка команды show.
     */
    private CommandResponse handleShow() {
        if (collectionManager.size() == 0) {
            return new CommandResponse(true, "Коллекция пуста.");
        }
        List<SpaceMarine> sorted = collectionManager.sortedValues();
        StringBuilder sb = new StringBuilder();
        for (SpaceMarine marine : sorted) {
            String key = collectionManager.findKeyById(marine.getId());
            sb.append("Ключ: ").append(key).append(", ").append(marine).append("\n");
        }
        return new CommandResponse(true, sb.toString(), sorted);
    }

    /**
     * Обработка команды update.
     */
    private CommandResponse handleUpdate(UpdateCommand command) {
        int id = command.getId();
        String key = collectionManager.findKeyById(id);
        if (key == null) {
            return new CommandResponse(false, "Элемент с id=" + id + " не найден.");
        }
        SpaceMarine oldMarine = collectionManager.getByKey(key);
        SpaceMarine newMarineData = command.getMarine();
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
        return new CommandResponse(true, "Элемент с id=" + id + " успешно обновлён.");
    }

    private CommandResponse handleServerSave() {
        collectionManager.saveToFile(collectionManager.getFilePath());
        return new CommandResponse(true, "Коллекция сохранена по команде администратора.");
    }
}