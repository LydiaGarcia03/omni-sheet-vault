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

/** Fixtures are trimmed copies of real 5etools class-*.json subclass entries (Evocation, Eldritch Knight). */
class SubclassConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SubclassConverter converter = new SubclassConverter(objectMapper, FiveEToolsMechanicsOverlay.empty());

    private Path dataRoot;

    @BeforeEach
    void createDataRoot() throws IOException {
        dataRoot = Files.createTempDirectory("subclass-converter-test");
        Files.createDirectories(dataRoot.resolve("class"));
        Files.writeString(dataRoot.resolve("books.json"), "{\"book\":[]}");
        Files.writeString(dataRoot.resolve("adventures.json"), "{\"adventure\":[]}");
    }

    @Test
    void keepsOnlySubclassesWhoseParentClassIsKept() throws IOException {
        writeClassFile(FILE);

        List<JsonNode> subclasses = converter.loadRawEntries(dataSource());

        assertThat(subclasses).extracting(subclass -> subclass.get("shortName").asString())
                .containsExactly("Evocation", "Eldritch Knight");
    }

    @Test
    void splitsTheIntroductionFromNestedFeatures() throws IOException {
        writeClassFile(FILE);

        CatalogueEntryImport evocation = converter.convert(converter.loadRawEntries(dataSource()).getFirst());

        assertThat(evocation.slug()).isEqualTo("wizard-evocation");
        assertThat(evocation.kind()).isEqualTo(CatalogueEntryKind.SUBCLASS);
        assertThat(evocation.description()).isEqualTo("You focus your study on evocation.");
        assertThat(evocation.data().get("classSlug").asString()).isEqualTo("wizard");
        assertThat(evocation.data().get("features")).extracting(feature -> feature.get("name").asString())
                .containsExactly("Evocation Savant", "Potent Cantrip");
        assertThat(evocation.data().get("spellcasting").isNull()).isTrue();
    }

    @Test
    void mapsASubclassesOwnSpellcasting() throws IOException {
        writeClassFile(FILE);

        CatalogueEntryImport eldritchKnight = converter.convert(converter.loadRawEntries(dataSource()).get(1));

        JsonNode spellcasting = eldritchKnight.data().get("spellcasting");
        assertThat(eldritchKnight.slug()).isEqualTo("fighter-eldritch-knight");
        assertThat(spellcasting.get("casterProgression").asString()).isEqualTo("1/3");
        assertThat(spellcasting.get("ability").asString()).isEqualTo("intelligence");
        assertThat(spellcasting.get("spellSlotsByLevel").get(2).get(0).asInt()).isEqualTo(2);
    }

    private FiveEToolsDataSource dataSource() {
        return new FiveEToolsDataSource(dataRoot, objectMapper);
    }

    private void writeClassFile(String content) throws IOException {
        Files.writeString(dataRoot.resolve("class").resolve("class-test.json"), content);
    }

    private static final String FILE = """
            {"class":[
              {"name":"Wizard","source":"PHB","edition":"classic","classFeatures":[]},
              {"name":"Fighter","source":"PHB","edition":"classic","classFeatures":[]},
              {"name":"Wizard","source":"XPHB","edition":"one","classFeatures":[]}],
             "subclass":[
              {"name":"School of Evocation","shortName":"Evocation","source":"PHB","className":"Wizard","classSource":"PHB","page":117,
               "subclassFeatures":["School of Evocation|Wizard||Evocation||2","Potent Cantrip|Wizard||Evocation||6"]},
              {"name":"School of Evocation","shortName":"Evocation","source":"PHB","className":"Wizard","classSource":"XPHB",
               "_copy":{},"subclassFeatures":[]},
              {"name":"Eldritch Knight","shortName":"Eldritch Knight","source":"PHB","className":"Fighter","classSource":"PHB","page":74,
               "spellcastingAbility":"int","casterProgression":"1/3",
               "subclassTableGroups":[{"title":"Spell Slots per Spell Level","colLabels":["1st"],"rowsSpellProgression":[[0],[0],[2]]}],
               "subclassFeatures":["Eldritch Knight|Fighter||Eldritch Knight||3"]}],
             "subclassFeature":[
              {"name":"School of Evocation","source":"PHB","page":117,"className":"Wizard","classSource":"PHB",
               "subclassShortName":"Evocation","subclassSource":"PHB","level":2,
               "entries":["You focus your study on evocation.",
                 {"type":"refSubclassFeature","subclassFeature":"Evocation Savant|Wizard||Evocation||2"}]},
              {"name":"Evocation Savant","source":"PHB","page":117,"className":"Wizard","classSource":"PHB",
               "subclassShortName":"Evocation","subclassSource":"PHB","level":2,"header":1,"entries":["Copying costs are halved."]},
              {"name":"Potent Cantrip","source":"PHB","page":117,"className":"Wizard","classSource":"PHB",
               "subclassShortName":"Evocation","subclassSource":"PHB","level":6,"header":2,"entries":["Half damage on a save."]},
              {"name":"Eldritch Knight","source":"PHB","page":74,"className":"Fighter","classSource":"PHB",
               "subclassShortName":"Eldritch Knight","subclassSource":"PHB","level":3,"entries":["You combine magic and martial skill."]}]}
            """;
}
