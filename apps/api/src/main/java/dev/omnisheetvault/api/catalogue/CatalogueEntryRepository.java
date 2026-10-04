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

    interface SourceBookCount {
        String getSourceBook();

        String getSourceCode();

        long getEntryCount();
    }
}
