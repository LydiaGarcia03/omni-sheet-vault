package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.ChoicePlacement;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Walks one build in a fixed order — species, background, then each class level by
 * level — and produces every choice it offers, applying answered choices to a running
 * {@link Dnd5eGrantState} so later options are filtered by what earlier ones granted.
 * Structural questions (subspecies, variant, subclass, rolled hit points) are asked
 * before anything that depends on them.
 */
final class Dnd5eBuildPlanner {

    private static final List<Integer> STANDARD_ARRAY = List.of(8, 10, 12, 13, 14, 15);
    private static final Map<Integer, Integer> POINT_BUY_COSTS = Map.of(8, 0, 9, 1, 10, 2, 11, 3, 12, 4, 13, 5, 14, 7, 15, 9);
    private static final int POINT_BUY_BUDGET = 27;
    private static final int MANUAL_MINIMUM = 1;
    private static final int MANUAL_MAXIMUM = 30;
    private static final int ABILITY_ROLL_DICE = 4;
    private static final int ABILITY_ROLL_SIDES = 6;
    private static final int ABILITY_ROLL_KEPT = 3;
    private static final Pattern GOLD_DICE = Pattern.compile("(\\d+)d(\\d+)(?:\\s*[×x*]\\s*(\\d+))?");
    private static final List<String> ABILITIES =
            List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
    private static final Set<String> SPECIES_SCALARS = Set.of("size", "speed");
    private static final Set<String> SPECIES_DESCRIPTIVE_FIELDS =
            Set.of("name", "sourceBook", "sourcePage", "description", "overwrite", "variants", "subspecies");
    private static final Map<String, String> EQUIPMENT_TYPE_LABELS = Map.ofEntries(
            Map.entry("weaponSimple", "simple weapon"), Map.entry("weaponSimpleMelee", "simple melee weapon"),
            Map.entry("weaponMartial", "martial weapon"), Map.entry("weaponMartialMelee", "martial melee weapon"),
            Map.entry("focusSpellcastingArcane", "arcane focus"), Map.entry("focusSpellcastingDruidic", "druidic focus"),
            Map.entry("focusSpellcastingHoly", "holy symbol"), Map.entry("instrumentMusical", "musical instrument"),
            Map.entry("toolArtisan", "artisan's tools"), Map.entry("setGaming", "gaming set"));

    private final Dnd5eCharacterBuild build;
    private final CatalogueLookup catalogue;
    private final Dnd5eChoiceOptions options;
    private final Map<String, List<String>> answers = new HashMap<>();
    private final List<CreationChoice> choices = new ArrayList<>();
    private final List<String> problems = new ArrayList<>();
    private final Dnd5eGrantState state;
    private final Dnd5eBuildOutcome outcome = new Dnd5eBuildOutcome();
    private final Dnd5eSpellPlanner spells;
    private BuildPlan plan;
    /** The background's name, which is the source of every grant it makes. */
    private String backgroundSource;
    /** The class and level being planned, stamped on every choice offered meanwhile; null outside a class. */
    private ChoicePlacement placement;

    Dnd5eBuildPlanner(Dnd5eCharacterBuild build, CatalogueLookup catalogue) {
        this.build = build;
        this.catalogue = new Dnd5eSourceFilteredCatalogue(catalogue, build.preferences());
        this.options = new Dnd5eChoiceOptions(this.catalogue, build.preferences().optionalClassFeatures());
        this.state = new Dnd5eGrantState(build.baseAbilityScores());
        this.spells = new Dnd5eSpellPlanner(this.catalogue, state, outcome,
                (id, type, parentId, prompt, source, count, spellOptions) ->
                        offer(id, type, parentId, prompt, source, count, spellOptions, null, false),
                id -> answers.getOrDefault(id, List.of()), problems);
        build.choices().forEach(choice -> answers.put(choice.id(), choice.selections()));
    }

    /** Walks the build once; later calls return the same plan, since the walk accumulates grant state. */
    BuildPlan plan() {
        if (plan == null) {
            checkAbilityScoreMethod();
            planSpecies();
            planBackground();
            planClasses();
            applyAbilityScoreAdjustments();
            planHitPoints();
            checkEveryAnswerWasOffered();
            plan = new BuildPlan(List.copyOf(choices), List.copyOf(problems));
        }
        return plan;
    }

    /** The grant state after {@link #plan()} has walked the whole build. */
    Dnd5eGrantState state() {
        return state;
    }

    /** Everything else {@link #plan()} collected for materialization. */
    Dnd5eBuildOutcome outcome() {
        return outcome;
    }

    // ---- species ------------------------------------------------------------------

    private void planSpecies() {
        Optional<CatalogueRecord> found = offerCatalogueEntry("build.species", "SPECIES", "Choose a species", "Species", build.speciesSlug());
        if (found.isEmpty()) {
            return;
        }
        CatalogueRecord species = found.get();
        state.setSpeciesName(species.name());
        JsonNode data = species.data();
        JsonNode subspecies = null;
        List<JsonNode> subspeciesList = list(data.get("subspecies"));
        if (!subspeciesList.isEmpty()) {
            Map<String, JsonNode> byKey = subspeciesByKey(subspeciesList);
            List<CreationChoiceOption> subspeciesOptions = byKey.entrySet().stream()
                    .map(entry -> new CreationChoiceOption(entry.getKey(), subspeciesLabel(entry.getValue()), text(entry.getValue(), "sourceBook"),
                            options.summaries().species(entry.getValue())))
                    .toList();
            List<String> selected = offer("build.subspecies", "SUBSPECIES", null, "Choose a subspecies", species.name(), 1,
                    subspeciesOptions, singleton(build.subspeciesName()), false);
            if (selected.isEmpty()) {
                return;
            }
            subspecies = byKey.get(selected.getFirst());
        }
        JsonNode variantOwner = subspecies == null ? data : subspecies;
        List<JsonNode> variants = list(variantOwner.get("variants"));
        JsonNode variant = null;
        if (!variants.isEmpty()) {
            List<CreationChoiceOption> variantOptions = variants.stream()
                    .map(v -> new CreationChoiceOption(text(v, "name"), text(v, "name"), species.sourceBook(), options.summaries().species(v)))
                    .toList();
            List<String> selected = offer("build.variant", "SPECIES_VARIANT", null, "Choose a variant", species.name(), 1,
                    variantOptions, singleton(build.speciesVariantName()), false);
            if (selected.isEmpty()) {
                return;
            }
            variant = variants.stream().filter(v -> text(v, "name").equals(selected.getFirst())).findFirst().orElseThrow();
        }
        JsonNode mechanics = speciesMechanics(data, subspecies, variant);
        String source = speciesLabel(species, subspecies);
        String prefix = "species:" + species.slug();
        outcome.species(source, mechanics);
        for (JsonNode trait : list(mechanics.get("traits"))) {
            outcome.addFeature(prefix + ":" + Dnd5ePrerequisites.slug(text(trait, "name")), text(trait, "name"),
                    Dnd5eFeatureTraitCategory.SPECIES_TRAIT, source, text(trait, "description"), trait, null);
        }
        abilityBlock(prefix + ":ability", mechanics.get("abilityAlternatives"), source, null);
        List<String> sizes = strings(mechanics.get("size"));
        if (sizes.size() > 1) {
            offer(prefix + ":size", "SIZE", null, "Choose your size", source, 1,
                    sizes.stream().map(size -> new CreationChoiceOption(size, Dnd5eChoiceOptions.capitalize(size), null, null)).toList(),
                    null, false);
        }
        grantBlock(prefix, "skills", Dnd5eGrantState.Kind.SKILL, mechanics.get("skillAlternatives"), source, null);
        grantBlock(prefix, "tools", Dnd5eGrantState.Kind.TOOL, mechanics.get("toolAlternatives"), source, null);
        grantBlock(prefix, "languages", Dnd5eGrantState.Kind.LANGUAGE, mechanics.get("languageAlternatives"), source, null);
        grantBlock(prefix, "weapons", Dnd5eGrantState.Kind.WEAPON, mechanics.get("weaponAlternatives"), source, null);
        grantBlock(prefix, "armor", Dnd5eGrantState.Kind.ARMOR, mechanics.get("armorAlternatives"), source, null);
        skillToolLanguageBlock(prefix, mechanics.get("skillToolLanguageChoices"), source, null);
        pickListBlock(prefix + ":resistance", mechanics.get("damageResistances"), source);
        featBlock(prefix + ":feat", mechanics.get("featAlternatives"), source, null);
        spells.grant(prefix, mechanics.get("additionalSpells"), source, build.characterLevel(), null);
    }

