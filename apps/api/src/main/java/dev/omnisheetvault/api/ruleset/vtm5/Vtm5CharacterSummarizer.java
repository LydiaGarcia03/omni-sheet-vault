package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CharacterSummarizer;
import dev.omnisheetvault.api.ruleset.SummaryFact;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** A VtM character card: clan, age and predator type. */
@Component
class Vtm5CharacterSummarizer implements CharacterSummarizer {

    private final ObjectMapper objectMapper;

    Vtm5CharacterSummarizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String systemId() {
        return Vtm5GameSystem.SYSTEM_ID;
    }

    @Override
    public List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup catalogue) {
        JsonNode source = draftBuildJson != null ? objectMapper.readTree(draftBuildJson) : objectMapper.readTree(sheetJson).path("identity");
        List<SummaryFact> facts = new ArrayList<>();
        add(facts, "Clan", Vtm5CharacterCreationFlow.text(source, "clan"));
        add(facts, "Age", Vtm5CharacterCreationFlow.text(source, "age"));
        add(facts, "Predator", Vtm5CharacterCreationFlow.text(source, "predator"));
        return facts;
    }

    private static void add(List<SummaryFact> facts, String label, String value) {
        if (!value.isEmpty()) {
            facts.add(new SummaryFact(label, value));
        }
    }
}
