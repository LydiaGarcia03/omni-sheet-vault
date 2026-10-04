package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.GameSystem;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameSystemRegistry {

    private final Map<String, GameSystem> bySystem;

    public GameSystemRegistry(List<GameSystem> systems) {
        this.bySystem = systems.stream().collect(toMap(GameSystem::systemId, identity()));
    }

    public GameSystem forSystem(String systemId) {
        GameSystem system = bySystem.get(systemId);
        if (system == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return system;
    }
}
