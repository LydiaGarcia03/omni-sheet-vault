package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.StartingItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Turns a fully planned build (its grant state and outcome) into the sheet's
 * build-derived fields, keeping every play-state field of the current sheet: hit
 * points spent, conditions, used slots and uses, prepared spells, item-granted spells,
 * inventory, coins, notes and the background's personal text. Hit points past level 1 use each class's average die,
 * {@code (faces / 2) + 1}, as 5etools prints it, unless the build rolled them.
 */
final class Dnd5eBuildMaterializer {

    private static final List<String> ABILITIES =
            List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
    private static final List<String> ARMOR_ORDER = List.of("light", "medium", "heavy", "shield");
    private static final Map<String, String> ARMOR_LABELS =
            Map.of("light", "Light", "medium", "Medium", "heavy", "Heavy", "shield", "Shields");
    private static final Set<String> SPELL_SLOT_PROGRESSIONS = Set.of("full", "1/2", "1/3", "artificer");
    private static final String FULL_CASTER_REFERENCE_CLASS = "wizard";
    private static final int MAX_SPECIAL_SENSE_RANGE = 120;
    private static final int SUMMARY_LENGTH = 140;
    private static final String BUILD_ACTION_PREFIX = "feature-action:";
    private static final JsonNode EMPTY_ROW = JsonNodeFactory.instance.arrayNode();

    private final Dnd5eCharacterBuild build;
    private final Dnd5eGrantState state;
    private final Dnd5eBuildOutcome outcome;
    private final CatalogueLookup catalogue;
    private final Dnd5eFeatureUses featureUses;

    Dnd5eBuildMaterializer(Dnd5eCharacterBuild build, Dnd5eGrantState state, Dnd5eBuildOutcome outcome, CatalogueLookup catalogue) {
        this.build = build;
        this.state = state;
        this.outcome = outcome;
        this.catalogue = catalogue;
        this.featureUses = new Dnd5eFeatureUses(outcome.features(), outcome.classes());
    }

    Dnd5eSheet materialize(Dnd5eSheet current) {
        Dnd5eSheet base = current == null ? blankSheet() : current;
        List<Dnd5eClassLevel> classLevels = classLevels(Dnd5eHitDicePools.normalized(base).classLevelsOrEmpty());
        List<Dnd5eDerivation.SourcedAmount> hitPoints = hitPoints();
        int hitPointBase = hitPoints.stream().mapToInt(Dnd5eDerivation.SourcedAmount::amount).sum();
        int level = build.characterLevel();
        JsonNode species = outcome.speciesMechanics();
        int speed = species == null ? base.speed() : species.path("speed").path("walk").asInt(base.speed());
        Set<String> expertSkills = new LinkedHashSet<>();
        state.expertiseWithSource().keySet().stream().filter(Dnd5eSkills.LABELS::containsKey).forEach(expertSkills::add);

        Dnd5eSheet materialized = new Dnd5eSheet(
                state.score("strength"), state.score("dexterity"), state.score("constitution"),
                state.score("intelligence"), state.score("wisdom"), state.score("charisma"),
                level, classLevels.getFirst().hitDieSize(), speed,
                Set.copyOf(state.keys(Dnd5eGrantState.Kind.SAVING_THROW)),
                Set.copyOf(state.keys(Dnd5eGrantState.Kind.SKILL)),
                armorLabels(), weaponLabels(), toolLabels(), languageLabels(),
                damageList("damageResistances", outcome.resistancePicks()),
                damageList("damageImmunities", List.of()),
                damageList("damageVulnerabilities", List.of()),
                damageList("conditionImmunities", List.of()),
                base.activeConditions(), base.exhaustionLevel(), base.currentHitPoints(), base.temporaryHitPoints(),
                base.maxHitPointsAdjustment(), base.heroicInspiration(),
                base.attacks(), featureActions(base.featureActions()), spellcastingClasses(), spells(base.spells()), base.items(),
                base.copperPieces(), base.silverPieces(), base.electrumPieces(), base.goldPieces(), base.platinumPieces(),
                featureTraits(base.featureTraits()), background(base.background()), base.extras(),
                hitDiceUsed(base, classLevels, level), spellSlots(base.spellSlots()), specialSenses(),
                base.customActions(),
                current == null ? build.preferences().encumbrance() == Dnd5ePreferences.Dnd5eEncumbrance.STANDARD : base.trackEncumbrance(),
                build, Set.copyOf(expertSkills), classLevels, hitPointBase,
                derivation(hitPoints, speed), base.conditionModifiers(), base.activeEffects(), Dnd5eSheet.SCHEMA_VERSION);

        int maxHitPoints = Dnd5eFormulas.maxHitPoints(materialized);
        int currentHitPoints = current == null ? maxHitPoints : Math.min(current.currentHitPoints(), maxHitPoints);
        return withCurrentHitPoints(materialized.withPlayStateOf(base), currentHitPoints);
    }

