package dev.omnisheetvault.api.ruleset;

import java.util.List;
import java.util.Optional;

/**
 * Read-only access to one game system's catalogue, by kind name (e.g. {@code "CLASS"})
 * and slug. Implemented by the {@code catalogue} package, so rules code never depends
 * on how or where entries are stored.
 */
public interface CatalogueLookup {

    Optional<CatalogueRecord> find(String kind, String slug);

    List<CatalogueRecord> list(String kind);

    /** Whether descriptions are redacted for display (adr-0005); lookups still return them, for copying onto a sheet. */
    default boolean redactsProse() {
        return false;
    }
}
