package dev.omnisheetvault.api.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DiceNotationTest {

    @Test
    void findsTheFirstDiceInText() {
        assertThat(DiceNotation.first("1d8")).contains(new DiceNotation.Found(1, 8, 3));
        assertThat(DiceNotation.first("deals 12d6 fire damage")).contains(new DiceNotation.Found(12, 6, 10));
        assertThat(DiceNotation.first("4d4 × 10")).contains(new DiceNotation.Found(4, 4, 3));
    }

    @Test
    void ignoresADWithoutNumbersOnBothSides() {
        assertThat(DiceNotation.first("dragon d6 3d")).isEmpty();
        assertThat(DiceNotation.first("")).isEmpty();
    }

    @Test
    void readsANumberAfterSpaces() {
        assertThat(DiceNotation.numberAt("× 10 gp", 1)).contains(10);
        assertThat(DiceNotation.numberAt("× gp", 1)).isEmpty();
    }
}
