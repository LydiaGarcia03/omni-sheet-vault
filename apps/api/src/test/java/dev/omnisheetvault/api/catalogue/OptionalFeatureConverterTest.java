package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures are trimmed copies of real 5etools optionalfeatures.json entries. */
class OptionalFeatureConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OptionalFeatureConverter converter = new OptionalFeatureConverter(objectMapper);

    @Test
    void convertsAFightingStyleSharedByThreeClasses() {
        CatalogueEntryImport imported = convert("""
                {"name":"Defense","source":"PHB","page":72,"featureType":["FS:F","FS:P","FS:R"],
                 "entries":["While you are wearing armor, you gain a +1 bonus to AC."]}""");

        assertThat(imported.kind()).isEqualTo(CatalogueEntryKind.OPTIONAL_FEATURE);
        assertThat(imported.slug()).isEqualTo("defense");
        assertThat(imported.description()).isEqualTo("While you are wearing armor, you gain a +1 bonus to AC.");
        assertThat(imported.data().get("featureTypes")).extracting(JsonNode::asString).containsExactly("FS:F", "FS:P", "FS:R");
        assertThat(imported.data().get("prerequisiteText").isNull()).isTrue();
    }

    @Test
    void rendersPrerequisitesTheWay5etoolsDoes() {
        CatalogueEntryImport imported = convert("""
                {"name":"Relentless Hex","source":"XGE","page":57,"featureType":["EI"],
                 "prerequisite":[{"level":{"level":7,"class":{"name":"Warlock"}},"spell":["hex/curse#x"]}],
                 "entries":["Your curse creates a temporary bond."]}""");

        assertThat(imported.data().get("prerequisiteText").asString())
                .isEqualTo("7th level, hex spell or a warlock feature that curses");
        assertThat(imported.description()).startsWith("Prerequisite: 7th level, hex spell or a warlock feature that curses\n\n");
        assertThat(imported.data().get("prerequisites").get(0).get("level").get("level").asInt()).isEqualTo(7);
    }

    @Test
    void rendersPactAndCantripPrerequisites() {
        assertThat(prerequisiteOf("""
                [{"pact":"Chain","level":{"level":15,"class":{"name":"Warlock"}}}]""")).isEqualTo("15th level, Pact of the Chain");
        assertThat(prerequisiteOf("""
                [{"spell":["eldritch blast#c"]}]""")).isEqualTo("eldritch blast cantrip");
    }

    @Test
    void mapsSensesConsumptionAndGrantedPicks() {
        JsonNode data = convert("""
                {"name":"Superior Technique","source":"TCE","page":41,"featureType":["FS:F"],"isClassFeatureVariant":true,
                 "senses":[{"blindsight":10}],"consumes":{"name":"Superiority Die"},
                 "optionalfeatureProgression":[{"name":"Maneuvers","featureType":["MV:B"],"progression":{"*":1}}],
                 "entries":["You learn one maneuver."]}""").data();

        assertThat(data.get("optional").asBoolean()).isTrue();
        assertThat(data.get("senses").get(0).get("type").asString()).isEqualTo("blindsight");
        assertThat(data.get("senses").get(0).get("range").asInt()).isEqualTo(10);
        assertThat(data.get("consumes").get("name").asString()).isEqualTo("Superiority Die");
        assertThat(data.get("optionalFeatureProgressions").get(0).get("countByLevel").get(0).asInt()).isEqualTo(1);
    }

    @Test
    void failsOnAnUnknownPrerequisiteKey() {
        assertThatThrownBy(() -> prerequisiteOf("""
                [{"race":[{"name":"elf"}]}]"""))
                .isInstanceOf(FiveEToolsIngestException.class);
    }

    private String prerequisiteOf(String prerequisiteJson) {
        return FiveEToolsPrerequisites.text(objectMapper.readTree(prerequisiteJson));
    }

    private CatalogueEntryImport convert(String json) {
        return converter.convert(objectMapper.readTree(json));
    }
}
