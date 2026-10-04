package dev.omnisheetvault.api.identity;

import dev.omnisheetvault.api.shared.KeycloakRealmRoles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the authenticated player. Identity and display name come from the local
 * {@code players} row (created on first sight of the subject, see
 * {@link PlayerService}); email and roles are read live from the token — Keycloak
 * owns them, so they are never duplicated into the database.
 */
@RestController
class CurrentUserController {

    private final PlayerService playerService;

    CurrentUserController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/api/me")
    CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        Player player = playerService.currentPlayer(jwt);
        return new CurrentUserResponse(
                player.id(),
                player.subject(),
                player.displayName(),
                jwt.getClaimAsString("email"),
                KeycloakRealmRoles.from(jwt));
    }
}
