package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class Dnd5eFeatureUsesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void laterFeaturesAddToOrRaiseTheFirstFeaturesResource() {
        Dnd5eFeatureUses uses = new Dnd5eFeatureUses(List.of(
                feature("combat-superiority", "{\"resource\":\"superiority-dice\",\"count\":4,\"die\":8,\"recharge\":\"SHORT_OR_LONG_REST\"}"),
                feature("additional-7", "{\"resource\":\"superiority-dice\",\"add\":1}"),
                feature("improved-d10", "{\"resource\":\"superiority-dice\",\"die\":10}"),
                feature("additional-15", "{\"resource\":\"superiority-dice\",\"add\":1}"),
                feature("action-surge", "{\"resource\":\"action-surge\",\"count\":1,\"recharge\":\"SHORT_OR_LONG_REST\"}"),
                feature("action-surge-two", "{\"resource\":\"action-surge\",\"count\":2}")), List.of());

        assertThat(uses.ownedBy("combat-superiority"))
                .isEqualTo(new Dnd5eFeatureUses.Resource("combat-superiority", 6, Dnd5eRechargeTrigger.SHORT_OR_LONG_REST, 10));
        assertThat(uses.ownedBy("action-surge").maxUses()).isEqualTo(2);
        assertThat(uses.ownedBy("action-surge-two")).isNull();
        assertThat(uses.counterKeyFor("action-surge-two")).isEqualTo("action-surge");
    }

    @Test
    void aTableColumnCountReadsTheClassTableAtTheLevelReached() {
        CatalogueRecord sorcerer = new CatalogueRecord("CLASS", "sorcerer", "Sorcerer", "PHB", 1, "", MAPPER.readTree("""
                {"tableColumns":[{"label":"Sorcery Points","valuesByLevel":["0","2","3","4"]}]}"""));
        Dnd5eBuildOutcome.ClassOutcome taken = new Dnd5eBuildOutcome.ClassOutcome(sorcerer, new Dnd5eBuildClass("sorcerer", null, 3), null);
        Dnd5eBuildOutcome.FeatureOutcome fontOfMagic = new Dnd5eBuildOutcome.FeatureOutcome("font", "Font of Magic",
                Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Sorcerer 2", "", MAPPER.readTree("""
                {"uses":{"resource":"sorcery-points","fromTableColumn":"Sorcery Points","recharge":"LONG_REST"}}"""), "sorcerer");

        Dnd5eFeatureUses uses = new Dnd5eFeatureUses(List.of(fontOfMagic), List.of(taken));

        assertThat(uses.ownedBy("font").maxUses()).isEqualTo(3);
        assertThat(uses.ownedBy("font").recharge()).isEqualTo(Dnd5eRechargeTrigger.LONG_REST);
    }

    @Test
    void aResourceWithNoUsesYetHasNoCounter() {
        Dnd5eFeatureUses uses = new Dnd5eFeatureUses(List.of(feature("cunning-action", "null")), List.of());

        assertThat(uses.ownedBy("cunning-action")).isNull();
        assertThat(uses.counterKeyFor("cunning-action")).isNull();
    }

    private static Dnd5eBuildOutcome.FeatureOutcome feature(String key, String usesJson) {
        return new Dnd5eBuildOutcome.FeatureOutcome(key, key, Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Fighter", "",
                MAPPER.readTree("{\"uses\":" + usesJson + "}"), "fighter");
    }
}