    List<StartingItem> startingItems() {
        return startingItems(outcome, build);
    }

    /** The build's starting items; a line whose {@link #lineKeys key} the build names in {@code equippedStartingItems} starts equipped. */
    static List<StartingItem> startingItems(Dnd5eBuildOutcome outcome, Dnd5eCharacterBuild build) {
        List<String> keys = lineKeys(outcome);
        List<StartingItem> items = new ArrayList<>();
        for (int index = 0; index < outcome.startingItems().size(); index++) {
            Dnd5eBuildOutcome.StartingItem item = outcome.startingItems().get(index);
            String key = keys.get(index);
            items.add(new StartingItem(item.itemSlug(), item.customName(), item.quantity(), item.displayName(),
                    key != null && build.equippedStartingItems().contains(key)));
        }
        return items;
    }

    /**
     * One key per starting-item line, so two lines of the same item are equipped apart: the catalogue slug plus
     * its occurrence among lines of that slug ({@code dagger#0}, {@code dagger#1}); null for a free-text item.
     */
    static List<String> lineKeys(Dnd5eBuildOutcome outcome) {
        Map<String, Integer> seen = new HashMap<>();
        List<String> keys = new ArrayList<>();
        for (Dnd5eBuildOutcome.StartingItem item : outcome.startingItems()) {
            if (item.itemSlug() == null) {
                keys.add(null);
                continue;
            }
            int occurrence = seen.merge(item.itemSlug(), 1, Integer::sum) - 1;
            keys.add(item.itemSlug() + "#" + occurrence);
        }
        return keys;
    }

    int startingCopper() {
        return outcome.startingCopper();
    }

    // ---- classes and hit points -----------------------------------------------------

    /** The per-class total; a current sheet without classes keeps its own total, spread later by {@link Dnd5eHitDicePools#normalized}. */
    private static int hitDiceUsed(Dnd5eSheet base, List<Dnd5eClassLevel> classLevels, int level) {
        int tracked = classLevels.stream().mapToInt(Dnd5eClassLevel::hitDiceUsed).sum();
        return base.classLevelsOrEmpty().isEmpty() ? Math.max(tracked, Math.min(base.hitDiceUsed(), level)) : tracked;
    }

    /** Each class keeps its spent hit dice from the current sheet, capped at its new level. */
    private List<Dnd5eClassLevel> classLevels(List<Dnd5eClassLevel> current) {
        return outcome.classes().stream().map(taken -> {
            int level = taken.buildClass().level();
            int used = current.stream()
                    .filter(existing -> existing.classSlug().equals(taken.playerClass().slug()))
                    .mapToInt(Dnd5eClassLevel::hitDiceUsed)
                    .findFirst()
                    .orElse(0);
            return new Dnd5eClassLevel(
                    taken.playerClass().slug(), taken.playerClass().name(),
                    taken.subclass() == null ? null : taken.subclass().slug(),
                    taken.subclass() == null ? null : taken.subclass().name(),
                    level, hitDie(taken.playerClass()), Math.min(used, level));
        }).toList();
    }

