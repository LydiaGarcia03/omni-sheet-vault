package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a build has granted so far while the planner walks it in order: ability scores,
 * proficiencies with the source that granted each, expertise, feats, optional
 * features and spells. Choice options are filtered against it, the way D&D Beyond filters a
 * pick by what the character already has.
 */
final class Dnd5eGrantState {

    private static final int DEFAULT_ABILITY_MAXIMUM = 20;

    enum Kind { SKILL, TOOL, LANGUAGE, WEAPON, ARMOR, SAVING_THROW }

    private final Map<String, Integer> abilityScores;
    private final Map<String, List<Dnd5eDerivation.SourcedAmount>> abilityContributions = new LinkedHashMap<>();
    private final Map<Kind, Map<String, String>> proficiencies = new EnumMap<>(Kind.class);
    private final Map<String, String> expertise = new LinkedHashMap<>();
    private final Set<String> feats = new HashSet<>();
    private final Set<String> optionalFeatures = new HashSet<>();
    private final Set<String> spells = new HashSet<>();
    private final Map<String, Integer> classLevels = new LinkedHashMap<>();
    private String speciesName;

    Dnd5eGrantState(Map<String, Integer> baseAbilityScores) {
        this.abilityScores = new HashMap<>(baseAbilityScores);
        baseAbilityScores.forEach((ability, score) ->
                abilityContributions.computeIfAbsent(ability, key -> new ArrayList<>()).add(new Dnd5eDerivation.SourcedAmount("Base", score)));
        for (Kind kind : Kind.values()) {
            proficiencies.put(kind, new LinkedHashMap<>());
        }
    }

    void grant(Kind kind, String key, String source) {
        proficiencies.get(kind).putIfAbsent(key, source);
    }

    boolean has(Kind kind, String key) {
        return proficiencies.get(kind).containsKey(key);
    }

    /** The source that granted {@code key}, or {@code null}. */
    String sourceOf(Kind kind, String key) {
        return proficiencies.get(kind).get(key);
    }

    Set<String> keys(Kind kind) {
        return proficiencies.get(kind).keySet();
    }

    /** Adds to a score; a second increase from the same source joins the first ("+1, +1" becomes one "+2"). */
    void increase(String ability, int amount, String source) {
        abilityScores.merge(ability, amount, Integer::sum);
        List<Dnd5eDerivation.SourcedAmount> contributions = abilityContributions.computeIfAbsent(ability, key -> new ArrayList<>());
        for (int index = 0; index < contributions.size(); index++) {
            Dnd5eDerivation.SourcedAmount existing = contributions.get(index);
            if (existing.source().equals(source)) {
                contributions.set(index, new Dnd5eDerivation.SourcedAmount(source, existing.amount() + amount));
                return;
            }
        }
        contributions.add(new Dnd5eDerivation.SourcedAmount(source, amount));
    }

    /** Replaces the calculated total, recorded as the difference so the contributions still add up. */
    void overrideScore(String ability, int score, String source) {
        increase(ability, score - score(ability), source);
    }

    Map<String, List<Dnd5eDerivation.SourcedAmount>> abilityContributions() {
        return abilityContributions;
    }

    Map<String, String> proficienciesWithSource(Kind kind) {
        return proficiencies.get(kind);
    }

    Map<String, String> expertiseWithSource() {
        return expertise;
    }

    int score(String ability) {
        return abilityScores.getOrDefault(ability, 0);
    }

    boolean isMaxed(String ability, Integer maximum) {
        return score(ability) >= (maximum == null ? DEFAULT_ABILITY_MAXIMUM : maximum);
    }

    void addExpertise(String key, String source) {
        expertise.putIfAbsent(key, source);
    }

    boolean hasExpertise(String key) {
        return expertise.containsKey(key);
    }

    void addFeat(String slug) {
        feats.add(slug);
    }

    boolean hasFeat(String slug) {
        return feats.contains(slug);
    }

    void addOptionalFeature(String slug) {
        optionalFeatures.add(slug);
    }

    boolean hasOptionalFeature(String slug) {
        return optionalFeatures.contains(slug);
    }

    /** Records a spell the character knows, by its lowercase name, for prerequisites and duplicate filtering. */
    void addSpell(String name) {
        spells.add(Dnd5eSpellLists.nameKey(name));
    }

    boolean knowsSpell(String name) {
        return spells.contains(Dnd5eSpellLists.nameKey(name));
    }

    boolean canCastSpells() {
        return !spells.isEmpty();
    }

    void setClassLevel(String classSlug, int level) {
        classLevels.put(classSlug, level);
    }

    int classLevel(String classSlug) {
        return classLevels.getOrDefault(classSlug, 0);
    }

    int characterLevel() {
        return classLevels.values().stream().mapToInt(Integer::intValue).sum();
    }

    void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    String speciesName() {
        return speciesName;
    }
}
