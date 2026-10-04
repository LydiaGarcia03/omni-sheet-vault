package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ExtraStatBlock;
import java.util.List;

public record ExtraStatBlockResponse(
        String size,
        String creatureType,
        String alignment,
        int initiativeBonus,
        String hitDiceLabel,
        String additionalSpeeds,
        List<ExtraAbilityScoreResponse> abilityScores,
        List<ExtraSkillResponse> skills,
        String senses,
        String languages,
        String challengeRating,
        List<ExtraStatEntryResponse> traits,
        List<ExtraStatEntryResponse> actions) {

    static ExtraStatBlockResponse from(ExtraStatBlock statBlock) {
        return new ExtraStatBlockResponse(
                statBlock.size(), statBlock.creatureType(), statBlock.alignment(), statBlock.initiativeBonus(),
                statBlock.hitDiceLabel(), statBlock.additionalSpeeds(),
                statBlock.abilityScores().stream().map(ExtraAbilityScoreResponse::from).toList(),
                statBlock.skills().stream().map(ExtraSkillResponse::from).toList(),
                statBlock.senses(), statBlock.languages(), statBlock.challengeRating(),
                statBlock.traits().stream().map(ExtraStatEntryResponse::from).toList(),
                statBlock.actions().stream().map(ExtraStatEntryResponse::from).toList());
    }
}
