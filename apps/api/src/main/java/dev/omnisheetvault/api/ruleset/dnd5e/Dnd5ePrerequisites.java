package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Evaluates a 5etools {@code prerequisite} array against the grant state: the array is
 * a list of alternatives (any one passing is enough) whose conditions must all pass —
 * the same grouping D&D Beyond's prerequisite validator uses. Conditions this planner
 * can't check yet (campaign, items…) don't fail the check; they're returned as
 * {@code unverified} so the option can say so.
 */
final class Dnd5ePrerequisites {

    private static final Map<String, String> ABILITY_KEYS = Map.of(
            "str", "strength", "dex", "dexterity", "con", "constitution",
            "int", "intelligence", "wis", "wisdom", "cha", "charisma");

    record Result(boolean met, List<String> unverified) {
    }

    private Dnd5ePrerequisites() {
    }

    static Result evaluate(JsonNode prerequisites, Dnd5eGrantState state) {
        if (prerequisites == null || prerequisites.isNull() || prerequisites.isEmpty()) {
            return new Result(true, List.of());
        }
        List<String> unverified = new ArrayList<>();
        for (JsonNode alternative : prerequisites) {
            List<String> alternativeUnverified = new ArrayList<>();
            if (alternativeMet(alternative, state, alternativeUnverified)) {
                return new Result(true, alternativeUnverified);
            }
            unverified.addAll(alternativeUnverified);
        }
        return new Result(false, unverified);
    }

    private static boolean alternativeMet(JsonNode alternative, Dnd5eGrantState state, List<String> unverified) {
        for (Map.Entry<String, JsonNode> condition : alternative.properties()) {
            boolean met = switch (condition.getKey()) {
                case "ability" -> abilityMet(condition.getValue(), state);
                case "level" -> levelMet(condition.getValue(), state);
                case "race" -> raceMet(condition.getValue(), state);
                case "proficiency" -> proficiencyMet(condition.getValue(), state);
                case "pact" -> state.hasOptionalFeature("pact-of-the-" + condition.getValue().asString().toLowerCase(Locale.ROOT));
                case "feat" -> featMet(condition.getValue(), state);
                case "spell" -> spellMet(condition.getValue(), state);
                case "spellcasting", "spellcasting2020", "spellcastingFeature", "spellcastingPrepared" -> state.canCastSpells();
                default -> {
                    unverified.add(condition.getKey());
                    yield true;
                }
            };
            if (!met) {
                return false;
            }
        }
        return true;
    }

    private static boolean abilityMet(JsonNode abilityOptions, Dnd5eGrantState state) {
        for (JsonNode option : abilityOptions) {
            for (Map.Entry<String, JsonNode> minimum : option.properties()) {
                String ability = ABILITY_KEYS.get(minimum.getKey());
                if (ability != null && state.score(ability) >= minimum.getValue().asInt()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A class-bound level ("5th-level warlock") checks that class's level; a bare level checks character level. */
    private static boolean levelMet(JsonNode level, Dnd5eGrantState state) {
        if (level.isNumber()) {
            return state.characterLevel() >= level.asInt();
        }
        int required = level.get("level").asInt();
        JsonNode playerClass = level.get("class");
        if (playerClass == null) {
            return state.characterLevel() >= required;
        }
        return state.classLevel(slug(playerClass.get("name").asString())) >= required;
    }

    private static boolean raceMet(JsonNode races, Dnd5eGrantState state) {
        String species = state.speciesName() == null ? "" : state.speciesName().toLowerCase(Locale.ROOT);
        for (JsonNode race : races) {
            String name = race.path("name").asString("").toLowerCase(Locale.ROOT);
            if (!name.isEmpty() && species.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean proficiencyMet(JsonNode proficiencies, Dnd5eGrantState state) {
        for (JsonNode requirement : proficiencies) {
            for (Map.Entry<String, JsonNode> field : requirement.properties()) {
                String value = field.getValue().asString("").toLowerCase(Locale.ROOT);
                boolean has = switch (field.getKey()) {
                    case "armor" -> state.has(Dnd5eGrantState.Kind.ARMOR, value);
                    case "weapon" -> state.has(Dnd5eGrantState.Kind.WEAPON, value);
                    default -> false;
                };
                if (has) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean featMet(JsonNode feats, Dnd5eGrantState state) {
        for (JsonNode feat : feats) {
            String name = feat.asString().split("\\|")[0];
            if (state.hasFeat(slug(name))) {
                return true;
            }
        }
        return false;
    }

    /** A spell reference ("eldritch blast#c", "fireball|phb") matches by name. */
    private static boolean spellMet(JsonNode spells, Dnd5eGrantState state) {
        for (JsonNode spell : spells) {
            if (state.knowsSpell(spell.asString().split("[#|]")[0])) {
                return true;
            }
        }
        return false;
    }

    static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replace("'", "").replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }
}
