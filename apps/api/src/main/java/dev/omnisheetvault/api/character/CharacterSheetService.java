package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueEntry;
import dev.omnisheetvault.api.catalogue.CatalogueEntryKind;
import dev.omnisheetvault.api.catalogue.CatalogueEntryResponse;
import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.ruleset.CustomAction;
import dev.omnisheetvault.api.ruleset.DeathSaves;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.InsufficientHitDiceException;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.ItemGrantedSpell;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.SheetMutator;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.StartingItem;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.ruleset.registry.MechanicResolverRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetMutatorRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/**
 * Orchestrates a character's sheet: ownership through {@link CharacterService}, the
 * math and the mutation rules through the ruleset seam. No game rules live here — see
 * architecture.md. Every mutation persists immediately and returns the recalculated
 * vitals, so the caller always gets the server's truth back — see
 * features/character-sheet.md's "no explicit save". {@link #spendHitDice} additionally
 * resolves a roll through the mechanic resolver seam and records it through
 * {@link RollService#recordRoll} — a hit dice spend is a roll whose result heals, so
 * it needs both seams, but stays here (not in {@code dice}) because only this
 * package's {@link CharacterService} can persist the resulting sheet.
 * {@link #learnSpell} additionally depends on {@code catalogue}'s
 * {@link CatalogueService} — allowed cross-feature (architecture.md: "through the
 * other feature's service") — to turn a catalogue entry into the generic
 * {@link Spell} shape {@link SheetMutator#learnSpell} expects. {@link #addCatalogueItem}
 * is the same cross-feature dependency for the same reason —
 * systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 2 — turning a catalogue entry
 * into the generic {@link Item} shape {@link SheetMutator#addCatalogueItem} expects.
 * {@link #learnFeat} is the same pattern again, turning a catalogue entry into the
 * generic {@link FeatureTrait} shape {@link SheetMutator#learnFeat} expects.
 * {@link #applyShortRest(Jwt, UUID, int)} is the Mechanic mold's first real
 * trigger (phase 9): like {@link #spendHitDice}, it is a roll and a mutation
 * at once when the player chooses to spend hit dice, so it returns its own
 * result type rather than a plain {@link VitalsZone}.
 */
@Service
public class CharacterSheetService {

    private final CharacterService characterService;
    private final SheetCalculatorRegistry sheetCalculatorRegistry;
    private final SheetMutatorRegistry sheetMutatorRegistry;
    private final MechanicResolverRegistry mechanicResolverRegistry;
    private final RollService rollService;
    private final CatalogueService catalogueService;
    private final ObjectMapper objectMapper;

    public CharacterSheetService(
            CharacterService characterService,
            SheetCalculatorRegistry sheetCalculatorRegistry,
            SheetMutatorRegistry sheetMutatorRegistry,
            MechanicResolverRegistry mechanicResolverRegistry,
            RollService rollService,
            CatalogueService catalogueService,
            ObjectMapper objectMapper) {
        this.characterService = characterService;
        this.sheetCalculatorRegistry = sheetCalculatorRegistry;
        this.sheetMutatorRegistry = sheetMutatorRegistry;
        this.mechanicResolverRegistry = mechanicResolverRegistry;
        this.rollService = rollService;
        this.catalogueService = catalogueService;
        this.objectMapper = objectMapper;
    }

