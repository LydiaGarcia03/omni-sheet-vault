package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.AttunementLimitExceededException;
import dev.omnisheetvault.api.ruleset.AttunementNotAllowedException;
import dev.omnisheetvault.api.ruleset.CustomAction;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.InvalidBackgroundFieldException;
import dev.omnisheetvault.api.ruleset.InvalidCoinDenominationException;
import dev.omnisheetvault.api.ruleset.InvalidConditionException;
import dev.omnisheetvault.api.ruleset.InvalidCustomActionFieldException;
import dev.omnisheetvault.api.ruleset.InvalidItemFieldException;
import dev.omnisheetvault.api.ruleset.InvalidSheetThemeException;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.ItemGrantedSpell;
import dev.omnisheetvault.api.ruleset.SheetMutator;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellLimitExceededException;
import dev.omnisheetvault.api.ruleset.SpellPreparationLimitExceededException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Phase 6's session-state rules (damage, healing, temporary hit points, heroic
 * inspiration and conditions — see features/character-sheet.md's mutation table)
 * plus phase 8's Collection editor rules for proficiencies/training and inventory,
 * plus phase 9's limited-use spend/restore for feature actions and feature traits,
 * spell slot consume/restore, the short/long rest mechanic that restores several of
 * those resources at once, and "Manage spells" (learn/remove/prepare/unprepare a
 * spell — see {@link Dnd5eSpellCastingType}).
 */
@Component
public class Dnd5eSheetMutator implements SheetMutator {

    private static final int ATTUNEMENT_LIMIT = 3;
    private static final String EXHAUSTION = "exhaustion";
    private static final String ALLY_EFFECT_SUFFIX = "-ally";

    private final ObjectMapper objectMapper;
    private final Dnd5eCustomizationEditor customizationEditor;

    public Dnd5eSheetMutator(ObjectMapper objectMapper) {
        this.objectMapper = Dnd5eSheetJsonMapper.lenient(objectMapper);
        this.customizationEditor = new Dnd5eCustomizationEditor(objectMapper);
    }

    @Override
    public String systemId() {
        return Dnd5eGameSystem.SYSTEM_ID;
    }

    /** Damage consumes temporary hit points first, per features/character-sheet.md. */
    @Override
    public String applyDamage(String sheetJson, int amount) {
        return applyDamage(sheetJson, amount, false);
    }

    /**
     * PHB 2014, "Dropping to 0 Hit Points": dropping to 0 starts fresh death saves; damage
     * that leaves at least the hit point maximum over kills outright; damage taken at 0 is
     * one failure, two on a critical hit, and a stable character starts dying again.
     */
    @Override
    public String applyDamage(String sheetJson, int amount, boolean critical) {
        Dnd5eSheet sheet = read(sheetJson);
        int newTemporary = Math.max(0, sheet.temporaryHitPoints() - amount);
        int remainingDamage = Math.max(0, amount - sheet.temporaryHitPoints());
        int newCurrent = Math.max(0, sheet.currentHitPoints() - remainingDamage);
        Dnd5eSheet damaged = sheet.withSessionState(newCurrent, newTemporary, sheet.heroicInspiration(), sheet.activeConditions());
        if (remainingDamage == 0 || newCurrent > 0) {
            return write(damaged);
        }
        int overflow = remainingDamage - sheet.currentHitPoints();
        if (overflow >= Dnd5eFormulas.maxHitPoints(sheet)) {
            return write(damaged.withDeathSaves(new Dnd5eDeathSaves(0, Dnd5eDeathSaves.LIMIT)));
        }
        if (sheet.currentHitPoints() > 0) {
            return write(damaged.withDeathSaves(Dnd5eDeathSaves.NONE));
        }
        Dnd5eDeathSaves saves = sheet.deathSavesOrDefault();
        Dnd5eDeathSaves from = saves.stable() ? Dnd5eDeathSaves.NONE : saves;
        return write(damaged.withDeathSaves(from.withFailures(critical ? 2 : 1)));
    }

    /** Counts set by hand; ignored above 0 hit points. */
    @Override
    public String setDeathSaves(String sheetJson, int successes, int failures) {
        Dnd5eSheet sheet = read(sheetJson);
        if (sheet.currentHitPoints() > 0) {
            return sheetJson;
        }
        return write(sheet.withDeathSaves(new Dnd5eDeathSaves(successes, failures)));
    }

    @Override
    public String clearDeathSaves(String sheetJson) {
        return write(read(sheetJson).withDeathSaves(Dnd5eDeathSaves.NONE));
    }

    @Override
    public String setSheetTheme(String sheetJson, String theme) {
        if (!Dnd5eAppearance.THEMES.contains(theme)) {
            throw new InvalidSheetThemeException(theme);
        }
        return write(read(sheetJson).withAppearance(new Dnd5eAppearance(theme)));
    }

