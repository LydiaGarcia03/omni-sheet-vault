package dev.omnisheetvault.api.catalogue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads every {@code *.json} file in a directory into one catalogue entry each,
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
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : files) {
                CatalogueEntryImport imported = importFile(file);
                importedSlugs.computeIfAbsent(new SystemKind(imported.systemId(), imported.kind()), key -> new HashSet<>())
                        .add(imported.slug());
            }
        } catch (IOException e) {
            throw new CatalogueImportException(directory, e);
        }
        importedSlugs.forEach(this::removeEntriesWithoutFiles);
        return importedSlugs.values().stream().mapToInt(Set::size).sum();
    }

    private record SystemKind(String systemId, CatalogueEntryKind kind) {
    }

    private void removeEntriesWithoutFiles(SystemKind systemKind, Set<String> slugs) {
        List<CatalogueEntry> stale = catalogueEntryRepository.findBySystemIdAndKind(systemKind.systemId(), systemKind.kind())
                .stream()
                .filter(entry -> !slugs.contains(entry.slug()))
                .toList();
        catalogueEntryRepository.deleteAll(stale);
    }

    private CatalogueEntryImport importFile(Path file) {
        CatalogueEntryImport imported = readAndValidate(file);
        String dataJson = objectMapper.writeValueAsString(imported.data());

        catalogueEntryRepository.findBySystemIdAndKindAndSlug(imported.systemId(), imported.kind(), imported.slug())
                .ifPresentOrElse(
                        existing -> {
                            existing.replaceContent(
                                    imported.name(), imported.sourceBook(), imported.sourceCode(), imported.sourcePage(),
                                    imported.tagsOrEmpty(), imported.description(), dataJson);
                            catalogueEntryRepository.save(existing);
                        },
                        () -> catalogueEntryRepository.save(CatalogueEntry.create(
                                imported.systemId(), imported.kind(), imported.slug(), imported.name(),
                                imported.sourceBook(), imported.sourceCode(), imported.sourcePage(), imported.tagsOrEmpty(),
                                imported.description(), dataJson)));
        return imported;
    }

    private CatalogueEntryImport readAndValidate(Path file) {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            throw new CatalogueImportException(file, e);
        }

        CatalogueEntryImport imported = objectMapper.readValue(content, CatalogueEntryImport.class);
        Set<ConstraintViolation<CatalogueEntryImport>> violations = validator.validate(imported);
        if (!violations.isEmpty()) {
            throw new CatalogueImportException(file, violations);
        }
        return imported;
    }
}
