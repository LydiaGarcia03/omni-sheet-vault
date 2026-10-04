package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a character is built from: catalogue references (species, background, classes)
 * plus every choice made along the way. The source of truth for the sheet's
 * build-derived fields, which the build resolver regenerates whenever this changes.
 * {@code subspeciesName} {@code null} means not chosen yet, and {@code ""} means the
 * species' unnamed default subspecies (e.g. the standard Human); {@code classes} lists
 * the starting class first. {@code baseAbilityScores} are
 * before any species or feat increase, keyed by the sheet's ability names.
 * {@code rolledHitPoints} holds one die result per level after the first (null for a
 * level not rolled yet), used only when {@code hitPointMethod} is {@code ROLLED}.
 * {@code equippedStartingItems} names the starting-item lines that begin worn or wielded,
 * by line key (item slug and occurrence, e.g. {@code dagger#1}).
 * <p>
 * A draft build may leave the structure unanswered (no species, background, classes
 * or ability scores yet); the {@link Complete} group holds the constraints a build
 * must meet before it can be materialized. Missing lists and maps read as empty.
 */
public record Dnd5eCharacterBuild(
        @NotBlank(groups = Complete.class) String speciesSlug,
        String subspeciesName,
        String speciesVariantName,
        @NotBlank(groups = Complete.class) String backgroundSlug,
        @NotEmpty(groups = Complete.class) @Valid List<Dnd5eBuildClass> classes,
        @NotNull(groups = Complete.class) Dnd5eAbilityScoreMethod abilityScoreMethod,
        Map<String, @Min(1) @Max(30) Integer> baseAbilityScores,
        Dnd5eHitPointMethod hitPointMethod,
        List<@Min(1) Integer> rolledHitPoints,
        @Valid List<Dnd5eBuildChoice> choices,
        Map<String, @Valid Dnd5eAbilityAdjustment> abilityScoreAdjustments,
        Dnd5ePreferences preferences,
        List<@NotBlank String> equippedStartingItems) {

    /** Constraints only a finished build must meet. */
    public interface Complete {
    }

    private static final Set<String> ABILITIES =
            Set.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
    private static final int MAX_CHARACTER_LEVEL = 20;

    public Dnd5eCharacterBuild {
        classes = classes == null ? List.of() : classes;
        baseAbilityScores = baseAbilityScores == null ? Map.of() : baseAbilityScores;
        hitPointMethod = hitPointMethod == null ? Dnd5eHitPointMethod.FIXED : hitPointMethod;
        rolledHitPoints = rolledHitPoints == null ? List.of() : rolledHitPoints;
        choices = choices == null ? List.of() : Dnd5eLegacyChoiceIds.pooled(choices);
        abilityScoreAdjustments = abilityScoreAdjustments == null ? Map.of() : abilityScoreAdjustments;
        preferences = preferences == null ? Dnd5ePreferences.defaults() : preferences;
        equippedStartingItems = equippedStartingItems == null ? List.of() : equippedStartingItems;
    }

    /** A build whose starting items all begin unequipped. */
    public Dnd5eCharacterBuild(
            String speciesSlug, String subspeciesName, String speciesVariantName, String backgroundSlug,
            List<Dnd5eBuildClass> classes, Dnd5eAbilityScoreMethod abilityScoreMethod, Map<String, Integer> baseAbilityScores,
            Dnd5eHitPointMethod hitPointMethod, List<Integer> rolledHitPoints, List<Dnd5eBuildChoice> choices,
            Map<String, Dnd5eAbilityAdjustment> abilityScoreAdjustments, Dnd5ePreferences preferences) {
        this(speciesSlug, subspeciesName, speciesVariantName, backgroundSlug, classes, abilityScoreMethod, baseAbilityScores,
                hitPointMethod, rolledHitPoints, choices, abilityScoreAdjustments, preferences, null);
    }

    /** A build with default preferences. */
    public Dnd5eCharacterBuild(
            String speciesSlug, String subspeciesName, String speciesVariantName, String backgroundSlug,
            List<Dnd5eBuildClass> classes, Dnd5eAbilityScoreMethod abilityScoreMethod, Map<String, Integer> baseAbilityScores,
            Dnd5eHitPointMethod hitPointMethod, List<Integer> rolledHitPoints, List<Dnd5eBuildChoice> choices,
            Map<String, Dnd5eAbilityAdjustment> abilityScoreAdjustments) {
        this(speciesSlug, subspeciesName, speciesVariantName, backgroundSlug, classes, abilityScoreMethod, baseAbilityScores,
                hitPointMethod, rolledHitPoints, choices, abilityScoreAdjustments, null);
    }

    /** A build without manual ability corrections, under default preferences. */
    public Dnd5eCharacterBuild(
            String speciesSlug, String subspeciesName, String speciesVariantName, String backgroundSlug,
            List<Dnd5eBuildClass> classes, Dnd5eAbilityScoreMethod abilityScoreMethod, Map<String, Integer> baseAbilityScores,
            Dnd5eHitPointMethod hitPointMethod, List<Integer> rolledHitPoints, List<Dnd5eBuildChoice> choices) {
        this(speciesSlug, subspeciesName, speciesVariantName, backgroundSlug, classes, abilityScoreMethod, baseAbilityScores,
                hitPointMethod, rolledHitPoints, choices, Map.of(), null);
    }

    public int characterLevel() {
        return classes.stream().mapToInt(Dnd5eBuildClass::level).sum();
    }

    /** True once all six base scores are set. */
    public boolean hasEveryAbilityScore() {
        return baseAbilityScores.keySet().equals(ABILITIES);
    }

    @AssertTrue(message = "total class levels must be between 1 and 20")
    boolean isCharacterLevelInRange() {
        return characterLevel() <= MAX_CHARACTER_LEVEL;
    }

    @AssertTrue(message = "base ability scores may only name the six abilities")
    boolean isEveryScoredNameAnAbility() {
        return ABILITIES.containsAll(baseAbilityScores.keySet());
    }

    @AssertTrue(message = "ability score adjustments may only name the six abilities")
    boolean isEveryAdjustedNameAnAbility() {
        return ABILITIES.containsAll(abilityScoreAdjustments.keySet());
    }

    @AssertTrue(message = "base ability scores must name exactly the six abilities", groups = Complete.class)
    boolean isEveryAbilityScored() {
        return hasEveryAbilityScore();
    }

    @AssertTrue(message = "each class may appear only once")
    boolean isEachClassListedOnce() {
        return classes == null || classes.stream().map(Dnd5eBuildClass::classSlug).distinct().count() == classes.size();
    }

    @AssertTrue(message = "choice ids must be unique")
    boolean isEachChoiceAnsweredOnce() {
        return choices == null || choices.stream().map(Dnd5eBuildChoice::id).distinct().count() == choices.size();
    }
}