    public VitalsZone getVitals(Jwt jwt, UUID characterId) {
        Character character = characterService.getMineActive(jwt, characterId);
        return sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet());
    }

    public VitalsZone applyDamage(Jwt jwt, UUID characterId, int amount) {
        return applyDamage(jwt, characterId, amount, false);
    }

    public VitalsZone applyDamage(Jwt jwt, UUID characterId, int amount, boolean critical) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.applyDamage(sheetJson, amount, critical));
    }

    public VitalsZone setSheetTheme(Jwt jwt, UUID characterId, String theme) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setSheetTheme(sheetJson, theme));
    }

    public VitalsZone customize(Jwt jwt, UUID characterId, String group, String target, String valueJson) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.customize(sheetJson, group, target, valueJson));
    }

    public VitalsZone removeCustomization(Jwt jwt, UUID characterId, String group, String target) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeCustomization(sheetJson, group, target));
    }

    public VitalsZone setDeathSaves(Jwt jwt, UUID characterId, int successes, int failures) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setDeathSaves(sheetJson, successes, failures));
    }

    public VitalsZone clearDeathSaves(Jwt jwt, UUID characterId) {
        return mutate(jwt, characterId, SheetMutator::clearDeathSaves);
    }

    /** {@code newTotal} maps the current experience points to the new ones. */
    public VitalsZone changeExperience(Jwt jwt, UUID characterId, IntUnaryOperator newTotal) {
        Character character = characterService.getMineActive(jwt, characterId);
        VitalsZone vitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet());
        int current = vitals.experience() == null ? 0 : vitals.experience().points();
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setExperiencePoints(sheetJson, newTotal.applyAsInt(current)));
    }

    /** Checks the character is dying before rolling, so a refused save never leaves a roll in the log. */
    public DeathSaveRollResult rollDeathSave(Jwt jwt, UUID characterId) {
        Character character = characterService.getMineActive(jwt, characterId);
        DeathSaves saves = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet()).deathSaves();
        if (saves == null || !saves.dying() || saves.stable() || saves.dead()) {
            throw new NotDyingException();
        }
        Roll roll = rollService.recordRoll(character.id(), 1, 20, 0, "Death saving throw");
        String updatedSheetJson = sheetMutatorRegistry.forSystem(character.systemId()).applyDeathSaveRoll(character.sheet(), roll.total());
        character.replaceSheet(updatedSheetJson);
        characterService.save(character);
        return new DeathSaveRollResult(sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(updatedSheetJson), roll);
    }

    public VitalsZone applyHealing(Jwt jwt, UUID characterId, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.applyHealing(sheetJson, amount));
    }

    public VitalsZone setTemporaryHitPoints(Jwt jwt, UUID characterId, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setTemporaryHitPoints(sheetJson, amount));
    }

    public VitalsZone toggleInspiration(Jwt jwt, UUID characterId) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.toggleInspiration(sheetJson));
    }

    /** The condition's catalogue mechanics are copied onto the sheet as it turns on (copy-once). */
    public VitalsZone toggleCondition(Jwt jwt, UUID characterId, String condition) {
        Character character = characterService.getMineActive(jwt, characterId);
        String conditionData = catalogueService.findData(character.systemId(), CatalogueEntryKind.CONDITION, condition).orElse(null);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.toggleCondition(sheetJson, condition, conditionData));
    }

    public VitalsZone setExhaustionLevel(Jwt jwt, UUID characterId, int level) {
        Character character = characterService.getMineActive(jwt, characterId);
        String exhaustionData = catalogueService.findData(character.systemId(), CatalogueEntryKind.CONDITION, "exhaustion").orElse(null);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setExhaustionLevel(sheetJson, level, exhaustionData));
    }

    public VitalsZone addItem(
            Jwt jwt, UUID characterId, String name, int quantity, String cost, String notes, boolean requiresAttunement,
            String storageLocation) {
        return mutate(jwt, characterId,
                (mutator, sheetJson) -> mutator.addItem(sheetJson, name, quantity, cost, notes, requiresAttunement, storageLocation));
    }

    /**
     * Copies a real catalogue item onto the character's own sheet —
     * systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 2. Same "always read
     * unredacted" reasoning as {@link #learnSpell}: a player's own copy of an item
     * they picked up is no longer catalogue browsing.
     */
    public VitalsZone addCatalogueItem(Jwt jwt, UUID characterId, UUID catalogueEntryId) {
        CatalogueEntryResponse entry = catalogueService.getUnredacted(catalogueEntryId);
        Item item = toItem(entry);
        String itemData = objectMapper.writeValueAsString(entry.data());
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.addCatalogueItem(sheetJson, item, itemData));
    }

    public VitalsZone removeItem(Jwt jwt, UUID characterId, String itemKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeItem(sheetJson, itemKey));
    }

    public VitalsZone toggleItemEquipped(Jwt jwt, UUID characterId, String itemKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.toggleItemEquipped(sheetJson, itemKey));
    }

    public VitalsZone toggleItemAttuned(Jwt jwt, UUID characterId, String itemKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.toggleItemAttuned(sheetJson, itemKey));
    }

    public VitalsZone setItemQuantity(Jwt jwt, UUID characterId, String itemKey, int quantity) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setItemQuantity(sheetJson, itemKey, quantity));
    }

    public VitalsZone moveItem(Jwt jwt, UUID characterId, String itemKey, String storageLocation) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.moveItem(sheetJson, itemKey, storageLocation));
    }

    public VitalsZone setTrackEncumbrance(Jwt jwt, UUID characterId, boolean trackEncumbrance) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setTrackEncumbrance(sheetJson, trackEncumbrance));
    }

    public VitalsZone addCoins(Jwt jwt, UUID characterId, String denomination, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.addCoins(sheetJson, denomination, amount));
    }

    public VitalsZone removeCoins(Jwt jwt, UUID characterId, String denomination, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeCoins(sheetJson, denomination, amount));
    }

    public VitalsZone applyExtraDamage(Jwt jwt, UUID characterId, String extraKey, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.applyExtraDamage(sheetJson, extraKey, amount));
    }

    public VitalsZone applyExtraHealing(Jwt jwt, UUID characterId, String extraKey, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.applyExtraHealing(sheetJson, extraKey, amount));
    }

    public VitalsZone setExtraTemporaryHitPoints(Jwt jwt, UUID characterId, String extraKey, int amount) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.setExtraTemporaryHitPoints(sheetJson, extraKey, amount));
    }

    public VitalsZone removeExtra(Jwt jwt, UUID characterId, String extraKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeExtra(sheetJson, extraKey));
    }

    public VitalsZone useFeatureAction(Jwt jwt, UUID characterId, String featureKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.useFeatureAction(sheetJson, featureKey));
    }

    public VitalsZone restoreFeatureAction(Jwt jwt, UUID characterId, String featureKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.restoreFeatureAction(sheetJson, featureKey));
    }

    public VitalsZone useFeatureTraitUse(Jwt jwt, UUID characterId, String featureKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.useFeatureTraitUse(sheetJson, featureKey));
    }

    public VitalsZone restoreFeatureTraitUse(Jwt jwt, UUID characterId, String featureKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.restoreFeatureTraitUse(sheetJson, featureKey));
    }

    public VitalsZone consumeSpellSlot(Jwt jwt, UUID characterId, int level, boolean pact) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.consumeSpellSlot(sheetJson, level, pact));
    }

    public VitalsZone restoreSpellSlot(Jwt jwt, UUID characterId, int level, boolean pact) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.restoreSpellSlot(sheetJson, level, pact));
    }

    /** The spell's catalogue data is read as it is cast, so its lasting effect is copied onto the sheet (copy-once). */
    public VitalsZone castSpell(Jwt jwt, UUID characterId, String spellKey, int slotLevel, boolean pact, boolean onSelf) {
        Character character = characterService.getMineActive(jwt, characterId);
        String spellData = catalogueService.findData(character.systemId(), CatalogueEntryKind.SPELL, spellKey).orElse(null);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.castSpell(sheetJson, spellKey, slotLevel, pact, spellData, onSelf));
    }

    public VitalsZone endActiveEffect(Jwt jwt, UUID characterId, String effectKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.endActiveEffect(sheetJson, effectKey));
    }

    /** Spends an item-granted spell's own charge cost — systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 7. */
    public VitalsZone castItemGrantedSpell(Jwt jwt, UUID characterId, String spellKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.castItemGrantedSpell(sheetJson, spellKey));
    }

    /**
     * A short rest restores every matching resource and, optionally, spends the
     * player's chosen number of hit dice in the same operation — matching D&D
     * Beyond's own short rest panel, which offers the hit-dice choice inline
     * rather than as a separate action. Every chosen die size is checked before
     * anything is rolled or persisted, same reasoning as {@link #spendHitDice}; then
     * each size gets its own roll. Spending nothing makes no roll at all.
     * {@code hitDiceBySize} (die size → count) wins over {@code hitDiceSpent}, which
     * spends dice of the largest size with dice left.
     */
    public ShortRestResult applyShortRest(Jwt jwt, UUID characterId, int hitDiceSpent, Map<Integer, Integer> hitDiceBySize) {
        Character character = characterService.getMineActive(jwt, characterId);
        SheetMutator mutator = sheetMutatorRegistry.forSystem(character.systemId());
        VitalsZone vitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet());
        Map<Integer, Integer> spending = hitDiceBySize != null
                ? hitDiceBySize
                : hitDiceSpent > 0 ? Map.of(vitals.hitDice().defaultDieSize(), hitDiceSpent) : Map.of();
        spending.forEach((dieSize, count) -> requireAvailable(vitals, dieSize, count));

        String updatedSheetJson = mutator.applyShortRest(character.sheet());
        List<Roll> rolls = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : spending.entrySet()) {
            if (entry.getValue() > 0) {
                Roll roll = rollHitDice(character, vitals, entry.getKey(), entry.getValue());
                rolls.add(roll);
                updatedSheetJson = mutator.spendHitDice(updatedSheetJson, entry.getKey(), entry.getValue(), roll.total());
            }
        }

        character.replaceSheet(updatedSheetJson);
        characterService.save(character);
        VitalsZone updatedVitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(updatedSheetJson);
        return new ShortRestResult(updatedVitals, List.copyOf(rolls));
    }

    /** {@code hitDiceRecovered} is the player's choice of spent dice to recover (die size → count); null lets the system choose. */
    public VitalsZone applyLongRest(Jwt jwt, UUID characterId, Map<Integer, Integer> hitDiceRecovered) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.applyLongRest(sheetJson, hitDiceRecovered));
    }

    /**
     * Copies a catalogue spell into the character's own known spells — phase 9's
     * "Manage spells". Reads the catalogue entry unredacted (see
     * {@link CatalogueService#getUnredacted}): a player's own learned copy is no
     * longer catalogue browsing, so it always carries the real description,
     * regardless of the display-only redaction switch.
     */
    public VitalsZone learnSpell(Jwt jwt, UUID characterId, UUID catalogueEntryId, String className) {
        Spell spell = toSpell(catalogueService.getUnredacted(catalogueEntryId), className);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.learnSpell(sheetJson, spell));
    }

    public VitalsZone removeSpell(Jwt jwt, UUID characterId, String spellKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeSpell(sheetJson, spellKey));
    }

    /**
     * Copies a catalogue feat into the character's own feature traits. Same
     * "always read unredacted" reasoning as {@link #learnSpell}.
     */
    public VitalsZone learnFeat(Jwt jwt, UUID characterId, UUID catalogueEntryId) {
        FeatureTrait feat = toFeatureTrait(catalogueService.getUnredacted(catalogueEntryId));
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.learnFeat(sheetJson, feat));
    }

    public VitalsZone removeFeat(Jwt jwt, UUID characterId, String featureKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeFeat(sheetJson, featureKey));
    }

    public VitalsZone prepareSpell(Jwt jwt, UUID characterId, String spellKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.prepareSpell(sheetJson, spellKey));
    }

    public VitalsZone unprepareSpell(Jwt jwt, UUID characterId, String spellKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.unprepareSpell(sheetJson, spellKey));
    }

    public VitalsZone updateBackgroundField(Jwt jwt, UUID characterId, String field, String value) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.updateBackgroundField(sheetJson, field, value));
    }

    public VitalsZone addCustomAction(Jwt jwt, UUID characterId, AddCustomActionRequest request) {
        CustomAction action = toCustomAction(request);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.addCustomAction(sheetJson, action));
    }

    public VitalsZone updateCustomAction(Jwt jwt, UUID characterId, String actionKey, AddCustomActionRequest request) {
        CustomAction action = toCustomAction(request);
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.updateCustomAction(sheetJson, actionKey, action));
    }

    public VitalsZone removeCustomAction(Jwt jwt, UUID characterId, String actionKey) {
        return mutate(jwt, characterId, (mutator, sheetJson) -> mutator.removeCustomAction(sheetJson, actionKey));
    }

    private CustomAction toCustomAction(AddCustomActionRequest request) {
        return new CustomAction(
                null, request.template(), request.name(), request.snippet(), request.description(),
                request.rangeCategory(), request.rangeFeet(), request.stat(), request.diceCount(), request.dieType(),
                request.fixedValue(), request.damageType(), request.saveType(), request.fixedSaveDc(),
                request.spellRangeType(), request.aoeType(), request.aoeSize(), request.activationType(),
                request.activationTime(), request.affectedByMartialArts(), request.proficient(),
                request.displayAsAttack(), request.weaponAttackType(), request.longRange(), request.dualWield(),
                request.silvered());
    }

    /**
     * A feat's catalogue entry carries only a name and a full description — no
     * separate short "summary" line the way a hand-authored
     * {@code Dnd5eFeatureTrait} does. {@link #featSummary} derives one mechanically
     * from the real description's own first sentence, rather than writing new
     * text, so the tab's inline row still gets a short line without inventing
     * content 5etools itself never supplied.
     */
    private FeatureTrait toFeatureTrait(CatalogueEntryResponse entry) {
        String description = entry.description().value();
        return new FeatureTrait(
                entry.slug(), entry.name(), "FEAT", "Feat", featSummary(description), description, null, 0, null);
    }

    private static String featSummary(String description) {
        int periodSpace = description.indexOf(". ");
        int end = periodSpace >= 0 ? periodSpace + 1 : Math.min(description.length(), 120);
        String summary = description.substring(0, end).trim();
        return end < description.length() && periodSpace < 0 ? summary + "…" : summary;
    }

    private Spell toSpell(CatalogueEntryResponse entry, String className) {
        SpellCatalogueData data = objectMapper.treeToValue(entry.data(), SpellCatalogueData.class);
        return new Spell(
                entry.slug(), entry.name(), className, data.level(), data.school(), data.castingTime(), data.range(),
                data.concentration(), data.ritual(), data.attackRoll(), data.damageDiceCount(), data.damageDiceSides(),
                data.damageType(), data.notes(), data.effectSummary(), false, false, entry.description().value(),
                data.saveAbility(), data.components(), data.materialComponent(), data.duration(),
                data.higherLevelsDescription(), data.higherLevelsDamageDiceCount(), data.higherLevelsDamageDiceSides());
    }

    /**
     * Adds a materialized build's starting items and coins to a sheet, through the
     * same catalogue copy a player's own item takes. Coins go in as gold, silver and
     * copper.
     */
    String withStartingEquipment(String systemId, String sheetJson, List<StartingItem> startingItems, int copper) {
        SheetMutator mutator = sheetMutatorRegistry.forSystem(systemId);
        String sheet = sheetJson;
        for (StartingItem startingItem : startingItems) {
            String notes = startingItem.displayName() == null ? "" : startingItem.displayName();
            if (startingItem.catalogueSlug() != null) {
                CatalogueEntry entry = catalogueService.findBySlug(systemId, CatalogueEntryKind.ITEM, startingItem.catalogueSlug());
                CatalogueEntryResponse catalogueEntry = catalogueService.getUnredacted(entry.id());
                sheet = mutator.addCatalogueItem(sheet, toItem(catalogueEntry, startingItem.quantity(), notes, startingItem.equipped()),
                        objectMapper.writeValueAsString(catalogueEntry.data()));
            } else {
                sheet = mutator.addItem(sheet, startingItem.customName(), startingItem.quantity(), "", notes, false, null);
            }
        }
        sheet = addCoinsIfAny(mutator, sheet, "GOLD", copper / 100);
        sheet = addCoinsIfAny(mutator, sheet, "SILVER", copper % 100 / 10);
        return addCoinsIfAny(mutator, sheet, "COPPER", copper % 10);
    }

    private static String addCoinsIfAny(SheetMutator mutator, String sheetJson, String denomination, int amount) {
        return amount > 0 ? mutator.addCoins(sheetJson, denomination, amount) : sheetJson;
    }

    private Item toItem(CatalogueEntryResponse entry) {
        return toItem(entry, 1, "", false);
    }

    private Item toItem(CatalogueEntryResponse entry, int quantity, String notes, boolean equipped) {
        ItemCatalogueData data = objectMapper.treeToValue(entry.data(), ItemCatalogueData.class);
        return new Item(
                entry.slug(), entry.name(), quantity, formatCost(data.costGp()), notes, equipped, false,
                data.requiresAttunement(), entry.slug(),
                data.itemKind(), data.typeLabel(), data.rarity(), data.attunementRequirement(), data.weightLb(),
                data.costGp(), data.weaponCategory(), data.attackType(), data.damageDiceCount(),
                data.damageDiceSides(), data.damageType(), data.versatileDamageDiceCount(),
                data.versatileDamageDiceSides(), data.properties(), data.finesse(), data.normalRange(),
                data.longRange(), data.armorCategory(), data.baseArmorClass(), data.stealthDisadvantage(),
                data.strengthRequirement(), data.weaponAttackBonus(), data.weaponDamageBonus(),
                data.armorClassBonus(), data.charges(), data.rechargeTrigger(), data.rechargeFormula(),
                data.grantedSpells().stream().map(granted -> toItemGrantedSpell(entry.systemId(), granted)).toList(),
                0, "EQUIPMENT", formatSource(entry.sourceBook(), entry.sourcePage()));
    }

    /** {@code sourceBook} is never null for a real catalogue entry; {@code sourcePage} is, for the rare entry 5etools gives no page. */
    private static String formatSource(String sourceBook, Integer sourcePage) {
        return sourcePage == null ? sourceBook : sourceBook + ", p. " + sourcePage;
    }

    /**
     * Resolves the granted spell's own catalogue entry to build the full
     * display/cast data {@code Dnd5eSheetMutator#addCatalogueItem} needs — see
     * {@code ItemGrantedSpell}'s own doc comment for why this can't be deferred
     * to calculation time. Read unredacted, same reasoning as {@link #toSpell}:
     * a granted spell is about to be copied onto the player's own sheet.
     */
    private ItemGrantedSpell toItemGrantedSpell(String systemId, ItemCatalogueData.GrantedSpellData granted) {
        CatalogueEntry spellEntry = catalogueService.findBySlug(systemId, CatalogueEntryKind.SPELL, granted.spellSlug());
        SpellCatalogueData spellData = objectMapper.readValue(spellEntry.data(), SpellCatalogueData.class);
        return new ItemGrantedSpell(
                granted.spellSlug(), spellEntry.name(), granted.chargeCost(), granted.fixedSaveDc(),
                spellData.level(), spellData.school(), spellData.castingTime(), spellData.range(),
                spellData.concentration(), spellData.ritual(), spellData.attackRoll(), spellData.damageDiceCount(),
                spellData.damageDiceSides(), spellData.damageType(), spellData.notes(), spellData.effectSummary(),
                spellEntry.description(), spellData.saveAbility(), spellData.components(),
                spellData.materialComponent(), spellData.duration());
    }

    /** {@code cost} is a display string (see {@code Dnd5eItem}'s own doc comment) — {@code null} for the many magic items 5etools prices at "—". */
    private static void requireAvailable(VitalsZone vitals, int dieSize, int count) {
        int available = vitals.hitDice().pool(dieSize).map(HitDice.Pool::available).orElse(0);
        if (count > available) {
            throw new InsufficientHitDiceException(count, available);
        }
    }

    private Roll rollHitDice(Character character, VitalsZone vitals, int dieSize, int count) {
        ResolvedRoll perDie = mechanicResolverRegistry.forSystem(character.systemId())
                .resolve(vitals, RollKind.HIT_DICE, String.valueOf(dieSize), null);
        return rollService.recordRoll(character.id(), count, perDie.diceSides(), count * perDie.modifier(), perDie.context());
    }

    private static String formatCost(Double costGp) {
        if (costGp == null) {
            return "";
        }
        return (costGp == Math.floor(costGp) ? String.valueOf(costGp.intValue()) : String.valueOf(costGp)) + " gp";
    }

    /**
     * Availability is checked before anything is rolled or persisted, so a rejected
     * spend never leaves an orphaned roll in the log.
     */
    public HitDiceSpendResult spendHitDice(Jwt jwt, UUID characterId, int count, Integer dieSize) {
        Character character = characterService.getMineActive(jwt, characterId);
        VitalsZone vitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet());
        int size = dieSize != null ? dieSize : vitals.hitDice().defaultDieSize();
        requireAvailable(vitals, size, count);

        Roll roll = rollHitDice(character, vitals, size, count);
        SheetMutator mutator = sheetMutatorRegistry.forSystem(character.systemId());
        String updatedSheetJson = mutator.spendHitDice(character.sheet(), size, count, roll.total());
        character.replaceSheet(updatedSheetJson);
        characterService.save(character);
        VitalsZone updatedVitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(updatedSheetJson);

        return new HitDiceSpendResult(updatedVitals, roll);
    }

    private VitalsZone mutate(Jwt jwt, UUID characterId, BiFunction<SheetMutator, String, String> mutation) {
        Character character = characterService.getMineActive(jwt, characterId);
        SheetMutator mutator = sheetMutatorRegistry.forSystem(character.systemId());
        String updatedSheetJson = mutation.apply(mutator, character.sheet());
        character.replaceSheet(updatedSheetJson);
        characterService.save(character);
        return sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(updatedSheetJson);
    }
}
