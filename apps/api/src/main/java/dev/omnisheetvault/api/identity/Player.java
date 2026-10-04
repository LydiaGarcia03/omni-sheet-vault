package dev.omnisheetvault.api.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

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

    public String displayName() {
        return displayName;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
