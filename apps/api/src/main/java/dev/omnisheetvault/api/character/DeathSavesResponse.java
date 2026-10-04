package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.DeathSaves;

record DeathSavesResponse(int successes, int failures, boolean dying, boolean stable, boolean dead) {

    static DeathSavesResponse from(DeathSaves deathSaves) {
        DeathSaves saves = deathSaves == null ? DeathSaves.NOT_DYING : deathSaves;
        return new DeathSavesResponse(saves.successes(), saves.failures(), saves.dying(), saves.stable(), saves.dead());
    }
}
