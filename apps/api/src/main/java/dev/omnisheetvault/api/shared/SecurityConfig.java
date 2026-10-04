package dev.omnisheetvault.api.shared;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * The API is the web app's backend-for-frontend (adr-0008): a browser logs in through Keycloak and keeps only an
 * {@code HttpOnly} session cookie, while the tokens stay server-side. Each request's session access token is
 * validated against Keycloak's JWKS (the decoder comes from {@code spring.security.oauth2.resourceserver.jwt}) and
 * reaches controllers as a {@code JwtAuthenticationToken} with realm roles as authorities. No Bearer header is read.
 */
@Configuration
@EnableWebSecurity
@Profile("!desktop")
public class SecurityConfig {

    private final KeycloakLogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfig(
            @Value("${app.auth.end-session-uri}") String endSessionUri,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId) {
        this.logoutSuccessHandler = new KeycloakLogoutSuccessHandler(endSessionUri, clientId);
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientManager authorizedClientManager,
            JwtDecoder jwtDecoder) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(new BrowserLoginRequestResolver(clientRegistrations)))
                        .successHandler((request, response, authentication) -> response.sendRedirect(LoginReturnPath.consume(request))))
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterBefore(
                        new SessionTokenAuthenticationFilter(authorizedClientManager, jwtDecoder, keycloakAuthenticationConverter()),
                        AnonymousAuthenticationFilter.class)
                .build();
    }

    /** The session's tokens live in the session itself, so they survive an API restart (Spring Session JDBC). */
    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientRepository authorizedClients) {
        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build());
        return manager;
    }

    private Converter<Jwt, AbstractAuthenticationToken> keycloakAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::realmRoleAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> realmRoleAuthorities(Jwt jwt) {
        List<String> roles = KeycloakRealmRoles.from(jwt);
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toUnmodifiableList());
    }
}
