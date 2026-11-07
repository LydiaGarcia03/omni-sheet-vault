package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Daggerheart's own endpoints: a character's stored sheet with its derived values, and its play-state actions. */
@RestController
class DaggerheartSheetController {

    private final CharacterService characterService;
    private final ObjectMapper objectMapper;
    private final DaggerheartDerivedStats derivedStats;
    private final DaggerheartSheetActions actions;
    private final RollService rollService;

    DaggerheartSheetController(CharacterService characterService, ObjectMapper objectMapper, RollService rollService) {
        this.characterService = characterService;
        this.rollService = rollService;
        this.objectMapper = objectMapper;
        this.derivedStats = new DaggerheartDerivedStats(objectMapper);
        this.actions = new DaggerheartSheetActions(objectMapper);
    }

    @GetMapping("/api/daggerheart/characters/{id}/sheet")
    ObjectNode sheet(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return view(storedSheet(mine(jwt, id)));
    }

    /** Applies one play-state action (trackers, damage, conditions, gold, inventory, cards, rests, counters) and returns the sheet. */
    @PostMapping("/api/daggerheart/characters/{id}/actions")
    ObjectNode act(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode action) {
        ObjectNode sheet = storedSheet(mine(jwt, id));
        actions.apply(sheet, action);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        if ("rest".equals(action.path("type").asString(""))) {
            recordRest(id, sheet.path("lastRest"));
        }
        return view(sheet);
    }

    /** Logs the dice a rest's moves rolled (1d4 + Tier), so the roll history shows them. */
    private void recordRest(UUID characterId, JsonNode lastRest) {
        String kind = lastRest.path("kind").asString("short");
        for (JsonNode move : lastRest.path("moves")) {
            if (move.has("rolled")) {
                int rolled = move.path("rolled").asInt();
                int tier = move.path("tier").asInt();
                String label = DaggerheartSheetActions.moveName(move.path("move").asString()) + " (" + kind + " rest"
                        + (move.path("forAlly").asBoolean(false) ? ", for an ally" : "") + ")";
                rollService.recordReadRoll(characterId, "rest 1d4+" + tier, label, new int[] {rolled}, rolled + tier);
            }
        }
    }

    private Character mine(Jwt jwt, UUID id) {
        Character character = characterService.getMineActive(jwt, id);
        if (!DaggerheartGameSystem.SYSTEM_ID.equals(character.systemId())) {
            throw new UnsupportedGameSystemException(character.systemId());
        }
        return character;
    }

    private ObjectNode storedSheet(Character character) {
        return (ObjectNode) objectMapper.readTree(character.sheet());
    }

    private ObjectNode view(ObjectNode stored) {
        ObjectNode sheet = stored.deepCopy();
        sheet.set("derived", derivedStats.derive(sheet));
        return sheet;
    }
}
