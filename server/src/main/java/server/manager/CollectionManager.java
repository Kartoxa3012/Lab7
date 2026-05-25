package server.manager;

import common.model.SpaceMarine;
import server.utility.CsvReader;
import server.utility.CsvWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс, управляющий коллекцией объектов SpaceMarine.
 * <p>
 * Хранит элементы в {@code Hashtable<String, SpaceMarine>}.
 * Предоставляет методы для доступа, поиска, генерации id, загрузки и сохранения.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 */
public class CollectionManager {
    private static final Logger logger = LogManager.getLogger(CollectionManager.class);
    /** Основное хранилище (ключ — строка, значение — объект). */
    private final Hashtable<String, SpaceMarine> collection = new Hashtable<>();
    /** Дата инициализации коллекции (создания менеджера). */
    private final LocalDateTime initDate = LocalDateTime.now();
    private String filePath;

    public CollectionManager() {
        logger.info("CollectionManager инициализирован");
        logger.debug("Дата инициализации: {}", initDate);
    }


    /**
     * Возвращает всю коллекцию.
     *
     * @return Hashtable, содержащая все элементы
     */
    public Hashtable<String, SpaceMarine> getCollection() {
        logger.trace("getCollection: возвращена коллекция (размер: {})", collection.size());
        return collection;
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return дата инициализации
     */
    public LocalDateTime getInitDate() {
        return initDate;
    }

    /**
     * Возвращает количество элементов в коллекции.
     *
     * @return количество элементов
     */
    public int size() {
        int size = collection.size();
        logger.trace("size: текущий размер коллекции = {}", size);
        return collection.size();
    }

    /**
     * Возвращает элемент по ключу.
     *
     * @param key ключ
     * @return элемент или null, если ключ отсутствует
     */
    public SpaceMarine getByKey(String key) {
        SpaceMarine marine = collection.get(key);
        if (marine == null) {
            logger.debug("getByKey: элемент с ключом '{}' не найден", key);
        } else {
            logger.trace("getByKey: найден элемент с ключом '{}', id={}", key, marine.getId());
        }
        return marine;
    }

    /**
     * Проверяет наличие ключа.
     *
     * @param key ключ
     * @return true, если ключ существует
     */
    public boolean containsKey(String key) {
        boolean exists = collection.containsKey(key);
        logger.trace("containsKey: ключ '{}' {} в коллекции", key, exists ? "присутствует" : "отсутствует");
        return exists;
    }

    /**
     * Добавляет или заменяет элемент по ключу.
     *
     * @param key    ключ
     * @param marine элемент
     * @return предыдущее значение или null
     */
    public SpaceMarine put(String key, SpaceMarine marine) {
        SpaceMarine previous = collection.get(key);
        if (previous != null) {
            logger.info("Замена элемента: ключ='{}', старый id={}, новый id={}, новое имя='{}'",
                    key, previous.getId(), marine.getId(), marine.getName());
        } else {
            logger.debug("Добавление нового элемента: ключ='{}', id={}, имя='{}'",
                    key, marine.getId(), marine.getName());
        }

        SpaceMarine result = collection.put(key, marine);
        logger.debug("put: текущий размер коллекции = {}", collection.size());
        return result;
    }

    /**
     * Удаляет элемент по ключу.
     *
     * @param key ключ
     * @return удалённый элемент или null
     */
    public SpaceMarine remove(String key) {
        SpaceMarine removed = collection.remove(key);
        if (removed != null) {
            logger.info("Удалён элемент: ключ='{}', id={}, имя='{}'",
                    key, removed.getId(), removed.getName());
        } else {
            logger.warn("Попытка удаления несуществующего ключа: '{}'", key);
        }
        logger.debug("remove: текущий размер коллекции = {}", collection.size());
        return removed;
    }

    /**
     * Очищает коллекцию.
     */
    public void clear() {
        int oldSize = collection.size();
        collection.clear();
        logger.info("Коллекция очищена. Удалено {} элементов", oldSize);
    }

    /**
     * Ищет ключ, под которым хранится элемент с заданным id.
     *
     * @param id идентификатор элемента
     * @return ключ или null, если элемент не найден
     */
    public String findKeyById(Integer id) {
        for (Map.Entry<String, SpaceMarine> entry : collection.entrySet()) {
            if (entry.getValue().getId().equals(id)) {
                logger.trace("findKeyById: id={} соответствует ключу '{}'", id, entry.getKey());
                return entry.getKey();
            }
        }
        logger.debug("findKeyById: элемент с id={} не найден", id);
        return null;
    }

    /**
     * Генерирует новый уникальный идентификатор.
     * <p>
     * Если коллекция пуста, возвращает 1, иначе максимальный id + 1.
     * </p>
     *
     * @return новый уникальный id
     */
    public Integer generateId() {
        if (collection.isEmpty()) {
            logger.debug("generateId: коллекция пуста, возвращаем 1");
            return 1;
        }

        int maxId = collection.values().stream()
                .mapToInt(SpaceMarine::getId)
                .max()
                .getAsInt();

        int newId = maxId + 1;
        logger.debug("Сгенерирован новый id: максимальный id = {}, новый id = {}", maxId, newId);
        return newId;
    }

    /**
     * Возвращает все элементы коллекции, отсортированные согласно естественному порядку.
     * <p>
     * Порядок: сначала по убыванию health, затем по возрастанию name, затем по возрастанию id.
     * </p>
     *
     * @return список отсортированных элементов
     */
    public List<SpaceMarine> sortedValues() {
        List<SpaceMarine> list = new ArrayList<>(collection.values());
        Collections.sort(list);
        logger.trace("возвращено {} отсортированных элементов", list.size());
        return list;
    }

    /**
     * Загружает коллекцию из CSV-файла.
     *
     * @param filePath путь к файлу
     */
    public void loadFromFile(String filePath) {
        this.filePath = filePath;
        logger.info("Загрузка коллекции из файла: {}", filePath);

        CsvReader.readCsv(filePath, this);
        logger.info("Коллекция загружена. Загружено {} элементов", collection.size());
    }

    /**
     * Сохраняет коллекцию в CSV-файл.
     *
     * @param filePath путь к файлу
     */
    public void saveToFile(String filePath) {
        this.filePath = filePath;
        logger.info("Сохранение коллекции в файл: {} (количество элементов: {})", filePath, collection.size());

        try {
            CsvWriter.writeCsv(filePath, this);
            logger.info("Коллекция успешно сохранена в файл: {}", filePath);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении коллекции в файл {}: {}", filePath, e.getMessage(), e);
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        logger.debug("Установлен путь к файлу: {}", filePath);
    }

    public String getFilePath() {
        return filePath;

    }
}