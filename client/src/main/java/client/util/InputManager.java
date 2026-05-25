package client.util;

import common.model.*;

import java.util.Scanner;

/**
 * Менеджер ввода данных от пользователя (клиентская часть).
 * <p>
 * Все методы обрабатывают ошибки ввода и повторяют запрос до получения корректного значения.
 * Используется только на клиенте для чтения команд и ввода составных объектов.
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 */
public class InputManager {
    private Scanner scanner;

    /**
     * Конструктор, принимающий источник ввода.
     *
     * @param scanner источник ввода (обычно {@code new Scanner(System.in)})
     */
    public InputManager(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Читает целое число.
     *
     * @param prompt    приглашение к вводу
     * @param allowNull разрешён ли null (пустая строка)
     * @return введённое число или {@code null}, если allowNull = true и введена пустая строка
     */
    public Integer readInt(String prompt, boolean allowNull) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    /**
     * Читает строку.
     *
     * @param prompt     приглашение к вводу
     * @param allowEmpty разрешена ли пустая строка
     * @return введённая строка (может быть пустой, если allowEmpty = true)
     */
    public String readString(String prompt, boolean allowEmpty) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        while (!allowEmpty && line.isEmpty()) {
            System.out.print("Значение не может быть пустым. " + prompt);
            line = scanner.nextLine().trim();
        }
        return line;
    }

    /**
     * Читает значение типа float.
     *
     * @param prompt    приглашение к вводу
     * @param allowNull разрешён ли null (пустая строка)
     * @return введённое число; если allowNull = true и введена пустая строка, возвращается 0
     */
    public float readFloat(String prompt, boolean allowNull) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return 0f;
            try {
                return Float.parseFloat(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число (можно дробное).");
            }
        }
    }

    /**
     * Читает значение типа Float (обёртка).
     *
     * @param prompt    приглашение к вводу
     * @param allowNull разрешён ли null (пустая строка)
     * @return введённое число или {@code null}, если allowNull = true и введена пустая строка
     */
    public Float readFloatWrapper(String prompt, boolean allowNull) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Float.parseFloat(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число (можно дробное).");
            }
        }
    }

    /**
     * Читает значение перечисления (enum).
     *
     * @param prompt     приглашение к вводу
     * @param enumClass  класс перечисления
     * @param allowNull  разрешён ли null (пустая строка)
     * @param <T>        тип перечисления
     * @return выбранная константа или {@code null}, если allowNull = true и введена пустая строка
     */
    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass, boolean allowNull) {
        T[] constants = enumClass.getEnumConstants();
        System.out.print("Допустимые значения: ");
        for (T c : constants) System.out.print(c + " ");
        System.out.println();
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Enum.valueOf(enumClass, line);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите одно из допустимых значений.");
            }
        }
    }

    /**
     * Читает координаты.
     *
     * @return объект {@link Coordinates} с введёнными значениями
     */
    public Coordinates readCoordinates() {
        float x = readFloat("Введите координату x (float): ", false);
        Float y = readFloatWrapper("Введите координату y (Float, не null): ", false);
        return new Coordinates(x, y);
    }

    /**
     * Читает данные главы (Chapter). Если имя главы не введено (пустая строка),
     * возвращает {@code null}.
     *
     * @return объект {@link Chapter} или {@code null}
     */
    public Chapter readChapter() {
        System.out.println("Введите данные главы (или оставьте имя пустым, чтобы пропустить):");
        String name = readString("Имя главы: ", true);
        if (name.isEmpty()) {
            return null;
        }
        Integer count = readInt("Количество морпехов (1-1000): ", false);
        while (count == null || count <= 0 || count > 1000) {
            System.out.println("Количество должно быть от 1 до 1000.");
            count = readInt("Количество морпехов (1-1000): ", false);
        }
        String world = readString("Мир (можно оставить пустым): ", true);
        if (world.isEmpty()) world = null;
        return new Chapter(name, count, world);
    }

    /**
     * Читает все поля нового объекта {@link SpaceMarine}.
     * Поля {@code id} и {@code creationDate} не запрашиваются – они генерируются автоматически на сервере.
     *
     * @return новый объект SpaceMarine (без id и с текущей датой)
     */
    public SpaceMarine readNewMarine() {
        String name = readString("Имя: ", false);
        Coordinates coords = readCoordinates();
        float health = 0;
        while (health <= 0) {
            health = readFloat("Здоровье (>0): ", false);
            if (health <= 0) System.out.println("Здоровье должно быть больше 0.");
        }
        AstartesCategory category = readEnum("Категория (можно пропустить): ", AstartesCategory.class, true);
        Weapon weapon = readEnum("Тип оружия (не null): ", Weapon.class, false);
        MeleeWeapon melee = readEnum("Оружие ближнего боя (можно пропустить): ", MeleeWeapon.class, true);
        Chapter chapter = readChapter();
        return new SpaceMarine(name, coords, health, category, weapon, melee, chapter);
    }

    /**
     * Читает поля объекта {@link SpaceMarine} для обновления существующего.
     * Поля {@code id} и {@code creationDate} не запрашиваются (будут скопированы из старого объекта на сервере).
     *
     * @return объект SpaceMarine с новыми значениями (без id и даты)
     */
    public SpaceMarine readMarineForUpdate() {
        String name = readString("Имя: ", false);
        Coordinates coords = readCoordinates();
        float health = 0;
        while (health <= 0) {
            health = readFloat("Здоровье (>0): ", false);
            if (health <= 0) System.out.println("Здоровье должно быть больше 0.");
        }
        AstartesCategory category = readEnum("Категория (можно пропустить): ", AstartesCategory.class, true);
        Weapon weapon = readEnum("Тип оружия (не null): ", Weapon.class, false);
        MeleeWeapon melee = readEnum("Оружие ближнего боя (можно пропустить): ", MeleeWeapon.class, true);
        Chapter chapter = readChapter();
        return new SpaceMarine(name, coords, health, category, weapon, melee, chapter);
    }

    /**
     * Устанавливает новый источник ввода.
     *
     * @param scanner новый Scanner
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Возвращает текущий источник ввода.
     *
     * @return текущий Scanner
     */
    public Scanner getScanner() {
        return scanner;
    }
}