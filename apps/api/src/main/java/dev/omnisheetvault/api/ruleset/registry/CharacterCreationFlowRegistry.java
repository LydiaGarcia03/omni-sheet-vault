package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import dev.omnisheetvault.api.ruleset.dnd5e.Dnd5eCharacterCreationFlow;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class CharacterCreationFlowRegistry {

    private final Map<String, CharacterCreationFlow> bySystem;

    public CharacterCreationFlowRegistry(List<CharacterCreationFlow> flows) {
        this.bySystem = flows.stream().collect(toMap(CharacterCreationFlow::systemId, identity()));
    }

    /** For command-line tools that run without a Spring context (e.g. {@code planBuild}); lists every built-in flow. */
    public static CharacterCreationFlowRegistry standalone(ObjectMapper objectMapper) {
        return new CharacterCreationFlowRegistry(List.of(
                new Dnd5eCharacterCreationFlow(objectMapper, Validation.buildDefaultValidatorFactory().getValidator())));
    }

    public CharacterCreationFlow forSystem(String systemId) {
        CharacterCreationFlow flow = bySystem.get(systemId);
        if (flow == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return flow;
    }
}
