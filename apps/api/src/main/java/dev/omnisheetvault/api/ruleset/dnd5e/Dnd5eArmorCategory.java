package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * An armour item's own category — matches 5etools' {@code LA}/{@code MA}/{@code HA}
 * type codes. Governs the dexterity-modifier cap applied to armour class (light: none,
 * medium: +2 cap, heavy: none allowed) — a standard PHB rule applied where armour
 * class is calculated, not stored on the item itself.
 */
public enum Dnd5eArmorCategory {
    LIGHT,
    MEDIUM,
    HEAVY
}
