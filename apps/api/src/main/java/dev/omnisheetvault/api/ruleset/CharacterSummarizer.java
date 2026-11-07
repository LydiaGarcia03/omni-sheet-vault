package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * What a character card shows under the name, per game system, so the character list
 * itself stays generic (ui-design-system.md, "Rules that do not bend").
 */
public interface CharacterSummarizer {

    /** A card has four lines at most: the name, the draft tag, and two facts. */
    int MAX_FACTS = 2;

    String systemId();

    /**
     * The card's facts in display order, the most important first; the card shows at most
     * {@link #MAX_FACTS}, one line each. Exactly one of the two documents is given: the sheet
     * of a playable character, or the build of a draft.
     */
    List<SummaryFact> summarize(String sheetJson, String draftBuildJson, CatalogueLookup catalogue);
}
