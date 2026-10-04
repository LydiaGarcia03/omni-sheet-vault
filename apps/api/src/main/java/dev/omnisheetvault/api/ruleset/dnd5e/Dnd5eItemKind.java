package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * What mechanical shape an item has — see {@link Dnd5eItem}'s doc comment. A shield
 * is its own kind, not {@code ARMOR}: it never has an {@link Dnd5eArmorCategory} and
 * its armour class contribution works differently (a flat bonus, not a base value
 * with a dexterity cap) — confirmed against 5etools' own data, see
 * systems/dnd-5e/features/inventory-equipment-mechanics.md.
 */
public enum Dnd5eItemKind {
    WEAPON,
    ARMOR,
    SHIELD,
    GEAR
}
