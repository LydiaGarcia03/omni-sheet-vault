package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import tools.jackson.databind.JsonNode;

/**
 * The spell half of build planning. Per class it asks once for every cantrip, known
 * spell or spellbook spell up to the class's level, following D&D Beyond's three caster
 * kinds (known, spellbook, prepared-from-the-whole-list), then which spells start
 * prepared. It also resolves 5etools {@code additionalSpells} grants from species,
 * backgrounds, feats and subclasses.
 */
final class Dnd5eSpellPlanner {

    /** How {@link Dnd5eBuildPlanner} records a choice and returns its valid selections. */
    @FunctionalInterface
    interface Offer {
        List<String> offer(String id, String type, String parentId, String prompt, String source, int count,
                List<CreationChoiceOption> options);
    }

    enum CasterKind { KNOWN, SPELLBOOK, PREPARED_FROM_LIST }

    private static final Map<String, String> ABILITY_KEYS = Map.of(
            "str", "strength", "dex", "dexterity", "con", "constitution",
            "int", "intelligence", "wis", "wisdom", "cha", "charisma");
    private static final List<String> GRANT_KINDS = List.of("known", "prepared", "innate");
    private static final List<String> POOLED_KINDS = List.of("cantrips", "spells", "spellbook");

    private final Dnd5eSpellLists lists;
    private final Dnd5eGrantState state;
    private final Dnd5eBuildOutcome outcome;
    private final Offer offer;
    private final Function<String, List<String>> answers;
    private final List<String> problems;
    private final Map<String, List<JsonNode>> expandedByClass = new HashMap<>();
    private final Set<String> claimed = new HashSet<>();
    private String sourceText;

    /**
     * A grant's frequency: its note label and, for a spell cast without a slot, its mode,
     * uses, recharge and shared pool. A null mode is a note only ("Ritual only", "Costs 2").
     */
    private record Frequency(String label, Dnd5eSpellUsage.Mode mode, int uses, Dnd5eRechargeTrigger recharge, String pool) {

        static Frequency atWill() {
            return new Frequency("At will", Dnd5eSpellUsage.Mode.AT_WILL, 0, null, null);
        }

        static Frequency note(String label) {
            return new Frequency(label, null, 0, null, null);
        }

        /** 5etools "daily"/"rest"/"resource" with a uses key such as "1" (shared) or "1e" (each spell). */
        static Frequency of(String kind, String usesKey, String pool) {
            boolean each = usesKey.endsWith("e");
            String count = each ? usesKey.substring(0, usesKey.length() - 1) : usesKey;
            Dnd5eRechargeTrigger recharge = switch (kind) {
                case "daily" -> Dnd5eRechargeTrigger.LONG_REST;
                case "rest" -> Dnd5eRechargeTrigger.SHORT_OR_LONG_REST;
                default -> null;
            };
            if (recharge == null || !count.chars().allMatch(Character::isDigit)) {
                return note(usageLabel(kind, usesKey));
            }
            return new Frequency(usageLabel(kind, usesKey), Dnd5eSpellUsage.Mode.LIMITED, Integer.parseInt(count), recharge,
                    each ? null : pool);
        }
    }

    Dnd5eSpellPlanner(CatalogueLookup catalogue, Dnd5eGrantState state, Dnd5eBuildOutcome outcome, Offer offer,
            Function<String, List<String>> answers, List<String> problems) {
        this.lists = new Dnd5eSpellLists(catalogue);
        this.state = state;
        this.outcome = outcome;
        this.offer = offer;
        this.answers = answers;
        this.problems = problems;
    }

    /** A class's own spellcasting, or its subclass's when the subclass is the caster (Eldritch Knight, Arcane Trickster). */
    static Optional<JsonNode> spellcasting(CatalogueRecord playerClass, CatalogueRecord subclass) {
        JsonNode own = playerClass.data().path("spellcasting");
        if (own.isObject() && own.hasNonNull("casterProgression")) {
            return Optional.of(own);
        }
        if (subclass != null) {
            JsonNode fromSubclass = subclass.data().path("spellcasting");
            if (fromSubclass.isObject() && fromSubclass.hasNonNull("casterProgression")) {
                return Optional.of(fromSubclass);
            }
        }
        return Optional.empty();
    }

