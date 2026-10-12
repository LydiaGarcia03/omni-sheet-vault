package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Each system's catalogue read and parsed once and shared by every request, instead of once per request. A cheap
 * per-kind fingerprint (count and latest change) is checked on every lookup, so an import run by another process
 * shows up on the next request. Callers must not mutate the records' JSON; the rules engine copies before it edits.
 */
@Component
class CatalogueRecordCache {

    private final CatalogueEntryRepository repository;
    private final ObjectMapper objectMapper;
    private final Map<String, Snapshot> bySystem = new ConcurrentHashMap<>();

    CatalogueRecordCache(CatalogueEntryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /** The system's current snapshot, replaced when the catalogue changed since it was taken. */
    Snapshot snapshot(String systemId) {
        Map<CatalogueEntryKind, Fingerprint> current = repository.fingerprintByKind(systemId).stream()
                .collect(Collectors.toMap(CatalogueEntryRepository.KindFingerprint::getKind,
                        row -> new Fingerprint(row.getEntryCount(), row.getLastUpdated())));
        return bySystem.compute(systemId, (id, cached) -> cached != null && cached.fingerprints.equals(current)
                ? cached
                : new Snapshot(id, current));
    }

    private record Fingerprint(long count, Instant lastUpdated) {
    }

    /** One version of a system's catalogue; each kind is read on first use. */
    final class Snapshot {

        private final String systemId;
        private final Map<CatalogueEntryKind, Fingerprint> fingerprints;
        private final Map<CatalogueEntryKind, Map<String, CatalogueRecord>> byKind = new ConcurrentHashMap<>();

        private Snapshot(String systemId, Map<CatalogueEntryKind, Fingerprint> fingerprints) {
            this.systemId = systemId;
            this.fingerprints = fingerprints;
        }

        Map<String, CatalogueRecord> entries(CatalogueEntryKind kind) {
            return byKind.computeIfAbsent(kind, this::read);
        }

        private Map<String, CatalogueRecord> read(CatalogueEntryKind kind) {
            Map<String, CatalogueRecord> entries = new LinkedHashMap<>();
            for (CatalogueEntry entry : repository.findBySystemIdAndKind(systemId, kind)) {
                entries.put(entry.slug(), new CatalogueRecord(kind.name(), entry.slug(), entry.name(), entry.sourceBook(),
                        entry.sourcePage(), entry.description(), objectMapper.readTree(entry.data()),
                        PlaytestSources.isPlaytest(entry.sourceCode()), PartneredSources.partnerOf(entry.sourceCode())));
            }
            return Collections.unmodifiableMap(entries);
        }
    }
}
