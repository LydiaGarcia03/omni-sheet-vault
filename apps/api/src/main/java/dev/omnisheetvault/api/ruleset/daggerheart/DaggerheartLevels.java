package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.CLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.DOMAIN_CARD;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.SUBCLASS;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Daggerheart leveling (SRD pp. 53–54) and the advancement grid of the official character sheet (Darrington Press,
 * 2026): each level after the first takes the tier achievement, two advancements from its tier or below, and a new
 * domain card. A level entry in the build reads:
 * <pre>{"experience":"…","advancements":[{"option":"traits","tier":2,"traits":["agility","finesse"]}],"domainCard":"…"}</pre>
 * {@link #walk} replays the levels in order and reports what each one adds and what breaks a rule.
 */
final class DaggerheartLevels {

    static final int MAX_LEVEL = 10;
    static final int PICKS_PER_LEVEL = 2;
    static final int NEW_EXPERIENCE_BONUS = 2;
    static final int MAX_SLOTS = 12;
    static final int SUBCLASS_UPGRADES = 2;
    /** The levels whose tier achievement adds an Experience and Proficiency; 5 and 8 also clear trait marks. */
    static final Set<Integer> ACHIEVEMENT_LEVELS = Set.of(2, 5, 8);
    static final Set<Integer> MARK_CLEARING_LEVELS = Set.of(5, 8);
    static final int MULTICLASS_LEVEL = 5;

    /** One advancement of the sheet's grid: its slots in each tier from {@code minTier}, and whether it takes both picks. */
    enum Option {
        TRAITS("traits", "Gain a +1 bonus to two unmarked character traits and mark them", 3, 2, false),
        HIT_POINT("hitPoint", "Permanently gain one Hit Point slot", 2, 2, false),
        STRESS("stress", "Permanently gain one Stress slot", 2, 2, false),
        EXPERIENCES("experiences", "Permanently gain a +1 bonus to two Experiences", 1, 2, false),
        DOMAIN_CARD("domainCard", "Choose an additional domain card of your level or lower", 1, 2, false),
        EVASION("evasion", "Permanently gain a +1 bonus to your Evasion", 1, 2, false),
        SUBCLASS("subclass", "Take an upgraded subclass card", 1, 3, false),
        PROFICIENCY("proficiency", "Increase your Proficiency by +1", 1, 3, true),
        MULTICLASS("multiclass", "Multiclass: choose an additional class", 1, 3, true);

        final String id;
        final String label;
        final int slots;
        final int minTier;
        final boolean takesBothPicks;

        Option(String id, String label, int slots, int minTier, boolean takesBothPicks) {
            this.id = id;
            this.label = label;
            this.slots = slots;
            this.minTier = minTier;
            this.takesBothPicks = takesBothPicks;
        }

        static Optional<Option> of(String id) {
            return Arrays.stream(values()).filter(option -> option.id.equals(id)).findFirst();
        }
    }

    /** The highest card level the extra-card advancement reaches in each tier ("up to level 4", "up to level 7"). */
    static int extraCardCap(int tier) {
        return switch (tier) {
            case 2 -> 4;
            case 3 -> 7;
            default -> MAX_LEVEL;
        };
    }

    /** Tier 1 is level 1; tiers 2, 3 and 4 start at levels 2, 5 and 8. */
    static int tier(int level) {
        return level <= 1 ? 1 : level <= 4 ? 2 : level <= 7 ? 3 : 4;
    }

    /** A multiclass domain gives cards up to half the character's level, rounded up. */
    static int multiclassCardCap(int level) {
        return (level + 1) / 2;
    }

    /** What the levels add up to, and the rules they break. */
    static final class Walk {
        final List<String> problems = new ArrayList<>();
        /** Marks per tier and option id. */
        final Map<Integer, Map<String, Integer>> marked = new HashMap<>();
        final List<String> newExperiences = new ArrayList<>();
        /** Experience index → bonus gained. */
        final Map<Integer, Integer> experienceBonuses = new HashMap<>();
        final List<Gain> gains = new ArrayList<>();
        final List<String> cards = new ArrayList<>();
        int proficiency;
        int subclassUpgrades;
        Multiclass multiclass;
        /** Traits marked now, which a trait advancement can't raise until the next clearing level. */
        final Set<String> markedTraits = new LinkedHashSet<>();
        final Set<Integer> tiersWithSubclass = new HashSet<>();
        Integer multiclassTier;

        int marks(int tier, Option option) {
            return marked.getOrDefault(tier, Map.of()).getOrDefault(option.id, 0);
        }
    }

    /** One always-on bonus an advancement gives, for the derived values. */
    record Gain(int level, String target, int value) {
    }

    record Multiclass(CatalogueRecord playerClass, String domain, CatalogueRecord subclass) {
    }

    private DaggerheartLevels() {
    }

    /** Replays the build's levels (plus {@code draft} as the next one, when given) against the rules. */
    static Walk walk(DaggerheartBuild build, DaggerheartCatalogue catalogue, JsonNode draft) {
        Walk walk = new Walk();
        List<JsonNode> levels = new ArrayList<>(build.levels());
        if (draft != null && draft.isObject()) {
            levels.add(draft);
        }
        Set<String> classDomains = new HashSet<>();
        catalogue.find(CLASS, build.classSlug()).ifPresent(entry -> entry.data().path("domains").forEach(domain -> classDomains.add(domain.asString())));
        walk.cards.addAll(build.domainCards());
        int experiences = (int) build.experiences().stream().filter(name -> !name.isEmpty()).count();
        List<String> experienceNames = new ArrayList<>(build.experiences().stream().filter(name -> !name.isEmpty()).toList());

        for (int index = 0; index < levels.size(); index++) {
            int level = index + 2;
            int tier = tier(level);
            JsonNode entry = levels.get(index);
            String at = "Level " + level + ": ";
            if (level > MAX_LEVEL) {
                walk.problems.add("The highest level is " + MAX_LEVEL + ".");
                break;
            }
            if (MARK_CLEARING_LEVELS.contains(level)) {
                walk.markedTraits.clear();
            }
            if (ACHIEVEMENT_LEVELS.contains(level)) {
                walk.proficiency++;
                String name = DaggerheartBuild.text(entry, "experience");
                if (!name.isEmpty()) {
                    if (experienceNames.stream().anyMatch(existing -> existing.equalsIgnoreCase(name))) {
                        walk.problems.add(at + "the new Experience \"" + name + "\" repeats one you have.");
                    }
                    experienceNames.add(name);
                    walk.newExperiences.add(name);
                    experiences++;
                }
            }
            int picks = 0;
            for (JsonNode advancement : entry.path("advancements")) {
                Optional<Option> found = Option.of(advancement.path("option").asString(""));
                if (found.isEmpty()) {
                    walk.problems.add(at + "unknown advancement \"" + advancement.path("option").asString("") + "\".");
                    continue;
                }
                Option option = found.get();
                picks += option.takesBothPicks ? 2 : 1;
                int fromTier = advancement.path("tier").asInt(tier);
                if (fromTier < 2 || fromTier > tier) {
                    walk.problems.add(at + "advancements come from Tier 2 up to your tier (" + tier + ").");
                    continue;
                }
                if (fromTier < option.minTier) {
                    walk.problems.add(at + "\"" + option.label + "\" starts at Tier " + option.minTier + ".");
                    continue;
                }
                Map<String, Integer> tierMarks = walk.marked.computeIfAbsent(fromTier, key -> new HashMap<>());
                int used = tierMarks.merge(option.id, 1, Integer::sum);
                if (used > option.slots) {
                    walk.problems.add(at + "Tier " + fromTier + " has " + option.slots + " slot" + (option.slots == 1 ? "" : "s") + " for \"" + option.label + "\".");
                }
                apply(walk, option, advancement, level, fromTier, at, experiences, classDomains, catalogue, build);
            }
            if (picks > PICKS_PER_LEVEL) {
                walk.problems.add(at + "a level takes " + PICKS_PER_LEVEL + " advancements; Proficiency and multiclassing take both.");
            }
            String card = DaggerheartBuild.text(entry, "domainCard");
            if (!card.isEmpty()) {
                checkCard(walk, card, level, level, classDomains, catalogue, at);
            }
        }
        return walk;
    }

    private static void apply(Walk walk, Option option, JsonNode advancement, int level, int fromTier, String at, int experiences,
            Set<String> classDomains, DaggerheartCatalogue catalogue, DaggerheartBuild build) {
        switch (option) {
            case TRAITS -> {
                List<String> traits = new ArrayList<>();
                advancement.path("traits").forEach(trait -> traits.add(trait.asString("")));
                if (traits.size() != 2 || traits.get(0).equals(traits.get(1)) || !DaggerheartBuild.TRAITS.containsAll(traits)) {
                    walk.problems.add(at + "the trait advancement raises two different traits.");
                    return;
                }
                for (String trait : traits) {
                    if (!walk.markedTraits.add(trait)) {
                        walk.problems.add(at + DaggerheartCharacterCreationFlow.capitalize(trait) + " is marked until level " + nextClearing(level) + ".");
                    }
                    walk.gains.add(new Gain(level, trait, 1));
                }
            }
            case HIT_POINT -> walk.gains.add(new Gain(level, "hitPointSlots", 1));
            case STRESS -> walk.gains.add(new Gain(level, "stressSlots", 1));
            case EVASION -> walk.gains.add(new Gain(level, "evasion", 1));
            case PROFICIENCY -> walk.proficiency++;
            case EXPERIENCES -> {
                List<Integer> picked = new ArrayList<>();
                advancement.path("experiences").forEach(index -> picked.add(index.asInt(-1)));
                if (picked.size() != 2 || picked.get(0).equals(picked.get(1)) || picked.stream().anyMatch(index -> index < 0 || index >= experiences)) {
                    walk.problems.add(at + "the Experience advancement raises two different Experiences you have.");
                    return;
                }
                picked.forEach(index -> walk.experienceBonuses.merge(index, 1, Integer::sum));
            }
            case DOMAIN_CARD -> {
                String card = DaggerheartBuild.text(advancement, "card");
                if (card.isEmpty()) {
                    walk.problems.add(at + "pick the extra domain card.");
                } else {
                    checkCard(walk, card, level, Math.min(level, extraCardCap(fromTier)), classDomains, catalogue, at);
                }
            }
            case SUBCLASS -> {
                if (++walk.subclassUpgrades > SUBCLASS_UPGRADES) {
                    walk.problems.add(at + "a subclass has a specialization and a mastery, no more.");
                }
                if (Integer.valueOf(fromTier).equals(walk.multiclassTier)) {
                    walk.problems.add(at + "multiclassing crossed out the upgraded subclass in Tier " + fromTier + ".");
                }
                walk.tiersWithSubclass.add(fromTier);
            }
            case MULTICLASS -> multiclass(walk, advancement, level, fromTier, at, catalogue, build);
        }
    }

    private static void multiclass(Walk walk, JsonNode advancement, int level, int fromTier, String at, DaggerheartCatalogue catalogue, DaggerheartBuild build) {
        if (level < MULTICLASS_LEVEL) {
            walk.problems.add(at + "multiclassing starts at level " + MULTICLASS_LEVEL + ".");
            return;
        }
        if (walk.multiclass != null) {
            walk.problems.add(at + "a character multiclasses once; the other multiclass options are crossed out.");
            return;
        }
        if (walk.tiersWithSubclass.contains(fromTier)) {
            walk.problems.add(at + "the upgraded subclass taken in Tier " + fromTier + " crossed out its multiclass option.");
        }
        Optional<CatalogueRecord> playerClass = catalogue.find(CLASS, DaggerheartBuild.text(advancement, "class"));
        if (playerClass.isEmpty() || playerClass.get().slug().equals(build.classSlug())) {
            walk.problems.add(at + "multiclass into a class you don't have.");
            return;
        }
        String domain = DaggerheartBuild.text(advancement, "domain");
        boolean ofClass = false;
        for (JsonNode candidate : playerClass.get().data().path("domains")) {
            ofClass |= candidate.asString().equals(domain);
        }
        if (!ofClass) {
            walk.problems.add(at + "pick one of the " + playerClass.get().name() + "'s domains.");
        }
        Optional<CatalogueRecord> subclass = catalogue.find(SUBCLASS, DaggerheartBuild.text(advancement, "subclass"))
                .filter(entry -> entry.data().path("classSlug").asString().equals(playerClass.get().slug()));
        if (subclass.isEmpty()) {
            walk.problems.add(at + "take the foundation card of a " + playerClass.get().name() + " subclass.");
        }
        walk.multiclassTier = fromTier;
        walk.multiclass = new Multiclass(playerClass.get(), domain, subclass.orElse(null));
    }

    /** A card at {@code maxLevel} or lower from the class's domains, or from the multiclass domain up to half the level. */
    private static void checkCard(Walk walk, String slug, int level, int maxLevel, Set<String> classDomains, DaggerheartCatalogue catalogue, String at) {
        Optional<CatalogueRecord> card = catalogue.find(DOMAIN_CARD, slug);
        if (card.isEmpty()) {
            walk.problems.add(at + "unknown domain card " + slug + ".");
            return;
        }
        String domain = card.get().data().path("domain").asString();
        int cardLevel = card.get().data().path("level").asInt();
        boolean fromClass = classDomains.contains(domain) && cardLevel <= maxLevel;
        boolean fromMulticlass = walk.multiclass != null && domain.equals(walk.multiclass.domain()) && cardLevel <= Math.min(maxLevel, multiclassCardCap(level));
        if (walk.cards.contains(slug)) {
            walk.problems.add(at + card.get().name() + " is already yours.");
        } else if (!fromClass && !fromMulticlass) {
            walk.problems.add(at + card.get().name() + " (level " + cardLevel + ", " + domain + ") is beyond your level or outside your domains.");
        }
        walk.cards.add(slug);
    }

    private static int nextClearing(int level) {
        return MARK_CLEARING_LEVELS.stream().filter(clearing -> clearing > level).min(Integer::compare).orElse(MAX_LEVEL + 1);
    }
}
