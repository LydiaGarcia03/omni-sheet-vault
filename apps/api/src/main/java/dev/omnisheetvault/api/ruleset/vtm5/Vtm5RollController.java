package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

/** VtM 5e rolls, resolved and stored on the server: dice pools with Hunger dice, Rouse Checks and Willpower re-rolls. */
@RestController
class Vtm5RollController {

    private static final String REROLL_OF = "Re-roll of ";
    private static final String ROUSE = "Rouse Check";
    private static final String FRENZY = "Frenzy check";
    private static final String REMORSE = "Remorse";
    private static final int MAX_REROLLED_DICE = 3;
    private static final Pattern HUNGER_NOTE = Pattern.compile("Hunger (\\d+)");
    private static final Pattern HUMANITY_LOST = Pattern.compile("Humanity (\\d+) → (\\d+)");

    /** One VtM roll as the sheet's log shows it. */
    record Vtm5Roll(UUID id, String kind, String label, List<Integer> faces, int hunger, int successes, int difficulty, int margin, boolean win, boolean critical,
            boolean messy, boolean bestial, String outcome, boolean rerolled, Instant rolledAt) {
    }

    private final CharacterService characterService;
    private final RollService rollService;
    private final Vtm5SheetActions actions;
    private final ObjectMapper objectMapper;
    private final Vtm5Messages messages;

    Vtm5RollController(CharacterService characterService, RollService rollService, Vtm5SheetActions actions, ObjectMapper objectMapper,
            Vtm5Messages messages) {
        this.characterService = characterService;
        this.rollService = rollService;
        this.actions = actions;
        this.objectMapper = objectMapper;
        this.messages = messages;
    }

    /** Rolls a pool: the named traits' ratings plus {@code bonus} (at least one die), Hunger dice up to the current Hunger, against an optional {@code difficulty}. */
    @PostMapping("/api/vtm5/characters/{id}/rolls")
    Vtm5Roll pool(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode request) {
        Character character = mine(jwt, id);
        JsonNode sheet = objectMapper.readTree(character.sheet());
        List<String> traits = new ArrayList<>();
        int pool = request.path("bonus").asInt(0);
        for (JsonNode trait : request.path("traits")) {
            traits.add(trait.asString());
            pool += rating(sheet, trait.asString());
        }
        pool = Math.max(1, pool);
        int hunger = Math.min(sheet.path("hunger").asInt(), pool);
        String label = request.path("label").asString("").isBlank() ? String.join(" + ", traits) : request.path("label").asString();
        int[] faces = rollService.rollDice(pool, 10);
        int difficulty = Math.clamp(request.path("difficulty").asInt(0), 0, 20);
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(faces, hunger, difficulty);
        Roll roll = rollService.recordReadRoll(id, Vtm5Dice.expression(pool, hunger, difficulty), label + tagSuffix(outcome), faces, outcome.successes());
        return view(roll, false);
    }

    /** One die: 6+ passes, anything else raises Hunger by one (at Hunger 5 the Beast stirs instead). */
    @PostMapping("/api/vtm5/characters/{id}/rolls/rouse")
    Vtm5Roll rouse(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody(required = false) JsonNode request) {
        Character character = mine(jwt, id);
        ObjectNode sheet = (ObjectNode) objectMapper.readTree(character.sheet());
        int[] faces = rollService.rollDice(1, 10);
        boolean passed = faces[0] >= 6;
        int hunger = sheet.path("hunger").asInt();
        String reason = request == null ? "" : request.path("reason").asString("");
        String note = passed ? "Passed" : hunger >= 5 ? "Failed at Hunger 5: hunger frenzy test" : "Hunger " + (hunger + 1);
        if (!passed && hunger < 5) {
            sheet.put("hunger", hunger + 1);
            characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        }
        Roll roll = rollService.recordReadRoll(id, "1d10 rouse", ROUSE + (reason.isBlank() ? "" : " · " + reason) + " — " + note, faces, passed ? 1 : 0);
        return view(roll, false);
    }

