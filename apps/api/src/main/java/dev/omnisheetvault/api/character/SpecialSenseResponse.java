package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.SpecialSense;

record SpecialSenseResponse(String type, int rangeFeet, String label) {

    static SpecialSenseResponse from(SpecialSense specialSense) {
        return new SpecialSenseResponse(specialSense.type(), specialSense.rangeFeet(), specialSense.label());
    }
}
