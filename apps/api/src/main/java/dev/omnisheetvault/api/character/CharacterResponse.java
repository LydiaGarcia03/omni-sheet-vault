package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.SummaryFact;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** {@code portrait} is null when the character has none; {@code summary} is the card's facts, composed by its game system. */
public record CharacterResponse(
        UUID id, String name, String systemId, CharacterStatus status, Instant createdAt, PortraitResponse portrait,
        List<SummaryFactResponse> summary) {

    public record SummaryFactResponse(String label, String value) {

        static SummaryFactResponse from(SummaryFact fact) {
            return new SummaryFactResponse(fact.label(), fact.value());
        }
    }

    static CharacterResponse from(Character character, PortraitResponse portrait, List<SummaryFact> summary) {
        return new CharacterResponse(
                character.id(), character.name(), character.systemId(), character.status(), character.createdAt(), portrait,
                summary.stream().map(SummaryFactResponse::from).toList());
    }
}
