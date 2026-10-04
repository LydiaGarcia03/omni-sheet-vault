package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;

/** The outcome of {@link CharacterSheetService#applyShortRest} — updated vitals, plus one hit-dice roll per die size spent (none if no dice were spent). */
record ShortRestResult(VitalsZone vitals, List<Roll> rolls) {
}