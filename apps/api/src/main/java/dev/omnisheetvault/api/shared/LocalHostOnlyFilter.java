package dev.omnisheetvault.api.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Answers only requests addressed to this machine by name. The server already listens on 127.0.0.1 alone; this also
 * stops a web page that rebinds its own domain to 127.0.0.1 (DNS rebinding) from reading the local vault.
 */
final class LocalHostOnlyFilter extends OncePerRequestFilter {

    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]", "::1");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!LOCAL_HOSTS.contains(request.getServerName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