    /**
     * Level 1: the starting class's maximum die. Every later level: the class's average, or the build's
     * rolled result in order; a level not rolled yet (null) takes the average.
     */
    private List<Dnd5eDerivation.SourcedAmount> hitPoints() {
        List<Dnd5eDerivation.SourcedAmount> perLevel = new ArrayList<>();
        int rolledIndex = 0;
        boolean first = true;
        for (Dnd5eBuildOutcome.ClassOutcome taken : outcome.classes()) {
            int die = hitDie(taken.playerClass());
            for (int classLevel = 1; classLevel <= taken.buildClass().level(); classLevel++) {
                String label = taken.playerClass().name() + " " + classLevel;
                Integer rolled = rolledIndex < build.rolledHitPoints().size() ? build.rolledHitPoints().get(rolledIndex) : null;
                if (first) {
                    perLevel.add(new Dnd5eDerivation.SourcedAmount(label + " (maximum d" + die + ")", die));
                    first = false;
                    continue;
                }
                rolledIndex++;
                if (build.hitPointMethod() == Dnd5eHitPointMethod.ROLLED && rolled != null) {
                    perLevel.add(new Dnd5eDerivation.SourcedAmount(label + " (rolled d" + die + ")", rolled));
                } else {
                    perLevel.add(new Dnd5eDerivation.SourcedAmount(label + " (average d" + die + ")", die / 2 + 1));
                }
            }
        }
        return perLevel;
    }

    private static int hitDie(CatalogueRecord playerClass) {
        return playerClass.data().path("hitDie").asInt(8);
    }

    // ---- proficiency labels ---------------------------------------------------------

    private List<String> armorLabels() {
        Set<String> armor = state.keys(Dnd5eGrantState.Kind.ARMOR);
        List<String> labels = new ArrayList<>();
        ARMOR_ORDER.stream().filter(armor::contains).forEach(key -> labels.add(armorLabel(key)));
        armor.stream().filter(key -> !ARMOR_ORDER.contains(key)).forEach(key -> labels.add(armorLabel(key)));
        return labels;
    }

    private List<String> weaponLabels() {
        List<String> categories = new ArrayList<>();
        List<String> items = new ArrayList<>();
        for (String key : state.keys(Dnd5eGrantState.Kind.WEAPON)) {
            (isWeaponCategory(key) ? categories : items).add(weaponLabel(key));
        }
        items.sort(Comparator.naturalOrder());
        categories.addAll(items);
        return categories;
    }

    private List<String> toolLabels() {
        List<String> labels = new ArrayList<>(state.keys(Dnd5eGrantState.Kind.TOOL).stream().map(this::toolLabel).toList());
        labels.sort(Comparator.naturalOrder());
        return labels;
    }

    /** 5etools' "other" language grant (a species' own tongue, named only in prose) has no catalogue entry and is left out. */
    private List<String> languageLabels() {
        List<String> labels = new ArrayList<>(state.keys(Dnd5eGrantState.Kind.LANGUAGE).stream()
                .filter(key -> !key.equals("other"))
                .map(this::languageLabel)
                .toList());
        labels.sort(Comparator.naturalOrder());
        return labels;
    }

    private static String armorLabel(String key) {
        return ARMOR_LABELS.getOrDefault(key, Dnd5eChoiceOptions.capitalize(key));
    }

    private static boolean isWeaponCategory(String key) {
        return key.equals("simple") || key.equals("martial") || key.equals("firearms");
    }

    private String weaponLabel(String key) {
        return isWeaponCategory(key) ? Dnd5eChoiceOptions.capitalize(key) : catalogueName("ITEM", key);
    }

    private String toolLabel(String key) {
        String label = catalogueName("ITEM", key);
        return state.hasExpertise(key) ? label + " (Expertise)" : label;
    }

    /** 5etools grants name a language by its bare key ("elvish"); the catalogue keeps one entry per source, so the PHB one is tried next. */
    private String languageLabel(String key) {
        return catalogue.find("LANGUAGE", key).or(() -> catalogue.find("LANGUAGE", key + "-phb")).map(CatalogueRecord::name)
                .orElseGet(() -> catalogueName("LANGUAGE", key));
    }