    /** Species-level variants are complete species; a subspecies-level variant replaces that subspecies' layer. */
    private static JsonNode speciesMechanics(JsonNode species, JsonNode subspecies, JsonNode variant) {
        if (variant != null && subspecies == null) {
            return variant;
        }
        return merge(species, variant != null ? variant : subspecies);
    }

    /** The A2 merge rule: a layer's non-null scalars replace; its lists add to the species', unless named in {@code overwrite}. */
    static ObjectNode merge(JsonNode species, JsonNode layer) {
        ObjectNode result = (ObjectNode) species.deepCopy();
        result.remove("subspecies");
        result.remove("variants");
        if (layer == null) {
            return result;
        }
        Set<String> overwrite = new HashSet<>(strings(layer.get("overwrite")));
        for (Map.Entry<String, JsonNode> field : layer.properties()) {
            String name = field.getKey();
            JsonNode value = field.getValue();
            if (SPECIES_DESCRIPTIVE_FIELDS.contains(name) || value == null || value.isNull()) {
                continue;
            }
            JsonNode existing = result.get(name);
            if (SPECIES_SCALARS.contains(name) || existing == null || existing.isNull() || overwrite.contains(name)) {
                result.set(name, value.deepCopy());
            } else if (name.endsWith("Alternatives") && value.isArray() && existing.isArray()) {
                result.set(name, combineAlternatives((ArrayNode) existing, (ArrayNode) value));
            } else if (value.isArray() && existing.isArray()) {
                ArrayNode combined = ((ArrayNode) existing).deepCopy();
                value.forEach(item -> combined.add(item.deepCopy()));
                result.set(name, combined);
            } else if (value.isObject() && value.has("fixed") && existing.isObject()) {
                ObjectNode combined = (ObjectNode) existing.deepCopy();
                for (String part : List.of("fixed", "choices")) {
                    ArrayNode target = (ArrayNode) combined.get(part);
                    value.path(part).forEach(item -> target.add(item.deepCopy()));
                }
                result.set(name, combined);
            } else {
                result.set(name, value.deepCopy());
            }
        }
        return result;
    }

    /**
     * Species and subspecies grants both apply, so their alternatives combine pairwise:
     * each species alternative with each subspecies alternative, their fixed grants and
     * choices added together (fixed ability increases summed).
     */
    private static ArrayNode combineAlternatives(ArrayNode base, ArrayNode layer) {
        if (base.isEmpty()) {
            return layer.deepCopy();
        }
        if (layer.isEmpty()) {
            return base.deepCopy();
        }
        ArrayNode combined = base.arrayNode();
        for (JsonNode left : base) {
            for (JsonNode right : layer) {
                ObjectNode merged = (ObjectNode) left.deepCopy();
                JsonNode rightFixed = right.get("fixed");
                if (rightFixed != null && rightFixed.isObject()) {
                    ObjectNode fixed = (ObjectNode) merged.get("fixed");
                    for (Map.Entry<String, JsonNode> increase : rightFixed.properties()) {
                        fixed.put(increase.getKey(), fixed.path(increase.getKey()).asInt(0) + increase.getValue().asInt());
                    }
                } else if (rightFixed != null) {
                    rightFixed.forEach(item -> ((ArrayNode) merged.get("fixed")).add(item.deepCopy()));
                }
                right.path("choices").forEach(choice -> ((ArrayNode) merged.get("choices")).add(choice.deepCopy()));
                if (right.hasNonNull("maximum")) {
                    merged.set("maximum", right.get("maximum").deepCopy());
                }
                combined.add(merged);
            }
        }
        return combined;
    }

    // ---- background ---------------------------------------------------------------

    private void planBackground() {
        Optional<CatalogueRecord> found = offerCatalogueEntry("build.background", "BACKGROUND", "Choose a background", "Background", build.backgroundSlug());
        if (found.isEmpty()) {
            return;
        }
        CatalogueRecord background = found.get();
        outcome.background(background);
        JsonNode data = background.data();
        String prefix = "background:" + background.slug();
        String source = background.name();
        backgroundSource = source;
        grantBlock(prefix, "skills", Dnd5eGrantState.Kind.SKILL, data.get("skillAlternatives"), source, null);
        grantBlock(prefix, "tools", Dnd5eGrantState.Kind.TOOL, data.get("toolAlternatives"), source, null);
        grantBlock(prefix, "languages", Dnd5eGrantState.Kind.LANGUAGE, data.get("languageAlternatives"), source, null);
        skillToolLanguageBlock(prefix, data.get("skillToolLanguageChoices"), source, null);
        featBlock(prefix + ":feat", data.get("featAlternatives"), background.name(), null);
        equipmentGroups(prefix, data.get("startingEquipment"), background.name(), null);
        spells.grant(prefix, data.get("additionalSpells"), background.name(), build.characterLevel(), null);
    }

    // ---- classes ------------------------------------------------------------------

    private void planClasses() {
        choices.add(new CreationChoice("build.classes", "CLASSES", null, "Choose a class", "Classes",
                Math.max(1, build.classes().size()), false, catalogueOptions("CLASS"),
                build.classes().stream().map(Dnd5eBuildClass::classSlug).toList()));
        for (int index = 0; index < build.classes().size(); index++) {
            Dnd5eBuildClass buildClass = build.classes().get(index);
            Optional<CatalogueRecord> found = catalogue.find("CLASS", buildClass.classSlug());
            if (found.isEmpty()) {
                problems.add("Unknown class: " + buildClass.classSlug());
                continue;
            }
            CatalogueRecord playerClass = found.get();
            placement = new ChoicePlacement(playerClass.slug(), 1);
            if (index == 0) {
                planStartingClass(playerClass);
            } else {
                planMulticlass(playerClass);
            }
            outcome.addClass(playerClass, buildClass, planClassLevels(playerClass, buildClass));
            placement = null;
        }
    }

    private void planStartingClass(CatalogueRecord playerClass) {
        JsonNode data = playerClass.data();
        String prefix = "class:" + playerClass.slug();
        String source = playerClass.name() + " 1";
        strings(data.get("savingThrows")).forEach(save -> state.grant(Dnd5eGrantState.Kind.SAVING_THROW, save, source));
        applyFixedProficiencies(data.get("proficiencies"), source);
        grantBlock(prefix, "skills", Dnd5eGrantState.Kind.SKILL, data.path("proficiencies").get("skillAlternatives"), source, null);
        grantBlock(prefix, "tools", Dnd5eGrantState.Kind.TOOL, data.path("proficiencies").get("toolAlternatives"), source, null);
        JsonNode equipment = data.get("startingEquipment");
        String gold = equipment == null ? null : text(equipment, "goldAlternative");
        if (gold != null) {
            List<String> method = offer(prefix + ":equipment-method", "EQUIPMENT_METHOD", null,
                    "Take the starting equipment, or starting gold", playerClass.name(), 1,
                    List.of(new CreationChoiceOption("equipment", "Starting equipment", null, null),
                            new CreationChoiceOption("gold", "Starting gold", null, gold)),
                    null, false);
            if (method.contains("gold")) {
                outcome.addStartingCopper(averageGoldInCopper(gold));
            }
            if (!method.contains("equipment")) {
                return;
            }
        }
        equipmentGroups(prefix, equipment == null ? null : equipment.get("groups"), playerClass.name(), prefix + ":equipment-method");
    }

