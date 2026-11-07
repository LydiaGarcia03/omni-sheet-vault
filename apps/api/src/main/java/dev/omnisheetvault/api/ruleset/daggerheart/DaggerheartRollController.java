package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Daggerheart rolls, resolved and stored on the server (SRD pp. 47–50): Duality action and reaction rolls with
 * their Hope and Stress effects, damage rolls by Proficiency, and the history with the rests' dice.
 */
@RestController
class DaggerheartRollController {

    static final int DUALITY_DIE = 12;
    static final int ADVANTAGE_DIE = 6;
    static final int UNARMED_DIE = 4;
    private static final Set<Integer> DAMAGE_DICE = Set.of(4, 6, 8, 10, 12, 20);
    private static final int MAX_DAMAGE_DICE = 20;

    /** One roll as the sheet's log shows it; {@code sides} gives each of {@code dice} its die. Duality fields are zero for other rolls. */
    record DaggerheartRoll(UUID id, String kind, String label, List<Integer> dice, List<Integer> sides, int hope, int fear, int advantageDie, int modifier,
            int total, int difficulty, boolean critical, boolean withHope, Boolean success, String outcome, Instant rolledAt) {
    }

    /** A roll and the sheet it changed (Hope gained, Stress cleared, Hope spent on Experiences). */
    record RollResult(DaggerheartRoll roll, ObjectNode sheet) {
    }

    private final CharacterService characterService;
    private final RollService rollService;
    private final ObjectMapper objectMapper;
    private final DaggerheartDerivedStats derivedStats;

    DaggerheartRollController(CharacterService characterService, RollService rollService, ObjectMapper objectMapper) {
        this.characterService = characterService;
        this.rollService = rollService;
        this.objectMapper = objectMapper;
        this.derivedStats = new DaggerheartDerivedStats(objectMapper);
    }

