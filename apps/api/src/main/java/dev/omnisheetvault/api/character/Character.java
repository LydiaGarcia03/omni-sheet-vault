package dev.omnisheetvault.api.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A player's character: name, game system and its system-specific sheet payload. A
 * {@link CharacterStatus#DRAFT DRAFT} has only a creation draft (its build so far) and
 * an empty sheet until the creation flow finishes it.
 */
@Entity
@Table(name = "characters")
public class Character {

    private static final String EMPTY_SHEET = "{}";
    private static final int NO_RULESET_YET_SCHEMA_VERSION = 0;

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "system_id", nullable = false)
    private String systemId;

    @Column(nullable = false)
    private String name;

    /** Written only by {@code CharacterRepository.updatePortraitKey}, never by a whole-entity save. */
    @Column(name = "portrait_key", updatable = false)
    private String portraitKey;

    private String backstory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String sheet;

    @Column(name = "sheet_schema_version", nullable = false)
    private int sheetSchemaVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "creation_draft", columnDefinition = "jsonb")
    private String creationDraft;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "level_up_draft", columnDefinition = "jsonb")
    private String levelUpDraft;

    protected Character() {
    }

    /** The level up in progress (the class leveled and the build with the new level), or null. */
    public String levelUpDraft() {
        return levelUpDraft;
    }

    void saveLevelUpDraft(String levelUpDraftJson) {
        this.levelUpDraft = levelUpDraftJson;
        this.updatedAt = Instant.now();
    }

    /** Applies a finished level up: the re-materialized sheet, with the draft cleared. */
    void finishLevelUp(String sheetJson, int schemaVersion) {
        replaceSheet(sheetJson, schemaVersion);
        this.levelUpDraft = null;
    }

    private Character(UUID id, UUID playerId, String systemId, String name, Instant now) {
        this.id = id;
        this.playerId = playerId;
        this.systemId = systemId;
        this.name = name;
        this.sheet = EMPTY_SHEET;
        this.sheetSchemaVersion = NO_RULESET_YET_SCHEMA_VERSION;
        this.status = CharacterStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Character create(UUID playerId, String name, String systemId) {
        return new Character(UUID.randomUUID(), playerId, systemId, name, Instant.now());
    }

    /** A character still in the creation flow: only its name and its (possibly incomplete) build draft. */
    public static Character createDraft(UUID playerId, String name, String systemId, String draftJson) {
        Character character = create(playerId, name, systemId);
        character.status = CharacterStatus.DRAFT;
        character.creationDraft = draftJson;
        return character;
    }

    void saveDraft(String name, String draftJson) {
        this.name = name;
        this.creationDraft = draftJson;
        this.updatedAt = Instant.now();
    }

    /** Turns the draft into a playable character with its materialized sheet. */
    void finish(String sheetJson, int schemaVersion) {
        replaceSheet(sheetJson, schemaVersion);
        this.status = CharacterStatus.ACTIVE;
        this.creationDraft = null;
    }

    void rename(String newName) {
        this.name = newName;
        this.updatedAt = Instant.now();
    }

    public boolean isDraft() {
        return status == CharacterStatus.DRAFT;
    }

    public CharacterStatus status() {
        return status;
    }

    public String creationDraft() {
        return creationDraft;
    }

    /** Mirrors a stored portrait change on this instance; the database row is written by the repository. */
    void changePortrait(String portraitKey) {
        this.portraitKey = portraitKey;
    }

    public String portraitKey() {
        return portraitKey;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    void replaceSheet(String sheetJson, int schemaVersion) {
        this.sheet = sheetJson;
        this.sheetSchemaVersion = schemaVersion;
        this.updatedAt = Instant.now();
    }

    /** For mutations, which never change the schema — only creation and migration do. */
    void replaceSheet(String sheetJson) {
        this.sheet = sheetJson;
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID playerId() {
        return playerId;
    }

    public String systemId() {
        return systemId;
    }

    public String name() {
        return name;
    }

    public String sheet() {
        return sheet;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
