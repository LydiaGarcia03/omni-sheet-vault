package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;
import java.util.Map;

/** The spent hit dice the player chooses to recover (die size → count); null lets the system choose. */
record LongRestRequest(Map<Integer, @Min(0) Integer> hitDiceRecovered) {
}