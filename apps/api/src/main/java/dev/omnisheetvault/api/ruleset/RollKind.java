package dev.omnisheetvault.api.ruleset;

/** The shape of roll a client can ask for — see MechanicResolver. */
public enum RollKind {
    ABILITY_CHECK,
    SAVING_THROW,
    SKILL_CHECK,
    INITIATIVE,
    ATTACK_HIT,
    ATTACK_DAMAGE,
    /** The attack's two-handed damage; rejected if the attack has no versatile dice. */
    ATTACK_DAMAGE_VERSATILE,
    /** One hit die's healing shape (die size + the ability modifier that governs it); the count spent is player-chosen, applied by the caller. */
    HIT_DICE,
    /** Rejected (UnresolvableRollException) if the spell's own {@code attackRoll} flag is false. */
    SPELL_ATTACK,
    /** Rejected if the spell carries no damage dice. */
    SPELL_DAMAGE,
    /** Rejected if the spell carries no healing dice. Unlike {@link #SPELL_DAMAGE}, adds the spellcasting modifier — PHB: a healing spell like Cure Wounds restores its dice plus the caster's spellcasting ability modifier. */
    SPELL_HEAL
}