    /** The label the sheet's proficiency lists show for a grant; null for kinds shown elsewhere (skills, saves). */
    private String grantLabel(Dnd5eGrantState.Kind kind, String key) {
        return switch (kind) {
            case ARMOR -> armorLabel(key);
            case WEAPON -> weaponLabel(key);
            case TOOL -> toolLabel(key);
            case LANGUAGE -> key.equals("other") ? null : languageLabel(key);
            case SKILL, SAVING_THROW -> null;
        };
    }

    /** The catalogue's own name; a grant with no catalogue entry is labelled from its key ("vehicles-land" → "Vehicles (Land)"). */
    private String catalogueName(String kind, String slug) {
        if (slug.startsWith("vehicles-")) {
            return "Vehicles (" + titleCase(slug.substring("vehicles-".length())) + ")";
        }
        return catalogue.find(kind, slug).map(CatalogueRecord::name).orElse(titleCase(slug));
    }

    /** The species' defenses by sheet field, all granted by the species. */
    private Map<String, List<String>> speciesDefenses() {
        Map<String, List<String>> defenses = new LinkedHashMap<>();
        defenses.put("damageResistances", damageList("damageResistances", outcome.resistancePicks()));
        defenses.put("damageImmunities", damageList("damageImmunities", List.of()));
        defenses.put("damageVulnerabilities", damageList("damageVulnerabilities", List.of()));
        defenses.put("conditionImmunities", damageList("conditionImmunities", List.of()));
        return defenses;
    }

    private List<String> damageList(String field, List<String> picks) {
        List<String> labels = new ArrayList<>();
        JsonNode species = outcome.speciesMechanics();
        if (species != null) {
            species.path(field).path("fixed").forEach(type -> labels.add(Dnd5eChoiceOptions.capitalize(type.asString())));
        }
        picks.forEach(type -> labels.add(Dnd5eChoiceOptions.capitalize(type)));
        return labels;
    }

    private List<Dnd5eSpecialSense> specialSenses() {
        List<Dnd5eSpecialSense> senses = new ArrayList<>();
        JsonNode species = outcome.speciesMechanics();
        if (species == null) {
            return senses;
        }
        for (JsonNode sense : species.path("senses")) {
            Dnd5eSenseType type = Dnd5eSenseType.valueOf(sense.path("type").asString().toUpperCase(Locale.ROOT));
            senses.add(new Dnd5eSpecialSense(type, Math.min(sense.path("range").asInt(), MAX_SPECIAL_SENSE_RANGE)));
        }
        return senses;
    }

    // ---- features and background ----------------------------------------------------

    /**
     * Keeps an existing feature's used count when its key survives the re-materialization,
     * capped at its new maximum. A trait owning a resource gets its counter and recharge;
     * a die-based resource names its die in the summary.
     */
    private List<Dnd5eFeatureTrait> featureTraits(List<Dnd5eFeatureTrait> current) {
        Map<String, Integer> usedByKey = new HashMap<>();
        current.forEach(trait -> usedByKey.put(trait.key(), trait.usedCount()));
        Map<String, Dnd5eFeatureTrait> traits = new LinkedHashMap<>();
        for (Dnd5eBuildOutcome.FeatureOutcome feature : outcome.features()) {
            Dnd5eFeatureUses.Resource resource = featureUses.ownedBy(feature.key());
            Integer maxUses = resource == null ? null : resource.maxUses();
            int used = Math.min(usedByKey.getOrDefault(feature.key(), 0), maxUses == null ? 0 : maxUses);
            String summary = resource != null && resource.die() > 0 ? "d" + resource.die() + " × " + maxUses + ". " + summary(feature) : summary(feature);
            traits.putIfAbsent(feature.key(), new Dnd5eFeatureTrait(feature.key(), feature.name(), feature.category(),
                    feature.source() == null ? feature.name() : feature.source(), summary,
                    feature.description(), maxUses, used, resource == null ? null : resource.recharge(), modifiers(feature),
                    outcome.featureChoices(feature.key())));
        }
        return List.copyOf(traits.values());
    }

