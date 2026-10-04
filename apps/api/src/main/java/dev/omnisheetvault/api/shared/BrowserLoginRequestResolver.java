package dev.omnisheetvault.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Starts the browser's Keycloak login with PKCE. {@code ?returnTo=/path} is remembered for after the login, and
 * {@code ?signup} opens Keycloak's registration page instead of the sign-in form (OIDC {@code prompt=create}).
 */
final class BrowserLoginRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";
    static final String SIGN_UP_PARAMETER = "signup";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    BrowserLoginRequestResolver(ClientRegistrationRepository clientRegistrations) {
        delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrations, AUTHORIZATION_BASE_URI);
        delegate.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return startingLogin(request, delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return startingLogin(request, delegate.resolve(request, clientRegistrationId));
    }

    private static OAuth2AuthorizationRequest startingLogin(HttpServletRequest request, OAuth2AuthorizationRequest authorization) {
        if (authorization == null) {
            return null;
        }
        LoginReturnPath.remember(request);
        if (request.getParameter(SIGN_UP_PARAMETER) == null) {
            return authorization;
        }
        return OAuth2AuthorizationRequest.from(authorization)
                .additionalParameters(parameters -> parameters.put("prompt", "create"))
                .build();
    }
}
