package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.InvalidHitDiceRecoveryException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hit dice per die size (PHB: each class brings its own). Spent dice are tracked per class
 * in {@link Dnd5eClassLevel#hitDiceUsed}; classes sharing a die size form one pool.
 * {@link Dnd5eSheet#hitDiceUsed} stays the total. A sheet with no class levels (one not
 * built from a build) has a single pool of its own die size.
 */
final class Dnd5eHitDicePools {

    private Dnd5eHitDicePools() {
    }

    static HitDice of(Dnd5eSheet stored) {
        Dnd5eSheet sheet = normalized(stored);
        List<Dnd5eClassLevel> classes = sheet.classLevelsOrEmpty();
        if (classes.isEmpty()) {
            return new HitDice(sheet.hitDieSize(), sheet.level(), sheet.hitDiceUsed(),
                    List.of(new HitDice.Pool(sheet.hitDieSize(), sheet.level(), sheet.hitDiceUsed())), recoveryBudget(sheet));
        }
        Map<Integer, List<Dnd5eClassLevel>> bySize = new LinkedHashMap<>();
        for (Dnd5eClassLevel classLevel : classes) {
            bySize.computeIfAbsent(classLevel.hitDieSize(), size -> new ArrayList<>()).add(classLevel);
        }
        List<HitDice.Pool> pools = bySize.entrySet().stream()
                .map(entry -> new HitDice.Pool(entry.getKey(),
                        entry.getValue().stream().mapToInt(Dnd5eClassLevel::level).sum(),
                        entry.getValue().stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum(),
                        entry.getValue().stream().map(Dnd5eClassLevel::className).toList()))
                .sorted(Comparator.comparingInt(HitDice.Pool::dieSize).reversed())
                .toList();
        int max = pools.stream().mapToInt(HitDice.Pool::max).sum();
        int used = pools.stream().mapToInt(HitDice.Pool::used).sum();
        return new HitDice(classes.getFirst().hitDieSize(), max, used, pools, recoveryBudget(sheet));
    }

    /** Spends {@code count} dice of one size, capped at what that size has left. */
    static Dnd5eSheet spend(Dnd5eSheet stored, int dieSize, int count) {
        Dnd5eSheet sheet = normalized(stored);
        if (sheet.classLevelsOrEmpty().isEmpty()) {
            return sheet.withHitDiceUsed(Math.min(sheet.level(), sheet.hitDiceUsed() + count));
        }
        return distribute(sheet, dieSize, count);
    }

    /** The long rest's default: half the character's hit dice (minimum 1), largest dice first. */
    static Map<Integer, Integer> defaultRecovery(Dnd5eSheet sheet) {
        int budget = recoveryBudget(sheet);
        Map<Integer, Integer> recovered = new LinkedHashMap<>();
        for (HitDice.Pool pool : of(sheet).pools()) {
            int amount = Math.min(budget, pool.used());
            if (amount > 0) {
                recovered.put(pool.dieSize(), amount);
                budget -= amount;
            }
        }
        return recovered;
    }

    /**
     * Returns spent dice by size.
     *
     * @throws InvalidHitDiceRecoveryException if the choice exceeds half the character's dice
     *         or returns more of a size than were spent
     */
    static Dnd5eSheet recover(Dnd5eSheet stored, Map<Integer, Integer> recovered) {
        Dnd5eSheet sheet = normalized(stored);
        HitDice hitDice = of(sheet);
        int total = recovered.values().stream().mapToInt(Integer::intValue).sum();
        if (total > recoveryBudget(sheet)) {
            throw new InvalidHitDiceRecoveryException(
                    "A long rest recovers at most " + recoveryBudget(sheet) + " hit dice, not " + total);
        }
        Dnd5eSheet result = sheet;
        for (Map.Entry<Integer, Integer> entry : recovered.entrySet()) {
            int spent = hitDice.pool(entry.getKey()).map(HitDice.Pool::used).orElse(0);
            if (entry.getValue() < 0 || entry.getValue() > spent) {
                throw new InvalidHitDiceRecoveryException(
                        "Cannot recover " + entry.getValue() + " d" + entry.getKey() + ": " + spent + " spent");
            }
            result = result.classLevelsOrEmpty().isEmpty()
                    ? result.withHitDiceUsed(result.hitDiceUsed() - entry.getValue())
                    : distribute(result, entry.getKey(), -entry.getValue());
        }
        return result;
    }

    /**
     * A sheet whose classes predate per-class tracking has its total in {@code hitDiceUsed} only;
     * that total is read as spent dice of the largest sizes first.
     */
    static Dnd5eSheet normalized(Dnd5eSheet sheet) {
        List<Dnd5eClassLevel> classes = sheet.classLevelsOrEmpty();
        int tracked = classes.stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum();
        if (classes.isEmpty() || tracked >= sheet.hitDiceUsed()) {
            return sheet;
        }
        Dnd5eSheet result = sheet;
        int untracked = sheet.hitDiceUsed() - tracked;
        List<Integer> sizesLargestFirst = classes.stream().map(Dnd5eClassLevel::hitDieSize).distinct()
                .sorted(Comparator.reverseOrder()).toList();
        for (int size : sizesLargestFirst) {
            int before = result.classLevelsOrEmpty().stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum();
            result = distribute(result, size, untracked);
            untracked -= result.classLevelsOrEmpty().stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum() - before;
        }
        return result;
    }

    private static int recoveryBudget(Dnd5eSheet sheet) {
        return Math.max(1, sheet.level() / 2);
    }

    /** Adds {@code delta} spent dice across the classes of one die size, in class order, within 0..level. */
    private static Dnd5eSheet distribute(Dnd5eSheet sheet, int dieSize, int delta) {
        List<Dnd5eClassLevel> classes = new ArrayList<>();
        int remaining = delta;
        for (Dnd5eClassLevel classLevel : sheet.classLevelsOrEmpty()) {
            if (classLevel.hitDieSize() != dieSize || remaining == 0) {
                classes.add(classLevel);
                continue;
            }
            int used = Math.clamp((long) classLevel.hitDiceUsed() + remaining, 0, classLevel.level());
            remaining -= used - classLevel.hitDiceUsed();
            classes.add(classLevel.withHitDiceUsed(used));
        }
        return sheet.withClassLevels(classes).withHitDiceUsed(classes.stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum());
    }
}