    private void planMulticlass(CatalogueRecord playerClass) {
        JsonNode multiclassing = playerClass.data().get("multiclassing");
        if (multiclassing == null || multiclassing.isNull()) {
            return;
        }
        if (build.preferences().multiclassPrerequisites()) {
            checkMulticlassRequirements(playerClass.name(), multiclassing);
        }
        String prefix = "class:" + playerClass.slug() + ":multiclass";
        String source = playerClass.name() + " (multiclass)";
        JsonNode gained = multiclassing.get("proficienciesGained");
        applyFixedProficiencies(gained, source);
        grantBlock(prefix, "skills", Dnd5eGrantState.Kind.SKILL, gained.get("skillAlternatives"), source, null);
        grantBlock(prefix, "tools", Dnd5eGrantState.Kind.TOOL, gained.get("toolAlternatives"), source, null);
    }

    private void checkMulticlassRequirements(String className, JsonNode multiclassing) {
        for (Map.Entry<String, JsonNode> minimum : multiclassing.path("allOf").properties()) {
            if (state.score(minimum.getKey()) < minimum.getValue().asInt()) {
                problems.add(className + " multiclass requires " + minimum.getKey() + " " + minimum.getValue().asInt());
            }
        }
        JsonNode anyOf = multiclassing.path("anyOf");
        if (!anyOf.isEmpty()) {
            boolean met = false;
            for (Map.Entry<String, JsonNode> minimum : anyOf.properties()) {
                met = met || state.score(minimum.getKey()) >= minimum.getValue().asInt();
            }
            if (!met) {
                problems.add(className + " multiclass requires one of " + anyOf);
            }
        }
    }

    private void applyFixedProficiencies(JsonNode proficiencies, String source) {
        if (proficiencies == null || proficiencies.isNull()) {
            return;
        }
        strings(proficiencies.get("armor")).forEach(armor -> state.grant(Dnd5eGrantState.Kind.ARMOR, armor, source));
        strings(proficiencies.get("weaponCategories")).forEach(weapon -> state.grant(Dnd5eGrantState.Kind.WEAPON, weapon, source));
        strings(proficiencies.get("weaponItems")).forEach(weapon -> state.grant(Dnd5eGrantState.Kind.WEAPON, weapon, source));
    }

    /** Starting gold at its average, like hit points: "4d4 × 10" gp is 4 × 2.5 × 10 = 100 gp. */
    private int averageGoldInCopper(String goldText) {
        Matcher matcher = GOLD_DICE.matcher(goldText);
        if (!matcher.find()) {
            problems.add("Unreadable starting gold: " + goldText);
            return 0;
        }
        int dice = Integer.parseInt(matcher.group(1));
        int faces = Integer.parseInt(matcher.group(2));
        int multiplier = matcher.group(3) == null ? 1 : Integer.parseInt(matcher.group(3));
        return dice * (faces + 1) * multiplier * 100 / 2;
    }

    private CatalogueRecord planClassLevels(CatalogueRecord playerClass, Dnd5eBuildClass buildClass) {
        JsonNode data = playerClass.data();
        String slug = playerClass.slug();
        int subclassLevel = data.path("subclassLevel").asInt(0);
        Set<Integer> asiLevels = new HashSet<>();
        data.path("abilityScoreImprovementLevels").forEach(level -> asiLevels.add(level.asInt()));
        CatalogueRecord subclass = null;
        spells.startClass(playerClass);
        String featureGroup = playerClass.name();
        for (int level = 1; level <= buildClass.level(); level++) {
            placement = new ChoicePlacement(slug, level);
            state.setClassLevel(slug, level);
            String source = playerClass.name() + " " + level;
            for (JsonNode feature : data.path("features")) {
                if (feature.path("level").asInt() != level || (feature.path("optional").asBoolean(false) && !build.preferences().optionalClassFeatures())) {
                    continue;
                }
                String featureKey = "class:" + slug + ":" + level + ":" + Dnd5ePrerequisites.slug(text(feature, "name"));
                if (!isSubclassPlaceholder(feature)) {
                    outcome.addFeature(featureKey, text(feature, "name"), Dnd5eFeatureTraitCategory.CLASS_FEATURE, featureGroup,
                            text(feature, "description"), feature, slug);
                }
                featureChoices(feature, "class:" + slug + ":" + level, source, featureGroup);
                featureGrants(feature, source);
                if (feature.path("grantsSubclassFeature").asBoolean(false) && level == subclassLevel) {
                    subclass = chooseSubclass(playerClass, buildClass, source).orElse(null);
                    if (subclass != null) {
                        outcome.addFeatureChoice(featureKey, subclass.name());
                        spells.grant("subclass:" + subclass.slug(), subclass.data().get("additionalSpells"), subclass.name(),
                                buildClass.level(), playerClass);
                    }
                }
            }
            if (asiLevels.contains(level)) {
                planAbilityScoreImprovement(slug, level, source);
            }
            optionalFeatureProgressions(data.get("optionalFeatureProgressions"), "class:" + slug + ":" + level, level, source, featureGroup);
            if (subclass != null) {
                planSubclassLevel(subclass, level, featureGroup);
            }
        }
        placement = new ChoicePlacement(slug, null);
        spells.finishClass(playerClass, subclass, buildClass.level());
        return subclass;
    }

    /** "Martial Archetype feature" rows only say a subclass feature arrives; D&D Beyond doesn't list them either. */
    private static boolean isSubclassPlaceholder(JsonNode feature) {
        return feature.path("grantsSubclassFeature").asBoolean(false) && text(feature, "name").endsWith(" feature");
    }

    private Optional<CatalogueRecord> chooseSubclass(CatalogueRecord playerClass, Dnd5eBuildClass buildClass, String source) {
        String title = text(playerClass.data(), "subclassTitle");
        List<String> selected = offer("build.class." + playerClass.slug() + ".subclass", "SUBCLASS", null,
                "Choose " + withArticle(title == null ? "subclass" : title), source, 1,
                options.subclasses(playerClass.slug()), singleton(buildClass.subclassSlug()), false);
        if (selected.isEmpty()) {
            return Optional.empty();
        }
        return catalogue.find("SUBCLASS", selected.getFirst());
    }

    /** Subclass features are listed under their class's {@code featureGroup}, as D&D Beyond does. */
    private void planSubclassLevel(CatalogueRecord subclass, int level, String featureGroup) {
        String source = subclass.name() + " " + level;
        String prefix = "subclass:" + subclass.slug() + ":" + level;
        for (JsonNode feature : subclass.data().path("features")) {
            if (feature.path("level").asInt() == level) {
                outcome.addFeature(prefix + ":" + Dnd5ePrerequisites.slug(text(feature, "name")), text(feature, "name"),
                        Dnd5eFeatureTraitCategory.CLASS_FEATURE, featureGroup, text(feature, "description"),
                        feature, text(subclass.data(), "classSlug"));
                featureChoices(feature, prefix, source, featureGroup);
                featureGrants(feature, source);
            }
        }
        optionalFeatureProgressions(subclass.data().get("optionalFeatureProgressions"), prefix, level, source, featureGroup);
    }

