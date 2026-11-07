package dev.omnisheetvault.api.ruleset.daggerheart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads Daggerheart rolls (SRD pp. 47–50) and the expressions they are stored under. A Duality roll is the
 * Hope die and the Fear die, plus a d6 with advantage or minus one with disadvantage, plus modifiers; matching
 * dice are a critical success, which counts as with Hope. A damage roll sums its dice and bonus; a critical
 * adds the dice's maximum.
 */
final class DaggerheartDice {

    private static final Pattern DUALITY = Pattern.compile("duality (action|reaction) ([+-]\\d++)( adv| dis)?(?: dc (\\d++))?((?: \\d++d\\d++)*+)");
    private static final Pattern DAMAGE = Pattern.compile("damage (\\d++)d(\\d++)([+-]\\d++)?( crit)?");
    private static final Pattern REST = Pattern.compile("rest 1d4\\+(\\d++)");

    enum Advantage { NONE, ADVANTAGE, DISADVANTAGE }

    /** {@code extraDice} are dice the player added to the pool ("1d4"), rolled into {@code extraFaces} and added to the total. */
    record Duality(int hope, int fear, int advantageDie, int modifier, int difficulty, boolean reaction, List<String> extraDice, List<Integer> extraFaces) {

        Duality(int hope, int fear, int advantageDie, int modifier, int difficulty, boolean reaction) {
            this(hope, fear, advantageDie, modifier, difficulty, reaction, List.of(), List.of());
        }

        Duality withExtras(List<String> dice, List<Integer> faces) {
            return new Duality(hope, fear, advantageDie, modifier, difficulty, reaction, List.copyOf(dice), List.copyOf(faces));
        }

        boolean critical() {
            return hope == fear;
        }

        boolean withHope() {
            return critical() || hope > fear;
        }

        int total() {
            return hope + fear + advantageDie + modifier + extraFaces.stream().mapToInt(Integer::intValue).sum();
        }

        /** Null without a Difficulty: the roll then only reads as "with Hope" or "with Fear". */
        Boolean success() {
            if (critical()) {
                return true;
            }
            return difficulty > 0 ? total() >= difficulty : null;
        }

        String outcome() {
            if (critical()) {
                return "Critical Success";
            }
            String side = withHope() ? "with Hope" : "with Fear";
            Boolean success = success();
            return success == null ? total() + " " + side : (success ? "Success " : "Failure ") + side;
        }

        String expression() {
            String advantage = advantageDie > 0 ? " adv" : advantageDie < 0 ? " dis" : "";
            return "duality " + (reaction ? "reaction" : "action") + " " + signed(modifier) + advantage + (difficulty > 0 ? " dc " + difficulty : "")
                    + extraDice.stream().map(dice -> " " + dice).reduce("", String::concat);
        }

        int[] results() {
            List<Integer> results = new ArrayList<>(List.of(hope, fear));
            if (advantageDie != 0) {
                results.add(Math.abs(advantageDie));
            }
            results.addAll(extraFaces);
            return results.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    record Damage(int[] dice, int sides, int bonus, boolean critical) {

        int total() {
            return Arrays.stream(dice).sum() + bonus + (critical ? dice.length * sides : 0);
        }

        String expression() {
            return "damage " + dice.length + "d" + sides + (bonus != 0 ? signed(bonus) : "") + (critical ? " crit" : "");
        }
    }

    /** A free roll of any dice: groups like 2d6 and 1d8, plus a flat modifier; stored as {@code dice 2d6 1d8 +3}. */
    record Custom(List<int[]> groups, int[] faces, int bonus, boolean critical) {

        /** A critical (damage after a critical attack) adds the dice's maximum (SRD p. 50). */
        int total() {
            return Arrays.stream(faces).sum() + bonus + (critical ? groups.stream().mapToInt(group -> group[0] * group[1]).sum() : 0);
        }

        String expression() {
            StringBuilder expression = new StringBuilder("dice");
            groups.forEach(group -> expression.append(' ').append(group[0]).append('d').append(group[1]));
            return expression.append(' ').append(signed(bonus)).append(critical ? " crit" : "").toString();
        }
    }

    private static final Pattern CUSTOM = Pattern.compile("dice((?: \\d++d\\d++)++) ([+-]\\d++)( crit)?");
    private static final Pattern GROUP = Pattern.compile("(\\d++)d(\\d++)");

    static Custom readCustom(String expression, int[] results) {
        Matcher matcher = CUSTOM.matcher(expression);
        if (!matcher.matches()) {
            return null;
        }
        List<int[]> groups = new ArrayList<>();
        Matcher group = GROUP.matcher(matcher.group(1));
        while (group.find()) {
            groups.add(new int[] {Integer.parseInt(group.group(1)), Integer.parseInt(group.group(2))});
        }
        return new Custom(groups, results, Integer.parseInt(matcher.group(2)), matcher.group(3) != null);
    }

    private DaggerheartDice() {
    }

    static Duality duality(int hope, int fear, Advantage advantage, int d6, int modifier, int difficulty, boolean reaction) {
        int advantageDie = switch (advantage) {
            case ADVANTAGE -> d6;
            case DISADVANTAGE -> -d6;
            case NONE -> 0;
        };
        return new Duality(hope, fear, advantageDie, modifier, Math.max(0, difficulty), reaction);
    }

    /** A stored Duality roll, or null when the expression is another kind. */
    static Duality readDuality(String expression, int[] results) {
        Matcher matcher = DUALITY.matcher(expression);
        if (!matcher.matches() || results.length < 2) {
            return null;
        }
        String advantage = matcher.group(3) == null ? "" : matcher.group(3).strip();
        int die = results.length > 2 && !advantage.isEmpty() ? results[2] : 0;
        int advantageDie = advantage.equals("adv") ? die : advantage.equals("dis") ? -die : 0;
        int difficulty = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
        List<String> extraDice = matcher.group(5).isBlank() ? List.of() : List.of(matcher.group(5).strip().split(" "));
        int first = advantageDie == 0 ? 2 : 3;
        List<Integer> extraFaces = Arrays.stream(results).skip(first).boxed().toList();
        return new Duality(results[0], results[1], advantageDie, Integer.parseInt(matcher.group(2)), difficulty, matcher.group(1).equals("reaction"), extraDice, extraFaces);
    }

    static Damage readDamage(String expression, int[] results) {
        Matcher matcher = DAMAGE.matcher(expression);
        if (!matcher.matches()) {
            return null;
        }
        int bonus = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new Damage(results, Integer.parseInt(matcher.group(2)), bonus, matcher.group(4) != null);
    }

    /** The Tier a rest move's 1d4 + Tier was rolled with, or -1 for another kind. */
    static int restTier(String expression) {
        Matcher matcher = REST.matcher(expression);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }
}
