package dev.omnisheetvault.api.ruleset;

/**
 * How a spell is cast without a slot: {@code mode} "AT_WILL" or "LIMITED"; for a limited
 * spell its uses and {@code recharge} ("SHORT_OR_LONG_REST", "LONG_REST"). {@code castLevel}
 * fixes the level it is cast at (null: its own); {@code selfOnly} limits it to the caster.
 */
public record SpellUsage(String mode, int maxUses, int usedUses, String recharge, Integer castLevel, boolean selfOnly) {
}
