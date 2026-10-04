package dev.omnisheetvault.api.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * The desktop edition's security: no login and no Keycloak, one local player per machine, requests from this machine
 * only. Writes still carry the CSRF token, so another web page open in the browser cannot change the vault.
 */
@Configuration
@EnableWebSecurity
@Profile("desktop")
public class DesktopSecurityConfig {

    private final String localPlayerName;

    public DesktopSecurityConfig(@Value("${user.name:Player}") String localPlayerName) {
        this.localPlayerName = localPlayerName;
    }

    @Bean
    SecurityFilterChain desktopFilterChain(HttpSecurity http) throws Exception {
        LocalPlayerSecurityContextRepository localPlayer = new LocalPlayerSecurityContextRepository(localPlayerName);
        return http
                .securityContext(context -> context.securityContextRepository(localPlayer))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .addFilterBefore(new LocalHostOnlyFilter(), CsrfFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .build();
    }
}
