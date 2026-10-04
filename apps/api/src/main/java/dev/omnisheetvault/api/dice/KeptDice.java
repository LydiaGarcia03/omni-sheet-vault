package dev.omnisheetvault.api.dice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/** "Keep highest" dice notation ({@code 4d6kh3}): which dice a roll drops, and its expression. */
final class KeptDice {

    private static final Pattern KEEP_HIGHEST = Pattern.compile("^(\\d+)d(\\d+)kh(\\d+)$");

    private KeptDice() {
    }

    static String expression(int diceCount, int diceSides, Integer keepHighest) {
        String dice = diceCount + "d" + diceSides;
        return keepHighest == null || keepHighest >= diceCount ? dice : dice + "kh" + keepHighest;
    }

    /** Indexes of the dice left out when only the {@code keep} highest count; ties drop the earliest die. */
    static int[] dropped(int[] results, int keep) {
        int dropCount = Math.max(0, results.length - keep);
        return IntStream.range(0, results.length)
                .boxed()
                .sorted(Comparator.<Integer>comparingInt(index -> results[index]).thenComparingInt(index -> index))
                .limit(dropCount)
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
    }

    /** The sum of the dice {@link #dropped} keeps. */
    static int keptTotal(int[] results, int keep) {
        int[] dropped = dropped(results, keep);
        return IntStream.range(0, results.length).filter(index -> Arrays.binarySearch(dropped, index) < 0).map(index -> results[index]).sum();
    }

    /** The dropped dice of a stored roll, read back from its expression; none unless it keeps only its highest dice. */
    static int[] droppedFor(String expression, int[] results) {
        Matcher matcher = KEEP_HIGHEST.matcher(expression);
        return matcher.matches() ? dropped(results, Integer.parseInt(matcher.group(3))) : new int[0];
    }
}
