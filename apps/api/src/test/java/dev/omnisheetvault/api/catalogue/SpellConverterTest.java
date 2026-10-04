package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Fixtures are hand-built to match this converter's own assumptions about 5etools'
 * JSON shape (not real book text — same precedent as CatalogueImportServiceTest's own
 * note). Field-shape assumptions still need confirming against the real download once
 * the owner runs slice 3 — see systems/dnd-5e/features/5etools-ingestion.md.
 */
class SpellConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpellConverter converter = new SpellConverter(objectMapper);

    @Test
    void convertsAnAttackRollDamageCantrip() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Bolt","source":"PHB","page":42,"level":0,"school":"V",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"feet","amount":120}},
                "components":{"v":true,"s":true},
                "duration":[{"type":"instant"}],
                "spellAttack":["R"],"damageInflict":["fire"],
                "entries":["You hurl a mote of fire. Make a ranged spell attack against the target. On a hit, the target takes {@damage 2d10} fire damage."]}""");

        assertThat(imported.systemId()).isEqualTo("dnd-5e");
        assertThat(imported.kind()).isEqualTo(CatalogueEntryKind.SPELL);
        assertThat(imported.slug()).isEqualTo("test-bolt");
        assertThat(imported.name()).isEqualTo("Test Bolt");
        assertThat(imported.sourceBook()).isEqualTo("PHB");
        assertThat(imported.sourcePage()).isEqualTo(42);
        assertThat(imported.description())
                .isEqualTo("You hurl a mote of fire. Make a ranged spell attack against the target. On a hit, the target takes 2d10 fire damage.");
        assertThat(imported.tagsOrEmpty()).isEmpty();

        JsonNode data = imported.data();
        assertThat(data.get("level").asInt()).isZero();
        assertThat(data.get("school").asString()).isEqualTo("evocation");
        assertThat(data.get("castingTime").asString()).isEqualTo("1 Action");
        assertThat(data.get("range").asString()).isEqualTo("120 ft.");
        assertThat(data.get("concentration").asBoolean()).isFalse();
        assertThat(data.get("ritual").asBoolean()).isFalse();
        assertThat(data.get("attackRoll").asBoolean()).isTrue();
        assertThat(data.get("damageDiceCount").asInt()).isEqualTo(2);
        assertThat(data.get("damageDiceSides").asInt()).isEqualTo(10);
        assertThat(data.get("damageType").asString()).isEqualTo("fire");
        assertThat(data.get("notes").asString()).isEqualTo("V, S");
        assertThat(data.get("effectSummary").asString()).isEqualTo("Damage");
        assertThat(data.get("saveAbility").isNull()).isTrue();
        assertThat(data.get("components").asString()).isEqualTo("V, S");
        assertThat(data.get("materialComponent").isNull()).isTrue();
        assertThat(data.get("duration").asString()).isEqualTo("Instantaneous");
        assertThat(data.get("higherLevelsDescription").isNull()).isTrue();
        assertThat(data.get("higherLevelsDamageDiceCount").isNull()).isTrue();
        assertThat(data.get("higherLevelsDamageDiceSides").isNull()).isTrue();
    }

    @Test
    void attachesClassListsFromSourcesJson() throws Exception {
        Path dataRoot = Files.createTempDirectory("spell-converter-test");
        Files.createDirectories(dataRoot.resolve("spells"));
        Files.writeString(dataRoot.resolve("spells/spells-phb.json"), """
                {"spell":[{"name":"Test Aid","source":"PHB","page":211,"level":2,"school":"A",
                  "time":[{"number":1,"unit":"action"}],"range":{"type":"point","distance":{"type":"feet","amount":30}},
                  "components":{"v":true},"duration":[{"type":"instant"}],"entries":["Allies gain hit points."]},
                 {"name":"Test Unlisted","source":"PHB","page":1,"level":1,"school":"A",
                  "time":[{"number":1,"unit":"action"}],"range":{"type":"point","distance":{"type":"self"}},
                  "components":{"v":true},"duration":[{"type":"instant"}],"entries":["Nothing."]}]}""");
        Files.writeString(dataRoot.resolve("spells/sources.json"), """
                {"PHB":{"Test Aid":{"class":[{"name":"Cleric","source":"PHB"},{"name":"Cleric","source":"XPHB"}],
                  "classVariant":[{"name":"Bard","source":"PHB","definedInSource":"TCE"},
                                  {"name":"Wizard","source":"PHB","definedInSource":"XGE"}]}}}""");
        Files.createDirectories(dataRoot.resolve("generated"));
        Files.writeString(dataRoot.resolve("generated/gendata-spell-source-lookup.json"), """
                {"phb":{"test aid":{"subclass":{
                  "PHB":{"Wizard":{"EGW":{"Chronurgy":{"name":"Chronurgy Magic"}}}},
                  "XPHB":{"Cleric":{"XPHB":{"Life":{"name":"Life Domain"}}}}}}}}""");
        Files.createDirectories(dataRoot.resolve("class"));
        Files.writeString(dataRoot.resolve("class/index.json"), "{}");
        Files.writeString(dataRoot.resolve("class/class-cleric.json"), """
                {"class":[{"name":"Cleric","source":"PHB","edition":"classic"},{"name":"Cleric","source":"XPHB","edition":"one"}]}""");
        Files.writeString(dataRoot.resolve("class/class-bard.json"), """
                {"class":[{"name":"Bard","source":"PHB"}],
                 "classFeature":[{"name":"Magical Inspiration","source":"TCE","className":"Bard","classSource":"PHB","isClassFeatureVariant":true}]}""");

        var raw = converter.loadRawEntries(new FiveEToolsDataSource(dataRoot, objectMapper));
        JsonNode aid = converter.convert(raw.get(0)).data();
        JsonNode unlisted = converter.convert(raw.get(1)).data();

        assertThat(aid.get("classes")).extracting(node -> node.get("name").asString() + "|" + node.get("source").asString())
                .containsExactly("Cleric|PHB", "Wizard|PHB");
        assertThat(aid.get("optionalClasses")).extracting(node -> node.get("name").asString()).containsExactly("Bard");
        assertThat(unlisted.get("classes").isEmpty()).isTrue();
        assertThat(aid.get("sourceCode").asString()).isEqualTo("PHB");
        assertThat(aid.get("subclasses")).extracting(node -> node.get("className").asString() + "/" + node.get("subclassShortName").asString())
                .containsExactly("Wizard/Chronurgy");
    }

    @Test
    void extractsTheAtHigherLevelsTextUsingThePerLevelIncrementNotTheBaseOrTheLevelRange() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Wound","source":"PHB","page":50,"level":1,"school":"N",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"touch"}},
                "components":{"v":true,"s":true},
                "duration":[{"type":"instant"}],
                "damageInflict":["necrotic"],
                "entries":["On a hit, the target takes {@damage 3d10} necrotic damage."],
                "entriesHigherLevel":[{"type":"entries","name":"At Higher Levels",
                "entries":["When you cast this spell using a spell slot of 2nd level or higher, the damage increases by {@scaledamage 3d10|1-9|1d10} for each slot level above 1st."]}]}""");

        JsonNode data = imported.data();
        assertThat(data.get("higherLevelsDescription").asString()).isEqualTo(
                "When you cast this spell using a spell slot of 2nd level or higher, the damage increases by 1d10 for each slot level above 1st.");
        assertThat(data.get("higherLevelsDamageDiceCount").asInt()).isEqualTo(1);
        assertThat(data.get("higherLevelsDamageDiceSides").asInt()).isEqualTo(10);
    }

    @Test
    void extractsPlainAtHigherLevelsTextRegardlessOfItsOwnLabel() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Beasts","source":"XGE","page":51,"level":1,"school":"C",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"feet","amount":30}},
                "components":{"v":true,"s":true},
                "duration":[{"type":"instant"}],
                "entries":["You summon a spirit."],
                "entriesHigherLevel":[{"type":"entries","name":"Using a Higher-Level Spell Slot",
                "entries":["You can affect one additional beast for each slot level above 1st."]}]}""");

        assertThat(imported.data().get("higherLevelsDescription").asString())
                .isEqualTo("You can affect one additional beast for each slot level above 1st.");
    }

    @Test
    void convertsAHealingSpellTaggedByMiscTags() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Cure","source":"PHB","page":43,"level":1,"school":"V",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"touch"}},
                "components":{"v":true,"s":true},
                "duration":[{"type":"instant"}],
                "miscTags":["HL"],
                "entries":["A creature you touch regains {@dice 1d8} hit points."],
                "entriesHigherLevel":[{"type":"entries","name":"At Higher Levels",
                "entries":["When you cast this spell using a spell slot of 2nd level or higher, the healing increases by {@scaledice 1d8|1-9|1d8} for each slot level above 1st."]}]}""");

        assertThat(imported.description()).isEqualTo("A creature you touch regains 1d8 hit points.");
        assertThat(imported.tagsOrEmpty()).containsExactly("Healing");
        JsonNode data = imported.data();
        assertThat(data.get("range").asString()).isEqualTo("Touch");
        assertThat(data.get("damageDiceCount").asInt()).isEqualTo(1);
        assertThat(data.get("damageDiceSides").asInt()).isEqualTo(8);
        assertThat(data.get("damageType").isNull()).isTrue();
        assertThat(data.get("effectSummary").asString()).isEqualTo("Healing");
        assertThat(data.get("higherLevelsDamageDiceCount").asInt()).isEqualTo(1);
        assertThat(data.get("higherLevelsDamageDiceSides").asInt()).isEqualTo(8);
    }

    @Test
    void doesNotFallBackToADiceTagForANonHealingSpell() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Utility","source":"PHB","page":45,"level":1,"school":"D",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"self"}},
                "components":{"v":true,"s":true},
                "duration":[{"type":"instant"}],
                "entries":["Roll {@dice 1d20} to determine a random direction, purely for flavor."]}""");

        JsonNode data = imported.data();
        assertThat(data.get("damageDiceCount").isNull()).isTrue();
        assertThat(data.get("damageDiceSides").isNull()).isTrue();
    }

    @Test
    void convertsAControlSpellThatInflictsACondition() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Slumber","source":"PHB","page":44,"level":1,"school":"E",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"feet","amount":90}},
                "components":{"v":true,"s":true,"m":"a pinch of fine sand"},
                "duration":[{"type":"timed","duration":{"type":"minute","amount":1}}],
                "conditionInflict":["unconscious"],
                "entries":["Creatures fall {@condition unconscious}."]}""");

        assertThat(imported.description()).isEqualTo("Creatures fall unconscious.");
        assertThat(imported.tagsOrEmpty()).containsExactly("Unconscious");
        JsonNode data = imported.data();
        assertThat(data.get("school").asString()).isEqualTo("enchantment");
        assertThat(data.get("components").asString()).isEqualTo("V, S, M");
        assertThat(data.get("materialComponent").asString()).isEqualTo("a pinch of fine sand");
        assertThat(data.get("notes").asString()).isEqualTo("V, S, M (a pinch of fine sand)");
        assertThat(data.get("duration").asString()).isEqualTo("1 minute");
        assertThat(data.get("effectSummary").asString()).isEqualTo("Control");
    }

    @Test
    void convertsAConcentrationRitualSpellWithAnObjectMaterialAndAConeRange() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Aura","source":"PHB","page":46,"level":3,"school":"A",
                "time":[{"number":1,"unit":"bonus"}],
                "range":{"type":"cone","distance":{"type":"feet","amount":15}},
                "components":{"v":true,"s":true,"m":{"text":"a vial of holy water","cost":"25gp"}},
                "duration":[{"type":"timed","duration":{"type":"minute","amount":10,"upTo":true},"concentration":true}],
                "meta":{"ritual":true},
                "entries":["You radiate protective energy."]}""");

        JsonNode data = imported.data();
        assertThat(data.get("castingTime").asString()).isEqualTo("1 Bonus Action");
        assertThat(data.get("range").asString()).isEqualTo("Self (15-foot cone)");
        assertThat(data.get("materialComponent").asString()).isEqualTo("a vial of holy water");
        assertThat(data.get("duration").asString()).isEqualTo("Concentration, up to 10 minutes");
        assertThat(data.get("concentration").asBoolean()).isTrue();
        assertThat(data.get("notes").asString()).isEqualTo("Concentration, up to 10 minutes, V, S, M (a vial of holy water)");
        assertThat(data.get("ritual").asBoolean()).isTrue();
        assertThat(data.get("effectSummary").asString()).isEqualTo("Buff");
    }

    @Test
    void slugifiesAnApostropheOutOfTheName() {
        CatalogueEntryImport imported = convert("""
                {"name":"Tasha's Test Spell","source":"PHB","page":1,"level":1,"school":"A",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"self"}},
                "components":{"v":true},
                "duration":[{"type":"instant"}],
                "entries":["Placeholder."]}""");

        assertThat(imported.slug()).isEqualTo("tashas-test-spell");
    }

    @Test
    void fallsBackToTheRawSourceCodeWhenNoDisplayNameIsKnown() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Obscure","source":"XYZ","page":1,"level":1,"school":"A",
                "time":[{"number":1,"unit":"action"}],
                "range":{"type":"point","distance":{"type":"self"}},
                "components":{"v":true},
                "duration":[{"type":"instant"}],
                "entries":["Placeholder."]}""");

        assertThat(imported.sourceBook()).isEqualTo("XYZ");
    }

    private CatalogueEntryImport convert(String rawJson) {
        return converter.convert(objectMapper.readTree(rawJson));
    }
}
