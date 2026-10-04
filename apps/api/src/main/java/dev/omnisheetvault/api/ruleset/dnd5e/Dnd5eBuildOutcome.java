package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * What the planner collected while walking a build, beyond the grant state: the chosen
 * species mechanics, background, classes with their subclasses, features, spells,
 * damage picks and starting equipment. {@link Dnd5eBuildMaterializer} turns it into sheet fields.
 */
final class Dnd5eBuildOutcome {

    record ClassOutcome(CatalogueRecord playerClass, Dnd5eBuildClass buildClass, CatalogueRecord subclass) {
    }

    /** {@code mechanics} is the catalogue node carrying {@code modifiers}, {@code uses} and {@code action}; {@code classSlug} names the class the feature belongs to. */
    record FeatureOutcome(String key, String name, Dnd5eFeatureTraitCategory category, String source, String description,
            JsonNode mechanics, String classSlug) {
    }

    /** A starting item: a catalogue {@code itemSlug}, or a free-text {@code customName} for a trinket with no catalogue entry. */
    record StartingItem(String itemSlug, String customName, int quantity, String displayName) {
    }

    /**
     * A spell the build grants. {@code owner} is the class that casts it, or the granting
     * source's label ("High Elf"); {@code usage} is a limit such as "1/day", null when
     * the spell is cast normally. {@code slotless} is the same limit as data, for a spell
     * cast without a slot; null when it is cast with slots.
     */
    record SpellOutcome(String spellSlug, String owner, boolean prepared, boolean alwaysPrepared, String usage,
            Dnd5eSpellUsage slotless) {
    }

    /** A non-class source that casts its own granted spells with {@code abilityKey}. */
    record GrantedCaster(String owner, String abilityKey) {
    }

    private String speciesLabel;
    private final List<SpellOutcome> spells = new ArrayList<>();
    private final List<GrantedCaster> grantedCasters = new ArrayList<>();
    private JsonNode speciesMechanics;
    private final List<String> resistancePicks = new ArrayList<>();
    private CatalogueRecord background;
    private final List<ClassOutcome> classes = new ArrayList<>();
    private final List<FeatureOutcome> features = new ArrayList<>();
    private final Map<String, List<String>> featureChoices = new HashMap<>();
    private final List<StartingItem> startingItems = new ArrayList<>();
    private int startingCopper;

    void species(String label, JsonNode mechanics) {
        this.speciesLabel = label;
        this.speciesMechanics = mechanics;
    }

    void addResistancePick(String damageType) {
        resistancePicks.add(damageType);
    }

    void background(CatalogueRecord background) {
        this.background = background;
    }

    void addClass(CatalogueRecord playerClass, Dnd5eBuildClass buildClass, CatalogueRecord subclass) {
        classes.add(new ClassOutcome(playerClass, buildClass, subclass));
    }

    void addFeature(String key, String name, Dnd5eFeatureTraitCategory category, String source, String description) {
        addFeature(key, name, category, source, description, null, null);
    }

    void addFeature(String key, String name, Dnd5eFeatureTraitCategory category, String source, String description,
            JsonNode mechanics, String classSlug) {
        features.add(new FeatureOutcome(key, name, category, source, description == null ? "" : description, mechanics, classSlug));
    }

    /** What the player chose for a feature (the subclass under "Martial Archetype"), shown beneath it. */
    void addFeatureChoice(String featureKey, String label) {
        featureChoices.computeIfAbsent(featureKey, key -> new ArrayList<>()).add(label);
    }

    List<String> featureChoices(String featureKey) {
        return featureChoices.getOrDefault(featureKey, List.of());
    }

    void addStartingItem(String itemSlug, String customName, int quantity, String displayName) {
        startingItems.add(new StartingItem(itemSlug, customName, quantity, displayName));
    }

    void addStartingCopper(int copper) {
        startingCopper += copper;
    }

    void addSpell(String spellSlug, String owner, boolean prepared, boolean alwaysPrepared, String usage) {
        addSpell(spellSlug, owner, prepared, alwaysPrepared, usage, null);
    }

    void addSpell(String spellSlug, String owner, boolean prepared, boolean alwaysPrepared, String usage, Dnd5eSpellUsage slotless) {
        spells.add(new SpellOutcome(spellSlug, owner, prepared, alwaysPrepared, usage, slotless));
    }

    void addGrantedCaster(String owner, String abilityKey) {
        if (grantedCasters.stream().noneMatch(caster -> caster.owner().equals(owner))) {
            grantedCasters.add(new GrantedCaster(owner, abilityKey));
        }
    }

    List<SpellOutcome> spells() {
        return spells;
    }

    List<GrantedCaster> grantedCasters() {
        return grantedCasters;
    }

    String speciesLabel() {
        return speciesLabel;
    }

    JsonNode speciesMechanics() {
        return speciesMechanics;
    }

    List<String> resistancePicks() {
        return resistancePicks;
    }

    CatalogueRecord background() {
        return background;
    }

    List<ClassOutcome> classes() {
        return classes;
    }

    List<FeatureOutcome> features() {
        return features;
    }

    List<StartingItem> startingItems() {
        return startingItems;
    }

    int startingCopper() {
        return startingCopper;
    }
}
