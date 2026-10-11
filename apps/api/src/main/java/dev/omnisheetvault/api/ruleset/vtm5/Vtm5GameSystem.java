package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.ruleset.GameSystem;
import org.springframework.stereotype.Component;

@Component
public class Vtm5GameSystem implements GameSystem {

    public static final String SYSTEM_ID = "vtm-v5";

    @Override
    public String systemId() {
        return SYSTEM_ID;
    }

    @Override
    public String displayName() {
        return "Vampire: The Masquerade 5e";
    }
}
