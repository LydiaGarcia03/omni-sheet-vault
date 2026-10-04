package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.GameSystem;
import org.springframework.stereotype.Component;

@Component
public class Dnd5eGameSystem implements GameSystem {

    static final String SYSTEM_ID = "dnd-5e";

    @Override
    public String systemId() {
        return SYSTEM_ID;
    }

    @Override
    public String displayName() {
        return "D&D 5e";
    }
}
