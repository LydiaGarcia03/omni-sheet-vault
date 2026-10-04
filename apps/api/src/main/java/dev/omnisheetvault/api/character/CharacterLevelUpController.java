package dev.omnisheetvault.api.character;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
class CharacterLevelUpController {

    record StartLevelUpRequest(@NotBlank String classSlug) {
    }

    record SaveLevelUpRequest(@NotNull JsonNode build) {
    }

    private final CharacterLevelUpService levelUpService;
    private final CharacterPortraitService portraitService;
    private final CharacterSummaryService summaryService;

    CharacterLevelUpController(
            CharacterLevelUpService levelUpService, CharacterPortraitService portraitService, CharacterSummaryService summaryService) {
        this.levelUpService = levelUpService;
        this.portraitService = portraitService;
        this.summaryService = summaryService;
    }

    @PostMapping("/api/characters/{id}/level-up")
    LevelUpResponse start(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody StartLevelUpRequest request) {
        return LevelUpResponse.from(levelUpService.start(jwt, id, request.classSlug()));
    }

    @GetMapping("/api/characters/{id}/level-up")
    LevelUpResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return LevelUpResponse.from(levelUpService.get(jwt, id));
    }

    @PutMapping("/api/characters/{id}/level-up")
    LevelUpResponse save(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody SaveLevelUpRequest request) {
        return LevelUpResponse.from(levelUpService.save(jwt, id, request.build()));
    }

    @DeleteMapping("/api/characters/{id}/level-up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        levelUpService.cancel(jwt, id);
    }

    @PostMapping("/api/characters/{id}/level-up/finish")
    CharacterResponse finish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Character character = levelUpService.finish(jwt, id);
        return CharacterResponse.from(character, portraitService.describe(character), summaryService.summarize(character));
    }
}
