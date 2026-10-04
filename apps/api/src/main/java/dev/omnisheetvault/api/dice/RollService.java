package dev.omnisheetvault.api.dice;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.ManualRollRequest.DiceGroupRequest;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollModeInfo;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.ruleset.registry.MechanicResolverRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Orchestrates a roll: ownership through {@link CharacterService}, the vitals through
 * the sheet calculator seam, the dice expression through the mechanic resolver seam,
 * the randomness through {@link DiceRoller}. No game rules live here — see
 * architecture.md. {@link #recordRoll} is the narrower seam a compound action (e.g.
 * {@code CharacterSheetService.spendHitDice}) uses to roll and log dice whose shape
 * it already resolved itself — {@link DiceRoller} and {@link RollRepository} stay
 * private to this package either way.
 */
@Service
public class RollService {

    private final CharacterService characterService;
    private final SheetCalculatorRegistry sheetCalculatorRegistry;
    private final MechanicResolverRegistry mechanicResolverRegistry;
    private final DiceRoller diceRoller;
    private final RollRepository rollRepository;

    public RollService(
            CharacterService characterService,
            SheetCalculatorRegistry sheetCalculatorRegistry,
            MechanicResolverRegistry mechanicResolverRegistry,
            DiceRoller diceRoller,
            RollRepository rollRepository) {
        this.characterService = characterService;
        this.sheetCalculatorRegistry = sheetCalculatorRegistry;
        this.mechanicResolverRegistry = mechanicResolverRegistry;
        this.diceRoller = diceRoller;
        this.rollRepository = rollRepository;
    }

    public Roll roll(Jwt jwt, UUID characterId, RollRequest request) {
        Character character = characterService.getMineActive(jwt, characterId);
        VitalsZone vitals = sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet());
        ResolvedRoll resolved = mechanicResolverRegistry.forSystem(character.systemId())
                .resolve(vitals, request.kind(), request.key(), request.castAtLevel());

        RollModeInfo forced = forcedMode(vitals, request);
        boolean advantage = request.advantage() || (forced != null && !forced.advantageSources().isEmpty());
        boolean disadvantage = request.disadvantage() || (forced != null && !forced.disadvantageSources().isEmpty());
        RollMode mode = RollMode.from(advantage, disadvantage);
        if (mode != RollMode.NORMAL && !isSingleD20(resolved)) {
            throw new IneligibleRollModeException(request.kind());
        }
        int diceCount = mode == RollMode.NORMAL ? resolved.diceCount() : 2;

        int[] results = diceRoller.roll(diceCount, resolved.diceSides());
        int total = keep(results, mode) + resolved.modifier();
        String expression = diceExpression(diceCount, resolved.diceSides(), resolved.modifier());
        String context = mode == RollMode.NORMAL ? resolved.context() : resolved.context() + " (" + mode.label() + ")";

