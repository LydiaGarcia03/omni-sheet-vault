package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A {@link CatalogueLookup} over one system's imported catalogue, read from the shared {@link CatalogueRecordCache}. */
final class DatabaseCatalogueLookup implements CatalogueLookup {

    private final CatalogueRecordCache.Snapshot snapshot;
    private final boolean redactsProse;

    DatabaseCatalogueLookup(CatalogueRecordCache.Snapshot snapshot, boolean redactsProse) {
        this.snapshot = snapshot;
        this.redactsProse = redactsProse;
    }

    @Override
    public boolean redactsProse() {
        return redactsProse;
    }

    @Override
    public Optional<CatalogueRecord> find(String kind, String slug) {
        return Optional.ofNullable(snapshot.entries(CatalogueEntryKind.valueOf(kind)).get(slug));
    }

    @Override
    public List<CatalogueRecord> list(String kind) {
        return new ArrayList<>(snapshot.entries(CatalogueEntryKind.valueOf(kind)).values());
    }
}
