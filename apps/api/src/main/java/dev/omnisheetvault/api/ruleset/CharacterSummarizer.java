package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * What a character card shows under the name, per game system, so the character list
 * itself stays generic (ui-design-system.md, "Rules that do not bend").
 */
public interface CharacterSummarizer {

    String systemId();

    /**
     * The card's facts in display order, the most important first. Exactly one of the two
     * documents is given: the sheet of a playable character, or the build of a draft.
     */
    List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup catalogue);
}
