package dev.omnisheetvault.api.shared;

import java.util.Locale;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

/** The interface language of the current request; English outside a request (tools, tests), never the machine's own. */
public final class InterfaceLocale {

    private InterfaceLocale() {
    }

    public static Locale current() {
        LocaleContext context = LocaleContextHolder.getLocaleContext();
        Locale locale = context == null ? null : context.getLocale();
        return locale == null ? Locale.ENGLISH : locale;
    }
}