    static CasterKind casterKind(JsonNode spellcasting) {
        if (spellcasting.hasNonNull("spellbookSpellsAddedByLevel")) {
            return CasterKind.SPELLBOOK;
        }
        return spellcasting.hasNonNull("preparedSpellsFormula") ? CasterKind.PREPARED_FROM_LIST : CasterKind.KNOWN;
    }

    // ---- class spells -------------------------------------------------------------

    /**
     * Before a class's levels are planned: the spells already picked in its pooled choices
     * count as known, so prerequisites checked along the way (Agonizing Blast needs Eldritch
     * Blast) see them, and other sources don't offer them again.
     */
    void startClass(CatalogueRecord playerClass) {
        claimed.clear();
        for (String kind : POOLED_KINDS) {
            for (String slug : answers.apply(pooledId(playerClass, kind))) {
                lists.bySlug(slug).filter(spell -> !state.knowsSpell(spell.name())).ifPresent(spell -> {
                    state.addSpell(spell.name());
                    claimed.add(spell.slug());
                });
            }
        }
    }

    /**
     * After a class's last level: one choice each for all its cantrips and all its known or
     * spellbook spells up to that level, then which spells start prepared.
     */
    void finishClass(CatalogueRecord playerClass, CatalogueRecord subclass, int level) {
        Optional<JsonNode> found = spellcasting(playerClass, subclass);
        if (found.isEmpty()) {
            return;
        }
        JsonNode spellcasting = found.get();
        String className = playerClass.name();
        String id = "class:" + playerClass.slug() + ":prepared";
        int maxLevel = maxSpellLevel(spellcasting, level);
        int prepareMax = prepareMax(spellcasting, level);
        JsonNode cantripTable = spellcasting.get("cantripsKnownByLevel");
        int cantrips = atLevel(cantripTable, level);
        if (cantrips > 0) {
            choose(pooledId(playerClass, "cantrips"), "CANTRIP", "Choose " + cantrips + " " + className + " cantrip(s)",
                    gainSource(className, increaseLevels(cantripTable, level)), cantrips,
                    classOptions(playerClass, subclass, level, 0, 0), className, false);
        }
        switch (casterKind(spellcasting)) {
            case KNOWN -> {
                JsonNode knownTable = spellcasting.get("spellsKnownByLevel");
                int known = atLevel(knownTable, level);
                if (known > 0 && maxLevel > 0) {
                    choose(pooledId(playerClass, "spells"), "SPELL", "Choose " + known + " " + className + " spell(s)",
                            gainSource(className, increaseLevels(knownTable, level)), known,
                            classOptions(playerClass, subclass, level, 1, maxLevel), className, false);
                }
            }
            case SPELLBOOK -> {
                JsonNode addedTable = spellcasting.get("spellbookSpellsAddedByLevel");
                int added = total(addedTable, level);
                List<String> book = added > 0 && maxLevel > 0
                        ? offer.offer(pooledId(playerClass, "spellbook"), "SPELLBOOK", null, "Add " + added + " spell(s) to your spellbook",
                                gainSource(className, additionLevels(addedTable, level)), added,
                                classOptions(playerClass, subclass, level, 1, maxLevel))
                        : List.of();
                List<CreationChoiceOption> options = book.stream().map(lists::bySlug).flatMap(Optional::stream)
                        .map(Dnd5eChoiceOptions::option).toList();
                List<String> prepared = options.isEmpty() ? List.of()
                        : offer.offer(id, "PREPARED_SPELL", null, "Choose " + Math.min(prepareMax, book.size()) + " spell(s) to prepare",
                                className, Math.min(prepareMax, book.size()), options);
                for (String slug : book) {
                    lists.bySlug(slug).ifPresent(spell -> state.addSpell(spell.name()));
                    outcome.addSpell(slug, className, prepared.contains(slug), false, null);
                }
            }
            case PREPARED_FROM_LIST -> {
                if (maxLevel > 0) {
                    choose(id, "PREPARED_SPELL", "Choose " + prepareMax + " spell(s) to prepare", className, prepareMax,
                            classOptions(playerClass, subclass, level, 1, maxLevel), className, true);
                }
            }
        }
    }

