package dev.omnisheetvault.api.shared;

import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Reads Keycloak's {@code realm_access.roles} claim, which Spring Security does not
 * understand natively — it only maps the standard {@code scope}/{@code scp} claims.
 */
public final class KeycloakRealmRoles {

    private KeycloakRealmRoles() {
    }

    @SuppressWarnings("unchecked")
    public static List<String> from(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        return (List<String>) realmAccess.getOrDefault("roles", List.of());
    }
}
