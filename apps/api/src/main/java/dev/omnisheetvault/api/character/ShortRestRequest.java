package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * The hit dice the player spends during the rest — zero is valid. {@code hitDiceBySize} (die size → count) chooses
 * dice of several sizes; without it, {@code hitDiceSpent} dice of the largest size with dice left are spent.
 */
record ShortRestRequest(@Min(0) int hitDiceSpent, Map<Integer, @Min(0) Integer> hitDiceBySize) {
}