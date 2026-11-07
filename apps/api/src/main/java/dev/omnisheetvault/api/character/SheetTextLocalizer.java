package dev.omnisheetvault.api.character;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Translates the catalogue texts a sheet copied: whole texts, composed names ("Fighting Style: Archery"), and row summaries. */
final class SheetTextLocalizer {

    /** A row summary rebuilt from its description's translation. */
    @FunctionalInterface
    interface Summary {
        String of(String summary, String englishDescription, String translatedDescription);
    }

    private static final int SUMMARY_LENGTH = 140;
    private static final int SUMMARY_ANCHOR = 15;
    private static final Pattern NAME_WITH_LEVEL = Pattern.compile("(.+) (\\d+)");

    private final UnaryOperator<String> translator;

    SheetTextLocalizer(UnaryOperator<String> translator) {
        this.translator = translator;
    }

    String text(String english) {
        return translator.apply(english);
    }

    /** A name as written, or its parts: "Progression: Option" halves, or a name followed by a level ("Fighter 3"). */
    String name(String english) {
        if (english == null) {
            return null;
        }
        String whole = translator.apply(english);
        if (!whole.equals(english)) {
            return whole;
        }
        int colon = english.indexOf(": ");
        if (colon > 0) {
            return name(english.substring(0, colon)) + ": " + name(english.substring(colon + 2));
        }
        Matcher withLevel = NAME_WITH_LEVEL.matcher(english);
        return withLevel.matches() ? translator.apply(withLevel.group(1)) + " " + withLevel.group(2) : english;
    }

    /** Keeps whatever precedes the description's first sentence ("d8 × 4. ") and puts the translation's first sentence after it. */
    String summary(String summary, String englishDescription, String translatedDescription) {
        if (summary == null || englishDescription == null || translatedDescription == null || translatedDescription.equals(englishDescription)) {
            return summary == null ? null : name(summary);
        }
        String english = englishDescription.strip();
        int at = english.isEmpty() ? -1 : summary.indexOf(english.substring(0, Math.min(SUMMARY_ANCHOR, english.length())));
        return at < 0 ? summary : summary.substring(0, at) + firstSentence(translatedDescription.strip());
    }

    private static String firstSentence(String description) {
        int end = description.indexOf(". ");
        String sentence = end > 0 ? description.substring(0, end + 1) : description;
        return sentence.length() <= SUMMARY_LENGTH ? sentence : sentence.substring(0, SUMMARY_LENGTH - 1) + "…";
    }
}
