package dev.omnisheetvault.api.ruleset;

import java.util.List;
import java.util.Map;

/**
 * What a build — possibly still a draft — adds up to so far. {@code abilityScores}
 * lists every score with its contributions (base, species, feats) and is always
 * present; {@code sheetJson} is a sheet materialized with pending choices left out,
 * or {@code null} while the build lacks what a sheet needs (a class, the base scores).
 * {@code progressions} holds one table per chosen class; {@code selections} describes
 * each chosen option a step shows in detail, by step key (e.g. "species", "background");
 * {@code startingEquipment} is what the character starts carrying.
 */
public record CreationPreview(
        Map<String, CalculatedValue> abilityScores, String sheetJson, List<ProgressionTable> progressions,
        Map<String, SelectionDetail> selections, StartingEquipmentPreview startingEquipment) {
}
