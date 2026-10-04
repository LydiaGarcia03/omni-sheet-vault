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

/** Fixtures are trimmed copies of real 5etools races.json entries (Dwarf, Human, Dragonborn, Aarakocra). */
class SpeciesConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpeciesConverter converter = new SpeciesConverter(objectMapper);

    private Path dataRoot;

    @BeforeEach
    void createDataRoot() throws IOException {
        dataRoot = Files.createTempDirectory("species-converter-test");
        Files.writeString(dataRoot.resolve("books.json"), """
                {"book":[{"name":"Player's Handbook (2014)","id":"PHB","source":"PHB"}]}""");
        Files.writeString(dataRoot.resolve("adventures.json"), "{\"adventure\":[]}");
        Files.writeString(dataRoot.resolve("races.json"), RACES);
    }

    @Test
    void dropsThe2024EditionAndKeepsSubracesOnlyForKeptRaces() {
        List<JsonNode> species = load();

        assertThat(species).extracting(race -> race.get("name").asString())
                .containsExactly("Dwarf", "Human", "Dragonborn", "Aarakocra");
    }

    @Test
    void mapsCoreSpeciesMechanicsWithSheetKeys() {
        JsonNode data = convert("Dwarf").data();

        assertThat(data.get("size")).extracting(JsonNode::asString).containsExactly("medium");
        assertThat(data.get("speed").get("walk").asInt()).isEqualTo(25);
        assertThat(data.get("senses").get(0).get("type").asString()).isEqualTo("darkvision");
        assertThat(data.get("abilityAlternatives").get(0).get("fixed").get("constitution").asInt()).isEqualTo(2);
        assertThat(data.get("damageResistances").get("fixed")).extracting(JsonNode::asString).containsExactly("poison");
        assertThat(data.get("weaponAlternatives").get(0).get("fixed")).extracting(JsonNode::asString)
                .containsExactly("battleaxe", "handaxe");
        assertThat(data.get("toolAlternatives").get(0).get("choices").get(0).get("from")).extracting(JsonNode::asString)
                .containsExactly("smiths-tools", "brewers-supplies");
        assertThat(data.get("traits")).extracting(trait -> trait.get("name").asString())
                .containsExactly("Darkvision", "Dwarven Resilience");
    }

    @Test
    void nestsSubspeciesWithTheirOwnMechanicsAndOverwrites() {
        JsonNode subspecies = convert("Dwarf").data().get("subspecies");

        assertThat(subspecies).hasSize(1);
        JsonNode mountain = subspecies.get(0);
        assertThat(mountain.get("name").asString()).isEqualTo("Mountain");
        assertThat(mountain.get("sourceBook").asString()).isEqualTo("Player's Handbook");
        assertThat(mountain.get("abilityAlternatives").get(0).get("fixed").get("strength").asInt()).isEqualTo(2);
        assertThat(mountain.get("armorAlternatives").get(0).get("fixed")).extracting(JsonNode::asString)
                .containsExactly("light", "medium");
        assertThat(mountain.get("speed").isNull()).isTrue();
        assertThat(mountain.get("overwrite")).extracting(JsonNode::asString).containsExactly("languageAlternatives");
    }

    @Test
    void keepsTheUnnamedDefaultSubspeciesAndChoiceBasedOnes() {
        JsonNode subspecies = convert("Human").data().get("subspecies");

        assertThat(subspecies.get(0).get("name").isNull()).isTrue();
        assertThat(subspecies.get(0).get("abilityAlternatives").get(0).get("fixed").get("charisma").asInt()).isEqualTo(1);
        JsonNode variant = subspecies.get(1);
        assertThat(variant.get("abilityAlternatives").get(0).get("choices").get(0).get("count").asInt()).isEqualTo(2);
        assertThat(variant.get("featAlternatives").get(0).get("choices").get(0).get("category").asString()).isEqualTo("any");
        assertThat(variant.get("skillAlternatives").get(0).get("choices").get(0).get("category").asString()).isEqualTo("any");
    }

    @Test
    void expandsSubspeciesVersionsAgainstTheCombinedSpeciesText() {
        JsonNode variants = convert("Dragonborn").data().get("subspecies").get(0).get("variants");

        assertThat(variants).extracting(variant -> variant.get("name").asString()).containsExactly("Dragonborn (Black)");
        JsonNode black = variants.get(0);
        assertThat(black.get("damageResistances").get("fixed")).extracting(JsonNode::asString).containsExactly("acid");
        assertThat(black.get("traits")).extracting(trait -> trait.get("name").asString()).containsExactly("Breath Weapon");
        assertThat(black.get("traits").get(0).get("description").asString()).isEqualTo("acid breath");
    }

    @Test
    void resolvesAnotherMovementTypeEqualToWalkingSpeed() {
        JsonNode speed = convert("Aarakocra").data().get("speed");

        assertThat(speed.get("walk").asInt()).isEqualTo(25);
        assertThat(speed.get("fly").asInt()).isEqualTo(25);
    }

    private List<JsonNode> load() {
        return converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper));
    }

    private CatalogueEntryImport convert(String name) {
        return converter.convert(load().stream().filter(race -> name.equals(race.get("name").asString())).findFirst().orElseThrow());
    }

    private static final String RACES = """
            {"race":[
              {"name":"Dwarf","source":"PHB","page":18,"size":["M"],"speed":25,"ability":[{"con":2}],"darkvision":60,
               "languageProficiencies":[{"common":true,"dwarvish":true}],
               "toolProficiencies":[{"choose":{"from":["smith's tools","brewer's supplies"]}}],
               "weaponProficiencies":[{"battleaxe|phb":true,"handaxe|phb":true}],"resist":["poison"],
               "entries":[{"type":"entries","name":"Darkvision","entries":["You see in the dark."]},
                          {"type":"entries","name":"Dwarven Resilience","entries":["Poison resistance."]}]},
              {"name":"Dwarf","source":"XPHB","edition":"one","size":["M"],"speed":30,"entries":[]},
              {"name":"Human","source":"PHB","page":29,"size":["M"],"speed":30,"entries":[]},
              {"name":"Dragonborn","source":"PHB","page":32,"size":["M"],"speed":30,
               "entries":[{"type":"entries","name":"Draconic Ancestry","entries":["Pick a dragon."]}]},
              {"name":"Aarakocra","source":"EEPC","page":5,"size":["M"],"speed":{"walk":25,"fly":true},"entries":[]}],
             "subrace":[
              {"name":"Mountain","source":"PHB","raceName":"Dwarf","raceSource":"PHB","page":20,"ability":[{"str":2}],
               "armorProficiencies":[{"light":true,"medium":true}],"overwrite":{"languageProficiencies":true},"entries":[]},
              {"name":"Hill","source":"XPHB","raceName":"Dwarf","raceSource":"XPHB","ability":[{"wis":1}],"entries":[]},
              {"source":"PHB","raceName":"Human","raceSource":"PHB","ability":[{"str":1,"dex":1,"con":1,"int":1,"wis":1,"cha":1}]},
              {"name":"Variant","source":"PHB","raceName":"Human","raceSource":"PHB",
               "ability":[{"choose":{"from":["str","dex","con","int","wis","cha"],"count":2}}],
               "skillProficiencies":[{"any":1}],"feats":[{"any":1}],"entries":[]},
              {"source":"PHB","raceName":"Dragonborn","raceSource":"PHB",
               "_versions":[{"_abstract":{"name":"Dragonborn ({{color}})","source":"PHB","_mod":{"entries":[
                 {"mode":"removeArr","names":"Draconic Ancestry"},
                 {"mode":"appendArr","items":{"type":"entries","name":"Breath Weapon","entries":["{{damageType}} breath"]}}]}},
                 "_implementations":[{"_variables":{"color":"Black","damageType":"acid"},"resist":["acid"]}]}]}]}
            """;
}
