package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Fixtures follow the shapes the 5etools converters write (PHB Dwarf, Soldier, Fighter, Battle Master; EEPC Fire Genasi). */
class Dnd5eOptionSummariesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final CatalogueLookup CATALOGUE = new CatalogueLookup() {
        @Override
        public Optional<CatalogueRecord> find(String kind, String slug) {
            return Optional.empty();
        }

        @Override
        public List<CatalogueRecord> list(String kind) {
            return List.of();
        }
    };

    private final Dnd5eOptionSummaries summaries = new Dnd5eOptionSummaries(CATALOGUE, true);

    @Test
    void speciesListsIncreasesSpeedSensesAndResistances() {
        CatalogueRecord dwarf = record("SPECIES", "Dwarf", "", """
                {"abilityAlternatives":{"fixed":{"constitution":2},"choices":[]},"speed":{"walk":25},
                 "senses":{"type":"darkvision","range":60},"damageResistances":{"fixed":["poison"],"choices":[]}}""");

        assertThat(summaries.of(dwarf)).isEqualTo("CON +2 · Speed 25 ft · Darkvision 60 ft · Poison resistance");
    }

    @Test
    void aSubspeciesAddsItsLevelOneSpells() {
        JsonNode fire = json("""
                {"abilityAlternatives":{"fixed":{"intelligence":1},"choices":[]},"senses":{"type":"darkvision","range":60},
                 "damageResistances":{"fixed":["fire"],"choices":[]},
                 "additionalSpells":{"innate":{"3":{"daily":{"1":["burning hands"]}}},"ability":"con","known":{"1":["produce flame#c"]}}}""");

        assertThat(summaries.species(fire)).isEqualTo("INT +1 · Darkvision 60 ft · Fire resistance · Produce Flame");
    }

    @Test
    void aChosenIncreaseNamesWhatItCanRaise() {
        JsonNode halfElf = json("""
                {"abilityAlternatives":{"fixed":{"charisma":2},"choices":[{"from":["strength","dexterity","constitution","intelligence","wisdom","charisma"],"count":2,"amount":1}]},
                 "speed":{"walk":30}}""");

        assertThat(summaries.species(halfElf)).isEqualTo("CHA +2, +1 to 2 abilities of your choice · Speed 30 ft");
    }

    @Test
    void backgroundListsSkillsToolsLanguagesAndItsFeature() {
        CatalogueRecord soldier = record("BACKGROUND", "Soldier", "", """
                {"skillAlternatives":[{"fixed":["athletics","intimidation"],"choices":[]}],
                 "toolAlternatives":[{"fixed":["vehicles-land"],"choices":[{"from":null,"category":"gamingSet","count":1}]}],
                 "languageAlternatives":[],"features":[{"name":"Military Rank"}]}""");

        assertThat(summaries.of(soldier)).isEqualTo("Athletics, Intimidation · Vehicles (Land), 1 gaming set · Military Rank");
    }

    @Test
    void classListsHitDieSavesArmorAndWeapons() {
        CatalogueRecord fighter = record("CLASS", "Fighter", "", """
                {"hitDie":10,"savingThrows":["strength","constitution"],
                 "proficiencies":{"armor":["light","medium","heavy","shield"],"weaponCategories":["simple","martial"]}}""");

        assertThat(summaries.of(fighter)).isEqualTo("d10 · Saves STR, CON · All armor, shields · Simple & martial weapons");
    }

    @Test
    void subclassListsItsFirstLevelFeaturesWithoutOptionIndexes() {
        CatalogueRecord battleMaster = record("SUBCLASS", "Battle Master", "", """
                {"features":[{"level":3,"name":"Student of War"},{"level":3,"name":"Combat Superiority"},{"level":3,"name":"Maneuvers"},
                             {"level":3,"name":"Maneuver Options"},{"level":7,"name":"Know Your Enemy"}]}""");

        assertThat(summaries.of(battleMaster)).isEqualTo("Level 3: Student of War, Combat Superiority, Maneuvers");
    }

    @Test
    void featQuotesItsFirstBenefitAfterItsIncrease() {
        CatalogueRecord actor = record("FEAT", "Actor",
                "Skilled at mimicry and dramatics, you gain the following benefits: Increase your Charisma score by 1. You have advantage.",
                """
                {"abilityAlternatives":{"fixed":{"charisma":1},"choices":[]}}""");

        assertThat(summaries.of(actor)).isEqualTo("CHA +1 · Increase your Charisma score by 1.");
    }

    @Test
    void optionalFeatureSkipsItsPrerequisiteLine() {
        CatalogueRecord agonizingBlast = record("OPTIONAL_FEATURE", "Agonizing Blast",
                "Prerequisite: eldritch blast cantrip\n\nWhen you cast eldritch blast, add your Charisma modifier to the damage it deals on a hit.", "{}");

        assertThat(summaries.of(agonizingBlast)).isEqualTo("When you cast eldritch blast, add your Charisma modifier to the damage it deals on a hit.");
    }

    @Test
    void redactedProseLeavesOnlyTheStructuredParts() {
        Dnd5eOptionSummaries redacted = new Dnd5eOptionSummaries(CATALOGUE, false);
        CatalogueRecord actor = record("FEAT", "Actor", "You gain the following benefits: Increase your Charisma score by 1.",
                "{\"abilityAlternatives\":{\"fixed\":{\"charisma\":1},\"choices\":[]}}");
        CatalogueRecord dueling = record("OPTIONAL_FEATURE", "Dueling", "When you are wielding a melee weapon in one hand, you gain +2.", "{}");

        assertThat(redacted.of(actor)).isEqualTo("CHA +1");
        assertThat(redacted.of(dueling)).isNull();
    }

    @Test
    void spellListsItsTechnicalLine() {
        assertThat(Dnd5eOptionSummaries.spell(json("""
                {"level":0,"school":"evocation","castingTime":"1 Action","range":"120 ft.","components":"V, S",
                 "duration":"Instantaneous","damageDiceCount":1,"damageDiceSides":10,"damageType":"fire"}""")))
                .isEqualTo("Evocation cantrip · 1 action · 120 ft. · V, S · Instantaneous · 1d10 fire");
        assertThat(Dnd5eOptionSummaries.spell(json("""
                {"level":1,"school":"divination","ritual":true,"concentration":true,"castingTime":"1 Action","range":"Self",
                 "components":"V, S","duration":"up to 10 minutes"}""")))
                .isEqualTo("1st-level divination (ritual) · 1 action · Self · V, S · Concentration, up to 10 minutes");
    }

    @Test
    void languageShowsItsTypeAndScript() {
        assertThat(Dnd5eOptionSummaries.language(json("{\"type\":\"standard\",\"script\":\"Infernal\"}"))).isEqualTo("Standard · Infernal script");
        assertThat(Dnd5eOptionSummaries.language(json("{\"type\":null,\"script\":null}"))).isNull();
    }

    @Test
    void itemsKeepAPlainLine() {
        assertThat(summaries.of(record("ITEM", "Longsword", "", "{}"))).isNull();
    }

    private static CatalogueRecord record(String kind, String name, String description, String data) {
        return new CatalogueRecord(kind, name.toLowerCase().replace(' ', '-'), name, "Player's Handbook", 1, description, json(data));
    }

    private static JsonNode json(String text) {
        return MAPPER.readTree(text);
    }
}
