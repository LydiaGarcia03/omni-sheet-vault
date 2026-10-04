package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * What kind of rest, if any, restores a limited-use feature's uses — see
 * roadmap.md phase 9 ("resources modelled with an explicit recharge trigger").
 * Replaces the phase-8 free-text {@code rechargeLabel}: a rest mechanic needs
 * something it can match against, not a display string. The frontend maps a
 * value to its own display label, the same treatment as
 * {@link Dnd5eFeatureTraitCategory}.
 */
public enum Dnd5eRechargeTrigger {
    SHORT_OR_LONG_REST,
    LONG_REST
}
