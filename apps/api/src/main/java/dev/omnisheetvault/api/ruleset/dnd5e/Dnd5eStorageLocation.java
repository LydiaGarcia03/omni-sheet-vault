package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * Where an item physically sits — see {@link Dnd5eItem}'s doc comment. Attunement is
 * not a location: an attuned item still lives in whichever of these it was placed in,
 * same as D&D Beyond (confirmed live, systems/dnd-5e/features/inventory-equipment-mechanics.md's
 * research) — the Attunement panel is a filtered cross-reference, not a fifth bucket.
 * {@code BAG_OF_HOLDING} only ever holds items when the character owns an attuned
 * item named "Bag of Holding"; its own weight counts toward carried weight, but this
 * location's contents do not — an interdimensional space, not a physical container.
 */
public enum Dnd5eStorageLocation {
    EQUIPMENT,
    BACKPACK,
    BAG_OF_HOLDING,
    OTHER_POSSESSIONS
}