    private void planAbilityScoreImprovement(String classSlug, int level, String source) {
        String id = "class:" + classSlug + ":" + level + ":asi-or-feat";
        List<CreationChoiceOption> asiOrFeat = new ArrayList<>();
        asiOrFeat.add(new CreationChoiceOption("asi", "Ability Score Improvement", null, "+2 to one ability, or +1 to two"));
        asiOrFeat.addAll(availableFeats());
        List<String> selected = offer(id, "ASI_OR_FEAT", null, "Ability Score Improvement or a feat", source, 1, asiOrFeat, null, false);
        if (selected.isEmpty()) {
            return;
        }
        if ("asi".equals(selected.getFirst())) {
            List<String> increases = offer("class:" + classSlug + ":" + level + ":asi", "ABILITY_SCORE", id,
                    "Increase one ability by 2, or two abilities by 1", source, 2, abilityOptions(ABILITIES, null), null, true);
            increases.forEach(ability -> state.increase(ability, 1, "Ability Score Improvement (" + source + ")"));
            increases.stream().distinct().forEach(ability -> checkMaximum(ability, null, source));
        } else {
            planFeat(selected.getFirst(), "class:" + classSlug + ":" + level + ":feat", id, source);
        }
    }

    // ---- features, feats, optional features ---------------------------------------

    /** A class or subclass feature's adr-0007 overlay grants: languages its text names (Thieves' Cant, Dragon Ancestor's Draconic). */
    private void featureGrants(JsonNode feature, String source) {
        strings(feature.path("grants").get("languages")).forEach(language -> state.grant(Dnd5eGrantState.Kind.LANGUAGE, language, source));
    }

    /** A class or subclass feature's adr-0007 overlay choices; a chosen option becomes a feature under {@code featureGroup}. */
    private void featureChoices(JsonNode feature, String prefix, String source, String featureGroup) {
        List<JsonNode> featureChoices = list(feature.get("choices"));
        String featurePrefix = prefix + ":" + Dnd5ePrerequisites.slug(text(feature, "name"));
        for (int index = 0; index < featureChoices.size(); index++) {
            JsonNode choice = featureChoices.get(index);
            String id = featurePrefix + ":" + index;
            String type = text(choice, "type");
            int count = choice.path("count").asInt(1);
            String prompt = text(feature, "name");
            switch (type) {
                case "EXPERTISE" -> expertiseChoice(id, null, prompt, source, count, strings(choice.get("from")));
                case "PROFICIENCY" -> {
                    Dnd5eGrantState.Kind kind = kindOf(text(choice, "grant"));
                    List<String> selected = offer(id, kind.name(), null, prompt, source, count,
                            grantOptions(kind, choice.has("from") ? strings(choice.get("from")) : null, text(choice, "category"), null),
                            null, false);
                    selected.forEach(key -> state.grant(kind, key, source));
                }
                case "FEATURE_OPTION" -> {
                    List<CreationChoiceOption> featureOptions = list(choice.get("options")).stream()
                            .map(option -> new CreationChoiceOption(text(option, "key"), text(option, "label"), null, text(option, "summary")))
                            .toList();
                    for (String selected : offer(id, "FEATURE_OPTION", null, prompt, source, count, featureOptions, null, false)) {
                        CreationChoiceOption option = featureOptions.stream().filter(candidate -> candidate.key().equals(selected)).findFirst().orElseThrow();
                        String detail = option.summary() == null ? "" : option.summary();
                        outcome.addFeature(id + ":" + selected, prompt + ": " + option.label(), Dnd5eFeatureTraitCategory.CLASS_FEATURE,
                                featureGroup, detail);
                    }
                }
                default -> problems.add("Unsupported overlay choice type " + type + " on " + prompt);
            }
        }
    }

    private void optionalFeatureProgressions(JsonNode progressions, String prefix, int level, String source, String featureGroup) {
        for (JsonNode progression : list(progressions)) {
            List<JsonNode> counts = list(progression.get("countByLevel"));
            int now = counts.size() >= level ? counts.get(level - 1).asInt() : 0;
            int before = level > 1 && counts.size() >= level - 1 ? counts.get(level - 2).asInt() : 0;
            if (now > before) {
                offerOptionalFeatures(prefix + ":" + Dnd5ePrerequisites.slug(text(progression, "name")), null,
                        text(progression, "name"), strings(progression.get("featureTypes")), now - before, level, source, featureGroup);
            }
        }
    }

    /** {@code source} labels the choice; {@code featureGroup} is where each chosen feature is listed on the sheet. */
    private void offerOptionalFeatures(String id, String parentId, String name, List<String> featureTypes, int count, int level,
            String source, String featureGroup) {
        List<CreationChoiceOption> available = new ArrayList<>();
        Map<String, CatalogueRecord> byKey = new HashMap<>();
        for (CatalogueRecord feature : options.optionalFeatures(featureTypes)) {
            if (state.hasOptionalFeature(feature.slug())) {
                continue;
            }
            Dnd5ePrerequisites.Result prerequisites = Dnd5ePrerequisites.evaluate(feature.data().get("prerequisites"), state);
            if (!prerequisites.met()) {
                continue;
            }
            available.add(withUnverified(options.summarized(feature), prerequisites, text(feature.data(), "prerequisiteText")));
            byKey.put(feature.slug(), feature);
        }
        List<String> selected = offer(id, "OPTIONAL_FEATURE", parentId, "Choose " + count + " " + name, source, count, available, null, false);
        for (String key : selected) {
            state.addOptionalFeature(key);
            CatalogueRecord feature = byKey.get(key);
            if (feature == null) {
                continue;
            }
            outcome.addFeature(id + ":" + key, name + ": " + feature.name(), Dnd5eFeatureTraitCategory.CLASS_FEATURE, featureGroup,
                    feature.description(), feature.data(), null);
            grantBlock(id + ":" + key, "skills", Dnd5eGrantState.Kind.SKILL, feature.data().get("skillAlternatives"), feature.name(), id);
            spells.grant(id + ":" + key, feature.data().get("additionalSpells"), feature.name(), build.characterLevel(), null,
                    feature.description());
            for (JsonNode progression : list(feature.data().get("optionalFeatureProgressions"))) {
                List<JsonNode> counts = list(progression.get("countByLevel"));
                int granted = counts.size() >= level ? counts.get(level - 1).asInt() : 0;
                if (granted > 0) {
                    offerOptionalFeatures(id + ":" + key + ":" + Dnd5ePrerequisites.slug(text(progression, "name")), id,
                            text(progression, "name"), strings(progression.get("featureTypes")), granted, level, feature.name(), featureGroup);
                }
            }
        }
    }

    private void featBlock(String prefix, JsonNode featAlternatives, String source, String parentId) {
        List<JsonNode> alternatives = list(featAlternatives);
        if (alternatives.isEmpty()) {
            return;
        }
        JsonNode alternative = alternatives.getFirst();
        for (String fixed : strings(alternative.get("fixed"))) {
            planFeat(fixed, prefix + ":" + fixed, parentId, source);
        }
        List<JsonNode> featChoices = list(alternative.get("choices"));
        for (int index = 0; index < featChoices.size(); index++) {
            JsonNode choice = featChoices.get(index);
            String id = prefix + ":" + index;
            List<CreationChoiceOption> available = choice.hasNonNull("from")
                    ? availableFeats().stream().filter(feat -> strings(choice.get("from")).contains(feat.key())).toList()
                    : availableFeats();
            List<String> selected = offer(id, "FEAT", parentId, "Choose a feat", source, choice.path("count").asInt(1), available, null, false);
            selected.forEach(feat -> planFeat(feat, id + ":" + feat, id, source));
        }
    }

    private List<CreationChoiceOption> availableFeats() {
        List<CreationChoiceOption> available = new ArrayList<>();
        for (CatalogueRecord feat : options.feats()) {
            if (state.hasFeat(feat.slug()) && !feat.data().path("repeatable").asBoolean(false)) {
                continue;
            }
            Dnd5ePrerequisites.Result prerequisites = Dnd5ePrerequisites.evaluate(feat.data().get("prerequisites"), state);
            if (prerequisites.met() || !build.preferences().featPrerequisites()) {
                available.add(withUnverified(options.summarized(feat), prerequisites, text(feat.data(), "prerequisiteText")));
            }
        }
        available.sort((a, b) -> a.label().compareTo(b.label()));
        return available;
    }

