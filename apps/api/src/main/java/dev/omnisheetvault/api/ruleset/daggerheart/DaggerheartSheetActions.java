package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Daggerheart's play-state changes on a stored sheet (SRD pp. 50–53): HP, Stress, Armor Slots and
 * Hope, damage against the thresholds, conditions, gold, inventory, notes, the loadout and vault,
 * short and long rests with their downtime moves, and the player's own counters.
 */
final class DaggerheartSheetActions {

    static final int LOADOUT_SIZE = 5;
    static final int MOVES_PER_REST = 2;
    static final int SHORT_RESTS_BEFORE_LONG = 3;
    static final String VULNERABLE = "Vulnerable";
    private static final int GOLD_PER_UNIT = 10;
    private static final Set<String> TRACKS = Set.of("hitPoints", "stress", "armorSlots", "hope");
    private static final Set<String> SHORT_MOVES = Set.of("tendToWounds", "clearStress", "repairArmor", "prepare");
    private static final Set<String> LONG_MOVES = Set.of("tendToAllWounds", "clearAllStress", "repairAllArmor", "prepare", "workOnAProject");
    private static final Map<String, String> MOVE_NAMES = Map.of("tendToWounds", "Tend to Wounds", "clearStress", "Clear Stress", "repairArmor", "Repair Armor",
            "prepare", "Prepare", "tendToAllWounds", "Tend to All Wounds", "clearAllStress", "Clear All Stress", "repairAllArmor", "Repair All Armor",
            "workOnAProject", "Work on a Project");

    private final ObjectMapper objectMapper;
    private final DaggerheartDerivedStats derivedStats;
    /** Rolls one die with the given number of sides. */
    private final IntUnaryOperator die;

    DaggerheartSheetActions(ObjectMapper objectMapper) {
        this(objectMapper, sides -> ThreadLocalRandom.current().nextInt(1, sides + 1));
    }

    DaggerheartSheetActions(ObjectMapper objectMapper, IntUnaryOperator die) {
        this.objectMapper = objectMapper;
        this.derivedStats = new DaggerheartDerivedStats(objectMapper);
        this.die = die;
    }

    /** Applies one action to {@code sheet} in place. */
    void apply(ObjectNode sheet, JsonNode action) {
        Limits limits = limits(sheet);
        switch (action.path("type").asString("")) {
            case "track" -> track(sheet, limits, action.path("track").asString(""), action.path("delta").asInt());
            case "damage" -> damage(sheet, limits, action.path("amount").asInt(), action.path("useArmor").asBoolean(false));
            case "conditions" -> conditions(sheet, action.path("value"));
            case "gold" -> gold(sheet, action);
            case "notes" -> sheet.put("notes", action.path("value").asString(""));
            case "addItem" -> addItem(sheet, action);
            case "updateItem" -> updateItem(sheet, action);
            case "removeItem" -> {
                item(sheet, action.path("index").asInt(-1));
                inventory(sheet).remove(action.path("index").asInt());
            }
            case "moveCard" -> moveCard(sheet, limits, action);
            case "rest" -> rest(sheet, limits, action);
            case "counter" -> counter(sheet, action);
            default -> throw new InvalidBuildException("Unknown Daggerheart action: " + action.path("type").asString(""));
        }
        syncStressVulnerability(sheet, limits(sheet));
    }

    record Limits(int hitPoints, int stress, int armorSlots, int hope, int tier) {
        int of(String track) {
            return switch (track) {
                case "hitPoints" -> hitPoints;
                case "stress" -> stress;
                case "armorSlots" -> armorSlots;
                default -> hope;
            };
        }
    }

    private Limits limits(ObjectNode sheet) {
        ObjectNode derived = derivedStats.derive(sheet);
        return new Limits(derived.path("hitPointSlots").path("value").asInt(), derived.path("stressSlots").path("value").asInt(),
                derived.path("armorScore").path("value").asInt(), derived.path("maxHope").asInt(), DaggerheartLevels.tier(sheet.path("level").asInt(1)));
    }

    // Trackers

    private static ObjectNode trackers(ObjectNode sheet) {
        return sheet.has("trackers") ? (ObjectNode) sheet.get("trackers") : sheet.putObject("trackers");
    }

