package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.ProficiencySource;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** C2a: the build's recorded provenance shown as contributions. Fixture shaped like Vex's real derivation. */
class Dnd5eProvenanceTest {

    private static final Dnd5eDerivation.SourcedAmount[] VEX_HIT_POINTS = {
        amount("Rogue 1 (maximum d8)", 8), amount("Rogue 2 (average d8)", 5), amount("Rogue 3 (average d8)", 5),
        amount("Rogue 4 (average d8)", 5), amount("Sorcerer 1 (average d6)", 4), amount("Sorcerer 2 (average d6)", 4),
        amount("Sorcerer 3 (average d6)", 4)};

    private static final Dnd5eDerivation VEX = new Dnd5eDerivation(
            Map.of("dexterity", List.of(amount("Base", 15), amount("Changeling", 2), amount("Ability Score Improvement (Rogue 4)", 1))),
            List.of(VEX_HIT_POINTS),
            List.of(amount("Changeling", 30)),
            List.of(new Dnd5eDerivation.SourcedGrant("SKILL", "acrobatics", "Rogue 1", null),
                    new Dnd5eDerivation.SourcedGrant("SKILL", "stealth", "Background", null),
                    new Dnd5eDerivation.SourcedGrant("EXPERTISE", "stealth", "Rogue 1", null),
                    new Dnd5eDerivation.SourcedGrant("SAVING_THROW", "dexterity", "Rogue 1", null),
                    new Dnd5eDerivation.SourcedGrant("TOOL", "thieves-tools", "Background", "Thieves' Tools (Expertise)")));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void abilityScoresListEveryRecordedContribution() {
        CalculatedValue dexterity = calculate(sheet(VEX, 18, List.of())).provenance().abilityScores().get("dexterity");

        assertThat(dexterity.value()).isEqualTo(18);
        assertThat(dexterity.contributions()).extracting(Contribution::source)
                .containsExactly("Base", "Changeling", "Ability Score Improvement (Rogue 4)");
    }

    @Test
    void aScoreThatNoLongerMatchesItsDerivationShowsTheDifference() {
        CalculatedValue dexterity = calculate(sheet(VEX, 19, List.of())).provenance().abilityScores().get("dexterity");

        assertThat(dexterity.contributions()).last().isEqualTo(new Contribution("Other", 1));
    }

    @Test
    void aSheetWithoutADerivationShowsOnlyTheBase() {
        VitalsZone vitals = calculate(sheet(null, 18, List.of()));

        assertThat(vitals.provenance().abilityScores().get("dexterity").contributions()).containsExactly(new Contribution("Base", 18));
        assertThat(vitals.provenance().speed().contributions()).containsExactly(new Contribution("Base", 30));
        assertThat(vitals.provenance().proficiencySources()).isEmpty();
    }

    @Test
    void hitPointsListEachClassWithAverageLevelsMerged() {
        VitalsZone vitals = calculate(sheet(VEX, 18, List.of()));

        assertThat(vitals.hitPoints().contributions()).extracting(Contribution::source).startsWith(
                "Rogue 1 (maximum d8)", "Rogue 2–4 (average d8 × 3)", "Sorcerer 1–3 (average d6 × 3)");
        assertThat(vitals.hitPoints().contributions().get(1).amount()).isEqualTo(15);
    }

    @Test
    void proficienciesNameTheirSource() {
        VitalsZone vitals = calculate(sheet(VEX, 18, List.of()));

        assertThat(vitals.skills().get("acrobatics").contributions()).extracting(Contribution::source).contains("Proficiency bonus (Rogue 1)");
        assertThat(vitals.skills().get("stealth").contributions()).extracting(Contribution::source)
                .contains("Proficiency bonus ×2 (Expertise, Rogue 1)");
        assertThat(vitals.savingThrows().get("dexterity").contributions()).extracting(Contribution::source)
                .contains("Proficiency bonus (Rogue 1)");
        assertThat(vitals.provenance().proficiencySources())
                .containsExactly(new ProficiencySource("TOOL", "Thieves' Tools (Expertise)", "Background"));
    }

    @Test
    void speedShowsItsSourceAndConditionChanges() {
        Dnd5eModifier grappled = new Dnd5eModifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.SPEED, 0, null, null, "Grappled");

        CalculatedValue speed = calculate(sheet(VEX, 18, List.of(grappled))).provenance().speed();

        assertThat(speed.value()).isZero();
        assertThat(speed.contributions()).containsExactly(new Contribution("Changeling", 30), new Contribution("Grappled", -30));
    }

    private VitalsZone calculate(Dnd5eSheet sheet) {
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    private static Dnd5eDerivation.SourcedAmount amount(String source, int amount) {
        return new Dnd5eDerivation.SourcedAmount(source, amount);
    }

    /** Level 7, hit point base 35 (Vex's), proficient in Acrobatics and Stealth (expertise) and Dexterity saves. */
    private static Dnd5eSheet sheet(Dnd5eDerivation derivation, int dexterity, List<Dnd5eModifier> conditionModifiers) {
        return new Dnd5eSheet(
                8, dexterity, 13, 10, 12, 16, 7, 8, 30, Set.of("dexterity"), Set.of("acrobatics", "stealth"), List.of(), List.of(),
                List.of("Thieves' Tools (Expertise)"), List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0,
                false, List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(),
                0, List.of(), List.of(), List.of(), false, null, Set.of("stealth"), null, 35, derivation,
                conditionModifiers.isEmpty() ? null : Map.of("grappled", conditionModifiers), null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