    /** {@code grantedBy} names what gave the feat ("Fighter 4", "Variant Human"), shown as the feature's source. */
    private void planFeat(String slug, String prefix, String parentId, String grantedBy) {
        Optional<CatalogueRecord> found = catalogue.find("FEAT", slug);
        if (found.isEmpty()) {
            problems.add("Unknown feat: " + slug);
            return;
        }
        CatalogueRecord feat = found.get();
        Dnd5ePrerequisites.Result prerequisites = Dnd5ePrerequisites.evaluate(feat.data().get("prerequisites"), state);
        if (!prerequisites.met() && build.preferences().featPrerequisites()) {
            problems.add("Feat prerequisites not met: " + feat.name());
        }
        state.addFeat(slug);
        outcome.addFeature("feat:" + slug, feat.name(), Dnd5eFeatureTraitCategory.FEAT, grantedBy, feat.description(),
                feat.data(), null);
        JsonNode data = feat.data();
        String source = feat.name();
        abilityBlock(prefix + ":ability", data.get("abilityAlternatives"), source, parentId);
        grantBlock(prefix, "skills", Dnd5eGrantState.Kind.SKILL, data.get("skillAlternatives"), source, parentId);
        grantBlock(prefix, "tools", Dnd5eGrantState.Kind.TOOL, data.get("toolAlternatives"), source, parentId);
        grantBlock(prefix, "languages", Dnd5eGrantState.Kind.LANGUAGE, data.get("languageAlternatives"), source, parentId);
        grantBlock(prefix, "weapons", Dnd5eGrantState.Kind.WEAPON, data.get("weaponAlternatives"), source, parentId);
        grantBlock(prefix, "armor", Dnd5eGrantState.Kind.ARMOR, data.get("armorAlternatives"), source, parentId);
        grantBlock(prefix, "saving-throws", Dnd5eGrantState.Kind.SAVING_THROW, data.get("savingThrowAlternatives"), source, parentId);
        skillToolLanguageBlock(prefix, data.get("skillToolLanguageChoices"), source, parentId);
        spells.grant(prefix, data.get("additionalSpells"), source, build.characterLevel(), null, feat.description());
        List<JsonNode> expertise = list(data.get("expertiseAlternatives"));
        if (!expertise.isEmpty()) {
            JsonNode alternative = expertise.getFirst();
            strings(alternative.get("fixed")).forEach(key -> state.addExpertise(key, source));
            List<JsonNode> expertiseChoices = list(alternative.get("choices"));
            for (int index = 0; index < expertiseChoices.size(); index++) {
                expertiseChoice(prefix + ":expertise:" + index, parentId, "Expertise", source,
                        expertiseChoices.get(index).path("count").asInt(1), List.of("proficientSkills"));
            }
        }
        int characterLevel = Math.max(1, state.characterLevel());
        for (JsonNode progression : list(data.get("optionalFeatureProgressions"))) {
            List<JsonNode> counts = list(progression.get("countByLevel"));
            int granted = counts.size() >= characterLevel ? counts.get(characterLevel - 1).asInt() : 0;
            if (granted > 0) {
                offerOptionalFeatures(prefix + ":" + Dnd5ePrerequisites.slug(text(progression, "name")), parentId,
                        text(progression, "name"), strings(progression.get("featureTypes")), granted, characterLevel, source, source);
            }
        }
    }

    // ---- grant blocks -------------------------------------------------------------

    /** One block of alternatives in the shared grant shape: an alternative pick if there's more than one, then fixed grants, then each choice. */
    private void grantBlock(String prefix, String label, Dnd5eGrantState.Kind kind, JsonNode alternatives, String source, String parentId) {
        Optional<JsonNode> alternative = chooseAlternative(prefix + ":" + label + "-set", alternatives, label, source, parentId);
        if (alternative.isEmpty()) {
            return;
        }
        strings(alternative.get().get("fixed")).forEach(key -> grantOrReplace(prefix + ":" + label, kind, key, source, parentId));
        List<JsonNode> grantChoices = list(alternative.get().get("choices"));
        for (int index = 0; index < grantChoices.size(); index++) {
            JsonNode choice = grantChoices.get(index);
            String id = prefix + ":" + label + ":" + index;
            List<CreationChoiceOption> choiceOptions = grantOptions(kind,
                    choice.hasNonNull("from") ? strings(choice.get("from")) : null, text(choice, "category"), text(choice, "fromFilter"));
            List<String> selected = offer(id, kind.name(), parentId, "Choose " + choice.path("count").asInt(1) + " " + label, source,
                    choice.path("count").asInt(1), choiceOptions, null, false);
            selected.forEach(key -> grantOrReplace(id, kind, key, source, parentId));
        }
    }

    /**
     * Grants a proficiency; a skill or tool the character already has from another source is replaced by
     * a choice of another one of the same kind instead (PHB p. 125).
     */
    private void grantOrReplace(String idPrefix, Dnd5eGrantState.Kind kind, String key, String source, String parentId) {
        String grantedBy = state.sourceOf(kind, key);
        boolean replaceable = kind == Dnd5eGrantState.Kind.SKILL || kind == Dnd5eGrantState.Kind.TOOL;
        if (grantedBy == null || grantedBy.equals(source) || !replaceable) {
            state.grant(kind, key, source);
            return;
        }
        List<CreationChoiceOption> replacements = allGrantOptions(kind, null, null, null).stream()
                .filter(option -> state.sourceOf(kind, option.key()) == null)
                .toList();
        if (replacements.isEmpty()) {
            state.grant(kind, key, source);
            return;
        }
        String duplicate = allGrantOptions(kind, List.of(key), null, null).stream().findFirst()
                .map(CreationChoiceOption::label).orElse(key);
        List<String> selected = offer(idPrefix + ":replace:" + key, kind.name(), parentId,
                "Replace duplicate " + duplicate + " (already from " + grantedBy + ")", source, 1, replacements, null, false);
        selected.forEach(replacement -> state.grant(kind, replacement, source));
    }

    private Optional<JsonNode> chooseAlternative(String id, JsonNode alternatives, String label, String source, String parentId) {
        List<JsonNode> all = list(alternatives);
        if (all.isEmpty()) {
            return Optional.empty();
        }
        if (all.size() == 1) {
            return Optional.of(all.getFirst());
        }
        List<CreationChoiceOption> alternativeOptions = new ArrayList<>();
        for (int index = 0; index < all.size(); index++) {
            alternativeOptions.add(new CreationChoiceOption(String.valueOf(index), "Option " + (index + 1), null, all.get(index).toString()));
        }
        List<String> selected = offer(id, "ALTERNATIVE", parentId, "Choose which " + label + " to take", source, 1, alternativeOptions, null, false);
        return selected.isEmpty() ? Optional.empty() : Optional.of(all.get(Integer.parseInt(selected.getFirst())));
    }

    /** Options for one grant choice, dropping what another source already granted — except a background grant, which stays offered and labelled. */
    private List<CreationChoiceOption> grantOptions(Dnd5eGrantState.Kind kind, List<String> from, String category, String fromFilter) {
        List<CreationChoiceOption> raw = allGrantOptions(kind, from, category, fromFilter);
        List<CreationChoiceOption> remaining = new ArrayList<>();
        for (CreationChoiceOption option : raw) {
            String grantedBy = state.sourceOf(kind, option.key());
            if (grantedBy == null) {
                remaining.add(option);
            } else if (grantedBy.equals(backgroundSource)) {
                remaining.add(new CreationChoiceOption(option.key(), option.label() + " (" + backgroundSource + ")", option.sourceBook(), option.summary()));
            }
        }
        return remaining;
    }

