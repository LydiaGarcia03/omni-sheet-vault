package dev.omnisheetvault.api.shared;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;

/** The interface language the web app keeps in the {@code osv_locale} cookie, shared with the Keycloak login pages. */
final class InterfaceLocaleCookie {

    static final String NAME = "osv_locale";
    static final String DEFAULT_LOCALE = "en";
    private static final Set<String> SUPPORTED = Set.of("en", "pt-BR");

    private InterfaceLocaleCookie() {
    }

    /** The cookie's language when it is a supported one, else English. */
    static String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return DEFAULT_LOCALE;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> NAME.equals(cookie.getName()) && SUPPORTED.contains(cookie.getValue()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(DEFAULT_LOCALE);
    }
}
