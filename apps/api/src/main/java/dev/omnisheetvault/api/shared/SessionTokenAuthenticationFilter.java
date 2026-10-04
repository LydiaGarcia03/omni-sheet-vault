package dev.omnisheetvault.api.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Turns a browser session's login into a {@code JwtAuthenticationToken} built from the session's access token, for
 * this request only: the token is renewed when it is about to expire, then decoded and converted.
 * A session whose tokens can no longer be renewed is treated as signed out.
 */
final class SessionTokenAuthenticationFilter extends OncePerRequestFilter {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final JwtDecoder jwtDecoder;
    private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;

    SessionTokenAuthenticationFilter(
            OAuth2AuthorizedClientManager authorizedClientManager,
            JwtDecoder jwtDecoder,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) {
        this.authorizedClientManager = authorizedClientManager;
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken login) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(tokenAuthentication(login, request, response));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }

    private Authentication tokenAuthentication(
            OAuth2AuthenticationToken login, HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuth2AuthorizedClient client = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                    .withClientRegistrationId(login.getAuthorizedClientRegistrationId())
                    .principal(login)
                    .attribute(HttpServletRequest.class.getName(), request)
                    .attribute(HttpServletResponse.class.getName(), response)
                    .build());
            if (client == null) {
                return null;
            }
            return jwtAuthenticationConverter.convert(jwtDecoder.decode(client.getAccessToken().getTokenValue()));
        } catch (OAuth2AuthorizationException | JwtException expiredOrRevoked) {
            return null;
        }
    }
}