    // ---- granted spells ------------------------------------------------------------

    /**
     * Resolves one source's {@code additionalSpells} up to {@code level}: the character
     * level, or the owning class's level for a subclass. A subclass's spells are cast by
     * its class; any other source casts with the block's own ability, asked for when the
     * source offers a choice. {@code expanded} lists only widen the class's options.
     */
    void grant(String prefix, JsonNode additionalSpells, String source, int level, CatalogueRecord owningClass) {
        grant(prefix, additionalSpells, source, level, owningClass, null);
    }

    /** As above; {@code sourceText} is the granting source's description, read for spells it casts only on the caster. */
    void grant(String prefix, JsonNode additionalSpells, String source, int level, CatalogueRecord owningClass, String sourceText) {
        this.sourceText = sourceText;
        List<JsonNode> alternatives = new ArrayList<>();
        if (additionalSpells != null && additionalSpells.isArray()) {
            additionalSpells.forEach(alternatives::add);
        }
        if (alternatives.isEmpty()) {
            return;
        }
        JsonNode block = alternatives.getFirst();
        if (alternatives.size() > 1) {
            List<CreationChoiceOption> sets = new ArrayList<>();
            for (int index = 0; index < alternatives.size(); index++) {
                sets.add(new CreationChoiceOption(String.valueOf(index), alternatives.get(index).path("name").asString("Option " + (index + 1)), null, null));
            }
            List<String> selected = offer.offer(prefix + ":spell-set", "SPELL_SET", null, "Choose your spells", source, 1, sets);
            if (selected.isEmpty()) {
                return;
            }
            block = alternatives.get(Integer.parseInt(selected.getFirst()));
        }
        if (owningClass != null) {
            expandedByClass.computeIfAbsent(owningClass.slug(), key -> new ArrayList<>()).add(block.path("expanded"));
        }
        String owner = owningClass != null ? owningClass.name() : source;
        if (owningClass == null && !grantAbility(prefix, block.get("ability"), source)) {
            return;
        }
        for (String kind : GRANT_KINDS) {
            for (Map.Entry<String, JsonNode> byLevel : block.path(kind).properties()) {
                if (unlocked(byLevel.getKey(), level)) {
                    grantEntries(prefix + ":" + kind + ":" + byLevel.getKey(), kind, byLevel.getValue(), null, owner, source);
                }
            }
        }
    }

    /** Records the caster entry for a non-class source; false while its ability is still an unanswered choice. */
    private boolean grantAbility(String prefix, JsonNode ability, String source) {
        if (ability == null || ability.isNull()) {
            return true;
        }
        if (ability.isString()) {
            outcome.addGrantedCaster(source, ABILITY_KEYS.getOrDefault(ability.asString(), ability.asString()));
            return true;
        }
        List<CreationChoiceOption> options = new ArrayList<>();
        for (JsonNode code : ability.path("choose")) {
            String key = ABILITY_KEYS.getOrDefault(code.asString(), code.asString());
            options.add(new CreationChoiceOption(key, Dnd5eChoiceOptions.capitalize(key), null, null));
        }
        List<String> selected = offer.offer(prefix + ":spell-ability", "SPELLCASTING_ABILITY", null,
                "Choose the spellcasting ability for these spells", source, 1, options);
        if (selected.isEmpty()) {
            return false;
        }
        outcome.addGrantedCaster(source, selected.getFirst());
        return true;
    }

