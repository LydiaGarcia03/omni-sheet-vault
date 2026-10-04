package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.ruleset.SummaryFact;
import dev.omnisheetvault.api.ruleset.registry.CharacterSummarizerRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** A character card's facts, composed by its game system's summarizer. */
@Service
public class CharacterSummaryService {

    private static final Logger LOG = LoggerFactory.getLogger(CharacterSummaryService.class);

    private final CharacterSummarizerRegistry summarizers;
    private final CatalogueService catalogueService;

    CharacterSummaryService(CharacterSummarizerRegistry summarizers, CatalogueService catalogueService) {
        this.summarizers = summarizers;
        this.catalogueService = catalogueService;
    }

    /** Empty when the system can't read this character yet: a card without facts beats a list that fails to load. */
    public List<SummaryFact> summarize(Character character) {
        try {
            return summarizers.forSystem(character.systemId()).summarize(
                    character.isDraft() ? null : character.sheet(),
                    character.isDraft() ? character.creationDraft() : null,
                    catalogueService.lookup(character.systemId()));
        } catch (RuntimeException e) {
            LOG.warn("Could not summarize character {}", character.id(), e);
            return List.of();
        }
    }
}
