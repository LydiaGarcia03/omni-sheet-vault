package dev.omnisheetvault.api.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Local mirror of an authenticated identity, keyed by the token subject. Holds no
 * credentials — see adr-0002.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String subject;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "seen_tutorials", nullable = false)
    private String[] seenTutorials = new String[0];

    @Column(name = "preferred_locale")
    private String preferredLocale;

    @Column(name = "preferred_theme")
    private String preferredTheme;

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "avatar_key")
    private String avatarKey;

    protected Player() {
    }

    private Player(UUID id, String subject, String displayName, Instant createdAt) {
        this.id = id;
        this.subject = subject;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public static Player create(String subject, String displayName) {
        return new Player(UUID.randomUUID(), subject, displayName, Instant.now());
    }

    public UUID id() {
        return id;
    }

    public String subject() {
        return subject;
    }

    /** The Keycloak username, as first seen; tools look players up by it. */
    public String displayName() {
        return displayName;
    }

    /** The name the player chose to be shown, or null while they keep their username. */
    public String profileName() {
        return profileName;
    }

    /** The name shown in the app: the chosen one, else the username. */
    public String shownName() {
        return profileName != null ? profileName : displayName;
    }

    /** The profile picture's object key in storage, or null without one. */
    public String avatarKey() {
        return avatarKey;
    }

    /** Sets the shown name; a blank one goes back to the username. */
    public void rename(String name) {
        profileName = name == null || name.isBlank() ? null : name.strip();
    }

    public void changeAvatar(String key) {
        avatarKey = key;
    }

    public Instant createdAt() {
        return createdAt;
    }

    /** Ids of the tutorials this player finished or skipped, e.g. {@code builder:dnd-5e}. */
    public List<String> seenTutorials() {
        return List.of(seenTutorials);
    }

    /** The interface language the player chose ({@code en}, {@code pt-BR}), or null while they haven't chosen. */
    public String preferredLocale() {
        return preferredLocale;
    }

    /** The app theme the player chose ({@code light}, {@code dark}), or null while they haven't chosen. */
    public String preferredTheme() {
        return preferredTheme;
    }

    /** Applies the given preferences; a null one keeps its current value. */
    public void choosePreferences(String locale, String theme) {
        if (locale != null) {
            preferredLocale = locale;
        }
        if (theme != null) {
            preferredTheme = theme;
        }
    }
}
