package dev.omnisheetvault.api.catalogue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strips 5etools' inline {@code {@tag ...}} markup down to plain text, following the
 * per-tag rules of 5etools' own {@code Renderer.stripTags} ({@code js/render.js}).
 * Entity tags are {@code name|source|display…}, so the display text sits at a fixed
 * segment per tag family and the name is the fallback. A tag with no known rule fails
 * ingestion rather than being guessed. Runs until no markup remains, since a payload
 * can itself contain a nested tag.
 */
final class TagMarkupStripper {

    private static final Pattern TAG = Pattern.compile("\\{@(\\w++)(?:\\s++([^{}]*+))?}");

    private static final Set<String> TEXT_STYLE_TAGS = Set.of(
            "b", "bold", "i", "italic", "s", "strike", "s2", "strikeDouble", "u", "underline", "u2",
            "underlineDouble", "sup", "sub", "kbd", "code", "style", "font", "comic", "comicH1", "comicH2",
            "comicH3", "comicH4", "comicNote", "note", "tip");

    private static final Set<String> NO_DISPLAY_TEXT_TAGS = Set.of(
            "5etools", "5etoolsImg", "5etoolsAudio", "adventure", "book", "filter", "footnote", "link",
            "loader", "color", "highlight", "help");

    private static final Map<String, Integer> DISPLAY_TEXT_SEGMENT = displayTextSegments();

    private TagMarkupStripper() {
    }

    static String strip(String text) {
        if (text == null) {
            return null;
        }
        String previous;
        String current = text;
        do {
            previous = current;
            current = stripOnePass(current);
        } while (!current.equals(previous));
        return current;
    }

    private static String stripOnePass(String text) {
        Matcher matcher = TAG.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String payload = matcher.group(2) == null ? "" : matcher.group(2);
            matcher.appendReplacement(result, Matcher.quoteReplacement(render(matcher.group(1), payload)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String render(String tag, String payload) {
        String[] parts = Arrays.stream(payload.split("\\|", -1)).map(String::trim).toArray(String[]::new);
        if (TEXT_STYLE_TAGS.contains(tag) || NO_DISPLAY_TEXT_TAGS.contains(tag)) {
            return parts[0];
        }
        Integer displaySegment = DISPLAY_TEXT_SEGMENT.get(tag);
        if (displaySegment != null) {
            return segmentOr(parts, displaySegment, parts[0]);
        }
        return switch (tag) {
            case "damage", "dice", "autodice" -> segmentOr(parts, 1, parts[0].replace(';', '/'));
            case "d20", "hit", "initiative" -> segmentOr(parts, 1, signedIfNumber(parts[0]));
            case "chance" -> segmentOr(parts, 1, parts[0] + " percent");
            case "savingThrow", "skillCheck" -> segmentOr(parts, 1, parts[0]);
            case "scaledice", "scaledamage" -> segmentOr(parts, 4, segmentOr(parts, 2, parts[0]));
            case "dc" -> "DC " + segmentOr(parts, 1, parts[0]);
            case "dcYourSpellSave" -> segmentOr(parts, 0, "your spell save DC");
            case "hitYourSpellAttack" -> segmentOr(parts, 0, "your spell attack modifier");
            case "coinflip" -> segmentOr(parts, 0, "flip a coin");
            case "quickref" -> segmentOr(parts, 4, parts[0]);
            case "h" -> "Hit: ";
            case "m" -> "Miss: ";
            case "hom" -> "Hit or Miss: ";
            case "atk" -> attackText(parts[0], false);
            case "atkr" -> attackText(parts[0], true);
            case "recharge" -> rechargeText(parts[0]);
            default -> throw new FiveEToolsIngestException("Unsupported 5etools tag: {@" + tag + " " + payload + "}");
        };
    }

    private static Map<String, Integer> displayTextSegments() {
        Map<String, Integer> segments = new HashMap<>();
        for (String tag : Set.of(
                "action", "background", "boon", "charoption", "class", "condition", "creature", "creatureFluff",
                "cult", "deck", "disease", "facility", "feat", "hazard", "item", "itemProperty", "itemMastery",
                "language", "legroup", "object", "optfeature", "psionic", "race", "raceFluff", "recipe",
                "crochet", "crochetFluff", "reward", "vehicle", "vehupgrade", "sense", "skill", "spell",
                "status", "table", "trap", "variantrule", "cite")) {
            segments.put(tag, 2);
        }
        segments.put("card", 3);
        segments.put("deity", 3);
        segments.put("subclass", 4);
        segments.put("classFeature", 5);
        segments.put("subclassFeature", 7);
        return Map.copyOf(segments);
    }

    private static String segmentOr(String[] parts, int index, String fallback) {
        return parts.length > index && !parts[index].isEmpty() ? parts[index] : fallback;
    }

    private static String signedIfNumber(String text) {
        try {
            int value = Integer.parseInt(text);
            return value >= 0 ? "+" + value : String.valueOf(value);
        } catch (NumberFormatException e) {
            return text;
        }
    }

    /** {@code mw} → "Melee Weapon Attack:"; comma-separated groups join with " or ". */
    private static String attackText(String codes, boolean isRoll) {
        StringBuilder text = new StringBuilder();
        for (String group : codes.toLowerCase(Locale.ROOT).split(",")) {
            if (!text.isEmpty()) {
                text.append(" or ");
            }
            String type = group.contains("m") ? "Melee " : group.contains("r") ? "Ranged "
                    : group.contains("g") ? "Magical " : group.contains("a") ? "Area " : "";
            String method = group.contains("w") ? "Weapon " : group.contains("s") ? "Spell "
                    : group.contains("p") ? "Power " : "";
            text.append(type).append(method);
        }
        return text + "Attack" + (isRoll ? " Roll" : "") + ":";
    }

    private static String rechargeText(String value) {
        int recharge = value.isEmpty() ? 6 : Integer.parseInt(value);
        return "(Recharge " + recharge + (recharge < 6 ? "–6" : "") + ")";
    }
}
