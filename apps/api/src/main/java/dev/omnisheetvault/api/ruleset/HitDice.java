package dev.omnisheetvault.api.ruleset;

import java.util.List;
import java.util.Optional;

/**
 * A character's hit dice — see VitalsZone. {@code pools} holds one entry per die size
 * (a multiclass character can have d8 and d6 dice), largest first. {@code dieSize},
 * {@code max} and {@code used} summarise them: the starting class's die size and the
 * totals. {@code longRestRecoveryMax} is how many spent dice a long rest can return.
 */
public record HitDice(int dieSize, int max, int used, List<Pool> pools, int longRestRecoveryMax) {

    /** The dice of one size: {@code max} of them, {@code used} already spent, brought by {@code classNames}. */
    public record Pool(int dieSize, int max, int used, List<String> classNames) {

        public Pool {
            classNames = List.copyOf(classNames);
        }

        /** A pool with no class attached. */
        public Pool(int dieSize, int max, int used) {
            this(dieSize, max, used, List.of());
        }

        public int available() {
            return max - used;
        }
    }

    /** A single-size pool; a long rest returns up to half its dice, minimum one. */
    public HitDice(int dieSize, int max, int used) {
        this(dieSize, max, used, List.of(new Pool(dieSize, max, used)), Math.max(1, max / 2));
    }

    public Optional<Pool> pool(int dieSize) {
        return pools.stream().filter(pool -> pool.dieSize() == dieSize).findFirst();
    }

    /** The largest die size with dice left; the summary size when none are left. */
    public int defaultDieSize() {
        return pools.stream().filter(pool -> pool.available() > 0).map(Pool::dieSize).findFirst().orElse(dieSize);
    }
}
