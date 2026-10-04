package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.ActiveEffectInfo;
import dev.omnisheetvault.api.ruleset.SpellSlotLevel;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Active effects (C3d): cast → effect, manual end, concentration and rest endings. */
class Dnd5eActiveEffectsTest {

    private static final String MAGE_ARMOR = """
            {"level":1,"effect":{"modifiers":[{"type":"SET_BASE","target":"UNARMORED_ARMOR_CLASS","value":13}],
                                 "endsOnRests":[],"durationText":"8 hours"}}""";
    private static final String BARKSKIN = """
            {"level":2,"effect":{"modifiers":[{"type":"BONUS","target":"ARMOR_CLASS","value":1}],
                                 "endsOnRests":[],"durationText":"1 hour"}}""";
    private static final String UNTIL_LONG_REST = """
            {"level":1,"effect":{"modifiers":[{"type":"BONUS","target":"ARMOR_CLASS","value":1}],
                                 "endsOnRests":["LONG_REST"],"durationText":null}}""";
    private static final String NO_EFFECT = """
            {"level":2,"effect":null}""";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void castingMageArmorSpendsTheSlotAndSetsTheUnarmoredBase() {
        VitalsZone vitals = calculator.calculateVitals(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true));

        assertThat(vitals.armorClass().value()).isEqualTo(13 + 2);
        assertThat(usedSlots(vitals, 1)).isEqualTo(1);
        ActiveEffectInfo effect = vitals.activeEffects().getFirst();
        assertThat(effect.name()).isEqualTo("Mage Armor");
        assertThat(effect.durationText()).isEqualTo("8 hours");
        assertThat(effect.castAtLevel()).isEqualTo(1);
        assertThat(effect.endsOnRests()).isEmpty();
    }

    @Test
    void aSpellWithoutALastingEffectOnlySpendsTheSlot() {
        VitalsZone vitals = calculator.calculateVitals(mutator.castSpell(sheet(), "hold-person", 2, false, NO_EFFECT, true));

        assertThat(vitals.activeEffects()).isEmpty();
        assertThat(usedSlots(vitals, 2)).isEqualTo(1);
    }

    @Test
    void castingTheSameSpellAgainReplacesItsEffect() {
        String twice = mutator.castSpell(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true), "mage-armor", 2, false, MAGE_ARMOR, true);

        VitalsZone vitals = calculator.calculateVitals(twice);

        assertThat(vitals.activeEffects()).singleElement().extracting(ActiveEffectInfo::castAtLevel).isEqualTo(2);
    }

    @Test
    void aConcentrationSpellEndsTheCurrentConcentrationEffectButNotOthers() {
        String concentrating = mutator.castSpell(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true), "barkskin", 2, false, BARKSKIN, true);

        VitalsZone vitals = calculator.calculateVitals(mutator.castSpell(concentrating, "hold-person", 2, false, NO_EFFECT, true));

        assertThat(vitals.activeEffects()).extracting(ActiveEffectInfo::name).containsExactly("Mage Armor");
    }

    @Test
    void endingAnEffectRemovesItsModifiers() {
        String cast = mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true);

        VitalsZone vitals = calculator.calculateVitals(mutator.endActiveEffect(cast, "mage-armor"));

        assertThat(vitals.activeEffects()).isEmpty();
        assertThat(vitals.armorClass().value()).isEqualTo(10 + 2);
    }

    @Test
    void restsEndOnlyTheEffectsWhoseSourceNamesThem() {
        String both = mutator.castSpell(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true), "shield-of-faith", 1, false, UNTIL_LONG_REST, true);

        VitalsZone afterShortRest = calculator.calculateVitals(mutator.applyShortRest(both));
        VitalsZone afterLongRest = calculator.calculateVitals(mutator.applyLongRest(both));

        assertThat(afterShortRest.activeEffects()).extracting(ActiveEffectInfo::name).containsExactly("Mage Armor", "Shield of Faith");
        assertThat(afterLongRest.activeEffects()).extracting(ActiveEffectInfo::name).containsExactly("Mage Armor");
    }

    @Test
    void anEffectCastOnAnAllyIsTrackedButLeavesTheCharactersValues() {
        VitalsZone vitals = calculator.calculateVitals(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, false));

        assertThat(vitals.armorClass().value()).isEqualTo(10 + 2);
        assertThat(vitals.activeEffects()).singleElement().satisfies(effect -> {
            assertThat(effect.onSelf()).isFalse();
            assertThat(effect.key()).isEqualTo("mage-armor-ally");
        });
    }

    @Test
    void theSameSpellCanBeOnTheCharacterAndOnAnAllyAtOnce() {
        String both = mutator.castSpell(mutator.castSpell(sheet(), "mage-armor", 1, false, MAGE_ARMOR, true), "mage-armor", 1, false, MAGE_ARMOR, false);

        VitalsZone vitals = calculator.calculateVitals(both);

        assertThat(vitals.activeEffects()).extracting(ActiveEffectInfo::onSelf).containsExactly(true, false);
        assertThat(vitals.armorClass().value()).isEqualTo(13 + 2);
    }

    @Test
    void anUnknownSpellStillSpendsTheSlot() {
        VitalsZone vitals = calculator.calculateVitals(mutator.castSpell(sheet(), "fireball", 1, false, MAGE_ARMOR, true));

        assertThat(vitals.activeEffects()).isEmpty();
        assertThat(usedSlots(vitals, 1)).isEqualTo(1);
    }

    private static int usedSlots(VitalsZone vitals, int level) {
        return vitals.spellSlots().stream().filter(slot -> slot.level() == level).findFirst().map(SpellSlotLevel::usedSlots).orElseThrow();
    }

    /** Dexterity 14 (+2), unarmored; two 1st- and two 2nd-level slots. */
    private String sheet() {
        List<Dnd5eSpell> spells = List.of(
                spell("mage-armor", "Mage Armor", 1, false),
                spell("barkskin", "Barkskin", 2, true),
                spell("hold-person", "Hold Person", 2, true),
                spell("shield-of-faith", "Shield of Faith", 1, false));
        List<Dnd5eSpellSlotLevel> slots = List.of(new Dnd5eSpellSlotLevel(1, 2, 0), new Dnd5eSpellSlotLevel(2, 2, 0));
        Dnd5eSheet sheet = new Dnd5eSheet(
                10, 14, 14, 10, 10, 10, 5, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 30, 0, 0, false, List.of(), List.of(),
                List.of(), spells, List.of(), 0, 0, 0, 0, 0, List.of(), background(), List.of(), 0,
                slots, List.of(), List.of(), false, null, null, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private static Dnd5eSpell spell(String key, String name, int level, boolean concentration) {
        return new Dnd5eSpell(key, name, "Wizard", level, "abjuration", "1 action", "Touch", concentration, false, false,
                null, null, null, "", "Buff", true, false, "", null, "V, S", null, "8 hours", null, null, null);
    }

    private static Dnd5eBackground background() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
