package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures are trimmed copies of the real 5etools entries they are named after (DMG). */
class ItemMechanicsMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemConverter converter = new ItemConverter(objectMapper);

    @Test
    void cloakOfProtectionAddsToArmorClassAndSavingThrows() {
        JsonNode mechanics = mechanics("""
                {"name":"Cloak of Protection","source":"DMG","page":159,"type":"W","rarity":"uncommon","reqAttune":true,
                 "wondrous":true,"bonusAc":"+1","bonusSavingThrow":"+1","entries":["You gain a +1 bonus to AC and saving throws."]}""");

        assertThat(mechanics.get("modifiers")).hasSize(2);
        assertThat(modifier(mechanics, 0)).isEqualTo("BONUS ARMOR_CLASS 1");
        assertThat(modifier(mechanics, 1)).isEqualTo("BONUS SAVING_THROWS 1");
    }

    @Test
    void armorKeepsItsOwnArmorClassBonusOutOfTheModifiers() {
        JsonNode mechanics = mechanics("""
                {"name":"Test Plate +1","source":"DMG","page":1,"type":"HA","rarity":"rare","ac":18,"bonusAc":"+1","armor":true}""");

        assertThat(mechanics.isNull()).isTrue();
    }

    @Test
    void amuletOfHealthSetsConstitution() {
        JsonNode mechanics = mechanics("""
                {"name":"Amulet of Health","source":"DMG","page":150,"type":"W","rarity":"rare","reqAttune":true,
                 "wondrous":true,"ability":{"static":{"con":19}}}""");

        assertThat(modifier(mechanics, 0)).isEqualTo("SET CONSTITUTION_SCORE 19");
    }

    @Test
    void anAdditiveAbilityScoreIsLeftOut() {
        JsonNode mechanics = mechanics("""
                {"name":"Ioun Stone, Fortitude","source":"DMG","page":176,"type":"W","rarity":"very rare","reqAttune":true,
                 "wondrous":true,"ability":{"con":2}}""");

        assertThat(mechanics.isNull()).isTrue();
    }

    @Test
    void aPotionGetsNoMechanics() {
        JsonNode mechanics = mechanics("""
                {"name":"Potion of Hill Giant Strength","source":"DMG","page":187,"type":"P","rarity":"uncommon",
                 "ability":{"static":{"str":21}}}""");

        assertThat(mechanics.isNull()).isTrue();
    }

    @Test
    void spellcastingFocusBonusesAndDefensesAreMapped() {
        JsonNode mechanics = mechanics("""
                {"name":"Test Focus","source":"TCE","page":1,"type":"SCF","rarity":"uncommon","reqAttune":true,
                 "bonusSpellAttack":"+1","bonusSpellSaveDc":"+1","resist":["fire",{"special":"conditional"}],
                 "conditionImmune":"frightened"}""");

        assertThat(modifier(mechanics, 0)).isEqualTo("BONUS SPELL_ATTACKS 1");
        assertThat(modifier(mechanics, 1)).isEqualTo("BONUS SPELL_SAVE_DC 1");
        assertThat(mechanics.get("damageResistances")).extracting(JsonNode::asString).containsExactly("fire");
        assertThat(mechanics.get("conditionImmunities")).extracting(JsonNode::asString).containsExactly("frightened");
    }

    private JsonNode mechanics(String json) {
        return converter.convert(objectMapper.readTree(json)).data().get("mechanics");
    }

    private static String modifier(JsonNode mechanics, int index) {
        JsonNode modifier = mechanics.get("modifiers").get(index);
        return modifier.get("type").asString() + " " + modifier.get("target").asString() + " " + modifier.get("value").asInt();
    }
}