    /** Resists frenzy: current Willpower plus a third of Humanity (rounded down), no Hunger dice, against the Difficulty. */
    @PostMapping("/api/vtm5/characters/{id}/rolls/frenzy")
    Vtm5Roll frenzy(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode request) {
        JsonNode sheet = objectMapper.readTree(mine(jwt, id).sheet());
        int willpower = sheet.path("attributes").path("Composure").asInt() + sheet.path("attributes").path("Resolve").asInt()
                - sheet.path("willpower").path("superficial").asInt() - sheet.path("willpower").path("aggravated").asInt();
        int pool = Math.max(1, willpower + sheet.path("humanity").asInt() / 3);
        int difficulty = Math.clamp(request.path("difficulty").asInt(3), 1, 20);
        int[] faces = rollService.rollDice(pool, 10);
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(faces, 0, difficulty);
        String result = outcome.win() ? "Resisted" : "Frenzy";
        Roll roll = rollService.recordReadRoll(id, Vtm5Dice.expression(pool, 0, difficulty), FRENZY + " — " + result, faces, outcome.successes());
        return view(roll, false);
    }

    /**
     * End-of-session Remorse: one die per empty, unstained Humanity box (at least one), no Hunger dice. Any success
     * keeps Humanity; none loses a dot. Either way the stains clear.
     */
    @PostMapping("/api/vtm5/characters/{id}/rolls/remorse")
    Vtm5Roll remorse(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        ObjectNode sheet = (ObjectNode) objectMapper.readTree(mine(jwt, id).sheet());
        int humanity = sheet.path("humanity").asInt();
        int stains = sheet.path("stains").asInt();
        if (stains == 0) {
            throw new InvalidBuildException(messages.text("roll.noStains"));
        }
        int pool = Math.max(1, 10 - humanity - stains);
        int[] faces = rollService.rollDice(pool, 10);
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(faces, 0);
        boolean kept = outcome.successes() > 0;
        sheet.put("humanity", kept ? humanity : Math.max(0, humanity - 1));
        sheet.put("stains", 0);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        String result = kept ? "Humanity kept" : "Humanity " + humanity + " → " + Math.max(0, humanity - 1);
        Roll roll = rollService.recordReadRoll(id, Vtm5Dice.expression(pool, 0), REMORSE + " — " + result, faces, outcome.successes());
        return view(roll, false);
    }

    /** Spends one Willpower (one Superficial damage) to re-roll up to three failed regular dice of an earlier pool. */
    @PostMapping("/api/vtm5/characters/{id}/rolls/{rollId}/reroll")
    Vtm5Roll reroll(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable UUID rollId) {
        Character character = mine(jwt, id);
        List<Roll> history = rollService.history(jwt, id);
        Roll original = history.stream().filter(r -> r.id().equals(rollId)).findFirst()
                .orElseThrow(() -> new InvalidBuildException(messages.text("roll.noSuchRoll")));
        int hunger = Vtm5Dice.hungerOf(original.expression());
        if (hunger < 0 || original.context().startsWith(FRENZY) || original.context().startsWith(REMORSE)) {
            throw new InvalidBuildException(messages.text("roll.onlyPoolReroll"));
        }
        if (history.stream().anyMatch(r -> r.context().startsWith(REROLL_OF + rollId))) {
            throw new InvalidBuildException(messages.text("roll.alreadyRerolled"));
        }
        int[] faces = original.results().clone();
        int regular = faces.length - hunger;
        int changed = 0;
        for (int i = 0; i < regular && changed < MAX_REROLLED_DICE; i++) {
            if (faces[i] < 6) {
                faces[i] = rollService.rollDice(1, 10)[0];
                changed++;
            }
        }
        if (changed == 0) {
            throw new InvalidBuildException(messages.text("roll.nothingToReroll"));
        }
        ObjectNode sheet = (ObjectNode) objectMapper.readTree(character.sheet());
        actions.apply(sheet, objectMapper.createObjectNode().put("type", "damage").put("track", "willpower").put("kind", "superficial").put("amount", 1));
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(faces, hunger, Vtm5Dice.difficultyOf(original.expression()));
        String label = labelOf(original.context());
        Roll roll = rollService.recordReadRoll(id, original.expression(), REROLL_OF + rollId + ": " + label + tagSuffix(outcome), faces, outcome.successes());
        return view(roll, false);
    }

    @GetMapping("/api/vtm5/characters/{id}/rolls")
    List<Vtm5Roll> history(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        mine(jwt, id);
        List<Roll> rolls = rollService.history(jwt, id);
        return rolls.stream()
                .map(roll -> view(roll, rolls.stream().anyMatch(other -> other.context().startsWith(REROLL_OF + roll.id()))))
                .toList();
    }

