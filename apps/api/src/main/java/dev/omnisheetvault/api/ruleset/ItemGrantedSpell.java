package dev.omnisheetvault.api.ruleset;

/**
 * One entry of an item's charge-cost-to-spell mapping — the Wand of Fireballs
 * mechanic. Mirrors {@code Dnd5eGrantedSpell}'s own richer shape (see its doc
 * comment): the referenced spell's full display/cast data, resolved once at
 * add-time, since nothing downstream re-reads the catalogue for it.
 */
public record ItemGrantedSpell(
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
