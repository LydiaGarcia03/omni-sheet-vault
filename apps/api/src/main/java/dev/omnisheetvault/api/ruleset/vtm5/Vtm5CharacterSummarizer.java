package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.shared.InterfaceLocale;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CharacterSummarizer;
import dev.omnisheetvault.api.ruleset.SummaryFact;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** A VtM character card: the clan, then age and predator type on one line ("Neonate · Alleycat"). */
@Component
class Vtm5CharacterSummarizer implements CharacterSummarizer {

    private final ObjectMapper objectMapper;
    private final Vtm5Content content;

    Vtm5CharacterSummarizer(ObjectMapper objectMapper, Vtm5Content content) {
        this.objectMapper = objectMapper;
        this.content = content;
    }

    @Override
    public String systemId() {
        return Vtm5GameSystem.SYSTEM_ID;
    }

    @Override
    public List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup catalogue) {
        JsonNode source = draftBuildJson != null ? objectMapper.readTree(draftBuildJson) : objectMapper.readTree(sheetJson).path("identity");
        List<SummaryFact> facts = new ArrayList<>();
        add(facts, "Clan", shown(source, "clan"));
        add(facts, "Age and predator", join(shown(source, "age"), shown(source, "predator")));
        return facts;
    }

    /** A content name from the build or sheet, in the request's language. */
    private String shown(JsonNode source, String field) {
        String name = Vtm5CharacterCreationFlow.text(source, field);
        return name.isEmpty() ? name : content.term(name, InterfaceLocale.current());
    }

    private static String join(String first, String second) {
        return first.isEmpty() || second.isEmpty() ? first + second : first + " · " + second;
    }

    private static void add(List<SummaryFact> facts, String label, String value) {
        if (!value.isEmpty()) {
            facts.add(new SummaryFact(label, value));
        }
    }
}
