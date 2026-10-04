package dev.omnisheetvault.api.dice;

import dev.omnisheetvault.api.ruleset.RollKind;
import jakarta.validation.constraints.NotNull;

/**
 * What the client asks to have rolled — never a result, never a modifier. {@code key}
 * names the ability or skill and is unused (and ignored) for {@link RollKind#INITIATIVE}.
 * {@code advantage}/{@code disadvantage} are the player's own roll-time choice — see
 * ground-rules.md's Dice section. Both set at once cancel out to a normal roll, and
 * either one is rejected ({@link IneligibleRollModeException}) for a roll that isn't a
 * single die. {@code castAtLevel} is the spell slot level a
 * {@link RollKind#SPELL_DAMAGE}/{@link RollKind#SPELL_HEAL} roll was cast at, for
 * upcasting to scale the dice — see {@code MechanicResolver#resolve}'s own doc
 * comment; null (every other kind, and the row's own quick-roll target) means no
 * scaling.
 */
record RollRequest(@NotNull RollKind kind, String key, boolean advantage, boolean disadvantage, Integer castAtLevel) {
}