    private List<CreationChoiceOption> allGrantOptions(Dnd5eGrantState.Kind kind, List<String> from, String category, String fromFilter) {
        return switch (kind) {
            case SKILL -> options.skills(from);
            case TOOL -> options.tools(from, category == null ? "any" : category);
            case LANGUAGE -> options.languages(from, category == null ? "any" : category);
            case SAVING_THROW -> abilityOptions(from == null ? ABILITIES : from, null);
            case WEAPON -> weaponOptions(from, fromFilter);
            case ARMOR -> from == null ? List.of() : from.stream()
                    .map(key -> new CreationChoiceOption(key, Dnd5eChoiceOptions.capitalize(key), null, null)).toList();
        };
    }

    private List<CreationChoiceOption> weaponOptions(List<String> from, String fromFilter) {
        if (from != null) {
            return from.stream().map(key -> catalogue.find("ITEM", key).map(Dnd5eChoiceOptions::option)
                    .orElse(new CreationChoiceOption(key, Dnd5eChoiceOptions.labelFromSlug(key), null, null))).toList();
        }
        if (fromFilter != null) {
            Optional<List<CreationChoiceOption>> filtered = options.weaponsByFilter(fromFilter);
            if (filtered.isEmpty()) {
                problems.add("Unsupported weapon filter: " + fromFilter);
            }
            return filtered.orElse(List.of());
        }
        return List.of();
    }

    private void abilityBlock(String prefix, JsonNode alternatives, String source, String parentId) {
        Optional<JsonNode> alternative = chooseAlternative(prefix + "-set", alternatives, "ability increase", source, parentId);
        if (alternative.isEmpty()) {
            return;
        }
        for (Map.Entry<String, JsonNode> increase : alternative.get().path("fixed").properties()) {
            state.increase(increase.getKey(), increase.getValue().asInt(), source);
        }
        Integer maximum = alternative.get().hasNonNull("maximum") ? alternative.get().get("maximum").asInt() : null;
        List<JsonNode> abilityChoices = list(alternative.get().get("choices"));
        for (int index = 0; index < abilityChoices.size(); index++) {
            JsonNode choice = abilityChoices.get(index);
            int count = choice.path("count").asInt(1);
            List<Integer> weights = list(choice.get("weights")).stream().map(JsonNode::asInt).toList();
            int amount = choice.path("amount").asInt(1);
            String prompt = weights.isEmpty()
                    ? "Increase " + count + (count == 1 ? " ability" : " abilities") + " by " + amount
                    : "Choose different abilities to increase by " + String.join(", ", weights.stream().map(weight -> "+" + weight).toList())
                            + ", in that order";
            List<String> selected = offer(prefix + ":" + index, "ABILITY_SCORE", parentId, prompt, source, count,
                    abilityOptions(strings(choice.get("from")), maximum), null, false);
            for (int position = 0; position < selected.size(); position++) {
                state.increase(selected.get(position), weights.isEmpty() ? amount : weights.get(position), source);
                checkMaximum(selected.get(position), maximum, source);
            }
        }
    }

    /** An increase may not raise a score past its maximum (20 unless the source says otherwise). */
    private void checkMaximum(String ability, Integer maximum, String source) {
        int cap = maximum == null ? 20 : maximum;
        if (state.score(ability) > cap) {
            problems.add(source + " raises " + ability + " to " + state.score(ability) + ", above its maximum of " + cap);
        }
    }

    private List<CreationChoiceOption> abilityOptions(List<String> from, Integer maximum) {
        return options.abilities(from.stream().filter(ability -> !state.isMaxed(ability, maximum)).toList());
    }

    /** Expertise picks among what the character is already proficient in and not yet expert at — D&D Beyond's EXPERTISE subtype. */
    private void expertiseChoice(String id, String parentId, String prompt, String source, int count, List<String> from) {
        List<CreationChoiceOption> expertiseOptions = new ArrayList<>();
        for (String entry : from) {
            if ("proficientSkills".equals(entry)) {
                for (String skill : state.keys(Dnd5eGrantState.Kind.SKILL)) {
                    if (!state.hasExpertise(skill)) {
                        expertiseOptions.add(new CreationChoiceOption(skill, Dnd5eSkills.LABELS.getOrDefault(skill, skill), null, null));
                    }
                }
            } else if (state.has(Dnd5eGrantState.Kind.TOOL, entry) && !state.hasExpertise(entry)) {
                expertiseOptions.add(new CreationChoiceOption(entry, Dnd5eChoiceOptions.labelFromSlug(entry), null, null));
            }
        }
        offer(id, "EXPERTISE", parentId, prompt, source, count, expertiseOptions, null, false)
                .forEach(key -> state.addExpertise(key, source));
    }

    /** 5etools' {@code skillToolLanguageProficiencies}: picks drawn from any skill, tool or language. */
    private void skillToolLanguageBlock(String prefix, JsonNode block, String source, String parentId) {
        int index = 0;
        for (JsonNode alternative : list(block)) {
            for (JsonNode choose : list(alternative.get("choose"))) {
                Map<String, Dnd5eGrantState.Kind> kindByKey = new HashMap<>();
                List<CreationChoiceOption> pool = new ArrayList<>();
                for (String category : strings(choose.get("from"))) {
                    Dnd5eGrantState.Kind kind = switch (category) {
                        case "anySkill" -> Dnd5eGrantState.Kind.SKILL;
                        case "anyTool" -> Dnd5eGrantState.Kind.TOOL;
                        case "anyLanguage" -> Dnd5eGrantState.Kind.LANGUAGE;
                        default -> null;
                    };
                    if (kind == null) {
                        problems.add("Unsupported skill/tool/language category: " + category);
                        continue;
                    }
                    for (CreationChoiceOption option : grantOptions(kind, null, "any", null)) {
                        pool.add(option);
                        kindByKey.put(option.key(), kind);
                    }
                }
                String id = prefix + ":skill-tool-language:" + index++;
                List<String> selected = offer(id, "SKILL_TOOL_LANGUAGE", parentId, "Choose skills, tools or languages", source,
                        choose.path("count").asInt(1), pool, null, false);
                selected.forEach(key -> state.grant(kindByKey.get(key), key, source));
            }
        }
    }

    /** Resistances and immunities offered as picks (e.g. a draconic ancestry's damage type). */
    private void pickListBlock(String prefix, JsonNode pickList, String source) {
        List<JsonNode> pickChoices = pickList == null ? List.of() : list(pickList.get("choices"));
        for (int index = 0; index < pickChoices.size(); index++) {
            JsonNode choice = pickChoices.get(index);
            offer(prefix + ":" + index, "RESISTANCE", null, "Choose a damage resistance", source, choice.path("count").asInt(1),
                    strings(choice.get("from")).stream().map(type -> new CreationChoiceOption(type, Dnd5eChoiceOptions.capitalize(type), null, null)).toList(),
                    null, false).forEach(outcome::addResistancePick);
        }
    }

