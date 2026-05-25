package server.utility;

import common.model.*;
import server.manager.CollectionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Утилитарный класс для чтения коллекции из CSV-файла.
 * <p>
 * Формат файла: строки с разделителем {@code ;} (точка с запятой).
 * Каждая строка должна содержать ровно 13 полей в следующем порядке:
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
 *     <li>название главы (String) – может быть пустым</li>
 *     <li>количество десантников в главе (int) – может быть пустым, если глава отсутствует</li>
 *     <li>мир главы (String) – может быть пустым</li>
 * </ol>
 * Пустые поля обозначаются отсутствием значения между разделителями (например, {@code ;;});
 * При чтении некорректные строки пропускаются с выводом сообщения в {@code System.err}.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 * @see CollectionManager
 * @see SpaceMarine
 */
public class CsvReader {

    /**
     * Загружает коллекцию из CSV-файла по указанному пути.
     * <p>
     * Метод открывает файл, построчно разбирает его содержимое, создаёт объекты
     * При возникновении ошибок (недостаток полей, неверный формат данных и т.п.) строка пропускается,
     * а сообщение об ошибке выводится в стандартный поток ошибок.
     * </p>
     *
     * @param filePath           путь к CSV-файлу
     * @param collectionManager  менеджер коллекции, в который будут добавлены элементы
     */
    public static void readCsv(String filePath, CollectionManager collectionManager) {
        System.out.println("Чтение файла: " + filePath);

        if (!filePath.toLowerCase().endsWith(".csv")) {
            System.err.println("Ошибка: файл должен иметь расширение .csv");
            System.exit(1);
        }

        try (Scanner scanner = new Scanner(new File(filePath))) {
            int rowCount = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                rowCount++;

                List<String> fields = parseCsvLine(line);

                if (fields.size() < 13) {
                    System.err.println("Строка " + rowCount + ": недостаточно полей, пропущена.");
                    System.err.println("Ожидалось 13 полей, получено " + fields.size());
                    continue;
                }

                try {
                    String key = fields.get(0);
                    if (key.isEmpty()) {
                        System.err.println("Строка " + rowCount + ": пустой ключ, пропущена.");
                        continue;
                    }

                    int id = Integer.parseInt(fields.get(1));
                    String name = fields.get(2);
                    float x = Float.parseFloat(fields.get(3));
                    Float y = fields.get(4).isEmpty() ? null : Float.parseFloat(fields.get(4));
                    if (y == null) throw new IllegalArgumentException("y не может быть null");

                    LocalDateTime creationDate = LocalDateTime.parse(fields.get(5));
                    float health = Float.parseFloat(fields.get(6));

                    AstartesCategory category = fields.get(7).isEmpty() ? null : AstartesCategory.valueOf(fields.get(7));
                    Weapon weapon = Weapon.valueOf(fields.get(8));
                    MeleeWeapon meleeWeapon = fields.get(9).isEmpty() ? null : MeleeWeapon.valueOf(fields.get(9));

                    Chapter chapter = null;
                    if (!fields.get(10).isEmpty()) {
                        String chName = fields.get(10);
                        int chCount = Integer.parseInt(fields.get(11));
                        String chWorld = fields.get(12).isEmpty() ? null : fields.get(12);
                        chapter = new Chapter(chName, chCount, chWorld);
                    }

                    Coordinates coords = new Coordinates(x, y);
                    SpaceMarine marine = new SpaceMarine(id, name, coords, creationDate, health, category, weapon, meleeWeapon, chapter);
                    collectionManager.put(key, marine);

                } catch (Exception e) {
                    System.err.println("Строка " + rowCount + ": ошибка парсинга – " + e.getMessage());
                }
            }

            System.out.println("Всего прочитано строк: " + rowCount);

        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден: " + e.getMessage());
            System.err.println("Проверьте путь: " + filePath);
            System.exit(1);
        }
    }

    /**
     * Парсит строку CSV с учётом кавычек.
     *
     * @param line строка для парсинга
     * @return список полей
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ';' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}