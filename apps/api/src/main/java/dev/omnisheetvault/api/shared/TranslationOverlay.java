package dev.omnisheetvault.api.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Lays a language's translation file over English content (features/localization.md). The translation mirrors the
 * English tree but holds only text: a non-empty string replaces the English one, an empty or missing one keeps it.
 * Inside an array, an element is addressed by its {@code name} (or {@code id}) field, every element of that name, or else
 * by its index.
 */
@Component
public class TranslationOverlay {

    private final ObjectMapper objectMapper;
    private final Map<String, Optional<JsonNode>> files = new ConcurrentHashMap<>();

    public TranslationOverlay(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * The English content in the given language: {@code resourceBase} names the translation files on the classpath,
     * e.g. {@code ruleset/vtm5/locales/vtm5-content} for {@code …/vtm5-content.pt-BR.json}.
     */
    public JsonNode localize(JsonNode english, String resourceBase, Locale locale) {
        return translation(resourceBase, locale).map(translation -> overlay(english, translation)).orElse(english);
    }

    /** The translation file for a language, read once; empty for English or a language without a file. */
    public Optional<JsonNode> translation(String resourceBase, Locale locale) {
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return Optional.empty();
        }
        String path = resourceBase + "." + locale.toLanguageTag() + ".json";
        return files.computeIfAbsent(path, this::read);
    }

    private Optional<JsonNode> read(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (var input = resource.getInputStream()) {
            return Optional.of(objectMapper.readTree(input));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unreadable translation file " + path, exception);
        }
    }

    /** {@code translation} laid over {@code english}: only non-empty strings replace English text. */
    public static JsonNode overlay(JsonNode english, JsonNode translation) {
        if (translation == null || translation.isNull() || translation.isMissingNode()) {
            return english;
        }
        if (english.isString()) {
            return translation.isString() && !translation.asString().isBlank() ? translation : english;
        }
        if (english.isObject() && translation.isObject()) {
            ObjectNode result = ((ObjectNode) english).deepCopy();
            for (String key : translation.propertyNames()) {
                JsonNode original = english.get(key);
                if (original != null) {
                    result.set(key, overlay(original, translation.get(key)));
                }
            }
            return result;
        }
        if (english.isArray() && translation.isObject()) {
            ArrayNode result = ((ArrayNode) english).deepCopy();
            for (String key : translation.propertyNames()) {
                for (int index : indexesOf(english, key)) {
                    result.set(index, overlay(english.get(index), translation.get(key)));
                }
            }
            return result;
        }
        return english;
    }

    /** Each English string paired with the string at the same place in {@code translated}, where they differ. */
    public static Map<String, String> textPairs(JsonNode english, JsonNode translated) {
        Map<String, String> pairs = new java.util.HashMap<>();
        collectPairs(english, translated, pairs);
        return pairs;
    }

    private static void collectPairs(JsonNode english, JsonNode translated, Map<String, String> pairs) {
        if (english.isString() && translated.isString()) {
            if (!english.asString().equals(translated.asString())) {
                pairs.putIfAbsent(english.asString(), translated.asString());
            }
        } else if (english.isObject()) {
            for (String key : english.propertyNames()) {
                collectPairs(english.get(key), translated.path(key), pairs);
            }
        } else if (english.isArray()) {
            for (int index = 0; index < english.size(); index++) {
                collectPairs(english.get(index), translated.path(index), pairs);
            }
        }
    }

    private static List<Integer> indexesOf(JsonNode array, String key) {
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < array.size(); index++) {
            JsonNode element = array.get(index);
            if (key.equals(element.path("name").asString(null)) || key.equals(element.path("id").asString(null))) {
                indexes.add(index);
            }
        }
        if (indexes.isEmpty() && !key.isEmpty() && key.chars().allMatch(Character::isDigit)) {
            int index = Integer.parseInt(key);
            if (index < array.size()) {
                indexes.add(index);
            }
        }
        return indexes;
    }
}
