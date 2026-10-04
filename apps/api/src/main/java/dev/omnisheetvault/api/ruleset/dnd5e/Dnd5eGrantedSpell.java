package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * One entry of an item's own {@code attachedSpells.charges} mapping — the Wand of
 * Fireballs mechanic (systems/dnd-5e/features/inventory-equipment-mechanics.md, slice 7). Carries
 * the referenced spell's own full display/cast data, resolved once from the
 * catalogue at the moment the item was added to the sheet ({@code
 * CharacterSheetService#toItem}) — the same "copy once, never re-read the
 * catalogue" treatment every other catalogue-sourced fact in this feature already
 * gets, since {@link Dnd5eSheetCalculator} has no catalogue access of its own to
 * resolve {@code spellSlug} against later. {@code fixedSaveDc} is the item's own
 * flat save DC (e.g. "save 15"), independent of the wielder's own spellcasting
 * ability — {@code null} for a granted spell with no save (an attack-roll spell
 * instead). At the moment this is added to the sheet ({@code
 * Dnd5eSheetMutator#addCatalogueItem}), it becomes a real {@link Dnd5eSpell} entry
 * with {@code grantedByItemKey}/{@code chargeCost}/{@code fixedSaveDc} set — see
 * that record's own doc comment.
 */
public record Dnd5eGrantedSpell(
        String spellSlug,
        String spellName,
        int chargeCost,
        Integer fixedSaveDc,
        int level,
        String school,
        String castingTime,
        String range,
        boolean concentration,
        boolean ritual,
        boolean attackRoll,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        String notes,
        String effectSummary,
        String description,
        String saveAbility,
        String components,
        String materialComponent,
        String duration) {
}
