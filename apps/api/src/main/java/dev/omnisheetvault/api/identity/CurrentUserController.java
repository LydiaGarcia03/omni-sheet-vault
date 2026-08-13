package dev.omnisheetvault.api.identity;

import dev.omnisheetvault.api.shared.KeycloakRealmRoles;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the identity carried by the access token. This is a read of the token
 * itself, not a database lookup — mapping a subject to a local {@code players} row is
 * a separate concern once that table exists.
 */
@RestController
class CurrentUserController {

    @GetMapping("/api/me")
    CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return new CurrentUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                KeycloakRealmRoles.from(jwt));
    }
}
