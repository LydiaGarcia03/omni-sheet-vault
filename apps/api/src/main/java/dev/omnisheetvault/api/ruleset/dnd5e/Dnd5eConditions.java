package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Set;

/**
 * The fourteen standard 5e conditions (exhaustion excluded — it tracks a level, not a
 * boolean, per {@link Dnd5eSheet}'s doc comment). Canonical source for validating a
 * condition toggle; the frontend's own copy is display labels only.
 */
final class Dnd5eConditions {

    static final Set<String> ALL = Set.of(
            "blinded", "charmed", "deafened", "frightened", "grappled", "incapacitated",
            "invisible", "paralyzed", "petrified", "poisoned", "prone", "restrained",
            "stunned", "unconscious");

    private Dnd5eConditions() {
    }
}
