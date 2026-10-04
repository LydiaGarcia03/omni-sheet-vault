package dev.omnisheetvault.api.ruleset.dnd5e;

import static dev.omnisheetvault.api.ruleset.dnd5e.Dnd5eBuildFixtures.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.Spell;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Feature-granted spells cast without a slot: at will, or a number of uses per rest (spellcasting-pools.md, P2). */
class Dnd5eSlotlessSpellsTest {

    private static final List<Dnd5eBuildChoice> FIGHTER_ANSWERS = List.of(
            answer("class:fighter:skills:0", "athletics"),
            answer("class:fighter:cantrips", "fire-bolt"),
            answer("class:fighter:spells", "shield"));
    private static final String EFFECT = """
            {"effect":{"modifiers":[],"endsOnRests":[]}}""";

    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(MAPPER);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(MAPPER);

    @Test
    void aDailyGrantIsLimitedToItsUsesPerLongRestAtItsCastLevel() {
        Dnd5eSheet sheet = materialize(null, fighter("tiefling"));

        assertThat(spell(sheet, "burning-hands").usage())
                .isEqualTo(Dnd5eSpellUsage.limited(1, Dnd5eRechargeTrigger.LONG_REST, "species:tiefling:innate:3:daily:1", 2, false));
        assertThat(spell(sheet, "light").usage()).isNull();
        assertThat(spell(sheet, "shield").usage()).isNull();
    }

    @Test
    void aPlainInnateGrantIsAtWillAndOnlyOnTheCasterWhenTheSourceSaysSo() {
        Dnd5eSheet sheet = materialize(null, warlock("armor-of-shadows"));

        assertThat(spell(sheet, "mage-armor").usage()).isEqualTo(Dnd5eSpellUsage.atWill(null, true));
    }

    @Test
    void usingALimitedSpellSpendsAUseUntilItsRest() {
        String sheet = json(materialize(null, fighter("tiefling")));

        String used = mutator.castSpell(sheet, "burning-hands", 0, false, null, true);
        String usedAgain = mutator.castSpell(used, "burning-hands", 0, false, EFFECT, true);
        String shortRest = mutator.applyShortRest(used);
        String longRest = mutator.applyLongRest(used);

        assertThat(spell(read(used), "burning-hands").usage().usedUses()).isEqualTo(1);
        assertThat(usedAgain).isEqualTo(used);
        assertThat(spell(read(shortRest), "burning-hands").usage().usedUses()).isEqualTo(1);
        assertThat(spell(read(longRest), "burning-hands").usage().usedUses()).isZero();
    }

    @Test
    void spellsSharingAPoolSpendTheSameUses() {
        String sheet = json(materialize(null, fighter("pooled")));

        String used = mutator.castSpell(sheet, "sleep", 0, false, null, true);

        assertThat(spell(read(used), "sleep").usage().usedUses()).isEqualTo(1);
        assertThat(spell(read(used), "magic-missile").usage().usedUses()).isEqualTo(1);
        assertThat(spell(read(used), "detect-magic").usage().usedUses()).isZero();
    }

    @Test
    void anAtWillSpellSpendsNothingAndASelfOnlyOneLandsOnTheCaster() {
        String sheet = json(materialize(null, warlock("armor-of-shadows")));

        String cast = mutator.castSpell(sheet, "mage-armor", 0, false, EFFECT, false);

        assertThat(spell(read(cast), "mage-armor").usage()).isEqualTo(spell(read(sheet), "mage-armor").usage());
        assertThat(read(cast).activeEffectsOrEmpty()).extracting(Dnd5eActiveEffect::key).containsExactly("mage-armor");
        assertThat(read(cast).spellSlots()).isEqualTo(read(sheet).spellSlots());
    }

    @Test
    void reapplyingTheBuildKeepsTheUsesAlreadySpent() {
        Dnd5eSheet used = read(mutator.castSpell(json(materialize(null, fighter("tiefling"))), "burning-hands", 0, false, null, true));

        Dnd5eSheet rebuilt = materialize(used, fighter("tiefling"));

        assertThat(spell(rebuilt, "burning-hands").usage().usedUses()).isEqualTo(1);
    }

    @Test
    void theSheetShowsTheUsage() {
        String sheet = mutator.castSpell(json(materialize(null, fighter("tiefling"))), "burning-hands", 0, false, null, true);

        Spell burningHands = calculator.calculateVitals(sheet).spells().stream()
                .filter(spell -> spell.key().equals("burning-hands")).findFirst().orElseThrow();

        assertThat(burningHands.usage().mode()).isEqualTo("LIMITED");
        assertThat(burningHands.usage().maxUses()).isEqualTo(1);
        assertThat(burningHands.usage().usedUses()).isEqualTo(1);
        assertThat(burningHands.usage().recharge()).isEqualTo("LONG_REST");
        assertThat(burningHands.usage().castLevel()).isEqualTo(2);
    }

