package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * A {@link CatalogueLookup} over one system's ingested content files
 * ({@code content/<system>/<kind>/}, see {@link CatalogueEntryFiles}) — the catalogue as it will be imported,
 * readable without a database. Each kind's directory is read once, on first use.
 */
public final class ContentDirectoryCatalogue implements CatalogueLookup {

    private final Path systemRoot;
    private final ObjectMapper objectMapper;
    private final Map<CatalogueEntryKind, Map<String, CatalogueRecord>> byKind = new HashMap<>();

    public ContentDirectoryCatalogue(Path systemRoot, ObjectMapper objectMapper) {
        this.systemRoot = systemRoot;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CatalogueRecord> find(String kind, String slug) {
        return Optional.ofNullable(entries(kind).get(slug));
    }

    @Override
    public List<CatalogueRecord> list(String kind) {
        return new ArrayList<>(entries(kind).values());
    }

    private Map<String, CatalogueRecord> entries(String kind) {
        return byKind.computeIfAbsent(CatalogueEntryKind.valueOf(kind), this::read);
    }

    private Map<String, CatalogueRecord> read(CatalogueEntryKind kind) {
        Map<String, CatalogueRecord> entries = new LinkedHashMap<>();
        Path directory = CatalogueContentLayout.directory(systemRoot, kind);
        if (!Files.isDirectory(directory)) {
            return entries;
        }
        try {
            Map<String, ObjectNode> translations = translationsBySlug(directory);
            for (Path file : CatalogueEntryFiles.entryFiles(directory)) {
                for (JsonNode node : CatalogueEntryFiles.entries(file, objectMapper)) {
                    CatalogueEntryImport entry = objectMapper.treeToValue(node, CatalogueEntryImport.class);
                    entries.put(entry.slug(), new CatalogueRecord(entry.kind().name(), entry.slug(), entry.name(),
                            entry.sourceBook(), entry.sourcePage(), entry.description(), entry.data(),
                            PlaytestSources.isPlaytest(entry.sourceCode()), PartneredSources.partnerOf(entry.sourceCode()),
                            translations.get(entry.slug())));
                }
            }
        } catch (IOException e) {
            throw new CatalogueImportException(directory, e);
        }
        return entries;
    }

    /** Each slug's translations by language tag, from the directory's translation files. */
    private Map<String, ObjectNode> translationsBySlug(Path directory) throws IOException {
        Map<String, ObjectNode> bySlug = new HashMap<>();
        for (Map.Entry<String, Path> file : CatalogueEntryFiles.translationFiles(directory).entrySet()) {
            JsonNode translation = objectMapper.readTree(Files.readString(file.getValue()));
            for (String slug : translation.propertyNames()) {
                bySlug.computeIfAbsent(slug, key -> objectMapper.createObjectNode()).set(file.getKey(), translation.get(slug));
            }
        }
        return bySlug;
    }
}
