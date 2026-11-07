package dev.omnisheetvault.api.identity;

import dev.omnisheetvault.api.identity.CurrentUserResponse.PreferencesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** The authenticated player's interface preferences: language and theme. */
@RestController
class PreferencesController {

    private final PlayerService playerService;

    PreferencesController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PutMapping("/api/me/preferences")
    PreferencesResponse choose(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PreferencesRequest request) {
        return PreferencesResponse.of(playerService.choosePreferences(jwt, request.locale(), request.theme()));
    }

    /** A null field keeps the player's current value. */
    record PreferencesRequest(@Pattern(regexp = "en|pt-BR") String locale, @Pattern(regexp = "light|dark") String theme) {
    }
}