    /** One level's grant: a list of references and choices, or an object splitting them by usage ("_", "daily", "will"…). */
    private void grantEntries(String id, String kind, JsonNode entries, Frequency frequency, String owner, String source) {
        if (entries.isArray()) {
            int index = 0;
            for (JsonNode entry : entries) {
                grantEntry(id + ":" + index++, kind, entry, frequency, owner, source);
            }
            return;
        }
        for (Map.Entry<String, JsonNode> part : entries.properties()) {
            switch (part.getKey()) {
                case "_" -> grantEntries(id, kind, part.getValue(), frequency, owner, source);
                case "will" -> grantEntries(id + ":will", kind, part.getValue(), Frequency.atWill(), owner, source);
                case "ritual" -> grantEntries(id + ":ritual", kind, part.getValue(), Frequency.note("Ritual only"), owner, source);
                case "daily", "rest", "resource" -> {
                    for (Map.Entry<String, JsonNode> uses : part.getValue().properties()) {
                        String usesId = id + ":" + part.getKey() + ":" + uses.getKey();
                        grantEntries(usesId, kind, uses.getValue(), Frequency.of(part.getKey(), uses.getKey(), usesId), owner, source);
                    }
                }
                default -> problems.add("Unsupported spell grant '" + part.getKey() + "' from " + source);
            }
        }
    }

    private void grantEntry(String id, String kind, JsonNode entry, Frequency frequency, String owner, String source) {
        boolean prepared = kind.equals("prepared");
        String usage = frequency == null ? null : frequency.label();
        if (entry.isString()) {
            Optional<CatalogueRecord> spell = lists.byReference(entry.asString());
            if (spell.isEmpty()) {
                problems.add("Unknown spell '" + entry.asString() + "' granted by " + source);
                return;
            }
            record(spell.get(), owner, false, prepared, withCastLevel(usage, entry.asString()),
                    slotless(kind, frequency, spell.get(), entry.asString()));
            return;
        }
        JsonNode choose = entry.get("choose");
        if (choose == null) {
            problems.add("Unsupported spell grant " + entry + " from " + source);
            return;
        }
        int count = entry.path("count").asInt(choose.path("count").asInt(1));
        List<CatalogueRecord> candidates = new ArrayList<>();
        if (choose.isString()) {
            try {
                candidates.addAll(lists.matching(choose.asString()));
            } catch (IllegalArgumentException e) {
                problems.add(e.getMessage() + " (" + source + ")");
                return;
            }
        } else {
            choose.path("from").forEach(reference -> lists.byReference(reference.asString()).ifPresent(candidates::add));
        }
        List<CreationChoiceOption> options = candidates.stream()
                .filter(spell -> !state.knowsSpell(spell.name()))
                .map(Dnd5eChoiceOptions::option).toList();
        List<String> selected = offer.offer(id, "SPELL", null, "Choose " + count + " spell(s)", source, count, options);
        selected.forEach(slug -> lists.bySlug(slug).ifPresent(spell ->
                record(spell, owner, false, prepared, usage, slotless(kind, frequency, spell, null))));
    }

    /**
     * How a granted spell is cast without a slot: a plain innate grant (Armor of Shadows) or "will" is at will,
     * "daily"/"rest" spend uses. Known or prepared grants and cantrips use the normal casting rules (null).
     */
    private Dnd5eSpellUsage slotless(String kind, Frequency frequency, CatalogueRecord spell, String reference) {
        if (Dnd5eSpellLists.level(spell) == 0) {
            return null;
        }
        Integer castLevel = castLevel(reference);
        boolean selfOnly = sourceText != null
                && sourceText.toLowerCase(Locale.ROOT).contains(spell.name().toLowerCase(Locale.ROOT) + " on yourself");
        if (frequency == null) {
            return kind.equals("innate") ? Dnd5eSpellUsage.atWill(castLevel, selfOnly) : null;
        }
        if (frequency.mode() == null) {
            return null;
        }
        return frequency.mode() == Dnd5eSpellUsage.Mode.AT_WILL
                ? Dnd5eSpellUsage.atWill(castLevel, selfOnly)
                : Dnd5eSpellUsage.limited(frequency.uses(), frequency.recharge(), frequency.pool(), castLevel, selfOnly);
    }

