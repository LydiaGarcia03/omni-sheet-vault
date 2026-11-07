package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertThat(CreatureConverter.Alignments.codes(List.of("U"))).isEqualTo("unaligned");
        assertThat(CreatureConverter.Alignments.codes(List.of("L", "G", "NY", "E"))).isEqualTo("any lawful alignment");
    }

    @Test
    void alignmentAlternativesKeepTheirChanceAndSpecialText() {
        assertThat(CreatureConverter.Alignments.text(null)).isEqualTo("unaligned");
        assertThat(CreatureConverter.Alignments.text(MAPPER.readTree("[]"))).isEqualTo("unaligned");
        assertThat(CreatureConverter.Alignments.text(MAPPER.readTree("""
                [{"alignment":["C","E"],"chance":70},{"alignment":["N"]},{"special":"as its creator"}]""")))
                .isEqualTo("chaotic evil (70%) or neutral or as its creator");
    }

    @Test
    void readsTheMonsterManualFileOnlyWhenItExists() {
        FiveEToolsDataSource dataSource = mock(FiveEToolsDataSource.class);
        when(dataSource.hasDataFile("bestiary/bestiary-mm.json")).thenReturn(true);
        when(dataSource.readDataFile("bestiary/bestiary-mm.json")).thenReturn(MAPPER.readTree("""
                {"monster":[{"name":"Test Hound"},{"name":"Test Drake"}]}"""));
        FiveEToolsDataSource withoutMonsters = mock(FiveEToolsDataSource.class);
        when(withoutMonsters.hasDataFile("bestiary/bestiary-mm.json")).thenReturn(true);
        when(withoutMonsters.readDataFile("bestiary/bestiary-mm.json")).thenReturn(MAPPER.readTree("{}"));

        assertThat(converter.kind()).isEqualTo(CatalogueEntryKind.CREATURE);
        assertThat(converter.loadRawEntries(dataSource)).hasSize(2);
        assertThat(converter.loadRawEntries(withoutMonsters)).isEmpty();
        assertThat(converter.loadRawEntries(mock(FiveEToolsDataSource.class))).isEmpty();
    }

    @Test
    void missingFieldsFallBackToNeutralValues() {
        CatalogueEntryImport entry = converter.convert(MAPPER.readTree("""
                {"name":"Test Blob","source":"MM","page":"unnumbered"}"""));
        JsonNode data = entry.data();

        assertThat(entry.sourcePage()).isNull();
        assertThat(data.get("size").asString()).isEqualTo("Medium");
        assertThat(data.get("creatureType").asString()).isEmpty();
        assertThat(data.get("armorClass").asInt()).isEqualTo(10);
        assertThat(data.get("armorClassText").asString()).isEqualTo("10");
        assertThat(data.get("hitPoints").asInt()).isZero();
        assertThat(data.get("hitDice").asString()).isEmpty();
        assertThat(data.get("speed").asInt()).isZero();
        assertThat(data.get("additionalSpeeds").isNull()).isTrue();
        assertThat(data.get("skills")).isEmpty();
        assertThat(data.get("challengeRating").asString()).isEqualTo("—");
        assertThat(data.get("damageResistances").isNull()).isTrue();
    }

    @Test
    void severalSizesAndUnreadableBonusesAreKept() {
        JsonNode data = converter.convert(MAPPER.readTree("""
                {"name":"Test Shifter","source":"MM","size":["S","M"],"str":14,
                 "save":{"str":"see below"},"skill":{"stealth":"special","perception":["+3"]},
                 "hp":{"special":"equal to its summoner's"}}""")).data();

        assertThat(data.get("size").asString()).isEqualTo("Small or Medium");
        assertThat(data.get("abilityScores").get(0).get("save").asInt()).isEqualTo(2);
        assertThat(data.get("skills")).hasSize(1);
        assertThat(data.get("skills").get(0).get("bonus").asInt()).isZero();
        assertThat(data.get("hitPoints").asInt()).isZero();
        assertThat(data.get("hitDice").asString()).isEqualTo("equal to its summoner's");
    }

    @Test
    void creatureTypesReadAsWords() {
        assertThat(CreatureConverter.creatureType(MAPPER.readTree("\"undead\""))).isEqualTo("Undead");
        assertThat(CreatureConverter.creatureType(MAPPER.readTree("""
                {"type":{"choose":["celestial","fiend"]}}"""))).isEqualTo("Celestial or fiend");
        assertThat(CreatureConverter.creatureType(MAPPER.readTree("""
                {"type":"beast","swarmSize":"T"}"""))).isEqualTo("Swarm of Tiny beasts");
        assertThat(CreatureConverter.creatureType(MAPPER.readTree("""
                {"type":"humanoid","tags":["elf",{"prefix":"any","tag":"race"}]}"""))).isEqualTo("Humanoid (elf, any race)");
        assertThat(CreatureConverter.creatureType(MAPPER.readTree("{}"))).isEmpty();
    }

    @Test
    void armorClassTextKeepsEveryAlternative() {
        JsonNode data = armor("""
                [{"ac":15,"from":["natural armor","shield"],"condition":"while enraged"},12,{"special":"varies"},{"unknown":true}]""");

        assertThat(data.get("armorClass").asInt()).isEqualTo(15);
        assertThat(data.get("armorClassText").asString()).isEqualTo("15 (natural armor, shield) while enraged, 12, varies");
    }

    @Test
    void armorClassWithoutANumberStaysAtTheDefault() {
        JsonNode special = armor("""
                [{"special":"varies"}]""");
        JsonNode notAList = armor("14");

        assertThat(special.get("armorClass").asInt()).isEqualTo(10);
        assertThat(special.get("armorClassText").asString()).isEqualTo("varies");
        assertThat(notAList.get("armorClass").asInt()).isEqualTo(10);
    }

    @Test
    void speedsKeepTheirConditionsAndHoverOnlyWithoutOne() {
        JsonNode conditional = speed("""
                {"walk":{"number":20,"condition":"in bear form"},"burrow":5,"fly":{"number":30,"condition":"in bat form"},"swim":15,"canHover":true}""");
        JsonNode withoutWalk = speed("""
                {"fly":30}""");

        assertThat(conditional.get("speed").asInt()).isEqualTo(20);
        assertThat(conditional.get("additionalSpeeds").asString()).isEqualTo("Burrow 5 ft., Fly 30 ft. in bat form, Swim 15 ft.");
        assertThat(withoutWalk.get("speed").asInt()).isZero();
        assertThat(withoutWalk.get("additionalSpeeds").asString()).isEqualTo("Fly 30 ft.");
    }

    @Test
    void defensesListPlainNamesFirstThenEachGroup() {
        assertThat(CreatureConverter.defenses(null, "resist")).isNull();
        assertThat(CreatureConverter.defenses(MAPPER.readTree("[]"), "resist")).isNull();
        assertThat(CreatureConverter.defenses(MAPPER.readTree("""
                [{"resist":[]},{"other":true}]"""), "resist")).isNull();
        assertThat(CreatureConverter.defenses(MAPPER.readTree("""
                [{"special":"damage from spells"},"poison","acid",
                 {"preNote":"while raging","resist":["cold","fire"],"note":"only"},{"resist":["thunder"]}]"""), "resist"))
                .isEqualTo("poison, acid; damage from spells; while raging cold and fire only; thunder");
    }

    @Test
    void spellcastingListsItsDailySpellsAndSlotsAsAnActionWhenDisplayedSo() {
        JsonNode data = converter.convert(MAPPER.readTree("""
                {"name":"Test Mage","source":"MM","spellcasting":[{"displayAs":"action",
                  "headerEntries":["It casts spells."],"footerEntries":["It rests afterwards."],
                  "daily":{"2":["{@spell light}"]},
                  "spells":{"0":{"spells":["{@spell light}"]},"1":{"slots":1,"spells":["{@spell shield}"]},
                            "2":{"slots":3,"spells":[{"entry":"{@spell web}"}]},"3":{"spells":["{@spell fly}"]},"4":{"slots":2}}}]}""")).data();

        assertThat(data.get("traits")).isEmpty();
        assertThat(data.get("actions").get(0).get("name").asString()).isEqualTo("Spellcasting");
        assertThat(data.get("actions").get(0).get("description").asString()).isEqualTo("""
                It casts spells.
                2/day: light
                Cantrips (at will): light
                1st level (1 slot): shield
                2nd level (3 slots): web
                3rd level: fly
                4th level (2 slots):\s
                It rests afterwards.""");
    }

    private JsonNode armor(String ac) {
        return converter.convert(MAPPER.readTree("{\"name\":\"Test Knight\",\"source\":\"MM\",\"ac\":" + ac + "}")).data();
    }

    private JsonNode speed(String speed) {
        return converter.convert(MAPPER.readTree("{\"name\":\"Test Runner\",\"source\":\"MM\",\"speed\":" + speed + "}")).data();
    }
}
