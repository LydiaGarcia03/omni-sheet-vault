package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures are trimmed copies of real 5etools class-*.json entries (Wizard, Warlock, Fighter). */
class ClassConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClassConverter converter = new ClassConverter(objectMapper, FiveEToolsMechanicsOverlay.empty());

    private Path dataRoot;

    @BeforeEach
    void createDataRoot() throws IOException {
        dataRoot = Files.createTempDirectory("class-converter-test");
        Files.createDirectories(dataRoot.resolve("class"));
        Files.writeString(dataRoot.resolve("books.json"), """
                {"book":[{"name":"Tasha's Cauldron of Everything","id":"TCE","source":"TCE"}]}""");
        Files.writeString(dataRoot.resolve("adventures.json"), "{\"adventure\":[]}");
    }

    @Test
    void dropsThe2024EditionAndSidekickClasses() throws IOException {
        writeClassFile("class-wizard.json", WIZARD_FILE);

        List<JsonNode> classes = converter.loadRawEntries(dataSource());

        assertThat(classes).extracting(playerClass -> playerClass.get("source").asString()).containsExactly("PHB");
    }

    @Test
    void mapsCoreClassFields() throws IOException {
        JsonNode data = convertOnly(WIZARD_FILE).data();

        assertThat(data.get("hitDie").asInt()).isEqualTo(6);
        assertThat(data.get("savingThrows")).extracting(JsonNode::asString).containsExactly("intelligence", "wisdom");
        assertThat(data.get("subclassTitle").asString()).isEqualTo("Arcane Tradition");
        assertThat(data.get("subclassLevel").asInt()).isEqualTo(2);
        assertThat(data.get("abilityScoreImprovementLevels")).extracting(JsonNode::asInt).containsExactly(4);
        assertThat(data.get("multiclassing").get("allOf").get("intelligence").asInt()).isEqualTo(13);
    }

    @Test
    void mapsProficienciesWithSheetKeysAndItemSlugs() throws IOException {
        JsonNode proficiencies = convertOnly(WIZARD_FILE).data().get("proficiencies");

        assertThat(proficiencies.get("weaponItems")).extracting(JsonNode::asString)
                .containsExactly("dagger", "light-crossbow");
        JsonNode choose = proficiencies.get("skillAlternatives").get(0).get("choices").get(0);
        assertThat(choose.get("from")).extracting(JsonNode::asString).containsExactly("arcana", "sleightOfHand");
        assertThat(choose.get("count").asInt()).isEqualTo(2);
    }

    @Test
    void mapsSpellcastingProgressionAndPreparedFormula() throws IOException {
        JsonNode spellcasting = convertOnly(WIZARD_FILE).data().get("spellcasting");

        assertThat(spellcasting.get("ability").asString()).isEqualTo("intelligence");
        assertThat(spellcasting.get("casterProgression").asString()).isEqualTo("full");
        assertThat(spellcasting.get("preparedSpellsFormula").get("levelDivisor").asInt()).isEqualTo(1);
        assertThat(spellcasting.get("preparedSpellsFormula").get("ability").asString()).isEqualTo("intelligence");
        assertThat(spellcasting.get("spellSlotsByLevel").get(2).get(1).asInt()).isEqualTo(2);
        assertThat(spellcasting.get("pactSlotsByLevel").isNull()).isTrue();
    }

    @Test
    void resolvesFeaturesInOrderAndFlagsOptionalAndSubclassFeatures() throws IOException {
        JsonNode features = convertOnly(WIZARD_FILE).data().get("features");

        assertThat(features).extracting(feature -> feature.get("name").asString())
                .containsExactly("Arcane Recovery", "Arcane Tradition", "Cantrip Formulas", "Ability Score Improvement");
        assertThat(features.get(1).get("grantsSubclassFeature").asBoolean()).isTrue();
        assertThat(features.get(2).get("optional").asBoolean()).isTrue();
        assertThat(features.get(2).get("sourceBook").asString()).isEqualTo("Tasha's Cauldron of Everything");
        assertThat(features.get(0).get("description").asString())
                .isEqualTo("You can regain some of your magical energy. "
                        + "Spell save DC = 8 + your proficiency bonus + your Intelligence modifier");
    }

    @Test
    void mapsStartingEquipmentChoicesAndCategories() throws IOException {
        JsonNode equipment = convertOnly(WIZARD_FILE).data().get("startingEquipment");

        assertThat(equipment.get("goldAlternative").asString()).isEqualTo("4d4 × 10");
        JsonNode firstGroup = equipment.get("groups").get(0).get("options");
        assertThat(firstGroup.get(0).get("label").asString()).isEqualTo("a");
        assertThat(firstGroup.get(0).get("grants").get(0).get("itemSlug").asString()).isEqualTo("quarterstaff");
        assertThat(firstGroup.get(1).get("grants").get(0).get("equipmentType").asString()).isEqualTo("focusSpellcastingArcane");
        JsonNode fixedGroup = equipment.get("groups").get(1).get("options").get(0);
        assertThat(fixedGroup.get("label").isNull()).isTrue();
        assertThat(fixedGroup.get("grants").get(0).get("quantity").asInt()).isEqualTo(2);
    }

    @Test
    void mapsPactMagicSlotsFromTheClassTable() throws IOException {
        JsonNode data = convertOnly(WARLOCK_FILE).data();

        JsonNode pactSlots = data.get("spellcasting").get("pactSlotsByLevel");
        assertThat(pactSlots.get(0).get("slots").asInt()).isEqualTo(1);
        assertThat(pactSlots.get(2).get("slotLevel").asInt()).isEqualTo(2);
        assertThat(data.get("spellcasting").get("spellSlotsByLevel").isNull()).isTrue();
        assertThat(data.get("tableColumns").get(1).get("valuesByLevel")).extracting(JsonNode::asString)
                .containsExactly("1st", "1st", "2nd");
    }

    @Test
    void mapsOptionalFeatureProgressionsTableCellsAndAlternativeMulticlassRequirements() throws IOException {
        JsonNode data = convertOnly(FIGHTER_FILE).data();

        JsonNode fightingStyle = data.get("optionalFeatureProgressions").get(0);
        assertThat(fightingStyle.get("countByLevel")).hasSize(20);
        assertThat(fightingStyle.get("countByLevel").get(0).asInt()).isEqualTo(1);
        assertThat(fightingStyle.get("countByLevel").get(19).asInt()).isEqualTo(1);
        assertThat(data.get("features").get(0).get("optionalFeatureOptions")).extracting(option -> option.get("name").asString())
                .containsExactly("Archery", "Blind Fighting");
        assertThat(data.get("features").get(0).get("optionalFeatureOptions").get(1).get("source").asString()).isEqualTo("TCE");
        assertThat(data.get("multiclassing").get("anyOf").get("strength").asInt()).isEqualTo(13);
        assertThat(data.get("multiclassing").get("anyOf").get("dexterity").asInt()).isEqualTo(13);
        assertThat(data.get("tableColumns").get(0).get("valuesByLevel")).extracting(JsonNode::asString)
                .containsExactly("1d6", "+2", "—");
    }

    @Test
    void failsOnAnUnsupportedPreparedSpellsFormula() throws IOException {
        writeClassFile("class-wizard.json", WIZARD_FILE.replace("<$level$> + <$int_mod$>", "<$level$> * 3"));

        assertThatThrownBy(() -> converter.convert(converter.loadRawEntries(dataSource()).getFirst()))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("prepared-spells formula");
    }

    @Test
    void failsOnAnUnresolvableFeatureReference() throws IOException {
        writeClassFile("class-wizard.json", WIZARD_FILE.replace("\"Arcane Recovery|Wizard||1\"", "\"Missing|Wizard||1\""));

        assertThatThrownBy(() -> converter.loadRawEntries(dataSource()))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("Missing|Wizard||1");
    }

    private CatalogueEntryImport convertOnly(String fileContent) throws IOException {
        writeClassFile("class-under-test.json", fileContent);
        List<JsonNode> classes = converter.loadRawEntries(dataSource());
        assertThat(classes).hasSize(1);
        return converter.convert(classes.getFirst());
    }

    private FiveEToolsDataSource dataSource() {
        return new FiveEToolsDataSource(dataRoot, objectMapper);
    }

    private void writeClassFile(String fileName, String content) throws IOException {
        Files.writeString(dataRoot.resolve("class").resolve(fileName), content);
    }

    private static final String WIZARD_FILE = """
            {"class":[
              {"name":"Wizard","source":"PHB","page":112,"edition":"classic","hd":{"number":1,"faces":6},
               "proficiency":["int","wis"],"spellcastingAbility":"int","casterProgression":"full",
               "preparedSpells":"<$level$> + <$int_mod$>","cantripProgression":[3,3,3],
               "spellsKnownProgressionFixed":[6,2,2],
               "startingProficiencies":{"skills":[{"choose":{"from":["arcana","sleight of hand"],"count":2}}],
                 "weapons":["{@item dagger|phb|daggers}","{@item light crossbow|phb|light crossbows}"]},
               "startingEquipment":{"additionalFromBackground":true,"default":["(a) a {@item quarterstaff|phb}"],
                 "goldAlternative":"{@dice 4d4 × 10|4d4 × 10|Starting Gold}",
                 "defaultData":[{"a":["quarterstaff|phb"],"b":[{"equipmentType":"focusSpellcastingArcane"}]},
                                {"_":[{"item":"dagger|phb","quantity":2}]}]},
               "multiclassing":{"requirements":{"int":13}},
               "classTableGroups":[
                 {"colLabels":["{@filter Cantrips Known|spells|level=0|class=Wizard}"],"rows":[[3],[3],[3]]},
                 {"title":"Spell Slots per Spell Level","colLabels":["{@filter 1st|spells|level=1|class=Wizard}","{@filter 2nd|spells|level=2|class=Wizard}"],
                  "rowsSpellProgression":[[2,0],[3,0],[4,2]]}],
               "classFeatures":["Arcane Recovery|Wizard||1",{"classFeature":"Arcane Tradition|Wizard||2","gainSubclassFeature":true},
                 "Cantrip Formulas|Wizard||3|TCE","Ability Score Improvement|Wizard||4"],
               "subclassTitle":"Arcane Tradition"},
              {"name":"Wizard","source":"XPHB","edition":"one","hd":{"number":1,"faces":6},"classFeatures":[]},
              {"name":"Expert Sidekick","source":"TCE","isSidekick":true,"classFeatures":[]}],
             "classFeature":[
              {"name":"Arcane Recovery","source":"PHB","page":115,"className":"Wizard","classSource":"PHB","level":1,
               "entries":["You can regain some of your {@i magical} energy.",{"type":"abilityDc","name":"Spell","attributes":["int"]}]},
              {"name":"Arcane Tradition","source":"PHB","page":115,"className":"Wizard","classSource":"PHB","level":2,
               "entries":["You choose an arcane tradition."]},
              {"name":"Cantrip Formulas","source":"TCE","page":75,"className":"Wizard","classSource":"PHB","level":3,
               "isClassFeatureVariant":true,"entries":["You can replace one wizard cantrip."]},
              {"name":"Ability Score Improvement","source":"PHB","page":115,"className":"Wizard","classSource":"PHB","level":4,
               "entries":["You can increase one ability score."]}]}
            """;

    private static final String WARLOCK_FILE = """
            {"class":[
              {"name":"Warlock","source":"PHB","page":105,"edition":"classic","hd":{"number":1,"faces":8},
               "proficiency":["wis","cha"],"spellcastingAbility":"cha","casterProgression":"pact",
               "spellsKnownProgression":[2,3,4],
               "startingEquipment":{"default":[],"defaultData":[]},
               "classTableGroups":[{"colLabels":["Spell Slots","Slot Level"],
                 "rows":[[1,"{@filter 1st|spells|level=1|class=Warlock}"],[2,"{@filter 1st|spells|level=1|class=Warlock}"],
                         [2,"{@filter 2nd|spells|level=2|class=Warlock}"]]}],
               "classFeatures":[],"subclassTitle":"Otherworldly Patron"}]}
            """;

    private static final String FIGHTER_FILE = """
            {"class":[
              {"name":"Fighter","source":"PHB","page":70,"edition":"classic","hd":{"number":1,"faces":10},
               "proficiency":["str","con"],
               "startingEquipment":{"default":[],"defaultData":[]},
               "multiclassing":{"requirements":{"or":[{"str":13,"dex":13}]}},
               "optionalfeatureProgression":[{"name":"Fighting Style","featureType":["FS:F"],"progression":{"1":1}}],
               "classTableGroups":[{"colLabels":["Test Column"],
                 "rows":[[{"type":"dice","toRoll":[{"number":1,"faces":6}]}],[{"type":"bonus","value":2}],
                         [{"type":"bonusSpeed","value":0}]]}],
               "classFeatures":["Fighting Style|Fighter||1"],"subclassTitle":"Martial Archetype"}],
             "classFeature":[
              {"name":"Fighting Style","source":"PHB","page":72,"className":"Fighter","classSource":"PHB","level":1,
               "entries":["Choose one of the following options.",{"type":"options","count":1,"entries":[
                 {"type":"refOptionalfeature","optionalfeature":"Archery"},
                 {"type":"refOptionalfeature","optionalfeature":"Blind Fighting|TCE"}]}]}]}
            """;
}
