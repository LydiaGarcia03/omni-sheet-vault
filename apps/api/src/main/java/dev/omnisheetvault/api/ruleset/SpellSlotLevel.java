package dev.omnisheetvault.api.ruleset;

/**
 * One spell slot level's pool — see VitalsZone. {@code level} identifies which
 * level's track this is; spending or restoring a slot targets it by level and
 * {@code pact}, the same treatment {@code Dnd5eSpellSlotLevel} uses. {@code pact}
 * marks a separate pool that also refreshes on a short rest (D&D 5e Pact Magic),
 * and {@code className} the class it belongs to (null for a regular pool).
 */
public record SpellSlotLevel(int level, int maxSlots, int usedSlots, boolean pact, String className) {

    /** A regular slot pool. */
    public SpellSlotLevel(int level, int maxSlots, int usedSlots) {
        this(level, maxSlots, usedSlots, false, null);
    }
}
