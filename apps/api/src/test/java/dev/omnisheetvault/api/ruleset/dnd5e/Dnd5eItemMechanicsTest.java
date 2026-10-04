package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Magic-item mechanics (C3e): active only while equipped, and attuned when required. */
class Dnd5eItemMechanicsTest {

    private static final String CLOAK_OF_PROTECTION = """
            {"mechanics":{"modifiers":[{"type":"BONUS","target":"ARMOR_CLASS","value":1},
                                       {"type":"BONUS","target":"SAVING_THROWS","value":1}],
                          "damageResistances":[],"damageImmunities":[],"damageVulnerabilities":[],"conditionImmunities":[]}}""";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void anAttunedCloakOfProtectionRaisesArmorClassAndEverySave() {
        VitalsZone vitals = calculate(sheet(item("Cloak of Protection", true, true, true, bonus(Dnd5eModifierTarget.ARMOR_CLASS, 1),
                bonus(Dnd5eModifierTarget.SAVING_THROWS, 1))));

        assertThat(vitals.armorClass().value()).isEqualTo(10 + 2 + 1);
        assertThat(vitals.armorClass().contributions()).extracting(Contribution::source).contains("Cloak of Protection");
        assertThat(vitals.savingThrows().get("wisdom").value()).isEqualTo(0 + 1);
        assertThat(vitals.savingThrows().get("dexterity").value()).isEqualTo(2 + 1);
    }

    @Test
    void anItemRequiringAttunementDoesNothingUntilAttuned() {
        VitalsZone equippedOnly = calculate(sheet(item("Cloak of Protection", true, false, true, bonus(Dnd5eModifierTarget.ARMOR_CLASS, 1))));
        VitalsZone carried = calculate(sheet(item("Cloak of Protection", false, true, true, bonus(Dnd5eModifierTarget.ARMOR_CLASS, 1))));

        assertThat(equippedOnly.armorClass().value()).isEqualTo(12);
        assertThat(carried.armorClass().value()).isEqualTo(12);
    }

    @Test
    void anAmuletOfHealthSetsConstitutionAndHitPointsFollow() {
        Dnd5eSheet sheet = sheet(item("Amulet of Health", true, true, true,
                new Dnd5eModifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.CONSTITUTION_SCORE, 19, null, null, "Amulet of Health")));

        VitalsZone vitals = calculate(sheet);

        assertThat(vitals.abilityScores().get("constitution")).isEqualTo(19);
        assertThat(vitals.hitPoints().value()).isEqualTo(Dnd5eFormulas.maxHitPoints(sheet));
        assertThat(vitals.hitPoints().value()).isEqualTo(8 + 4 + 4 * (5 + 4));
    }

    @Test
    void aSetScoreNeverLowersAHigherOne() {
        VitalsZone vitals = calculate(sheet(item("Gauntlets of Ogre Power", true, true, true,
                new Dnd5eModifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.DEXTERITY_SCORE, 12, null, null, "Test"))));

        assertThat(vitals.abilityScores().get("dexterity")).isEqualTo(14);
    }

    @Test
    void spellcastingFocusBonusesRaiseAttackAndSaveDc() {
        VitalsZone vitals = calculate(sheet(item("Arcane Grimoire", true, true, true,
                bonus(Dnd5eModifierTarget.SPELL_ATTACKS, 1), bonus(Dnd5eModifierTarget.SPELL_SAVE_DC, 1))));

        SpellcastingClassInfo wizard = vitals.spellcasting().getFirst();
        assertThat(wizard.spellAttackBonus().value()).isEqualTo(3 + 3 + 1);
        assertThat(wizard.spellSaveDc().value()).isEqualTo(8 + 3 + 3 + 1);
        assertThat(wizard.spellSaveDc().contributions()).extracting(Contribution::source).contains("Arcane Grimoire");
    }

    @Test
    void activeItemDefensesJoinTheSheets() {
        Dnd5eItem ring = new Dnd5eItem("ring", "Ring of Fire Resistance", 1, "", "", true, true, true, "ring", Dnd5eItemKind.GEAR,
                "Ring", "rare", null, null, null, null, null, null, null, null, null, null, List.of(), false, null, null,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null,
                new Dnd5eItemMechanics(List.of(), List.of("fire"), List.of(), List.of(), List.of("charmed")));

        VitalsZone vitals = calculate(sheet(ring));

        assertThat(vitals.damageResistances()).containsExactly("poison", "fire");
        assertThat(vitals.conditionImmunities()).containsExactly("charmed");
    }

    @Test
    void addingACatalogueItemCopiesItsMechanicsAndEquippingKeepsThem() {
        String added = mutator.addCatalogueItem(objectMapper.writeValueAsString(sheet()),
                new Item("ignored", "Cloak of Protection", 1, "", "", false, false, true), CLOAK_OF_PROTECTION);
        String itemKey = objectMapper.readValue(added, Dnd5eSheet.class).items().getFirst().key();

        String worn = mutator.toggleItemAttuned(mutator.toggleItemEquipped(added, itemKey), itemKey);

        Dnd5eItem cloak = objectMapper.readValue(worn, Dnd5eSheet.class).items().getFirst();
        assertThat(cloak.mechanics().modifiers()).extracting(Dnd5eModifier::source).containsOnly("Cloak of Protection");
        assertThat(calculator.calculateVitals(worn).armorClass().value()).isEqualTo(13);
    }

    private VitalsZone calculate(Dnd5eSheet sheet) {
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    private static Dnd5eModifier bonus(Dnd5eModifierTarget target, int value) {
        return new Dnd5eModifier(Dnd5eModifierType.BONUS, target, value, null, null, "placeholder");
    }

    private static Dnd5eItem item(String name, boolean equipped, boolean attuned, boolean requiresAttunement, Dnd5eModifier... modifiers) {
        List<Dnd5eModifier> named = Arrays.stream(modifiers)
                .map(modifier -> new Dnd5eModifier(modifier.type(), modifier.target(), modifier.value(), null, null, name))
                .toList();
        return new Dnd5eItem("item", name, 1, "", "", equipped, attuned, requiresAttunement, "slug", Dnd5eItemKind.GEAR,
                "Wondrous Item", "rare", null, null, null, null, null, null, null, null, null, null, List.of(), false, null, null,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null,
                new Dnd5eItemMechanics(named, List.of(), List.of(), List.of(), List.of()));
    }

    /** Dex 14 (+2), Con 12 (+1), Int 16 (+3), level 5 wizard-like caster, d8, poison resistance. */
    private static Dnd5eSheet sheet(Dnd5eItem... items) {
        return new Dnd5eSheet(
                10, 14, 12, 16, 10, 10, 5, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of("poison"), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, List.of(), List.of(),
                List.of(new Dnd5eSpellcastingClass("Wizard", "intelligence", Dnd5eSpellCastingType.PREPARED, 3, null, 8)),
                List.of(), List.of(items), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, null, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