    /**
     * Build-derived Actions tab rows, one per feature whose overlay names an action type,
     * linked to the trait holding its counter. Hand-made actions on the current sheet stay.
     */
    private List<Dnd5eFeatureAction> featureActions(List<Dnd5eFeatureAction> current) {
        List<Dnd5eFeatureAction> actions = new ArrayList<>();
        for (Dnd5eBuildOutcome.FeatureOutcome feature : outcome.features()) {
            JsonNode action = feature.mechanics() == null ? null : feature.mechanics().get("action");
            if (action == null || action.isNull()) {
                continue;
            }
            actions.add(new Dnd5eFeatureAction(BUILD_ACTION_PREFIX + feature.key(), feature.name(),
                    Dnd5eActionType.valueOf(action.path("type").asString()), feature.description(), null, 0, null,
                    featureUses.counterKeyFor(feature.key())));
        }
        current.stream().filter(action -> !action.key().startsWith(BUILD_ACTION_PREFIX)).forEach(actions::add);
        return List.copyOf(actions);
    }

    /** The catalogue's modifiers, labelled with the feature's name; a class-scoped one records the class it counts. */
    private static List<Dnd5eModifier> modifiers(Dnd5eBuildOutcome.FeatureOutcome feature) {
        List<Dnd5eModifier> modifiers = new ArrayList<>();
        if (feature.mechanics() == null || !feature.mechanics().path("modifiers").isArray()) {
            return modifiers;
        }
        for (JsonNode modifier : feature.mechanics().get("modifiers")) {
            boolean classScoped = "CLASS".equals(modifier.path("levelScope").asString(""));
            modifiers.add(new Dnd5eModifier(
                    Dnd5eModifierType.valueOf(modifier.path("type").asString()),
                    Dnd5eModifierTarget.valueOf(modifier.path("target").asString()),
                    modifier.path("value").asInt(),
                    modifier.hasNonNull("ability") ? modifier.get("ability").asString() : null,
                    classScoped ? feature.classSlug() : null,
                    feature.name(),
                    modifier.hasNonNull("restriction") ? modifier.get("restriction").asString() : null));
        }
        return modifiers;
    }

    /** The description's first sentence, trimmed to fit a row; the name when there's no description. */
    private static String summary(Dnd5eBuildOutcome.FeatureOutcome feature) {
        String description = feature.description() == null ? "" : feature.description().strip();
        if (description.isEmpty()) {
            return feature.name();
        }
        int sentenceEnd = description.indexOf(". ");
        String firstSentence = sentenceEnd > 0 ? description.substring(0, sentenceEnd + 1) : description;
        return firstSentence.length() <= SUMMARY_LENGTH ? firstSentence : firstSentence.substring(0, SUMMARY_LENGTH - 1) + "…";
    }

    private Dnd5eBackground background(Dnd5eBackground current) {
        CatalogueRecord background = outcome.background();
        if (background == null) {
            return current;
        }
        JsonNode feature = background.data().path("features").path(0);
        return new Dnd5eBackground(background.name(), feature.path("name").asString(""), feature.path("description").asString(""),
                current.alignment(), current.personalityTraits(), current.ideals(), current.bonds(), current.flaws(),
                current.appearance(), current.organizations(), current.allies(), current.enemies(), current.backstory(),
                current.other(), current.gender(), current.eyes(), current.size(), current.height(), current.faith(),
                current.hair(), current.skin(), current.age(), current.weight(), current.lifestyle());
    }

    // ---- spellcasting ---------------------------------------------------------------

