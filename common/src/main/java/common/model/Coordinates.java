package common.model;

import java.io.Serializable;

/**
 * Класс, представляющий координаты точки на плоскости.
 * Содержит два поля:
 * <ul>
 *   <li>{@code x} – координата по оси X (тип {@code float})</li>
 *   <li>{@code y} – координата по оси Y (тип {@code Float}), не может быть {@code null}</li>
 * </ul>
 *
 * @see SpaceMarine
 */
public class Coordinates implements Serializable {
    private float x;
    private Float y; // не может быть null

    /**
     * Создаёт объект координат.
     *
     * @param x координата по оси X
     * @param y координата по оси Y (не может быть {@code null})
     * @throws IllegalArgumentException если {@code y == null}
     */
    public Coordinates(float x, Float y) {
        this.x = x;
        if (y == null) {
            throw new IllegalArgumentException("координата y не может быть null");
        }
        this.y = y;
    }

    /**
     * Возвращает координату по оси X.
     *
     * @return значение x
     */
    public float getX() {
        return x;
    }

    /**
     * Возвращает координату по оси Y.
     *
     * @return значение y (никогда не {@code null})
     */
    public Float getY() {
        return y;
    }

    /**
     * Возвращает строковое представление координат.
     *
     * @return строка вида "координата по x = ..., координата по y = ..."
     */
    @Override
    public String toString() {
        return "координата по x = " + x + ", координата по y = " + y;
    }

}