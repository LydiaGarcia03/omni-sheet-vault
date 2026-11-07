package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.GameSystem;
import org.springframework.stereotype.Component;

@Component
public class DaggerheartGameSystem implements GameSystem {

    public static final String SYSTEM_ID = "daggerheart";

    @Override
    public String systemId() {
        return SYSTEM_ID;
    }

    @Override
    public String displayName() {
        return "Daggerheart";
    }
}
