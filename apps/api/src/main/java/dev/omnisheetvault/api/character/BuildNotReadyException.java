package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import java.util.stream.Collectors;

/** A build that can't be materialized yet: it still has pending choices or rule problems. */
public class BuildNotReadyException extends RuntimeException {

    BuildNotReadyException(String characterName, BuildPlan plan) {
        super(characterName + " is not ready: pending ["
                + plan.pending().stream().map(CreationChoice::id).collect(Collectors.joining(", "))
                + "], problems [" + String.join("; ", plan.problems()) + "]");
    }
}
