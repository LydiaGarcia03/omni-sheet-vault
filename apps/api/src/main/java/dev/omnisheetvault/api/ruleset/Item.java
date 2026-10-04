package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * One inventory item — see VitalsZone. Nothing here is calculated; it is a plain
 * pass-through of stored data, same treatment as a spell. {@code equipped} and
 * {@code attuned} are real state; this phase derives neither armor class nor the
 * Actions tab's attacks from them yet (see {@code Dnd5eItem}'s doc comment for the
 * slice plan). {@code requiresAttunement} gates whether {@code attuned} can ever be
 * true.
 *
 * <p>{@code catalogueSlug} through {@code grantedSpells} are
 * systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 2 — a 1:1 copy of the real
 * 5etools item data, same "copy once" treatment {@link Spell} already gives a
 * learned spell. {@code itemKind}/{@code weaponCategory}/{@code rangeCategory}/
 * {@code armorCategory} are plain strings, not shared enums: which values exist is a
 * per-system concept (adr-0003), same reasoning as {@link CustomAction}'s own
 * closed-set fields. Used both for display ({@code VitalsZone.items}) and as
 * {@link SheetMutator#addCatalogueItem}'s input, same double duty {@link Spell}
 * already has for {@link SheetMutator#learnSpell}. A freeform item (added through
 * {@link SheetMutator#addItem}, still primitive-shaped — see that method's own
 * unchanged signature) never produces one of these; it goes straight into
 * {@code Dnd5eItem}'s own freeform constructor instead.
 *
 * <p>{@code storageLocation} is which of the system's own storage buckets this item
 * sits in (e.g. D&D 5e's {@code EQUIPMENT}/{@code BACKPACK}/{@code BAG_OF_HOLDING}/
 * {@code OTHER_POSSESSIONS}) — a plain string, not a shared enum, same per-system
 * reasoning as {@code itemKind}.
 *
 * <p>{@code source} is a pre-formatted display string (e.g. {@code "Player's Handbook,
 * p. 149"}), copied once from the catalogue entry's own {@code sourceBook}/{@code
 * sourcePage} the same "copy once" way every other catalogue-sourced field here is —
 * {@code null} for a freeform item, since this app invents no book/page for one, same
 * reasoning {@code cost}'s own doc comment gives.
 */
public record Item(
        String key,
        String name,
        int quantity,
        String cost,
        String notes,
        boolean equipped,
        boolean attuned,
        boolean requiresAttunement,
        String catalogueSlug,
        String itemKind,
        String typeLabel,
        String rarity,
        String attunementRequirement,
        Double weightLb,
        Double costGp,
        String weaponCategory,
        String rangeCategory,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        Integer versatileDamageDiceCount,
        Integer versatileDamageDiceSides,
        List<String> properties,
        boolean finesse,
        Integer normalRange,
        Integer longRange,
        String armorCategory,
        Integer baseArmorClass,
        boolean stealthDisadvantage,
        Integer strengthRequirement,
        Integer weaponAttackBonus,
        Integer weaponDamageBonus,
        Integer armorClassBonus,
        Integer charges,
        String rechargeTrigger,
        String rechargeFormula,
        List<ItemGrantedSpell> grantedSpells,
        int chargesUsed,
        String storageLocation,
        String source) {

    /** A freeform/homebrew item — see {@code Dnd5eItem}'s own matching constructor. */
    public Item(
            String key, String name, int quantity, String cost, String notes,
            boolean equipped, boolean attuned, boolean requiresAttunement) {
        this(key, name, quantity, cost, notes, equipped, attuned, requiresAttunement,
                null, "GEAR", null, null, null, null, null,
                null, null, null, null, null,
                null, null, List.of(), false, null, null,
                null, null, false, null,
                null, null, null,
                null, null, null, List.of(), 0, "EQUIPMENT", null);
    }
}
