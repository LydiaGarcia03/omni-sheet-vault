package dev.omnisheetvault.api.ruleset;

/**
 * All derived values for a character's sheet — see architecture.md's granularity
 * guidance: one interface per behavior family, not one per formula.
 */
public interface SheetCalculator {

    String systemId();

    VitalsZone calculateVitals(String sheetJson);
}
