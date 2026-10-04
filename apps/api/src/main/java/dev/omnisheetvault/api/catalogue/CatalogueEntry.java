package dev.omnisheetvault.api.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One piece of reference content: a spell, item, feature or creature — see adr-0005.
 * Reference data, not owned by a player, so there is no ownership check anywhere in
 * this package. Re-importing the same {@code (systemId, kind, slug)} updates the row
 * in place rather than duplicating it — see {@link #replaceContent}.
 */
@Entity
@Table(name = "catalogue_entries")
public class CatalogueEntry {

    @Id
    private UUID id;

    @Column(name = "system_id", nullable = false)
    private String systemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CatalogueEntryKind kind;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(name = "source_book")
    private String sourceBook;

    @Column(name = "source_code")
    private String sourceCode;

    @Column(name = "source_page")
    private Integer sourcePage;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private String[] tags;

    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String data;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CatalogueEntry() {
    }

    private CatalogueEntry(
            UUID id, String systemId, CatalogueEntryKind kind, String slug, String name, String sourceBook,
            String sourceCode, Integer sourcePage, List<String> tags, String description, String data, Instant now) {
        this.id = id;
        this.systemId = systemId;
        this.kind = kind;
        this.slug = slug;
        this.name = name;
        this.sourceBook = sourceBook;
        this.sourceCode = sourceCode;
        this.sourcePage = sourcePage;
        this.tags = tags.toArray(new String[0]);
        this.description = description;
        this.data = data;
        this.createdAt = now;
        this.updatedAt = now;
    }

    static CatalogueEntry create(
            String systemId, CatalogueEntryKind kind, String slug, String name, String sourceBook, String sourceCode,
            Integer sourcePage, List<String> tags, String description, String data) {
        return new CatalogueEntry(
                UUID.randomUUID(), systemId, kind, slug, name, sourceBook, sourceCode, sourcePage, tags, description, data,
                Instant.now());
    }

    /** Applied on re-import: everything but the natural key and the id can change. */
    void replaceContent(String name, String sourceBook, String sourceCode, Integer sourcePage, List<String> tags,
            String description, String data) {
        this.name = name;
        this.sourceBook = sourceBook;
        this.sourceCode = sourceCode;
        this.sourcePage = sourcePage;
        this.tags = tags.toArray(new String[0]);
        this.description = description;
        this.data = data;
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String systemId() {
        return systemId;
    }

    public CatalogueEntryKind kind() {
        return kind;
    }

    public String slug() {
        return slug;
    }

    public String name() {
        return name;
    }

    public String sourceBook() {
        return sourceBook;
    }

    public String sourceCode() {
        return sourceCode;
    }

    public Integer sourcePage() {
        return sourcePage;
    }

    public List<String> tags() {
        return List.of(tags);
    }

    public String description() {
        return description;
    }

    public String data() {
        return data;
    }
}
