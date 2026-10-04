package dev.omnisheetvault.api.ruleset.dnd5e;

import static dev.omnisheetvault.api.ruleset.dnd5e.Dnd5eBuildFixtures.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.StartingItem;
import jakarta.validation.Validation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Dnd5eBuildMaterializerTest {

    private static final List<Dnd5eBuildChoice> FIGHTER_ANSWERS = List.of(
            answer("class:fighter:skills:0", "athletics"),
            answer("class:fighter:cantrips", "fire-bolt"),
            answer("class:fighter:spells", "shield"));
    private static final List<Dnd5eBuildChoice> WIZARD_ANSWERS = List.of(
            answer("class:wizard:cantrips", "light"),
            answer("class:wizard:spellbook", "magic-missile", "sleep", "burning-hands", "misty-step", "detect-magic"),
            answer("class:wizard:prepared", "magic-missile", "sleep", "burning-hands", "misty-step"));
    private static final List<Dnd5eBuildChoice> WARLOCK_ANSWERS = List.of(
            answer("class:warlock:cantrips", "light"),
            answer("class:warlock:spells", "hex", "burning-hands"),
            answer("class:warlock:2:eldritch-invocations", "devils-sight"));

    @Test
    void multiclassHitPointsTakeTheFirstClassMaximumThenEachClassAverage() {
        Dnd5eSheet sheet = materialize(null, eldritchKnightWizard());

        assertThat(sheet.hitPointBase()).isEqualTo(10 + 6 + 6 + 4 + 4 + 4);
        assertThat(Dnd5eFormulas.maxHitPoints(sheet)).isEqualTo(34 + 6 * 3);
        assertThat(sheet.currentHitPoints()).isEqualTo(52);
        assertThat(sheet.level()).isEqualTo(6);
        assertThat(sheet.classLevels()).extracting(Dnd5eClassLevel::className, Dnd5eClassLevel::level, Dnd5eClassLevel::hitDieSize)
                .containsExactly(tuple("Fighter", 3, 10), tuple("Wizard", 3, 6));
        assertThat(sheet.derivation().hitPoints()).extracting(Dnd5eDerivation.SourcedAmount::source)
                .startsWith("Fighter 1 (maximum d10)", "Fighter 2 (average d10)");
    }

    @Test
    void rolledHitPointsOfferOneRowPerLevelAfterTheFirstWithEachClassDie() {
        BuildPlan plan = new Dnd5eBuildPlanner(rolled(eldritchKnightWizard(), Arrays.asList(7, null)), Dnd5eBuildFixtures.catalogue()).plan();

        CreationChoice choice = plan.choices().stream().filter(candidate -> candidate.id().equals("build.rolledHitPoints")).findFirst().orElseThrow();
        assertThat(choice.options()).extracting(CreationChoiceOption::label)
                .containsExactly("Fighter lvl 2", "Fighter lvl 3", "Wizard lvl 1", "Wizard lvl 2", "Wizard lvl 3");
        assertThat(choice.options()).extracting(option -> option.data().path("sides").asInt(), option -> option.data().path("average").asInt())
                .containsExactly(tuple(10, 6), tuple(10, 6), tuple(6, 4), tuple(6, 4), tuple(6, 4));
        assertThat(choice.options()).extracting(option -> option.data().path("group").asString())
                .containsExactly("fighter", "fighter", "wizard", "wizard", "wizard");
        assertThat(choice.options().get(2).data().path("rollLabel").asString()).isEqualTo("Hit points, Wizard lvl 1 (d6)");
        assertThat(choice.selected()).containsExactly("7");
        assertThat(choice.isPending()).isTrue();
    }

    @Test
    void aRolledHitPointAboveItsDieIsAProblem() {
        BuildPlan plan = new Dnd5eBuildPlanner(rolled(eldritchKnightWizard(), List.of(7, 6, 7, 1, 1)), Dnd5eBuildFixtures.catalogue()).plan();

        assertThat(plan.problems()).contains("Hit points for Wizard lvl 1 must be between 1 and 6");
    }

    @Test
    void rolledHitPointsReplaceTheAverageAndAnUnrolledLevelKeepsIt() {
        Dnd5eSheet sheet = materialize(null, rolled(eldritchKnightWizard(), List.of(7, 2, 1, 6, 3)));

        assertThat(sheet.hitPointBase()).isEqualTo(10 + 7 + 2 + 1 + 6 + 3);
        assertThat(sheet.derivation().hitPoints()).extracting(Dnd5eDerivation.SourcedAmount::source)
                .startsWith("Fighter 1 (maximum d10)", "Fighter 2 (rolled d10)");

        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(rolled(eldritchKnightWizard(), Arrays.asList(7, null)), Dnd5eBuildFixtures.catalogue());
        planner.plan();
        Dnd5eSheet partial = new Dnd5eBuildMaterializer(rolled(eldritchKnightWizard(), Arrays.asList(7, null)), planner.state(), planner.outcome(),
                Dnd5eBuildFixtures.catalogue()).materialize(null);
        assertThat(partial.hitPointBase()).isEqualTo(10 + 7 + 6 + 4 + 4 + 4);
    }

    @Test
    void startingItemLinesTheBuildNamesStartEquippedEachOnItsOwn() {
        Dnd5eBuildOutcome outcome = new Dnd5eBuildOutcome();
        outcome.addStartingItem("chain-mail", null, 1, null);
        outcome.addStartingItem("dagger", null, 1, null);
        outcome.addStartingItem(null, "insignia of rank", 1, null);
        outcome.addStartingItem("dagger", null, 2, null);
        Dnd5eCharacterBuild base = eldritchKnightWizard();
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(base.speciesSlug(), null, null, base.backgroundSlug(), base.classes(),
                base.abilityScoreMethod(), base.baseAbilityScores(), null, null, base.choices(), null, null, List.of("chain-mail#0", "dagger#1"));

        assertThat(Dnd5eBuildMaterializer.lineKeys(outcome)).containsExactly("chain-mail#0", "dagger#0", null, "dagger#1");
        assertThat(Dnd5eBuildMaterializer.startingItems(outcome, build))
                .extracting(StartingItem::catalogueSlug, StartingItem::quantity, StartingItem::equipped)
                .containsExactly(tuple("chain-mail", 1, true), tuple("dagger", 1, false), tuple(null, 1, false), tuple("dagger", 2, true));
    }

    @Test
    void aPactCasterGetsItsPactMagicPoolAndKeepsItsUsedSlots() {
        Dnd5eCharacterBuild warlock = build("human", List.of(new Dnd5eBuildClass("warlock", "warlock-fiend", 2)), WARLOCK_ANSWERS);
        Dnd5eSheet first = materialize(null, warlock);
        Dnd5eSheet spent = first.withSpellSlots(List.of(new Dnd5eSpellSlotLevel(1, 2, 1, true, "Warlock")));

        Dnd5eSheet again = materialize(spent, warlock);

        assertThat(first.spellSlots()).extracting(Dnd5eSpellSlotLevel::level, Dnd5eSpellSlotLevel::maxSlots, Dnd5eSpellSlotLevel::pact)
                .containsExactly(tuple(1, 2, true));
        assertThat(first.spellSlots().getFirst().usedSlots()).isZero();
        assertThat(first.spellSlots().getFirst().className()).isEqualTo("Warlock");
        assertThat(again.spellSlots()).extracting(Dnd5eSpellSlotLevel::usedSlots).containsExactly(1);
    }

    @Test
    void multiclassSlotsCombineCasterLevelsOnTheFullCasterTable() {
        Dnd5eSheet sheet = materialize(null, eldritchKnightWizard());

        assertThat(sheet.spellSlots()).extracting(Dnd5eSpellSlotLevel::level, Dnd5eSpellSlotLevel::maxSlots)
                .containsExactly(tuple(1, 4), tuple(2, 3));
        assertThat(sheet.spellcastingClasses()).extracting(Dnd5eSpellcastingClass::className).containsExactly("Fighter", "Wizard");
        assertThat(sheet.spellcastingClasses().get(1).spellsPreparedMax()).isEqualTo(3 + 1);
    }

    @Test
    void aSubclassCasterUsesItsOwnSlotTableAndItsExpandedList() {
        Dnd5eSheet sheet = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));

        assertThat(sheet.spellSlots()).extracting(Dnd5eSpellSlotLevel::level, Dnd5eSpellSlotLevel::maxSlots)
                .containsExactly(tuple(1, 2));
        assertThat(sheet.spellcastingClasses().getFirst().cantripsKnownMax()).isEqualTo(1);
        assertThat(sheet.spells()).extracting(Dnd5eSpell::key, Dnd5eSpell::className)
                .containsExactly(tuple("fire-bolt", "Fighter"), tuple("shield", "Fighter"));
    }

    @Test
    void theSpellbookHoldsEveryPickAndOnlyThePreparedChoiceIsPrepared() {
        Dnd5eSheet sheet = materialize(null, eldritchKnightWizard());

        assertThat(sheet.spells()).filteredOn(spell -> spell.className().equals("Wizard"))
                .extracting(Dnd5eSpell::key, Dnd5eSpell::prepared)
                .containsExactlyInAnyOrder(tuple("light", false), tuple("magic-missile", true), tuple("sleep", true),
                        tuple("burning-hands", true), tuple("misty-step", true), tuple("detect-magic", false));
    }

    @Test
    void groupsClassFeaturesByClassAndListsTheSubclassUnderTheFeatureThatGrantsIt() {
        Dnd5eSheet sheet = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));

        assertThat(sheet.featureTraits())
                .filteredOn(trait -> trait.category() == Dnd5eFeatureTraitCategory.CLASS_FEATURE)
                .extracting(Dnd5eFeatureTrait::source)
                .containsOnly("Fighter");
        assertThat(sheet.featureTraits())
                .filteredOn(trait -> trait.name().equals("Martial Archetype"))
                .singleElement()
                .extracting(Dnd5eFeatureTrait::choices)
                .isEqualTo(List.of("Eldritch Knight"));
        assertThat(sheet.featureTraits())
                .filteredOn(trait -> trait.name().equals("Second Wind"))
                .singleElement()
                .extracting(Dnd5eFeatureTrait::choices)
                .isEqualTo(List.of());
    }

    @Test
    void recordsProvenanceAndSpeciesTraits() {
        Dnd5eSheet sheet = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));

        assertThat(sheet.constitution()).isEqualTo(16);
        assertThat(sheet.derivation().abilityScores().get("constitution")).extracting(Dnd5eDerivation.SourcedAmount::amount)
                .containsExactly(14, 2);
        assertThat(sheet.skillProficiencies()).containsExactlyInAnyOrder("athletics", "insight");
        assertThat(sheet.derivation().grants())
                .filteredOn(grant -> grant.kind().equals("ARMOR"))
                .allSatisfy(grant -> assertThat(sheet.armorProficiencies()).contains(grant.label()));
        assertThat(sheet.languages()).contains("Thieves Cant");
        assertThat(sheet.derivation().grants())
                .filteredOn(grant -> grant.kind().equals("LANGUAGE") && grant.key().equals("thieves-cant"))
                .extracting(Dnd5eDerivation.SourcedGrant::source)
                .containsExactly("Fighter 1");
        assertThat(sheet.derivation().grants())
                .filteredOn(grant -> grant.kind().equals("SKILL"))
                .extracting(Dnd5eDerivation.SourcedGrant::label)
                .containsOnlyNulls();
        assertThat(sheet.specialSenses()).extracting(Dnd5eSpecialSense::type).containsExactly(Dnd5eSenseType.DARKVISION);
        assertThat(sheet.featureTraits()).extracting(Dnd5eFeatureTrait::name).contains("Martial Archetype");
        assertThat(sheet.build()).isNotNull();
    }

    @Test
    void featureModifiersAreCopiedOntoTheSheetWithTheirSourceAndClass() {
        Dnd5eSheet fighter = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));
        Dnd5eSheet chronurgist = materialize(null, build("human", List.of(new Dnd5eBuildClass("wizard", "wizard-chronurgy", 3)),
                WIZARD_ANSWERS));

        assertThat(fighter.featureTraits()).flatExtracting(Dnd5eFeatureTrait::modifiersOrEmpty)
                .containsExactly(new Dnd5eModifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.EXTRA_ATTACKS, 1, null, null, "Extra Attack"));
        assertThat(chronurgist.featureTraits()).flatExtracting(Dnd5eFeatureTrait::modifiersOrEmpty)
                .containsExactly(new Dnd5eModifier(Dnd5eModifierType.BONUS, Dnd5eModifierTarget.HIT_POINTS_PER_LEVEL, 1, null, "wizard",
                        "Temporal Resilience"));
        assertThat(Dnd5eFormulas.maxHitPoints(chronurgist)).isEqualTo(6 + 4 + 4 + 3 * 3 + 3);
    }

    @Test
    void aFeatureWithUsesAndAnActionGetsOneCounterSharedByItsActionRow() {
        Dnd5eSheet sheet = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));

        Dnd5eFeatureTrait secondWind = sheet.featureTraits().stream().filter(trait -> trait.name().equals("Second Wind")).findFirst().orElseThrow();
        assertThat(secondWind.maxUses()).isEqualTo(1);
        assertThat(secondWind.rechargeTrigger()).isEqualTo(Dnd5eRechargeTrigger.SHORT_OR_LONG_REST);
        assertThat(sheet.featureActions()).extracting(Dnd5eFeatureAction::name, Dnd5eFeatureAction::actionType, Dnd5eFeatureAction::traitKey)
                .containsExactly(tuple("Second Wind", Dnd5eActionType.BONUS_ACTION, secondWind.key()));
    }

    @Test
    void spendingALinkedActionSpendsItsTraitAndARestRestoresIt() {
        Dnd5eSheet sheet = materialize(null, build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                FIGHTER_ANSWERS));
        Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(MAPPER);
        Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(MAPPER);
        String actionKey = sheet.featureActions().getFirst().key();

        String spent = mutator.useFeatureAction(MAPPER.writeValueAsString(sheet), actionKey);
        String rested = mutator.applyShortRest(spent);

        assertThat(calculator.calculateVitals(spent).featureActions().getFirst().usedCount()).isEqualTo(1);
        assertThat(calculator.calculateVitals(spent).featureTraits())
                .filteredOn(trait -> trait.name().equals("Second Wind")).extracting(trait -> trait.usedCount()).containsExactly(1);
        assertThat(calculator.calculateVitals(rested).featureActions().getFirst().usedCount()).isZero();
    }

    @Test
    void speciesSpellsGetTheirOwnCasterAndUsageNotes() {
        Dnd5eSheet sheet = materialize(null, build("tiefling", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)),
                List.of(answer("class:fighter:skills:0", "athletics"), answer("class:fighter:cantrips", "fire-bolt"),
                        answer("class:fighter:spells", "shield"))));

        assertThat(sheet.spellcastingClasses()).extracting(Dnd5eSpellcastingClass::className, Dnd5eSpellcastingClass::abilityKey)
                .containsExactly(tuple("Fighter", "intelligence"), tuple("Tiefling", "charisma"));
        assertThat(sheet.spells()).filteredOn(spell -> spell.className().equals("Tiefling"))
                .extracting(Dnd5eSpell::key, Dnd5eSpell::notes)
                .containsExactly(tuple("light", "V, S"), tuple("burning-hands", "1/day, cast at level 2; V, S"));
    }

    @Test
    void rematerializingKeepsPlayStateAndCapsItAtTheNewMaximums() {
        Dnd5eSheet materialized = materialize(null, eldritchKnightWizard());
        List<Dnd5eSpell> replanned = new ArrayList<>();
        for (Dnd5eSpell spell : materialized.spells()) {
            boolean prepared = spell.key().equals("detect-magic") || (spell.prepared() && !spell.key().equals("sleep"));
            replanned.add(new Dnd5eSpell(spell.key(), spell.name(), spell.className(), spell.level(), spell.school(),
                    spell.castingTime(), spell.range(), spell.concentration(), spell.ritual(), spell.attackRoll(),
                    spell.damageDiceCount(), spell.damageDiceSides(), spell.damageType(), spell.notes(), spell.effectSummary(),
                    prepared, spell.alwaysPrepared(), spell.description(), spell.saveAbility(), spell.components(),
                    spell.materialComponent(), spell.duration(), spell.higherLevelsDescription(),
                    spell.higherLevelsDamageDiceCount(), spell.higherLevelsDamageDiceSides()));
        }
        Dnd5eSheet played = materialized
                .withSessionState(20, 5, true, Set.of())
                .withSpellSlots(List.of(new Dnd5eSpellSlotLevel(1, 4, 3), new Dnd5eSpellSlotLevel(2, 3, 3)))
                .withCoins(10, 0, 0, 7, 0)
                .withSpells(replanned);

        Dnd5eSheet rebuilt = materialize(played, eldritchKnightWizard());
        Dnd5eSheet fighterOnly = materialize(played, build("human",
                List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3)), FIGHTER_ANSWERS));

        assertThat(rebuilt.spells()).filteredOn(Dnd5eSpell::prepared).extracting(Dnd5eSpell::key)
                .containsExactlyInAnyOrder("magic-missile", "burning-hands", "misty-step", "detect-magic");
        assertThat(fighterOnly.currentHitPoints()).isEqualTo(20);
        assertThat(fighterOnly.temporaryHitPoints()).isEqualTo(5);
        assertThat(fighterOnly.heroicInspiration()).isTrue();
        assertThat(fighterOnly.goldPieces()).isEqualTo(7);
        assertThat(fighterOnly.spellSlots()).extracting(Dnd5eSpellSlotLevel::maxSlots, Dnd5eSpellSlotLevel::usedSlots)
                .containsExactly(tuple(2, 2));
        assertThat(fighterOnly.spells()).extracting(Dnd5eSpell::key).containsExactly("fire-bolt", "shield");
    }

    @Test
    void refusesToMaterializeWhileChoicesArePending() throws Exception {
        Dnd5eCharacterCreationFlow flow = new Dnd5eCharacterCreationFlow(MAPPER, Validation.buildDefaultValidatorFactory().getValidator());
        Dnd5eCharacterBuild unanswered = build("human", List.of(new Dnd5eBuildClass("fighter", null, 1)), List.of());

        MaterializedSheet result = flow.materialize(null, MAPPER.writeValueAsString(unanswered), Dnd5eBuildFixtures.catalogue());

        assertThat(result.isMaterialized()).isFalse();
        assertThat(result.plan().pending()).isNotEmpty();
    }

    // ---- fixtures -----------------------------------------------------------------

    private static Dnd5eSheet materialize(Dnd5eSheet current, Dnd5eCharacterBuild build) {
        CatalogueLookup catalogue = Dnd5eBuildFixtures.catalogue();
        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(build, catalogue);
        BuildPlan plan = planner.plan();
        assertThat(plan.problems()).isEmpty();
        assertThat(plan.pending()).isEmpty();
        return new Dnd5eBuildMaterializer(build, planner.state(), planner.outcome(), catalogue).materialize(current);
    }

    private static Dnd5eCharacterBuild eldritchKnightWizard() {
        List<Dnd5eBuildChoice> answers = new ArrayList<>(FIGHTER_ANSWERS);
        answers.addAll(WIZARD_ANSWERS);
        return build("human", List.of(new Dnd5eBuildClass("fighter", "fighter-eldritch-knight", 3),
                new Dnd5eBuildClass("wizard", "wizard-evocation", 3)), answers);
    }

    private static Dnd5eCharacterBuild build(String species, List<Dnd5eBuildClass> classes, List<Dnd5eBuildChoice> answers) {
        return new Dnd5eCharacterBuild(species, null, null, "acolyte", classes, Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 12, "constitution", 14, "intelligence", 13, "wisdom", 10, "charisma", 8),
                Dnd5eHitPointMethod.FIXED, List.of(), answers);
    }

    private static Dnd5eCharacterBuild rolled(Dnd5eCharacterBuild build, List<Integer> rolledHitPoints) {
        return new Dnd5eCharacterBuild(build.speciesSlug(), build.subspeciesName(), build.speciesVariantName(), build.backgroundSlug(),
                build.classes(), build.abilityScoreMethod(), build.baseAbilityScores(), Dnd5eHitPointMethod.ROLLED, rolledHitPoints,
                build.choices(), build.abilityScoreAdjustments(), build.preferences());
    }

    private static Dnd5eBuildChoice answer(String id, String... selections) {
        return new Dnd5eBuildChoice(id, List.of(selections));
    }
}
