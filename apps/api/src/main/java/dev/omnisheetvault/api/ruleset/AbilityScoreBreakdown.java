package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * An ability score as D&D Beyond's ability pane lays it out: the total and its modifier, the base score, the bonuses
 * on top of it with where each comes from, a score an item sets, the stacking bonus, and the player's own Other
 * Modifier and Override Score (null when not set).
 */
public record AbilityScoreBreakdown(
        int total,
        int modifier,
        int base,
        int bonus,
        List<Contribution> bonusSources,
        int setScore,
        int stackingBonus,
        Integer otherModifier,
        Integer overrideScore) {
}
