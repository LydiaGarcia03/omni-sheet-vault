package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellAdjustments;
import dev.omnisheetvault.api.ruleset.SpellUsage;

public record SpellResponse(
        String key,
        String name,
        String className,
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
        boolean prepared,
        boolean alwaysPrepared,
        String description,
        String saveAbility,
        String components,
        String materialComponent,
        String duration,
        String higherLevelsDescription,
        Integer higherLevelsDamageDiceCount,
        Integer higherLevelsDamageDiceSides,
        String grantedByItemKey,
        Integer chargeCost,
        Integer fixedSaveDc,
        SpellAdjustmentsResponse adjustments,
        SpellUsageResponse usage) {

    public record SpellAdjustmentsResponse(
            Integer attackOverride, int attackBonus, int damageBonus, Integer saveDcOverride, int saveDcBonus, boolean displayAsAttack) {
    }

    /** How a feature-granted spell is cast without a slot; absent for a spell cast with slots. */
    public record SpellUsageResponse(String mode, int maxUses, int usedUses, String recharge, Integer castLevel, boolean selfOnly) {

        static SpellUsageResponse from(SpellUsage usage) {
            return usage == null ? null : new SpellUsageResponse(usage.mode(), usage.maxUses(), usage.usedUses(),
                    usage.recharge(), usage.castLevel(), usage.selfOnly());
        }
    }

    static SpellResponse from(Spell spell) {
        SpellAdjustments adjustments = spell.adjustments();
        return new SpellResponse(
                spell.key(), spell.name(), spell.className(), spell.level(), spell.school(), spell.castingTime(),
                spell.range(), spell.concentration(), spell.ritual(), spell.attackRoll(), spell.damageDiceCount(),
                spell.damageDiceSides(), spell.damageType(), spell.notes(), spell.effectSummary(), spell.prepared(),
                spell.alwaysPrepared(), spell.description(), spell.saveAbility(), spell.components(),
                spell.materialComponent(), spell.duration(), spell.higherLevelsDescription(),
                spell.higherLevelsDamageDiceCount(), spell.higherLevelsDamageDiceSides(),
                spell.grantedByItemKey(), spell.chargeCost(), spell.fixedSaveDc(),
                new SpellAdjustmentsResponse(adjustments.attackOverride(), adjustments.attackBonus(), adjustments.damageBonus(),
                        adjustments.saveDcOverride(), adjustments.saveDcBonus(), adjustments.displayAsAttack()),
                SpellUsageResponse.from(spell.usage()));
    }
}
