package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * One inventory item — see systems/dnd-5e/sheet-ui.md's Inventory tab. No
 * containers (adr deviation list): every item lives in this one flat list.
 * {@code cost} is a display string, not a currency amount tied to
 * {@code Dnd5eSheet}'s coin totals — buying and selling isn't modeled, only
 * carrying. {@code equipped} and {@code attuned} are real mutable state
 * (toggled through the Collection editor's item rows). {@code requiresAttunement}
 * is set once at item creation, not toggled later: only items flagged this way can
 * ever be attuned — see {@link dev.omnisheetvault.api.ruleset.AttunementNotAllowedException}.
 *
 * <p>{@code catalogueSlug} through {@code grantedSpells} are
 * systems/dnd-5e/features/inventory-equipment-mechanics.md's own addition (slice 2): a 1:1 copy of
 * the real 5etools item data at the moment it was added to the sheet — the same
 * "copy once, never re-read the catalogue" treatment {@code Dnd5eSpell} already gives
 * a learned spell, for the same reason (an item looted three sessions ago shouldn't
 * retroactively change if the catalogue is later re-ingested with a correction).
 * {@code catalogueSlug} is {@code null} for a freeform/homebrew item added by hand
 * (the Inventory tab's own free-text add form, unchanged since phase 8) — such an
 * item never gets weapon/armour/charge data, only the eight fields above; giving a
 * player-typed item a hand-picked weapon shape was a deliberate non-goal, see that
 * feature doc's own decision 3. {@code itemKind} defaults to {@code GEAR} for a
 * freeform item and for any item predating this field in already-persisted sheet
 * JSON — the compact constructor below normalizes a null the same way
 * {@code JacksonConfig}'s disabled {@code FAIL_ON_NULL_FOR_PRIMITIVES} already
 * normalizes a missing primitive, since {@code itemKind} drives dispatch in later
 * slices (equipped-weapon-to-attack, equipped-armour-to-AC) that should never see a
 * null. {@code properties}/{@code grantedSpells} are normalized to an empty list for
 * the same reason. Weapon fields ({@code weaponCategory} through {@code finesse},
 * plus {@code normalRange}/{@code longRange}) are only ever populated when
 * {@code itemKind} is {@code WEAPON}; armour fields ({@code armorCategory} through
 * {@code strengthRequirement}) only when {@code ARMOR} or {@code SHIELD} (shields
 * never set {@code armorCategory}, see {@link Dnd5eItemKind}). {@code
 * weaponAttackBonus}/{@code weaponDamageBonus}/{@code armorClassBonus} are a rare
 * named magic item's own literal {@code +N} (e.g. a specific enchanted blade, not a
 * generic {@code +1} variant — see that feature doc's own "Out of scope"). {@code
 * charges}/{@code rechargeTrigger}/{@code rechargeFormula}/{@code grantedSpells} are
 * the Wand of Fireballs mechanic. {@code chargesUsed} is the only mutable piece of
 * that mechanic (slice 7) — real session state, same treatment as {@code equipped}/
 * {@code attuned} — normalized to {@code 0} by the compact constructor for the same
 * reason {@code itemKind} is: an item predating this field, or one with no charges
 * at all, should never expose a null here. {@link Dnd5eSheetMutator#castItemGrantedSpell}
 * is the only thing that changes it; {@link Dnd5eSheetMutator#applyLongRest} resets it
 * to {@code 0} for every item — a full-restore simplification of the real "regains
 * 1d6+1 daily at dawn" rule (this app's session model has rests, not calendar days),
 * matching the "reuse the phase 9 recharge-trigger concept" decision this feature's own
 * doc records, not the random partial-recharge amount.
 *
 * <p>{@code storageLocation} (weight/encumbrance initiative) is which of
 * {@link Dnd5eStorageLocation}'s four buckets this item sits in, mutable via
 * {@link Dnd5eSheetMutator#moveItem} — normalized to {@code EQUIPMENT} by the compact
 * constructor for an item predating this field, same reasoning as {@code itemKind}.
 *
 * <p>{@code source} is a pre-formatted "book, page" display string, copied once from
 * the catalogue entry's own {@code sourceBook}/{@code sourcePage} at the moment a
 * catalogue item is added — same "copy once" treatment as every other catalogue field
 * above. {@code null} for a freeform item, or for one predating this field; unlike
 * {@code itemKind}/{@code storageLocation} there is no sensible default to normalize a
 * null to, so the compact constructor leaves it alone and the sidebar simply omits the
 * "Source" row when it's absent.
 */
public record Dnd5eItem(
        @NotBlank String key,
        @NotBlank String name,
        @Min(1) int quantity,
        @NotNull String cost,
        @NotNull String notes,
        boolean equipped,
        boolean attuned,
        boolean requiresAttunement,
        String catalogueSlug,
        Dnd5eItemKind itemKind,
        String typeLabel,
        String rarity,
        String attunementRequirement,
        Double weightLb,
        Double costGp,
        Dnd5eWeaponCategory weaponCategory,
        Dnd5eRangeCategory rangeCategory,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        Integer versatileDamageDiceCount,
        Integer versatileDamageDiceSides,
        List<String> properties,
        boolean finesse,
        Integer normalRange,
        Integer longRange,
        Dnd5eArmorCategory armorCategory,
        Integer baseArmorClass,
        boolean stealthDisadvantage,
        Integer strengthRequirement,
        Integer weaponAttackBonus,
        Integer weaponDamageBonus,
        Integer armorClassBonus,
        Integer charges,
        String rechargeTrigger,
        String rechargeFormula,
        List<Dnd5eGrantedSpell> grantedSpells,
        int chargesUsed,
        Dnd5eStorageLocation storageLocation,
        String source,
        @Valid Dnd5eItemMechanics mechanics) {

    public Dnd5eItem {
        itemKind = itemKind == null ? Dnd5eItemKind.GEAR : itemKind;
        properties = properties == null ? List.of() : properties;
        grantedSpells = grantedSpells == null ? List.of() : grantedSpells;
        storageLocation = storageLocation == null ? Dnd5eStorageLocation.EQUIPMENT : storageLocation;
        mechanics = mechanics == null ? Dnd5eItemMechanics.NONE : mechanics;
    }

    /** A catalogue item with no magic-item mechanics. */
    public Dnd5eItem(
            String key, String name, int quantity, String cost, String notes, boolean equipped, boolean attuned,
            boolean requiresAttunement, String catalogueSlug, Dnd5eItemKind itemKind, String typeLabel, String rarity,
            String attunementRequirement, Double weightLb, Double costGp, Dnd5eWeaponCategory weaponCategory,
            Dnd5eRangeCategory rangeCategory, Integer damageDiceCount, Integer damageDiceSides, String damageType,
            Integer versatileDamageDiceCount, Integer versatileDamageDiceSides, List<String> properties, boolean finesse,
            Integer normalRange, Integer longRange, Dnd5eArmorCategory armorCategory, Integer baseArmorClass,
            boolean stealthDisadvantage, Integer strengthRequirement, Integer weaponAttackBonus, Integer weaponDamageBonus,
            Integer armorClassBonus, Integer charges, String rechargeTrigger, String rechargeFormula,
            List<Dnd5eGrantedSpell> grantedSpells, int chargesUsed, Dnd5eStorageLocation storageLocation, String source) {
        this(key, name, quantity, cost, notes, equipped, attuned, requiresAttunement, catalogueSlug, itemKind, typeLabel,
                rarity, attunementRequirement, weightLb, costGp, weaponCategory, rangeCategory, damageDiceCount,
                damageDiceSides, damageType, versatileDamageDiceCount, versatileDamageDiceSides, properties, finesse,
                normalRange, longRange, armorCategory, baseArmorClass, stealthDisadvantage, strengthRequirement,
                weaponAttackBonus, weaponDamageBonus, armorClassBonus, charges, rechargeTrigger, rechargeFormula,
                grantedSpells, chargesUsed, storageLocation, source, Dnd5eItemMechanics.NONE);
    }

    /** The item with the player's name, notes, cost and weight in place of its own, and Silvered / Adamantine as properties. */
    Dnd5eItem customizedAs(Dnd5eItemCustomization customization) {
        List<String> customProperties = new ArrayList<>(properties);
        if (customization.silvered()) {
            customProperties.add("Silvered");
        }
        if (customization.adamantine()) {
            customProperties.add("Adamantine");
        }
        String customName = customization.name() != null ? customization.name() : name;
        String customNotes = customization.notes() != null ? customization.notes() : notes;
        Double customCostGp = customization.costOverride() != null ? customization.costOverride() : costGp;
        String customCost = customization.costOverride() != null ? formatGp(customization.costOverride()) : cost;
        Double customWeightLb = customization.weightOverride() != null ? customization.weightOverride() : weightLb;
        return new Dnd5eItem(key, customName, quantity, customCost, customNotes, equipped, attuned, requiresAttunement,
                catalogueSlug, itemKind, typeLabel, rarity, attunementRequirement, customWeightLb, customCostGp, weaponCategory,
                rangeCategory, damageDiceCount, damageDiceSides, damageType, versatileDamageDiceCount, versatileDamageDiceSides,
                List.copyOf(customProperties), finesse, normalRange, longRange, armorCategory, baseArmorClass,
                stealthDisadvantage, strengthRequirement, weaponAttackBonus, weaponDamageBonus, armorClassBonus, charges,
                rechargeTrigger, rechargeFormula, grantedSpells, chargesUsed, storageLocation, source, mechanics);
    }

    private static String formatGp(double gp) {
        return (gp == Math.rint(gp) ? String.valueOf((long) gp) : String.valueOf(gp)) + " gp";
    }

    /** Whether the item's benefits apply: equipped, and attuned too when it requires attunement. */
    public boolean active() {
        return equipped && (!requiresAttunement || attuned);
    }

    /** A freeform/homebrew item, unchanged since phase 8 — never carries catalogue-sourced weapon/armour/charge data. */
    public Dnd5eItem(
            String key, String name, int quantity, String cost, String notes,
            boolean equipped, boolean attuned, boolean requiresAttunement) {
        this(key, name, quantity, cost, notes, equipped, attuned, requiresAttunement, Dnd5eStorageLocation.EQUIPMENT);
    }

    /** Same freeform shape, with the caller choosing the storage location instead of always {@code EQUIPMENT}. */
    public Dnd5eItem(
            String key, String name, int quantity, String cost, String notes,
            boolean equipped, boolean attuned, boolean requiresAttunement, Dnd5eStorageLocation storageLocation) {
        this(key, name, quantity, cost, notes, equipped, attuned, requiresAttunement,
                null, Dnd5eItemKind.GEAR, null, null, null, null, null,
                null, null, null, null, null,
                null, null, List.of(), false, null, null,
                null, null, false, null,
                null, null, null,
                null, null, null, List.of(), 0, storageLocation, null);
    }
}