    /**
     * A Duality roll: the trait's value (none, a trait, or "spellcast"), each used Experience for 1 Hope, a flat
     * modifier, advantage or disadvantage, and an optional Difficulty. An action roll with Hope gains a Hope and a
     * critical also clears a Stress; a reaction roll does neither.
     */
    @PostMapping("/api/daggerheart/characters/{id}/rolls/duality")
    RollResult duality(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode request) {
        ObjectNode sheet = storedSheet(mine(jwt, id));
        ObjectNode derived = derivedStats.derive(sheet);
        ObjectNode trackers = (ObjectNode) sheet.path("trackers");
        boolean reaction = request.path("reaction").asBoolean(false);

        List<String> parts = new ArrayList<>();
        int modifier = 0;
        String trait = traitOf(request.path("trait").asString(""), derived);
        if (!trait.isEmpty()) {
            modifier += derived.path("traits").path(trait).path("value").asInt();
            parts.add(capitalize(trait));
        }
        int hopeSpent = 0;
        for (JsonNode index : request.path("experiences")) {
            JsonNode experience = sheet.path("experiences").path(index.asInt(-1));
            if (!experience.isObject()) {
                throw new InvalidBuildException("No Experience at " + index.asInt() + ".");
            }
            modifier += experience.path("bonus").asInt();
            parts.add(experience.path("name").asString());
            hopeSpent++;
        }
        int hope = trackers.path("hope").asInt();
        if (hopeSpent > hope) {
            throw new InvalidBuildException("Using " + hopeSpent + " Experience" + (hopeSpent == 1 ? "" : "s") + " costs " + hopeSpent + " Hope; you have " + hope + ".");
        }
        int flat = request.path("modifier").asInt(0);
        modifier += flat;

        DaggerheartDice.Advantage advantage = switch (request.path("advantage").asString("none")) {
            case "advantage" -> DaggerheartDice.Advantage.ADVANTAGE;
            case "disadvantage" -> DaggerheartDice.Advantage.DISADVANTAGE;
            default -> DaggerheartDice.Advantage.NONE;
        };
        int[] dice = rollService.rollDice(2, DUALITY_DIE);
        int d6 = advantage == DaggerheartDice.Advantage.NONE ? 0 : rollService.rollDice(1, ADVANTAGE_DIE)[0];
        List<String> extraDice = new ArrayList<>();
        List<Integer> extraFaces = new ArrayList<>();
        for (JsonNode group : request.path("dice")) {
            int count = group.path("count").asInt(0);
            int sides = group.path("sides").asInt(0);
            if (count < 1 || count > MAX_DAMAGE_DICE || !DAMAGE_DICE.contains(sides)) {
                throw new InvalidBuildException("Extra dice are d4, d6, d8, d10, d12 or d20.");
            }
            extraDice.add(count + "d" + sides);
            for (int face : rollService.rollDice(count, sides)) {
                extraFaces.add(face);
            }
        }
        DaggerheartDice.Duality roll = DaggerheartDice.duality(dice[0], dice[1], advantage, d6, modifier, request.path("difficulty").asInt(0), reaction)
                .withExtras(extraDice, extraFaces);

        hope -= hopeSpent;
        if (!reaction && roll.withHope()) {
            hope = Math.min(derived.path("maxHope").asInt(), hope + 1);
        }
        if (!reaction && roll.critical()) {
            trackers.put("stressMarked", Math.max(0, trackers.path("stressMarked").asInt() - 1));
        }
        trackers.put("hope", hope);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));

        String label = request.path("label").asString("").isBlank() ? (parts.isEmpty() ? "Duality roll" : String.join(" + ", parts)) : request.path("label").asString();
        if (flat != 0 && request.path("label").asString("").isBlank()) {
            label += " " + DaggerheartDice.signed(flat);
        }
        Roll stored = rollService.recordReadRoll(id, roll.expression(), label + (reaction ? " (reaction)" : ""), roll.results(), roll.total());
        return new RollResult(view(stored), view(sheet));
    }

    /**
     * A damage roll: an equipped weapon ([Proficiency] dice of its die plus its bonus), unarmed ([Proficiency]d4),
     * or custom dice; a critical adds the dice's maximum.
     */
    @PostMapping("/api/daggerheart/characters/{id}/rolls/damage")
    DaggerheartRoll damage(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode request) {
        ObjectNode sheet = storedSheet(mine(jwt, id));
        int proficiency = derivedStats.derive(sheet).path("proficiency").asInt(1);
        String weaponSlot = request.path("weapon").asString("");
        int count;
        int sides;
        int bonus;
        String label;
        if (weaponSlot.equals("unarmed")) {
            count = proficiency;
            sides = UNARMED_DIE;
            bonus = 0;
            label = "Unarmed damage";
        } else if (!weaponSlot.isEmpty()) {
            JsonNode weapon = sheet.path("weapons").path(weaponSlot);
            if (!weapon.isObject()) {
                throw new InvalidBuildException("No " + weaponSlot + " weapon equipped.");
            }
            count = proficiency;
            sides = Integer.parseInt(weapon.path("damage").path("die").asString("d6").substring(1));
            bonus = weapon.path("damage").path("bonus").asInt();
            label = weapon.path("name").asString() + " damage";
        } else {
            count = request.path("count").asInt(1);
            sides = request.path("sides").asInt(6);
            bonus = request.path("bonus").asInt(0);
            label = request.path("label").asString("").isBlank() ? "Damage" : request.path("label").asString();
        }
        if (count < 1 || count > MAX_DAMAGE_DICE || !DAMAGE_DICE.contains(sides)) {
            throw new InvalidBuildException("A damage roll takes 1 to " + MAX_DAMAGE_DICE + " dice of d4, d6, d8, d10, d12 or d20.");
        }
        boolean critical = request.path("critical").asBoolean(false);
        DaggerheartDice.Damage roll = new DaggerheartDice.Damage(rollService.rollDice(count, sides), sides, bonus, critical);
        return view(rollService.recordReadRoll(id, roll.expression(), label + (critical ? " (critical)" : ""), roll.dice(), roll.total()));
    }

    /** A free roll from the dice tray: any mix of d4–d20 plus a flat modifier. */
    @PostMapping("/api/daggerheart/characters/{id}/rolls/custom")
    DaggerheartRoll custom(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode request) {
        mine(jwt, id);
        List<int[]> groups = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        int dice = 0;
        for (JsonNode group : request.path("dice")) {
            int count = group.path("count").asInt(0);
            int sides = group.path("sides").asInt(0);
            if (count < 1 || !DAMAGE_DICE.contains(sides)) {
                throw new InvalidBuildException("A custom roll takes d4, d6, d8, d10, d12 or d20.");
            }
            dice += count;
            groups.add(new int[] {count, sides});
            for (int face : rollService.rollDice(count, sides)) {
                faces.add(face);
            }
        }
        if (dice < 1 || dice > MAX_DAMAGE_DICE) {
            throw new InvalidBuildException("A custom roll takes 1 to " + MAX_DAMAGE_DICE + " dice.");
        }
        boolean critical = request.path("critical").asBoolean(false);
        DaggerheartDice.Custom roll = new DaggerheartDice.Custom(groups, faces.stream().mapToInt(Integer::intValue).toArray(), request.path("bonus").asInt(0), critical);
        String label = request.path("label").asString("").isBlank() ? "Custom roll" : request.path("label").asString();
        return view(rollService.recordReadRoll(id, roll.expression(), label, roll.faces(), roll.total()));
    }

    @GetMapping("/api/daggerheart/characters/{id}/rolls")
    List<DaggerheartRoll> history(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        mine(jwt, id);
        return rollService.history(jwt, id).stream().map(this::view).toList();
    }

    private DaggerheartRoll view(Roll roll) {
        List<Integer> dice = Arrays.stream(roll.results()).boxed().toList();
        DaggerheartDice.Duality duality = DaggerheartDice.readDuality(roll.expression(), roll.results());
        if (duality != null) {
            List<Integer> sides = new ArrayList<>(List.of(DUALITY_DIE, DUALITY_DIE));
            if (duality.advantageDie() != 0) {
                sides.add(ADVANTAGE_DIE);
            }
            duality.extraDice().forEach(group -> sides.addAll(sidesOf(group)));
            return new DaggerheartRoll(roll.id(), duality.reaction() ? "reaction" : "duality", roll.context(), dice, sides, duality.hope(), duality.fear(),
                    duality.advantageDie(), duality.modifier(), duality.total(), duality.difficulty(), duality.critical(), duality.withHope(),
                    duality.success(), duality.outcome(), roll.rolledAt());
        }
        DaggerheartDice.Damage damage = DaggerheartDice.readDamage(roll.expression(), roll.results());
        if (damage != null) {
            return new DaggerheartRoll(roll.id(), "damage", roll.context(), dice, sidesOf(dice.size() + "d" + damage.sides()), 0, 0, 0, damage.bonus(),
                    damage.total(), 0, damage.critical(), false, null, damage.total() + " damage", roll.rolledAt());
        }
        DaggerheartDice.Custom custom = DaggerheartDice.readCustom(roll.expression(), roll.results());
        if (custom != null) {
            List<Integer> sides = custom.groups().stream().flatMap(group -> sidesOf(group[0] + "d" + group[1]).stream()).toList();
            return new DaggerheartRoll(roll.id(), "custom", roll.context(), dice, sides, 0, 0, 0, custom.bonus(), custom.total(), 0, custom.critical(), false,
                    null, String.valueOf(custom.total()), roll.rolledAt());
        }
        int tier = DaggerheartDice.restTier(roll.expression());
        String kind = tier >= 0 ? "rest" : "other";
        return new DaggerheartRoll(roll.id(), kind, roll.context(), dice, tier >= 0 ? List.of(4) : List.of(), 0, 0, 0, Math.max(0, tier), roll.total(), 0,
                false, false, null, tier >= 0 ? "cleared up to " + roll.total() : String.valueOf(roll.total()), roll.rolledAt());
    }

    /** "2d6" → [6, 6]. */
    private static List<Integer> sidesOf(String group) {
        String[] parts = group.split("d");
        return Collections.nCopies(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private ObjectNode view(ObjectNode stored) {
        ObjectNode sheet = stored.deepCopy();
        sheet.set("derived", derivedStats.derive(sheet));
        return sheet;
    }

    /** "spellcast" reads the subclass's Spellcast trait; an empty trait rolls with no trait. */
    private static String traitOf(String requested, ObjectNode derived) {
        String trait = requested.strip().toLowerCase(Locale.ROOT);
        if (trait.equals("spellcast")) {
            trait = derived.path("spellcastTrait").asString("").toLowerCase(Locale.ROOT);
            if (trait.isEmpty()) {
                throw new InvalidBuildException("This character has no Spellcast trait.");
            }
        }
        if (!trait.isEmpty() && !DaggerheartBuild.TRAITS.contains(trait)) {
            throw new InvalidBuildException("Unknown trait: " + requested);
        }
        return trait;
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private dev.omnisheetvault.api.character.Character mine(Jwt jwt, UUID id) {
        dev.omnisheetvault.api.character.Character character = characterService.getMineActive(jwt, id);
        if (!DaggerheartGameSystem.SYSTEM_ID.equals(character.systemId())) {
            throw new UnsupportedGameSystemException(character.systemId());
        }
        return character;
    }

    private ObjectNode storedSheet(dev.omnisheetvault.api.character.Character character) {
        return (ObjectNode) objectMapper.readTree(character.sheet());
    }
}
