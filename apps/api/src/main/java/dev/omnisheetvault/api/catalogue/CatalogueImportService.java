package dev.omnisheetvault.api.catalogue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Reads every entry file in a directory (see {@link CatalogueEntryFiles}) into catalogue entries,
 * upserting by the natural key {@code (systemId, kind, slug)} so re-running an import
 * is reproducible — see roadmap.md phase 7's "Done when". Never runs on its own; see
 * {@link CatalogueImportRunner} for the only caller.
 */
@Service
public class CatalogueImportService {

    private final CatalogueEntryRepository catalogueEntryRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CatalogueImportService(
            CatalogueEntryRepository catalogueEntryRepository, ObjectMapper objectMapper, Validator validator) {
        this.catalogueEntryRepository = catalogueEntryRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * The directory is the source of truth for every (system, kind) it contains: entries
     * of those kinds whose slug no longer has a file are removed.
     */
    @Transactional
    public int importFrom(Path directory) {
        Map<SystemKind, Set<String>> importedSlugs = new HashMap<>();
        try {
            Map<String, JsonNode> translations = translationsBySlug(directory);
            for (Path file : CatalogueEntryFiles.entryFiles(directory)) {
                for (JsonNode entry : CatalogueEntryFiles.entries(file, objectMapper)) {
                    CatalogueEntryImport imported = importEntry(file, entry, translations);
                    importedSlugs.computeIfAbsent(new SystemKind(imported.systemId(), imported.kind()), key -> new HashSet<>())
                            .add(imported.slug());
                }
            }
        } catch (IOException e) {
            throw new CatalogueImportException(directory, e);
        }
        importedSlugs.forEach(this::removeEntriesWithoutFiles);
        return importedSlugs.values().stream().mapToInt(Set::size).sum();
    }

    private record SystemKind(String systemId, CatalogueEntryKind kind) {
    }

    /** Each slug's translations by language tag ({@code {"pt-BR": {...}}}), from the directory's translation files. */
    private Map<String, JsonNode> translationsBySlug(Path directory) throws IOException {
        Map<String, JsonNode> bySlug = new HashMap<>();
        for (Map.Entry<String, Path> file : CatalogueEntryFiles.translationFiles(directory).entrySet()) {
            JsonNode translation = objectMapper.readTree(Files.readString(file.getValue()));
            for (String slug : translation.propertyNames()) {
                ObjectNode languages = (ObjectNode) bySlug.computeIfAbsent(slug, key -> objectMapper.createObjectNode());
                languages.set(file.getKey(), translation.get(slug));
            }
        }
        return bySlug;
    }

    private void removeEntriesWithoutFiles(SystemKind systemKind, Set<String> slugs) {
        List<CatalogueEntry> stale = catalogueEntryRepository.findBySystemIdAndKind(systemKind.systemId(), systemKind.kind())
                .stream()
                .filter(entry -> !slugs.contains(entry.slug()))
                .toList();
        catalogueEntryRepository.deleteAll(stale);
    }

    private CatalogueEntryImport importEntry(Path file, JsonNode entry, Map<String, JsonNode> translations) {
        CatalogueEntryImport imported = validated(file, entry);
        String dataJson = objectMapper.writeValueAsString(imported.data());
        String translationsJson = objectMapper.writeValueAsString(translations.getOrDefault(imported.slug(), objectMapper.createObjectNode()));

        catalogueEntryRepository.findBySystemIdAndKindAndSlug(imported.systemId(), imported.kind(), imported.slug())
                .ifPresentOrElse(
                        existing -> {
                            existing.replaceContent(
                                    imported.name(), imported.sourceBook(), imported.sourceCode(), imported.sourcePage(),
                                    imported.tagsOrEmpty(), imported.description(), dataJson);
                            existing.replaceTranslations(translationsJson);
                            catalogueEntryRepository.save(existing);
                        },
                        () -> {
                            CatalogueEntry created = CatalogueEntry.create(
                                    imported.systemId(), imported.kind(), imported.slug(), imported.name(),
                                    imported.sourceBook(), imported.sourceCode(), imported.sourcePage(), imported.tagsOrEmpty(),
                                    imported.description(), dataJson);
                            created.replaceTranslations(translationsJson);
                            catalogueEntryRepository.save(created);
                        });
        return imported;
    }

    private CatalogueEntryImport validated(Path file, JsonNode entry) {
        CatalogueEntryImport imported = objectMapper.treeToValue(entry, CatalogueEntryImport.class);
        Set<ConstraintViolation<CatalogueEntryImport>> violations = validator.validate(imported);
        if (!violations.isEmpty()) {
            throw new CatalogueImportException(file, violations);
        }
        return imported;
    }
}
