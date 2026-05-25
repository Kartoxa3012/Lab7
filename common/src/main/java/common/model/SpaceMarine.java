package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс, представляющий космического десантника (Space Marine) – основной объект коллекции.
 * Поля класса:
 * <ul>
 *   <li>{@code id} – уникальный идентификатор (генерируется автоматически, >0)</li>
 *   <li>{@code name} – имя десантника (не null, не пустое)</li>
 *   <li>{@code coordinates} – координаты (не null)</li>
 *   <li>{@code creationDate} – дата создания (генерируется автоматически, не null)</li>
 *   <li>{@code health} – здоровье (>0)</li>
 *   <li>{@code category} – категория (может быть null)</li>
 *   <li>{@code weaponType} – тип оружия (не null)</li>
 *   <li>{@code meleeWeapon} – оружие ближнего боя (может быть null)</li>
 *   <li>{@code chapter} – глава (может быть null)</li>
 * </ul>
 * Реализует {@link Comparable} для сортировки по умолчанию:
 * сначала по убыванию {@code health}, затем по возрастанию {@code name}, затем по возрастанию {@code id}.
 */
public class SpaceMarine implements Comparable<SpaceMarine>, Serializable {
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private float health;
    private AstartesCategory category;
    private Weapon weaponType;
    private MeleeWeapon meleeWeapon;
    private Chapter chapter;

    /**
     * Конструктор для восстановления объекта из CSV (все поля известны).
     *
     * @param id           идентификатор (должен быть >0)
     * @param name         имя (не null, не пустое)
     * @param coordinates  координаты (не null)
     * @param creationDate дата создания (не null)
     * @param health       здоровье (>0)
     * @param category     категория (может быть null)
     * @param weaponType   тип оружия (не null)
     * @param meleeWeapon  оружие ближнего боя (может быть null)
     * @param chapter      глава (может быть null)
     */
    public SpaceMarine(int id, String name, Coordinates coordinates, LocalDateTime creationDate,
                       float health, AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.health = health;
        this.category = category;
        this.weaponType = weaponType;
        this.meleeWeapon = meleeWeapon;
        this.chapter = chapter;
        this.creationDate = creationDate;
    }

    /**
     * Конструктор для создания нового элемента (id и дата генерируются автоматически).
     *
     * @param name         имя (не null, не пустое)
     * @param coordinates  координаты (не null)
     * @param health       здоровье (>0)
     * @param category     категория (может быть null)
     * @param weaponType   тип оружия (не null)
     * @param meleeWeapon  оружие ближнего боя (может быть null)
     * @param chapter      глава (может быть null)
     */
    public SpaceMarine(String name, Coordinates coordinates, float health,
                       AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter) {
        this.name = name;
        this.coordinates = coordinates;
        this.health = health;
        this.category = category;
        this.weaponType = weaponType;
        this.meleeWeapon = meleeWeapon;
        this.chapter = chapter;
        this.creationDate = LocalDateTime.now();
    }

    /**
     * @return идентификатор
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return имя
     */
    public String getName() {
        return name;
    }

    /**
     * @return координаты
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @return дата создания
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @return здоровье
     */
    public float getHealth() {
        return health;
    }

    /**
     * @return категория (может быть null)
     */
    public AstartesCategory getCategory() {
        return category;
    }

    /**
     * @return тип оружия
     */
    public Weapon getWeaponType() {
        return weaponType;
    }

    /**
     * @return оружие ближнего боя (может быть null)
     */
    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    /**
     * @return глава (может быть null)
     */
    public Chapter getChapter() {
        return chapter;
    }

    /**
     * Устанавливает идентификатор (используется только при добавлении).
     *
     * @param id новый идентификатор (должен быть >0)
     * @throws IllegalArgumentException если id {@code <= 0}
     */
    public void setId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id должен быть больше 0");
        }
        this.id = id;
    }

    /**
     * Проверяет равенство двух объектов SpaceMarine.
     * Сравниваются все значимые поля, включая содержимое вложенных объектов.
     *
     * @param object объект для сравнения
     * @return true, если объекты равны
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SpaceMarine that = (SpaceMarine) object;
        return Float.compare(that.health, health) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(coordinates, that.coordinates) &&
                Objects.equals(creationDate, that.creationDate) &&
                category == that.category &&
                weaponType == that.weaponType &&
                meleeWeapon == that.meleeWeapon &&
                Objects.equals(chapter, that.chapter);
    }

    /**
     * Возвращает хэш-код объекта, построенный на основе всех значимых полей.
     *
     * @return хэш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, health, category, weaponType, meleeWeapon, chapter);
    }

    /**
     * Возвращает строковое представление объекта.
     *
     * @return строка с описанием полей
     */
    @Override
    public String toString() {
        return "SpaceMarine{" +
                "id=" + id +
                ", время создания=" + creationDate +
                ", имя=" + name +
                ", координаты=" + coordinates +
                ", здоровье=" + health +
                ", категория=" + category +
                ", тип оружия=" + weaponType +
                ", тип оружия ближнего боя=" + meleeWeapon +
                ", глава=" + chapter + "}";
    }

    /**
     * Сравнивает текущий объект с другим для определения порядка.
     * Порядок: сначала по убыванию health, затем по возрастанию name, затем по возрастанию id.
     *
     * @param o другой объект SpaceMarine
     * @return отрицательное число, ноль или положительное число в зависимости от порядка
     */
    @Override
    public int compareTo(SpaceMarine o) {
        int healthCompare = Float.compare(o.health, this.health);
        if (healthCompare != 0) return healthCompare;
        int nameCompare = this.name.compareTo(o.name);
        if (nameCompare != 0) return nameCompare;
        return Integer.compare(this.id, o.id);
    }
}