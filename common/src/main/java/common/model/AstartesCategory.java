package common.model;

import java.io.Serializable;

/**
 * Перечисление возможных категорий космического десантника (Astartes).
 * <p>
 * Определяет типы десантников, которые могут быть в коллекции.
 * </p>
 */
public enum AstartesCategory implements Serializable {
    /** Дредноут */
    DREADNOUGHT,
    /** Агрессор */
    AGGRESSOR,
    /** Апотекарий */
    APOTHECARY;
}