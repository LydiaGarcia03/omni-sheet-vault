package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import java.util.List;

/**
 * What a magic item does to its wearer while it is active (equipped, and attuned when
 * it requires attunement): its {@code modifiers} (labelled with the item's name) and
 * the damage and condition defenses it grants. Copied once from the catalogue's
 * {@code data.mechanics} when the item is added.
 */
public record Dnd5eItemMechanics(
        List<@Valid Dnd5eModifier> modifiers,
        List<String> damageResistances,
        List<String> damageImmunities,
        List<String> damageVulnerabilities,
        List<String> conditionImmunities) {

    public static final Dnd5eItemMechanics NONE = new Dnd5eItemMechanics(List.of(), List.of(), List.of(), List.of(), List.of());

    public Dnd5eItemMechanics {
        modifiers = modifiers == null ? List.of() : modifiers;
        damageResistances = damageResistances == null ? List.of() : damageResistances;
        damageImmunities = damageImmunities == null ? List.of() : damageImmunities;
        damageVulnerabilities = damageVulnerabilities == null ? List.of() : damageVulnerabilities;
        conditionImmunities = conditionImmunities == null ? List.of() : conditionImmunities;
    }
}
