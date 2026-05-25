package common.model;

import java.io.Serializable;

/**
 * Перечисление видов оружия дальнего боя (Weapon), доступных для космического десантника.
 */
public enum Weapon implements Serializable {
    /** Тяжёлый болтер */
    HEAVY_BOLTGUN,
    /** Комби-огнемёт */
    COMBI_FLAMER,
    /** Комби-плазмаган */
    COMBI_PLASMA_GUN,
    /** Ракетная установка */
    MISSILE_LAUNCHER;
}