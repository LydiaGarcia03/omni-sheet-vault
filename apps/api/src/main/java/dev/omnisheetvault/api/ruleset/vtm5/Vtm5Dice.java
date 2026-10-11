package dev.omnisheetvault.api.ruleset.vtm5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a VtM d10 pool: 6+ is a success, each pair of 10s adds two more (a critical), a critical
 * with a 10 on a Hunger die is messy, and a failed roll with a 1 on a Hunger die is bestial.
 * Hunger dice are the last {@code hunger} dice of the pool. Without a Difficulty a roll fails
 * only with no successes.
 */
final class Vtm5Dice {

    private static final Pattern EXPRESSION = Pattern.compile("(\\d++)d10 hunger (\\d++)(?: difficulty (\\d++))?");

    record Outcome(int successes, int difficulty, boolean critical, boolean messy, boolean bestial) {
        boolean win() {
            return difficulty > 0 ? successes >= difficulty : successes > 0;
        }

        int margin() {
            return successes - difficulty;
        }

        String tag() {
            if (!win()) {
                return bestial ? "Bestial failure" : difficulty > 0 && successes > 0 ? "Failure · win at a cost" : "Failure";
            }
            return messy ? "Messy critical" : critical ? "Critical" : difficulty > 0 ? "Success" : "";
        }
    }

    private Vtm5Dice() {
    }

    static Outcome read(int[] faces, int hunger) {
        return read(faces, hunger, 0);
    }

    static Outcome read(int[] faces, int hunger, int difficulty) {
        int regularCount = faces.length - hunger;
        int successes = 0;
        int tens = 0;
        boolean hungerTen = false;
        boolean hungerOne = false;
        for (int i = 0; i < faces.length; i++) {
            boolean isHunger = i >= regularCount;
            if (faces[i] >= 6) successes++;
            if (faces[i] == 10) {
                tens++;
                hungerTen |= isHunger;
            }
            hungerOne |= isHunger && faces[i] == 1;
        }
        int pairs = tens / 2;
        int total = successes + pairs * 2;
        boolean failed = difficulty > 0 ? total < difficulty : total == 0;
        return new Outcome(total, difficulty, pairs > 0, pairs > 0 && hungerTen && !failed, failed && hungerOne);
    }

    static String expression(int pool, int hunger, int difficulty) {
        return pool + "d10 hunger " + hunger + (difficulty > 0 ? " difficulty " + difficulty : "");
    }

    static String expression(int pool, int hunger) {
        return expression(pool, hunger, 0);
    }

    /** The Hunger dice count stored in a roll's expression, or -1 for a roll that isn't a VtM pool. */
    static int hungerOf(String expression) {
        Matcher matcher = EXPRESSION.matcher(expression == null ? "" : expression);
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) : -1;
    }

    /** The Difficulty stored in a roll's expression, 0 when it had none. */
    static int difficultyOf(String expression) {
        Matcher matcher = EXPRESSION.matcher(expression == null ? "" : expression);
        return matcher.matches() && matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
    }
}
