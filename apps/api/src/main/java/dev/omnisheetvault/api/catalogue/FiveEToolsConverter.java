package dev.omnisheetvault.api.catalogue;

import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * One implementation per 5etools content kind — systems/dnd-5e/features/5etools-ingestion.md's own
 * "Tool shape" design, mirroring the Open/Closed seam {@code ruleset}'s
 * {@code GameSystem}/{@code SheetCalculator} registry already uses: a new content kind
 * (classes, species, items, feats, creatures) means a new implementation, never an edit
 * to {@link SpellConverter}. Each implementation owns its own kind's file layout (which
 * files under the data root hold it, and which array inside each one) — {@link Ingest5eToolsMain}
 * stays kind-agnostic: load raw entries → filter by source → convert → write.
 */
interface FiveEToolsConverter {

    CatalogueEntryKind kind();

    /** Every raw entry of this kind found across whatever files it's split into — not yet source-filtered. */
    List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource);

    /**
     * {@code rawEntry} is one element from {@link #loadRawEntries}, already source-filtered by the caller.
     * The returned {@code sourceBook} is the raw 5etools source code; the runner resolves it to the book's full name.
     */
    CatalogueEntryImport convert(JsonNode rawEntry);
}
