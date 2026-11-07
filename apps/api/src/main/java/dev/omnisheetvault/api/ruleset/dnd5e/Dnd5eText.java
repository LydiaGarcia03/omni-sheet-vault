package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Locale;
import org.springframework.context.support.ResourceBundleMessageSource;

/** D&D 5e's player-facing API texts ({@code locales/dnd5e-messages_*.properties}) in one language; codes omit the {@code dnd5e.} prefix. */
@FunctionalInterface
interface Dnd5eText {

    String text(String code, Object... arguments);

    /** The English texts, for code that runs outside a request: tests and command-line tools. */
    Dnd5eText ENGLISH = englishTexts();

    /** A game term from the owner's term list ({@code dnd5e-terms}, e.g. group {@code skills}, key {@code athletics}); {@code english} when untranslated. */
    default String term(String group, String key, String english) {
        return english;
    }

    /** A distance given in feet as the player reads it: "30 ft" in English, "9 m" in Portuguese (5 ft = 1.5 m). */
    default String distance(int feet) {
        double factor = Double.parseDouble(text("unit.distanceFactor"));
        return text("unit.distance", Math.round(feet * factor * 10) / 10.0);
    }

    /** "1st", "2nd"… in English; "1º", "2º" in Portuguese. */
    default String ordinal(int number) {
        int lastTwo = number % 100;
        String suffix = lastTwo >= 11 && lastTwo <= 13 ? "th" : switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
        return text("ordinal." + suffix, String.valueOf(number));
    }

    private static Dnd5eText englishTexts() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("locales/dnd5e-messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setDefaultLocale(Locale.ENGLISH);
        return (code, arguments) -> source.getMessage("dnd5e." + code, arguments.length == 0 ? null : arguments, Locale.ENGLISH);
    }
}
