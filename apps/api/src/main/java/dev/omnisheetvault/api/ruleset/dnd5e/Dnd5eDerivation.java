package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.List;
import java.util.Map;

/**
 * Where each build-derived value came from, recorded when a build is materialized:
 * the contributions to each ability score, hit points and walking speed, and the
 * source of every proficiency and sense granted.
 */
public record Dnd5eDerivation(
        Map<String, List<SourcedAmount>> abilityScores,
        List<SourcedAmount> hitPoints,
        List<SourcedAmount> speed,
        List<SourcedGrant> grants) {

    public record SourcedAmount(String source, int amount) {
    }

    /**
     * {@code kind} is e.g. {@code SKILL}, {@code EXPERTISE}, {@code TOOL}, {@code LANGUAGE}, {@code SENSE}.
     * {@code label} is the text the sheet's proficiency lists show for armor, weapon, tool and
     * language grants ("Thieves' Tools (Expertise)"); null for the other kinds.
     */
    public record SourcedGrant(String kind, String key, String source, String label) {
    }
}
