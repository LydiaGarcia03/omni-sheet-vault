package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.RollModeInfo;
import java.util.List;

public record RollModeResponse(String mode, List<String> advantageSources, List<String> disadvantageSources) {

    static RollModeResponse from(RollModeInfo info) {
        return new RollModeResponse(info.mode(), info.advantageSources(), info.disadvantageSources());
    }
}
