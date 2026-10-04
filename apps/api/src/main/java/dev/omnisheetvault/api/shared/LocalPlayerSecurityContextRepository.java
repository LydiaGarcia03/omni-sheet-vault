package dev.omnisheetvault.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * The desktop edition has one player per machine and no login: every request already belongs to that local player,
 * handed to controllers as the same {@code JwtAuthenticationToken} the server edition builds from a Keycloak session.
 * Being an existing context rather than a login per request, it keeps Spring from rotating the CSRF token each time.
 */
final class LocalPlayerSecurityContextRepository implements SecurityContextRepository {

    static final String SUBJECT = "local-player";
    private static final String ROLE = "player";

    private final JwtAuthenticationToken localPlayer;

    LocalPlayerSecurityContextRepository(String displayName) {
        Jwt jwt = Jwt.withTokenValue("local")
                .header("alg", "none")
                .subject(SUBJECT)
                .issuedAt(Instant.EPOCH)
                .claim("preferred_username", displayName)
                .claim("realm_access", Map.of("roles", List.of(ROLE)))
                .build();
        localPlayer = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + ROLE)));
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        return new DeferredSecurityContext() {
            @Override
            public SecurityContext get() {
                return localPlayerContext();
            }

            @Override
            public boolean isGenerated() {
                return false;
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return localPlayerContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // Nothing to keep: the next request is the same local player.
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return true;
    }

    private SecurityContext localPlayerContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(localPlayer);
        return context;
    }
}
