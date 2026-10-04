package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.ruleset.VitalsZone;

/** The outcome of {@link CharacterSheetService#rollDeathSave}: the updated vitals and the logged d20 roll. */
record DeathSaveRollResult(VitalsZone vitals, Roll roll) {
}
