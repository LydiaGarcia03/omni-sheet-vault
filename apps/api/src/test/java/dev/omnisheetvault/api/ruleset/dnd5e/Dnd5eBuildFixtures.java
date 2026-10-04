package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.ObjectMapper;

/** Trimmed catalogue records in the shapes the converters write (Fighter, Eldritch Knight, Wizard, Warlock, spells). */
final class Dnd5eBuildFixtures {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private Dnd5eBuildFixtures() {
    }

    static CatalogueLookup catalogue() {
        Map<String, Map<String, CatalogueRecord>> byKind = new HashMap<>();
        add(byKind, "SPECIES", "human", "Human", """
                {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[{"fixed":{"constitution":2},"choices":[],"maximum":null}],
                 "senses":[{"type":"darkvision","range":60}],"subspecies":[],"variants":[]}""");
        add(byKind, "SPECIES", "elf", "Elf", """
                {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[],"subspecies":[],"variants":[],
                 "additionalSpells":[{"ability":"int","known":{"1":{"_":[{"choose":"level=0|class=Wizard"}]}}}]}""");
        add(byKind, "SPECIES", "tiefling", "Tiefling", """
                {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[],"subspecies":[],"variants":[],
                 "additionalSpells":[{"ability":"cha","known":{"1":["light#c"]},"innate":{"3":{"daily":{"1":["burning hands#2"]}}}}]}""");
        add(byKind, "BACKGROUND", "acolyte", "Acolyte", """
                {"skillAlternatives":[{"fixed":["insight"],"choices":[]}],"startingEquipment":[],
                 "features":[{"name":"Shelter of the Faithful","description":"You command respect."}]}""");
        add(byKind, "CLASS", "fighter", "Fighter", """
                {"hitDie":10,"savingThrows":["strength","constitution"],"subclassTitle":"Martial Archetype","subclassLevel":3,
                 "abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light","medium","heavy","shield"],"weaponCategories":["simple","martial"],"weaponItems":[],
                   "skillAlternatives":[{"fixed":[],"choices":[{"from":["athletics","perception"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                   "toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"strength":13},"anyOf":{},"proficienciesGained":{}},
                 "spellcasting":null,"optionalFeatureProgressions":[],
                 "features":[{"name":"Martial Archetype","level":3,"optional":false,"grantsSubclassFeature":true,"choices":[]},
                   {"name":"Extra Attack","level":3,"optional":false,"grantsSubclassFeature":false,"choices":[],
                    "modifiers":[{"type":"SET","target":"EXTRA_ATTACKS","value":1}]},
                   {"name":"Second Wind","level":1,"optional":false,"grantsSubclassFeature":false,"choices":[],"modifiers":[],
                    "uses":{"resource":"second-wind","count":1,"recharge":"SHORT_OR_LONG_REST"},"action":{"type":"BONUS_ACTION"},
                    "grants":{"languages":["thieves-cant"]}}]}""");
        add(byKind, "CLASS", "wizard", "Wizard", """
                {"hitDie":6,"savingThrows":["intelligence","wisdom"],"subclassTitle":"Arcane Tradition","subclassLevel":2,
                 "abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":[],"weaponCategories":[],"weaponItems":[],"skillAlternatives":[],"toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"intelligence":13},"anyOf":{},"proficienciesGained":{}},
                 "spellcasting":{"ability":"intelligence","casterProgression":"full","preparedSpellsFormula":{"levelDivisor":1,"ability":"intelligence"},
                   "cantripsKnownByLevel":[1,1,1,2],"spellsKnownByLevel":null,"spellbookSpellsAddedByLevel":[2,1,2,1],
                   "spellSlotsByLevel":[[2,0,0],[3,0,0],[4,2,0],[4,3,0],[4,3,2]]},
                 "optionalFeatureProgressions":[],
                 "features":[{"name":"Arcane Tradition","level":2,"optional":false,"grantsSubclassFeature":true,"choices":[]}]}""");
        add(byKind, "CLASS", "warlock", "Warlock", """
                {"hitDie":8,"savingThrows":["wisdom","charisma"],"subclassTitle":"Otherworldly Patron","subclassLevel":1,
                 "abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light"],"weaponCategories":["simple"],"weaponItems":[],"skillAlternatives":[],"toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"charisma":13},"anyOf":{},"proficienciesGained":{}},
                 "spellcasting":{"ability":"charisma","casterProgression":"pact","preparedSpellsFormula":null,
                   "cantripsKnownByLevel":[1,1],"spellsKnownByLevel":[1,2],"spellbookSpellsAddedByLevel":null,"spellSlotsByLevel":null,
                   "pactSlotsByLevel":[{"slots":1,"slotLevel":1},{"slots":2,"slotLevel":1}]},
                 "optionalFeatureProgressions":[{"name":"Eldritch Invocations","featureTypes":["EI"],"countByLevel":[0,1]}],
                 "features":[{"name":"Otherworldly Patron","level":1,"optional":false,"grantsSubclassFeature":true,"choices":[]}]}""");
        add(byKind, "SUBCLASS", "fighter-eldritch-knight", "Eldritch Knight", """
                {"classSlug":"fighter","features":[],"optionalFeatureProgressions":[],
                 "additionalSpells":[{"expanded":{"3":[{"all":"level=0|class=Wizard"},{"all":"level=1|class=Wizard"}]}}],
                 "spellcasting":{"ability":"intelligence","casterProgression":"1/3","preparedSpellsFormula":null,
                   "cantripsKnownByLevel":[0,0,1,1],"spellsKnownByLevel":[0,0,1,2],"spellbookSpellsAddedByLevel":null,
                   "spellSlotsByLevel":[[0,0],[0,0],[2,0],[3,0]]}}""");
        add(byKind, "SUBCLASS", "wizard-evocation", "School of Evocation", """
                {"classSlug":"wizard","shortName":"Evocation","features":[],"optionalFeatureProgressions":[]}""");
        add(byKind, "SUBCLASS", "wizard-chronurgy", "Chronurgy Magic", """
                {"classSlug":"wizard","shortName":"Chronurgy","optionalFeatureProgressions":[],
                 "features":[{"name":"Temporal Resilience","level":2,"choices":[],
                   "modifiers":[{"type":"BONUS","target":"HIT_POINTS_PER_LEVEL","value":1,"levelScope":"CLASS"}]}]}""");
        add(byKind, "SPELL", "magnify-gravity", "Magnify Gravity", """
                {"level":1,"school":"transmutation","castingTime":"1 Action","range":"60 ft.","concentration":false,"ritual":false,
                 "attackRoll":false,"damageDiceCount":null,"damageDiceSides":null,"damageType":null,"notes":"V, S",
                 "effectSummary":"Buff","saveAbility":null,"components":"V, S","materialComponent":null,"duration":"1 round",
                 "higherLevelsDescription":null,"higherLevelsDamageDiceCount":null,"higherLevelsDamageDiceSides":null,
                 "sourceCode":"EGW","classes":[],"optionalClasses":[],
                 "subclasses":[{"className":"Wizard","classSource":"PHB","subclassShortName":"Chronurgy","subclassSource":"EGW"}]}""");
        add(byKind, "SUBCLASS", "warlock-fiend", "The Fiend", """
                {"classSlug":"warlock","features":[],"optionalFeatureProgressions":[],
                 "additionalSpells":[{"expanded":{"s1":["burning hands"]}}]}""");
        add(byKind, "OPTIONAL_FEATURE", "agonizing-blast", "Agonizing Blast", """
                {"featureTypes":["EI"],"optional":false,"prerequisites":[{"spell":["eldritch blast#c"]}]}""");
        add(byKind, "OPTIONAL_FEATURE", "devils-sight", "Devil's Sight", """
                {"featureTypes":["EI"],"optional":false,"prerequisites":null}""");
        spell(byKind, "fire-bolt", "Fire Bolt", 0, "evocation", "Sorcerer", "Wizard");
        spell(byKind, "light", "Light", 0, "evocation", "Wizard", "Warlock");
        spell(byKind, "eldritch-blast", "Eldritch Blast", 0, "evocation", "Warlock");
        spell(byKind, "magic-missile", "Magic Missile", 1, "evocation", "Wizard");
        spell(byKind, "shield", "Shield", 1, "abjuration", "Wizard");
        spell(byKind, "sleep", "Sleep", 1, "enchantment", "Wizard");
        spell(byKind, "burning-hands", "Burning Hands", 1, "evocation", "Wizard");
        spell(byKind, "detect-magic", "Detect Magic", 1, "divination", "Wizard");
        spell(byKind, "hex", "Hex", 1, "enchantment", "Warlock");
        spell(byKind, "misty-step", "Misty Step", 2, "conjuration", "Wizard", "Warlock");
        return lookup(byKind);
    }

