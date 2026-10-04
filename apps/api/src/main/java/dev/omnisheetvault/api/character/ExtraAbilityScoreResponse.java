package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ExtraAbilityScore;

public record ExtraAbilityScoreResponse(String abilityKey, int score, int modifier, int save) {

    static ExtraAbilityScoreResponse from(ExtraAbilityScore abilityScore) {
        return new ExtraAbilityScoreResponse(
                abilityScore.abilityKey(), abilityScore.score(), abilityScore.modifier(), abilityScore.save());
    }
}
