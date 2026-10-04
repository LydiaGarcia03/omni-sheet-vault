package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.SpellSlotLevel;

record SpellSlotLevelResponse(int level, int maxSlots, int usedSlots, boolean pact, String className) {

    static SpellSlotLevelResponse from(SpellSlotLevel spellSlot) {
        return new SpellSlotLevelResponse(spellSlot.level(), spellSlot.maxSlots(), spellSlot.usedSlots(), spellSlot.pact(),
                spellSlot.className());
    }
}
