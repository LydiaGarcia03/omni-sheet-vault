package dev.omnisheetvault.api.ruleset;

import java.util.List;
import java.util.Map;

/**
 * Where the sheet's build-derived values come from: each ability score's contributions
 * (base, species, improvements, an item's set score, the player's customizations) and its
 * breakdown for the ability pane, walking speed's, the source of every listed proficiency, and
 * the player's customizations as the game system stores them (sent as they are, for its panes).
 */
public record Provenance(
        Map<String, CalculatedValue> abilityScores,
        CalculatedValue speed,
        List<ProficiencySource> proficiencySources,
        Map<String, AbilityScoreBreakdown> abilityBreakdowns,
        Object customizations) {
}