    private void equipmentGroups(String prefix, JsonNode groups, String source, String parentId) {
        List<JsonNode> all = list(groups);
        for (int groupIndex = 0; groupIndex < all.size(); groupIndex++) {
            List<JsonNode> groupOptions = list(all.get(groupIndex).get("options"));
            String groupId = prefix + ":equipment:" + groupIndex;
            JsonNode chosen;
            if (groupOptions.size() == 1) {
                chosen = groupOptions.getFirst();
            } else {
                List<CreationChoiceOption> optionList = groupOptions.stream()
                        .map(option -> new CreationChoiceOption(text(option, "label"), "(" + text(option, "label") + ") " + grantsSummary(option.get("grants")), null, null))
                        .toList();
                List<String> selected = offer(groupId, "EQUIPMENT", parentId, "Choose starting equipment", source, 1, optionList, null, false);
                if (selected.isEmpty()) {
                    continue;
                }
                chosen = groupOptions.stream().filter(option -> selected.getFirst().equals(text(option, "label"))).findFirst().orElse(null);
                if (chosen == null) {
                    continue;
                }
            }
            List<JsonNode> grants = list(chosen.get("grants"));
            for (int grantIndex = 0; grantIndex < grants.size(); grantIndex++) {
                JsonNode grant = grants.get(grantIndex);
                int quantity = grant.path("quantity").asInt(1);
                recordFixedGrant(grant, quantity);
                String equipmentType = text(grant, "equipmentType");
                if (equipmentType == null) {
                    continue;
                }
                Optional<List<CreationChoiceOption>> items = options.equipmentCategory(equipmentType);
                if (items.isEmpty()) {
                    problems.add("Unsupported equipment category: " + equipmentType);
                    continue;
                }
                offer(groupId + ":" + grantIndex, "EQUIPMENT_ITEM", groupOptions.size() == 1 ? parentId : groupId,
                        "Choose " + quantity + " " + EQUIPMENT_TYPE_LABELS.getOrDefault(equipmentType, equipmentType), source,
                        quantity, items.get(), null, true)
                        .forEach(slug -> outcome.addStartingItem(slug, null, 1, null));
            }
        }
    }

    /** A grant that needs no further pick: a catalogue item, a free-text trinket, or coins (a pouch's contents too). */
    private void recordFixedGrant(JsonNode grant, int quantity) {
        if (text(grant, "itemSlug") != null) {
            outcome.addStartingItem(text(grant, "itemSlug"), null, quantity, text(grant, "displayName"));
        } else if (text(grant, "special") != null) {
            outcome.addStartingItem(null, text(grant, "special"), quantity, null);
        } else if (grant.hasNonNull("valueCp")) {
            outcome.addStartingCopper(grant.get("valueCp").asInt());
        }
        if (grant.hasNonNull("containsValueCp")) {
            outcome.addStartingCopper(grant.get("containsValueCp").asInt());
        }
    }

    /** A starting-equipment package in words: "Common Clothes, Pouch (with 10 gp), 2 × Dagger". */
    String grantsSummary(JsonNode grants) {
        List<String> parts = new ArrayList<>();
        for (JsonNode grant : list(grants)) {
            int quantity = grant.path("quantity").asInt(1);
            String prefix = quantity > 1 ? quantity + " × " : "";
            String displayName = text(grant, "displayName");
            String contents = grant.hasNonNull("containsValueCp") ? " (with " + grant.get("containsValueCp").asInt() / 100 + " gp)" : "";
            if (displayName != null) {
                parts.add(prefix + displayName + contents);
            } else if (text(grant, "itemSlug") != null) {
                String slug = text(grant, "itemSlug");
                parts.add(prefix + catalogue.find("ITEM", slug).map(CatalogueRecord::name).orElse(Dnd5eChoiceOptions.labelFromSlug(slug)) + contents);
            } else if (text(grant, "equipmentType") != null) {
                parts.add(prefix + "any " + EQUIPMENT_TYPE_LABELS.getOrDefault(text(grant, "equipmentType"), text(grant, "equipmentType")));
            } else if (text(grant, "special") != null) {
                parts.add(prefix + text(grant, "special"));
            } else if (grant.hasNonNull("valueCp")) {
                parts.add(grant.get("valueCp").asInt() / 100 + " gp");
            }
        }
        return String.join(", ", parts);
    }

    /** Every group of a starting-equipment list in words; a group with alternatives reads "A or B". */
    String equipmentSummary(JsonNode groups) {
        List<String> parts = new ArrayList<>();
        for (JsonNode group : list(groups)) {
            List<String> alternatives = list(group.get("options")).stream().map(option -> grantsSummary(option.get("grants"))).filter(text -> !text.isEmpty()).toList();
            if (!alternatives.isEmpty()) {
                parts.add(String.join(" or ", alternatives));
            }
        }
        return String.join("; ", parts);
    }

    // ---- hit points and validation ------------------------------------------------

    /**
     * One option per level after the first, in the order the materializer reads {@code rolledHitPoints}:
     * the class level it belongs to ("Warlock lvl 2", with the class key as {@code group}), its hit die
     * and the average it replaces. Unrolled levels are null.
     */
    private void planHitPoints() {
        if (build.hitPointMethod() != Dnd5eHitPointMethod.ROLLED) {
            return;
        }
        List<CreationChoiceOption> levels = rolledHitPointLevels();
        if (levels.isEmpty()) {
            return;
        }
        List<Integer> rolled = build.rolledHitPoints();
        for (int index = 0; index < Math.min(rolled.size(), levels.size()); index++) {
            Integer value = rolled.get(index);
            int sides = levels.get(index).data().path("sides").asInt();
            if (value != null && (value < 1 || value > sides)) {
                problems.add("Hit points for " + levels.get(index).label() + " must be between 1 and " + sides);
            }
        }
        choices.add(new CreationChoice("build.rolledHitPoints", "ROLLED_HIT_POINTS", null,
                "Roll one hit die per level after the first", "Hit Points", levels.size(), false, levels,
                rolled.stream().limit(levels.size()).filter(Objects::nonNull).map(String::valueOf).toList()));
    }

    private List<CreationChoiceOption> rolledHitPointLevels() {
        List<CreationChoiceOption> levels = new ArrayList<>();
        boolean first = true;
        for (Dnd5eBuildOutcome.ClassOutcome taken : outcome.classes()) {
            int sides = taken.playerClass().data().path("hitDie").asInt(8);
            for (int classLevel = 1; classLevel <= taken.buildClass().level(); classLevel++) {
                if (first) {
                    first = false;
                    continue;
                }
                String label = taken.playerClass().name() + " lvl " + classLevel;
                ObjectNode data = JsonNodeFactory.instance.objectNode()
                        .put("group", taken.playerClass().slug())
                        .put("classLevel", classLevel)
                        .put("sides", sides)
                        .put("average", sides / 2 + 1)
                        .put("rollLabel", "Hit points, " + label + " (d" + sides + ")");
                levels.add(new CreationChoiceOption(String.valueOf(levels.size()), label, null, null, data));
            }
        }
        return levels;
    }

    /** The player's manual corrections, applied last: other modifiers stack on the total, an override replaces it. */
    private void applyAbilityScoreAdjustments() {
        for (String ability : ABILITIES) {
            Dnd5eAbilityAdjustment adjustment = build.abilityScoreAdjustments().get(ability);
            if (adjustment == null) {
                continue;
            }
            if (adjustment.otherModifier() != null && adjustment.otherModifier() != 0) {
                state.increase(ability, adjustment.otherModifier(), "Other modifier");
            }
            if (adjustment.overrideScore() != null) {
                state.overrideScore(ability, adjustment.overrideScore(), "Override");
            }
        }
    }

    private void checkAbilityScoreMethod() {
        boolean answered = build.abilityScoreMethod() != null && build.hasEveryAbilityScore();
        choices.add(new CreationChoice("build.abilityScores", "ABILITY_SCORES", null, "Set your ability scores", "Abilities", 1, false,
                Arrays.stream(Dnd5eAbilityScoreMethod.values()).map(Dnd5eBuildPlanner::abilityScoreMethodOption).toList(),
                answered ? List.of(build.abilityScoreMethod().name()) : List.of()));
        if (!answered) {
            return;
        }
        List<Integer> scores = ABILITIES.stream().map(ability -> build.baseAbilityScores().getOrDefault(ability, 0)).sorted().toList();
        switch (build.abilityScoreMethod()) {
            case STANDARD_ARRAY -> {
                if (!scores.equals(STANDARD_ARRAY)) {
                    problems.add("Standard array scores must be 15, 14, 13, 12, 10 and 8");
                }
            }
            case POINT_BUY -> {
                int spent = 0;
                for (int score : scores) {
                    Integer cost = POINT_BUY_COSTS.get(score);
                    if (cost == null) {
                        problems.add("Point buy scores must be between 8 and 15");
                        return;
                    }
                    spent += cost;
                }
                if (spent > POINT_BUY_BUDGET) {
                    problems.add("Point buy spends " + spent + " of " + POINT_BUY_BUDGET + " points");
                }
            }
            case MANUAL -> {
            }
        }
    }

