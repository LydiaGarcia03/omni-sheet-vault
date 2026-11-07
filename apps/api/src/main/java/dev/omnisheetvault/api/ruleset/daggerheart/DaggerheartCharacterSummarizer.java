package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.ANCESTRY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.CLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.COMMUNITY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.SUBCLASS;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CharacterSummarizer;
import dev.omnisheetvault.api.ruleset.SummaryFact;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** A Daggerheart character card: class and subclass with the level ("Ranger (Wayfinder) 1", "… 6 · Wizard" once multiclassed), then heritage. */
@Component
class DaggerheartCharacterSummarizer implements CharacterSummarizer {

    private final ObjectMapper objectMapper;

    DaggerheartCharacterSummarizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String systemId() {
        return DaggerheartGameSystem.SYSTEM_ID;
    }

    @Override
    public List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup lookup) {
        return draftBuildJson != null ? fromBuild(DaggerheartBuild.read(draftBuildJson, objectMapper), new DaggerheartCatalogue(lookup))
                : fromSheet(objectMapper.readTree(sheetJson));
    }

    private static List<SummaryFact> fromSheet(JsonNode sheet) {
        JsonNode identity = sheet.path("identity");
        List<SummaryFact> facts = new ArrayList<>();
        String classLine = classLine(identity.path("class").path("name").asString(""), identity.path("subclass").path("name").asString(""));
        String multiclass = identity.path("multiclass").path("class").path("name").asString("");
        add(facts, "Class", classLine.isEmpty() ? "" : classLine + " " + sheet.path("level").asInt(1) + (multiclass.isEmpty() ? "" : " · " + multiclass));
        add(facts, "Heritage", join(identity.path("community").path("name").asString(""), identity.path("ancestry").path("name").asString("")));
        return facts;
    }

    private static List<SummaryFact> fromBuild(DaggerheartBuild build, DaggerheartCatalogue catalogue) {
        List<SummaryFact> facts = new ArrayList<>();
        add(facts, "Class", classLine(catalogue.name(CLASS, build.classSlug()), catalogue.name(SUBCLASS, build.subclassSlug())));
        String ancestry = build.isMixedAncestry() ? build.mixedAncestryName() : catalogue.name(ANCESTRY, build.ancestrySlug());
        add(facts, "Heritage", join(catalogue.name(COMMUNITY, build.communitySlug()), ancestry));
        return facts;
    }

    private static String classLine(String className, String subclassName) {
        return className.isEmpty() || subclassName.isEmpty() ? className : className + " (" + subclassName + ")";
    }

    private static String join(String first, String second) {
        return (first + " " + second).trim();
    }

    private static void add(List<SummaryFact> facts, String label, String value) {
        if (!value.isEmpty()) {
            facts.add(new SummaryFact(label, value));
        }
    }
}
