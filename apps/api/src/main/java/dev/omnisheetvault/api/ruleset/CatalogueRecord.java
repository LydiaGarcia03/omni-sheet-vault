package dev.omnisheetvault.api.ruleset;

import dev.omnisheetvault.api.shared.TranslationOverlay;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * A read-only view of one catalogue entry, as a {@link CatalogueLookup} returns it.
 * {@code playtest} marks an entry from playtest material (Unearthed Arcana);
 * {@code partner} names the partner brand its source belongs to, or is {@code null}.
 * {@code translations} holds, per language tag, the text that replaces the English entry's; the rules read the English
 * fields, and only what is displayed uses {@link #localizedName} and {@link #localizedDescription}.
 */
public record CatalogueRecord(
        String kind,
        String slug,
        String name,
        String sourceBook,
        Integer sourcePage,
        String description,
        JsonNode data,
        boolean playtest,
        String partner,
        JsonNode translations) {

    private static final JsonNode NO_TRANSLATIONS = JsonNodeFactory.instance.objectNode();

    public CatalogueRecord {
        translations = translations == null ? NO_TRANSLATIONS : translations;
    }

    public CatalogueRecord(String kind, String slug, String name, String sourceBook, Integer sourcePage, String description, JsonNode data,
            boolean playtest, String partner) {
        this(kind, slug, name, sourceBook, sourcePage, description, data, playtest, partner, NO_TRANSLATIONS);
    }

    public CatalogueRecord(String kind, String slug, String name, String sourceBook, Integer sourcePage, String description, JsonNode data) {
        this(kind, slug, name, sourceBook, sourcePage, description, data, false, null, NO_TRANSLATIONS);
    }

    /** This entry's translation into the language, or a missing node. */
    public JsonNode translation(Locale locale) {
        return translations.path(locale.toLanguageTag());
    }

    /** The name as the player reads it: the translation's when it has a non-empty one, else the English. */
    public String localizedName(Locale locale) {
        return textOr(translation(locale).path("name"), name);
    }

    /** The description as the player reads it, by the same rule as {@link #localizedName}. */
    public String localizedDescription(Locale locale) {
        return textOr(translation(locale).path("description"), description);
    }

    /** Translates any text of this entry (a nested trait's name or description) into {@code locale}; other text is unchanged. */
    public UnaryOperator<String> texts(Locale locale) {
        JsonNode translation = translation(locale);
        if (!translation.isObject()) {
            return UnaryOperator.identity();
        }
        ObjectNode english = JsonNodeFactory.instance.objectNode();
        english.put("name", name);
        english.put("description", description);
        english.set("data", data);
        Map<String, String> pairs = TranslationOverlay.textPairs(english, TranslationOverlay.overlay(english, translation));
        return text -> text == null ? null : pairs.getOrDefault(text, text);
    }

    private static String textOr(JsonNode translated, String english) {
        return translated.isString() && !translated.asString().isBlank() ? translated.asString() : english;
    }
}
