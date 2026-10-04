package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps same-named entries from different sources as separate catalogue entries: when
 * several entries of one kind share a slug, each one's slug gets its 5etools source
 * code appended ({@code aberrant-dragonmark-erlw}, {@code aberrant-dragonmark-efa}),
 * and the shared plain slug is retired.
 */
final class FiveEToolsSlugs {

    private FiveEToolsSlugs() {
    }

    record SourcedEntry(String sourceCode, CatalogueEntryImport entry) {
    }

    record Disambiguated(List<CatalogueEntryImport> entries, Set<String> retiredSlugs) {
    }

    static Disambiguated disambiguate(List<SourcedEntry> sourcedEntries) {
        Map<String, List<SourcedEntry>> bySlug = new LinkedHashMap<>();
        for (SourcedEntry sourced : sourcedEntries) {
            bySlug.computeIfAbsent(sourced.entry().slug(), slug -> new ArrayList<>()).add(sourced);
        }
        List<CatalogueEntryImport> entries = new ArrayList<>();
        Set<String> retiredSlugs = new HashSet<>();
        for (Map.Entry<String, List<SourcedEntry>> group : bySlug.entrySet()) {
            if (group.getValue().size() == 1) {
                entries.add(group.getValue().getFirst().entry());
                continue;
            }
            retiredSlugs.add(group.getKey());
            for (SourcedEntry sourced : group.getValue()) {
                entries.add(sourced.entry().withSlug(group.getKey() + "-" + FiveEToolsNaming.slug(sourced.sourceCode())));
            }
        }
        requireUnique(entries);
        return new Disambiguated(entries, retiredSlugs);
    }

    /** Two entries with the same name from the same source can't be told apart — that is a data problem, not a guess to make. */
    private static void requireUnique(List<CatalogueEntryImport> entries) {
        Set<String> seen = new HashSet<>();
        for (CatalogueEntryImport entry : entries) {
            if (!seen.add(entry.slug())) {
                throw new FiveEToolsIngestException("Slug collision on '" + entry.slug() + "' ("
                        + entry.name() + ", " + entry.sourceBook() + ")");
            }
        }
    }
}
