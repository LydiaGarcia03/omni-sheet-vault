package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Coins;

public record CoinsResponse(int copper, int silver, int electrum, int gold, int platinum) {

    static CoinsResponse from(Coins coins) {
        return new CoinsResponse(coins.copper(), coins.silver(), coins.electrum(), coins.gold(), coins.platinum());
    }
}
