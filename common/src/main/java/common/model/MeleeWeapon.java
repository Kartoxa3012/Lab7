package common.model;

import java.io.Serializable;

/**
 * Перечисление видов оружия ближнего боя (Melee Weapon), доступных для космического десантника.
 */
public enum MeleeWeapon implements Serializable {
    /** Цепной меч */
    CHAIN_SWORD,
    /** Силовой меч */
    POWER_SWORD,
    /** Цепной топор */
    CHAIN_AXE,
    /** Перчатка */
    LIGHTING_CLAW,
    /** Силовой клинок */
    POWER_BLADE;
}