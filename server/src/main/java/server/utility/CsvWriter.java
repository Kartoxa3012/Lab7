package server.utility;

import server.manager.CollectionManager;
import common.model.Chapter;
import common.model.SpaceMarine;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Утилитный класс для записи коллекции в CSV-файл.
 * <p>
 * Записывает элементы коллекции в формате CSV с разделителем {@code ;} (точка с запятой).
 * Формат строки (13 полей):
 * <ol start="0">
 *     <li>ключ (String) – идентификатор элемента в коллекции</li>
 *     <li>id (int) – уникальный идентификатор</li>
 *     <li>имя (String) – имя десантника</li>
 *     <li>координата x (float)</li>
 *     <li>координата y (float)</li>
 *     <li>дата создания (LocalDateTime) в формате ISO_LOCAL_DATE_TIME</li>
 *     <li>здоровье (float)</li>
 *     <li>категория (AstartesCategory) – может быть пустым</li>
 *     <li>тип оружия (Weapon) – не может быть пустым</li>
 *     <li>оружие ближнего боя (MeleeWeapon) – может быть пустым</li>
 *     <li>название главы (String) – может быть пустым, если глава отсутствует</li>
 *     <li>количество десантников в главе (int) – может быть пустым, если глава отсутствует</li>
 *     <li>мир главы (String) – может быть пустым, если глава отсутствует</li>
 * </ol>
 * Пустые поля записываются как пустые строки между разделителями.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 * @see CollectionManager
 * @see SpaceMarine
 */
public class CsvWriter {

    /**
     * Записывает все элементы коллекции в выходной поток в формате CSV.
     * <p>
     * Для каждого элемента коллекции формируется строка из 13 полей, разделённых {@code ;}.
     * Если глава (chapter) отсутствует, три соответствующих поля остаются пустыми.
     * Если опциональные поля (category, meleeWeapon, world) отсутствуют, они также заменяются пустыми строками.
     * </p>
     *
     * @param writer  PrintWriter, связанный с файлом (обычно через {@link java.io.FileWriter})
     * @param manager менеджер коллекции, из которого извлекаются элементы
     */
    public static void writeCsv(PrintWriter writer, CollectionManager manager) {
        Hashtable<String, SpaceMarine> collection = manager.getCollection();
        for (Map.Entry<String, SpaceMarine> entry : collection.entrySet()) {
            String key = entry.getKey();
            SpaceMarine m = entry.getValue();
            List<String> fields = new ArrayList<>();
            fields.add(escape(key));
            fields.add(m.getId().toString());
            fields.add(escape(m.getName()));
            fields.add(Float.toString(m.getCoordinates().getX()));
            fields.add(m.getCoordinates().getY().toString());
            fields.add(m.getCreationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fields.add(Float.toString(m.getHealth()));
            fields.add(escape(m.getCategory() == null ? "" : m.getCategory().name()));
            fields.add(escape(m.getWeaponType().name()));
            fields.add(escape(m.getMeleeWeapon() == null ? "" : m.getMeleeWeapon().name()));
            Chapter ch = m.getChapter();
            if (ch == null) {
                fields.add(escape(""));
                fields.add(escape(""));
                fields.add(escape(""));
            } else {
                fields.add(escape(ch.getName()));
                fields.add(escape(ch.getMarinesCount().toString()));
                fields.add(escape(ch.getWorld() == null ? "" : ch.getWorld()));
            }
            writer.println(String.join(";", fields));
        }
    }

    /**
     * Экранирует строку для CSV: заключает в кавычки и удваивает внутренние кавычки.
     *
     * @param s строка для экранирования
     * @return экранированная строка
     */
    private static String escape(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    /**
     * Сохраняет коллекцию в файл по указанному пути.
     *
     * @param filePath путь к файлу
     * @param manager  менеджер коллекции
     * @throws java.io.IOException если ошибка ввода-вывода
     */
    public static void writeCsv(String filePath, CollectionManager manager) throws java.io.IOException {
        try (PrintWriter writer = new PrintWriter(new java.io.FileWriter(filePath))) {
            writeCsv(writer, manager);
        }
    }
}