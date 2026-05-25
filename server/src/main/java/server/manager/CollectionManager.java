package server.manager;

import common.model.SpaceMarine;
import server.utility.CsvReader;
import server.utility.CsvWriter;

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
    /** Основное хранилище (ключ — строка, значение — объект). */
    private final Hashtable<String, SpaceMarine> collection = new Hashtable<>();
    /** Дата инициализации коллекции (создания менеджера). */
    private final LocalDateTime initDate = LocalDateTime.now();
    private String filePath;

    /**
     * Возвращает всю коллекцию.
     *
     * @return Hashtable, содержащая все элементы
     */
    public Hashtable<String, SpaceMarine> getCollection() {
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
        return collection.size();
    }

    /**
     * Возвращает элемент по ключу.
     *
     * @param key ключ
     * @return элемент или null, если ключ отсутствует
     */
    public SpaceMarine getByKey(String key) {
        return collection.get(key);
    }

    /**
     * Проверяет наличие ключа.
     *
     * @param key ключ
     * @return true, если ключ существует
     */
    public boolean containsKey(String key) {
        return collection.containsKey(key);
    }

    /**
     * Добавляет или заменяет элемент по ключу.
     *
     * @param key    ключ
     * @param marine элемент
     * @return предыдущее значение или null
     */
    public SpaceMarine put(String key, SpaceMarine marine) {
        return collection.put(key, marine);
    }

    /**
     * Удаляет элемент по ключу.
     *
     * @param key ключ
     * @return удалённый элемент или null
     */
    public SpaceMarine remove(String key) {
        return collection.remove(key);
    }

    /**
     * Очищает коллекцию.
     */
    public void clear() {
        collection.clear();
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
                return entry.getKey();
            }
        }
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
        if (collection.isEmpty()) return 1;
        return collection.values().stream()
                .mapToInt(SpaceMarine::getId)
                .max()
                .getAsInt() + 1;
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
        return list;
    }

    /**
     * Загружает коллекцию из CSV-файла.
     *
     * @param filePath путь к файлу
     */
    public void loadFromFile(String filePath) {
        CsvReader.readCsv(filePath, this);
        System.out.println("Коллекция загружена из файла: " + filePath);
    }

    /**
     * Сохраняет коллекцию в CSV-файл.
     *
     * @param filePath путь к файлу
     */
    public void saveToFile(String filePath) {
        try {
            CsvWriter.writeCsv(filePath, this);
            System.out.println("Коллекция сохранена в файл: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении коллекции: " + e.getMessage());
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}