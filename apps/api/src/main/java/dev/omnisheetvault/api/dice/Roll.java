package dev.omnisheetvault.api.dice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An immutable record of one dice resolution — see ground-rules.md's Dice section.
 * Append-only: there is no setter and no update method, only {@link #create}.
 */
@Entity
@Table(name = "rolls")
public class Roll {

    @Id
    private UUID id;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(nullable = false)
    private String expression;

    @Column(nullable = false)
    private String context;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "integer[]")
    private int[] results;

    @Column(nullable = false)
    private int total;

    @Column(name = "rolled_at", nullable = false)
    private Instant rolledAt;

    protected Roll() {
    }

    private Roll(UUID id, UUID characterId, String expression, String context, int[] results, int total, Instant rolledAt) {
        this.id = id;
        this.characterId = characterId;
        this.expression = expression;
        this.context = context;
        this.results = results;
        this.total = total;
        this.rolledAt = rolledAt;
    }

    public static Roll create(UUID characterId, String expression, String context, int[] results, int total) {
        return new Roll(UUID.randomUUID(), characterId, expression, context, results, total, Instant.now());
    }

    public UUID id() {
        return id;
    }

    public UUID characterId() {
        return characterId;
    }

    public String expression() {
        return expression;
    }

    public String context() {
        return context;
    }

    public int[] results() {
        return results;
    }

    public int total() {
        return total;
    }

    public Instant rolledAt() {
        return rolledAt;
    }
}