    /** "hellish rebuke#2" is cast at 2nd level; null otherwise. */
    private static Integer castLevel(String reference) {
        if (reference == null) {
            return null;
        }
        int hash = reference.indexOf('#');
        String level = hash < 0 ? "" : reference.substring(hash + 1);
        return !level.isEmpty() && level.chars().allMatch(Character::isDigit) ? Integer.valueOf(level) : null;
    }

    // ---- helpers -------------------------------------------------------------------

    private void choose(String id, String type, String prompt, String source, int count, List<CreationChoiceOption> options,
            String owner, boolean prepared) {
        for (String slug : offer.offer(id, type, null, prompt, source, count, options)) {
            lists.bySlug(slug).ifPresent(spell -> record(spell, owner, prepared, false, null));
        }
    }

    private void record(CatalogueRecord spell, String owner, boolean prepared, boolean alwaysPrepared, String usage) {
        record(spell, owner, prepared, alwaysPrepared, usage, null);
    }

    private void record(CatalogueRecord spell, String owner, boolean prepared, boolean alwaysPrepared, String usage,
            Dnd5eSpellUsage slotless) {
        state.addSpell(spell.name());
        outcome.addSpell(spell.slug(), owner, prepared, alwaysPrepared, usage, slotless);
    }

    /** The class's own list, its subclass's list, and any expanded entries unlocked at this class level, within the spell-level range, minus spells already known. */
    private List<CreationChoiceOption> classOptions(CatalogueRecord playerClass, CatalogueRecord subclass, int classLevel, int minLevel, int maxLevel) {
        Map<String, CatalogueRecord> candidates = new LinkedHashMap<>();
        if (playerClass.data().path("spellcasting").hasNonNull("casterProgression")) {
            lists.classList(playerClass.name(), minLevel, maxLevel).forEach(spell -> candidates.put(spell.slug(), spell));
        }
        if (subclass != null && subclass.data().hasNonNull("shortName")) {
            lists.subclassList(playerClass.name(), subclass.data().get("shortName").asString(), minLevel, maxLevel)
                    .forEach(spell -> candidates.put(spell.slug(), spell));
        }
        for (JsonNode expanded : expandedByClass.getOrDefault(playerClass.slug(), List.of())) {
            for (Map.Entry<String, JsonNode> byLevel : expanded.properties()) {
                if (expandedUnlocked(byLevel.getKey(), classLevel, maxLevel)) {
                    expandedSpells(byLevel.getValue()).forEach(spell -> candidates.put(spell.slug(), spell));
                }
            }
        }
        return candidates.values().stream()
                .filter(spell -> Dnd5eSpellLists.level(spell) >= minLevel && Dnd5eSpellLists.level(spell) <= maxLevel)
                .filter(spell -> claimed.contains(spell.slug()) || !state.knowsSpell(spell.name()))
                .map(Dnd5eChoiceOptions::option)
                .toList();
    }

    private static String pooledId(CatalogueRecord playerClass, String kind) {
        return "class:" + playerClass.slug() + ":" + kind;
    }