    private static String field(String track) {
        return switch (track) {
            case "hitPoints" -> "hitPointsMarked";
            case "stress" -> "stressMarked";
            case "armorSlots" -> "armorSlotsMarked";
            default -> "hope";
        };
    }

    /** Marks or clears slots; Stress that can't be marked marks 1 HP instead (SRD p. 50). */
    private static void track(ObjectNode sheet, Limits limits, String track, int delta) {
        if (!TRACKS.contains(track)) {
            throw new InvalidBuildException("Unknown tracker: " + track);
        }
        ObjectNode trackers = trackers(sheet);
        int current = trackers.path(field(track)).asInt();
        int wanted = current + delta;
        int max = limits.of(track);
        if (track.equals("stress") && delta > 0 && wanted > max) {
            trackers.put("hitPointsMarked", Math.min(limits.hitPoints(), trackers.path("hitPointsMarked").asInt() + 1));
        }
        if (track.equals("armorSlots") && delta > 0 && max == 0) {
            throw new InvalidBuildException("With an Armor Score of 0 you can't mark Armor Slots.");
        }
        trackers.put(field(track), Math.clamp(wanted, 0, max));
    }

    /** Marks 1, 2 or 3 HP by the thresholds; an Armor Slot first drops the damage one threshold. */
    private void damage(ObjectNode sheet, Limits limits, int amount, boolean useArmor) {
        if (amount < 0) {
            throw new InvalidBuildException("Damage can't be negative.");
        }
        ObjectNode derived = derivedStats.derive(sheet);
        int major = derived.path("majorThreshold").path("value").asInt();
        int severe = derived.path("severeThreshold").path("value").asInt();
        int severity = amount <= 0 ? 0 : amount >= severe ? 3 : amount >= major ? 2 : 1;
        ObjectNode trackers = trackers(sheet);
        boolean armorUsed = false;
        if (useArmor && severity > 0) {
            if (trackers.path("armorSlotsMarked").asInt() >= limits.armorSlots()) {
                throw new InvalidBuildException("No Armor Slot left to mark.");
            }
            trackers.put("armorSlotsMarked", trackers.path("armorSlotsMarked").asInt() + 1);
            severity--;
            armorUsed = true;
        }
        int before = trackers.path("hitPointsMarked").asInt();
        int after = Math.min(limits.hitPoints(), before + severity);
        trackers.put("hitPointsMarked", after);
        sheet.putObject("lastDamage").put("amount", amount).put("hitPoints", after - before).put("armorUsed", armorUsed)
                .put("fell", after == limits.hitPoints() && after > before);
    }

    /** Marking the last Stress makes the character Vulnerable until they clear one; only that Vulnerable is removed. */
    private static void syncStressVulnerability(ObjectNode sheet, Limits limits) {
        boolean full = limits.stress() > 0 && trackers(sheet).path("stressMarked").asInt() >= limits.stress();
        boolean fromStress = sheet.path("vulnerableFromStress").asBoolean(false);
        List<String> conditions = conditionList(sheet);
        if (full && !conditions.contains(VULNERABLE)) {
            conditions.add(VULNERABLE);
            sheet.put("vulnerableFromStress", true);
        } else if (!full && fromStress) {
            conditions.remove(VULNERABLE);
            sheet.put("vulnerableFromStress", false);
        }
        ArrayNode stored = sheet.putArray("conditions");
        conditions.forEach(stored::add);
    }

    private static List<String> conditionList(ObjectNode sheet) {
        List<String> conditions = new ArrayList<>();
        sheet.path("conditions").forEach(condition -> conditions.add(condition.asString()));
        return conditions;
    }

    /** The same condition can't apply twice (SRD p. 51). */
    private static void conditions(ObjectNode sheet, JsonNode value) {
        Set<String> conditions = new LinkedHashSet<>();
        value.forEach(condition -> {
            String name = condition.asString("").strip();
            if (!name.isEmpty()) {
                conditions.add(name);
            }
        });
        if (!conditions.contains(VULNERABLE)) {
            sheet.put("vulnerableFromStress", false);
        }
        ArrayNode stored = sheet.putArray("conditions");
        conditions.forEach(stored::add);
    }

