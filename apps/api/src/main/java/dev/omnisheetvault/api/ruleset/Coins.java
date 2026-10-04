package dev.omnisheetvault.api.ruleset;

/** A character's carried currency — see VitalsZone. Plain stored totals, not derived. */
public record Coins(int copper, int silver, int electrum, int gold, int platinum) {
}
