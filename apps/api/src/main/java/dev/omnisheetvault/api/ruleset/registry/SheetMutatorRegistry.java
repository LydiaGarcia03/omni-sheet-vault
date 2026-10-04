package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.SheetMutator;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SheetMutatorRegistry {

    private final Map<String, SheetMutator> bySystem;

    public SheetMutatorRegistry(List<SheetMutator> mutators) {
        this.bySystem = mutators.stream().collect(toMap(SheetMutator::systemId, identity()));
    }

    public SheetMutator forSystem(String systemId) {
        SheetMutator mutator = bySystem.get(systemId);
        if (mutator == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return mutator;
    }
}
