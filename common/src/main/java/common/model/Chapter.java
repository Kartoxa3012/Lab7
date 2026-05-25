package common.model;

import java.io.Serializable;

/**
 * Класс, представляющий главу (Chapter) космических десантников.
 * Поля класса:
 * <ul>
 *   <li>{@code name} – название главы, не может быть {@code null} или пустым.</li>
 *   <li>{@code marinesCount} – количество десантников в главе, не может быть {@code null},
 *       должно быть больше 0 и не превышать 1000.</li>
 *   <li>{@code world} – мир базирования главы, может быть {@code null}.</li>
 * </ul>
 */
public class Chapter implements Serializable {
    private String name;           // не null, не пустая
    private Integer marinesCount;  // не null, >0, ≤1000
    private String world;          // может быть null

    /**
     * Создаёт объект Chapter с заданными параметрами.
     * Внимание: в текущей реализации конструктор не выполняет проверку ограничений.
     * Предполагается, что валидация осуществляется вызывающим кодом до создания объекта.
     *
     * @param chName       название главы (не должно быть {@code null} или пустым)
     * @param chCount      количество десантников (не {@code null}, 1..1000)
     * @param chWorld      мир базирования (может быть {@code null})
     */
    public Chapter(String chName, Integer chCount, String chWorld) {
        this.name = chName;
        this.marinesCount = chCount;
        this.world = chWorld;
    }

    /**
     * Возвращает название главы.
     *
     * @return название главы
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает количество десантников в главе.
     *
     * @return количество десантников
     */
    public Integer getMarinesCount() {
        return marinesCount;
    }

    /**
     * Возвращает мир базирования главы.
     *
     * @return мир (может быть {@code null})
     */
    public String getWorld() {
        return world;
    }

    /**
     * Возвращает строковое представление главы.
     *
     * @return строка вида "Глава{имя='...', количество десантников=..., мир='...'}"
     */
    @Override
    public String toString() {
        return "Глава{имя='" + name + "', количество десантников=" + marinesCount +
                (world != null ? ", мир='" + world + "'" : "") + "}";
    }
}