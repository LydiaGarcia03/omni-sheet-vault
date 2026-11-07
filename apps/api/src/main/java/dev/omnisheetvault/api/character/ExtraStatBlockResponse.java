package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ExtraStatBlock;
import dev.omnisheetvault.api.ruleset.ExtraStatEntry;
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
        List<ExtraStatEntryResponse> actions,
        List<ExtraStatEntryResponse> reactions,
        List<ExtraStatEntryResponse> legendaryActions,
        Integer challengeXp,
        Integer challengeProficiencyBonus) {

    static ExtraStatBlockResponse from(ExtraStatBlock statBlock) {
        return new ExtraStatBlockResponse(
                statBlock.size(), statBlock.creatureType(), statBlock.alignment(), statBlock.initiativeBonus(),
                statBlock.hitDiceLabel(), statBlock.additionalSpeeds(),
                statBlock.abilityScores().stream().map(ExtraAbilityScoreResponse::from).toList(),
                statBlock.skills().stream().map(ExtraSkillResponse::from).toList(),
                statBlock.senses(), statBlock.languages(), statBlock.challengeRating(),
                entries(statBlock.traits()), entries(statBlock.actions()), entries(statBlock.reactions()), entries(statBlock.legendaryActions()),
                statBlock.challengeXp(), statBlock.challengeProficiencyBonus());
    }

    private static List<ExtraStatEntryResponse> entries(List<ExtraStatEntry> entries) {
        return entries == null ? List.of() : entries.stream().map(ExtraStatEntryResponse::from).toList();
    }
}
