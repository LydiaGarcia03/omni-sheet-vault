package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ActiveEffectInfo;
import java.util.List;

public record ActiveEffectResponse(
        String key, String name, Integer castAtLevel, boolean concentration, List<String> endsOnRests, String durationText, boolean onSelf) {

    static ActiveEffectResponse from(ActiveEffectInfo info) {
        return new ActiveEffectResponse(
                info.key(), info.name(), info.castAtLevel(), info.concentration(), info.endsOnRests(), info.durationText(), info.onSelf());
    }
}