    /**
     * The build's spells, copied from the catalogue. A spell still owned by the same
     * class keeps its current prepared flag; item-granted spells are kept as they are.
     */
    private List<Dnd5eSpell> spells(List<Dnd5eSpell> current) {
        Map<String, Dnd5eSpell> currentByKey = new HashMap<>();
        current.forEach(spell -> currentByKey.put(spell.key(), spell));
        Map<String, Dnd5eSpell> spells = new LinkedHashMap<>();
        for (Dnd5eBuildOutcome.SpellOutcome granted : outcome.spells()) {
            Optional<CatalogueRecord> found = catalogue.find("SPELL", granted.spellSlug());
            if (found.isEmpty() || spells.containsKey(granted.spellSlug())) {
                continue;
            }
            Dnd5eSpell existing = currentByKey.get(granted.spellSlug());
            boolean prepared = existing != null && existing.className().equals(granted.owner()) && existing.grantedByItemKey() == null
                    ? existing.prepared()
                    : granted.prepared();
            spells.put(granted.spellSlug(), spell(found.get(), granted, prepared, usedUses(existing, granted)));
        }
        current.stream().filter(spell -> spell.grantedByItemKey() != null).forEach(spell -> spells.putIfAbsent(spell.key(), spell));
        return List.copyOf(spells.values());
    }

    /** Uses already spent on a limited spell survive a re-apply while its grant is unchanged. */
    private static int usedUses(Dnd5eSpell existing, Dnd5eBuildOutcome.SpellOutcome granted) {
        Dnd5eSpellUsage before = existing == null ? null : existing.usage();
        Dnd5eSpellUsage after = granted.slotless();
        if (before == null || after == null || before.mode() != after.mode() || before.recharge() != after.recharge()) {
            return 0;
        }
        return before.usedUses();
    }

    private static Dnd5eSpell spell(CatalogueRecord record, Dnd5eBuildOutcome.SpellOutcome granted, boolean prepared, int usedUses) {
        JsonNode data = record.data();
        String notes = data.path("notes").asString("");
        Dnd5eSpellUsage usage = granted.slotless() == null ? null : granted.slotless().withUsedUses(usedUses);
        return new Dnd5eSpell(record.slug(), record.name(), granted.owner(), data.path("level").asInt(),
                data.path("school").asString(), data.path("castingTime").asString(), data.path("range").asString(),
                data.path("concentration").asBoolean(), data.path("ritual").asBoolean(), data.path("attackRoll").asBoolean(),
                nullableInt(data, "damageDiceCount"), nullableInt(data, "damageDiceSides"), nullableText(data, "damageType"),
                granted.usage() == null ? notes : granted.usage() + "; " + notes,
                data.path("effectSummary").asString("Buff"), prepared, granted.alwaysPrepared(), record.description(),
                nullableText(data, "saveAbility"), data.path("components").asString(), nullableText(data, "materialComponent"),
                data.path("duration").asString(), nullableText(data, "higherLevelsDescription"),
                nullableInt(data, "higherLevelsDamageDiceCount"), nullableInt(data, "higherLevelsDamageDiceSides"),
                null, null, null, usage);
    }

    private static Integer nullableInt(JsonNode data, String field) {
        return data.hasNonNull(field) ? data.get(field).asInt() : null;
    }

    private static String nullableText(JsonNode data, String field) {
        return data.hasNonNull(field) ? data.get(field).asString() : null;
    }

    /** Each spellcasting class, then each non-class source that casts its own granted spells ("High Elf", INT). */
    private List<Dnd5eSpellcastingClass> spellcastingClasses() {
        List<Dnd5eSpellcastingClass> classes = new ArrayList<>();
        for (Dnd5eBuildOutcome.ClassOutcome taken : outcome.classes()) {
            Dnd5eSpellPlanner.spellcasting(taken.playerClass(), taken.subclass()).ifPresent(spellcasting -> {
                int level = taken.buildClass().level();
                String ability = spellcasting.path("ability").asString();
                JsonNode formula = spellcasting.get("preparedSpellsFormula");
                boolean prepared = formula != null && !formula.isNull();
                Integer prepareMax = prepared
                        ? Math.max(1, level / formula.path("levelDivisor").asInt(1) + Dnd5eFormulas.modifier(state.score(ability)))
                        : null;
                classes.add(new Dnd5eSpellcastingClass(taken.playerClass().name(), ability,
                        prepared ? Dnd5eSpellCastingType.PREPARED : Dnd5eSpellCastingType.KNOWN,
                        atLevel(spellcasting.get("cantripsKnownByLevel"), level).orElse(0),
                        atLevel(spellcasting.get("spellsKnownByLevel"), level).orElse(null), prepareMax));
            });
        }
        for (Dnd5eBuildOutcome.GrantedCaster caster : outcome.grantedCasters()) {
            List<Dnd5eBuildOutcome.SpellOutcome> owned = outcome.spells().stream()
                    .filter(spell -> spell.owner().equals(caster.owner())).toList();
            if (!owned.isEmpty()) {
                int cantrips = (int) owned.stream().filter(spell -> spellLevel(spell.spellSlug()) == 0).count();
                classes.add(new Dnd5eSpellcastingClass(caster.owner(), caster.abilityKey(), Dnd5eSpellCastingType.KNOWN,
                        cantrips, owned.size() - cantrips, null));
            }
        }
        return classes;
    }

