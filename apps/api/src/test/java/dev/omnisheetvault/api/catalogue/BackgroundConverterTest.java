package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures are trimmed copies of real 5etools backgrounds.json entries (Soldier, Baldur's Gate Soldier). */
class BackgroundConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BackgroundConverter converter = new BackgroundConverter(objectMapper);

    private Path dataRoot;

    @BeforeEach
    void createDataRoot() throws IOException {
        dataRoot = Files.createTempDirectory("background-converter-test");
        Files.writeString(dataRoot.resolve("backgrounds.json"), BACKGROUNDS);
    }

    @Test
    void dropsThe2024EditionAndResolvesCopies() {
        assertThat(load()).extracting(background -> background.get("name").asString())
                .containsExactly("Soldier", "Baldur's Gate Soldier");
    }

    @Test
    void findsTheFourSuggestionTablesByColumnLabelAndKeepsOthers() {
        JsonNode data = convert("Soldier").data();

        assertThat(data.get("personalityTraits")).extracting(JsonNode::asString).containsExactly("I'm polite.", "I'm haunted.");
        assertThat(data.get("ideals")).extracting(JsonNode::asString).containsExactly("Greater Good. Defend others. (Good)");
        assertThat(data.get("bonds")).extracting(JsonNode::asString).containsExactly("My honor is my life.");
        assertThat(data.get("flaws")).extracting(JsonNode::asString).containsExactly("I obey the law.");
        JsonNode specialty = data.get("otherTables").get(0);
        assertThat(specialty.get("colLabels")).extracting(JsonNode::asString).containsExactly("d8", "Specialty");
        assertThat(specialty.get("rows").get(0)).extracting(JsonNode::asString).containsExactly("1", "Officer");
    }

    @Test
    void mapsFeatureGrantsAndStartingEquipment() {
        JsonNode data = convert("Soldier").data();

        assertThat(data.get("features").get(0).get("name").asString()).isEqualTo("Military Rank");
        assertThat(data.get("skillAlternatives").get(0).get("fixed")).extracting(JsonNode::asString)
                .containsExactly("athletics", "intimidation");
        assertThat(data.get("toolAlternatives").get(0).get("choices").get(0).get("category").asString()).isEqualTo("gamingSet");
        JsonNode fixedGroup = data.get("startingEquipment").get(0).get("options").get(0).get("grants");
        assertThat(fixedGroup.get(0).get("special").asString()).isEqualTo("insignia of rank");
        assertThat(fixedGroup.get(1).get("itemSlug").asString()).isEqualTo("pouch");
        assertThat(fixedGroup.get(1).get("containsValueCp").asInt()).isEqualTo(1000);
        JsonNode choiceA = data.get("startingEquipment").get(1).get("options").get(0).get("grants").get(0);
        assertThat(choiceA.get("displayName").asString()).isEqualTo("bone dice set");
    }

    @Test
    void aCopyKeepsItsTargetsTablesAndGainsInsertedFeatures() {
        JsonNode data = convert("Baldur's Gate Soldier").data();

        assertThat(data.get("features")).extracting(feature -> feature.get("name").asString())
                .containsExactly("Military Rank", "Flaming Fist Veteran");
        assertThat(data.get("personalityTraits")).hasSize(2);
    }

    private List<JsonNode> load() {
        return converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper));
    }

    private CatalogueEntryImport convert(String name) {
        return converter.convert(load().stream().filter(background -> name.equals(background.get("name").asString())).findFirst().orElseThrow());
    }

    private static final String BACKGROUNDS = """
            {"background":[
              {"name":"Soldier","source":"PHB","page":140,
               "skillProficiencies":[{"athletics":true,"intimidation":true}],
               "toolProficiencies":[{"anyGamingSet":1,"vehicles (land)":true}],
               "startingEquipment":[
                 {"_":[{"special":"insignia of rank"},{"item":"pouch|phb","containsValue":1000}]},
                 {"a":[{"item":"dice set|phb","displayName":"bone dice set"}],"b":["playing card set|phb"]}],
               "entries":[
                 {"type":"entries","name":"Feature: Military Rank","entries":["You have a military rank."]},
                 {"type":"entries","name":"Specialty","entries":[
                   {"type":"table","colLabels":["{@dice d8}","Specialty"],"rows":[["1","Officer"]]}]},
                 {"type":"entries","name":"Suggested Characteristics","entries":[
                   {"type":"table","colLabels":["{@dice d8}","Personality Trait"],"rows":[["1","I'm polite."],["2","I'm haunted."]]},
                   {"type":"table","colLabels":["{@dice d6}","Ideal"],"rows":[["1","Greater Good. Defend others. (Good)"]]},
                   {"type":"table","colLabels":["{@dice d6}","Bond"],"rows":[[{"type":"cell","roll":{"exact":1}},"My honor is my life."]]},
                   {"type":"table","colLabels":["{@dice d6}","Flaw"],"rows":[["1","I obey the law."]]}]}]},
              {"name":"Soldier","source":"XPHB","edition":"one","page":1,"entries":[]},
              {"name":"Baldur's Gate Soldier","source":"BGDIA","page":20,
               "_copy":{"name":"Soldier","source":"PHB","_mod":{"entries":{"mode":"insertArr","index":1,
                 "items":{"type":"entries","name":"Baldur's Gate Feature: Flaming Fist Veteran","entries":["You served."]}}}}}]}
            """;
}
