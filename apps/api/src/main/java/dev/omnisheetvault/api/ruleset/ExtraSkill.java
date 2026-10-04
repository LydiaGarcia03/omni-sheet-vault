package dev.omnisheetvault.api.ruleset;

/** One skill bonus in an extra's stat block — see {@code Dnd5eExtraSkill}. Plain pass-through. */
public record ExtraSkill(String name, int bonus) {
}
