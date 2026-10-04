package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when preparing a spell would exceed the class's own prepared-spell cap —
 * see features/character-sheet.md's mutation table ("Prepared spells: Preparing and
 * unpreparing, within the limits of each class") and
 * {@code Dnd5eSpellcastingClass#spellsPreparedMax}.
 */
public class SpellPreparationLimitExceededException extends RuntimeException {

    public SpellPreparationLimitExceededException(int limit) {
        super("Cannot prepare more than " + limit + " spells");
    }
}
