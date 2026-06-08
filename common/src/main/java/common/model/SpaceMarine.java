package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс, представляющий космического десантника.
 *
 * @author Kovalenko Vlad, 504673
 */
public class SpaceMarine implements Comparable<SpaceMarine>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String key;                    // ключ в коллекции
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private float health;
    private AstartesCategory category;
    private Weapon weaponType;
    private MeleeWeapon meleeWeapon;
    private Chapter chapter;

    /**
     * Конструктор для нового элемента (без id, id генерируется БД).
     */
    public SpaceMarine(String key, String name, Coordinates coordinates, float health,
                       AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter) {
        this.key = key;
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
     * Конструктор для загрузки из БД (со всеми полями).
     */
    public SpaceMarine(Integer id, String key, String name, Coordinates coordinates, LocalDateTime creationDate,
                       float health, AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.health = health;
        this.category = category;
        this.weaponType = weaponType;
        this.meleeWeapon = meleeWeapon;
        this.chapter = chapter;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public float getHealth() { return health; }
    public AstartesCategory getCategory() { return category; }
    public Weapon getWeaponType() { return weaponType; }
    public MeleeWeapon getMeleeWeapon() { return meleeWeapon; }
    public Chapter getChapter() { return chapter; }

    @Override
    public int compareTo(SpaceMarine o) {
        int healthCompare = Float.compare(o.health, this.health);
        if (healthCompare != 0) return healthCompare;
        int nameCompare = this.name.compareTo(o.name);
        if (nameCompare != 0) return nameCompare;
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return "SpaceMarine{" +
                "key='" + key + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", health=" + health +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceMarine that = (SpaceMarine) o;
        return Objects.equals(key, that.key) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, id);
    }
}