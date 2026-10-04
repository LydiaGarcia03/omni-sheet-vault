package dev.omnisheetvault.api.ruleset;

/**
 * One row of the actions tab's attack list — see VitalsZone. {@code toHit} carries its
 * derivation trace like any other {@link CalculatedValue}; damage is dice notation
 * (dice count/sides plus a flat modifier) rather than a single total, since it is
 * rolled, not displayed as a number. {@code actionType} is a plain string, not a
 * shared enum, same reasoning as {@code FeatureAction.actionType} (adr-0003) — which
 * action-type categories exist is a per-system concept. A catalog weapon or an
 * equipped item's own attack is always the PHB "Action" cost; only a custom action
 * folded into this same map (see the ruleset's own attacks() calculator) can carry a
 * different one, since it is the only source with a real activation type to read.
 * {@code versatileDiceCount}/{@code versatileDiceSides} are the two-handed damage dice of a
 * versatile weapon (same modifier), null together otherwise.
 */
public record AttackRow(
        String name,
        String range,
        CalculatedValue toHit,
        int damageDiceCount,
        int damageDiceSides,
        int damageModifier,
        String damageType,
        String category,
        String notes,
        String actionType,
        Integer versatileDiceCount,
        Integer versatileDiceSides) {

    /** An attack without versatile dice. */
    public AttackRow(
            String name, String range, CalculatedValue toHit, int damageDiceCount, int damageDiceSides, int damageModifier,
            String damageType, String category, String notes, String actionType) {
        this(name, range, toHit, damageDiceCount, damageDiceSides, damageModifier, damageType, category, notes, actionType, null, null);
    }
}
