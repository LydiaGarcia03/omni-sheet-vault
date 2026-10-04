package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CharacterSummarizer;
import dev.omnisheetvault.api.ruleset.SummaryFact;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A D&D 5e card: each class with its own level ("Rogue 4 / Sorcerer 3"), then the species.
 * Never a total level. Reads only the nodes it needs, so older sheets still summarize.
 */
@Component
class Dnd5eCharacterSummarizer implements CharacterSummarizer {

    static final String CLASSES = "Classes";
    static final String SPECIES = "Species";

    private final ObjectMapper objectMapper;

    Dnd5eCharacterSummarizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String systemId() {
        return Dnd5eGameSystem.SYSTEM_ID;
    }

    @Override
    public List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup catalogue) {
        if (draftBuildJson != null) {
            JsonNode build = objectMapper.readTree(draftBuildJson);
            List<String> classes = new ArrayList<>();
            for (JsonNode entry : build.path("classes")) {
                classes.add(name(catalogue, "CLASS", entry.path("classSlug").asString()) + " " + entry.path("level").asInt());
            }
            return facts(classes, species(build, catalogue));
        }
        JsonNode sheet = objectMapper.readTree(sheetJson);
        List<String> classes = new ArrayList<>();
        for (JsonNode entry : sheet.path("classLevels")) {
            classes.add(entry.path("className").asString() + " " + entry.path("level").asInt());
        }
        return facts(classes, species(sheet.path("build"), catalogue));
    }

    private static List<SummaryFact> facts(List<String> classes, String species) {
        List<SummaryFact> facts = new ArrayList<>();
        if (!classes.isEmpty()) {
            facts.add(new SummaryFact(CLASSES, String.join(" / ", classes)));
        }
        if (species != null) {
            facts.add(new SummaryFact(SPECIES, species));
        }
        return facts;
    }

    private static String species(JsonNode build, CatalogueLookup catalogue) {
        String slug = build.path("speciesSlug").asString(null);
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return Dnd5eBuildPlanner.speciesLabel(name(catalogue, "SPECIES", slug), build.path("subspeciesName").asString(null));
    }

    /** The catalogue's name for a slug; a slug the catalogue no longer has still reads as words. */
    private static String name(CatalogueLookup catalogue, String kind, String slug) {
        return catalogue.find(kind, slug).map(CatalogueRecord::name).orElse(Dnd5eChoiceOptions.labelFromSlug(slug));
    }
}
