package dev.omnisheetvault.api.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRolesTest {

    @Test
    void readsRolesFromTheRealmAccessClaim() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("player", "gm"))));

        assertThat(KeycloakRealmRoles.from(jwt)).containsExactly("player", "gm");
    }

    @Test
    void returnsEmptyWhenTheClaimIsAbsent() {
        Jwt jwt = jwtWithClaims(Map.of());

        assertThat(KeycloakRealmRoles.from(jwt)).isEmpty();
    }

    private Jwt jwtWithClaims(Map<String, Object> extraClaims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("11111111-1111-1111-1111-111111111111")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claims(claims -> claims.putAll(extraClaims))
                .build();
    }
}