    /** Ten handfuls make a bag and ten bags a chest (SRD p. 55); the total is kept in the fewest coins. */
    private static void gold(ObjectNode sheet, JsonNode action) {
        int handfuls = Math.max(0, action.path("handfuls").asInt());
        int bags = Math.max(0, action.path("bags").asInt());
        int chests = Math.max(0, action.path("chests").asInt());
        int total = handfuls + bags * GOLD_PER_UNIT + chests * GOLD_PER_UNIT * GOLD_PER_UNIT;
        sheet.putObject("gold")
                .put("handfuls", total % GOLD_PER_UNIT)
                .put("bags", total / GOLD_PER_UNIT % GOLD_PER_UNIT)
                .put("chests", total / (GOLD_PER_UNIT * GOLD_PER_UNIT));
    }

    // Inventory

    private static ArrayNode inventory(ObjectNode sheet) {
        return sheet.has("inventory") ? (ArrayNode) sheet.get("inventory") : sheet.putArray("inventory");
    }

    private static ObjectNode item(ObjectNode sheet, int index) {
        ArrayNode inventory = inventory(sheet);
        if (index < 0 || index >= inventory.size()) {
            throw new InvalidBuildException("No item at " + index + ".");
        }
        return (ObjectNode) inventory.get(index);
    }

    private static void addItem(ObjectNode sheet, JsonNode action) {
        String name = action.path("name").asString("").strip();
        if (name.isEmpty()) {
            throw new InvalidBuildException("An item needs a name.");
        }
        inventory(sheet).addObject().put("name", name).put("quantity", Math.max(1, action.path("quantity").asInt(1)));
    }

    private static void updateItem(ObjectNode sheet, JsonNode action) {
        ObjectNode item = item(sheet, action.path("index").asInt(-1));
        if (!action.path("name").asString("").isBlank()) {
            item.put("name", action.path("name").asString().strip());
        }
        if (action.has("quantity")) {
            item.put("quantity", Math.max(1, action.path("quantity").asInt(1)));
        }
    }

    // Domain cards

    /** To the loadout costs the card's Recall Cost in Stress unless it's free (at a rest); to the vault is free (SRD p. 6). */
    private static void moveCard(ObjectNode sheet, Limits limits, JsonNode action) {
        String slug = action.path("slug").asString("");
        boolean toLoadout = "loadout".equals(action.path("to").asString(""));
        ObjectNode card = null;
        int inLoadout = 0;
        for (JsonNode candidate : sheet.path("domainCards")) {
            if (candidate.path("inLoadout").asBoolean(false)) {
                inLoadout++;
            }
            if (candidate.path("slug").asString().equals(slug)) {
                card = (ObjectNode) candidate;
            }
        }
        if (card == null) {
            throw new InvalidBuildException("No domain card " + slug + ".");
        }
        if (card.path("inLoadout").asBoolean(false) == toLoadout) {
            return;
        }
        if (toLoadout) {
            if (inLoadout >= LOADOUT_SIZE) {
                throw new InvalidBuildException("The loadout holds " + LOADOUT_SIZE + " cards; move one to the vault first.");
            }
            int cost = action.path("free").asBoolean(false) ? 0 : card.path("recallCost").asInt();
            ObjectNode trackers = trackers(sheet);
            int stress = trackers.path("stressMarked").asInt();
            if (cost > 0 && stress + cost > limits.stress()) {
                throw new InvalidBuildException("Recalling " + card.path("name").asString() + " needs " + cost + " Stress; you have "
                        + (limits.stress() - stress) + " left.");
            }
            trackers.put("stressMarked", stress + cost);
        }
        card.put("inLoadout", toLoadout);
    }

    // Rests

