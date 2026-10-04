package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.AttunementLimitExceededException;
import dev.omnisheetvault.api.ruleset.AttunementNotAllowedException;
import dev.omnisheetvault.api.ruleset.CustomAction;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.InvalidBackgroundFieldException;
import dev.omnisheetvault.api.ruleset.InvalidCoinDenominationException;
import dev.omnisheetvault.api.ruleset.InvalidConditionException;
import dev.omnisheetvault.api.ruleset.InvalidCustomActionFieldException;
import dev.omnisheetvault.api.ruleset.InvalidCustomizationException;
import dev.omnisheetvault.api.ruleset.InvalidItemFieldException;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.ItemGrantedSpell;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellLimitExceededException;
import dev.omnisheetvault.api.ruleset.SpellPreparationLimitExceededException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class Dnd5eSheetMutatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);

    @Test
    void damageConsumesTemporaryHitPointsFirst() {
        String sheet = sheetJson(30, 5);

        Dnd5eSheet result = read(mutator.applyDamage(sheet, 8));

        assertThat(result.temporaryHitPoints()).isZero();
        assertThat(result.currentHitPoints()).isEqualTo(27);
    }

    @Test
    void damageWithinTemporaryHitPointsLeavesCurrentUntouched() {
        String sheet = sheetJson(30, 5);

        Dnd5eSheet result = read(mutator.applyDamage(sheet, 3));

        assertThat(result.temporaryHitPoints()).isEqualTo(2);
        assertThat(result.currentHitPoints()).isEqualTo(30);
    }

    @Test
    void damageNeverDropsCurrentHitPointsBelowZero() {
        String sheet = sheetJson(5, 0);

        Dnd5eSheet result = read(mutator.applyDamage(sheet, 20));

        assertThat(result.currentHitPoints()).isZero();
    }

    @Test
    void healingNeverExceedsMaxHitPoints() {
        String sheet = sheetJson(40, 0);

        Dnd5eSheet result = read(mutator.applyHealing(sheet, 10));

        assertThat(result.currentHitPoints()).isEqualTo(44);
    }

    @Test
    void healingDoesNotRestoreTemporaryHitPoints() {
        String sheet = sheetJson(30, 5);

        Dnd5eSheet result = read(mutator.applyHealing(sheet, 4));

        assertThat(result.temporaryHitPoints()).isEqualTo(5);
        assertThat(result.currentHitPoints()).isEqualTo(34);
    }

    @Test
    void temporaryHitPointsDoNotStackAndTakeTheHigherValue() {
        String sheet = sheetJson(30, 5);

        Dnd5eSheet lower = read(mutator.setTemporaryHitPoints(sheet, 3));
        Dnd5eSheet higher = read(mutator.setTemporaryHitPoints(sheet, 8));

        assertThat(lower.temporaryHitPoints()).isEqualTo(5);
        assertThat(higher.temporaryHitPoints()).isEqualTo(8);
    }

    @Test
    void maxHpModifierRaisesTheMaximumAndCurrentKeepsTheDamageTaken() {
        String sheet = sheetJson(40, 0);

        Dnd5eSheet modified = read(mutator.customize(sheet, "hitPoints", "maxModifier", "{\"value\":3}"));

        assertThat(Dnd5eFormulas.maxHitPoints(modified)).isEqualTo(47);
        assertThat(modified.currentHitPoints()).isEqualTo(43);
        assertThat(read(mutator.applyHealing(objectMapper.writeValueAsString(modified), 10)).currentHitPoints()).isEqualTo(47);
    }

    @Test
    void overrideMaxHpReplacesTheModifiedMaximumAndClearingItRestoresIt() {
        String modified = mutator.customize(sheetJson(44, 0), "hitPoints", "maxModifier", "{\"value\":3}");

        String overridden = mutator.customize(modified, "hitPoints", "overrideMax", "{\"value\":20}");
        Dnd5eSheet cleared = read(mutator.removeCustomization(overridden, "hitPoints", "overrideMax"));

        assertThat(Dnd5eFormulas.maxHitPoints(read(overridden))).isEqualTo(20);
        assertThat(read(overridden).currentHitPoints()).isEqualTo(20);
        assertThat(Dnd5eFormulas.maxHitPoints(cleared)).isEqualTo(47);
        assertThat(cleared.currentHitPoints()).isEqualTo(47);
    }

    @Test
    void aCustomizedMaximumLeavesADownedCharacterAtZero() {
        Dnd5eSheet modified = read(mutator.customize(sheetJson(0, 0), "hitPoints", "maxModifier", "{\"value\":5}"));

        assertThat(modified.currentHitPoints()).isZero();
    }

    @Test
    void anUnknownHitPointsFieldIsRefused() {
        assertThatThrownBy(() -> mutator.customize(sheetJson(30, 0), "hitPoints", "manualAdjustment", "{\"value\":2}"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void aLegacyMaxHitPointsAdjustmentReadsAsTheMaxHpModifier() {
        ObjectNode legacy = (ObjectNode) objectMapper.readTree(sheetJson(44, 0));
        legacy.put("maxHitPointsAdjustment", 2);

        Dnd5eSheet sheet = read(objectMapper.writeValueAsString(legacy));

        assertThat(sheet.maxHitPointsAdjustment()).isZero();
        assertThat(sheet.customizationsOrEmpty().hitPointsField("maxModifier").value()).isEqualTo(2);
        assertThat(Dnd5eFormulas.maxHitPoints(sheet)).isEqualTo(46);
    }

    @Test
    void togglingInspirationFlipsTheBoolean() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.toggleInspiration(sheet));

        assertThat(result.heroicInspiration()).isTrue();
        assertThat(read(mutator.toggleInspiration(objectMapper.writeValueAsString(result))).heroicInspiration()).isFalse();
    }

    @Test
    void togglingAnInactiveConditionActivatesIt() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.toggleCondition(sheet, "poisoned"));

        assertThat(result.activeConditions()).containsExactly("poisoned");
    }

    @Test
    void togglingAnActiveConditionDeactivatesIt() {
        String sheet = sheetJson(30, 0);
        String poisoned = mutator.toggleCondition(sheet, "poisoned");

        Dnd5eSheet result = read(mutator.toggleCondition(poisoned, "poisoned"));

        assertThat(result.activeConditions()).isEmpty();
    }

    @Test
    void togglingAnUnknownConditionIsRejected() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.toggleCondition(sheet, "not-a-condition"))
                .isInstanceOf(InvalidConditionException.class);
    }

    @Test
    void anExistingProficiencyIsAddedWithItsNameAndKeepsTheBuildLists() {
        String sheet = mutator.customize(sheetJson(30, 0), "proficiencies", "new", "{\"type\":\"LANGUAGE\",\"name\":\"Elvish\"}");

        Dnd5eSheet result = read(sheet);

        assertThat(result.languages()).isEmpty();
        assertThat(result.customizationsOrEmpty().proficiencies())
                .singleElement()
                .satisfies(added -> {
                    assertThat(added.type()).isEqualTo(Dnd5eCustomProficiency.Type.LANGUAGE);
                    assertThat(added.name()).isEqualTo("Elvish");
                    assertThat(added.custom()).isFalse();
                });
    }

    @Test
    void aCustomProficiencyStartsUnnamedAndTakesItsNameAndNotesLater() {
        String added = mutator.customize(sheetJson(30, 0), "proficiencies", "new", "{\"type\":\"TOOL\",\"custom\":true}");
        String key = read(added).customizationsOrEmpty().proficiencies().getFirst().key();

        Dnd5eCustomProficiency named = read(mutator.customize(added, "proficiencies", key, "{\"name\":\"Glassblower's Tools\",\"notes\":\"Guild\"}"))
                .customizationsOrEmpty().proficiencies().getFirst();

        assertThat(named.name()).isEqualTo("Glassblower's Tools");
        assertThat(named.notes()).isEqualTo("Guild");
    }

    @Test
    void anExistingProficiencyKeepsItsNameWhenEdited() {
        String added = mutator.customize(sheetJson(30, 0), "proficiencies", "new", "{\"type\":\"WEAPON\",\"name\":\"Longsword\"}");
        String key = read(added).customizationsOrEmpty().proficiencies().getFirst().key();

        Dnd5eCustomProficiency edited = read(mutator.customize(added, "proficiencies", key, "{\"name\":\"Other\",\"notes\":\"Feat\"}"))
                .customizationsOrEmpty().proficiencies().getFirst();

        assertThat(edited.name()).isEqualTo("Longsword");
        assertThat(edited.notes()).isEqualTo("Feat");
    }

    @Test
    void removingAHandAddedProficiencyDropsIt() {
        String added = mutator.customize(sheetJson(30, 0), "proficiencies", "new", "{\"type\":\"ARMOR\",\"name\":\"Shield\"}");
        String key = read(added).customizationsOrEmpty().proficiencies().getFirst().key();

        Dnd5eSheet result = read(mutator.removeCustomization(added, "proficiencies", key));

        assertThat(result.customizationsOrEmpty().proficiencies()).isEmpty();
    }

    @Test
    void aCustomArmorOrAnUnnamedExistingProficiencyIsRefused() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.customize(sheet, "proficiencies", "new", "{\"type\":\"ARMOR\",\"custom\":true}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.customize(sheet, "proficiencies", "new", "{\"type\":\"LANGUAGE\"}"))
                .isInstanceOf(InvalidCustomizationException.class);
        assertThatThrownBy(() -> mutator.removeCustomization(sheet, "proficiencies", "missing"))
                .isInstanceOf(InvalidCustomizationException.class);
    }

    @Test
    void addingAnItemAppendsItWithAGeneratedKey() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.addItem(sheet, "Rope, hempen", 1, "1 gp", "50 feet", false, null));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).key()).isNotBlank();
        assertThat(result.items().get(0).name()).isEqualTo("Rope, hempen");
        assertThat(result.items().get(0).equipped()).isFalse();
        assertThat(result.items().get(0).attuned()).isFalse();
        assertThat(result.items().get(0).requiresAttunement()).isFalse();
        assertThat(result.items().get(0).storageLocation()).isEqualTo(Dnd5eStorageLocation.EQUIPMENT);
    }

    @Test
    void addingAnItemWithAStorageLocationPlacesItThere() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.addItem(sheet, "Bedroll", 1, "1 gp", "", false, "OTHER_POSSESSIONS"));

        assertThat(result.items().get(0).storageLocation()).isEqualTo(Dnd5eStorageLocation.OTHER_POSSESSIONS);
    }

    @Test
    void addingAnItemWithAnUnknownStorageLocationThrows() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.addItem(sheet, "Bedroll", 1, "1 gp", "", false, "NOT_A_LOCATION"))
                .isInstanceOf(InvalidItemFieldException.class);
    }

    @Test
    void removingAnItemDropsIt() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Rope, hempen", 1, "1 gp", "50 feet", false, null);
        String itemKey = read(sheet).items().get(0).key();

        Dnd5eSheet result = read(mutator.removeItem(sheet, itemKey));

        assertThat(result.items()).isEmpty();
    }

    @Test
    void addingACatalogueItemCopiesItsWeaponDataAndGeneratesItsOwnKey() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.addCatalogueItem(sheet, catalogueWeaponItem()));

        assertThat(result.items()).hasSize(1);
        Dnd5eItem item = result.items().get(0);
        assertThat(item.key()).isNotBlank().isNotEqualTo("warhammer");
        assertThat(item.catalogueSlug()).isEqualTo("warhammer");
        assertThat(item.itemKind()).isEqualTo(Dnd5eItemKind.WEAPON);
        assertThat(item.weaponCategory()).isEqualTo(Dnd5eWeaponCategory.MARTIAL);
        assertThat(item.rangeCategory()).isEqualTo(Dnd5eRangeCategory.MELEE);
        assertThat(item.damageDiceCount()).isEqualTo(1);
        assertThat(item.damageDiceSides()).isEqualTo(8);
        assertThat(item.versatileDamageDiceSides()).isEqualTo(10);
        assertThat(item.equipped()).isFalse();
        assertThat(item.attuned()).isFalse();
    }

    @Test
    void addingACatalogueItemCopiesItsGrantedSpellsOntoTheItem() {
        Dnd5eSheet result = read(mutator.addCatalogueItem(sheetJson(30, 0), wandOfFireballs()));

        Dnd5eItem item = result.items().get(0);
        assertThat(item.charges()).isEqualTo(7);
        assertThat(item.chargesUsed()).isZero();
        assertThat(item.grantedSpells()).hasSize(1);
        Dnd5eGrantedSpell grantedSpell = item.grantedSpells().get(0);
        assertThat(grantedSpell.spellSlug()).isEqualTo("fireball");
        assertThat(grantedSpell.chargeCost()).isEqualTo(1);
        assertThat(grantedSpell.fixedSaveDc()).isEqualTo(15);
    }

    @Test
    void addingACatalogueItemAlsoAddsItsGrantedSpellAsAKnownSpell() {
        String sheet = mutator.addCatalogueItem(sheetJson(30, 0), wandOfFireballs());
        String itemKey = read(sheet).items().get(0).key();

        Dnd5eSpell grantedSpell = read(sheet).spells().get(0);

        assertThat(grantedSpell.key()).isEqualTo(itemKey + "-fireball");
        assertThat(grantedSpell.name()).isEqualTo("Fireball");
        assertThat(grantedSpell.className()).isEqualTo("Wand of Fireballs");
        assertThat(grantedSpell.grantedByItemKey()).isEqualTo(itemKey);
        assertThat(grantedSpell.chargeCost()).isEqualTo(1);
        assertThat(grantedSpell.fixedSaveDc()).isEqualTo(15);
        assertThat(grantedSpell.alwaysPrepared()).isTrue();
        assertThat(grantedSpell.prepared()).isFalse();
    }

    @Test
    void castingAnItemGrantedSpellSpendsItsChargeCost() {
        String sheet = mutator.addCatalogueItem(sheetJson(30, 0), wandOfFireballs());
        String spellKey = read(sheet).spells().get(0).key();

        Dnd5eSheet result = read(mutator.castItemGrantedSpell(sheet, spellKey));

        assertThat(result.items().get(0).chargesUsed()).isEqualTo(1);
    }

    @Test
    void castingAnItemGrantedSpellNeverExceedsItsItemsMaxCharges() {
        String sheet = mutator.addCatalogueItem(sheetJson(30, 0), wandOfFireballs());
        String spellKey = read(sheet).spells().get(0).key();
        for (int i = 0; i < 10; i++) {
            sheet = mutator.castItemGrantedSpell(sheet, spellKey);
        }

        assertThat(read(sheet).items().get(0).chargesUsed()).isEqualTo(7);
    }

    @Test
    void aLongRestFullyRestoresEveryItemsSpentCharges() {
        String sheet = mutator.addCatalogueItem(sheetJson(30, 0), wandOfFireballs());
        String spellKey = read(sheet).spells().get(0).key();
        sheet = mutator.castItemGrantedSpell(sheet, spellKey);

        Dnd5eSheet result = read(mutator.applyLongRest(sheet));

        assertThat(result.items().get(0).chargesUsed()).isZero();
    }

    @Test
    void castingAnUnknownSpellKeyIsANoOp() {
        String sheet = sheetJson(30, 0);

        String result = mutator.castItemGrantedSpell(sheet, "not-a-real-spell");

        assertThat(result).isEqualTo(sheet);
    }

    private Item wandOfFireballs() {
        return new Item(
                "wand-of-fireballs", "Wand of Fireballs", 1, "", "", false, false, true, "wand-of-fireballs",
                "GEAR", "Wand", "rare", "by a spellcaster", 1.0, null, null, null, null, null, null,
                null, null, List.of(), false, null, null, null, null, false, null, null, null, null,
                7, "Dawn", "1d6 + 1",
                List.of(new ItemGrantedSpell(
                        "fireball", "Fireball", 1, 15, 3, "evocation", "1 Action", "150 feet", false, false, false,
                        8, 6, "fire", "V, S, M", "Damage", "A bright streak flashes.", "dexterity", "V, S, M",
                        "a tiny ball of bat guano and sulfur", "Instantaneous")),
                0, "EQUIPMENT", null);
    }

    @Test
    void anUnknownItemKindIsRejected() {
        String sheet = sheetJson(30, 0);
        Item invalid = itemOfKind("NOT_A_KIND");

        assertThatThrownBy(() -> mutator.addCatalogueItem(sheet, invalid))
                .isInstanceOf(InvalidItemFieldException.class);
    }

    @Test
    void togglingEquippedOnACatalogueItemPreservesItsWeaponData() {
        String sheet = mutator.addCatalogueItem(sheetJson(30, 0), catalogueWeaponItem());
        String itemKey = read(sheet).items().get(0).key();

        Dnd5eSheet result = read(mutator.toggleItemEquipped(sheet, itemKey));

        Dnd5eItem item = result.items().get(0);
        assertThat(item.equipped()).isTrue();
        assertThat(item.damageDiceCount()).isEqualTo(1);
        assertThat(item.damageDiceSides()).isEqualTo(8);
        assertThat(item.weaponCategory()).isEqualTo(Dnd5eWeaponCategory.MARTIAL);
        assertThat(item.source()).isEqualTo("Player's Handbook, p. 149");
    }

    private Item itemOfKind(String itemKind) {
        return new Item(
                "bad-item", "Bad Item", 1, "", "", false, false, false, "bad-item",
                itemKind, null, null, null, null, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                null, null, false, null, null, null, null,
                null, null, null, List.of(), 0, "EQUIPMENT", null);
    }

    @Test
    void aCatalogueItemAddedEquippedStartsEquipped() {
        Dnd5eSheet result = read(mutator.addCatalogueItem(sheetJson(30, 0), catalogueWeaponItem(true)));

        assertThat(result.items().get(0).equipped()).isTrue();
        assertThat(result.items().get(0).attuned()).isFalse();
    }

    private Item catalogueWeaponItem() {
        return catalogueWeaponItem(false);
    }

    private Item catalogueWeaponItem(boolean equipped) {
        return new Item(
                "warhammer", "Warhammer", 1, "15 gp", "", equipped, false, false, "warhammer",
                "WEAPON", "Melee Weapon", "none", null, 2.0, 15.0, "MARTIAL", "MELEE",
                1, 8, "bludgeoning", 1, 10, List.of("Versatile"), false, null, null,
                null, null, false, null, null, null, null,
                null, null, null, List.of(), 0, "EQUIPMENT", "Player's Handbook, p. 149");
    }

    @Test
    void togglingItemEquippedFlipsItWithoutAffectingOtherItems() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Shield", 1, "10 gp", "", false, null);
        String itemKey = read(sheet).items().get(0).key();

        Dnd5eSheet result = read(mutator.toggleItemEquipped(sheet, itemKey));

        assertThat(result.items().get(0).equipped()).isTrue();
        assertThat(read(mutator.toggleItemEquipped(mutator.toggleItemEquipped(sheet, itemKey), itemKey))
                .items().get(0).equipped()).isFalse();
    }

    @Test
    void movingAnItemChangesOnlyItsStorageLocation() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Rope", 1, "1 gp", "50 feet", false, null);
        String itemKey = read(sheet).items().get(0).key();
        assertThat(read(sheet).items().get(0).storageLocation()).isEqualTo(Dnd5eStorageLocation.EQUIPMENT);

        Dnd5eSheet result = read(mutator.moveItem(sheet, itemKey, "BACKPACK"));

        Dnd5eItem item = result.items().get(0);
        assertThat(item.storageLocation()).isEqualTo(Dnd5eStorageLocation.BACKPACK);
        assertThat(item.name()).isEqualTo("Rope");
    }

    @Test
    void movingAnUnknownItemKeyIsANoOp() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Rope", 1, "1 gp", "50 feet", false, null);

        String result = mutator.moveItem(sheet, "unknown-key", "BACKPACK");

        assertThat(read(result).items().get(0).storageLocation()).isEqualTo(Dnd5eStorageLocation.EQUIPMENT);
    }

    @Test
    void movingAnItemToAnInvalidStorageLocationIsRejected() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Rope", 1, "1 gp", "50 feet", false, null);
        String itemKey = read(sheet).items().get(0).key();

        assertThatThrownBy(() -> mutator.moveItem(sheet, itemKey, "NOT_A_LOCATION"))
                .isInstanceOf(InvalidItemFieldException.class);
    }

    @Test
    void attuningUpToTheLimitSucceeds() {
        String sheet = sheetJson(30, 0);
        sheet = mutator.addItem(sheet, "Cloak of Protection", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Ring of Protection", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Wand of Magic Missiles", 1, "", "", true, null);
        List<Dnd5eItem> items = read(sheet).items();

        for (Dnd5eItem item : items) {
            sheet = mutator.toggleItemAttuned(sheet, item.key());
        }

        assertThat(read(sheet).items()).allMatch(Dnd5eItem::attuned);
    }

    @Test
    void attuningBeyondTheLimitIsRejected() {
        String sheet = sheetJson(30, 0);
        sheet = mutator.addItem(sheet, "Item One", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Item Two", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Item Three", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Item Four", 1, "", "", true, null);
        List<Dnd5eItem> items = read(sheet).items();
        for (int i = 0; i < 3; i++) {
            sheet = mutator.toggleItemAttuned(sheet, items.get(i).key());
        }
        String fourthItemKey = items.get(3).key();
        String finalSheet = sheet;

        assertThatThrownBy(() -> mutator.toggleItemAttuned(finalSheet, fourthItemKey))
                .isInstanceOf(AttunementLimitExceededException.class);
    }

    @Test
    void unattuningIsAlwaysAllowedEvenAtTheLimit() {
        String sheet = sheetJson(30, 0);
        sheet = mutator.addItem(sheet, "Item One", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Item Two", 1, "", "", true, null);
        sheet = mutator.addItem(sheet, "Item Three", 1, "", "", true, null);
        List<Dnd5eItem> items = read(sheet).items();
        for (Dnd5eItem item : items) {
            sheet = mutator.toggleItemAttuned(sheet, item.key());
        }

        Dnd5eSheet result = read(mutator.toggleItemAttuned(sheet, items.get(0).key()));

        assertThat(result.items().get(0).attuned()).isFalse();
    }

    @Test
    void attuningAnItemThatDoesNotRequireItIsRejected() {
        String sheet = mutator.addItem(sheetJson(30, 0), "Rope, hempen", 1, "1 gp", "50 feet", false, null);
        String itemKey = read(sheet).items().get(0).key();
        String finalSheet = sheet;

        assertThatThrownBy(() -> mutator.toggleItemAttuned(finalSheet, itemKey))
                .isInstanceOf(AttunementNotAllowedException.class);
    }

    @Test
    void addingACustomActionAppendsItWithAGeneratedKey() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.addCustomAction(sheet, customAction("SPELL", "Custom Action 2", "ACTION")));

        assertThat(result.customActions()).hasSize(1);
        Dnd5eCustomAction action = result.customActions().get(0);
        assertThat(action.key()).isNotBlank();
        assertThat(action.template()).isEqualTo(Dnd5eCustomActionTemplate.SPELL);
        assertThat(action.name()).isEqualTo("Custom Action 2");
        assertThat(action.activationType()).isEqualTo(Dnd5eActivationType.ACTION);
        assertThat(action.rangeCategory()).isEqualTo(Dnd5eRangeCategory.RANGED);
    }

    @Test
    void removingACustomActionDropsIt() {
        String sheet = mutator.addCustomAction(sheetJson(30, 0), customAction("GENERAL", "Custom Action 1", "ACTION"));
        String actionKey = read(sheet).customActions().get(0).key();

        Dnd5eSheet result = read(mutator.removeCustomAction(sheet, actionKey));

        assertThat(result.customActions()).isEmpty();
    }

    @Test
    void updatingACustomActionReplacesItsFieldsAndKeepsItsKey() {
        String sheet = mutator.addCustomAction(sheetJson(30, 0), customAction("GENERAL", "Custom Action 1", "ACTION"));
        String actionKey = read(sheet).customActions().get(0).key();

        Dnd5eSheet result = read(mutator.updateCustomAction(sheet, actionKey, customAction("GENERAL", "Tail Swipe", "BONUS_ACTION")));

        assertThat(result.customActions()).singleElement().satisfies(action -> {
            assertThat(action.key()).isEqualTo(actionKey);
            assertThat(action.name()).isEqualTo("Tail Swipe");
            assertThat(action.activationType()).isEqualTo(Dnd5eActivationType.BONUS_ACTION);
        });
    }

    @Test
    void aNewCustomActionMayHaveNoActivationTypeYet() {
        Dnd5eSheet result = read(mutator.addCustomAction(sheetJson(30, 0), customAction("WEAPON", "Custom Action 1", null)));

        assertThat(result.customActions()).singleElement().extracting(Dnd5eCustomAction::activationType).isNull();
    }

    @Test
    void updatingAnUnknownCustomActionKeyIsANoOp() {
        String sheet = mutator.addCustomAction(sheetJson(30, 0), customAction("GENERAL", "Custom Action 1", "ACTION"));

        Dnd5eSheet result = read(mutator.updateCustomAction(sheet, "not-a-real-key", customAction("GENERAL", "Other", "ACTION")));

        assertThat(result.customActions()).singleElement().extracting(Dnd5eCustomAction::name).isEqualTo("Custom Action 1");
    }

    @Test
    void removingAnUnknownCustomActionKeyIsANoOp() {
        String sheet = mutator.addCustomAction(sheetJson(30, 0), customAction("GENERAL", "Custom Action 1", "ACTION"));

        Dnd5eSheet result = read(mutator.removeCustomAction(sheet, "not-a-real-key"));

        assertThat(result.customActions()).hasSize(1);
    }

    @Test
    void addingACustomActionWithAnUnknownTemplateIsRejected() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.addCustomAction(sheet, customAction("NOT_A_TEMPLATE", "Broken", "ACTION")))
                .isInstanceOf(InvalidCustomActionFieldException.class);
    }

    @Test
    void addingACustomActionLeavesBlankOptionalFieldsNull() {
        String sheet = sheetJson(30, 0);
        CustomAction input = new CustomAction(
                null, "GENERAL", "Custom Action 1", "", "", null, null, null, null, null, null, null, null, null,
                null, null, null, "ACTION", null, false, false, false, null, null, false, false);

        Dnd5eSheet result = read(mutator.addCustomAction(sheet, input));

        Dnd5eCustomAction action = result.customActions().get(0);
        assertThat(action.rangeCategory()).isNull();
        assertThat(action.spellRangeType()).isNull();
        assertThat(action.aoeType()).isNull();
        assertThat(action.weaponAttackType()).isNull();
    }

    private CustomAction customAction(String template, String name, String activationType) {
        return new CustomAction(
                null, template, name, "", "", "RANGED", 180, "charisma", 1, 8, null, "force", "charisma", null,
                "RANGED", null, null, activationType, null, false, false, true, null, null, false, false);
    }

    @Test
    void addingCoinsIncreasesOnlyThatDenomination() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.addCoins(sheet, "gold", 10));

        assertThat(result.goldPieces()).isEqualTo(10);
        assertThat(result.silverPieces()).isZero();
    }

    @Test
    void removingCoinsNeverDropsBelowZero() {
        String sheet = mutator.addCoins(sheetJson(30, 0), "gold", 5);

        Dnd5eSheet result = read(mutator.removeCoins(sheet, "gold", 20));

        assertThat(result.goldPieces()).isZero();
    }

    @Test
    void anUnknownCoinDenominationIsRejected() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.addCoins(sheet, "not-a-denomination", 5))
                .isInstanceOf(InvalidCoinDenominationException.class);
    }

    @Test
    void extraDamageConsumesTemporaryHitPointsFirst() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 3);

        Dnd5eSheet result = read(mutator.applyExtraDamage(sheet, "warhorse", 5));

        assertThat(result.extras().get(0).temporaryHitPoints()).isZero();
        assertThat(result.extras().get(0).currentHitPoints()).isEqualTo(13);
    }

    @Test
    void extraHealingNeverExceedsItsOwnMax() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 0);

        Dnd5eSheet result = read(mutator.applyExtraHealing(sheet, "warhorse", 10));

        assertThat(result.extras().get(0).currentHitPoints()).isEqualTo(19);
    }

    @Test
    void extraTemporaryHitPointsDoNotStackAndTakeTheHigherValue() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 5);

        Dnd5eSheet lower = read(mutator.setExtraTemporaryHitPoints(sheet, "warhorse", 3));
        Dnd5eSheet higher = read(mutator.setExtraTemporaryHitPoints(sheet, "warhorse", 8));

        assertThat(lower.extras().get(0).temporaryHitPoints()).isEqualTo(5);
        assertThat(higher.extras().get(0).temporaryHitPoints()).isEqualTo(8);
    }

    @Test
    void anUnknownExtraKeyIsANoOp() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 0);

        Dnd5eSheet result = read(mutator.applyExtraDamage(sheet, "not-an-extra", 5));

        assertThat(result.extras().get(0).currentHitPoints()).isEqualTo(15);
    }

    @Test
    void removingAnExtraDropsIt() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 0);

        Dnd5eSheet result = read(mutator.removeExtra(sheet, "warhorse"));

        assertThat(result.extras()).isEmpty();
    }

    @Test
    void removingAnUnknownExtraKeyIsANoOp() {
        String sheet = sheetJsonWithExtra("warhorse", 15, 0);

        Dnd5eSheet result = read(mutator.removeExtra(sheet, "not-an-extra"));

        assertThat(result.extras()).hasSize(1);
    }

    @Test
    void usingAFeatureActionIncrementsUsedCountCappedAtMaxUses() {
        String sheet = sheetJsonWithFeatureAction("secondWind", 1, 0);

        Dnd5eSheet onceUsed = read(mutator.useFeatureAction(sheet, "secondWind"));
        Dnd5eSheet twiceUsed = read(mutator.useFeatureAction(objectMapper.writeValueAsString(onceUsed), "secondWind"));

        assertThat(onceUsed.featureActions().get(0).usedCount()).isEqualTo(1);
        assertThat(twiceUsed.featureActions().get(0).usedCount()).isEqualTo(1);
    }

    @Test
    void restoringAFeatureActionDecrementsUsedCountFlooredAtZero() {
        String sheet = sheetJsonWithFeatureAction("secondWind", 1, 1);

        Dnd5eSheet restored = read(mutator.restoreFeatureAction(sheet, "secondWind"));
        Dnd5eSheet restoredAgain = read(mutator.restoreFeatureAction(objectMapper.writeValueAsString(restored), "secondWind"));

        assertThat(restored.featureActions().get(0).usedCount()).isZero();
        assertThat(restoredAgain.featureActions().get(0).usedCount()).isZero();
    }

    @Test
    void anUnknownFeatureActionKeyIsANoOp() {
        String sheet = sheetJsonWithFeatureAction("secondWind", 1, 0);

        Dnd5eSheet result = read(mutator.useFeatureAction(sheet, "not-a-feature"));

        assertThat(result.featureActions().get(0).usedCount()).isZero();
    }

    @Test
    void usingAFeatureTraitUseIsIndependentFromFeatureActions() {
        String sheet = sheetJsonWithFeatureTrait("secondWindTrait", 1, 0);

        Dnd5eSheet result = read(mutator.useFeatureTraitUse(sheet, "secondWindTrait"));

        assertThat(result.featureTraits().get(0).usedCount()).isEqualTo(1);
        assertThat(result.featureActions()).isEmpty();
    }

    @Test
    void restoringAFeatureTraitUseDecrementsUsedCountFlooredAtZero() {
        String sheet = sheetJsonWithFeatureTrait("secondWindTrait", 1, 1);

        Dnd5eSheet result = read(mutator.restoreFeatureTraitUse(sheet, "secondWindTrait"));

        assertThat(result.featureTraits().get(0).usedCount()).isZero();
    }

    @Test
    void consumingASpellSlotIncrementsUsedSlotsCappedAtMaxSlots() {
        String sheet = sheetJsonWithSpellSlot(1, 3, 2);

        Dnd5eSheet onceConsumed = read(mutator.consumeSpellSlot(sheet, 1, false));
        Dnd5eSheet twiceConsumed = read(mutator.consumeSpellSlot(objectMapper.writeValueAsString(onceConsumed), 1, false));

        assertThat(onceConsumed.spellSlots().get(0).usedSlots()).isEqualTo(3);
        assertThat(twiceConsumed.spellSlots().get(0).usedSlots()).isEqualTo(3);
    }

    @Test
    void restoringASpellSlotDecrementsUsedSlotsFlooredAtZero() {
        String sheet = sheetJsonWithSpellSlot(1, 3, 0);

        Dnd5eSheet restored = read(mutator.restoreSpellSlot(sheet, 1, false));
        Dnd5eSheet restoredAgain = read(mutator.restoreSpellSlot(objectMapper.writeValueAsString(restored), 1, false));

        assertThat(restored.spellSlots().get(0).usedSlots()).isZero();
        assertThat(restoredAgain.spellSlots().get(0).usedSlots()).isZero();
    }

    @Test
    void thePactMagicPoolIsSpentApartAndRegainedOnAShortRest() {
        Dnd5eSheet base = read(sheetJson(30, 0)).withSpellSlots(List.of(
                new Dnd5eSpellSlotLevel(2, 3, 0), new Dnd5eSpellSlotLevel(2, 2, 0, true, "Warlock")));

        Dnd5eSheet spent = read(mutator.castSpell(
                mutator.consumeSpellSlot(objectMapper.writeValueAsString(base), 2, false), "unknown", 2, true, null, true));
        Dnd5eSheet rested = read(mutator.applyShortRest(objectMapper.writeValueAsString(spent)));

        assertThat(spent.spellSlots()).extracting(Dnd5eSpellSlotLevel::usedSlots).containsExactly(1, 1);
        assertThat(rested.spellSlots()).extracting(Dnd5eSpellSlotLevel::usedSlots).containsExactly(1, 0);
    }

    @Test
    void anUnknownSpellSlotLevelIsANoOp() {
        String sheet = sheetJsonWithSpellSlot(1, 3, 1);

        Dnd5eSheet result = read(mutator.consumeSpellSlot(sheet, 2, false));

        assertThat(result.spellSlots().get(0).usedSlots()).isEqualTo(1);
    }

    @Test
    void shortRestRestoresOnlyShortOrLongRestTriggeredResources() {
        String sheet = sheetJsonForRest();

        Dnd5eSheet result = read(mutator.applyShortRest(sheet));

        assertThat(result.featureActions().get(0).usedCount()).isZero(); // shortOrLong
        assertThat(result.featureActions().get(1).usedCount()).isEqualTo(1); // longOnly, untouched
        assertThat(result.featureActions().get(2).usedCount()).isEqualTo(1); // no trigger, untouched
        assertThat(result.featureTraits().get(0).usedCount()).isZero(); // shortOrLong
        assertThat(result.featureTraits().get(1).usedCount()).isEqualTo(1); // longOnly, untouched
        assertThat(result.featureTraits().get(2).usedCount()).isEqualTo(1); // no trigger, untouched
        assertThat(result.spellSlots().get(0).usedSlots()).isEqualTo(2);
        assertThat(result.hitDiceUsed()).isEqualTo(4);
        assertThat(result.currentHitPoints()).isEqualTo(20);
    }

    @Test
    void longRestRestoresEveryTriggeredResourceHitPointsSpellSlotsAndHalfHitDice() {
        String sheet = sheetJsonForRest();

        Dnd5eSheet result = read(mutator.applyLongRest(sheet));

        assertThat(result.featureActions().get(0).usedCount()).isZero();
        assertThat(result.featureActions().get(1).usedCount()).isZero();
        assertThat(result.featureActions().get(2).usedCount()).isEqualTo(1); // no trigger, untouched
        assertThat(result.featureTraits().get(0).usedCount()).isZero();
        assertThat(result.featureTraits().get(1).usedCount()).isZero();
        assertThat(result.featureTraits().get(2).usedCount()).isEqualTo(1); // no trigger, untouched
        assertThat(result.spellSlots().get(0).usedSlots()).isZero();
        assertThat(result.currentHitPoints()).isEqualTo(44);
        // Level 5: half of 5 hit dice, rounded down, minimum one -> 2 restored: 4 used - 2 = 2.
        assertThat(result.hitDiceUsed()).isEqualTo(2);
    }

    @Test
    void longRestRestoresAtLeastOneHitDieEvenWhenHalfRoundsDownToZero() {
        String sheet = sheetJsonForRestAtLevel(1, 1);

        Dnd5eSheet result = read(mutator.applyLongRest(sheet));

        assertThat(result.hitDiceUsed()).isZero();
    }

    @Test
    void learningANewSpellAppendsItUnpreparedAndNotAlwaysPrepared() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 4, null, List.of());

        Dnd5eSheet result = read(mutator.learnSpell(sheet, testSpell("shield", 1)));

        assertThat(result.spells()).hasSize(1);
        assertThat(result.spells().get(0).key()).isEqualTo("shield");
        assertThat(result.spells().get(0).prepared()).isFalse();
        assertThat(result.spells().get(0).alwaysPrepared()).isFalse();
    }

    @Test
    void learningAnAlreadyKnownSpellIsANoOp() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 4, null, List.of());
        String onceLearned = mutator.learnSpell(sheet, testSpell("shield", 1));

        Dnd5eSheet result = read(mutator.learnSpell(onceLearned, testSpell("shield", 1)));

        assertThat(result.spells()).hasSize(1);
    }

    @Test
    void learningACantripBeyondTheCantripCapIsRejected() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 1, 4, null,
                List.of(dnd5eSpell("fireBolt", 0)));

        assertThatThrownBy(() -> mutator.learnSpell(sheet, testSpell("rayOfFrost", 0)))
                .isInstanceOf(SpellLimitExceededException.class);
    }

    @Test
    void learningALeveledSpellBeyondAKnownClassesCapIsRejected() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 1, null,
                List.of(dnd5eSpell("mageArmor", 1)));

        assertThatThrownBy(() -> mutator.learnSpell(sheet, testSpell("shield", 1)))
                .isInstanceOf(SpellLimitExceededException.class);
    }

    @Test
    void learningALeveledSpellForAPreparedClassHasNoCap() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.PREPARED, 4, null, 3, List.of());

        Dnd5eSheet result = read(mutator.learnSpell(sheet, testSpell("cureWounds", 1)));

        assertThat(result.spells()).hasSize(1);
    }

    @Test
    void removingAKnownSpellDropsIt() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 4, null,
                List.of(dnd5eSpell("mageArmor", 1)));

        Dnd5eSheet result = read(mutator.removeSpell(sheet, "mageArmor"));

        assertThat(result.spells()).isEmpty();
    }

    @Test
    void removingAnUnknownSpellKeyIsANoOp() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 4, null,
                List.of(dnd5eSpell("mageArmor", 1)));

        Dnd5eSheet result = read(mutator.removeSpell(sheet, "not-a-spell"));

        assertThat(result.spells()).hasSize(1);
    }

    @Test
    void learningANewFeatAppendsItAsAFeatureTrait() {
        Dnd5eSheet result = read(mutator.learnFeat(sheetJson(44, 0), testFeat("alert")));

        assertThat(result.featureTraits()).hasSize(1);
        assertThat(result.featureTraits().get(0).key()).isEqualTo("alert");
        assertThat(result.featureTraits().get(0).category()).isEqualTo(Dnd5eFeatureTraitCategory.FEAT);
        assertThat(result.featureTraits().get(0).source()).isEqualTo("Feat");
        assertThat(result.featureTraits().get(0).maxUses()).isNull();
    }

    @Test
    void learningAnAlreadyKnownFeatIsANoOp() {
        String onceLearned = mutator.learnFeat(sheetJson(44, 0), testFeat("alert"));

        Dnd5eSheet result = read(mutator.learnFeat(onceLearned, testFeat("alert")));

        assertThat(result.featureTraits()).hasSize(1);
    }

    @Test
    void removingAKnownFeatDropsIt() {
        String withFeat = mutator.learnFeat(sheetJson(44, 0), testFeat("alert"));

        Dnd5eSheet result = read(mutator.removeFeat(withFeat, "alert"));

        assertThat(result.featureTraits()).isEmpty();
    }

    @Test
    void removingAnUnknownFeatKeyIsANoOp() {
        String withFeat = mutator.learnFeat(sheetJson(44, 0), testFeat("alert"));

        Dnd5eSheet result = read(mutator.removeFeat(withFeat, "not-a-feat"));

        assertThat(result.featureTraits()).hasSize(1);
    }

    private FeatureTrait testFeat(String key) {
        return new FeatureTrait(key, "Alert", "FEAT", "Feat", "Always on the lookout for danger.", "Full text.", null, 0, null);
    }

    @Test
    void preparingAKnownLeveledSpellUnderAPreparedClassMarksItPrepared() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.PREPARED, 4, null, 2,
                List.of(dnd5eSpell("cureWounds", 1)));

        Dnd5eSheet result = read(mutator.prepareSpell(sheet, "cureWounds"));

        assertThat(result.spells().get(0).prepared()).isTrue();
    }

    @Test
    void unpreparingASpellClearsTheFlag() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.PREPARED, 4, null, 2,
                List.of(dnd5eSpell("cureWounds", 1)));
        String prepared = mutator.prepareSpell(sheet, "cureWounds");

        Dnd5eSheet result = read(mutator.unprepareSpell(prepared, "cureWounds"));

        assertThat(result.spells().get(0).prepared()).isFalse();
    }

    @Test
    void preparingBeyondThePreparedCapIsRejected() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.PREPARED, 4, null, 1,
                List.of(dnd5eSpell("cureWounds", 1), dnd5eSpell("bless", 1)));
        String onePrepared = mutator.prepareSpell(sheet, "cureWounds");

        assertThatThrownBy(() -> mutator.prepareSpell(onePrepared, "bless"))
                .isInstanceOf(SpellPreparationLimitExceededException.class);
    }

    @Test
    void preparingACantripIsANoOp() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.PREPARED, 4, null, 2,
                List.of(dnd5eSpell("sacredFlame", 0)));

        Dnd5eSheet result = read(mutator.prepareSpell(sheet, "sacredFlame"));

        assertThat(result.spells().get(0).prepared()).isFalse();
    }

    @Test
    void preparingASpellUnderAKnownClassIsANoOp() {
        String sheet = sheetJsonWithSpellcasting(Dnd5eSpellCastingType.KNOWN, 2, 4, null,
                List.of(dnd5eSpell("mageArmor", 1)));

        Dnd5eSheet result = read(mutator.prepareSpell(sheet, "mageArmor"));

        assertThat(result.spells().get(0).prepared()).isFalse();
    }

    private Spell testSpell(String key, int level) {
        return new Spell(
                key, "Test Spell", "Fighter", level, "abjuration", "1 Action", "Self", false, false, false, null,
                null, null, "V, S", "Buff", false, false, "", null, "V, S", null, "Instantaneous", null, null, null);
    }

    private Dnd5eSpell dnd5eSpell(String key, int level) {
        return new Dnd5eSpell(
                key, "Test Spell", "Fighter", level, "abjuration", "1 Action", "Self", false, false, false, null,
                null, null, "V, S", "Buff", false, false, "", null, "V, S", null, "Instantaneous", null, null, null);
    }

    private String sheetJsonWithSpellcasting(
            Dnd5eSpellCastingType castingType, int cantripsKnownMax, Integer spellsKnownMax, Integer spellsPreparedMax,
            List<Dnd5eSpell> spells) {
        Dnd5eSpellcastingClass spellcastingClass = new Dnd5eSpellcastingClass(
                "Fighter", "intelligence", castingType, cantripsKnownMax, spellsKnownMax, spellsPreparedMax);
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                30, 0, 0, false, List.of(), List.of(), List.of(spellcastingClass), spells,
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    @Test
    void updatingABackgroundFieldChangesOnlyThatField() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.updateBackgroundField(sheet, "backstory", "Raised by wolves."));

        assertThat(result.background().backstory()).isEqualTo("Raised by wolves.");
        assertThat(result.background().alignment()).isEmpty();
    }

    @Test
    void updatingACharacteristicsFieldWorksTheSameWay() {
        String sheet = sheetJson(30, 0);

        Dnd5eSheet result = read(mutator.updateBackgroundField(sheet, "gender", "She/her"));

        assertThat(result.background().gender()).isEqualTo("She/her");
    }

    @Test
    void updatingAnUnknownBackgroundFieldIsRejected() {
        String sheet = sheetJson(30, 0);

        assertThatThrownBy(() -> mutator.updateBackgroundField(sheet, "not-a-field", "x"))
                .isInstanceOf(InvalidBackgroundFieldException.class);
    }

    private String sheetJsonForRest() {
        return sheetJsonForRestAtLevel(5, 4);
    }

    private String sheetJsonForRestAtLevel(int level, int hitDiceUsed) {
        Dnd5eFeatureAction shortOrLongAction = new Dnd5eFeatureAction(
                "secondWind", "Second Wind", Dnd5eActionType.BONUS_ACTION, "Regain hit points.", 1, 1,
                Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        Dnd5eFeatureAction longOnlyAction = new Dnd5eFeatureAction(
                "channelDivinity", "Channel Divinity", Dnd5eActionType.ACTION, "Channel divine energy.", 1, 1,
                Dnd5eRechargeTrigger.LONG_REST);
        Dnd5eFeatureTrait shortOrLongTrait = new Dnd5eFeatureTrait(
                "secondWindTrait", "Second Wind", Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Fighter",
                "Regain hit points.", "Regain hit points.", 1, 1, Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        Dnd5eFeatureTrait longOnlyTrait = new Dnd5eFeatureTrait(
                "channelDivinityTrait", "Channel Divinity", Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Cleric",
                "Channel divine energy.", "Channel divine energy.", 1, 1, Dnd5eRechargeTrigger.LONG_REST);
        Dnd5eFeatureAction noTriggerAction = new Dnd5eFeatureAction(
                "extraAttack", "Extra Attack", Dnd5eActionType.ACTION, "Attack twice.", 1, 1, null);
        Dnd5eFeatureTrait noTriggerTrait = new Dnd5eFeatureTrait(
                "extraAttackTrait", "Extra Attack", Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Fighter",
                "Attack twice.", "Attack twice.", 1, 1, null);
        Dnd5eSpellSlotLevel spellSlot = new Dnd5eSpellSlotLevel(1, 3, 2);
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, level, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                20, 0, 0, false, List.of(), List.of(shortOrLongAction, longOnlyAction, noTriggerAction), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(shortOrLongTrait, longOnlyTrait, noTriggerTrait), emptyBackground(),
                List.of(), hitDiceUsed, List.of(spellSlot), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    @Test
    void spendingHitDiceHealsAndIncrementsHitDiceUsed() {
        String sheet = sheetJsonWithHitDiceUsed(30, 1);

        Dnd5eSheet result = read(mutator.spendHitDice(sheet, 2, 12));

        assertThat(result.currentHitPoints()).isEqualTo(42);
        assertThat(result.hitDiceUsed()).isEqualTo(3);
    }

    @Test
    void spendingHitDiceNeverHealsPastMax() {
        String sheet = sheetJsonWithHitDiceUsed(40, 0);

        Dnd5eSheet result = read(mutator.spendHitDice(sheet, 1, 20));

        assertThat(result.currentHitPoints()).isEqualTo(44);
    }

    /** Constitution 14 (+2 modifier), level 5, d10 hit die: max hit points is 44. */
    private String sheetJson(int currentHitPoints, int temporaryHitPoints) {
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                currentHitPoints, temporaryHitPoints, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private String sheetJsonWithExtra(String extraKey, int currentHitPoints, int temporaryHitPoints) {
        Dnd5eExtra extra = new Dnd5eExtra(
                extraKey, "Warhorse", Dnd5eExtraCategory.MOUNT, 11, 19, currentHitPoints, temporaryHitPoints, 60,
                emptyExtraStatBlock());
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                30, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(extra), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private String sheetJsonWithFeatureAction(String key, int maxUses, int usedCount) {
        Dnd5eFeatureAction feature = new Dnd5eFeatureAction(
                key, "Second Wind", Dnd5eActionType.BONUS_ACTION, "Regain hit points.", maxUses, usedCount,
                Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                30, 0, 0, false, List.of(), List.of(feature), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private String sheetJsonWithFeatureTrait(String key, int maxUses, int usedCount) {
        Dnd5eFeatureTrait feature = new Dnd5eFeatureTrait(
                key, "Second Wind", Dnd5eFeatureTraitCategory.CLASS_FEATURE, "Fighter", "Regain hit points.",
                "Regain hit points.", maxUses, usedCount, Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                30, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(feature), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private String sheetJsonWithSpellSlot(int level, int maxSlots, int usedSlots) {
        Dnd5eSpellSlotLevel slot = new Dnd5eSpellSlotLevel(level, maxSlots, usedSlots);
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                30, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(slot), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private String sheetJsonWithHitDiceUsed(int currentHitPoints, int hitDiceUsed) {
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                currentHitPoints, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), hitDiceUsed, List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private Dnd5eSheet read(String sheetJson) {
        return objectMapper.readValue(sheetJson, Dnd5eSheet.class);
    }

    private Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    private Dnd5eExtraStatBlock emptyExtraStatBlock() {
        return new Dnd5eExtraStatBlock(
                "Medium", "Beast", "Unaligned", "1d8", null, List.of(), List.of(), "", "", "", List.of(), List.of());
    }
}
