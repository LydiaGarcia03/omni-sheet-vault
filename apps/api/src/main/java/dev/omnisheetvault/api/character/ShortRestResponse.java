package dev.omnisheetvault.api.character;

import java.util.List;

/**
 * {@code rolls} holds one hit-dice roll per die size spent, empty when none were spent. {@code roll} is the
 * last of them (null when none), kept for clients that read a single roll.
 */
record ShortRestResponse(CharacterSheetResponse sheet, HitDiceRollResponse roll, List<HitDiceRollResponse> rolls) {

    static ShortRestResponse from(ShortRestResult result) {
        List<HitDiceRollResponse> rolls = result.rolls().stream().map(HitDiceRollResponse::from).toList();
        return new ShortRestResponse(CharacterSheetResponse.from(result.vitals()), rolls.isEmpty() ? null : rolls.getLast(), rolls);
    }
}