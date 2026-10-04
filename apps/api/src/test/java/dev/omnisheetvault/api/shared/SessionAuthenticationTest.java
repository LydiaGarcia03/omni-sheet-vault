package dev.omnisheetvault.api.shared;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(SessionAuthenticationTest.ProbeController.class)
@Import({SecurityConfig.class, SessionAuthenticationTest.ProbeController.class})
class SessionAuthenticationTest {

    private static final String SESSION_ACCESS_TOKEN = "session-access-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRegistrationRepository clientRegistrations;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @BeforeEach
    void sessionHoldsAnAccessToken() {
        ClientRegistration keycloak = clientRegistrations.findByRegistrationId("keycloak");
        Instant now = Instant.now();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, SESSION_ACCESS_TOKEN, now, now.plusSeconds(300));
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(new OAuth2AuthorizedClient(keycloak, "player-1", accessToken));
        when(jwtDecoder.decode(SESSION_ACCESS_TOKEN)).thenReturn(Jwt.withTokenValue(SESSION_ACCESS_TOKEN)
                .header("alg", "RS256")
                .subject("player-1")
                .build());
    }

    @Test
    void aSessionLoginReachesControllersAsTheSessionsAccessToken() throws Exception {
        mockMvc.perform(get("/probe").with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(content().string("player-1"));
    }

    @Test
    void aSessionWhoseTokensCanNoLongerBeRenewedIsSignedOut() throws Exception {
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenThrow(new ClientAuthorizationException(new OAuth2Error("invalid_grant"), "keycloak"));

        mockMvc.perform(get("/probe").with(oauth2Login())).andExpect(status().isUnauthorized());
    }

    @Test
    void aCookieAuthenticatedWriteNeedsTheCsrfToken() throws Exception {
        mockMvc.perform(post("/probe").with(oauth2Login()).with(sessionCookie()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/probe").with(oauth2Login()).with(sessionCookie()).with(csrf()))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor sessionCookie() {
        return request -> {
            request.setRequestedSessionId("browser-session");
            request.setRequestedSessionIdFromCookie(true);
            return request;
        };
    }

    @Test
    void aBearerHeaderIsNotAWayIn() throws Exception {
        mockMvc.perform(get("/probe").header("Authorization", "Bearer " + SESSION_ACCESS_TOKEN))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void everyWriteNeedsTheCsrfTokenEvenWithoutASessionCookie() throws Exception {
        mockMvc.perform(post("/probe").with(oauth2Login())).andExpect(status().isForbidden());
        mockMvc.perform(post("/probe")).andExpect(status().isForbidden());
    }

    @Test
    void anAnonymousRequestGetsA401RatherThanALoginRedirect() throws Exception {
        mockMvc.perform(get("/probe")).andExpect(status().isUnauthorized());
    }

    @Test
    void theLoginStartsAtKeycloakWithTheConfidentialClientAndPkce() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/keycloak"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", allOf(
                        containsString("/realms/omni-sheet-vault/protocol/openid-connect/auth"),
                        containsString("client_id=omni-sheet-vault-bff"),
                        containsString("code_challenge_method=S256"))));
    }

    @Test
    void theLoginRemembersAnInAppReturnPathAndCanOpenTheRegistrationPage() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/keycloak").param("returnTo", "/characters/abc").param("signup", ""))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("prompt=create")))
                .andExpect(request().sessionAttribute(LoginReturnPath.class.getName(), "/characters/abc"));
    }

    @Test
    void aReturnPathOutsideTheAppFallsBackToHome() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/keycloak").param("returnTo", "https://evil.example/"))
                .andExpect(header().string("Location", not(containsString("prompt=create"))))
                .andExpect(request().sessionAttribute(LoginReturnPath.class.getName(), "/"));
    }

    @Test
    void theCsrfCookieReachesTheBrowserBeforeItsFirstWrite() throws Exception {
        mockMvc.perform(get("/probe").with(oauth2Login()))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void logoutEndsTheKeycloakSessionTooAndComesBackHome() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(oidcLogin().idToken(token -> token.tokenValue("the-id-token")))
                        .with(sessionCookie())
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", allOf(
                        startsWith("http://localhost:8081/realms/omni-sheet-vault/protocol/openid-connect/logout?"),
                        containsString("client_id=omni-sheet-vault-bff"),
                        containsString("id_token_hint=the-id-token"),
                        containsString("post_logout_redirect_uri=http://localhost/"))));
    }

    @Test
    void logoutNeedsTheCsrfTokenSoAnotherSiteCannotSignThePlayerOut() throws Exception {
        mockMvc.perform(post("/logout").with(oidcLogin()).with(sessionCookie()))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe")
        String read(@AuthenticationPrincipal Jwt jwt) {
            return jwt.getSubject();
        }

        @PostMapping("/probe")
        String write(@AuthenticationPrincipal Jwt jwt) {
            return jwt.getSubject();
        }
    }
}
