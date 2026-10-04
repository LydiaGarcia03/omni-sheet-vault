package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Fixtures follow the shapes SpeciesConverter and BackgroundConverter write (trimmed PHB Mountain Dwarf and Soldier). */
class Dnd5eSelectionDetailsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final CatalogueRecord COMMON_CLOTHES = new CatalogueRecord("ITEM", "common-clothes", "Common Clothes", "Player's Handbook", 1, "",
            MAPPER.readTree("{}"));
    private static final CatalogueLookup CATALOGUE = new CatalogueLookup() {
        @Override
        public Optional<CatalogueRecord> find(String kind, String slug) {
            return "ITEM".equals(kind) && "common-clothes".equals(slug) ? Optional.of(COMMON_CLOTHES) : Optional.empty();
        }

        @Override
        public List<CatalogueRecord> list(String kind) {
            return List.of();
        }
    };

    private final Dnd5eOptionSummaries summaries = new Dnd5eOptionSummaries(CATALOGUE, true);

    @Test
    void speciesCardListsItsFactsAndItsMechanicalTraits() {
        CatalogueRecord dwarf = record("SPECIES", "Dwarf", "{}");
        SelectionDetail detail = Dnd5eSelectionDetails.species(dwarf, "Mountain Dwarf", MAPPER.readTree("""
                {"abilityAlternatives":{"fixed":{"constitution":2,"strength":2},"choices":[]},"speed":{"walk":25},"size":"medium",
                 "languageAlternatives":{"fixed":["common","dwarvish"],"choices":[]},"senses":{"type":"darkvision","range":60},
                 "traits":[{"name":"Age","description":"Dwarves mature slowly."},{"name":"Speed","description":"Your speed is not reduced by wearing heavy armor."},
                           {"name":"Dwarven Resilience","description":"You have advantage on saving throws against poison."},{"name":"Languages","description":"Common."}]}"""),
                summaries);

        assertThat(detail.name()).isEqualTo("Mountain Dwarf");
        assertThat(detail.facts()).extracting(SelectionDetail.Entry::label, SelectionDetail.Entry::text).containsExactly(
                tuple("Ability bonuses", "CON +2, STR +2"), tuple("Speed", "25 ft"), tuple("Size", "Medium"),
                tuple("Languages", "Common, Dwarvish"), tuple("Senses", "Darkvision 60 ft"));
        assertThat(detail.grants()).extracting(SelectionDetail.Entry::label)
                .containsExactly("Speed", "Dwarven Resilience");
        assertThat(detail.grants().get(1).text()).isEqualTo("You have advantage on saving throws against poison.");
    }

    @Test
    void backgroundCardListsProficienciesItsFeatureAndItsEquipment() {
        CatalogueRecord soldier = record("BACKGROUND", "Soldier", """
                {"skillAlternatives":[{"fixed":["athletics","intimidation"],"choices":[]}],
                 "toolAlternatives":[{"fixed":["vehicles-land"],"choices":[{"category":"gamingSet","count":1}]}],
                 "languageAlternatives":[],
                 "features":[{"name":"Military Rank","description":"You have a military rank from your career as a soldier."}],
                 "startingEquipment":[{"options":[{"grants":[{"special":"insignia of rank","quantity":1},{"itemSlug":"common-clothes","quantity":1},
                                                             {"itemSlug":"pouch","quantity":1,"containsValueCp":1000}]}]}]}""");

        SelectionDetail detail = Dnd5eSelectionDetails.background(soldier, planner(), summaries);

        assertThat(detail.facts()).isEmpty();
        assertThat(detail.grants()).extracting(SelectionDetail.Entry::label, SelectionDetail.Entry::text).containsExactly(
                tuple("Skill proficiencies", "Athletics, Intimidation"),
                tuple("Tool proficiencies", "Vehicles (Land), 1 gaming set"),
                tuple("Feature: Military Rank", "You have a military rank from your career as a soldier."),
                tuple("Equipment", "insignia of rank, Common Clothes, Pouch (with 10 gp)"));
    }

    @Test
    void redactedProseKeepsTheTitlesWithoutText() {
        Dnd5eOptionSummaries redacted = new Dnd5eOptionSummaries(CATALOGUE, false);
        SelectionDetail detail = Dnd5eSelectionDetails.species(record("SPECIES", "Dwarf", "{}"), "Dwarf",
                MAPPER.readTree("{\"traits\":[{\"name\":\"Stonecunning\",\"description\":\"Double proficiency on stonework.\"}]}"), redacted);

        assertThat(detail.grants()).extracting(SelectionDetail.Entry::label, SelectionDetail.Entry::text).containsExactly(tuple("Stonecunning", null));
    }

    private static Dnd5eBuildPlanner planner() {
        return new Dnd5eBuildPlanner(new Dnd5eCharacterBuild(null, null, null, null, null, null, Map.of(), null, null, null), CATALOGUE);
    }

    private static CatalogueRecord record(String kind, String name, String data) {
        return new CatalogueRecord(kind, name.toLowerCase(), name, "Player's Handbook", 1, "", MAPPER.readTree(data));
    }
}
