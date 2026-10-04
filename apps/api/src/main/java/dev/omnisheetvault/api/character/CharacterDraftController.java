package dev.omnisheetvault.api.character;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
class CharacterDraftController {

    private final CharacterDraftService draftService;
    private final CharacterPortraitService portraitService;
    private final CharacterSummaryService summaryService;
    private final ObjectMapper objectMapper;

    CharacterDraftController(
            CharacterDraftService draftService, CharacterPortraitService portraitService, CharacterSummaryService summaryService,
            ObjectMapper objectMapper) {
        this.draftService = draftService;
        this.portraitService = portraitService;
        this.summaryService = summaryService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/characters/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    DraftResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateCharacterRequest request) {
        return respond(draftService.create(jwt, request.name(), request.systemId()));
    }

    @GetMapping("/api/characters/{id}/draft")
    DraftResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return respond(draftService.get(jwt, id));
    }

    @PutMapping("/api/characters/{id}/draft")
    DraftResponse save(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody SaveDraftRequest request) {
        String buildJson = objectMapper.writeValueAsString(request.build());
        return respond(draftService.save(jwt, id, request.name(), buildJson));
    }

    @PostMapping("/api/characters/{id}/draft/finish")
    CharacterResponse finish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Character character = draftService.finish(jwt, id);
        return CharacterResponse.from(character, portraitService.describe(character), summaryService.summarize(character));
    }

    private DraftResponse respond(CharacterDraftService.DraftView view) {
        return DraftResponse.from(view, objectMapper, portraitService.describe(view.character()));
    }
}
