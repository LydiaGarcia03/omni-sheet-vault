package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.MechanicResolver;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MechanicResolverRegistry {

    private final Map<String, MechanicResolver> bySystem;

    public MechanicResolverRegistry(List<MechanicResolver> resolvers) {
        this.bySystem = resolvers.stream().collect(toMap(MechanicResolver::systemId, identity()));
    }

    public MechanicResolver forSystem(String systemId) {
        MechanicResolver resolver = bySystem.get(systemId);
        if (resolver == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return resolver;
    }
}
