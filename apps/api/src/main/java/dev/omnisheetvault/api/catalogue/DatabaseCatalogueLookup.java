package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.ObjectMapper;

/** A {@link CatalogueLookup} over one system's imported catalogue rows; each kind is read once, on first use. */
final class DatabaseCatalogueLookup implements CatalogueLookup {

    private final CatalogueEntryRepository repository;
    private final String systemId;
    private final ObjectMapper objectMapper;
    private final boolean redactsProse;
    private final Map<CatalogueEntryKind, Map<String, CatalogueRecord>> byKind = new HashMap<>();

    DatabaseCatalogueLookup(CatalogueEntryRepository repository, String systemId, ObjectMapper objectMapper, boolean redactsProse) {
        this.repository = repository;
        this.systemId = systemId;
        this.objectMapper = objectMapper;
        this.redactsProse = redactsProse;
    }

    @Override
    public boolean redactsProse() {
        return redactsProse;
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
        for (CatalogueEntry entry : repository.findBySystemIdAndKind(systemId, kind)) {
            entries.put(entry.slug(), new CatalogueRecord(kind.name(), entry.slug(), entry.name(), entry.sourceBook(),
                    entry.sourcePage(), entry.description(), objectMapper.readTree(entry.data()),
                    PlaytestSources.isPlaytest(entry.sourceCode()), PartneredSources.partnerOf(entry.sourceCode())));
        }
        return entries;
    }
}
