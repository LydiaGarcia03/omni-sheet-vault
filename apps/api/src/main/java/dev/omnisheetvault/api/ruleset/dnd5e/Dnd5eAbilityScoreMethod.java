package dev.omnisheetvault.api.ruleset.dnd5e;

/** How a build's base ability scores were generated — D&D Beyond's three methods. */
public enum Dnd5eAbilityScoreMethod {
    STANDARD_ARRAY("Standard array"),
    POINT_BUY("Point buy"),
    MANUAL("Manual / rolled");

    private final String label;

    Dnd5eAbilityScoreMethod(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