    /** A stored roll as the log shows it, in the request's language; the stored context stays English. */
    private Vtm5Roll view(Roll roll, boolean rerolled) {
        List<Integer> faces = Arrays.stream(roll.results()).boxed().toList();
        if (roll.expression().endsWith("rouse")) {
            return new Vtm5Roll(roll.id(), "rouse", shownRouse(roll.context()), faces, 0, roll.total(), 0, 0, roll.total() > 0, false, false, false, "",
                    false, roll.rolledAt());
        }
        int hunger = Math.max(0, Vtm5Dice.hungerOf(roll.expression()));
        Vtm5Dice.Outcome outcome = Vtm5Dice.read(roll.results(), hunger, Vtm5Dice.difficultyOf(roll.expression()));
        String kind = roll.context().startsWith(REROLL_OF) ? "reroll"
                : roll.context().startsWith(FRENZY) ? "frenzy" : roll.context().startsWith(REMORSE) ? "remorse" : "pool";
        String label = switch (kind) {
            case "frenzy" -> shownFrenzy(roll.context());
            case "remorse" -> shownRemorse(roll.context());
            default -> shownPool(labelOf(roll.context()));
        };
        return new Vtm5Roll(roll.id(), kind, label, faces, hunger, outcome.successes(), outcome.difficulty(), outcome.margin(), outcome.win(),
                outcome.critical(), outcome.messy(), outcome.bestial(), shownTag(outcome), rerolled, roll.rolledAt());
    }

    /** "Strength + Brawl": each trait in the request's language; a label the player typed stays as it is. */
    private String shownPool(String label) {
        return Arrays.stream(label.split(" \\+ ")).map(messages::term).collect(Collectors.joining(" + "));
    }

    /** "Rouse Check · reason — Hunger 3". */
    private String shownRouse(String context) {
        int split = context.lastIndexOf(" — ");
        String head = split < 0 ? context : context.substring(0, split);
        String note = split < 0 ? "" : context.substring(split + 3);
        String shownHead = messages.text("roll.rouse") + head.substring(Math.min(head.length(), ROUSE.length()));
        Matcher hunger = HUNGER_NOTE.matcher(note);
        String shownNote = switch (note) {
            case "Passed" -> messages.text("roll.passed");
            case "Failed at Hunger 5: hunger frenzy test" -> messages.text("roll.hungerFrenzy");
            default -> hunger.matches() ? messages.text("roll.hungerRises", hunger.group(1)) : note;
        };
        return shownNote.isEmpty() ? shownHead : shownHead + " — " + shownNote;
    }

    private String shownFrenzy(String context) {
        String result = context.endsWith("Resisted") ? messages.text("roll.resisted") : messages.text("roll.frenzied");
        return messages.text("roll.frenzy") + " — " + result;
    }

    private String shownRemorse(String context) {
        Matcher lost = HUMANITY_LOST.matcher(context);
        String result = lost.find() ? messages.text("roll.humanityLost", lost.group(1), lost.group(2)) : messages.text("roll.humanityKept");
        return messages.text("roll.remorse") + " — " + result;
    }

    private String shownTag(Vtm5Dice.Outcome outcome) {
        if (!outcome.win()) {
            return messages.text(outcome.bestial() ? "outcome.bestialFailure"
                    : outcome.difficulty() > 0 && outcome.successes() > 0 ? "outcome.failureAtCost" : "outcome.failure");
        }
        return outcome.messy() ? messages.text("outcome.messyCritical")
                : outcome.critical() ? messages.text("outcome.critical") : outcome.difficulty() > 0 ? messages.text("outcome.success") : "";
    }

    /** The roll's own name: without the re-roll prefix and the outcome suffix. */
    private static String labelOf(String context) {
        String label = context.startsWith(REROLL_OF) ? context.substring(context.indexOf(':') + 2) : context;
        int tag = label.lastIndexOf(" — ");
        return tag < 0 ? label : label.substring(0, tag);
    }

    private static String tagSuffix(Vtm5Dice.Outcome outcome) {
        return outcome.tag().isEmpty() ? "" : " — " + outcome.tag();
    }

    private static int rating(JsonNode sheet, String trait) {
        if (sheet.path("attributes").has(trait)) return sheet.path("attributes").path(trait).asInt();
        if (sheet.path("skills").has(trait)) return sheet.path("skills").path(trait).asInt();
        if (sheet.path("disciplines").has(trait)) return sheet.path("disciplines").path(trait).path("dots").asInt();
        throw new InvalidBuildException("Unknown trait: " + trait);
    }

    private Character mine(Jwt jwt, UUID id) {
        Character character = characterService.getMineActive(jwt, id);
        if (!Vtm5GameSystem.SYSTEM_ID.equals(character.systemId())) {
            throw new UnsupportedGameSystemException(character.systemId());
        }
        return character;
    }
}