    /** "Bard 1, 4, 10"; three or more consecutive levels collapse to a range ("Wizard 1–5"). */
    static String gainSource(String className, List<Integer> levels) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < levels.size()) {
            int end = start;
            while (end + 1 < levels.size() && levels.get(end + 1) == levels.get(end) + 1) {
                end++;
            }
            if (end - start >= 2) {
                parts.add(levels.get(start) + "–" + levels.get(end));
            } else {
                for (int index = start; index <= end; index++) {
                    parts.add(String.valueOf(levels.get(index)));
                }
            }
            start = end + 1;
        }
        return className + " " + String.join(", ", parts);
    }

    /** The class levels up to {@code level} at which a running-total table grows. */
    private static List<Integer> increaseLevels(JsonNode byLevel, int level) {
        List<Integer> levels = new ArrayList<>();
        for (int at = 1; at <= level; at++) {
            if (increase(byLevel, at) > 0) {
                levels.add(at);
            }
        }
        return levels;
    }

    /** The class levels up to {@code level} at which a per-level table adds something. */
    private static List<Integer> additionLevels(JsonNode byLevel, int level) {
        List<Integer> levels = new ArrayList<>();
        for (int at = 1; at <= level; at++) {
            if (atLevel(byLevel, at) > 0) {
                levels.add(at);
            }
        }
        return levels;
    }

    private static int total(JsonNode byLevel, int level) {
        int sum = 0;
        for (int at = 1; at <= level; at++) {
            sum += atLevel(byLevel, at);
        }
        return sum;
    }

    private List<CatalogueRecord> expandedSpells(JsonNode entries) {
        List<CatalogueRecord> spells = new ArrayList<>();
        for (JsonNode entry : entries) {
            if (entry.isString()) {
                lists.byReference(entry.asString()).ifPresent(spells::add);
            } else if (entry.hasNonNull("all")) {
                try {
                    spells.addAll(lists.matching(entry.get("all").asString()));
                } catch (IllegalArgumentException e) {
                    problems.add(e.getMessage());
                }
            }
        }
        return spells;
    }

    /** A level key: "_" always, a number at that level or later. */
    private static boolean unlocked(String key, int level) {
        return key.equals("_") || (key.chars().allMatch(Character::isDigit) && Integer.parseInt(key) <= level);
    }

    /** Expanded lists also use "s3" — available once the class can cast 3rd-level spells. */
    private static boolean expandedUnlocked(String key, int classLevel, int maxSpellLevel) {
        if (key.startsWith("s")) {
            return Integer.parseInt(key.substring(1)) <= maxSpellLevel;
        }
        return unlocked(key, classLevel);
    }

    /** The highest spell level the class's own table reaches at {@code level}; pact casters use their pact slot level. */
    static int maxSpellLevel(JsonNode spellcasting, int level) {
        JsonNode pact = spellcasting.path("pactSlotsByLevel");
        if (pact.isArray() && pact.size() >= level) {
            return pact.get(level - 1).path("slotLevel").asInt();
        }
        JsonNode slots = spellcasting.path("spellSlotsByLevel");
        if (!slots.isArray() || slots.size() < level) {
            return 0;
        }
        JsonNode row = slots.get(level - 1);
        int highest = 0;
        for (int index = 0; index < row.size(); index++) {
            if (row.get(index).asInt() > 0) {
                highest = index + 1;
            }
        }
        return highest;
    }

    private int prepareMax(JsonNode spellcasting, int level) {
        JsonNode formula = spellcasting.path("preparedSpellsFormula");
        int divisor = Math.max(1, formula.path("levelDivisor").asInt(1));
        String ability = spellcasting.path("ability").asString();
        return Math.max(1, level / divisor + Dnd5eFormulas.modifier(state.score(ability)));
    }

    private static int increase(JsonNode byLevel, int level) {
        return atLevel(byLevel, level) - (level > 1 ? atLevel(byLevel, level - 1) : 0);
    }

    private static int atLevel(JsonNode byLevel, int level) {
        return byLevel != null && byLevel.isArray() && byLevel.size() >= level ? byLevel.get(level - 1).asInt() : 0;
    }

    private static String usageLabel(String kind, String uses) {
        boolean each = uses.endsWith("e");
        String count = each ? uses.substring(0, uses.length() - 1) : uses;
        return switch (kind) {
            case "daily" -> count + "/day" + (each ? " each" : "");
            case "rest" -> count + "/rest" + (each ? " each" : "");
            default -> "Costs " + count;
        };
    }

    /** "hellish rebuke#2" is cast at 2nd level; "#c" only marks a cantrip. */
    private static String withCastLevel(String usage, String reference) {
        int hash = reference.indexOf('#');
        if (hash < 0 || !reference.substring(hash + 1).chars().allMatch(Character::isDigit)) {
            return usage;
        }
        String castAt = "cast at level " + reference.substring(hash + 1);
        return usage == null ? castAt : usage + ", " + castAt;
    }
}
