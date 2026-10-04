package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * A decision a character build offers — never persisted; a {@link CharacterCreationFlow}
 * produces it from a build and the catalogue. {@code type} classifies it (e.g.
 * {@code SKILL}, {@code EXPERTISE}, {@code FEAT}, {@code SUBCLASS}); {@code parentChoiceId}
 * links a sub-choice to the choice whose answer created it (a feat's own picks under
 * the ASI-or-feat choice). {@code sourceLabel} names what asks for it ("Fighter 1");
 * {@code selected} echoes the build's current answer. Ids starting with {@code build.}
 * are answered by a field of the build itself, every other id in the build's choices.
 * {@code placement} files it under a class and level, or is null.
 */
public record CreationChoice(
        String id,
        String type,
        String parentChoiceId,
        String prompt,
        String sourceLabel,
        int count,
        boolean optional,
        List<CreationChoiceOption> options,
        List<String> selected,
        ChoicePlacement placement) {

    public CreationChoice(String id, String type, String parentChoiceId, String prompt, String sourceLabel, int count,
            boolean optional, List<CreationChoiceOption> options, List<String> selected) {
        this(id, type, parentChoiceId, prompt, sourceLabel, count, optional, options, selected, null);
    }

    /** Not optional and not fully answered — D&D Beyond's own "todo" rule. */
    public boolean isPending() {
        return !optional && selected.size() < count;
    }
}