    /** A short or long rest: up to two downtime moves, the same one allowed twice (SRD p. 52). */
    private void rest(ObjectNode sheet, Limits limits, JsonNode action) {
        boolean isLong = "long".equals(action.path("kind").asString(""));
        JsonNode moves = action.path("moves");
        if (moves.size() > MOVES_PER_REST) {
            throw new InvalidBuildException("A rest has up to " + MOVES_PER_REST + " downtime moves.");
        }
        int shortRests = sheet.path("shortRestsInARow").asInt();
        if (!isLong && shortRests >= SHORT_RESTS_BEFORE_LONG) {
            throw new InvalidBuildException("After three short rests in a row, the next rest must be a long rest.");
        }
        ObjectNode trackers = trackers(sheet);
        ObjectNode result = objectMapper.createObjectNode().put("kind", isLong ? "long" : "short");
        ArrayNode done = result.putArray("moves");
        for (JsonNode move : moves) {
            String name = move.path("move").asString("");
            if (!(isLong ? LONG_MOVES : SHORT_MOVES).contains(name)) {
                throw new InvalidBuildException(name + " is not a " + (isLong ? "long" : "short") + " rest move.");
            }
            ObjectNode entry = done.addObject().put("move", name);
            boolean forAlly = move.path("forAlly").asBoolean(false);
            entry.put("forAlly", forAlly);
            switch (name) {
                case "tendToWounds" -> clear(trackers, "hitPointsMarked", rollClear(entry, limits.tier()), forAlly, entry);
                case "clearStress" -> clear(trackers, "stressMarked", rollClear(entry, limits.tier()), false, entry);
                case "repairArmor" -> clear(trackers, "armorSlotsMarked", rollClear(entry, limits.tier()), forAlly, entry);
                case "tendToAllWounds" -> clear(trackers, "hitPointsMarked", Integer.MAX_VALUE, forAlly, entry);
                case "clearAllStress" -> clear(trackers, "stressMarked", Integer.MAX_VALUE, false, entry);
                case "repairAllArmor" -> clear(trackers, "armorSlotsMarked", Integer.MAX_VALUE, forAlly, entry);
                case "prepare" -> {
                    int gain = move.path("withParty").asBoolean(false) ? 2 : 1;
                    int hope = trackers.path("hope").asInt();
                    trackers.put("hope", Math.min(limits.hope(), hope + gain));
                    entry.put("gained", trackers.path("hope").asInt() - hope);
                }
                default -> entry.put("note", move.path("note").asString(""));
            }
        }
        sheet.put("shortRestsInARow", isLong ? 0 : shortRests + 1);
        sheet.set("lastRest", result);
    }

    static String moveName(String move) {
        return MOVE_NAMES.getOrDefault(move, move);
    }

    /** 1d4 + Tier, recorded on the move. */
    private int rollClear(ObjectNode entry, int tier) {
        int rolled = die.applyAsInt(4);
        entry.put("rolled", rolled).put("tier", tier);
        return rolled + tier;
    }

    private static void clear(ObjectNode trackers, String field, int amount, boolean forAlly, ObjectNode entry) {
        if (forAlly) {
            entry.put("cleared", amount == Integer.MAX_VALUE ? -1 : amount);
            return;
        }
        int marked = trackers.path(field).asInt();
        int cleared = Math.min(marked, amount);
        trackers.put(field, marked - cleared);
        entry.put("cleared", cleared);
    }

    // Counters: class resources and anything else the player tracks (Prayer Dice, Favor, a project's countdown…)

    private static void counter(ObjectNode sheet, JsonNode action) {
        ArrayNode counters = sheet.has("counters") ? (ArrayNode) sheet.get("counters") : sheet.putArray("counters");
        int index = action.path("index").asInt(-1);
        if (action.path("remove").asBoolean(false)) {
            counterAt(counters, index);
            counters.remove(index);
            return;
        }
        ObjectNode counter;
        if (index < 0) {
            String name = action.path("name").asString("").strip();
            if (name.isEmpty()) {
                throw new InvalidBuildException("A counter needs a name.");
            }
            counter = counters.addObject().put("name", name).put("value", 0).put("max", 0);
        } else {
            counter = counterAt(counters, index);
        }
        if (!action.path("name").asString("").isBlank()) {
            counter.put("name", action.path("name").asString().strip());
        }
        if (action.has("max")) {
            counter.put("max", Math.max(0, action.path("max").asInt()));
        }
        if (action.has("value")) {
            int max = counter.path("max").asInt();
            int value = Math.max(0, action.path("value").asInt());
            counter.put("value", max > 0 ? Math.min(max, value) : value);
        }
    }

    private static ObjectNode counterAt(ArrayNode counters, int index) {
        if (index < 0 || index >= counters.size()) {
            throw new InvalidBuildException("No counter at " + index + ".");
        }
        return (ObjectNode) counters.get(index);
    }
}
