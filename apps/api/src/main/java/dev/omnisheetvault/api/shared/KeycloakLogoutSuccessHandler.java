package dev.omnisheetvault.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * After the API's session ends, also ends the Keycloak session (RP-initiated logout) and returns to the app's home.
 * Without a session login there is no Keycloak session to end, so it goes straight home.
 */
final class KeycloakLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String endSessionUri;
    private final String clientId;

    KeycloakLogoutSuccessHandler(String endSessionUri, String clientId) {
        this.endSessionUri = endSessionUri;
        this.clientId = clientId;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        String home = ServletUriComponentsBuilder.fromContextPath(request).path("/").toUriString();
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser user)) {
            response.sendRedirect(home);
            return;
        }
        response.sendRedirect(UriComponentsBuilder.fromUriString(endSessionUri)
                .queryParam("client_id", clientId)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue())
                .queryParam("post_logout_redirect_uri", home)
                .encode()
                .toUriString());
    }
}
