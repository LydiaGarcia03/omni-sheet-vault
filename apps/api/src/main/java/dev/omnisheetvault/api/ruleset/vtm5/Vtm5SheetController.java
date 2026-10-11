package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
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

/** VtM 5e's own endpoints: its content for the builder and sheet, and a character's sheet with derived values. */
@RestController
class Vtm5SheetController {

    private final CharacterService characterService;
    private final Vtm5Content content;
    private final ObjectMapper objectMapper;
    private final Vtm5SheetActions actions;

    Vtm5SheetController(CharacterService characterService, Vtm5Content content, ObjectMapper objectMapper, Vtm5SheetActions actions) {
        this.characterService = characterService;
        this.content = content;
        this.objectMapper = objectMapper;
        this.actions = actions;
    }

    @GetMapping("/api/vtm5/content")
    JsonNode content() {
        return content.all();
    }

    @GetMapping("/api/vtm5/characters/{id}/sheet")
    JsonNode sheet(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return view(storedSheet(mine(jwt, id)));
    }

    /** Applies one play-state action (trackers, notes, identity, experience) and returns the updated sheet. */
    @PostMapping("/api/vtm5/characters/{id}/actions")
    JsonNode act(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode action) {
        ObjectNode sheet = storedSheet(mine(jwt, id));
        actions.apply(sheet, action);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        return view(sheet);
    }

    private Character mine(Jwt jwt, UUID id) {
        Character character = characterService.getMineActive(jwt, id);
        if (!Vtm5GameSystem.SYSTEM_ID.equals(character.systemId())) {
            throw new UnsupportedGameSystemException(character.systemId());
        }
        return character;
    }

    private ObjectNode storedSheet(Character character) {
        return (ObjectNode) objectMapper.readTree(character.sheet());
    }

    private ObjectNode view(ObjectNode stored) {
        ObjectNode sheet = stored.deepCopy();
        sheet.set("derived", derived(sheet));
        return sheet;
    }

    private ObjectNode derived(JsonNode sheet) {
        ObjectNode derived = objectMapper.createObjectNode();
        JsonNode attributes = sheet.path("attributes");
        derived.put("healthMax", attributes.path("Stamina").asInt() + 3);
        derived.put("willpowerMax", attributes.path("Composure").asInt() + attributes.path("Resolve").asInt());
        JsonNode row = content.bloodPotencyRow(sheet.path("bloodPotency").asInt());
        derived.put("bloodSurge", row.path(0).asString());
        derived.put("mend", row.path(1).asString());
        derived.put("powerBonus", row.path(2).asString());
        derived.put("rouseReroll", row.path(3).asString());
        derived.put("baneSeverity", row.path(4).asString());
        int earned = 0;
        int spent = 0;
        for (JsonNode entry : sheet.path("xpLog")) {
            if ("award".equals(entry.path("kind").asString())) {
                earned += entry.path("amount").asInt();
            } else if (!entry.path("undone").asBoolean(false)) {
                spent += entry.path("cost").asInt();
            }
        }
        derived.put("xpEarned", earned);
        derived.put("xpSpent", spent);
        derived.put("xpAvailable", earned - spent);
        content.clan(sheet.path("identity").path("clan").asString()).ifPresent(clan -> derived.set("clan", clan));
        actions.nextCosts(sheet, derived.putObject("nextCost"));
        derived.put("coteriePool", Vtm5SheetActions.coteriePool(sheet.path("coterie")));
        derived.put("coterieSpent", Vtm5SheetActions.coterieSpent(sheet.path("coterie")));
        return derived;
    }
}
