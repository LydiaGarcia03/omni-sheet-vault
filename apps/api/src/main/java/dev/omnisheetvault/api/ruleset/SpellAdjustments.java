package dev.omnisheetvault.api.ruleset;

/**
 * What the player set over a spell's calculated values: an attack bonus override (null: none) or a bonus added to it,
 * a damage bonus, a save DC override (null: none) or a bonus added to it, and whether the spell shows among the attacks.
 */
public record SpellAdjustments(
        Integer attackOverride, int attackBonus, int damageBonus, Integer saveDcOverride, int saveDcBonus, boolean displayAsAttack) {

    public static final SpellAdjustments NONE = new SpellAdjustments(null, 0, 0, null, 0, false);

    /** The attack bonus once adjusted: the override, or {@code calculated} plus the bonus. */
    public int attack(int calculated) {
        return attackOverride != null ? attackOverride : calculated + attackBonus;
    }

    /** The save DC once adjusted: the override, or {@code calculated} plus the bonus. */
    public int saveDc(int calculated) {
        return saveDcOverride != null ? saveDcOverride : calculated + saveDcBonus;
    }
}
