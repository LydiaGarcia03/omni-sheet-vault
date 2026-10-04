package dev.omnisheetvault.api.ruleset;

import java.util.Optional;

/** One game system's character creation: the ordered choices a build offers and the problems in its answers. */
public interface CharacterCreationFlow {

    String systemId();

    /** The build document a brand-new draft starts from: nothing chosen yet, every structural question pending. */
    String emptyDraft();

    /**
     * {@code buildJson} is the system's own build document, possibly a draft still missing
     * its structure; malformed input fails with {@link InvalidBuildException}.
     */
    BuildPlan plan(String buildJson, CatalogueLookup catalogue);

    /** The build's values so far, pending choices left out, for the creation flow's live summary. */
    CreationPreview preview(String buildJson, CatalogueLookup catalogue);

    /**
     * Regenerates the build-derived fields of {@code currentSheetJson} ({@code null} for a new
     * character) from a fully answered build, keeping play state; refuses when the plan
     * still has pending choices or problems.
     */
    MaterializedSheet materialize(String currentSheetJson, String buildJson, CatalogueLookup catalogue);

    /** The build a character's sheet was materialized from; empty for a sheet written by hand. */
    Optional<String> buildOf(String sheetJson);

    /**
     * {@code buildJson} with one more level in {@code classSlug}: a class the build has, or a new
     * one at its first level.
     *
     * @throws LevelUpNotAllowedException at the top level, or for a class the catalogue doesn't have
     */
    LevelUp withLevelUp(String buildJson, String classSlug, CatalogueLookup catalogue);
}
