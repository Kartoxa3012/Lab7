package client.util;

import common.model.*;
import java.util.Scanner;

/**
 * Менеджер ввода данных от пользователя (клиентская часть).
 * <p>
 * Все методы обрабатывают ошибки ввода и повторяют запрос до получения корректного значения.
 * Поддерживает режим скрипта (чтение из файла вместо консоли).
 * </p>
 *
 * @author Kovalenko Vlad, 504673
 */
public class InputManager {
    private Scanner consoleScanner;      // для чтения с консоли
    private Scanner scriptScanner;       // для чтения из файла скрипта
    private boolean scriptMode;          // true — читаем из файла, false — из консоли

    /**
     * Конструктор, принимающий источник ввода для консоли.
     *
     * @param scanner источник ввода (обычно {@code new Scanner(System.in)})
     */
    public InputManager(Scanner scanner) {
        this.consoleScanner = scanner;
        this.scriptMode = false;
        this.scriptScanner = null;
    }

    /**
     * Переключает менеджер в режим чтения из файла скрипта.
     *
     * @param fileScanner Scanner, связанный с файлом скрипта
     */
    public void startScriptMode(Scanner fileScanner) {
        this.scriptScanner = fileScanner;
        this.scriptMode = true;
    }

    /**
     * Завершает режим скрипта и возвращается к чтению из консоли.
     */
    public void endScriptMode() {
        this.scriptMode = false;
        this.scriptScanner = null;
    }

    /**
     * Возвращает текущий активный Scanner.
     *
     * @return текущий Scanner (консольный или файловый)
     */
    private Scanner getCurrentScanner() {
        return scriptMode ? scriptScanner : consoleScanner;
    }

    /**
     * Читает целое число.
     *
     * @param prompt    приглашение к вводу
     * @param allowNull разрешён ли null (пустая строка)
     * @return введённое число или {@code null}, если allowNull = true и введена пустая строка
     */
    public Integer readInt(String prompt, boolean allowNull) {
        Scanner scanner = getCurrentScanner();
        while (true) {
            if (!scriptMode) System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                if (!scriptMode) {
                    System.out.println("Ошибка: введите целое число.");
                } else {
                    System.out.println("Ошибка в скрипте: ожидалось целое число, получено '" + line + "'");
                }
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
        Scanner scanner = getCurrentScanner();
        if (!scriptMode) System.out.print(prompt);
        String line = scanner.nextLine().trim();
        while (!allowEmpty && line.isEmpty()) {
            if (!scriptMode) {
                System.out.print("Значение не может быть пустым. " + prompt);
                line = scanner.nextLine().trim();
            } else {
                System.out.println("Ошибка в скрипте: поле не может быть пустым");
                return null;
            }
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
        Scanner scanner = getCurrentScanner();
        while (true) {
            if (!scriptMode) System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return 0f;
            try {
                return Float.parseFloat(line.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!scriptMode) {
                    System.out.println("Ошибка: введите число (можно дробное).");
                } else {
                    System.out.println("Ошибка в скрипте: ожидалось число, получено '" + line + "'");
                }
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
        Scanner scanner = getCurrentScanner();
        while (true) {
            if (!scriptMode) System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Float.parseFloat(line.replace(',', '.'));
            } catch (NumberFormatException e) {
                if (!scriptMode) {
                    System.out.println("Ошибка: введите число (можно дробное).");
                } else {
                    System.out.println("Ошибка в скрипте: ожидалось число, получено '" + line + "'");
                }
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
        Scanner scanner = getCurrentScanner();
        T[] constants = enumClass.getEnumConstants();

        if (!scriptMode) {
            System.out.print("Допустимые значения: ");
            for (T c : constants) System.out.print(c + " ");
            System.out.println();
        }

        while (true) {
            if (!scriptMode) System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (allowNull && line.isEmpty()) return null;
            try {
                return Enum.valueOf(enumClass, line);
            } catch (IllegalArgumentException e) {
                if (!scriptMode) {
                    System.out.println("Ошибка: введите одно из допустимых значений.");
                } else {
                    System.out.println("Ошибка в скрипте: недопустимое значение enum '" + line + "'");
                    return null;
                }
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
        if (!scriptMode) {
            System.out.println("Введите данные главы (или оставьте имя пустым, чтобы пропустить):");
        }
        String name = readString("Имя главы: ", true);
        if (name.isEmpty()) {
            return null;
        }
        Integer count = readInt("Количество морпехов (1-1000): ", false);
        while (count == null || count <= 0 || count > 1000) {
            if (!scriptMode) {
                System.out.println("Количество должно быть от 1 до 1000.");
                count = readInt("Количество морпехов (1-1000): ", false);
            } else {
                System.out.println("Ошибка в скрипте: количество морпехов должно быть от 1 до 1000");
                return null;
            }
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

        // Временно создаём с пустым ключом, он будет установлен позже
        return new SpaceMarine("", name, coords, health, category, weapon, melee, chapter);
    }

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

        // Временно создаём с пустым ключом, он будет установлен позже
        return new SpaceMarine("", name, coords, health, category, weapon, melee, chapter);
    }
}