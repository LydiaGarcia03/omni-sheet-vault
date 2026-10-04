package dev.omnisheetvault.api.character;

import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * A level up in progress: the class leveled and the level it reaches, the build with the new level,
 * the choices that level asks for, the builder's preview of the result, and the maximum hit points
 * before and with the new level ({@code maxHitPointsAfter} null until a sheet can form).
 */
record LevelUpResponse(
        UUID id, String name, String systemId, String classSlug, int classLevel, JsonNode build,
        DraftResponse.PlanResponse plan, DraftPreviewResponse preview, int maxHitPointsBefore, Integer maxHitPointsAfter) {

    static LevelUpResponse from(CharacterLevelUpService.LevelUpView view) {
        Character character = view.character();
        return new LevelUpResponse(character.id(), character.name(), character.systemId(), view.stored().classSlug(),
                view.stored().classLevel(), view.stored().build(), DraftResponse.PlanResponse.from(view.plan()),
                DraftPreviewResponse.from(view.preview(), view.vitals()), view.maxHitPointsBefore(), view.maxHitPointsAfter());
    }
}
