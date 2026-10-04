package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.ruleset.VitalsZone;

/** The outcome of {@link CharacterSheetService#spendHitDice} — updated vitals plus the roll it made. */
record HitDiceSpendResult(VitalsZone vitals, Roll roll) {
}
