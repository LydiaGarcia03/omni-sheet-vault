package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.shared.InterfaceLocale;
import dev.omnisheetvault.api.shared.TranslationOverlay;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/** D&D 5e's player-facing API texts and game terms in the request's language. */
@Component
class Dnd5eMessages implements Dnd5eText {

    private static final String TERMS = "ruleset/dnd5e/locales/dnd5e-terms";

    private final MessageSource messageSource;
    private final TranslationOverlay overlay;

    Dnd5eMessages(MessageSource messageSource, TranslationOverlay overlay) {
        this.messageSource = messageSource;
        this.overlay = overlay;
    }

    @Override
    public String text(String code, Object... arguments) {
        return messageSource.getMessage("dnd5e." + code, arguments.length == 0 ? null : arguments, InterfaceLocale.current());
    }

    @Override
    public String term(String group, String key, String english) {
        String translated = overlay.translation(TERMS, InterfaceLocale.current())
                .map(terms -> terms.path("terms").path(group).path(key).asString(""))
                .orElse("");
        return translated.isBlank() ? english : translated;
    }
}
