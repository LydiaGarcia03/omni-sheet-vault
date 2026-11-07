package dev.omnisheetvault.api.catalogue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface CatalogueEntryRepository extends JpaRepository<CatalogueEntry, UUID> {

    Optional<CatalogueEntry> findBySystemIdAndKindAndSlug(String systemId, CatalogueEntryKind kind, String slug);

    List<CatalogueEntry> findBySystemIdAndKind(String systemId, CatalogueEntryKind kind);

    List<CatalogueEntry> findBySystemId(String systemId);

    /** Every source book with its code and how many entries it contributes, largest first. */
    @Query("""
            SELECT e.sourceBook AS sourceBook, MAX(e.sourceCode) AS sourceCode, COUNT(e) AS entryCount FROM CatalogueEntry e
            WHERE e.systemId = :systemId AND e.sourceBook IS NOT NULL
            GROUP BY e.sourceBook ORDER BY COUNT(e) DESC, e.sourceBook""")
    List<SourceBookCount> countBySourceBook(String systemId);

    /** One row per kind with its entry count and latest change: any import that adds, edits or removes an entry changes it. */
    @Query("""
            SELECT e.kind AS kind, COUNT(e) AS entryCount, MAX(e.updatedAt) AS lastUpdated FROM CatalogueEntry e
            WHERE e.systemId = :systemId GROUP BY e.kind""")
    List<KindFingerprint> fingerprintByKind(String systemId);

    /** Every entry that carries at least one translation. */
    @Query(value = "SELECT * FROM catalogue_entries WHERE translations <> '{}'::jsonb", nativeQuery = true)
    List<CatalogueEntry> findTranslated();

    /** The whole catalogue's entry count and latest change, across systems. */
    @Query("SELECT COUNT(e) AS entryCount, MAX(e.updatedAt) AS lastUpdated FROM CatalogueEntry e")
    CatalogueFingerprint fingerprint();

    interface CatalogueFingerprint {
        long getEntryCount();

        java.time.Instant getLastUpdated();
    }

    interface KindFingerprint {
        CatalogueEntryKind getKind();

        long getEntryCount();

        java.time.Instant getLastUpdated();
    }

    interface SourceBookCount {
        String getSourceBook();

        String getSourceCode();

        long getEntryCount();
    }
}
