package dev.omnisheetvault.api.ruleset.vtm5;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Vtm5DiceTest {

    @Test
    void countsSixesAndAboveAsSuccesses() {
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(new int[] {6, 7, 3, 9, 2}, 0);

        assertThat(outcome.successes()).isEqualTo(3);
        assertThat(outcome.critical()).isFalse();
        assertThat(outcome.tag()).isEmpty();
    }

    @Test
    void aPairOfTensIsACriticalWorthFourSuccesses() {
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(new int[] {10, 10, 1, 1}, 0);

        assertThat(outcome.successes()).isEqualTo(4);
        assertThat(outcome.critical()).isTrue();
        assertThat(outcome.messy()).isFalse();
        assertThat(outcome.tag()).isEqualTo("Critical");
    }

    @Test
    void aCriticalWithATenOnAHungerDieIsMessy() {
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(new int[] {10, 4, 10}, 1);

        assertThat(outcome.messy()).isTrue();
        assertThat(outcome.tag()).isEqualTo("Messy critical");
    }

    @Test
    void noSuccessesWithAOneOnAHungerDieIsBestial() {
        Vtm5Dice.Outcome bestial = Vtm5Dice.read(new int[] {3, 2, 1}, 1);
        Vtm5Dice.Outcome plainFailure = Vtm5Dice.read(new int[] {1, 2, 3}, 1);

        assertThat(bestial.bestial()).isTrue();
        assertThat(bestial.tag()).isEqualTo("Bestial failure");
        assertThat(plainFailure.bestial()).isFalse();
    }

    @Test
    void storesAndReadsBackTheHungerDiceInTheExpression() {
        assertThat(Vtm5Dice.expression(7, 2)).isEqualTo("7d10 hunger 2");
        assertThat(Vtm5Dice.hungerOf("7d10 hunger 2")).isEqualTo(2);
        assertThat(Vtm5Dice.hungerOf("1d10 rouse")).isEqualTo(-1);
        assertThat(Vtm5Dice.hungerOf(null)).isEqualTo(-1);
    }

    @Test
    void aDifficultyDecidesWinOrFailureAndTheMargin() {
        Vtm5Dice.Outcome win = Vtm5Dice.read(new int[] {6, 7, 8, 2}, 0, 2);
        Vtm5Dice.Outcome atACost = Vtm5Dice.read(new int[] {6, 2, 3}, 0, 3);
        Vtm5Dice.Outcome bestial = Vtm5Dice.read(new int[] {6, 2, 1}, 1, 3);
        Vtm5Dice.Outcome failedCritical = Vtm5Dice.read(new int[] {10, 10, 2, 2, 2, 10}, 1, 6);

        assertThat(win.win()).isTrue();
        assertThat(win.margin()).isEqualTo(1);
        assertThat(win.tag()).isEqualTo("Success");
        assertThat(atACost.win()).isFalse();
        assertThat(atACost.tag()).isEqualTo("Failure · win at a cost");
        assertThat(bestial.tag()).isEqualTo("Bestial failure");
        assertThat(failedCritical.messy()).isFalse();
        assertThat(Vtm5Dice.read(new int[] {2}, 0).tag()).isEqualTo("Failure");
    }

    @Test
    void storesTheDifficultyInTheExpression() {
        assertThat(Vtm5Dice.expression(5, 1, 3)).isEqualTo("5d10 hunger 1 difficulty 3");
        assertThat(Vtm5Dice.hungerOf("5d10 hunger 1 difficulty 3")).isEqualTo(1);
        assertThat(Vtm5Dice.difficultyOf("5d10 hunger 1 difficulty 3")).isEqualTo(3);
        assertThat(Vtm5Dice.difficultyOf("5d10 hunger 1")).isZero();
    }
}