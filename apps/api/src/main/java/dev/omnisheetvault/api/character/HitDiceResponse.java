package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.HitDice;
import java.util.List;

record HitDiceResponse(int dieSize, int max, int used, List<PoolResponse> pools, int longRestRecoveryMax) {

    record PoolResponse(int dieSize, int max, int used, List<String> classNames) {
    }

    static HitDiceResponse from(HitDice hitDice) {
        return new HitDiceResponse(hitDice.dieSize(), hitDice.max(), hitDice.used(),
                hitDice.pools().stream()
                        .map(pool -> new PoolResponse(pool.dieSize(), pool.max(), pool.used(), pool.classNames()))
                        .toList(),
                hitDice.longRestRecoveryMax());
    }
}