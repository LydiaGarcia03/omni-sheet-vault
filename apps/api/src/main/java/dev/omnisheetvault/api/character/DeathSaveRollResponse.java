package dev.omnisheetvault.api.character;

/** The sheet after a death saving throw, and the d20 roll that decided it. */
record DeathSaveRollResponse(CharacterSheetResponse sheet, HitDiceRollResponse roll) {
}
