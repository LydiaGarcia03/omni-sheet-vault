package dev.omnisheetvault.api.identity;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/** Which tutorials the authenticated player has already finished or skipped, so each opens on its own only once. */
@RestController
class TutorialController {

    private final PlayerService playerService;

    TutorialController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/api/me/tutorials")
    SeenTutorialsResponse seenTutorials(@AuthenticationPrincipal Jwt jwt) {
        return new SeenTutorialsResponse(playerService.currentPlayer(jwt).seenTutorials());
    }

    @PutMapping("/api/me/tutorials/{tutorialId}")
    ResponseEntity<Void> markSeen(@AuthenticationPrincipal Jwt jwt, @PathVariable String tutorialId) {
        playerService.markTutorialSeen(jwt, tutorialId);
        return ResponseEntity.noContent().build();
    }

    record SeenTutorialsResponse(List<String> seen) {
    }
}
