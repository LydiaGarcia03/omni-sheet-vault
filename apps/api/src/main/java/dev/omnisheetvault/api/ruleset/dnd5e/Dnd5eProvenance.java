package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.ProficiencySource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a sheet's {@link Dnd5eDerivation} (where each build-derived value came from) as
 * the labelled contributions the sheet displays. A sheet without a derivation (one not
 * built from a build) falls back to a single "Base" line, and a stored value that no
 * longer matches its derivation gets an "Other" line for the difference.
 */
final class Dnd5eProvenance {

    /** A materializer label such as "Fighter 7 (average d10)". */
    private static final Pattern AVERAGE_LEVEL = Pattern.compile("^(.+) (\\d+) \\((average d\\d+)\\)$");

    private final Dnd5eDerivation derivation;

    Dnd5eProvenance(Dnd5eSheet sheet) {
        this.derivation = sheet.derivation();
    }

    /** The source that granted a proficiency or expertise ({@code kind} as recorded, e.g. SKILL), or null. */
    String grantSource(String kind, String key) {
        if (derivation == null || derivation.grants() == null) {
            return null;
        }
        return derivation.grants().stream()
                .filter(grant -> grant.kind().equals(kind) && grant.key().equals(key))
                .map(Dnd5eDerivation.SourcedGrant::source)
                .findFirst()
                .orElse(null);
    }

    /** Armor, weapon, tool and language grants with the label the sheet shows for them. */
    List<ProficiencySource> proficiencySources() {
        if (derivation == null || derivation.grants() == null) {
            return List.of();
        }
        return derivation.grants().stream()
                .filter(grant -> grant.label() != null)
                .map(grant -> new ProficiencySource(grant.kind(), grant.label(), grant.source()))
                .toList();
    }

    List<Contribution> abilityScore(String ability, int storedScore) {
        List<Dnd5eDerivation.SourcedAmount> parts = derivation == null || derivation.abilityScores() == null
                ? List.of()
                : derivation.abilityScores().getOrDefault(ability, List.of());
        return reconciled(parts, storedScore, "Base");
    }

    List<Contribution> speed(int storedSpeed) {
        return reconciled(derivation == null || derivation.speed() == null ? List.of() : derivation.speed(), storedSpeed, "Base");
    }

    /**
     * One line per level from the derivation, with consecutive average levels of one class
     * merged ("Fighter 2–20 (average d10 × 19)"); null when the derivation doesn't add up to
     * {@code hitPointBase}, so the caller keeps its own summary line.
     */
    List<Contribution> hitDice(int hitPointBase) {
        if (derivation == null || derivation.hitPoints() == null || derivation.hitPoints().isEmpty()
                || derivation.hitPoints().stream().mapToInt(Dnd5eDerivation.SourcedAmount::amount).sum() != hitPointBase) {
            return null;
        }
        List<Contribution> lines = new ArrayList<>();
        AverageRun run = null;
        for (Dnd5eDerivation.SourcedAmount level : derivation.hitPoints()) {
            Matcher average = AVERAGE_LEVEL.matcher(level.source());
            if (average.matches() && run != null && run.continuesWith(average, level.amount())) {
                run = run.extended();
                continue;
            }
            if (run != null) {
                lines.add(run.contribution());
                run = null;
            }
            if (average.matches()) {
                run = new AverageRun(average.group(1), Integer.parseInt(average.group(2)), 1, average.group(3), level.amount());
            } else {
                lines.add(new Contribution(level.source(), level.amount()));
            }
        }
        if (run != null) {
            lines.add(run.contribution());
        }
        return lines;
    }

    private static List<Contribution> reconciled(List<Dnd5eDerivation.SourcedAmount> parts, int storedValue, String fallbackLabel) {
        if (parts.isEmpty()) {
            return List.of(new Contribution(fallbackLabel, storedValue));
        }
        List<Contribution> contributions = new ArrayList<>(parts.stream()
                .map(part -> new Contribution(part.source(), part.amount()))
                .toList());
        int difference = storedValue - contributions.stream().mapToInt(Contribution::amount).sum();
        if (difference != 0) {
            contributions.add(new Contribution("Other", difference));
        }
        return contributions;
    }

    private record AverageRun(String className, int firstLevel, int levels, String die, int amountPerLevel) {

        boolean continuesWith(Matcher average, int amount) {
            return average.group(1).equals(className) && average.group(3).equals(die) && amount == amountPerLevel
                    && Integer.parseInt(average.group(2)) == firstLevel + levels;
        }

        AverageRun extended() {
            return new AverageRun(className, firstLevel, levels + 1, die, amountPerLevel);
        }

        Contribution contribution() {
            String label = levels == 1
                    ? className + " " + firstLevel + " (" + die + ")"
                    : className + " " + firstLevel + "–" + (firstLevel + levels - 1) + " (" + die + " × " + levels + ")";
            return new Contribution(label, amountPerLevel * levels);
        }
    }
}
