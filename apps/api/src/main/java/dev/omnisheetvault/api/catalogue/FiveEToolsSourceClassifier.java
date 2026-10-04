package dev.omnisheetvault.api.catalogue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides whether a 5etools source code is in scope for the D&D 5e catalogue: every
 * source except the ones written for the 2024 rules, which belong to a future 5.5e
 * system (adr-0006). Collab sources stay in scope; players opt out of them per
 * character (adr-0006, phase 11). Codes compare case-insensitively, since 5etools
 * mixes case ({@code AitFR-AVT}, {@code SatO}).
 */
final class FiveEToolsSourceClassifier {

    private final Set<String> outOfScopeSources;

    FiveEToolsSourceClassifier(Set<String> outOfScopeSources) {
        this.outOfScopeSources =
                outOfScopeSources.stream().map(code -> code.toUpperCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /** Leaves out every source {@link FiveEToolsSourceNames} dates to the 2024 rules. */
    static FiveEToolsSourceClassifier forRules2014(FiveEToolsSourceNames sourceNames) {
        return new FiveEToolsSourceClassifier(sourceNames.revisedRulesSources());
    }

    boolean isInScope(String sourceCode) {
        return !outOfScopeSources.contains(sourceCode.toUpperCase(Locale.ROOT));
    }
}
