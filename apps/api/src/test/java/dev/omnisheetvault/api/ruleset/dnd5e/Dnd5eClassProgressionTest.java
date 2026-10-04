package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.ProgressionTable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Fixtures follow the shapes ClassConverter and SubclassConverter write (trimmed PHB Wizard, Barbarian, Fighter, Battle Master). */
class Dnd5eClassProgressionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void aCasterGetsOneColumnPerSpellLevelItReaches() {
        CatalogueRecord wizard = record("CLASS", "Wizard", """
                {"tableColumns":[{"label":"Cantrips Known","valuesByLevel":["3","3","3","4"]}],
                 "spellcasting":{"spellSlotsByLevel":[[2,0,0],[3,0,0],[4,2,0],[4,3,0],[4,3,2]]},
                 "features":[{"level":1,"name":"Spellcasting"},{"level":1,"name":"Arcane Recovery"},{"level":2,"name":"Arcane Tradition","grantsSubclassFeature":true}]}""");

        ProgressionTable table = Dnd5eClassProgression.table(wizard, null, 3, false);

        assertThat(table.columns()).containsExactly("Level", "Proficiency Bonus", "Features", "Cantrips Known", "1st", "2nd", "3rd");
        assertThat(table.rows()).hasSize(20);
        assertThat(table.rows().get(0)).containsExactly("1st", "+2", "Spellcasting, Arcane Recovery", "3", "2", "—", "—");
        assertThat(table.rows().get(4)).containsExactly("5th", "+3", "—", "—", "4", "3", "2");
        assertThat(table.currentLevel()).isEqualTo(3);
        assertThat(table.name()).isEqualTo("Wizard");
        assertThat(table.key()).isEqualTo("wizard");
    }

    @Test
    void aMartialClassShowsItsOwnColumns() {
        CatalogueRecord barbarian = record("CLASS", "Barbarian", """
                {"tableColumns":[{"label":"Rages","valuesByLevel":["2","2","3"]},{"label":"Rage Damage","valuesByLevel":["+2","+2","+2"]}],
                 "spellcasting":{"spellSlotsByLevel":null},
                 "features":[{"level":1,"name":"Rage"},{"level":1,"name":"Unarmored Defense"},{"level":2,"name":"Reckless Attack"}]}""");

        ProgressionTable table = Dnd5eClassProgression.table(barbarian, null, 1, false);

        assertThat(table.columns()).containsExactly("Level", "Proficiency Bonus", "Features", "Rages", "Rage Damage");
        assertThat(table.rows().get(0)).containsExactly("1st", "+2", "Rage, Unarmored Defense", "2", "+2");
        assertThat(table.rows().get(19).get(0)).isEqualTo("20th");
        assertThat(table.rows().get(19).get(1)).isEqualTo("+6");
    }

    @Test
    void aChosenSubclassFillsItsLevelsAndReplacesThePlaceholders() {
        CatalogueRecord fighter = record("CLASS", "Fighter", """
                {"tableColumns":[],"spellcasting":{"spellSlotsByLevel":null},
                 "features":[{"level":3,"name":"Martial Archetype","grantsSubclassFeature":true},
                             {"level":4,"name":"Martial Versatility","optional":true},
                             {"level":7,"name":"Martial Archetype feature","grantsSubclassFeature":true}]}""");
        CatalogueRecord battleMaster = record("SUBCLASS", "Battle Master", """
                {"features":[{"level":3,"name":"Combat Superiority"},{"level":3,"name":"Maneuver Options"},{"level":7,"name":"Know Your Enemy"}]}""");

        ProgressionTable table = Dnd5eClassProgression.table(fighter, battleMaster, 3, false);

        assertThat(table.name()).isEqualTo("Fighter (Battle Master)");
        assertThat(table.rows().get(2).get(2)).isEqualTo("Martial Archetype, Combat Superiority");
        assertThat(table.rows().get(3).get(2)).isEqualTo("—");
        assertThat(table.rows().get(6).get(2)).isEqualTo("Know Your Enemy");
        assertThat(Dnd5eClassProgression.table(fighter, null, 3, true).rows().get(6).get(2)).isEqualTo("Martial Archetype feature");
        assertThat(Dnd5eClassProgression.table(fighter, null, 3, true).rows().get(3).get(2)).isEqualTo("Martial Versatility");
    }

    private static CatalogueRecord record(String kind, String name, String data) {
        return new CatalogueRecord(kind, name.toLowerCase().replace(' ', '-'), name, "Player's Handbook", 1, "", MAPPER.readTree(data));
    }
}