        return rollRepository.save(Roll.create(character.id(), expression, context, results, total));
    }

    /**
     * The mode the sheet forces on this roll (conditions, exhaustion, being Overloaded),
     * whatever the client asked for, since the server owns every rule (architecture.md).
     * Attack and spell attack rolls share the {@code ATTACK} entry.
     */
    private RollModeInfo forcedMode(VitalsZone vitals, RollRequest request) {
        String key = switch (request.kind()) {
            case ATTACK_HIT, SPELL_ATTACK -> "ATTACK";
            case INITIATIVE -> "INITIATIVE";
            case ABILITY_CHECK, SAVING_THROW, SKILL_CHECK -> request.kind().name() + ":" + request.key();
            default -> null;
        };
        return key == null ? null : vitals.rollModes().get(key);
    }

    /**
     * Advantage/disadvantage is a d20 rule (PHB), not "any roll of one die" — a 1d4
     * damage roll is still a single die but is never rolled twice for it.
     */
    private boolean isSingleD20(ResolvedRoll resolved) {
        return resolved.diceCount() == 1 && resolved.diceSides() == 20;
    }

    /**
     * Advantage/disadvantage keeps one of the two dice rather than summing them —
     * everything else (a flat check, a multi-die damage roll) sums as usual.
     */
    private int keep(int[] results, RollMode mode) {
        return switch (mode) {
            case ADVANTAGE -> Arrays.stream(results).max().orElseThrow();
            case DISADVANTAGE -> Arrays.stream(results).min().orElseThrow();
            case NORMAL -> Arrays.stream(results).sum();
        };
    }

    /**
     * A player-picked roll with no mechanic source — see the manual/custom rolling
     * rule in features/character-sheet.md. Dice groups are rolled in {@link DieType}'s
     * own declared order (D4..D100) regardless of the request's own order, so the same
     * selection always produces the same expression shape.
     */
    public Roll manualRoll(Jwt jwt, UUID characterId, ManualRollRequest request) {
        Character character = characterService.getMineActive(jwt, characterId);
        Map<DieType, Integer> countByType = request.dice().stream()
                .collect(Collectors.toMap(DiceGroupRequest::type, DiceGroupRequest::count));

        List<Integer> allResults = new ArrayList<>();
        List<String> expressionParts = new ArrayList<>();
        for (DieType type : DieType.values()) {
            Integer count = countByType.get(type);
            if (count == null) {
                continue;
            }
            for (int result : diceRoller.roll(count, type.sides())) {
                allResults.add(result);
            }
            expressionParts.add(count + "d" + type.sides());
        }

        int[] results = allResults.stream().mapToInt(Integer::intValue).toArray();
        int total = Arrays.stream(results).sum();
        String expression = String.join(" + ", expressionParts);

        return rollRepository.save(Roll.create(character.id(), expression, "Custom: roll", results, total));
    }

    /**
     * A roll the character builder asks for, allowed on a draft: the dice come from the build
     * plan, and only the {@code keepHighest} highest count when it's set.
     */
    public Roll creationRoll(Jwt jwt, UUID characterId, CreationRollRequest request) {
        Character character = characterService.getMine(jwt, characterId);
        int[] results = diceRoller.roll(request.count(), request.die().sides());
        int keep = request.keepHighest() == null ? request.count() : request.keepHighest();
        String expression = KeptDice.expression(request.count(), request.die().sides(), request.keepHighest());
        return rollRepository.save(Roll.create(character.id(), expression, request.context(), results, KeptDice.keptTotal(results, keep)));
    }

    public List<Roll> history(Jwt jwt, UUID characterId) {
        characterService.getMineActive(jwt, characterId);
        return rollRepository.findByCharacterIdOrderByRolledAtDesc(characterId);
    }

    /**
     * Rolls {@code diceCount} dice of {@code diceSides}, adds {@code flatModifier} to
     * their sum, and persists the result — a narrower version of {@link #roll} for a
     * caller (in another package) that already resolved the roll's shape itself and
     * only needs the randomness plus the append-only log, without exposing
     * {@link DiceRoller} or {@link RollRepository} outside this package.
     */
    public Roll recordRoll(UUID characterId, int diceCount, int diceSides, int flatModifier, String context) {
        int[] results = diceRoller.roll(diceCount, diceSides);
        int total = Arrays.stream(results).sum() + flatModifier;
        String expression = diceCount + "d" + diceSides + formatModifier(flatModifier);
        return rollRepository.save(Roll.create(characterId, expression, context, results, total));
    }

    private String formatModifier(int modifier) {
        if (modifier == 0) {
            return "";
        }
        return modifier > 0 ? "+" + modifier : String.valueOf(modifier);
    }

    /**
     * {@code diceCount} of {@code 0} (e.g. Unarmed Strike, a flat ability-modifier
     * roll with nothing random about it — direct owner request, 2026-09-04) has no
     * dice to notate at all; the usual "NdM+mod" shape would render "0d1+3", dice
     * notation for a roll that never happens. That case is just the modifier alone.
     */
    private String diceExpression(int diceCount, int diceSides, int modifier) {
        if (diceCount == 0) {
            return String.valueOf(modifier);
        }
        return diceCount + "d" + diceSides + formatModifier(modifier);
    }
}
