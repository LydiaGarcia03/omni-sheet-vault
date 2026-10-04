package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when attuning an item that was never flagged {@code requiresAttunement} —
 * see Dnd5eItem's doc comment and features/character-sheet.md's mutation table.
 * Distinct from {@link AttunementLimitExceededException}, which rejects attuning
 * an eligible item once the system's slot limit is already full.
 */
public class AttunementNotAllowedException extends RuntimeException {

    public AttunementNotAllowedException(String itemKey) {
        super("Item " + itemKey + " does not require attunement");
    }
}
