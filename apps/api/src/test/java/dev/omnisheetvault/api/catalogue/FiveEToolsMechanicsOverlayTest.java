package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures are trimmed copies of the real Draconic Bloodline data and the adr-0007 overlay file for it. */
class FiveEToolsMechanicsOverlayTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path dataRoot;
    private Path mechanicsRoot;

    @BeforeEach
    void createDirectories() throws IOException {
        dataRoot = Files.createTempDirectory("overlay-test-data");
        mechanicsRoot = Files.createTempDirectory("overlay-test-mechanics");
        Files.createDirectories(dataRoot.resolve("class"));
        Files.createDirectories(mechanicsRoot.resolve("subclass-features"));
        Files.writeString(dataRoot.resolve("books.json"), "{\"book\":[]}");
        Files.writeString(dataRoot.resolve("adventures.json"), "{\"adventure\":[]}");
        Files.writeString(dataRoot.resolve("class").resolve("class-sorcerer.json"), """
                {"class":[{"name":"Sorcerer","source":"PHB","edition":"classic","classFeatures":[]}],
                 "subclass":[{"name":"Draconic Bloodline","shortName":"Draconic","source":"PHB","className":"Sorcerer","classSource":"PHB",
                   "subclassFeatures":["Dragon Ancestor|Sorcerer||Draconic||1"]}],
                 "subclassFeature":[{"name":"Dragon Ancestor","source":"PHB","page":102,"className":"Sorcerer","classSource":"PHB",
                   "subclassShortName":"Draconic","subclassSource":"PHB","level":1,"entries":["Choose a dragon.",
                   {"type":"table","colLabels":["Dragon","Damage Type"],"rows":[["Black","{@filter Acid|spells|damage type=acid}"],["Red","Fire"]]}]}]}""");
    }

    @Test
    void attachesChoicesToTheMatchingFeatureWithOptionsFromItsOwnTable() throws IOException {
        writeOverlay("sorcerer-draconic-dragon-ancestor.json", "Dragon Ancestor");

        JsonNode feature = subclassFeatures().get(0);

        JsonNode options = feature.get("choices").get(0).get("options");
        assertThat(feature.get("choices").get(0).get("type").asString()).isEqualTo("FEATURE_OPTION");
        assertThat(options).extracting(option -> option.get("key").asString()).containsExactly("black", "red");
        assertThat(options.get(0).get("summary").asString()).isEqualTo("Acid");
    }

    @Test
    void failsWhenAnOverlayMatchesNoIngestedFeature() throws IOException {
        writeOverlay("typo.json", "Dragon Ancestry");

        assertThatThrownBy(this::subclassFeatures)
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("match no ingested entry");
    }

    @Test
    void mergesModifiersIntoAFeatTargetedByNameAndSource() throws IOException {
        Files.writeString(dataRoot.resolve("feats.json"), """
                {"feat":[{"name":"Tough","source":"PHB","page":170,"entries":["Your hit point maximum increases."]},
                         {"name":"Tough","source":"XYZ","page":1,"entries":["Another book's Tough."]}]}""");
        Files.createDirectories(mechanicsRoot.resolve("feats"));
        Files.writeString(mechanicsRoot.resolve("feats").resolve("tough.json"), """
                {"target":{"feat":"Tough","source":"PHB"},"citation":{"sourceBook":"Player's Handbook","page":170},
                 "modifiers":[{"type":"BONUS","target":"HIT_POINTS_PER_LEVEL","value":2,"levelScope":"CHARACTER"}],"choices":[]}""");
        FeatConverter converter = new FeatConverter(objectMapper, FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper));

        var raw = converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper));

        assertThat(converter.convert(raw.get(0)).data().get("modifiers").get(0).get("value").asInt()).isEqualTo(2);
        assertThat(converter.convert(raw.get(1)).data().get("modifiers")).isEmpty();
    }

    @Test
    void mergesALastingEffectIntoASpellTargetedByNameAndSource() throws IOException {
        writeSpells();
        writeSpellOverlay("Mage Armor");
        SpellConverter converter = new SpellConverter(objectMapper, FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper));

        var raw = converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper));

        JsonNode effect = converter.convert(raw.get(0)).data().get("effect");
        assertThat(effect.get("durationText").asString()).isEqualTo("8 hours");
        assertThat(effect.get("modifiers").get(0).get("target").asString()).isEqualTo("UNARMORED_ARMOR_CLASS");
        assertThat(converter.convert(raw.get(1)).data().get("effect").isNull()).isTrue();
    }

    @Test
    void failsWhenASpellOverlayMatchesNoIngestedSpell() throws IOException {
        writeSpells();
        writeSpellOverlay("Mage Armour");
        SpellConverter converter = new SpellConverter(objectMapper, FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper));

        assertThatThrownBy(() -> converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper)))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("match no ingested entry");
    }

    private void writeSpells() throws IOException {
        Files.createDirectories(dataRoot.resolve("spells"));
        Files.writeString(dataRoot.resolve("spells").resolve("spells-phb.json"), """
                {"spell":[{"name":"Mage Armor","source":"PHB","page":256,"level":1,"school":"A",
                  "time":[{"number":1,"unit":"action"}],"range":{"type":"point","distance":{"type":"touch"}},
                  "components":{"v":true,"s":true,"m":"a piece of cured leather"},
                  "duration":[{"type":"timed","duration":{"type":"hour","amount":8}}],
                  "entries":["The target's base AC becomes 13 + its Dexterity modifier."]},
                 {"name":"Light","source":"PHB","page":255,"level":0,"school":"V",
                  "time":[{"number":1,"unit":"action"}],"range":{"type":"point","distance":{"type":"touch"}},
                  "components":{"v":true,"m":"a firefly"},
                  "duration":[{"type":"timed","duration":{"type":"hour","amount":1}}],
                  "entries":["You touch one object."]}]}""");
    }

    private void writeSpellOverlay(String spellName) throws IOException {
        Files.createDirectories(mechanicsRoot.resolve("spells"));
        Files.writeString(mechanicsRoot.resolve("spells").resolve("mage-armor.json"), """
                {"target":{"spell":"%s","source":"PHB"},"citation":{"sourceBook":"Player's Handbook","page":256},
                 "effect":{"modifiers":[{"type":"SET_BASE","target":"UNARMORED_ARMOR_CLASS","value":13}],
                           "endsOnRests":[],"durationText":"8 hours"}}""".formatted(spellName));
    }

    @Test
    void addsAnActionToANamedSpeciesTrait() throws IOException {
        Files.writeString(dataRoot.resolve("races.json"), """
                {"race":[{"name":"Changeling","source":"MPMM","page":10,"size":["M"],"speed":30,
                  "entries":[{"type":"entries","name":"Shapechanger","entries":["You can change your appearance."]},
                             {"type":"entries","name":"Changeling Instincts","entries":["You are proficient in two skills."]}]}],
                 "subrace":[]}""");
        Files.createDirectories(mechanicsRoot.resolve("species"));
        Files.writeString(mechanicsRoot.resolve("species").resolve("changeling-mpmm.json"), """
                {"target":{"species":"Changeling","source":"MPMM"},"citation":{"sourceBook":"MPMM","page":10},
                 "traits":{"Shapechanger":{"modifiers":[],"action":{"type":"ACTION"}}}}""");
        SpeciesConverter converter = new SpeciesConverter(objectMapper, FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper));

        JsonNode traits = converter.convert(converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper)).getFirst())
                .data().get("traits");

        assertThat(traits.get(0).get("action").get("type").asString()).isEqualTo("ACTION");
        assertThat(traits.get(1).has("action")).isFalse();
    }

    @Test
    void failsWhenAFeatOverlayMatchesNoFeat() throws IOException {
        Files.writeString(dataRoot.resolve("feats.json"), """
                {"feat":[{"name":"Tough","source":"PHB","page":170,"entries":["Your hit point maximum increases."]}]}""");
        Files.createDirectories(mechanicsRoot.resolve("feats"));
        Files.writeString(mechanicsRoot.resolve("feats").resolve("tuff.json"), """
                {"target":{"feat":"Tuff","source":"PHB"},"citation":{"sourceBook":"Player's Handbook","page":170},
                 "modifiers":[],"choices":[]}""");
        FeatConverter converter = new FeatConverter(objectMapper, FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper));

        assertThatThrownBy(() -> converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper)))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("feat:tuff|phb");
    }

    private JsonNode subclassFeatures() {
        FiveEToolsMechanicsOverlay overlay = FiveEToolsMechanicsOverlay.load(mechanicsRoot, objectMapper);
        SubclassConverter converter = new SubclassConverter(objectMapper, overlay);
        return converter.convert(converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper)).getFirst())
                .data().get("features");
    }

    private void writeOverlay(String fileName, String featureName) throws IOException {
        Files.writeString(mechanicsRoot.resolve("subclass-features").resolve(fileName), """
                {"target":{"subclassSlug":"sorcerer-draconic","feature":"%s"},
                 "citation":{"sourceBook":"Player's Handbook (2014)","page":102},"modifiers":[],
                 "choices":[{"type":"FEATURE_OPTION","count":1,"optionsFromTable":{"labelColumn":0,"summaryColumn":1}}]}""".formatted(featureName));
    }
}
