package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.CustomAction;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class Dnd5eSheetCalculatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void calculatesVitalsForAFifthLevelFighter() {
        Dnd5eSheet sheet = new Dnd5eSheet(16, 14, 14, 10, 12, 8, 5, 10, 30,
                Set.of("strength", "constitution"),
                Set.of("athletics", "perception"),
                List.of("Light", "Medium", "Heavy", "Shields"),
                List.of("Simple", "Martial"),
                List.of(),
                List.of("Common"),
                List.of("Poison"),
                List.of(),
                List.of(),
                List.of(),
                Set.of("prone"),
                0,
                30, 5, 0, true,
                List.of(new Dnd5eAttack("longsword", "Longsword", "5 ft", "strength", 1, 8, "slashing", "Melee Weapon", "")),
                List.of(new Dnd5eFeatureAction(
                        "secondWind", "Second Wind", Dnd5eActionType.BONUS_ACTION,
                        "Regain hit points.", 1, 0, Dnd5eRechargeTrigger.SHORT_OR_LONG_REST)),
                List.of(new Dnd5eSpellcastingClass("Fighter", "intelligence", Dnd5eSpellCastingType.KNOWN, 2, 4, null)),
                List.of(new Dnd5eSpell(
                        "fireBolt", "Fire Bolt", "Fighter", 0, "evocation", "1 Action", "120 feet", false, false,
                        true, 1, 10, "fire", "V, S", "Damage", false, false, "", null, "V, S", null, "Instantaneous",
                        null, null, null)),
                List.of(new Dnd5eItem("longsword-item", "Longsword", 1, "15 gp", "", true, false, false)),
                0, 0, 0, 45, 0,
                List.of(new Dnd5eFeatureTrait(
                        "extraAttack", "Extra Attack", Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Fighter",
                        "Attack twice, instead of once, whenever you take the Attack action.",
                        "Attack twice, instead of once, whenever you take the Attack action.", null, 0, null)),
                new Dnd5eBackground(
                        "Soldier", "Military Rank", "Soldiers loyal to your former military organization still recognize your authority.",
                        "Lawful Good", "I face problems head-on.", "I fight for those who cannot fight for themselves.",
                        "My honor is my life.", "I have little respect for anyone who is not a proven warrior.",
                        "Weathered and scarred.", "The King's Army", "Sergeant Kova", "The Crimson Blades", "Grew up in a border town.",
                        "", "", "", "", "", "", "", "", "", "", ""),
                List.of(new Dnd5eExtra("warhorseExtra", "Warhorse", Dnd5eExtraCategory.MOUNT, 11, 19, 12, 0, 60,
                        new Dnd5eExtraStatBlock(
                                "Large", "Beast", "Unaligned", "3d10 + 3", null,
                                List.of(
                                        new Dnd5eExtraAbilityScore("strength", 18, 4),
                                        new Dnd5eExtraAbilityScore("dexterity", 12, 1)),
                                List.of(), "Passive Perception 11", "--", "1/2 (XP 100; PB +2)",
                                List.of(), List.of(new Dnd5eExtraStatEntry("Hooves", "Melee Weapon Attack: +6 to hit."))))),
                1, List.of(new Dnd5eSpellSlotLevel(1, 3, 2)),
                List.of(new Dnd5eSpecialSense(Dnd5eSenseType.DARKVISION, 60)), List.of(), true, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.abilityScores().get("strength")).isEqualTo(16);
        assertThat(vitals.abilityScores().get("dexterity")).isEqualTo(14);
        assertThat(vitals.speed()).isEqualTo(30);
        assertThat(vitals.level()).isEqualTo(5);

        assertThat(vitals.abilityModifiers().get("strength").value()).isEqualTo(3);
        assertThat(vitals.abilityModifiers().get("dexterity").value()).isEqualTo(2);
        assertThat(vitals.abilityModifiers().get("constitution").value()).isEqualTo(2);
        assertThat(vitals.abilityModifiers().get("intelligence").value()).isEqualTo(0);
        assertThat(vitals.abilityModifiers().get("wisdom").value()).isEqualTo(1);
        assertThat(vitals.abilityModifiers().get("charisma").value()).isEqualTo(-1);

        assertThat(vitals.proficiencyBonus().value()).isEqualTo(3);

        assertThat(vitals.armorClass().value()).isEqualTo(12);
        assertThat(vitals.armorClass().contributions()).hasSize(2);

        assertThat(vitals.initiative().value()).isEqualTo(2);

        assertThat(vitals.hitPoints().value()).isEqualTo(44);
        assertThat(vitals.hitPoints().contributions()).hasSize(4);

        assertThat(vitals.savingThrowProficiencies().get("strength")).isEqualTo("FULL");
        assertThat(vitals.savingThrowProficiencies().get("constitution")).isEqualTo("FULL");
        assertThat(vitals.savingThrowProficiencies().get("dexterity")).isEqualTo("NONE");

        assertThat(vitals.savingThrows().get("strength").value()).isEqualTo(6);
        assertThat(vitals.savingThrows().get("strength").contributions()).hasSize(2);
        assertThat(vitals.savingThrows().get("dexterity").value()).isEqualTo(2);
        assertThat(vitals.savingThrows().get("dexterity").contributions()).hasSize(1);

        assertThat(vitals.senses().get("passivePerception").value()).isEqualTo(14);
        assertThat(vitals.senses().get("passivePerception").contributions()).hasSize(3);
        assertThat(vitals.senses().get("passiveInvestigation").value()).isEqualTo(10);
        assertThat(vitals.senses().get("passiveInsight").value()).isEqualTo(11);

        assertThat(vitals.specialSenses()).hasSize(1);
        assertThat(vitals.specialSenses().get(0).type()).isEqualTo("DARKVISION");
        assertThat(vitals.specialSenses().get(0).rangeFeet()).isEqualTo(60);
        assertThat(vitals.specialSenses().get(0).label()).isEqualTo("Darkvision 60 ft.");

        assertThat(vitals.armorProficiencies()).containsExactly("Light", "Medium", "Heavy", "Shields");
        assertThat(vitals.languages()).containsExactly("Common");

        assertThat(vitals.skillProficiencies().get("athletics")).isEqualTo("FULL");
        assertThat(vitals.skillProficiencies().get("stealth")).isEqualTo("NONE");

        assertThat(vitals.skills().get("athletics").value()).isEqualTo(6);
        assertThat(vitals.skills().get("athletics").contributions()).hasSize(2);
        assertThat(vitals.skills().get("stealth").value()).isEqualTo(2);
        assertThat(vitals.skills().get("stealth").contributions()).hasSize(1);

        assertThat(vitals.skillGoverningAbilities().get("athletics")).isEqualTo("strength");
        assertThat(vitals.skillGoverningAbilities().get("stealth")).isEqualTo("dexterity");
        assertThat(vitals.skillGoverningAbilities()).hasSize(18);

        assertThat(vitals.damageResistances()).containsExactly("Poison");
        assertThat(vitals.damageImmunities()).isEmpty();
        assertThat(vitals.damageVulnerabilities()).isEmpty();
        assertThat(vitals.conditionImmunities()).isEmpty();
        assertThat(vitals.activeConditions()).containsExactly("prone");

        assertThat(vitals.currentHitPoints()).isEqualTo(30);
        assertThat(vitals.temporaryHitPoints()).isEqualTo(5);
        assertThat(vitals.heroicInspiration()).isTrue();

        assertThat(vitals.attacks().get("longsword").toHit().value()).isEqualTo(6);
        assertThat(vitals.attacks().get("longsword").toHit().contributions()).hasSize(2);
        assertThat(vitals.attacks().get("longsword").damageDiceCount()).isEqualTo(1);
        assertThat(vitals.attacks().get("longsword").damageDiceSides()).isEqualTo(8);
        assertThat(vitals.attacks().get("longsword").damageModifier()).isEqualTo(3);
        assertThat(vitals.attacks().get("longsword").damageType()).isEqualTo("slashing");
        assertThat(vitals.attacks().get("longsword").category()).isEqualTo("Melee Weapon");

        assertThat(vitals.featureActions()).hasSize(1);
        assertThat(vitals.featureActions().get(0).name()).isEqualTo("Second Wind");
        assertThat(vitals.featureActions().get(0).actionType()).isEqualTo("BONUS_ACTION");
        assertThat(vitals.featureActions().get(0).maxUses()).isEqualTo(1);
        assertThat(vitals.featureActions().get(0).usedCount()).isZero();
        assertThat(vitals.featureActions().get(0).key()).isEqualTo("secondWind");
        assertThat(vitals.featureActions().get(0).rechargeTrigger()).isEqualTo("SHORT_OR_LONG_REST");

        assertThat(vitals.spellcasting()).hasSize(1);
        assertThat(vitals.spellcasting().get(0).className()).isEqualTo("Fighter");
        assertThat(vitals.spellcasting().get(0).spellcastingModifier().value()).isZero();
        assertThat(vitals.spellcasting().get(0).spellAttackBonus().value()).isEqualTo(3);
        assertThat(vitals.spellcasting().get(0).spellSaveDc().value()).isEqualTo(11);
        assertThat(vitals.spellcasting().get(0).spellSaveDc().contributions()).hasSize(3);

        assertThat(vitals.spells()).hasSize(1);
        assertThat(vitals.spells().get(0).key()).isEqualTo("fireBolt");
        assertThat(vitals.spells().get(0).name()).isEqualTo("Fire Bolt");
        assertThat(vitals.spells().get(0).level()).isZero();
        assertThat(vitals.spells().get(0).concentration()).isFalse();
        assertThat(vitals.spells().get(0).ritual()).isFalse();
        assertThat(vitals.spells().get(0).attackRoll()).isTrue();
        assertThat(vitals.spells().get(0).damageDiceCount()).isEqualTo(1);
        assertThat(vitals.spells().get(0).damageDiceSides()).isEqualTo(10);
        assertThat(vitals.spells().get(0).damageType()).isEqualTo("fire");

        assertThat(vitals.items()).hasSize(1);
        assertThat(vitals.items().get(0).name()).isEqualTo("Longsword");
        assertThat(vitals.items().get(0).equipped()).isTrue();
        assertThat(vitals.items().get(0).attuned()).isFalse();

        assertThat(vitals.coins().gold()).isEqualTo(45);
        assertThat(vitals.coins().copper()).isZero();

        assertThat(vitals.featureTraits()).hasSize(1);
        assertThat(vitals.featureTraits().get(0).name()).isEqualTo("Extra Attack");
        assertThat(vitals.featureTraits().get(0).category()).isEqualTo("CLASS_FEATURE");
        assertThat(vitals.featureTraits().get(0).source()).isEqualTo("Fighter");
        assertThat(vitals.featureTraits().get(0).maxUses()).isNull();

        assertThat(vitals.background().name()).isEqualTo("Soldier");
        assertThat(vitals.background().featureName()).isEqualTo("Military Rank");
        assertThat(vitals.background().alignment()).isEqualTo("Lawful Good");

        assertThat(vitals.extras()).hasSize(1);
        assertThat(vitals.extras().get(0).name()).isEqualTo("Warhorse");
        assertThat(vitals.extras().get(0).category()).isEqualTo("MOUNT");
        assertThat(vitals.extras().get(0).armorClass()).isEqualTo(11);
        assertThat(vitals.extras().get(0).maxHitPoints()).isEqualTo(19);
        assertThat(vitals.extras().get(0).currentHitPoints()).isEqualTo(12);
        assertThat(vitals.extras().get(0).statBlock().abilityScores().get(0).modifier()).isEqualTo(4);
        assertThat(vitals.extras().get(0).statBlock().initiativeBonus()).isEqualTo(1);
        assertThat(vitals.extras().get(0).statBlock().actions().get(0).name()).isEqualTo("Hooves");

        assertThat(vitals.hitDice().dieSize()).isEqualTo(10);
        assertThat(vitals.hitDice().max()).isEqualTo(5);
        assertThat(vitals.hitDice().used()).isEqualTo(1);

        assertThat(vitals.spellSlots()).hasSize(1);
        assertThat(vitals.spellSlots().get(0).level()).isEqualTo(1);
        assertThat(vitals.spellSlots().get(0).maxSlots()).isEqualTo(3);
        assertThat(vitals.spellSlots().get(0).usedSlots()).isEqualTo(2);
    }

    @Test
    void spellcastingUsesItsOwnAbilityNotAlwaysWisdom() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 18, 10, 5, 8);
        Dnd5eSheet withSpellcasting = new Dnd5eSheet(
                sheet.strength(), sheet.dexterity(), sheet.constitution(), sheet.intelligence(), sheet.wisdom(),
                sheet.charisma(), sheet.level(), sheet.hitDieSize(), sheet.speed(), sheet.savingThrowProficiencies(),
                sheet.skillProficiencies(), sheet.armorProficiencies(), sheet.weaponProficiencies(),
                sheet.toolProficiencies(), sheet.languages(), sheet.damageResistances(), sheet.damageImmunities(),
                sheet.damageVulnerabilities(), sheet.conditionImmunities(), sheet.activeConditions(),
                sheet.exhaustionLevel(),
                sheet.currentHitPoints(), sheet.temporaryHitPoints(), sheet.maxHitPointsAdjustment(),
                sheet.heroicInspiration(), sheet.attacks(),
                sheet.featureActions(),
                List.of(new Dnd5eSpellcastingClass("Cleric", "wisdom", Dnd5eSpellCastingType.PREPARED, 4, null, 3)),
                sheet.spells(),
                sheet.items(), sheet.copperPieces(), sheet.silverPieces(), sheet.electrumPieces(), sheet.goldPieces(),
                sheet.platinumPieces(), sheet.featureTraits(), sheet.background(), sheet.extras(), sheet.hitDiceUsed(), sheet.spellSlots(), sheet.specialSenses(), sheet.customActions(), sheet.trackEncumbrance(), sheet.build(), sheet.skillExpertise(), sheet.classLevels(), sheet.hitPointBase(), sheet.derivation(), sheet.schemaVersion());

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(withSpellcasting));

        assertThat(vitals.spellcasting().get(0).spellcastingModifier().value()).isEqualTo(4);
        assertThat(vitals.spellcasting().get(0).spellAttackBonus().value()).isEqualTo(7);
    }

    @Test
    void anAttacksToHitUsesItsOwnAbilityModifierNotStrength() {
        Dnd5eSheet sheet = emptySheet(10, 16, 10, 10, 10, 10, 5, 8);
        Dnd5eSheet withAttack = new Dnd5eSheet(
                sheet.strength(), sheet.dexterity(), sheet.constitution(), sheet.intelligence(), sheet.wisdom(),
                sheet.charisma(), sheet.level(), sheet.hitDieSize(), sheet.speed(), sheet.savingThrowProficiencies(),
                sheet.skillProficiencies(), sheet.armorProficiencies(), sheet.weaponProficiencies(),
                sheet.toolProficiencies(), sheet.languages(), sheet.damageResistances(), sheet.damageImmunities(),
                sheet.damageVulnerabilities(), sheet.conditionImmunities(), sheet.activeConditions(),
                sheet.exhaustionLevel(),
                sheet.currentHitPoints(), sheet.temporaryHitPoints(), sheet.maxHitPointsAdjustment(),
                sheet.heroicInspiration(),
                List.of(new Dnd5eAttack("shortbow", "Shortbow", "80/320 ft", "dexterity", 1, 6, "piercing", "Ranged Weapon", "")),
                sheet.featureActions(), sheet.spellcastingClasses(), sheet.spells(), sheet.items(),
                sheet.copperPieces(), sheet.silverPieces(), sheet.electrumPieces(), sheet.goldPieces(),
                sheet.platinumPieces(), sheet.featureTraits(), sheet.background(), sheet.extras(), sheet.hitDiceUsed(), sheet.spellSlots(), sheet.specialSenses(), sheet.customActions(), sheet.trackEncumbrance(), sheet.build(), sheet.skillExpertise(), sheet.classLevels(), sheet.hitPointBase(), sheet.derivation(), sheet.schemaVersion());

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(withAttack));

        assertThat(vitals.attacks().get("shortbow").toHit().value()).isEqualTo(6);
        assertThat(vitals.attacks().get("shortbow").damageModifier()).isEqualTo(3);
        assertThat(vitals.attacks().get("shortbow").actionType()).isEqualTo("ACTION");
    }

    @Test
    void aDisplayAsAttackCustomActionFoldsIntoAttacksUsingProficiencyOnlyWhenSet() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 16, 5, 8)
                .withCustomActions(List.of(customAction(true, true, Dnd5eActivationType.ACTION)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("customAction2").toHit().value()).isEqualTo(6);
        assertThat(vitals.attacks().get("customAction2").damageModifier()).isEqualTo(4);
        assertThat(vitals.customActions()).hasSize(1);
    }

    @Test
    void aDisplayAsAttackCustomActionOmitsProficiencyBonusWhenNotProficient() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 16, 5, 8)
                .withCustomActions(List.of(customAction(true, false, Dnd5eActivationType.ACTION)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("customAction2").toHit().value()).isEqualTo(3);
    }

    @Test
    void aCustomActionWithoutAnActivationTypeIsNotAnAttack() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 16, 5, 8)
                .withCustomActions(List.of(customAction(true, true, null)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks()).doesNotContainKey("customAction2");
        assertThat(vitals.customActions()).singleElement().extracting(CustomAction::activationType).isNull();
    }

    @Test
    void aNonDisplayAsAttackCustomActionStaysListOnly() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 16, 5, 8)
                .withCustomActions(List.of(customAction(false, true, Dnd5eActivationType.ACTION)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks()).doesNotContainKey("customAction2");
        assertThat(vitals.customActions()).hasSize(1);
    }

    /**
     * The Actions tab buckets each section's own attack table by this field
     * (systems/dnd-5e/sheet-build.md's "Tabbed section" row) — a displayAsAttack custom
     * action folded into `attacks` must carry its own real activation type
     * through {@link Dnd5eActivationType#toActionType()}, not silently default
     * to ACTION like every other attack source (catalog weapon, equipped item)
     * does, per {@link AttackRow}'s own doc comment.
     */
    @Test
    void aDisplayAsAttackCustomActionCarriesItsOwnActionTypeThroughTheFold() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 16, 5, 8)
                .withCustomActions(List.of(customAction(true, true, Dnd5eActivationType.BONUS_ACTION)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("customAction2").actionType()).isEqualTo("BONUS_ACTION");
    }

    private Dnd5eCustomAction customAction(boolean displayAsAttack, boolean proficient, Dnd5eActivationType activationType) {
        return new Dnd5eCustomAction(
                "customAction2", Dnd5eCustomActionTemplate.SPELL, "Custom Action 2", "", "",
                Dnd5eRangeCategory.RANGED, 180, "charisma", 1, 8, 1, "force", null, null,
                Dnd5eSpellRangeType.RANGED, null, null, activationType, null, false, proficient,
                displayAsAttack, null, null, false, false);
    }

    @Test
    void anEquippedWeaponItemFoldsIntoAttacksWhenProficient() {
        Dnd5eSheet sheet = emptySheet(16, 10, 10, 10, 10, 10, 5, 8)
                .withProficiencies(List.of(), List.of("Martial"), List.of(), List.of())
                .withItems(List.of(weaponItem("warhammer-item", "Warhammer")));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("warhammer-item").toHit().value()).isEqualTo(6);
        assertThat(vitals.attacks().get("warhammer-item").toHit().contributions()).hasSize(2);
        assertThat(vitals.attacks().get("warhammer-item").damageDiceCount()).isEqualTo(1);
        assertThat(vitals.attacks().get("warhammer-item").damageDiceSides()).isEqualTo(8);
        assertThat(vitals.attacks().get("warhammer-item").damageModifier()).isEqualTo(3);
        assertThat(vitals.attacks().get("warhammer-item").damageType()).isEqualTo("bludgeoning");
        assertThat(vitals.attacks().get("warhammer-item").category()).isEqualTo("Melee Weapon");
        assertThat(vitals.attacks().get("warhammer-item").range()).isEqualTo("5 ft. Reach");
        assertThat(vitals.attacks().get("warhammer-item").actionType()).isEqualTo("ACTION");
    }

    @Test
    void anEquippedWeaponItemOmitsProficiencyBonusWhenNotProficient() {
        Dnd5eSheet sheet = emptySheet(16, 10, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(weaponItem("warhammer-item", "Warhammer")));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("warhammer-item").toHit().value()).isEqualTo(3);
        assertThat(vitals.attacks().get("warhammer-item").toHit().contributions()).hasSize(1);
    }

    @Test
    void anUnequippedWeaponItemNeverAppearsInAttacks() {
        Dnd5eItem unequipped = new Dnd5eItem(
                "warhammer-item", "Warhammer", 1, "15 gp", "", false, false, false, "warhammer",
                Dnd5eItemKind.WEAPON, "Melee Weapon", "none", null, 2.0, 15.0, Dnd5eWeaponCategory.MARTIAL,
                Dnd5eRangeCategory.MELEE, 1, 8, "bludgeoning", 1, 10, List.of("Versatile"), false, null, null,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
        Dnd5eSheet sheet = emptySheet(16, 10, 10, 10, 10, 10, 5, 8).withItems(List.of(unequipped));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks()).doesNotContainKey("warhammer-item");
    }

    @Test
    void aFinesseWeaponUsesTheBetterOfStrengthOrDexterity() {
        Dnd5eItem rapier = new Dnd5eItem(
                "rapier-item", "Rapier", 1, "25 gp", "", true, false, false, "rapier",
                Dnd5eItemKind.WEAPON, "Melee Weapon", "none", null, 2.0, 25.0, Dnd5eWeaponCategory.MARTIAL,
                Dnd5eRangeCategory.MELEE, 1, 8, "piercing", null, null, List.of("Finesse"), true, null, null,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
        Dnd5eSheet sheet = emptySheet(10, 16, 10, 10, 10, 10, 5, 8).withItems(List.of(rapier));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("rapier-item").toHit().value()).isEqualTo(3);
        assertThat(vitals.attacks().get("rapier-item").damageModifier()).isEqualTo(3);
    }

    @Test
    void aRangedWeaponAlwaysUsesDexterity() {
        Dnd5eItem shortbow = new Dnd5eItem(
                "shortbow-item", "Shortbow", 1, "25 gp", "", true, false, false, "shortbow",
                Dnd5eItemKind.WEAPON, "Ranged Weapon", "none", null, 2.0, 25.0, Dnd5eWeaponCategory.SIMPLE,
                Dnd5eRangeCategory.RANGED, 1, 6, "piercing", null, null, List.of("Ammunition"), false, 80, 320,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
        Dnd5eSheet sheet = emptySheet(18, 16, 10, 10, 10, 10, 5, 8).withItems(List.of(shortbow));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("shortbow-item").toHit().value()).isEqualTo(3);
        assertThat(vitals.attacks().get("shortbow-item").range()).isEqualTo("80/320 ft.");
    }

    @Test
    void aNamedMagicWeaponsLiteralBonusAddsToHitAndDamage() {
        Dnd5eItem magicBlade = new Dnd5eItem(
                "blade-item", "Blade of the Sun", 1, "", "", true, true, true, "blade-of-the-sun",
                Dnd5eItemKind.WEAPON, "Melee Weapon", "rare", null, 3.0, null, Dnd5eWeaponCategory.MARTIAL,
                Dnd5eRangeCategory.MELEE, 1, 8, "slashing", null, null, List.of(), false, null, null,
                null, null, false, null, 1, 1, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
        Dnd5eSheet sheet = emptySheet(16, 10, 10, 10, 10, 10, 5, 8)
                .withProficiencies(List.of(), List.of("Martial"), List.of(), List.of())
                .withItems(List.of(magicBlade));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.attacks().get("blade-item").toHit().value()).isEqualTo(7);
        assertThat(vitals.attacks().get("blade-item").toHit().contributions()).hasSize(3);
        assertThat(vitals.attacks().get("blade-item").damageModifier()).isEqualTo(4);
    }

    @Test
    void mediumArmorCapsTheDexterityBonusAtTwo() {
        Dnd5eSheet sheet = emptySheet(10, 18, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(armorItem("armor-item", "Half Plate", Dnd5eArmorCategory.MEDIUM, 12, true)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(14);
        assertThat(vitals.armorClass().contributions()).containsExactly(
                new Contribution("Base (Half Plate)", 12),
                new Contribution("Dexterity modifier", 2));
    }

    @Test
    void lightArmorAppliesTheFullDexterityBonus() {
        Dnd5eSheet sheet = emptySheet(10, 18, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(armorItem("armor-item", "Studded Leather", Dnd5eArmorCategory.LIGHT, 12, true)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(16);
    }

    @Test
    void heavyArmorAllowsNoDexterityBonusButStillShowsItsContribution() {
        Dnd5eSheet sheet = emptySheet(10, 18, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(armorItem("armor-item", "Plate", Dnd5eArmorCategory.HEAVY, 18, true)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(18);
        assertThat(vitals.armorClass().contributions()).contains(new Contribution("Dexterity modifier", 0));
    }

    @Test
    void anEquippedShieldAddsOnTopOfArmor() {
        Dnd5eSheet sheet = emptySheet(10, 18, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(
                        armorItem("armor-item", "Half Plate", Dnd5eArmorCategory.MEDIUM, 12, true),
                        shieldItem("shield-item", true)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(16);
    }

    @Test
    void anEquippedShieldAddsOnTopOfUnarmored() {
        Dnd5eSheet sheet = emptySheet(10, 14, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(shieldItem("shield-item", true)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(14);
    }

    @Test
    void anUnequippedArmorOrShieldNeverContributesToArmorClass() {
        Dnd5eSheet sheet = emptySheet(10, 14, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(
                        armorItem("armor-item", "Half Plate", Dnd5eArmorCategory.MEDIUM, 12, false),
                        shieldItem("shield-item", false)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.armorClass().value()).isEqualTo(12);
    }

    @Test
    void carriedWeightSumsEquipmentAndBackpackAndCoinsInKilogramsExcludingOtherPossessions() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(
                        weightedItem("sword", 3.0, Dnd5eStorageLocation.EQUIPMENT),
                        weightedItem("rope", 10.0, Dnd5eStorageLocation.BACKPACK),
                        weightedItem("deed", 1000.0, Dnd5eStorageLocation.OTHER_POSSESSIONS)))
                .withCoins(0, 0, 0, 50, 0);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        // (3 + 10) lb items + (50 coins / 50) lb coins = 14 lb -> 14 * 0.453592 kg, rounded to one decimal.
        assertThat(vitals.encumbrance().carriedWeightKg()).isEqualTo(6.4);
    }

    @Test
    void overloadedIsAlwaysFalseWhenTrackEncumbranceIsOff() {
        Dnd5eSheet sheet = emptySheet(3, 10, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(weightedItem("anvil", 500.0, Dnd5eStorageLocation.EQUIPMENT)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.encumbrance().trackWeight()).isFalse();
        assertThat(vitals.encumbrance().overloaded()).isFalse();
    }

    @Test
    void overloadedIsTrueWhenTrackedAndCarriedWeightExceedsCapacity() {
        Dnd5eSheet sheet = emptySheet(3, 10, 10, 10, 10, 10, 5, 8)
                .withTrackEncumbrance(true)
                .withItems(List.of(weightedItem("anvil", 500.0, Dnd5eStorageLocation.EQUIPMENT)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.encumbrance().overloaded()).isTrue();
    }

    @Test
    void bagOfHoldingSectionOnlyAppearsWhenOwnedAndEquippedAndItsContentsAreExcludedFromCarriedWeight() {
        Dnd5eSheet withoutBag = emptySheet(10, 10, 10, 10, 10, 10, 5, 8);
        assertThat(calculator.calculateVitals(objectMapper.writeValueAsString(withoutBag))
                .encumbrance().sections().stream().map(section -> section.location()))
                .doesNotContain("BAG_OF_HOLDING");

        Dnd5eItem bag = weightedItem("bag-of-holding", 15.0, Dnd5eStorageLocation.EQUIPMENT);
        Dnd5eItem bagEquipped = new Dnd5eItem(
                bag.key(), "Bag of Holding", bag.quantity(), bag.cost(), bag.notes(), true, bag.attuned(), false,
                bag.catalogueSlug(), bag.itemKind(), bag.typeLabel(), bag.rarity(), bag.attunementRequirement(),
                bag.weightLb(), bag.costGp(), bag.weaponCategory(), bag.rangeCategory(), bag.damageDiceCount(),
                bag.damageDiceSides(), bag.damageType(), bag.versatileDamageDiceCount(), bag.versatileDamageDiceSides(),
                bag.properties(), bag.finesse(), bag.normalRange(), bag.longRange(), bag.armorCategory(),
                bag.baseArmorClass(), bag.stealthDisadvantage(), bag.strengthRequirement(), bag.weaponAttackBonus(),
                bag.weaponDamageBonus(), bag.armorClassBonus(), bag.charges(), bag.rechargeTrigger(),
                bag.rechargeFormula(), bag.grantedSpells(), bag.chargesUsed(), bag.storageLocation(), bag.source());
        Dnd5eSheet withBag = emptySheet(10, 10, 10, 10, 10, 10, 5, 8)
                .withItems(List.of(bagEquipped, weightedItem("gold-bars", 200.0, Dnd5eStorageLocation.BAG_OF_HOLDING)));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(withBag));

        assertThat(vitals.encumbrance().sections().stream().map(section -> section.location()))
                .contains("BAG_OF_HOLDING");
        // Only the bag's own 15 lb counts, never the 200 lb of gold bars stored inside it.
        assertThat(vitals.encumbrance().carriedWeightKg()).isEqualTo(6.8);
    }

    private Dnd5eItem weightedItem(String key, double weightLb, Dnd5eStorageLocation location) {
        return new Dnd5eItem(
                key, key, 1, "", "", false, false, false, null,
                Dnd5eItemKind.GEAR, null, "none", null, weightLb, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                null, null, false, null, null, null, null,
                null, null, null, List.of(), 0, location, null);
    }

    private Dnd5eItem armorItem(String key, String name, Dnd5eArmorCategory category, int baseArmorClass, boolean equipped) {
        return new Dnd5eItem(
                key, name, 1, "", "", equipped, false, false, name.toLowerCase(),
                Dnd5eItemKind.ARMOR, "Armor", "none", null, null, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                category, baseArmorClass, false, null, null, null, null,
                null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    private Dnd5eItem shieldItem(String key, boolean equipped) {
        return new Dnd5eItem(
                key, "Shield", 1, "", "", equipped, false, false, "shield",
                Dnd5eItemKind.SHIELD, "Shield", "none", null, null, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                null, 2, false, null, null, null, null,
                null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    private Dnd5eItem weaponItem(String key, String name) {
        return new Dnd5eItem(
                key, name, 1, "15 gp", "", true, false, false, name.toLowerCase(),
                Dnd5eItemKind.WEAPON, "Melee Weapon", "none", null, 2.0, 15.0, Dnd5eWeaponCategory.MARTIAL,
                Dnd5eRangeCategory.MELEE, 1, 8, "bludgeoning", 1, 10, List.of("Versatile"), false, null, null,
                null, null, false, null, null, null, null, null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    @Test
    void negativeAbilityModifiersFloorTowardNegativeInfinity() {
        Dnd5eSheet sheet = emptySheet(7, 10, 10, 10, 10, 10, 1, 8);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.abilityModifiers().get("strength").value()).isEqualTo(-2);
    }

    @Test
    void firstLevelHitPointsHaveNoAverageRollContribution() {
        Dnd5eSheet sheet = emptySheet(10, 10, 10, 10, 10, 10, 1, 8);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.hitPoints().value()).isEqualTo(8);
        assertThat(vitals.hitPoints().contributions()).hasSize(2);
    }

    @Test
    void maxHpModifierAddsAVisibleContributionAndKeepsTheCalculatedMaximum() {
        Dnd5eSheet sheet = withHitPoints(emptySheet(10, 10, 10, 10, 10, 10, 1, 8), "maxModifier", 3);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.hitPoints().value()).isEqualTo(11);
        assertThat(vitals.calculatedMaxHitPoints()).isEqualTo(8);
        assertThat(vitals.hitPoints().contributions())
                .anyMatch(c -> c.source().equals("Max HP Modifier") && c.amount() == 3);
    }

    @Test
    void overrideMaxHpReplacesTheCalculatedLinesAndTheModifier() {
        Dnd5eSheet sheet = withHitPoints(withHitPoints(emptySheet(10, 10, 10, 10, 10, 10, 1, 8), "maxModifier", 3), "overrideMax", 5);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.hitPoints().value()).isEqualTo(5);
        assertThat(vitals.hitPoints().contributions()).extracting(c -> c.source()).containsExactly("Override Max HP");
        assertThat(vitals.calculatedMaxHitPoints()).isEqualTo(8);
    }

    @Test
    void maxHitPointsNeverDropsBelowOneEvenWithAPunitiveModifier() {
        Dnd5eSheet sheet = withHitPoints(emptySheet(10, 10, 10, 10, 10, 10, 1, 8), "maxModifier", -99);

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(sheet));

        assertThat(vitals.hitPoints().value()).isEqualTo(1);
    }

    @Test
    void handAddedProficienciesJoinTheListsOnceAndAnUnnamedOneGrantsNothing() {
        Dnd5eSheet base = emptySheet(10, 10, 10, 10, 10, 10, 1, 8).withProficiencies(List.of(), List.of(), List.of(), List.of("Common"));
        Dnd5eCustomizations customizations = base.customizationsOrEmpty()
                .withProficiency(new Dnd5eCustomProficiency("a", Dnd5eCustomProficiency.Type.LANGUAGE, "Elvish", false, null))
                .withProficiency(new Dnd5eCustomProficiency("b", Dnd5eCustomProficiency.Type.LANGUAGE, "common", false, null))
                .withProficiency(new Dnd5eCustomProficiency("c", Dnd5eCustomProficiency.Type.TOOL, "", true, null));

        VitalsZone vitals = calculator.calculateVitals(objectMapper.writeValueAsString(base.withCustomizations(customizations)));

        assertThat(vitals.languages()).containsExactly("Common", "Elvish");
        assertThat(vitals.toolProficiencies()).isEmpty();
    }

    private static Dnd5eSheet withHitPoints(Dnd5eSheet sheet, String field, int value) {
        return sheet.withCustomizations(sheet.customizationsOrEmpty().withHitPointsField(field, new Dnd5eNotedValue(value, null)));
    }

    private Dnd5eSheet emptySheet(
            int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, int level, int hitDieSize) {
        return new Dnd5eSheet(strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                0, 0, 0, false, List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(),
                emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