    @Override
    public String customize(String sheetJson, String group, String target, String valueJson) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(keepingDamageTaken(sheet, sheet.withCustomizations(customizationEditor.apply(sheet, group, target, valueJson))));
    }

    @Override
    public String removeCustomization(String sheetJson, String group, String target) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(keepingDamageTaken(sheet, sheet.withCustomizations(customizationEditor.remove(sheet, group, target))));
    }

    /** As on D&D Beyond, current hit points move with a customized maximum, so the damage taken stays the same; 0 stays 0. */
    private static Dnd5eSheet keepingDamageTaken(Dnd5eSheet before, Dnd5eSheet after) {
        int newMaximum = Dnd5eFormulas.maxHitPoints(after);
        int change = newMaximum - Dnd5eFormulas.maxHitPoints(before);
        if (change == 0 || before.currentHitPoints() == 0) {
            return after;
        }
        int current = Math.clamp((long) before.currentHitPoints() + change, 0, newMaximum);
        return after.withSessionState(current, after.temporaryHitPoints(), after.heroicInspiration(), after.activeConditions());
    }

    @Override
    public String setExperiencePoints(String sheetJson, int points) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(sheet.withExperiencePoints(Math.max(Dnd5eExperience.threshold(sheet.level()), points)));
    }

    /**
     * PHB 2014, "Death Saving Throws": 10 or higher succeeds, lower fails; a natural 1 is two
     * failures; a natural 20 brings the character back with 1 hit point.
     */
    @Override
    public String applyDeathSaveRoll(String sheetJson, int naturalRoll) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eDeathSaves saves = sheet.deathSavesOrDefault();
        if (naturalRoll == 20) {
            return write(sheet.withSessionState(1, sheet.temporaryHitPoints(), sheet.heroicInspiration(), sheet.activeConditions()));
        }
        if (naturalRoll == 1) {
            return write(sheet.withDeathSaves(saves.withFailures(2)));
        }
        return write(sheet.withDeathSaves(naturalRoll >= 10 ? saves.withSuccesses(1) : saves.withFailures(1)));
    }

    /** Healing never restores temporary hit points, and never exceeds the max. */
    @Override
    public String applyHealing(String sheetJson, int amount) {
        Dnd5eSheet sheet = read(sheetJson);
        int newCurrent = Math.min(Dnd5eFormulas.maxHitPoints(sheet), sheet.currentHitPoints() + amount);
        return write(sheet.withSessionState(
                newCurrent, sheet.temporaryHitPoints(), sheet.heroicInspiration(), sheet.activeConditions()));
    }

    /** Temporary hit points never stack — PHB: take the higher of the old and new value. */
    @Override
    public String setTemporaryHitPoints(String sheetJson, int amount) {
        Dnd5eSheet sheet = read(sheetJson);
        int newTemporary = Math.max(sheet.temporaryHitPoints(), amount);
        return write(sheet.withSessionState(
                sheet.currentHitPoints(), newTemporary, sheet.heroicInspiration(), sheet.activeConditions()));
    }

    @Override
    public String toggleInspiration(String sheetJson) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(sheet.withSessionState(
                sheet.currentHitPoints(), sheet.temporaryHitPoints(), !sheet.heroicInspiration(), sheet.activeConditions()));
    }

    @Override
    public String toggleCondition(String sheetJson, String condition) {
        if (!Dnd5eConditions.ALL.contains(condition)) {
            throw new InvalidConditionException(condition);
        }
        Dnd5eSheet sheet = read(sheetJson);
        Set<String> activeConditions = new HashSet<>(sheet.activeConditions());
        if (!activeConditions.remove(condition)) {
            activeConditions.add(condition);
        }
        return write(sheet.withSessionState(
                sheet.currentHitPoints(), sheet.temporaryHitPoints(), sheet.heroicInspiration(), activeConditions));
    }

    @Override
    public String setExhaustionLevel(String sheetJson, int level) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(sheet.withExhaustionLevel(Math.clamp(level, 0, 6)));
    }

    /** Turning a condition on copies its modifiers onto the sheet (copy-once); turning it off removes them. */
    @Override
    public String toggleCondition(String sheetJson, String condition, String conditionDataJson) {
        Dnd5eSheet toggled = read(toggleCondition(sheetJson, condition));
        Map<String, List<Dnd5eModifier>> conditionModifiers = new LinkedHashMap<>(toggled.conditionModifiersOrEmpty());
        conditionModifiers.remove(condition);
        if (toggled.activeConditions().contains(condition) && conditionDataJson != null) {
            List<Dnd5eModifier> modifiers = modifiersFrom(objectMapper.readTree(conditionDataJson).path("modifiers"), conditionLabel(condition));
            if (!modifiers.isEmpty()) {
                conditionModifiers.put(condition, modifiers);
            }
        }
        return write(toggled.withConditions(toggled.activeConditions(), toggled.exhaustionLevel(), conditionModifiers));
    }

    /** Exhaustion's effects are cumulative: every level up to the new one contributes its modifiers. */
    @Override
    public String setExhaustionLevel(String sheetJson, int level, String exhaustionDataJson) {
        Dnd5eSheet leveled = read(setExhaustionLevel(sheetJson, level));
        Map<String, List<Dnd5eModifier>> conditionModifiers = new LinkedHashMap<>(leveled.conditionModifiersOrEmpty());
        conditionModifiers.remove(EXHAUSTION);
        if (exhaustionDataJson != null && leveled.exhaustionLevel() > 0) {
            JsonNode levels = objectMapper.readTree(exhaustionDataJson).path("levels");
            List<Dnd5eModifier> modifiers = new ArrayList<>();
            for (int current = 1; current <= leveled.exhaustionLevel(); current++) {
                modifiers.addAll(modifiersFrom(levels.path(String.valueOf(current)), "Exhaustion " + current));
            }
            if (!modifiers.isEmpty()) {
                conditionModifiers.put(EXHAUSTION, modifiers);
            }
        }
        return write(leveled.withConditions(leveled.activeConditions(), leveled.exhaustionLevel(), conditionModifiers));
    }

    private static List<Dnd5eModifier> modifiersFrom(JsonNode modifiers, String source) {
        List<Dnd5eModifier> result = new ArrayList<>();
        for (JsonNode modifier : modifiers) {
            result.add(new Dnd5eModifier(Dnd5eModifierType.valueOf(modifier.path("type").asString()),
                    Dnd5eModifierTarget.valueOf(modifier.path("target").asString()), modifier.path("value").asInt(), null, null, source,
                    modifier.hasNonNull("restriction") ? modifier.get("restriction").asString() : null));
        }
        return result;
    }

    private static String conditionLabel(String condition) {
        return Character.toUpperCase(condition.charAt(0)) + condition.substring(1);
    }

    @Override
    public String addItem(
            String sheetJson, String name, int quantity, String cost, String notes, boolean requiresAttunement,
            String storageLocation) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eStorageLocation parsedLocation = parseNullableItemEnum(Dnd5eStorageLocation.class, "storageLocation", storageLocation);
        List<Dnd5eItem> items = new ArrayList<>(sheet.items());
        items.add(new Dnd5eItem(
                UUID.randomUUID().toString(), name, quantity, cost, notes, false, false, requiresAttunement, parsedLocation));
        return write(sheet.withItems(items));
    }

    @Override
    public String addCatalogueItem(String sheetJson, Item item) {
        return addCatalogueItem(sheetJson, item, null);
    }

    /** The item's {@code data.mechanics} is copied onto it, each modifier labelled with the item's name. */
    @Override
    public String addCatalogueItem(String sheetJson, Item item, String itemDataJson) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eItemMechanics mechanics = itemMechanics(itemDataJson, item.name());
        String itemKey = UUID.randomUUID().toString();
        List<Dnd5eGrantedSpell> grantedSpells = item.grantedSpells().stream()
                .map(this::toDnd5eGrantedSpell)
                .toList();

        List<Dnd5eItem> items = new ArrayList<>(sheet.items());
        items.add(new Dnd5eItem(
                itemKey, item.name(), item.quantity(), item.cost(), item.notes(),
                item.equipped(), false, item.requiresAttunement(), item.catalogueSlug(),
                parseItemEnum(Dnd5eItemKind.class, "itemKind", item.itemKind()), item.typeLabel(), item.rarity(),
                item.attunementRequirement(), item.weightLb(), item.costGp(),
                parseNullableItemEnum(Dnd5eWeaponCategory.class, "weaponCategory", item.weaponCategory()),
                parseNullableItemEnum(Dnd5eRangeCategory.class, "rangeCategory", item.rangeCategory()),
                item.damageDiceCount(), item.damageDiceSides(), item.damageType(),
                item.versatileDamageDiceCount(), item.versatileDamageDiceSides(), item.properties(), item.finesse(),
                item.normalRange(), item.longRange(),
                parseNullableItemEnum(Dnd5eArmorCategory.class, "armorCategory", item.armorCategory()),
                item.baseArmorClass(), item.stealthDisadvantage(), item.strengthRequirement(),
                item.weaponAttackBonus(), item.weaponDamageBonus(), item.armorClassBonus(),
                item.charges(), item.rechargeTrigger(), item.rechargeFormula(), grantedSpells, 0,
                Dnd5eStorageLocation.EQUIPMENT, item.source(), mechanics));

        List<Dnd5eSpell> spells = new ArrayList<>(sheet.spells());
        for (Dnd5eGrantedSpell grantedSpell : grantedSpells) {
            spells.add(grantedSpellEntry(itemKey, item.name(), grantedSpell));
        }

        return write(sheet.withItems(items).withSpells(spells));
    }

    private Dnd5eItemMechanics itemMechanics(String itemDataJson, String itemName) {
        JsonNode mechanics = itemDataJson == null ? null : objectMapper.readTree(itemDataJson).get("mechanics");
        if (mechanics == null || mechanics.isNull()) {
            return Dnd5eItemMechanics.NONE;
        }
        return new Dnd5eItemMechanics(
                modifiersFrom(mechanics.path("modifiers"), itemName),
                texts(mechanics.path("damageResistances")),
                texts(mechanics.path("damageImmunities")),
                texts(mechanics.path("damageVulnerabilities")),
                texts(mechanics.path("conditionImmunities")));
    }

    private static List<String> texts(JsonNode array) {
        List<String> texts = new ArrayList<>();
        array.forEach(text -> texts.add(text.asString()));
        return texts;
    }

    private Dnd5eGrantedSpell toDnd5eGrantedSpell(ItemGrantedSpell granted) {
        return new Dnd5eGrantedSpell(
                granted.spellSlug(), granted.spellName(), granted.chargeCost(), granted.fixedSaveDc(),
                granted.level(), granted.school(), granted.castingTime(), granted.range(), granted.concentration(),
                granted.ritual(), granted.attackRoll(), granted.damageDiceCount(), granted.damageDiceSides(),
                granted.damageType(), granted.notes(), granted.effectSummary(), granted.description(),
                granted.saveAbility(), granted.components(), granted.materialComponent(), granted.duration());
    }

    /**
     * {@code className} holds the granting item's own name (D&D Beyond's real
     * source line for one of these), not a real spellcasting class —
     * {@code prepared}/{@code alwaysPrepared} are meaningless for a charge-cast
     * spell, set to {@code false}/{@code true} matching the "always usable, no
     * prep step" treatment a {@code KNOWN}-type class's own spell already gets.
     */
    private Dnd5eSpell grantedSpellEntry(String itemKey, String itemName, Dnd5eGrantedSpell grantedSpell) {
        return new Dnd5eSpell(
                itemKey + "-" + grantedSpell.spellSlug(), grantedSpell.spellName(), itemName, grantedSpell.level(),
                grantedSpell.school(), grantedSpell.castingTime(), grantedSpell.range(), grantedSpell.concentration(),
                grantedSpell.ritual(), grantedSpell.attackRoll(), grantedSpell.damageDiceCount(),
                grantedSpell.damageDiceSides(), grantedSpell.damageType(), grantedSpell.notes(),
                grantedSpell.effectSummary(), false, true, grantedSpell.description(), grantedSpell.saveAbility(),
                grantedSpell.components(), grantedSpell.materialComponent(), grantedSpell.duration(), null, null,
                null, itemKey, grantedSpell.chargeCost(), grantedSpell.fixedSaveDc());
    }

    @Override
    public String removeItem(String sheetJson, String itemKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eItem> items = sheet.items().stream().filter(item -> !item.key().equals(itemKey)).toList();
        return write(sheet.withItems(items));
    }

    @Override
    public String toggleItemEquipped(String sheetJson, String itemKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.key().equals(itemKey) ? withEquipped(item, !item.equipped()) : item)
                .toList();
        return write(sheet.withItems(items));
    }

    /**
     * Attuning an item that was never flagged {@code requiresAttunement}, or attuning
     * past the limit, is rejected; unattuning is always allowed. An unknown
     * {@code itemKey} is a no-op, same treatment as an unknown feature key.
     */
    @Override
    public String toggleItemAttuned(String sheetJson, String itemKey) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eItem target = sheet.items().stream().filter(item -> item.key().equals(itemKey)).findFirst().orElse(null);
        if (target == null) {
            return sheetJson;
        }
        boolean isAttuning = !target.attuned();
        if (isAttuning && !target.requiresAttunement()) {
            throw new AttunementNotAllowedException(itemKey);
        }
        long attunedCount = sheet.items().stream().filter(Dnd5eItem::attuned).count();
        if (isAttuning && attunedCount >= ATTUNEMENT_LIMIT) {
            throw new AttunementLimitExceededException(ATTUNEMENT_LIMIT);
        }
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.key().equals(itemKey) ? withAttuned(item, !item.attuned()) : item)
                .toList();
        return write(sheet.withItems(items));
    }

    @Override
    public String setItemQuantity(String sheetJson, String itemKey, int quantity) {
        Dnd5eSheet sheet = read(sheetJson);
        int clampedQuantity = Math.max(1, quantity);
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.key().equals(itemKey) ? withQuantity(item, clampedQuantity) : item)
                .toList();
        return write(sheet.withItems(items));
    }

    /** An unknown {@code itemKey} or an invalid {@code storageLocation} name is a no-op / a validation error, same treatment as other item field mutations. */
    @Override
    public String moveItem(String sheetJson, String itemKey, String storageLocation) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eStorageLocation parsedLocation = parseItemEnum(Dnd5eStorageLocation.class, "storageLocation", storageLocation);
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.key().equals(itemKey) ? withStorageLocation(item, parsedLocation) : item)
                .toList();
        return write(sheet.withItems(items));
    }

    @Override
    public String setTrackEncumbrance(String sheetJson, boolean trackEncumbrance) {
        return write(read(sheetJson).withTrackEncumbrance(trackEncumbrance));
    }

    @Override
    public String addCoins(String sheetJson, String denomination, int amount) {
        return write(mutateCoins(sheetJson, denomination, current -> current + amount));
    }

    /** Never drops a denomination's total below zero. */
    @Override
    public String removeCoins(String sheetJson, String denomination, int amount) {
        return write(mutateCoins(sheetJson, denomination, current -> Math.max(0, current - amount)));
    }

    /** Preserves every catalogue-sourced field untouched (see {@code Dnd5eItem}'s own doc comment) — only the one field named by each caller changes. */
    private Dnd5eItem withEquipped(Dnd5eItem item, boolean equipped) {
        return copyItem(item, equipped, item.attuned(), item.quantity(), item.chargesUsed(), item.storageLocation());
    }

    private Dnd5eItem withAttuned(Dnd5eItem item, boolean attuned) {
        return copyItem(item, item.equipped(), attuned, item.quantity(), item.chargesUsed(), item.storageLocation());
    }

    private Dnd5eItem withQuantity(Dnd5eItem item, int quantity) {
        return copyItem(item, item.equipped(), item.attuned(), quantity, item.chargesUsed(), item.storageLocation());
    }

    private Dnd5eItem withChargesUsed(Dnd5eItem item, int chargesUsed) {
        return copyItem(item, item.equipped(), item.attuned(), item.quantity(), chargesUsed, item.storageLocation());
    }

    private Dnd5eItem withStorageLocation(Dnd5eItem item, Dnd5eStorageLocation storageLocation) {
        return copyItem(item, item.equipped(), item.attuned(), item.quantity(), item.chargesUsed(), storageLocation);
    }

    private Dnd5eItem copyItem(
            Dnd5eItem item, boolean equipped, boolean attuned, int quantity, int chargesUsed,
            Dnd5eStorageLocation storageLocation) {
        return new Dnd5eItem(
                item.key(), item.name(), quantity, item.cost(), item.notes(), equipped, attuned,
                item.requiresAttunement(), item.catalogueSlug(), item.itemKind(), item.typeLabel(), item.rarity(),
                item.attunementRequirement(), item.weightLb(), item.costGp(), item.weaponCategory(),
                item.rangeCategory(), item.damageDiceCount(), item.damageDiceSides(), item.damageType(),
                item.versatileDamageDiceCount(), item.versatileDamageDiceSides(), item.properties(), item.finesse(),
                item.normalRange(), item.longRange(), item.armorCategory(), item.baseArmorClass(),
                item.stealthDisadvantage(), item.strengthRequirement(), item.weaponAttackBonus(),
                item.weaponDamageBonus(), item.armorClassBonus(), item.charges(), item.rechargeTrigger(),
                item.rechargeFormula(), item.grantedSpells(), chargesUsed, storageLocation, item.source(), item.mechanics());
    }

    private Dnd5eSheet mutateCoins(String sheetJson, String denomination, IntUnaryOperator edit) {
        Dnd5eCoinDenomination parsedDenomination = parseDenomination(denomination);
        Dnd5eSheet sheet = read(sheetJson);
        int copper = sheet.copperPieces();
        int silver = sheet.silverPieces();
        int electrum = sheet.electrumPieces();
        int gold = sheet.goldPieces();
        int platinum = sheet.platinumPieces();
        switch (parsedDenomination) {
            case COPPER -> copper = edit.applyAsInt(copper);
            case SILVER -> silver = edit.applyAsInt(silver);
            case ELECTRUM -> electrum = edit.applyAsInt(electrum);
            case GOLD -> gold = edit.applyAsInt(gold);
            case PLATINUM -> platinum = edit.applyAsInt(platinum);
        }
        return sheet.withCoins(copper, silver, electrum, gold, platinum);
    }

    private Dnd5eCoinDenomination parseDenomination(String denomination) {
        try {
            return Dnd5eCoinDenomination.valueOf(denomination.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidCoinDenominationException(denomination);
        }
    }

    /** Damage consumes temporary hit points first, same rule as the character's own. */
    @Override
    public String applyExtraDamage(String sheetJson, String extraKey, int amount) {
        return write(mutateExtra(sheetJson, extraKey, extra -> {
            int newTemporary = Math.max(0, extra.temporaryHitPoints() - amount);
            int remainingDamage = Math.max(0, amount - extra.temporaryHitPoints());
            int newCurrent = Math.max(0, extra.currentHitPoints() - remainingDamage);
            return withHitPoints(extra, newCurrent, newTemporary);
        }));
    }

    /** Healing never restores temporary hit points, and never exceeds the extra's own max. */
    @Override
    public String applyExtraHealing(String sheetJson, String extraKey, int amount) {
        return write(mutateExtra(sheetJson, extraKey, extra -> {
            int newCurrent = Math.min(extra.maxHitPoints(), extra.currentHitPoints() + amount);
            return withHitPoints(extra, newCurrent, extra.temporaryHitPoints());
        }));
    }

    /** Temporary hit points never stack — take the higher of the old and new value. */
    @Override
    public String setExtraTemporaryHitPoints(String sheetJson, String extraKey, int amount) {
        return write(mutateExtra(sheetJson, extraKey,
                extra -> withHitPoints(extra, extra.currentHitPoints(), Math.max(extra.temporaryHitPoints(), amount))));
    }

    @Override
    public String removeExtra(String sheetJson, String extraKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eExtra> extras = sheet.extras().stream().filter(extra -> !extra.key().equals(extraKey)).toList();
        return write(sheet.withExtras(extras));
    }

    /** An unknown {@code extraKey} leaves the list unchanged, same treatment as an unknown item key. */
    private Dnd5eSheet mutateExtra(String sheetJson, String extraKey, UnaryOperator<Dnd5eExtra> edit) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eExtra> extras = sheet.extras().stream()
                .map(extra -> extra.key().equals(extraKey) ? edit.apply(extra) : extra)
                .toList();
        return sheet.withExtras(extras);
    }

    private Dnd5eExtra withHitPoints(Dnd5eExtra extra, int currentHitPoints, int temporaryHitPoints) {
        return new Dnd5eExtra(
                extra.key(), extra.name(), extra.category(), extra.armorClass(), extra.maxHitPoints(),
                currentHitPoints, temporaryHitPoints, extra.speed(), extra.statBlock());
    }

    /** An unknown {@code featureKey} is a no-op, same treatment as an unknown item key; a trait-linked action spends its trait. */
    @Override
    public String useFeatureAction(String sheetJson, String featureKey) {
        String traitKey = linkedTraitKey(sheetJson, featureKey);
        if (traitKey != null) {
            return useFeatureTraitUse(sheetJson, traitKey);
        }
        return write(mutateFeatureAction(sheetJson, featureKey,
                feature -> withUsedCount(feature, Math.min(safeMaxUses(feature), feature.usedCount() + 1))));
    }

    @Override
    public String restoreFeatureAction(String sheetJson, String featureKey) {
        String traitKey = linkedTraitKey(sheetJson, featureKey);
        if (traitKey != null) {
            return restoreFeatureTraitUse(sheetJson, traitKey);
        }
        return write(mutateFeatureAction(sheetJson, featureKey,
                feature -> withUsedCount(feature, Math.max(0, feature.usedCount() - 1))));
    }

    private String linkedTraitKey(String sheetJson, String featureKey) {
        return read(sheetJson).featureActions().stream()
                .filter(feature -> feature.key().equals(featureKey) && feature.traitKey() != null)
                .map(Dnd5eFeatureAction::traitKey)
                .findFirst()
                .orElse(null);
    }

    private Dnd5eSheet mutateFeatureAction(String sheetJson, String featureKey, UnaryOperator<Dnd5eFeatureAction> edit) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eFeatureAction> featureActions = sheet.featureActions().stream()
                .map(feature -> feature.key().equals(featureKey) ? edit.apply(feature) : feature)
                .toList();
        return sheet.withFeatureActions(featureActions);
    }

    private Dnd5eFeatureAction withUsedCount(Dnd5eFeatureAction feature, int usedCount) {
        return new Dnd5eFeatureAction(
                feature.key(), feature.name(), feature.actionType(), feature.description(), feature.maxUses(),
                usedCount, feature.rechargeTrigger(), feature.traitKey());
    }

    private int safeMaxUses(Dnd5eFeatureAction feature) {
        return feature.maxUses() != null ? feature.maxUses() : feature.usedCount();
    }

    /** Same rules as {@link #useFeatureAction}, scoped to the independent feature traits list. */
    @Override
    public String useFeatureTraitUse(String sheetJson, String featureKey) {
        return write(mutateFeatureTrait(sheetJson, featureKey,
                feature -> withUsedCount(feature, Math.min(safeMaxUses(feature), feature.usedCount() + 1))));
    }

    @Override
    public String restoreFeatureTraitUse(String sheetJson, String featureKey) {
        return write(mutateFeatureTrait(sheetJson, featureKey,
                feature -> withUsedCount(feature, Math.max(0, feature.usedCount() - 1))));
    }

    private Dnd5eSheet mutateFeatureTrait(String sheetJson, String featureKey, UnaryOperator<Dnd5eFeatureTrait> edit) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eFeatureTrait> featureTraits = sheet.featureTraits().stream()
                .map(feature -> feature.key().equals(featureKey) ? edit.apply(feature) : feature)
                .toList();
        return sheet.withFeatureTraits(featureTraits);
    }

    private Dnd5eFeatureTrait withUsedCount(Dnd5eFeatureTrait feature, int usedCount) {
        return new Dnd5eFeatureTrait(
                feature.key(), feature.name(), feature.category(), feature.source(), feature.summary(),
                feature.description(), feature.maxUses(), usedCount, feature.rechargeTrigger(), feature.modifiers(),
                feature.choices());
    }

    private int safeMaxUses(Dnd5eFeatureTrait feature) {
        return feature.maxUses() != null ? feature.maxUses() : feature.usedCount();
    }

    /**
     * Trusts the caller's already-validated {@code count} and already-rolled
     * {@code healAmount} — see {@link dev.omnisheetvault.api.ruleset.SheetMutator#spendHitDice}.
     */
    @Override
    public String spendHitDice(String sheetJson, int count, int healAmount) {
        Dnd5eSheet sheet = read(sheetJson);
        return spendHitDice(sheetJson, Dnd5eHitDicePools.of(sheet).defaultDieSize(), count, healAmount);
    }

    @Override
    public String spendHitDice(String sheetJson, int dieSize, int count, int healAmount) {
        Dnd5eSheet sheet = read(sheetJson);
        int newCurrent = Math.min(Dnd5eFormulas.maxHitPoints(sheet), sheet.currentHitPoints() + healAmount);
        Dnd5eSheet spent = Dnd5eHitDicePools.spend(sheet, dieSize, count);
        return write(spent.withHitDiceSpend(newCurrent, spent.hitDiceUsed()));
    }

    /** An unknown {@code level} leaves the list unchanged, same treatment as an unknown item key. */
    @Override
    public String consumeSpellSlot(String sheetJson, int level, boolean pact) {
        return write(mutateSpellSlot(sheetJson, level, pact,
                slot -> slot.withUsedSlots(Math.min(slot.maxSlots(), slot.usedSlots() + 1))));
    }

    @Override
    public String restoreSpellSlot(String sheetJson, int level, boolean pact) {
        return write(mutateSpellSlot(sheetJson, level, pact,
                slot -> slot.withUsedSlots(Math.max(0, slot.usedSlots() - 1))));
    }

    private Dnd5eSheet mutateSpellSlot(String sheetJson, int level, boolean pact, UnaryOperator<Dnd5eSpellSlotLevel> edit) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eSpellSlotLevel> spellSlots = sheet.spellSlots().stream()
                .map(slot -> slot.matches(level, pact) ? edit.apply(slot) : slot)
                .toList();
        return sheet.withSpellSlots(spellSlots);
    }

    /** PHB: the Warlock regains its Pact Magic slots on a short rest. */
    @Override
    public String applyShortRest(String sheetJson) {
        Dnd5eSheet sheet = read(sheetJson);
        Set<Dnd5eRechargeTrigger> triggers = Set.of(Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        List<Dnd5eSpellSlotLevel> spellSlots = sheet.spellSlots().stream()
                .map(slot -> slot.pact() ? slot.withUsedSlots(0) : slot)
                .toList();
        Dnd5eSheet updated = sheet
                .withFeatureActions(restoreFeatureActionsByTrigger(sheet.featureActions(), triggers))
                .withFeatureTraits(restoreFeatureTraitsByTrigger(sheet.featureTraits(), triggers))
                .withSpellSlots(spellSlots)
                .withSpells(restoreSpellUses(sheet.spells(), triggers))
                .withActiveEffects(effectsSurvivingRest(sheet, triggers));
        return write(updated);
    }

    /**
     * Also restores every item's spent charges to full — systems/dnd-5e/features/
     * inventory-equipment-mechanics.md's slice 7: a deliberate full-restore
     * simplification of the real "regains 1d6+1 daily at dawn" rule, matching this
     * app's existing rest-trigger model rather than a new random-partial-recharge
     * mechanic (see {@code Dnd5eItem}'s own doc comment).
     */
    @Override
    public String applyLongRest(String sheetJson) {
        return applyLongRest(sheetJson, null);
    }

    /** {@code hitDiceRecovered} null recovers half the character's dice, largest first. */
    @Override
    public String applyLongRest(String sheetJson, Map<Integer, Integer> hitDiceRecovered) {
        Dnd5eSheet stored = read(sheetJson);
        Dnd5eSheet sheet = Dnd5eHitDicePools.recover(stored,
                hitDiceRecovered == null ? Dnd5eHitDicePools.defaultRecovery(stored) : hitDiceRecovered);
        Set<Dnd5eRechargeTrigger> triggers = Set.of(Dnd5eRechargeTrigger.SHORT_OR_LONG_REST, Dnd5eRechargeTrigger.LONG_REST);
        List<Dnd5eSpellSlotLevel> spellSlots = sheet.spellSlots().stream()
                .map(slot -> slot.withUsedSlots(0))
                .toList();
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.chargesUsed() == 0 ? item : withChargesUsed(item, 0))
                .toList();

        Dnd5eSheet updated = sheet
                .withFeatureActions(restoreFeatureActionsByTrigger(sheet.featureActions(), triggers))
                .withFeatureTraits(restoreFeatureTraitsByTrigger(sheet.featureTraits(), triggers))
                .withSpellSlots(spellSlots)
                .withSpells(restoreSpellUses(sheet.spells(), triggers))
                .withHitDiceSpend(Dnd5eFormulas.maxHitPoints(sheet), sheet.hitDiceUsed())
                .withItems(items)
                .withActiveEffects(effectsSurvivingRest(sheet, triggers));
        return write(updated);
    }

    /** Effects stay through a rest unless their own {@code endsOnRests} names it. */
    private static List<Dnd5eActiveEffect> effectsSurvivingRest(Dnd5eSheet sheet, Set<Dnd5eRechargeTrigger> triggers) {
        return sheet.activeEffectsOrEmpty().stream()
                .filter(effect -> effect.endsOnRests().stream().noneMatch(triggers::contains))
                .toList();
    }

    /**
     * The slot (a Pact Magic one when {@code pact}) is spent even for an unknown {@code spellKey}, like
     * {@link #consumeSpellSlot}. Casting again on the same target replaces that effect, so the spell can be on the
     * character and on an ally at once; any concentration spell ends the current concentration effect (PHB: one at a
     * time).
     */
    @Override
    public String castSpell(String sheetJson, String spellKey, int slotLevel, boolean pact, String spellDataJson, boolean onSelf) {
        Dnd5eSheet sheet = slotLevel > 0 ? read(consumeSpellSlot(sheetJson, slotLevel, pact)) : read(sheetJson);
        Dnd5eSpell spell = sheet.spells().stream().filter(known -> known.key().equals(spellKey)).findFirst().orElse(null);
        if (spell == null) {
            return write(sheet);
        }
        Dnd5eSpellUsage usage = spell.usage();
        if (usage != null && slotLevel == 0) {
            if (usage.mode() == Dnd5eSpellUsage.Mode.LIMITED && usage.remainingUses() == 0) {
                return write(sheet);
            }
            sheet = spendUse(sheet, spell);
            onSelf = onSelf || usage.selfOnly();
        }
        int effectLevel = slotLevel > 0 ? slotLevel : usage != null && usage.castLevel() != null ? usage.castLevel() : 0;
        String effectKey = onSelf ? spellKey : spellKey + ALLY_EFFECT_SUFFIX;
        List<Dnd5eActiveEffect> effects = sheet.activeEffectsOrEmpty().stream()
                .filter(effect -> !effect.key().equals(effectKey))
                .filter(effect -> !(spell.concentration() && effect.concentration()))
                .collect(Collectors.toCollection(ArrayList::new));
        JsonNode effect = spellDataJson == null ? null : objectMapper.readTree(spellDataJson).get("effect");
        if (effect != null && !effect.isNull()) {
            effects.add(activeEffect(effectKey, spell, effectLevel, effect, onSelf));
        }
        return write(sheet.withActiveEffects(effects));
    }

    /** Spends one use of a limited spell, and of every spell sharing its pool; an at-will spell is unchanged. */
    private static Dnd5eSheet spendUse(Dnd5eSheet sheet, Dnd5eSpell cast) {
        return sheet.withSpells(sheet.spells().stream()
                .map(spell -> spell.usage() != null && spell.usage().sharesUsesWith(spell.key(), cast.key(), cast.usage())
                        ? spell.withUsage(spell.usage().withUsedUses(spell.usage().usedUses() + 1))
                        : spell)
                .toList());
    }

    /** Restores the uses of limited spells whose recharge is one of {@code triggers}. */
    private static List<Dnd5eSpell> restoreSpellUses(List<Dnd5eSpell> spells, Set<Dnd5eRechargeTrigger> triggers) {
        return spells.stream()
                .map(spell -> spell.usage() != null && spell.usage().usedUses() > 0 && triggers.contains(spell.usage().recharge())
                        ? spell.withUsage(spell.usage().withUsedUses(0))
                        : spell)
                .toList();
    }

    private static Dnd5eActiveEffect activeEffect(String effectKey, Dnd5eSpell spell, int slotLevel, JsonNode effect, boolean onSelf) {
        Set<Dnd5eRechargeTrigger> endsOnRests = new HashSet<>();
        effect.path("endsOnRests").forEach(rest -> endsOnRests.add(Dnd5eRechargeTrigger.valueOf(rest.asString())));
        String durationText = effect.hasNonNull("durationText") ? effect.get("durationText").asString() : null;
        return new Dnd5eActiveEffect(effectKey, spell.name(), spell.key(), slotLevel > 0 ? slotLevel : null,
                spell.concentration(), endsOnRests, durationText, modifiersFrom(effect.path("modifiers"), spell.name()), onSelf);
    }

    @Override
    public String endActiveEffect(String sheetJson, String effectKey) {
        Dnd5eSheet sheet = read(sheetJson);
        return write(sheet.withActiveEffects(sheet.activeEffectsOrEmpty().stream()
                .filter(effect -> !effect.key().equals(effectKey))
                .toList()));
    }

    /**
     * An unknown {@code spellKey}, or one not granted by an item, is a no-op —
     * same treatment as an unknown item key. Clamps at the item's own maximum
     * rather than throwing, same "plain resource spend" philosophy
     * {@link #consumeSpellSlot} already uses, not an error surfaced to the
     * player: the frontend's own Cast button is already disabled once the
     * remaining charges are too few, so this is a defensive floor, not the
     * primary guard.
     */
    @Override
    public String castItemGrantedSpell(String sheetJson, String spellKey) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eSpell spell = sheet.spells().stream().filter(s -> s.key().equals(spellKey)).findFirst().orElse(null);
        if (spell == null || spell.grantedByItemKey() == null || spell.chargeCost() == null) {
            return sheetJson;
        }
        int chargeCost = spell.chargeCost();
        List<Dnd5eItem> items = sheet.items().stream()
                .map(item -> item.key().equals(spell.grantedByItemKey())
                        ? withChargesUsed(item, Math.min(safeCharges(item), item.chargesUsed() + chargeCost))
                        : item)
                .toList();
        return write(sheet.withItems(items));
    }

    private int safeCharges(Dnd5eItem item) {
        return item.charges() != null ? item.charges() : 0;
    }

    private List<Dnd5eFeatureAction> restoreFeatureActionsByTrigger(
            List<Dnd5eFeatureAction> featureActions, Set<Dnd5eRechargeTrigger> triggers) {
        return featureActions.stream()
                .map(feature -> matchesTrigger(feature.rechargeTrigger(), triggers) ? withUsedCount(feature, 0) : feature)
                .toList();
    }

    private List<Dnd5eFeatureTrait> restoreFeatureTraitsByTrigger(
            List<Dnd5eFeatureTrait> featureTraits, Set<Dnd5eRechargeTrigger> triggers) {
        return featureTraits.stream()
                .map(feature -> matchesTrigger(feature.rechargeTrigger(), triggers) ? withUsedCount(feature, 0) : feature)
                .toList();
    }

    /** A null trigger means "no automatic rest recharge", never matched by either rest. */
    private boolean matchesTrigger(Dnd5eRechargeTrigger trigger, Set<Dnd5eRechargeTrigger> triggers) {
        return trigger != null && triggers.contains(trigger);
    }

    /** A no-op if {@code spell.key()} is already known; otherwise checked against the owning class's own cap. */
    @Override
    public String learnSpell(String sheetJson, Spell spell) {
        Dnd5eSheet sheet = read(sheetJson);
        if (sheet.spells().stream().anyMatch(known -> known.key().equals(spell.key()))) {
            return sheetJson;
        }

        Dnd5eSpellcastingClass spellcastingClass = findSpellcastingClass(sheet, spell.className());
        if (spellcastingClass != null) {
            if (spell.level() == 0) {
                long cantripsKnown = countByClassAndLevel(sheet, spell.className(), 0);
                if (cantripsKnown >= spellcastingClass.cantripsKnownMax()) {
                    throw new SpellLimitExceededException(spellcastingClass.cantripsKnownMax());
                }
            } else if (spellcastingClass.castingType() == Dnd5eSpellCastingType.KNOWN
                    && spellcastingClass.spellsKnownMax() != null) {
                long spellsKnown = sheet.spells().stream()
                        .filter(known -> known.className().equals(spell.className()) && known.level() > 0)
                        .count();
                if (spellsKnown >= spellcastingClass.spellsKnownMax()) {
                    throw new SpellLimitExceededException(spellcastingClass.spellsKnownMax());
                }
            }
        }

        List<Dnd5eSpell> spells = new ArrayList<>(sheet.spells());
        spells.add(new Dnd5eSpell(
                spell.key(), spell.name(), spell.className(), spell.level(), spell.school(), spell.castingTime(),
                spell.range(), spell.concentration(), spell.ritual(), spell.attackRoll(), spell.damageDiceCount(),
                spell.damageDiceSides(), spell.damageType(), spell.notes(), spell.effectSummary(), false, false,
                spell.description(), spell.saveAbility(), spell.components(), spell.materialComponent(),
                spell.duration(), spell.higherLevelsDescription(), spell.higherLevelsDamageDiceCount(),
                spell.higherLevelsDamageDiceSides()));
        return write(sheet.withSpells(spells));
    }

    @Override
    public String removeSpell(String sheetJson, String spellKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eSpell> spells = sheet.spells().stream().filter(spell -> !spell.key().equals(spellKey)).toList();
        return write(sheet.withSpells(spells));
    }

    @Override
    public String learnFeat(String sheetJson, FeatureTrait feat) {
        Dnd5eSheet sheet = read(sheetJson);
        if (sheet.featureTraits().stream().anyMatch(known -> known.key().equals(feat.key()))) {
            return sheetJson;
        }

        List<Dnd5eFeatureTrait> featureTraits = new ArrayList<>(sheet.featureTraits());
        featureTraits.add(new Dnd5eFeatureTrait(
                feat.key(), feat.name(), Dnd5eFeatureTraitCategory.FEAT, feat.source(), feat.summary(),
                feat.description(), null, 0, null));
        return write(sheet.withFeatureTraits(featureTraits));
    }

    @Override
    public String removeFeat(String sheetJson, String featureKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eFeatureTrait> featureTraits =
                sheet.featureTraits().stream().filter(feature -> !feature.key().equals(featureKey)).toList();
        return write(sheet.withFeatureTraits(featureTraits));
    }

    @Override
    public String prepareSpell(String sheetJson, String spellKey) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eSpell target = findSpell(sheet, spellKey);
        if (!preparable(sheet, target)) {
            return sheetJson;
        }

        Dnd5eSpellcastingClass spellcastingClass = findSpellcastingClass(sheet, target.className());
        if (spellcastingClass != null && spellcastingClass.spellsPreparedMax() != null) {
            long prepared = countPrepared(sheet, target.className());
            if (prepared >= spellcastingClass.spellsPreparedMax()) {
                throw new SpellPreparationLimitExceededException(spellcastingClass.spellsPreparedMax());
            }
        }
        return write(sheet.withSpells(withPrepared(sheet.spells(), spellKey, true)));
    }

    @Override
    public String unprepareSpell(String sheetJson, String spellKey) {
        Dnd5eSheet sheet = read(sheetJson);
        Dnd5eSpell target = findSpell(sheet, spellKey);
        if (!preparable(sheet, target)) {
            return sheetJson;
        }
        return write(sheet.withSpells(withPrepared(sheet.spells(), spellKey, false)));
    }

    /** A cantrip, an always-prepared spell, an unknown key or a KNOWN-type class's spell are never preparable. */
    private boolean preparable(Dnd5eSheet sheet, Dnd5eSpell spell) {
        if (spell == null || spell.level() == 0 || spell.alwaysPrepared()) {
            return false;
        }
        Dnd5eSpellcastingClass spellcastingClass = findSpellcastingClass(sheet, spell.className());
        return spellcastingClass == null || spellcastingClass.castingType() == Dnd5eSpellCastingType.PREPARED;
    }

    private long countPrepared(Dnd5eSheet sheet, String className) {
        return sheet.spells().stream()
                .filter(spell -> spell.className().equals(className) && spell.level() > 0 && spell.prepared())
                .count();
    }

    private long countByClassAndLevel(Dnd5eSheet sheet, String className, int level) {
        return sheet.spells().stream()
                .filter(spell -> spell.className().equals(className) && spell.level() == level)
                .count();
    }

    private List<Dnd5eSpell> withPrepared(List<Dnd5eSpell> spells, String spellKey, boolean prepared) {
        return spells.stream()
                .map(spell -> spell.key().equals(spellKey) ? withPrepared(spell, prepared) : spell)
                .toList();
    }

    private Dnd5eSpell withPrepared(Dnd5eSpell spell, boolean prepared) {
        return spell.withPrepared(prepared);
    }

    private Dnd5eSpell findSpell(Dnd5eSheet sheet, String spellKey) {
        return sheet.spells().stream().filter(spell -> spell.key().equals(spellKey)).findFirst().orElse(null);
    }

    private Dnd5eSpellcastingClass findSpellcastingClass(Dnd5eSheet sheet, String className) {
        return sheet.spellcastingClasses().stream()
                .filter(spellcastingClass -> spellcastingClass.className().equals(className))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String updateBackgroundField(String sheetJson, String field, String value) {
        Dnd5eBackgroundField parsedField = parseBackgroundField(field);
        Dnd5eSheet sheet = read(sheetJson);
        return write(sheet.withBackground(sheet.background().withField(parsedField, value)));
    }

    private Dnd5eBackgroundField parseBackgroundField(String field) {
        try {
            return Dnd5eBackgroundField.valueOf(field.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidBackgroundFieldException(field);
        }
    }

    @Override
    public String addCustomAction(String sheetJson, CustomAction action) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eCustomAction> customActions = new ArrayList<>(sheet.customActions());
        customActions.add(toCustomAction(UUID.randomUUID().toString(), action));
        return write(sheet.withCustomActions(customActions));
    }

    @Override
    public String updateCustomAction(String sheetJson, String actionKey, CustomAction action) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eCustomAction> customActions = sheet.customActions().stream()
                .map(existing -> existing.key().equals(actionKey) ? toCustomAction(actionKey, action) : existing)
                .toList();
        return write(sheet.withCustomActions(customActions));
    }

    private Dnd5eCustomAction toCustomAction(String key, CustomAction action) {
        return new Dnd5eCustomAction(
                key,
                parseEnum(Dnd5eCustomActionTemplate.class, "template", action.template()),
                action.name(),
                action.snippet(),
                action.description(),
                parseNullableEnum(Dnd5eRangeCategory.class, "rangeCategory", action.rangeCategory()),
                action.rangeFeet(),
                action.stat(),
                action.diceCount(),
                action.dieType(),
                action.fixedValue(),
                action.damageType(),
                action.saveType(),
                action.fixedSaveDc(),
                parseNullableEnum(Dnd5eSpellRangeType.class, "spellRangeType", action.spellRangeType()),
                parseNullableEnum(Dnd5eAreaOfEffectType.class, "aoeType", action.aoeType()),
                action.aoeSize(),
                parseNullableEnum(Dnd5eActivationType.class, "activationType", action.activationType()),
                action.activationTime(),
                action.affectedByMartialArts(),
                action.proficient(),
                action.displayAsAttack(),
                parseNullableEnum(Dnd5eWeaponAttackType.class, "weaponAttackType", action.weaponAttackType()),
                action.longRange(),
                action.dualWield(),
                action.silvered());
    }

    @Override
    public String removeCustomAction(String sheetJson, String actionKey) {
        Dnd5eSheet sheet = read(sheetJson);
        List<Dnd5eCustomAction> customActions = sheet.customActions().stream()
                .filter(action -> !action.key().equals(actionKey))
                .toList();
        return write(sheet.withCustomActions(customActions));
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String field, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidCustomActionFieldException(field, value);
        }
    }

    /** Blank/null is a valid "unset" for the several optional closed-set fields — D&D Beyond's own {@code --}. */
    private <T extends Enum<T>> T parseNullableEnum(Class<T> type, String field, String value) {
        return value == null || value.isBlank() ? null : parseEnum(type, field, value);
    }

    /** Same parsing as {@link #parseEnum}, but a server-side data bug (see {@link InvalidItemFieldException}), not a client mistake. */
    private <T extends Enum<T>> T parseItemEnum(Class<T> type, String field, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidItemFieldException(field, value);
        }
    }

    private <T extends Enum<T>> T parseNullableItemEnum(Class<T> type, String field, String value) {
        return value == null || value.isBlank() ? null : parseItemEnum(type, field, value);
    }

    private Dnd5eSheet read(String sheetJson) {
        return objectMapper.readValue(sheetJson, Dnd5eSheet.class);
    }

    /** Death save counts only exist at 0 hit points: regaining any hit points clears them. */
    private String write(Dnd5eSheet sheet) {
        boolean hasCounts = sheet.deathSaves() != null && !sheet.deathSaves().equals(Dnd5eDeathSaves.NONE);
        Dnd5eSheet settled = sheet.currentHitPoints() > 0 && hasCounts ? sheet.withDeathSaves(Dnd5eDeathSaves.NONE) : sheet;
        return objectMapper.writeValueAsString(settled);
    }
}
