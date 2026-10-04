package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.CharacterSummarizer;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CharacterSummarizerRegistry {

    private final Map<String, CharacterSummarizer> bySystem;

    public CharacterSummarizerRegistry(List<CharacterSummarizer> summarizers) {
        this.bySystem = summarizers.stream().collect(toMap(CharacterSummarizer::systemId, identity()));
    }

    public CharacterSummarizer forSystem(String systemId) {
        CharacterSummarizer summarizer = bySystem.get(systemId);
        if (summarizer == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return summarizer;
    }
}
