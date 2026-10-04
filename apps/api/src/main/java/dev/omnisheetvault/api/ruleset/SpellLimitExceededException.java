package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when learning a spell would exceed the class's own cantrip or known-spell
 * cap — see features/character-sheet.md's mutation table and
 * {@code Dnd5eSpellcastingClass}'s {@code cantripsKnownMax}/{@code spellsKnownMax}.
 */
public class SpellLimitExceededException extends RuntimeException {

    public SpellLimitExceededException(int limit) {
        super("Cannot know more than " + limit + " of this kind of spell");
    }
}
