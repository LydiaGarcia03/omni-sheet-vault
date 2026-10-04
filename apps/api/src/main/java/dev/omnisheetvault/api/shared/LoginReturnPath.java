package dev.omnisheetvault.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Where the browser goes after logging in: an in-app path asked for when the login started, kept in the session. */
final class LoginReturnPath {

    static final String PARAMETER = "returnTo";
    static final String DEFAULT = "/";
    private static final String SESSION_ATTRIBUTE = LoginReturnPath.class.getName();

    private LoginReturnPath() {
    }

    static void remember(HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_ATTRIBUTE, safe(request.getParameter(PARAMETER)));
    }

    static String consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return DEFAULT;
        }
        Object remembered = session.getAttribute(SESSION_ATTRIBUTE);
        session.removeAttribute(SESSION_ATTRIBUTE);
        return safe(remembered instanceof String path ? path : null);
    }

    /** Only same-app paths, so a return target can never send the browser to another site. */
    static String safe(String path) {
        boolean inApp = path != null
                && path.startsWith("/")
                && !path.startsWith("//")
                && !path.startsWith("/\\")
                && path.chars().noneMatch(Character::isISOControl);
        return inApp ? path : DEFAULT;
    }
}
