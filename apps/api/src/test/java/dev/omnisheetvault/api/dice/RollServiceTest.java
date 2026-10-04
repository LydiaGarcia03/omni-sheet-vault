package dev.omnisheetvault.api.dice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Coins;
import dev.omnisheetvault.api.ruleset.Encumbrance;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.MechanicResolver;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.RollModeInfo;
import dev.omnisheetvault.api.ruleset.SheetCalculator;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.ruleset.registry.MechanicResolverRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Unit tests for the advantage/disadvantage arithmetic in {@link RollService#roll} —
 * see ground-rules.md's Dice section. Every collaborator is mocked; this class does
 * not touch Spring or a database, only the dice-resolution logic itself.
 */
class RollServiceTest {

    private static final UUID CHARACTER_ID = UUID.randomUUID();

    private CharacterService characterService;
    private SheetCalculatorRegistry sheetCalculatorRegistry;
    private MechanicResolverRegistry mechanicResolverRegistry;
    private DiceRoller diceRoller;
    private RollRepository rollRepository;
    private RollService rollService;
    private MechanicResolver mechanicResolver;

    @BeforeEach
    void setUp() {
        characterService = mock(CharacterService.class);
        sheetCalculatorRegistry = mock(SheetCalculatorRegistry.class);
        mechanicResolverRegistry = mock(MechanicResolverRegistry.class);
        diceRoller = mock(DiceRoller.class);
        rollRepository = mock(RollRepository.class);
        rollService = new RollService(characterService, sheetCalculatorRegistry, mechanicResolverRegistry, diceRoller, rollRepository);

        Character character = Character.create(UUID.randomUUID(), "Fjord", "dnd5e");
        when(characterService.getMineActive(any(Jwt.class), eq(CHARACTER_ID))).thenReturn(character);
        SheetCalculator sheetCalculator = mock(SheetCalculator.class);
        when(sheetCalculator.calculateVitals(anyString())).thenReturn(notOverloadedVitals());
        when(sheetCalculatorRegistry.forSystem("dnd5e")).thenReturn(sheetCalculator);
        mechanicResolver = mock(MechanicResolver.class);
        when(mechanicResolverRegistry.forSystem("dnd5e")).thenReturn(mechanicResolver);
        when(rollRepository.save(any(Roll.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /** A minimal, otherwise-empty vitals fixture — real, not mocked, with no forced roll modes. */
    private VitalsZone notOverloadedVitals() {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), List.of(),
                new Coins(0, 0, 0, 0, 0), List.of(), null, List.of(), new HitDice(10, 5, 0), List.of(), List.of(),
                List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    @Test
    void advantageRollsTwiceAndKeepsTheHigherResult() {
        resolveAsSingleD20(4, "Strength: check");
        when(diceRoller.roll(2, 20)).thenReturn(new int[] {5, 17});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "strength", true, false, null));

        assertThat(roll.total()).isEqualTo(21);
        assertThat(roll.results()).containsExactly(5, 17);
        assertThat(roll.context()).isEqualTo("Strength: check (advantage)");
    }

    @Test
    void disadvantageRollsTwiceAndKeepsTheLowerResult() {
        resolveAsSingleD20(4, "Strength: check");
        when(diceRoller.roll(2, 20)).thenReturn(new int[] {5, 17});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "strength", false, true, null));

        assertThat(roll.total()).isEqualTo(9);
        assertThat(roll.context()).isEqualTo("Strength: check (disadvantage)");
    }

    @Test
    void advantageAndDisadvantageTogetherCancelOutToANormalRoll() {
        resolveAsSingleD20(4, "Strength: check");
        when(diceRoller.roll(1, 20)).thenReturn(new int[] {12});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "strength", true, true, null));

        assertThat(roll.total()).isEqualTo(16);
        assertThat(roll.context()).isEqualTo("Strength: check");
    }

    @Test
    void anOverloadedCharacterRollsStrengthChecksWithDisadvantageEvenWhenNoneWasRequested() {
        SheetCalculator sheetCalculator = mock(SheetCalculator.class);
        when(sheetCalculator.calculateVitals(anyString())).thenReturn(overloadedVitals());
        when(sheetCalculatorRegistry.forSystem("dnd5e")).thenReturn(sheetCalculator);
        resolveAsSingleD20(4, "Strength: check");
        when(diceRoller.roll(2, 20)).thenReturn(new int[] {5, 17});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "strength", false, false, null));

        assertThat(roll.total()).isEqualTo(9);
        assertThat(roll.context()).isEqualTo("Strength: check (disadvantage)");
    }

    @Test
    void anOverloadedCharactersForcedDisadvantageStillCancelsWithRequestedAdvantage() {
        SheetCalculator sheetCalculator = mock(SheetCalculator.class);
        when(sheetCalculator.calculateVitals(anyString())).thenReturn(overloadedVitals());
        when(sheetCalculatorRegistry.forSystem("dnd5e")).thenReturn(sheetCalculator);
        resolveAsSingleD20(4, "Strength: check");
        when(diceRoller.roll(1, 20)).thenReturn(new int[] {12});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "strength", true, false, null));

        assertThat(roll.total()).isEqualTo(16);
        assertThat(roll.context()).isEqualTo("Strength: check");
    }

    @Test
    void anOverloadedCharacterDoesNotGetDisadvantageOnANonStrengthAbilityCheck() {
        SheetCalculator sheetCalculator = mock(SheetCalculator.class);
        when(sheetCalculator.calculateVitals(anyString())).thenReturn(overloadedVitals());
        when(sheetCalculatorRegistry.forSystem("dnd5e")).thenReturn(sheetCalculator);
        resolveAsSingleD20(1, "Dexterity: check");
        when(diceRoller.roll(1, 20)).thenReturn(new int[] {14});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ABILITY_CHECK, "dexterity", false, false, null));

        assertThat(roll.context()).isEqualTo("Dexterity: check");
    }

    private VitalsZone overloadedVitals() {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), List.of(),
                new Coins(0, 0, 0, 0, 0), List.of(), null, List.of(), new HitDice(10, 5, 0), List.of(), List.of(),
                List.of(), new Encumbrance(true, 100, 50, true, List.of()), 1,
                Map.of("ABILITY_CHECK:strength", new RollModeInfo(List.of(), List.of("Overloaded")),
                        "SAVING_THROW:strength", new RollModeInfo(List.of(), List.of("Overloaded"))));
    }

    @Test
    void aForcedAttackModeAppliesToSpellAttacksToo() {
        SheetCalculator sheetCalculator = mock(SheetCalculator.class);
        CalculatedValue zero = new CalculatedValue(0, List.of());
        when(sheetCalculator.calculateVitals(anyString())).thenReturn(new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of("poisoned"), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), List.of(),
                new Coins(0, 0, 0, 0, 0), List.of(), null, List.of(), new HitDice(10, 5, 0), List.of(), List.of(),
                List.of(), new Encumbrance(false, 0, 0, false, List.of()), 1,
                Map.of("ATTACK", new RollModeInfo(List.of(), List.of("Poisoned")))));
        when(sheetCalculatorRegistry.forSystem("dnd5e")).thenReturn(sheetCalculator);
        when(mechanicResolver.resolve(any(), eq(RollKind.SPELL_ATTACK), any(), any())).thenReturn(new ResolvedRoll(1, 20, 5, "Fire Bolt: spell attack roll"));
        when(diceRoller.roll(2, 20)).thenReturn(new int[] {18, 3});

        Roll roll = rollService.roll(mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.SPELL_ATTACK, "fire-bolt", false, false, null));

        assertThat(roll.total()).isEqualTo(8);
        assertThat(roll.context()).isEqualTo("Fire Bolt: spell attack roll (disadvantage)");
    }

    @Test
    void rejectsAdvantageOnAMultiDieRoll() {
        resolveAs(2, 6, 3, "Longsword: damage");

        assertThatThrownBy(() -> rollService.roll(
                        mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ATTACK_DAMAGE, "longsword", true, false, null)))
                .isInstanceOf(IneligibleRollModeException.class);
        verifyNoInteractions(rollRepository);
    }

    @Test
    void rejectsAdvantageOnASingleDieRollThatIsNotAD20() {
        resolveAs(1, 4, 0, "Dagger: damage");

        assertThatThrownBy(() -> rollService.roll(
                        mock(Jwt.class), CHARACTER_ID, new RollRequest(RollKind.ATTACK_DAMAGE, "dagger", false, true, null)))
                .isInstanceOf(IneligibleRollModeException.class);
        verifyNoInteractions(rollRepository);
    }

    @Test
    void aCreationRollKeepsOnlyTheHighestDiceAndWorksOnADraft() {
        Character draft = Character.create(UUID.randomUUID(), "Mez", "dnd5e");
        when(characterService.getMine(any(Jwt.class), eq(CHARACTER_ID))).thenReturn(draft);
        when(diceRoller.roll(4, 6)).thenReturn(new int[] {5, 2, 6, 3});

        Roll roll = rollService.creationRoll(mock(Jwt.class), CHARACTER_ID,
                new CreationRollRequest(DieType.D6, 4, 3, "Strength (4d6 drop lowest)"));

        assertThat(roll.expression()).isEqualTo("4d6kh3");
        assertThat(roll.results()).containsExactly(5, 2, 6, 3);
        assertThat(roll.total()).isEqualTo(14);
        assertThat(roll.context()).isEqualTo("Strength (4d6 drop lowest)");
        assertThat(roll.characterId()).isEqualTo(draft.id());
    }

    @Test
    void aCreationRollWithoutKeepSumsEveryDie() {
        when(characterService.getMine(any(Jwt.class), eq(CHARACTER_ID))).thenReturn(Character.create(UUID.randomUUID(), "Mez", "dnd5e"));
        when(diceRoller.roll(1, 8)).thenReturn(new int[] {7});

        Roll roll = rollService.creationRoll(mock(Jwt.class), CHARACTER_ID, new CreationRollRequest(DieType.D8, 1, null, "Hit points, Warlock 2 (d8)"));

        assertThat(roll.expression()).isEqualTo("1d8");
        assertThat(roll.total()).isEqualTo(7);
    }

    private void resolveAsSingleD20(int modifier, String context) {
        resolveAs(1, 20, modifier, context);
    }

    private void resolveAs(int diceCount, int diceSides, int modifier, String context) {
        when(mechanicResolver.resolve(any(), any(RollKind.class), anyString(), any()))
                .thenReturn(new ResolvedRoll(diceCount, diceSides, modifier, context));
    }
}
