package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * The result of turning a build into a sheet. {@code sheetJson} is {@code null} when the
 * build isn't ready — the {@code plan} still has pending choices or problems.
 * {@code startingItems} and {@code startingCopper} are granted separately by the caller,
 * through the catalogue item path every added item already goes through.
 */
public record MaterializedSheet(String sheetJson, List<StartingItem> startingItems, int startingCopper, BuildPlan plan) {

    public boolean isMaterialized() {
        return sheetJson != null;
    }
}
