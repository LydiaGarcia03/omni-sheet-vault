package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Contribution;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The values a Daggerheart sheet derives from what it stores (SRD pp. 4–5, 55, 72): traits, Evasion,
 * Armor Score, damage thresholds, HP and Stress slots, each with the contributions that make it up,
 * plus the equipped weapons' attack lines. Always-on bonuses come from the features' and the
 * equipped gear's {@code modifiers}.
 */
final class DaggerheartDerivedStats {

    static final int MAX_ARMOR_SCORE = 12;
    static final int MAX_HOPE = 6;
    private static final String SPELLCAST = "Spellcast";

    private final ObjectMapper objectMapper;

    DaggerheartDerivedStats(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ObjectNode derive(JsonNode sheet) {
        Map<String, List<Contribution>> bonuses = bonuses(sheet);
        int level = sheet.path("level").asInt(1);
        JsonNode base = sheet.path("base");
        JsonNode armor = sheet.path("armor");
        boolean armored = armor.isObject();

        ObjectNode derived = objectMapper.createObjectNode();
        ObjectNode traits = derived.putObject("traits");
        Map<String, CalculatedValue> traitValues = new LinkedHashMap<>();
        for (String trait : DaggerheartBuild.TRAITS) {
            JsonNode stored = sheet.path("traits").path(trait);
            List<Contribution> parts = new ArrayList<>();
            if (stored.isInt()) {
                parts.add(new Contribution("Assigned", stored.asInt()));
            }
            CalculatedValue value = total(parts, bonuses.get(trait));
            traitValues.put(trait, value);
            traits.set(trait, objectMapper.valueToTree(value));
        }
        put(derived, "evasion", total(List.of(new Contribution(base.path("origin").asString(), base.path("evasion").asInt())), bonuses.get("evasion")));
        CalculatedValue armorScore = total(armored ? List.of(new Contribution(armor.path("name").asString(), armor.path("baseScore").asInt())) : List.of(),
                bonuses.get("armorScore"));
        put(derived, "armorScore", new CalculatedValue(Math.clamp(armorScore.value(), 0, MAX_ARMOR_SCORE), armorScore.contributions()));
        put(derived, "majorThreshold", threshold(armored ? armor.path("baseThresholds").path("major").asInt() : 0, armor, level, bonuses.get("majorThreshold")));
        put(derived, "severeThreshold", threshold(armored ? armor.path("baseThresholds").path("severe").asInt() : level, armor, level, bonuses.get("severeThreshold")));
        put(derived, "hitPointSlots", capped(total(List.of(new Contribution(base.path("origin").asString(), base.path("hitPointSlots").asInt())), bonuses.get("hitPointSlots"))));
        put(derived, "stressSlots", capped(total(List.of(new Contribution("Every character", base.path("stressSlots").asInt())), bonuses.get("stressSlots"))));
        derived.put("maxHope", MAX_HOPE);
        derived.put("proficiency", sheet.path("proficiency").asInt(1));
        String spellcastTrait = sheet.path("identity").path("subclass").path("spellcastTrait").asString("");
        derived.put("spellcastTrait", spellcastTrait);
        attacks(derived.putArray("attacks"), sheet, traitValues, spellcastTrait);
        return derived;
    }

    /** Unarmored, the Major threshold is the level and the Severe threshold twice the level. */
    private static CalculatedValue threshold(int armorBase, JsonNode armor, int level, List<Contribution> bonuses) {
        List<Contribution> parts = new ArrayList<>();
        if (armor.isObject()) {
            parts.add(new Contribution(armor.path("name").asString(), armorBase));
        } else if (armorBase > 0) {
            parts.add(new Contribution("Unarmored", armorBase));
        }
        parts.add(new Contribution("Level", level));
        return total(parts, bonuses);
    }

    /** Every always-on bonus, by target ("evasion", "agility"…), labelled with the feature that grants it. */
    private static Map<String, List<Contribution>> bonuses(JsonNode sheet) {
        Map<String, List<Contribution>> bonuses = new LinkedHashMap<>();
        sheet.path("features").forEach(feature -> collect(bonuses, feature.path("name").asString(), feature.path("modifiers")));
        sheet.path("advancements").forEach(gain -> bonuses.computeIfAbsent(gain.path("target").asString(), key -> new ArrayList<>())
                .add(new Contribution("Level " + gain.path("level").asInt() + " advancement", gain.path("value").asInt())));
        for (JsonNode gear : List.of(sheet.path("weapons").path("primary"), sheet.path("weapons").path("secondary"), sheet.path("armor"))) {
            if (gear.isObject()) {
                collect(bonuses, gear.path("name").asString(), gear.path("feature").path("modifiers"));
            }
        }
        return bonuses;
    }

    private static void collect(Map<String, List<Contribution>> bonuses, String source, JsonNode modifiers) {
        modifiers.forEach(modifier -> bonuses.computeIfAbsent(modifier.path("target").asString(), key -> new ArrayList<>())
                .add(new Contribution(source, modifier.path("value").asInt())));
    }

    /** HP and Stress slots top out at 12 (SRD p. 50). */
    private static CalculatedValue capped(CalculatedValue slots) {
        return new CalculatedValue(Math.min(slots.value(), DaggerheartLevels.MAX_SLOTS), slots.contributions());
    }

    private static CalculatedValue total(List<Contribution> base, List<Contribution> bonuses) {
        List<Contribution> parts = new ArrayList<>(base);
        if (bonuses != null) {
            parts.addAll(bonuses);
        }
        return new CalculatedValue(parts.stream().mapToInt(Contribution::amount).sum(), parts);
    }

    private void put(ObjectNode derived, String field, CalculatedValue value) {
        derived.set(field, objectMapper.valueToTree(value));
    }

    /** One line per equipped weapon: its trait's modifier and its damage with Proficiency dice ("1d8+1 phy"). */
    private void attacks(ArrayNode attacks, JsonNode sheet, Map<String, CalculatedValue> traits, String spellcastTrait) {
        int proficiency = sheet.path("proficiency").asInt(1);
        for (JsonNode weapon : List.of(sheet.path("weapons").path("primary"), sheet.path("weapons").path("secondary"))) {
            if (!weapon.isObject()) {
                continue;
            }
            String trait = weapon.path("trait").asString();
            String usedTrait = SPELLCAST.equals(trait) ? spellcastTrait : trait;
            CalculatedValue modifier = traits.get(usedTrait.toLowerCase(Locale.ROOT));
            JsonNode damage = weapon.path("damage");
            int bonus = damage.path("bonus").asInt();
            ObjectNode attack = attacks.addObject();
            attack.put("name", weapon.path("name").asString());
            attack.put("slot", weapon.path("slot").asString());
            attack.put("trait", usedTrait);
            attack.put("modifier", modifier == null ? 0 : modifier.value());
            attack.put("range", weapon.path("range").asString());
            attack.put("damage", proficiency + damage.path("die").asString() + (bonus > 0 ? "+" + bonus : ""));
            attack.put("damageType", damage.path("type").asString());
            attack.put("burden", weapon.path("burden").asInt());
        }
    }
}