    /** Each method with its rules as data: the standard array's values, point buy's budget and costs, manual entry's range and dice. */
    private static CreationChoiceOption abilityScoreMethodOption(Dnd5eAbilityScoreMethod method) {
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        switch (method) {
            case STANDARD_ARRAY -> {
                ArrayNode values = data.putArray("values");
                STANDARD_ARRAY.reversed().forEach(value -> values.add(value.intValue()));
            }
            case POINT_BUY -> {
                data.put("budget", POINT_BUY_BUDGET);
                ObjectNode costs = data.putObject("costs");
                new TreeMap<>(POINT_BUY_COSTS).forEach((score, cost) -> costs.put(String.valueOf(score), cost.intValue()));
            }
            case MANUAL -> {
                data.put("min", MANUAL_MINIMUM).put("max", MANUAL_MAXIMUM);
                data.putObject("roll")
                        .put("count", ABILITY_ROLL_DICE)
                        .put("sides", ABILITY_ROLL_SIDES)
                        .put("keepHighest", ABILITY_ROLL_KEPT)
                        .put("label", ABILITY_ROLL_DICE + "d" + ABILITY_ROLL_SIDES + " drop lowest");
            }
        }
        return new CreationChoiceOption(method.name(), method.label(), null, null, data);
    }

    private void checkEveryAnswerWasOffered() {
        Set<String> offered = new HashSet<>();
        choices.forEach(choice -> offered.add(choice.id()));
        for (String answered : answers.keySet()) {
            if (!offered.contains(answered)) {
                problems.add("Answer for a choice this build doesn't offer: " + answered);
            }
        }
    }

    // ---- choice bookkeeping -------------------------------------------------------

    /** Offers a pick among every catalogue entry of {@code kind}; an unknown slug is a problem, not a selection. */
    private Optional<CatalogueRecord> offerCatalogueEntry(String id, String kind, String prompt, String label, String slug) {
        Optional<CatalogueRecord> found = slug == null ? Optional.empty() : catalogue.find(kind, slug);
        if (slug != null && found.isEmpty()) {
            problems.add("Unknown " + label.toLowerCase() + ": " + slug);
        }
        offer(id, kind, null, prompt, label, 1, catalogueOptions(kind), found.map(entry -> List.of(entry.slug())).orElse(List.of()), false);
        return found;
    }

    /** Every entry of {@code kind}, alphabetical by name, then by source book for same-named entries. */
    private List<CreationChoiceOption> catalogueOptions(String kind) {
        return catalogue.list(kind).stream()
                .sorted(Comparator.comparing(CatalogueRecord::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(entry -> entry.sourceBook() == null ? "" : entry.sourceBook()))
                .map(options::summarized)
                .toList();
    }

    /**
     * Records one choice and returns its valid selections. {@code structuralAnswer}
     * overrides the build's choices list for {@code build.*} questions; options that
     * don't match are reported as problems and ignored.
     */
    private List<String> offer(
            String id, String type, String parentId, String prompt, String source, int count,
            List<CreationChoiceOption> choiceOptions, List<String> structuralAnswer, boolean allowRepeats) {
        List<String> answer = structuralAnswer != null ? structuralAnswer : answers.getOrDefault(id, List.of());
        Set<String> keys = new HashSet<>();
        choiceOptions.forEach(option -> keys.add(option.key()));
        List<String> valid = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String selection : answer) {
            if (!keys.contains(selection)) {
                problems.add("'" + selection + "' is not an option of " + id);
            } else if (!allowRepeats && !seen.add(selection)) {
                problems.add("'" + selection + "' is selected twice in " + id);
            } else {
                valid.add(selection);
            }
        }
        if (valid.size() > count) {
            problems.add(id + " allows " + count + " selection(s), got " + valid.size());
            valid = valid.subList(0, count);
        }
        if (choiceOptions.isEmpty() && count > 0) {
            problems.add("No options available for " + id);
        }
        choices.add(new CreationChoice(id, type, parentId, prompt, source, count, false, choiceOptions, List.copyOf(valid), placement));
        return valid.size() == count ? valid : List.of();
    }

    /** Appends the prerequisite, and any part of it not checked yet, to the option's own summary. */
    private static CreationChoiceOption withUnverified(CreationChoiceOption option, Dnd5ePrerequisites.Result prerequisites, String prerequisiteText) {
        String prerequisite = prerequisiteText == null ? null : "Prerequisite: " + prerequisiteText;
        if (!prerequisites.unverified().isEmpty()) {
            String note = "prerequisite not checked yet: " + String.join(", ", prerequisites.unverified());
            prerequisite = prerequisite == null ? note : prerequisite + " (" + note + ")";
        }
        String summary = option.summary() == null ? prerequisite : prerequisite == null ? option.summary() : option.summary() + " · " + prerequisite;
        return new CreationChoiceOption(option.key(), option.label(), option.sourceBook(), summary, option.data());
    }

    private static Dnd5eGrantState.Kind kindOf(String grant) {
        return switch (grant) {
            case "skill" -> Dnd5eGrantState.Kind.SKILL;
            case "tool" -> Dnd5eGrantState.Kind.TOOL;
            case "language" -> Dnd5eGrantState.Kind.LANGUAGE;
            case "weapon" -> Dnd5eGrantState.Kind.WEAPON;
            case "armor" -> Dnd5eGrantState.Kind.ARMOR;
            default -> throw new IllegalArgumentException("Unknown overlay grant: " + grant);
        };
    }

    /**
     * Keyed by name ({@code ""} for the unnamed default); a name two books share gets
     * its source book appended — "Eladrin (Mordenkainen's Tome of Foes)" — so each
     * stays selectable.
     */
    private static Map<String, JsonNode> subspeciesByKey(List<JsonNode> subspeciesList) {
        Map<String, Integer> nameCounts = new HashMap<>();
        subspeciesList.forEach(sub -> nameCounts.merge(subspeciesKey(sub), 1, Integer::sum));
        Map<String, JsonNode> byKey = new LinkedHashMap<>();
        for (JsonNode sub : subspeciesList) {
            String name = subspeciesKey(sub);
            byKey.put(nameCounts.get(name) > 1 ? name + " (" + text(sub, "sourceBook") + ")" : name, sub);
        }
        return byKey;
    }

    private static String subspeciesKey(JsonNode subspecies) {
        return subspecies.hasNonNull("name") ? subspecies.get("name").asString() : "";
    }

    private static String subspeciesLabel(JsonNode subspecies) {
        return subspecies.hasNonNull("name") ? subspecies.get("name").asString() : "Standard";
    }

    private static String speciesLabel(CatalogueRecord species, JsonNode subspecies) {
        return speciesLabel(species.name(), subspecies == null || !subspecies.hasNonNull("name") ? null : subspecies.get("name").asString());
    }

    /** "Mountain Dwarf": the subspecies name before the species name, or the species alone. */
    static String speciesLabel(String speciesName, String subspeciesName) {
        return subspeciesName == null || subspeciesName.isBlank() ? speciesName : subspeciesName + " " + speciesName;
    }

    private static String withArticle(String noun) {
        return ("AEIOUaeiou".indexOf(noun.charAt(0)) >= 0 ? "an " : "a ") + noun;
    }

    private static List<String> singleton(String value) {
        return value == null ? List.of() : List.of(value);
    }

    private static List<JsonNode> list(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            return List.of(node);
        }
        return StreamSupport.stream(node.spliterator(), false).toList();
    }

    private static List<String> strings(JsonNode node) {
        return list(node).stream().filter(JsonNode::isValueNode).map(JsonNode::asString).toList();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }
}
