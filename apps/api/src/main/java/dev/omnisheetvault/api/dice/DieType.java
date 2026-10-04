package dev.omnisheetvault.api.dice;

/** The seven standard polyhedral dice a manual roll can be built from. */
enum DieType {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    D100(100);

    private final int sides;

    DieType(int sides) {
        this.sides = sides;
    }

    int sides() {
        return sides;
    }
}
