package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ExtraStatEntry;

public record ExtraStatEntryResponse(String name, String description) {

    static ExtraStatEntryResponse from(ExtraStatEntry entry) {
        return new ExtraStatEntryResponse(entry.name(), entry.description());
    }
}