    private int spellLevel(String spellSlug) {
        return catalogue.find("SPELL", spellSlug).map(Dnd5eSpellLists::level).orElse(0);
    }

    /**
     * One caster uses its own slot table. Several combine by the PHB multiclass rule —
     * full-caster levels, half of half-caster levels, a third of third-caster levels
     * (the Artificer rounds up) — read against a full caster's own table. Used slots are
     * kept, capped at the new maximum. A Pact Magic class adds its own pool after them.
     */
    private List<Dnd5eSpellSlotLevel> spellSlots(List<Dnd5eSpellSlotLevel> current) {
        List<Dnd5eSpellSlotLevel> slots = new ArrayList<>(spellcastingSlots(current.stream().filter(slot -> !slot.pact()).toList()));
        pactSlots(current).ifPresent(slots::add);
        return slots;
    }

    /** The Pact Magic pool of the class that has one (PHB: Warlock), from its level's row; its used slots are kept. */
    private Optional<Dnd5eSpellSlotLevel> pactSlots(List<Dnd5eSpellSlotLevel> current) {
        for (Dnd5eBuildOutcome.ClassOutcome taken : outcome.classes()) {
            Optional<JsonNode> row = Dnd5eSpellPlanner.spellcasting(taken.playerClass(), taken.subclass())
                    .flatMap(spellcasting -> atRow(spellcasting.path("pactSlotsByLevel"), taken.buildClass().level()));
            if (row.isPresent() && row.get().path("slots").asInt() > 0) {
                int level = row.get().path("slotLevel").asInt();
                int max = row.get().path("slots").asInt();
                int used = current.stream().filter(Dnd5eSpellSlotLevel::pact).mapToInt(Dnd5eSpellSlotLevel::usedSlots).findFirst().orElse(0);
                return Optional.of(new Dnd5eSpellSlotLevel(level, max, Math.min(used, max), true, taken.playerClass().name()));
            }
        }
        return Optional.empty();
    }

    private static Optional<JsonNode> atRow(JsonNode byLevel, int level) {
        return byLevel.isArray() && level >= 1 && byLevel.size() >= level ? Optional.of(byLevel.get(level - 1)) : Optional.empty();
    }

    private List<Dnd5eSpellSlotLevel> spellcastingSlots(List<Dnd5eSpellSlotLevel> current) {
        List<JsonNode> casters = new ArrayList<>();
        int casterLevel = 0;
        for (Dnd5eBuildOutcome.ClassOutcome taken : outcome.classes()) {
            Optional<JsonNode> spellcasting = Dnd5eSpellPlanner.spellcasting(taken.playerClass(), taken.subclass());
            if (spellcasting.isEmpty() || !SPELL_SLOT_PROGRESSIONS.contains(spellcasting.get().path("casterProgression").asString(""))) {
                continue;
            }
            int level = taken.buildClass().level();
            casters.add(slotRow(spellcasting.get(), level));
            casterLevel += switch (spellcasting.get().path("casterProgression").asString()) {
                case "full" -> level;
                case "1/2" -> level / 2;
                case "1/3" -> level / 3;
                default -> (level + 1) / 2;
            };
        }
        JsonNode row;
        if (casters.isEmpty()) {
            return List.of();
        } else if (casters.size() == 1) {
            row = casters.getFirst();
        } else {
            int combinedCasterLevel = casterLevel;
            row = catalogue.find("CLASS", FULL_CASTER_REFERENCE_CLASS)
                    .map(reference -> slotRow(reference.data().path("spellcasting"), combinedCasterLevel))
                    .orElse(null);
            if (row == null) {
                return List.of();
            }
        }
        Map<Integer, Integer> usedByLevel = new HashMap<>();
        current.forEach(slot -> usedByLevel.put(slot.level(), slot.usedSlots()));
        List<Dnd5eSpellSlotLevel> slots = new ArrayList<>();
        for (int index = 0; index < row.size(); index++) {
            int max = row.get(index).asInt();
            if (max > 0) {
                slots.add(new Dnd5eSpellSlotLevel(index + 1, max, Math.min(usedByLevel.getOrDefault(index + 1, 0), max)));
            }
        }
        return slots;
    }

