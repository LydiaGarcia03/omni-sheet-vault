package dev.omnisheetvault.api.dice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KeptDiceTest {

    @Test
    void writesKeepHighestOnlyWhenSomeDiceAreDropped() {
        assertThat(KeptDice.expression(4, 6, 3)).isEqualTo("4d6kh3");
        assertThat(KeptDice.expression(4, 6, 4)).isEqualTo("4d6");
        assertThat(KeptDice.expression(1, 8, null)).isEqualTo("1d8");
    }

    @Test
    void dropsTheLowestDiceAndTheEarliestOfATie() {
        assertThat(KeptDice.dropped(new int[] {5, 2, 6, 3}, 3)).containsExactly(1);
        assertThat(KeptDice.dropped(new int[] {4, 1, 1, 6}, 3)).containsExactly(1);
        assertThat(KeptDice.dropped(new int[] {4, 1, 1, 6}, 2)).containsExactly(1, 2);
        assertThat(KeptDice.keptTotal(new int[] {5, 2, 6, 3}, 3)).isEqualTo(14);
    }

    @Test
    void readsTheDroppedDiceBackFromAStoredExpression() {
        assertThat(KeptDice.droppedFor("4d6kh3", new int[] {5, 2, 6, 3})).containsExactly(1);
        assertThat(KeptDice.droppedFor("1d20+4", new int[] {12})).isEmpty();
        assertThat(KeptDice.droppedFor("2d20 + 3d6", new int[] {14, 9, 1, 4, 6})).isEmpty();
    }
}
