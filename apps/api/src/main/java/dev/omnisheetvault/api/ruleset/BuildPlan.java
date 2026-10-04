package dev.omnisheetvault.api.ruleset;

import java.util.List;

/** Every choice a build offers, in order, plus the rule problems found in its current answers. */
public record BuildPlan(List<CreationChoice> choices, List<String> problems) {

    public List<CreationChoice> pending() {
        return choices.stream().filter(CreationChoice::isPending).toList();
    }
}
