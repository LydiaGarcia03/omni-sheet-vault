package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Extra;

public record ExtraResponse(
        String key,
        String name,
        String category,
        int armorClass,
        int maxHitPoints,
        int currentHitPoints,
        int temporaryHitPoints,
        int speed,
        ExtraStatBlockResponse statBlock) {

    static ExtraResponse from(Extra extra) {
        return new ExtraResponse(
                extra.key(), extra.name(), extra.category(), extra.armorClass(), extra.maxHitPoints(),
                extra.currentHitPoints(), extra.temporaryHitPoints(), extra.speed(),
                ExtraStatBlockResponse.from(extra.statBlock()));
    }
}
