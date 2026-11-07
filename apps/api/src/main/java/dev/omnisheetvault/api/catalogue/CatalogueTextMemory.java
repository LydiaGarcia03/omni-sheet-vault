package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.shared.TranslationOverlay;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Every catalogue text with its translation, for content a sheet copied in English (feature names and descriptions):
 * the English text is the key, so a copy is translated wherever it ended up. Rebuilt when the catalogue changes.
 */
@Component
public class CatalogueTextMemory {

    private final CatalogueEntryRepository repository;
    private final ObjectMapper objectMapper;
    private final Map<String, Memory> byLanguage = new HashMap<>();

    CatalogueTextMemory(CatalogueEntryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    private record Memory(long entryCount, Instant lastUpdated, Map<String, String> texts) {
    }

    /** Translates a catalogue text into {@code locale}; any other text, or English, comes back unchanged. */
    public UnaryOperator<String> translator(Locale locale) {
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return UnaryOperator.identity();
        }
        Map<String, String> texts = texts(locale.toLanguageTag());
        return text -> text == null ? null : texts.getOrDefault(text, text);
    }

    private synchronized Map<String, String> texts(String languageTag) {
        CatalogueEntryRepository.CatalogueFingerprint fingerprint = repository.fingerprint();
        Memory cached = byLanguage.get(languageTag);
        if (cached != null && cached.entryCount() == fingerprint.getEntryCount()
                && Objects.equals(cached.lastUpdated(), fingerprint.getLastUpdated())) {
            return cached.texts();
        }
        Map<String, String> texts = new HashMap<>();
        for (CatalogueEntry entry : repository.findTranslated()) {
            JsonNode translation = objectMapper.readTree(entry.translations()).path(languageTag);
            if (translation.isObject()) {
                ObjectNode english = objectMapper.createObjectNode();
                english.put("name", entry.name());
                english.put("description", entry.description());
                english.set("data", objectMapper.readTree(entry.data()));
                TranslationOverlay.textPairs(english, TranslationOverlay.overlay(english, translation)).forEach(texts::putIfAbsent);
            }
        }
        byLanguage.put(languageTag, new Memory(fingerprint.getEntryCount(), fingerprint.getLastUpdated(), Map.copyOf(texts)));
        return byLanguage.get(languageTag).texts();
    }
}
