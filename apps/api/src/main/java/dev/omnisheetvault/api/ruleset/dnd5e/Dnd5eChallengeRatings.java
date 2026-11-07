package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Map;

/** The Monster Manual's (2014) challenge table: experience points and proficiency bonus by challenge rating. */
final class Dnd5eChallengeRatings {

    private static final Map<String, Integer> EXPERIENCE = Map.ofEntries(
            Map.entry("0", 10), Map.entry("1/8", 25), Map.entry("1/4", 50), Map.entry("1/2", 100),
            Map.entry("1", 200), Map.entry("2", 450), Map.entry("3", 700), Map.entry("4", 1100), Map.entry("5", 1800),
            Map.entry("6", 2300), Map.entry("7", 2900), Map.entry("8", 3900), Map.entry("9", 5000), Map.entry("10", 5900),
            Map.entry("11", 7200), Map.entry("12", 8400), Map.entry("13", 10000), Map.entry("14", 11500), Map.entry("15", 13000),
            Map.entry("16", 15000), Map.entry("17", 18000), Map.entry("18", 20000), Map.entry("19", 22000), Map.entry("20", 25000),
            Map.entry("21", 33000), Map.entry("22", 41000), Map.entry("23", 50000), Map.entry("24", 62000), Map.entry("25", 75000),
            Map.entry("26", 90000), Map.entry("27", 105000), Map.entry("28", 120000), Map.entry("29", 135000), Map.entry("30", 155000));

    private Dnd5eChallengeRatings() {
    }

    static Integer experience(String rating) {
        return rating == null ? null : EXPERIENCE.get(rating.trim());
    }

    /** +2 up to CR 4, then one more every four ratings, to +9 at CR 29–30. */
    static Integer proficiencyBonus(String rating) {
        if (experience(rating) == null) {
            return null;
        }
        String trimmed = rating.trim();
        int whole = trimmed.contains("/") ? 0 : Integer.parseInt(trimmed);
        return whole < 5 ? 2 : 2 + (whole - 1) / 4;
    }
}
