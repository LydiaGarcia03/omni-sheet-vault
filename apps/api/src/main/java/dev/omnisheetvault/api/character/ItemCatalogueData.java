package dev.omnisheetvault.api.character;

import java.util.List;

/**
 * The mechanical shape of an {@code ITEM}-kind catalogue entry's {@code data} field —
 * see {@code ItemConverter} and systems/dnd-5e/features/inventory-equipment-mechanics.md's field
 * mapping table for where every one of these comes from. {@code itemKind}/
 * {@code weaponCategory}/{@code rangeCategory}/{@code armorCategory} are plain
 * strings here too (same reasoning as {@code SpellCatalogueData}'s own fields):
 * {@link CharacterSheetService#toItem} passes them straight through into the generic
 * {@code Item} shape, and the system-specific mutator is the one place that parses
 * them into real enums.
 */
record ItemCatalogueData(
        String itemKind,
        String typeLabel,
        String rarity,
        boolean requiresAttunement,
        String attunementRequirement,
        Double weightLb,
        Double costGp,
        String weaponCategory,
        String attackType,
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
        List<GrantedSpellData> grantedSpells) {

    /**
     * One entry of {@code ItemConverter}'s own {@code grantedSpells} — a charge cost
     * mapped to the spell it casts. {@code fixedSaveDc} is the item's own flat save DC
     * (e.g. Wand of Fireballs' "save 15"), independent of the wielder's own
     * spellcasting ability — {@code null} for a granted spell with no save (an
     * attack-roll spell instead).
     */
    record GrantedSpellData(String spellSlug, int chargeCost, Integer fixedSaveDc) {
    }
}
