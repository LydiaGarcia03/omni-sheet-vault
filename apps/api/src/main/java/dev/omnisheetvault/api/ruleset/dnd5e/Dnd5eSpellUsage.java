package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * How a spell granted by a feature, species, feat or invocation is cast without a slot.
 * {@code maxUses}, {@code usedUses} and {@code recharge} only matter for {@code LIMITED}.
 * Spells sharing a {@code pool} spend the same uses (5etools "1/day" over several spells);
 * a null pool means the spell has its own. {@code castLevel} fixes the level it is cast at
 * (null: the spell's own level). {@code selfOnly} limits a lasting effect to the caster.
 */
public record Dnd5eSpellUsage(
        @NotNull Mode mode,
        @Min(0) int maxUses,
        @Min(0) int usedUses,
        Dnd5eRechargeTrigger recharge,
        String pool,
        Integer castLevel,
        boolean selfOnly) {

    public enum Mode { AT_WILL, LIMITED }

    public static Dnd5eSpellUsage atWill(Integer castLevel, boolean selfOnly) {
        return new Dnd5eSpellUsage(Mode.AT_WILL, 0, 0, null, null, castLevel, selfOnly);
    }

    public static Dnd5eSpellUsage limited(int maxUses, Dnd5eRechargeTrigger recharge, String pool, Integer castLevel, boolean selfOnly) {
        return new Dnd5eSpellUsage(Mode.LIMITED, maxUses, 0, recharge, pool, castLevel, selfOnly);
    }

    public int remainingUses() {
        return Math.max(0, maxUses - usedUses);
    }

    public Dnd5eSpellUsage withUsedUses(int used) {
        return new Dnd5eSpellUsage(mode, maxUses, Math.max(0, Math.min(maxUses, used)), recharge, pool, castLevel, selfOnly);
    }

    /** Whether spending a use of {@code other} also spends one of this. */
    public boolean sharesUsesWith(String spellKey, String otherKey, Dnd5eSpellUsage other) {
        if (mode != Mode.LIMITED || other == null || other.mode != Mode.LIMITED) {
            return false;
        }
        return pool != null ? pool.equals(other.pool) : spellKey.equals(otherKey);
    }
}
