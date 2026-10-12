package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fictional creatures in 5etools' bestiary shape, so no book text is copied into the tests. */
class CreatureConverterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final CreatureConverter converter = new CreatureConverter(MAPPER);

    @Test
    void aSimpleBeastBecomesAnExtraShapedStatBlock() {
        CatalogueEntryImport entry = converter.convert(MAPPER.readTree("""
                {"name":"Test Hound","source":"MM","page":999,"size":["S"],"type":"beast","alignment":["U"],
                 "ac":[12],"hp":{"average":5,"formula":"2d4"},"speed":{"walk":40,"climb":30},
                 "str":8,"dex":15,"con":10,"int":3,"wis":12,"cha":6,
                 "skill":{"perception":"+3","sleight of hand":"+4"},"passive":13,"cr":"1/8",
                 "trait":[{"name":"Keen Nose","entries":["It notices {@skill Perception} things."]}],
                 "action":[{"name":"Bite","entries":["{@atk mw} {@hit 4} to hit, reach 5 ft. {@h}3 ({@damage 1d4 + 1}) piercing damage."]}],
                 "environment":["forest"],"familiar":true}"""));
        JsonNode data = entry.data();

        assertThat(entry.kind()).isEqualTo(CatalogueEntryKind.CREATURE);
        assertThat(entry.slug()).isEqualTo("test-hound");
        assertThat(entry.sourcePage()).isEqualTo(999);
        assertThat(entry.tags()).containsExactly("familiar", "forest");
        assertThat(data.get("size").asString()).isEqualTo("Small");
        assertThat(data.get("creatureType").asString()).isEqualTo("Beast");
        assertThat(data.get("alignment").asString()).isEqualTo("unaligned");
        assertThat(data.get("armorClass").asInt()).isEqualTo(12);
        assertThat(data.get("hitPoints").asInt()).isEqualTo(5);
        assertThat(data.get("hitDice").asString()).isEqualTo("2d4");
        assertThat(data.get("speed").asInt()).isEqualTo(40);
        assertThat(data.get("additionalSpeeds").asString()).isEqualTo("Climb 30 ft.");
        assertThat(data.get("abilityScores").get(1).get("abilityKey").asString()).isEqualTo("dexterity");
        assertThat(data.get("abilityScores").get(1).get("save").asInt()).isEqualTo(2);
        assertThat(data.get("skills").get(1).get("name").asString()).isEqualTo("Sleight Of Hand");
        assertThat(data.get("senses").asString()).isEqualTo("passive Perception 13");
        assertThat(data.get("languages").asString()).isEqualTo("—");
        assertThat(data.get("challengeRating").asString()).isEqualTo("1/8");
        assertThat(data.get("traits").get(0).get("description").asString()).isEqualTo("It notices Perception things.");
        assertThat(data.get("actions").get(0).get("description").asString())
                .isEqualTo("Melee Weapon Attack: +4 to hit, reach 5 ft. Hit: 3 (1d4 + 1) piercing damage.");
        assertThat(data.get("familiar").asBoolean()).isTrue();
    }

    @Test
    void aComplexCreatureKeepsItsArmorSourceSavesDefensesSpellsAndLegendaryActions() {
        JsonNode data = converter.convert(MAPPER.readTree("""
                {"name":"Test Drake","source":"MM","page":998,"size":["H"],
                 "type":{"type":"dragon","tags":["shapechanger"]},"alignment":["C","E"],
                 "ac":[{"ac":18,"from":["natural armor"]}],"hp":{"average":200,"formula":"16d12 + 96"},
                 "speed":{"walk":40,"fly":80,"canHover":true},
                 "str":24,"dex":10,"con":22,"int":14,"wis":12,"cha":18,
                 "save":{"dex":"+6","con":"+12"},"senses":["blindsight 60 ft.","{@sense darkvision} 120 ft."],"passive":21,
                 "languages":["Common","Draconic"],"cr":{"cr":"17","lair":"18"},
                 "immune":["fire"],"resist":[{"resist":["bludgeoning","piercing","slashing"],"note":"from nonmagical attacks"}],
                 "conditionImmune":["frightened"],
                 "spellcasting":[{"name":"Innate Spellcasting","headerEntries":["It casts innately."],
                   "will":["{@spell mage hand}"],"daily":{"1e":["{@spell fog cloud}","{@spell invisibility}"]},"ability":"cha"}],
                 "action":[{"name":"Fire Breath {@recharge 5}","entries":["It breathes fire."]}],
                 "reaction":[{"name":"Tail Swipe","entries":["It swipes."]}],
                 "legendary":[{"name":"Wing Attack (Costs 2 Actions)","entries":["It beats its wings."]}]}""")).data();

        assertThat(data.get("creatureType").asString()).isEqualTo("Dragon (shapechanger)");
        assertThat(data.get("alignment").asString()).isEqualTo("chaotic evil");
        assertThat(data.get("armorClass").asInt()).isEqualTo(18);
        assertThat(data.get("armorClassText").asString()).isEqualTo("18 (natural armor)");
        assertThat(data.get("additionalSpeeds").asString()).isEqualTo("Fly 80 ft. (hover)");
        assertThat(data.get("abilityScores").get(0).get("save").asInt()).isEqualTo(7);
        assertThat(data.get("abilityScores").get(1).get("save").asInt()).isEqualTo(6);
        assertThat(data.get("senses").asString()).isEqualTo("blindsight 60 ft., darkvision 120 ft., passive Perception 21");
        assertThat(data.get("languages").asString()).isEqualTo("Common, Draconic");
        assertThat(data.get("challengeRating").asString()).isEqualTo("17");
        assertThat(data.get("damageImmunities").asString()).isEqualTo("fire");
        assertThat(data.get("damageResistances").asString()).isEqualTo("bludgeoning, piercing, and slashing from nonmagical attacks");
        assertThat(data.get("conditionImmunities").asString()).isEqualTo("frightened");
        assertThat(data.get("traits").get(0).get("name").asString()).isEqualTo("Innate Spellcasting");
        assertThat(data.get("traits").get(0).get("description").asString())
                .isEqualTo("It casts innately.\nAt will: mage hand\n1/day each: fog cloud, invisibility");
        assertThat(data.get("actions").get(0).get("name").asString()).isEqualTo("Fire Breath (Recharge 5–6)");
        assertThat(data.get("reactions").get(0).get("name").asString()).isEqualTo("Tail Swipe");
        assertThat(data.get("legendaryActions").get(0).get("name").asString()).isEqualTo("Wing Attack (Costs 2 Actions)");
    }

    @Test
    void alignmentCodesReadAsWords() {
        assertThat(CreatureConverter.Alignments.codes(List.of("A"))).isEqualTo("any alignment");
        assertThat(CreatureConverter.Alignments.codes(List.of("N"))).isEqualTo("neutral");
        assertThat(CreatureConverter.Alignments.codes(List.of("L", "G"))).isEqualTo("lawful good");
        assertThat(CreatureConverter.Alignments.codes(List.of("L", "NX", "C", "E"))).isEqualTo("any evil alignment");
        assertThat(CreatureConverter.Alignments.codes(List.of("NX", "C", "G", "NY", "E"))).isEqualTo("any non-lawful alignment");
    }
}