    private static JsonNode slotRow(JsonNode spellcasting, int level) {
        JsonNode table = spellcasting.path("spellSlotsByLevel");
        return level >= 1 && table.size() >= level ? table.get(level - 1) : EMPTY_ROW;
    }

    private static Optional<Integer> atLevel(JsonNode byLevel, int level) {
        if (byLevel == null || byLevel.isNull() || byLevel.size() < level) {
            return Optional.empty();
        }
        return Optional.of(byLevel.get(level - 1).asInt());
    }

    // ---- provenance -----------------------------------------------------------------

    private Dnd5eDerivation derivation(List<Dnd5eDerivation.SourcedAmount> hitPoints, int speed) {
        Map<String, List<Dnd5eDerivation.SourcedAmount>> abilities = new LinkedHashMap<>();
        ABILITIES.forEach(ability -> abilities.put(ability, List.copyOf(state.abilityContributions().getOrDefault(ability, List.of()))));
        List<Dnd5eDerivation.SourcedGrant> grants = new ArrayList<>();
        for (Dnd5eGrantState.Kind kind : Dnd5eGrantState.Kind.values()) {
            state.proficienciesWithSource(kind).forEach((key, source) ->
                    grants.add(new Dnd5eDerivation.SourcedGrant(kind.name(), key, source, grantLabel(kind, key))));
        }
        state.expertiseWithSource().forEach((key, source) -> grants.add(new Dnd5eDerivation.SourcedGrant("EXPERTISE", key, source, null)));
        JsonNode species = outcome.speciesMechanics();
        if (species != null) {
            for (JsonNode sense : species.path("senses")) {
                grants.add(new Dnd5eDerivation.SourcedGrant("SENSE", sense.path("type").asString() + ":" + sense.path("range").asInt(),
                        outcome.speciesLabel(), null));
            }
        }
        for (Map.Entry<String, List<String>> defenses : speciesDefenses().entrySet()) {
            defenses.getValue().forEach(name -> grants.add(new Dnd5eDerivation.SourcedGrant(
                    "DEFENSE", defenses.getKey() + ":" + name, outcome.speciesLabel(), null)));
        }
        return new Dnd5eDerivation(abilities, List.copyOf(hitPoints),
                List.of(new Dnd5eDerivation.SourcedAmount(outcome.speciesLabel() == null ? "Base" : outcome.speciesLabel(), speed)),
                List.copyOf(grants));
    }

    // ---- helpers --------------------------------------------------------------------

    private static String titleCase(String slug) {
        StringBuilder label = new StringBuilder();
        for (String word : slug.split("-")) {
            if (!word.isEmpty()) {
                if (!label.isEmpty()) {
                    label.append(' ');
                }
                label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return label.toString();
    }

    private static Dnd5eSheet withCurrentHitPoints(Dnd5eSheet sheet, int currentHitPoints) {
        return sheet.withSessionState(currentHitPoints, sheet.temporaryHitPoints(), sheet.heroicInspiration(), sheet.activeConditions());
    }

    private static Dnd5eSheet blankSheet() {
        Dnd5eBackground background = new Dnd5eBackground(
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
        return new Dnd5eSheet(10, 10, 10, 10, 10, 10, 1, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), background, List.of(), 0, List.of(), List.of(), List.of(), false,
                null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
    }

}
