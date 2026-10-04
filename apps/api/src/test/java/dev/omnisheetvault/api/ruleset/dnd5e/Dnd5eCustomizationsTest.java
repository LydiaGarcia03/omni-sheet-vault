package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.AbilityScoreBreakdown;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.InvalidCustomizationException;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** D&D Beyond's "Customize" values on the D&D 5e sheet: stored as play state and applied over the calculated ones. */
class Dnd5eCustomizationsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void anOtherModifierAddsToTheScoreAndShowsInTheBreakdown() {
        String customized = mutator.customize(sheetJson(), "abilities", "strength", "{\"otherModifier\":2}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.abilityScores().get("strength")).isEqualTo(12);
        assertThat(vitals.abilityModifiers().get("strength").value()).isEqualTo(1);
        assertThat(vitals.provenance().abilityScores().get("strength").contributions()).contains(new Contribution("Other Modifier", 2));
        assertThat(vitals.provenance().abilityBreakdowns().get("strength"))
                .isEqualTo(new AbilityScoreBreakdown(12, 1, 10, 0, List.of(), 0, 2, 2, null));
    }

    @Test
    void anOverrideScoreReplacesTheScoreEverywhere() {
        int maxBefore = calculator.calculateVitals(sheetJson()).hitPoints().value();

        String customized = mutator.customize(sheetJson(), "abilities", "constitution", "{\"otherModifier\":4,\"overrideScore\":18}");
        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.abilityScores().get("constitution")).isEqualTo(18);
        assertThat(vitals.provenance().abilityScores().get("constitution").contributions())
                .containsExactly(new Contribution("Override Score", 18));
        assertThat(vitals.hitPoints().value()).isEqualTo(maxBefore + 5 * 2);
    }

    @Test
    void anEmptyValueClearsTheCustomization() {
        String customized = mutator.customize(sheetJson(), "abilities", "wisdom", "{\"overrideScore\":20}");

        String cleared = mutator.customize(customized, "abilities", "wisdom", "{}");

        assertThat(read(cleared).customizationsOrEmpty().abilities()).isEmpty();
        assertThat(calculator.calculateVitals(cleared).abilityScores().get("wisdom")).isEqualTo(10);
    }

    @Test
    void reMaterializingKeepsTheCustomizations() {
        Dnd5eSheet previous = read(mutator.customize(sheetJson(), "abilities", "charisma", "{\"otherModifier\":1}"));

        Dnd5eSheet carried = read(sheetJson()).withPlayStateOf(previous);

        assertThat(carried.customizationsOrEmpty().ability("charisma").otherModifier()).isEqualTo(1);
    }

    @Test
    void aSavingThrowTakesItsProficiencyLevelAndBonusesFromTheCustomization() {
        String customized = mutator.customize(sheetJson(), "savingThrows", "constitution",
                "{\"proficiencyLevel\":\"EXPERT\",\"magicBonus\":1,\"miscBonus\":2,\"miscBonusNotes\":\" Blessing \"}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.savingThrowProficiencies().get("constitution")).isEqualTo("EXPERT");
        assertThat(vitals.savingThrows().get("constitution").value()).isEqualTo(2 + 6 + 1 + 2);
        assertThat(vitals.savingThrows().get("constitution").contributions())
                .contains(new Contribution("Magic Bonus", 1), new Contribution("Misc Bonus", 2));
        assertThat(read(customized).customizationsOrEmpty().savingThrow("constitution").miscBonusNotes()).isEqualTo("Blessing");
    }

    @Test
    void aSavingThrowOverrideReplacesTheTotal() {
        String customized = mutator.customize(sheetJson(), "savingThrows", "wisdom", "{\"override\":9,\"magicBonus\":3}");

        assertThat(calculator.calculateVitals(customized).savingThrows().get("wisdom").value()).isEqualTo(9);
    }

    @Test
    void aSavingThrowTakesNoStatOverrideOrUnknownLevel() {
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "savingThrows", "wisdom", "{\"statOverride\":\"charisma\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "savingThrows", "wisdom", "{\"proficiencyLevel\":\"GODLIKE\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void aSkillUsesItsStatOverrideAndCustomizedProficiency() {
        String customized = mutator.customize(sheetJson(), "skills", "athletics",
                "{\"statOverride\":\"constitution\",\"proficiencyLevel\":\"EXPERT\",\"magicBonus\":1}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.skillGoverningAbilities().get("athletics")).isEqualTo("constitution");
        assertThat(vitals.skillProficiencies().get("athletics")).isEqualTo("EXPERT");
        assertThat(vitals.skills().get("athletics").value()).isEqualTo(2 + 6 + 1);
    }

    @Test
    void aCustomSkillIsAddedEditedAndRemoved() {
        String added = mutator.customize(sheetJson(), "customSkills", "new", "{}");
        Dnd5eCustomSkill skill = read(added).customizationsOrEmpty().customSkills().getFirst();

        assertThat(calculator.calculateVitals(added).customSkills().getFirst())
                .satisfies(custom -> {
                    assertThat(custom.name()).isEqualTo("Custom Skill 1");
                    assertThat(custom.abilityKey()).isNull();
                    assertThat(custom.value().value()).isEqualTo(3);
                });

        String edited = mutator.customize(added, "customSkills", skill.key(),
                "{\"key\":\"ignored\",\"name\":\"Cartography\",\"statOverride\":\"constitution\",\"proficiencyLevel\":\"NONE\",\"miscBonus\":1}");
        assertThat(calculator.calculateVitals(edited).customSkills().getFirst())
                .satisfies(custom -> {
                    assertThat(custom.key()).isEqualTo(skill.key());
                    assertThat(custom.name()).isEqualTo("Cartography");
                    assertThat(custom.value().value()).isEqualTo(2 + 1);
                });

        ResolvedRoll roll = new Dnd5eMechanicResolver().resolve(calculator.calculateVitals(edited), RollKind.SKILL_CHECK, skill.key(), null);
        assertThat(roll.modifier()).isEqualTo(3);
        assertThat(roll.context()).isEqualTo("Cartography: check");

        String removed = mutator.removeCustomization(edited, "customSkills", skill.key());
        assertThat(calculator.calculateVitals(removed).customSkills()).isEmpty();
    }

    @Test
    void removingASkillCustomizationClearsIt() {
        String customized = mutator.customize(sheetJson(), "skills", "stealth", "{\"override\":7}");

        String cleared = mutator.removeCustomization(customized, "skills", "stealth");

        assertThat(read(cleared).customizationsOrEmpty().skills()).isEmpty();
    }

    @Test
    void refusesAnUnknownSkillStatOrCustomSkill() {
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "skills", "juggling", "{}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "skills", "stealth", "{\"statOverride\":\"luck\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.removeCustomization(sheetJson(), "customSkills", "missing"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void aPassiveScoreFollowsItsCustomizedSkillUnlessOverridden() {
        String customized = mutator.customize(sheetJson(), "skills", "perception", "{\"proficiencyLevel\":\"FULL\",\"miscBonus\":1}");

        assertThat(calculator.calculateVitals(customized).senses().get("passivePerception").value()).isEqualTo(10 + 0 + 3 + 1);

        String overridden = mutator.customize(customized, "passives", "passivePerception", "{\"value\":18,\"notes\":\"Observant\"}");
        assertThat(calculator.calculateVitals(overridden).senses().get("passivePerception").value()).isEqualTo(18);
    }

    @Test
    void aSenseDistanceIsSetOrAdded() {
        String customized = mutator.customize(sheetJson(), "senses", "TREMORSENSE", "{\"value\":30}");

        assertThat(calculator.calculateVitals(customized).specialSenses())
                .extracting(sense -> sense.label())
                .containsExactly("Tremorsense 30 ft.");
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "senses", "XRAY", "{\"value\":30}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "passives", "passiveLuck", "{\"value\":3}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void aWalkingSpeedOverrideReplacesTheSpeed() {
        String customized = mutator.customize(sheetJson(), "speeds", "walking", "{\"value\":45}");

        assertThat(calculator.calculateVitals(customized).speed()).isEqualTo(45);
        assertThat(calculator.calculateVitals(mutator.removeCustomization(customized, "speeds", "walking")).speed()).isEqualTo(30);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "speeds", "teleporting", "{\"value\":30}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void theMovementDisplayIsStoredAndCleared() {
        String customized = mutator.customize(sheetJson(), "movementDisplay", "flying", "{}");

        assertThat(read(customized).customizationsOrEmpty().movementDisplay()).isEqualTo("flying");
        assertThat(read(mutator.removeCustomization(customized, "movementDisplay", "flying")).customizationsOrEmpty().movementDisplay())
                .isNull();
    }

    @Test
    void anArmorClassOverrideReplacesTheTotal() {
        String customized = mutator.customize(sheetJson(), "armorClass", "override", "{\"value\":19}");
        customized = mutator.customize(customized, "armorClass", "magicBonus", "{\"value\":2}");

        assertThat(calculator.calculateVitals(customized).armorClass().contributions())
                .containsExactly(new Contribution("Override AC", 19));
        assertThat(calculator.calculateVitals(customized).armorClass().value()).isEqualTo(19);
    }

    @Test
    void theArmorClassBaseIsReplacedAndBonusesAdd() {
        String customized = mutator.customize(sheetJson(), "armorClass", "baseArmorDex", "{\"value\":15}");
        customized = mutator.customize(customized, "armorClass", "magicBonus", "{\"value\":1}");
        customized = mutator.customize(customized, "armorClass", "miscBonus", "{\"value\":2,\"notes\":\"Blessing\"}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.armorClass().value()).isEqualTo(15 + 1 + 2);
        assertThat(vitals.armorClass().contributions()).containsExactly(
                new Contribution("Override Base Armor + DEX", 15),
                new Contribution("Additional Magic Bonus", 1),
                new Contribution("Additional Misc Bonus", 2));
    }

    @Test
    void aCustomDefenseIsAddedNotedAndRemoved() {
        String added = mutator.customize(sheetJson(), "defenses", "new", "{\"type\":\"RESISTANCE\",\"subtype\":\"fire\",\"notes\":\"Ring\"}");
        added = mutator.customize(added, "defenses", "new", "{\"type\":\"IMMUNITY\",\"subtype\":\"poisoned\"}");
        Dnd5eCustomDefense fire = read(added).customizationsOrEmpty().defenses().getFirst();

        VitalsZone vitals = calculator.calculateVitals(added);
        assertThat(vitals.damageResistances()).containsExactly("Fire");
        assertThat(vitals.conditionImmunities()).containsExactly("Poisoned");
        assertThat(vitals.damageImmunities()).isEmpty();
        assertThat(vitals.defenses()).extracting(defense -> defense.type() + " " + defense.name() + " " + defense.custom() + " " + defense.notes())
                .containsExactly("RESISTANCE Fire true Ring", "CONDITION_IMMUNITY Poisoned true null");

        String noted = mutator.customize(added, "defenses", fire.key(), "{\"notes\":\"Cloak\"}");
        assertThat(calculator.calculateVitals(noted).defenses().getFirst().notes()).isEqualTo("Cloak");

        String removed = mutator.removeCustomization(noted, "defenses", fire.key());
        assertThat(calculator.calculateVitals(removed).damageResistances()).isEmpty();
        assertThat(calculator.calculateVitals(removed).defenses()).hasSize(1);
    }

    @Test
    void refusesAnUnknownDefenseOrAConditionThatIsNotAnImmunity() {
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "defenses", "new", "{\"type\":\"RESISTANCE\",\"subtype\":\"sonic\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "defenses", "new", "{\"type\":\"RESISTANCE\",\"subtype\":\"charmed\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "defenses", "new", "{\"subtype\":\"fire\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.removeCustomization(sheetJson(), "defenses", "missing"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void anItemCustomizationRenamesRepricesAndAdjustsItsAttack() {
        String customized = mutator.customize(armedSheetJson(), "items", "longsword",
                "{\"toHitBonus\":1,\"damageBonus\":2,\"costOverride\":20,\"weightOverride\":2.5,\"silvered\":true,"
                        + "\"name\":\" Oathkeeper \",\"notes\":\"Family blade\"}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.items().getFirst()).satisfies(item -> {
            assertThat(item.name()).isEqualTo("Oathkeeper");
            assertThat(item.notes()).isEqualTo("Family blade");
            assertThat(item.cost()).isEqualTo("20 gp");
            assertThat(item.weightLb()).isEqualTo(2.5);
            assertThat(item.properties()).contains("Silvered");
        });
        assertThat(vitals.attacks().get("longsword")).satisfies(attack -> {
            assertThat(attack.name()).isEqualTo("Oathkeeper");
            assertThat(attack.toHit().value()).isEqualTo(0 + 3 + 1);
            assertThat(attack.toHit().contributions()).contains(new Contribution("To Hit Bonus", 1));
            assertThat(attack.damageModifier()).isEqualTo(0 + 2);
            assertThat(attack.notes()).isEqualTo("Family blade");
            assertThat(attack.versatileDiceSides()).isEqualTo(10);
        });
    }

    @Test
    void aToHitOverrideReplacesTheAttackBonusAndDisplayAsAttackShowsAnUnequippedWeapon() {
        String unequipped = objectMapper.writeValueAsString(read(armedSheetJson()).withItems(List.of(longsword(false))));
        assertThat(calculator.calculateVitals(unequipped).attacks()).doesNotContainKey("longsword");

        String customized = mutator.customize(unequipped, "items", "longsword", "{\"toHitOverride\":9,\"displayAsAttack\":true}");

        assertThat(calculator.calculateVitals(customized).attacks().get("longsword").toHit().value()).isEqualTo(9);
        String removed = mutator.removeCustomization(customized, "items", "longsword");
        assertThat(calculator.calculateVitals(removed).attacks()).doesNotContainKey("longsword");
    }

    @Test
    void aSpellCustomizationRenamesItAndCarriesItsAdjustmentsToTheRolls() {
        String customized = mutator.customize(armedSheetJson(), "spells", "fire-bolt",
                "{\"toHitBonus\":2,\"damageBonus\":3,\"dcOverride\":15,\"displayAsAttack\":true,\"name\":\"Ember\",\"notes\":\"Hot\"}");

        VitalsZone vitals = calculator.calculateVitals(customized);

        assertThat(vitals.spells().getFirst()).satisfies(spell -> {
            assertThat(spell.name()).isEqualTo("Ember");
            assertThat(spell.notes()).isEqualTo("Hot");
            assertThat(spell.adjustments().attack(5)).isEqualTo(7);
            assertThat(spell.adjustments().saveDc(13)).isEqualTo(15);
            assertThat(spell.adjustments().displayAsAttack()).isTrue();
        });
        ResolvedRoll damage = new Dnd5eMechanicResolver().resolve(vitals, RollKind.SPELL_DAMAGE, "fire-bolt", null);
        assertThat(damage.modifier()).isEqualTo(3);
        assertThat(damage.context()).isEqualTo("Ember: damage");
    }

    @Test
    void refusesAnUnknownItemOrSpell() {
        assertThatThrownBy(() -> mutator.customize(armedSheetJson(), "items", "missing", "{}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(armedSheetJson(), "spells", "missing", "{}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(armedSheetJson(), "items", "longsword", "{\"costOverride\":-1}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void refusesAnUnknownGroupOrAbilityAndAValueOutOfRange() {
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "luck", "strength", "{}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "abilities", "luck", "{}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheetJson(), "abilities", "strength", "{\"overrideScore\":31}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    private String sheetJson() {
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                20, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false,
                null, null, null, null, null, Map.of(), List.of(), null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    /** The base sheet with an equipped longsword (proficient, level 5 so +3) and Fire Bolt. */
    private String armedSheetJson() {
        Dnd5eSheet sheet = read(sheetJson())
                .withProficiencies(List.of(), List.of("Longsword"), List.of(), List.of())
                .withItems(List.of(longsword(true)))
                .withSpells(List.of(new Dnd5eSpell("fire-bolt", "Fire Bolt", "Wizard", 0, "evocation", "1 action", "120 feet",
                        false, false, true, 1, 10, "fire", "", "", false, false, "A mote of fire.", null, "V, S", null,
                        "Instantaneous", null, null, null)));
        return objectMapper.writeValueAsString(sheet);
    }

    private static Dnd5eItem longsword(boolean equipped) {
        return new Dnd5eItem("longsword", "Longsword", 1, "15 gp", "", equipped, false, false, "longsword", Dnd5eItemKind.WEAPON,
                "Martial Melee Weapon", null, null, 3.0, 15.0, Dnd5eWeaponCategory.MARTIAL, Dnd5eRangeCategory.MELEE, 1, 8,
                "slashing", 1, 10, List.of("Versatile"), false, null, null, null, null, false, null, null, null, null, null, null,
                null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    private Dnd5eSheet read(String sheetJson) {
        return objectMapper.readValue(sheetJson, Dnd5eSheet.class);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
