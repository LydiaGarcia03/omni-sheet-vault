package dev.omnisheetvault.api.ruleset.daggerheart;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartDice.Advantage;
import org.junit.jupiter.api.Test;

class DaggerheartDiceTest {

    @Test
    void readsSuccessAndFailureWithHopeOrFearAgainstTheDifficulty() {
        assertThat(DaggerheartDice.duality(9, 4, Advantage.NONE, 0, 2, 12, false).outcome()).isEqualTo("Success with Hope");
        assertThat(DaggerheartDice.duality(4, 9, Advantage.NONE, 0, 2, 12, false).outcome()).isEqualTo("Success with Fear");
        assertThat(DaggerheartDice.duality(3, 5, Advantage.NONE, 0, 0, 12, false).outcome()).isEqualTo("Failure with Fear");
        assertThat(DaggerheartDice.duality(5, 3, Advantage.NONE, 0, 0, 12, false).outcome()).isEqualTo("Failure with Hope");
    }

    @Test
    void matchingDiceAreACriticalSuccessWithHopeWhateverTheTotal() {
        DaggerheartDice.Duality roll = DaggerheartDice.duality(2, 2, Advantage.NONE, 0, 0, 20, false);

        assertThat(roll.critical()).isTrue();
        assertThat(roll.withHope()).isTrue();
        assertThat(roll.success()).isTrue();
        assertThat(roll.outcome()).isEqualTo("Critical Success");
    }

    @Test
    void withoutADifficultyTheRollReadsAsItsTotalWithHopeOrFear() {
        DaggerheartDice.Duality roll = DaggerheartDice.duality(5, 7, Advantage.NONE, 0, 1, 0, false);

        assertThat(roll.success()).isNull();
        assertThat(roll.outcome()).isEqualTo("13 with Fear");
    }

    @Test
    void advantageAddsTheD6AndDisadvantageSubtractsIt() {
        assertThat(DaggerheartDice.duality(6, 5, Advantage.ADVANTAGE, 4, 1, 0, false).total()).isEqualTo(16);
        assertThat(DaggerheartDice.duality(6, 5, Advantage.DISADVANTAGE, 4, 1, 0, false).total()).isEqualTo(8);
    }

    @Test
    void aStoredDualityRollReadsBackTheSame() {
        DaggerheartDice.Duality roll = DaggerheartDice.duality(8, 3, Advantage.DISADVANTAGE, 2, -1, 10, true);

        DaggerheartDice.Duality read = DaggerheartDice.readDuality(roll.expression(), roll.results());

        assertThat(roll.expression()).isEqualTo("duality reaction -1 dis dc 10");
        assertThat(read).isEqualTo(roll);
    }

    @Test
    void extraDiceAddToTheDualityTotalAndReadBack() {
        DaggerheartDice.Duality roll = DaggerheartDice.duality(7, 3, Advantage.ADVANTAGE, 2, 1, 0, false)
                .withExtras(java.util.List.of("1d4"), java.util.List.of(4));

        assertThat(roll.total()).isEqualTo(17);
        assertThat(roll.expression()).isEqualTo("duality action +1 adv 1d4");
        assertThat(DaggerheartDice.readDuality(roll.expression(), roll.results())).isEqualTo(roll);
    }

    @Test
    void aCriticalCustomRollAddsTheDiceMaximum() {
        DaggerheartDice.Custom roll = new DaggerheartDice.Custom(java.util.List.<int[]>of(new int[] {2, 6}), new int[] {1, 4}, 2, true);

        assertThat(roll.total()).isEqualTo(19);
        assertThat(DaggerheartDice.readCustom(roll.expression(), roll.faces()).total()).isEqualTo(19);
    }

    @Test
    void criticalDamageAddsTheDiceMaximum() {
        DaggerheartDice.Damage damage = new DaggerheartDice.Damage(new int[] {3, 5}, 8, 1, true);

        assertThat(damage.total()).isEqualTo(25);
        assertThat(damage.expression()).isEqualTo("damage 2d8+1 crit");
        assertThat(DaggerheartDice.readDamage(damage.expression(), damage.dice()).total()).isEqualTo(25);
    }

    @Test
    void storedRollsReadBackEqualByContent() {
        DaggerheartDice.Custom custom = new DaggerheartDice.Custom(java.util.List.of(new int[] {2, 6}, new int[] {1, 8}), new int[] {1, 4, 7}, -1, false);
        DaggerheartDice.Damage damage = new DaggerheartDice.Damage(new int[] {3, 5}, 8, 0, false);

        DaggerheartDice.Custom readCustom = DaggerheartDice.readCustom(custom.expression(), new int[] {1, 4, 7});
        DaggerheartDice.Damage readDamage = DaggerheartDice.readDamage(damage.expression(), new int[] {3, 5});

        assertThat(readCustom).isEqualTo(custom).hasSameHashCodeAs(custom).hasToString(custom.toString()).isNotEqualTo(damage);
        assertThat(readCustom.toString()).contains("[[2, 6], [1, 8]]", "[1, 4, 7]");
        assertThat(readDamage).isEqualTo(damage).hasSameHashCodeAs(damage).isNotEqualTo(custom);
        assertThat(readDamage.toString()).contains("[3, 5]");
        assertThat(readDamage).isNotEqualTo(new DaggerheartDice.Damage(new int[] {3, 5}, 8, 0, true));
    }

    @Test
    void otherExpressionsAreNotCustomRolls() {
        assertThat(DaggerheartDice.readCustom("dice +3", new int[0])).isNull();
        assertThat(DaggerheartDice.readCustom("damage 1d6 +3", new int[] {2})).isNull();
        assertThat(DaggerheartDice.readCustom("dice 2d6 3", new int[] {2, 3})).isNull();
        assertThat(DaggerheartDice.readCustom("dice 2x6 +3", new int[] {2, 3})).isNull();
        assertThat(DaggerheartDice.readCustom("dice 2d6 +3 crit", new int[] {2, 3}).critical()).isTrue();
    }

    @Test
    void readsTheTierOfARestMove() {
        assertThat(DaggerheartDice.restTier("rest 1d4+1")).isEqualTo(1);
        assertThat(DaggerheartDice.restTier("damage 1d6")).isEqualTo(-1);
    }
}
