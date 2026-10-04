package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * Which of the Actions tab's filter categories a feature-granted action falls under —
 * see systems/dnd-5e/sheet-ui.md. Attacks are their own implicit category (the
 * "attack" chip), not tagged with one of these.
 */
public enum Dnd5eActionType {
    ACTION,
    BONUS_ACTION,
    REACTION,
    OTHER
}
