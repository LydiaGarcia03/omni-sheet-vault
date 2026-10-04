package dev.omnisheetvault.api.catalogue;

import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps a 5etools item's structured magic-item fields to {@code data.mechanics}: modifiers
 * in the adr-0007 vocabulary plus damage and condition defenses. Only fields whose
 * meaning is unambiguous are mapped. Additive ability scores ({@code {"con":2}}) are
 * left out because the same shape covers worn items (Ioun Stones) and one-time books
 * (Manual of Bodily Health), and consumables (potions) never get mechanics.
 */
final class ItemMechanicsMapper {

    private static final Map<String, String> ABILITY_TARGETS = Map.of(
            "str", "STRENGTH_SCORE", "dex", "DEXTERITY_SCORE", "con", "CONSTITUTION_SCORE",
            "int", "INTELLIGENCE_SCORE", "wis", "WISDOM_SCORE", "cha", "CHARISMA_SCORE");

    private final ObjectMapper objectMapper;

    ItemMechanicsMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * {@code wornArmor} marks armor and shields, whose own {@code bonusAc} is already
     * part of their armor class; only other items (Cloak of Protection) get it as a modifier.
     */
    JsonNode mechanics(JsonNode item, boolean wornArmor, boolean consumable) {
        if (consumable) {
            return objectMapper.nullNode();
        }
        ObjectNode mechanics = objectMapper.createObjectNode();
        ArrayNode modifiers = mechanics.putArray("modifiers");
        if (!wornArmor) {
            addBonus(modifiers, item.get("bonusAc"), "ARMOR_CLASS");
        }
        addBonus(modifiers, item.get("bonusSavingThrow"), "SAVING_THROWS");
        addBonus(modifiers, item.get("bonusSpellAttack"), "SPELL_ATTACKS");
        addBonus(modifiers, item.get("bonusSpellSaveDc"), "SPELL_SAVE_DC");
        addStaticAbilityScores(modifiers, item.path("ability").path("static"));
        mechanics.set("damageResistances", texts(item.get("resist")));
        mechanics.set("damageImmunities", texts(item.get("immune")));
        mechanics.set("damageVulnerabilities", texts(item.get("vulnerable")));
        mechanics.set("conditionImmunities", texts(item.get("conditionImmune")));
        return isEmpty(mechanics) ? objectMapper.nullNode() : mechanics;
    }

    private void addBonus(ArrayNode modifiers, JsonNode bonus, String target) {
        Integer value = signedInt(bonus);
        if (value != null) {
            modifiers.addObject().put("type", "BONUS").put("target", target).put("value", value);
        }
    }

    private void addStaticAbilityScores(ArrayNode modifiers, JsonNode scores) {
        ABILITY_TARGETS.forEach((code, target) -> {
            JsonNode score = scores.get(code);
            if (score != null && score.isNumber()) {
                modifiers.addObject().put("type", "SET").put("target", target).put("value", score.asInt());
            }
        });
    }

    /** A single name or a list of names; conditional entries (objects) are skipped. */
    private ArrayNode texts(JsonNode node) {
        ArrayNode texts = objectMapper.createArrayNode();
        if (node == null) {
            return texts;
        }
        if (node.isString()) {
            texts.add(node.asString());
        } else if (node.isArray()) {
            node.forEach(entry -> {
                if (entry.isString()) {
                    texts.add(entry.asString());
                }
            });
        }
        return texts;
    }

    private static Integer signedInt(JsonNode bonus) {
        if (bonus == null) {
            return null;
        }
        try {
            return Integer.valueOf(bonus.asString().replace("+", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isEmpty(ObjectNode mechanics) {
        for (String field : new String[] {"modifiers", "damageResistances", "damageImmunities", "damageVulnerabilities", "conditionImmunities"}) {
            if (!mechanics.get(field).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