    // ---- fixtures ------------------------------------------------------------------

    /** The shared fixtures plus an invocation, a spell it grants, and a species whose daily use covers two spells. */
    private static CatalogueLookup catalogue() {
        CatalogueLookup shared = Dnd5eBuildFixtures.catalogue();
        Map<String, CatalogueRecord> extra = Map.of(
                "OPTIONAL_FEATURE/armor-of-shadows", record("OPTIONAL_FEATURE", "armor-of-shadows", "Armor of Shadows",
                        "You can cast mage armor on yourself at will, without expending a spell slot or material components.",
                        """
                        {"featureTypes":["EI"],"optional":false,"prerequisites":null,
                         "additionalSpells":[{"innate":{"_":["mage armor"]}}]}"""),
                "SPELL/mage-armor", record("SPELL", "mage-armor", "Mage Armor", "", """
                        {"level":1,"school":"abjuration","castingTime":"1 Action","range":"Touch","concentration":false,"ritual":false,
                         "attackRoll":false,"damageDiceCount":null,"damageDiceSides":null,"damageType":null,"notes":"V, S, M",
                         "effectSummary":"Buff","saveAbility":null,"components":"V, S, M","materialComponent":null,"duration":"8 hours",
                         "higherLevelsDescription":null,"higherLevelsDamageDiceCount":null,"higherLevelsDamageDiceSides":null,
                         "sourceCode":"PHB","classes":[{"name":"Wizard","source":"PHB"}],"optionalClasses":[]}"""),
                "SPECIES/pooled", record("SPECIES", "pooled", "Pooled", "", """
                        {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[],"subspecies":[],"variants":[],
                         "additionalSpells":[{"ability":"int","innate":{"1":{"daily":{"1":["sleep","magic missile"],"1e":["detect magic"]}}}}]}"""));
        return new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                CatalogueRecord found = extra.get(kind + "/" + slug);
                return found != null ? Optional.of(found) : shared.find(kind, slug);
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                List<CatalogueRecord> records = new ArrayList<>(shared.list(kind));
                extra.values().stream().filter(record -> record.kind().equals(kind)).forEach(records::add);
                return records;
            }
        };
    }

    private static CatalogueRecord record(String kind, String slug, String name, String description, String data) {
        return new CatalogueRecord(kind, slug, name, "PHB", 1, description, MAPPER.readTree(data));
    }

    private static Dnd5eSheet materialize(Dnd5eSheet current, Dnd5eCharacterBuild build) {
        CatalogueLookup catalogue = catalogue();
        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(build, catalogue);
        BuildPlan plan = planner.plan();
        assertThat(plan.problems()).isEmpty();
        assertThat(plan.pending()).isEmpty();
        return new Dnd5eBuildMaterializer(build, planner.state(), planner.outcome(), catalogue).materialize(current);
    }

    private static Dnd5eCharacterBuild fighter(String species) {
        return build(species, List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)), FIGHTER_ANSWERS);
    }

    private static Dnd5eCharacterBuild warlock(String invocation) {
        return build("human", List.of(new Dnd5eBuildClass("warlock", "warlock-fiend", 2)), List.of(
                answer("class:warlock:cantrips", "light"),
                answer("class:warlock:spells", "hex", "burning-hands"),
                answer("class:warlock:2:eldritch-invocations", invocation)));
    }

    private static Dnd5eCharacterBuild build(String species, List<Dnd5eBuildClass> classes, List<Dnd5eBuildChoice> answers) {
        return new Dnd5eCharacterBuild(species, null, null, "acolyte", classes, Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 12, "constitution", 14, "intelligence", 13, "wisdom", 10, "charisma", 13),
                Dnd5eHitPointMethod.FIXED, List.of(), answers);
    }

    private static Dnd5eBuildChoice answer(String id, String... selections) {
        return new Dnd5eBuildChoice(id, List.of(selections));
    }

    private static Dnd5eSpell spell(Dnd5eSheet sheet, String key) {
        return sheet.spells().stream().filter(spell -> spell.key().equals(key)).findFirst().orElseThrow();
    }

    private static String json(Dnd5eSheet sheet) {
        return MAPPER.writeValueAsString(sheet);
    }

    private static Dnd5eSheet read(String sheetJson) {
        return MAPPER.readValue(sheetJson, Dnd5eSheet.class);
    }
}
