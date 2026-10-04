package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * A custom action's real activation cost, confirmed live against D&D Beyond's own
 * "Add New Actions" form (systems/dnd-5e/references/sheet-fidelity-audit.md's punch list item
 * 7) — eight values, four more than {@link Dnd5eActionType}'s own Actions-tab
 * buckets. {@link #toActionType()} folds the four this app's tab has no section for
 * into {@link Dnd5eActionType#OTHER}, a deliberate, documented fold rather than a
 * guess: the value itself is still stored and shown in full on the action's own
 * detail, only the tab-bucketing loses the distinction. {@code NONE} is D&D
 * Beyond's own unset {@code --} option.
 */
public enum Dnd5eActivationType {
    NONE,
    ACTION,
    NO_ACTION,
    BONUS_ACTION,
    REACTION,
    MINUTE,
    HOUR,
    SPECIAL;

    Dnd5eActionType toActionType() {
        return switch (this) {
            case ACTION -> Dnd5eActionType.ACTION;
            case BONUS_ACTION -> Dnd5eActionType.BONUS_ACTION;
            case REACTION -> Dnd5eActionType.REACTION;
            case NONE, NO_ACTION, MINUTE, HOUR, SPECIAL -> Dnd5eActionType.OTHER;
        };
    }
}
