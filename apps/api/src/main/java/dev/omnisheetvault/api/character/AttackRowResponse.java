package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.AttackRow;

public record AttackRowResponse(
        String name,
        String range,
        CalculatedValueResponse toHit,
        int damageDiceCount,
        int damageDiceSides,
        int damageModifier,
        String damageType,
        String category,
        String notes,
        String actionType,
        Integer versatileDiceCount,
        Integer versatileDiceSides) {

    static AttackRowResponse from(AttackRow attack) {
        return new AttackRowResponse(
                attack.name(), attack.range(), CalculatedValueResponse.from(attack.toHit()),
                attack.damageDiceCount(), attack.damageDiceSides(), attack.damageModifier(),
                attack.damageType(), attack.category(), attack.notes(), attack.actionType(),
                attack.versatileDiceCount(), attack.versatileDiceSides());
    }
}
