package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.AbilityScoreBreakdown;
import dev.omnisheetvault.api.ruleset.Provenance;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProvenanceResponse(
        Map<String, CalculatedValueResponse> abilityScores,
        CalculatedValueResponse speed,
        List<ProficiencySourceResponse> proficiencySources,
        Map<String, AbilityBreakdownResponse> abilityBreakdowns,
        Object customizations) {

    public record ProficiencySourceResponse(String kind, String label, String source) {
    }

    public record AbilityBreakdownResponse(
            int total, int modifier, int base, int bonus, List<ContributionResponse> bonusSources, int setScore,
            int stackingBonus, Integer otherModifier, Integer overrideScore) {

        static AbilityBreakdownResponse from(AbilityScoreBreakdown breakdown) {
            return new AbilityBreakdownResponse(breakdown.total(), breakdown.modifier(), breakdown.base(), breakdown.bonus(),
                    breakdown.bonusSources().stream().map(ContributionResponse::from).toList(), breakdown.setScore(),
                    breakdown.stackingBonus(), breakdown.otherModifier(), breakdown.overrideScore());
        }
    }

    /** Null when the system offers no provenance. */
    static ProvenanceResponse from(Provenance provenance) {
        if (provenance == null) {
            return null;
        }
        return new ProvenanceResponse(
                provenance.abilityScores().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> CalculatedValueResponse.from(entry.getValue()))),
                CalculatedValueResponse.from(provenance.speed()),
                provenance.proficiencySources().stream()
                        .map(source -> new ProficiencySourceResponse(source.kind(), source.label(), source.source()))
                        .toList(),
                provenance.abilityBreakdowns() == null ? Map.of() : provenance.abilityBreakdowns().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> AbilityBreakdownResponse.from(entry.getValue()))),
                provenance.customizations());
    }
}
