package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when attuning an item would exceed the system's attunement limit — see
 * systems/dnd-5e/sheet-ui.md's Inventory tab ("attunement section with three
 * slots") and features/character-sheet.md's mutation table ("Attunement: Bounded
 * by the system's limit").
 */
public class AttunementLimitExceededException extends RuntimeException {

    public AttunementLimitExceededException(int limit) {
        super("Cannot attune more than " + limit + " items");
    }
}
