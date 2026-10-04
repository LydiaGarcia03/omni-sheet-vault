package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * What the sheet forces on one kind of d20 roll: the sources granting advantage and
 * those imposing disadvantage. Both present cancel out, as the PHB rules; the sources
 * are still listed so the sheet can say why.
 */
public record RollModeInfo(List<String> advantageSources, List<String> disadvantageSources) {

    public String mode() {
        boolean advantage = !advantageSources.isEmpty();
        boolean disadvantage = !disadvantageSources.isEmpty();
        if (advantage == disadvantage) {
            return "NORMAL";
        }
        return advantage ? "ADVANTAGE" : "DISADVANTAGE";
    }
}
