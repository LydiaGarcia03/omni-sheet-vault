package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.shared.InterfaceLocale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/** VtM's player-facing API texts ({@code locales/vtm5-messages_*.properties}) and content names, in the request's language. */
@Component
class Vtm5Messages {

    private final MessageSource messageSource;
    private final Vtm5Content content;

    Vtm5Messages(MessageSource messageSource, Vtm5Content content) {
        this.messageSource = messageSource;
        this.content = content;
    }

    String text(String code, Object... arguments) {
        return messageSource.getMessage("vtm5." + code, arguments.length == 0 ? null : arguments, InterfaceLocale.current());
    }

    /** A content name (clan, discipline, power…) as the player reads it. */
    String term(String name) {
        return content.term(name, InterfaceLocale.current());
    }
}