    private static void spell(Map<String, Map<String, CatalogueRecord>> byKind, String slug, String name, int level, String school,
            String... classes) {
        StringBuilder classList = new StringBuilder();
        for (String className : classes) {
            classList.append(classList.isEmpty() ? "" : ",").append("{\"name\":\"").append(className).append("\",\"source\":\"PHB\"}");
        }
        add(byKind, "SPELL", slug, name, """
                {"level":%d,"school":"%s","castingTime":"1 Action","range":"60 ft.","concentration":false,"ritual":false,
                 "attackRoll":false,"damageDiceCount":null,"damageDiceSides":null,"damageType":null,"notes":"V, S",
                 "effectSummary":"Buff","saveAbility":null,"components":"V, S","materialComponent":null,"duration":"Instantaneous",
                 "higherLevelsDescription":null,"higherLevelsDamageDiceCount":null,"higherLevelsDamageDiceSides":null,
                 "sourceCode":"PHB","classes":[%s],"optionalClasses":[]}""".formatted(level, school, classList));
    }

    private static CatalogueLookup lookup(Map<String, Map<String, CatalogueRecord>> byKind) {
        return new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                return Optional.ofNullable(byKind.getOrDefault(kind, Map.of()).get(slug));
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                return new ArrayList<>(byKind.getOrDefault(kind, Map.of()).values());
            }
        };
    }

    private static void add(Map<String, Map<String, CatalogueRecord>> byKind, String kind, String slug, String name, String data) {
        byKind.computeIfAbsent(kind, key -> new LinkedHashMap<>())
                .put(slug, new CatalogueRecord(kind, slug, name, "PHB", 1, "", MAPPER.readTree(data)));
    }
}
