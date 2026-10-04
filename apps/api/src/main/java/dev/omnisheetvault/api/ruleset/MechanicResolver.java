package dev.omnisheetvault.api.ruleset;

/**
 * Turns an action into a dice expression — see architecture.md's seams table. Phase 5
 * only asks it for the vitals zone's own roll targets (ability checks, saving throws,
 * skill checks, initiative); casting a spell into damage dice is phase 9.
 */
public interface MechanicResolver {

    String systemId();

    /**
     * @param castAtLevel the spell slot level the roll's spell was cast at, for
     *        {@link RollKind#SPELL_DAMAGE}/{@link RollKind#SPELL_HEAL} to scale by —
     *        null (the row's own quick-roll target never sends one) means "the
     *        spell's own base level, no scaling." Every other {@link RollKind}
     *        ignores it.
     * @throws UnresolvableRollException if {@code kind}/{@code key} names nothing on
     *         this character — an unknown skill, for instance
     */
    ResolvedRoll resolve(VitalsZone vitals, RollKind kind, String key, Integer castAtLevel);
}
