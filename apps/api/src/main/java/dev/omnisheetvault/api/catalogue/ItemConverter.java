package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools item entry (mundane or magic) into this project's own catalogue
 * shape — systems/dnd-5e/features/inventory-equipment-mechanics.md's field mapping table. Reads both
 * {@code items-base.json} (mundane weapons/armour/gear, the {@code baseitem} array) and
 * {@code items.json} (magic items, the {@code item} array): both single files directly
 * under the data root, unlike spells' per-sourcebook split, and both share this one
 * converter since their weapon/armour shape is identical.
 */
final class ItemConverter implements FiveEToolsConverter {

    private enum ItemKind {
        WEAPON, ARMOR, SHIELD, GEAR
    }

    /** Confident subset only — the rest fall back to the raw 5etools type code. */
    private static final Map<String, String> TYPE_LABELS = Map.ofEntries(
            Map.entry("M", "Melee Weapon"),
            Map.entry("R", "Ranged Weapon"),
            Map.entry("A", "Ammunition"),
            Map.entry("LA", "Light Armor"),
            Map.entry("MA", "Medium Armor"),
            Map.entry("HA", "Heavy Armor"),
            Map.entry("S", "Shield"),
            Map.entry("WD", "Wand"),
            Map.entry("RD", "Rod"),
            Map.entry("RG", "Ring"),
            Map.entry("P", "Potion"),
            Map.entry("SC", "Scroll"),
            Map.entry("G", "Adventuring Gear"),
            Map.entry("T", "Tool"),
            Map.entry("SCF", "Spellcasting Focus"),
            Map.entry("AT", "Artisan's Tools"),
            Map.entry("INS", "Instrument"),
            Map.entry("GS", "Gaming Set"));

    private static final Map<String, String> WEAPON_PROPERTY_NAMES = Map.ofEntries(
            Map.entry("A", "Ammunition"),
            Map.entry("F", "Finesse"),
            Map.entry("H", "Heavy"),
            Map.entry("L", "Light"),
            Map.entry("LD", "Loading"),
            Map.entry("R", "Reach"),
            Map.entry("S", "Special"),
            Map.entry("T", "Thrown"),
            Map.entry("V", "Versatile"),
            Map.entry("2H", "Two-Handed"));

    private static final Map<String, String> DAMAGE_TYPE_NAMES =
            Map.of("B", "bludgeoning", "P", "piercing", "S", "slashing");

    private static final Pattern DICE = Pattern.compile("(\\d++)d(\\d++)");
    /** A charge-cast item's own flat save DC, e.g. Wand of Fireballs' "cast the fireball spell (save {@dc 15})" — a fact of the item, not the wielder's own spellcasting ability. */
    private static final Pattern FIXED_SAVE_DC = Pattern.compile("\\{@dc\\s+(\\d+)");

    private static final String POTION_TYPE_CODE = "P";

    private final ObjectMapper objectMapper;
    private final ItemMechanicsMapper mechanicsMapper;

    ItemConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.mechanicsMapper = new ItemMechanicsMapper(objectMapper);
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.ITEM;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> items = new ArrayList<>();
        addArrayEntries(items, dataSource.readDataFile("items-base.json"), "baseitem");
        addArrayEntries(items, dataSource.readDataFile("items.json"), "item");
        return items;
    }

    private static void addArrayEntries(List<JsonNode> target, JsonNode file, String arrayField) {
        JsonNode array = file.get(arrayField);
        if (array != null) {
            for (JsonNode entry : array) {
                target.add(entry);
            }
        }
    }

    @Override
    public CatalogueEntryImport convert(JsonNode item) {
        String name = item.get("name").asString();
        String rawSource = item.get("source").asString();
        String typeCode = typeCode(item);
        ItemKind itemKind = itemKind(item, typeCode);

        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.ITEM,
                FiveEToolsNaming.slug(name),
                name,
                rawSource,
                intOrNull(item.get("page")),
                List.of(),
                description(item.get("entries")),
                data(item, itemKind, typeCode));
    }

    private JsonNode data(JsonNode item, ItemKind itemKind, String typeCode) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("itemKind", itemKind.name());
        data.put("typeLabel", typeLabel(item, typeCode));
        data.put("rarity", rarityOrNone(item.get("rarity")));
        putStringOrNull(data, "focusType", item.get("scfType") == null ? null : item.get("scfType").asString());

        AttunementResult attunement = attunement(item.get("reqAttune"));
        data.put("requiresAttunement", attunement.requires());
        putStringOrNull(data, "attunementRequirement", attunement.requirement());

        putDoubleOrNull(data, "weightLb", doubleOrNull(item.get("weight")));
        putDoubleOrNull(data, "costGp", costGp(item.get("value")));

        populateWeaponFields(data, item, itemKind);
        populateArmorFields(data, item, itemKind, typeCode);
        populateBonusFields(data, item);
        populateChargeFields(data, item);
        data.set("mechanics", mechanicsMapper.mechanics(
                item, itemKind == ItemKind.ARMOR || itemKind == ItemKind.SHIELD, POTION_TYPE_CODE.equals(typeCode)));

        return data;
    }

    private void populateWeaponFields(ObjectNode data, JsonNode item, ItemKind itemKind) {
        if (itemKind != ItemKind.WEAPON) {
            data.putNull("weaponCategory");
            data.putNull("attackType");
            data.putNull("damageDiceCount");
            data.putNull("damageDiceSides");
            data.putNull("damageType");
            data.putNull("versatileDamageDiceCount");
            data.putNull("versatileDamageDiceSides");
            data.putArray("properties");
            data.put("finesse", false);
            data.putNull("normalRange");
            data.putNull("longRange");
            return;
        }

        String typeCode = typeCode(item);
        putStringOrNull(data, "weaponCategory", weaponCategory(item.get("weaponCategory")));
        putStringOrNull(data, "attackType", attackType(typeCode));

        Dice damage = dice(item.get("dmg1"));
        putIntOrNull(data, "damageDiceCount", damage.count());
        putIntOrNull(data, "damageDiceSides", damage.sides());
        putStringOrNull(data, "damageType", damageTypeName(item.get("dmgType")));

        Dice versatile = dice(item.get("dmg2"));
        putIntOrNull(data, "versatileDamageDiceCount", versatile.count());
        putIntOrNull(data, "versatileDamageDiceSides", versatile.sides());

        JsonNode propertyArray = item.get("property");
        ArrayNode propertiesNode = data.putArray("properties");
        for (String property : weaponProperties(propertyArray)) {
            propertiesNode.add(property);
        }
        data.put("finesse", hasProperty(propertyArray, "F"));

        Range range = range(item.get("range"));
        putIntOrNull(data, "normalRange", range.normal());
        putIntOrNull(data, "longRange", range.longRange());
    }

    private void populateArmorFields(ObjectNode data, JsonNode item, ItemKind itemKind, String typeCode) {
        if (itemKind != ItemKind.ARMOR && itemKind != ItemKind.SHIELD) {
            data.putNull("armorCategory");
            data.putNull("baseArmorClass");
            data.put("stealthDisadvantage", false);
            data.putNull("strengthRequirement");
            return;
        }

        putStringOrNull(data, "armorCategory", armorCategory(typeCode));
        putIntOrNull(data, "baseArmorClass", intOrNull(item.get("ac")));
        data.put("stealthDisadvantage", isTrue(item.get("stealth")));
        putIntOrNull(data, "strengthRequirement", intOrNull(item.get("strength")));
    }

    private void populateBonusFields(ObjectNode data, JsonNode item) {
        Integer combinedWeaponBonus = bonusInt(item.get("bonusWeapon"));
        Integer attackBonus = firstNonNull(bonusInt(item.get("bonusWeaponAttack")), combinedWeaponBonus);
        Integer damageBonus = firstNonNull(bonusInt(item.get("bonusWeaponDamage")), combinedWeaponBonus);
        putIntOrNull(data, "weaponAttackBonus", attackBonus);
        putIntOrNull(data, "weaponDamageBonus", damageBonus);
        putIntOrNull(data, "armorClassBonus", bonusInt(item.get("bonusAc")));
    }

    private void populateChargeFields(ObjectNode data, JsonNode item) {
        putIntOrNull(data, "charges", intOrNull(item.get("charges")));
        putStringOrNull(data, "rechargeTrigger", rechargeTrigger(item.get("recharge")));
        putStringOrNull(data, "rechargeFormula", rechargeFormula(item.get("rechargeAmount")));

        Integer fixedSaveDc = fixedSaveDc(item.get("entries"));
        ArrayNode grantedSpellsNode = data.putArray("grantedSpells");
        for (GrantedSpell grantedSpell : grantedSpells(item.get("attachedSpells"))) {
            ObjectNode grantedSpellNode = objectMapper.createObjectNode();
            grantedSpellNode.put("spellSlug", grantedSpell.spellSlug());
            grantedSpellNode.put("chargeCost", grantedSpell.chargeCost());
            putIntOrNull(grantedSpellNode, "fixedSaveDc", fixedSaveDc);
            grantedSpellsNode.add(grantedSpellNode);
        }
    }

    private static Integer fixedSaveDc(JsonNode entriesArray) {
        if (entriesArray == null) {
            return null;
        }
        Matcher matcher = FIXED_SAVE_DC.matcher(FiveEToolsNaming.rawEntriesText(entriesArray));
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    private static String typeCode(JsonNode item) {
        JsonNode type = item.get("type");
        return type == null ? null : FiveEToolsNaming.stripSourceSuffix(type.asString());
    }

    private static ItemKind itemKind(JsonNode item, String typeCode) {
        if (isTrue(item.get("weapon"))) {
            return ItemKind.WEAPON;
        }
        if ("S".equals(typeCode)) {
            return ItemKind.SHIELD;
        }
        if (isTrue(item.get("armor")) || "LA".equals(typeCode) || "MA".equals(typeCode) || "HA".equals(typeCode)) {
            return ItemKind.ARMOR;
        }
        return ItemKind.GEAR;
    }

    private static String typeLabel(JsonNode item, String typeCode) {
        if (typeCode != null) {
            return TYPE_LABELS.getOrDefault(typeCode, typeCode);
        }
        return isTrue(item.get("wondrous")) ? "Wondrous Item" : "Adventuring Gear";
    }

    private static String rarityOrNone(JsonNode rarityNode) {
        return rarityNode == null ? "none" : rarityNode.asString();
    }

    private record AttunementResult(boolean requires, String requirement) {
        private static final AttunementResult NONE = new AttunementResult(false, null);
    }

    private static AttunementResult attunement(JsonNode reqAttune) {
        if (reqAttune == null) {
            return AttunementResult.NONE;
        }
        if (reqAttune.isString()) {
            return new AttunementResult(true, reqAttune.asString());
        }
        return new AttunementResult(reqAttune.asBoolean(), null);
    }

    private static String weaponCategory(JsonNode weaponCategoryNode) {
        return weaponCategoryNode == null ? null : weaponCategoryNode.asString().toUpperCase(Locale.ROOT);
    }

    private static String attackType(String typeCode) {
        if ("M".equals(typeCode)) {
            return "MELEE";
        }
        if ("R".equals(typeCode)) {
            return "RANGED";
        }
        return null;
    }

    private static String armorCategory(String typeCode) {
        if ("LA".equals(typeCode)) {
            return "LIGHT";
        }
        if ("MA".equals(typeCode)) {
            return "MEDIUM";
        }
        if ("HA".equals(typeCode)) {
            return "HEAVY";
        }
        return null;
    }

    private record Dice(Integer count, Integer sides) {
        private static final Dice NONE = new Dice(null, null);
    }

    private static Dice dice(JsonNode diceNode) {
        if (diceNode == null) {
            return Dice.NONE;
        }
        Matcher matcher = DICE.matcher(diceNode.asString());
        if (matcher.find()) {
            return new Dice(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
        }
        return Dice.NONE;
    }

    private static String damageTypeName(JsonNode dmgType) {
        if (dmgType == null) {
            return null;
        }
        String code = dmgType.asString().toUpperCase(Locale.ROOT);
        return DAMAGE_TYPE_NAMES.getOrDefault(code, dmgType.asString());
    }

    private static List<String> weaponProperties(JsonNode propertyArray) {
        List<String> properties = new ArrayList<>();
        if (propertyArray != null) {
            for (JsonNode property : propertyArray) {
                String code = FiveEToolsNaming.stripSourceSuffix(property.asString());
                properties.add(WEAPON_PROPERTY_NAMES.getOrDefault(code, code));
            }
        }
        return properties;
    }

    private static boolean hasProperty(JsonNode propertyArray, String targetCode) {
        if (propertyArray == null) {
            return false;
        }
        for (JsonNode property : propertyArray) {
            if (targetCode.equals(FiveEToolsNaming.stripSourceSuffix(property.asString()))) {
                return true;
            }
        }
        return false;
    }

    private record Range(Integer normal, Integer longRange) {
        private static final Range NONE = new Range(null, null);
    }

    private static Range range(JsonNode rangeNode) {
        if (rangeNode == null) {
            return Range.NONE;
        }
        String[] parts = rangeNode.asString().split("/");
        Integer normal = Integer.valueOf(parts[0].trim());
        Integer longRange = parts.length > 1 ? Integer.valueOf(parts[1].trim()) : null;
        return new Range(normal, longRange);
    }

    /** {@code bonusWeapon}/{@code bonusWeaponAttack}/{@code bonusWeaponDamage}/{@code bonusAc} are plain {@code "+N"} strings on the rare named items that carry them literally (as opposed to a generic {@code +N} variant, out of scope — see this feature's own doc). */
    private static Integer bonusInt(JsonNode bonusNode) {
        if (bonusNode == null) {
            return null;
        }
        try {
            return Integer.valueOf(bonusNode.asString().replace("+", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer firstNonNull(Integer preferred, Integer fallback) {
        return preferred != null ? preferred : fallback;
    }

    private static String rechargeTrigger(JsonNode recharge) {
        return recharge == null ? null : FiveEToolsNaming.capitalize(recharge.asString());
    }

    private static String rechargeFormula(JsonNode rechargeAmount) {
        return rechargeAmount == null ? null : TagMarkupStripper.strip(rechargeAmount.asString());
    }

    private record GrantedSpell(String spellSlug, int chargeCost) {
    }

    /** {@code attachedSpells.charges} maps a charge cost to the spell(s) it casts — the Wand of Fireballs mechanic. */
    private static List<GrantedSpell> grantedSpells(JsonNode attachedSpells) {
        List<GrantedSpell> result = new ArrayList<>();
        if (attachedSpells == null) {
            return result;
        }
        JsonNode charges = attachedSpells.get("charges");
        if (charges == null) {
            return result;
        }
        for (Map.Entry<String, JsonNode> entry : charges.properties()) {
            int chargeCost = Integer.parseInt(entry.getKey());
            for (JsonNode spellRef : entry.getValue()) {
                String slug = FiveEToolsNaming.slug(FiveEToolsNaming.stripSourceSuffix(spellRef.asString()));
                result.add(new GrantedSpell(slug, chargeCost));
            }
        }
        result.sort(Comparator.comparingInt(GrantedSpell::chargeCost));
        return result;
    }

    private static String description(JsonNode entriesArray) {
        if (entriesArray == null) {
            return "";
        }
        return TagMarkupStripper.strip(FiveEToolsNaming.rawEntriesText(entriesArray));
    }

    private static boolean isTrue(JsonNode node) {
        return node != null && node.asBoolean();
    }

    /** Null (rather than a crash) for the rare item whose count is itself a rollable {@code {@dice}} formula instead of a fixed number (e.g. Stonemaker War Pick's charges) — modelling a variable maximum is out of this pass's scope. */
    private static Integer intOrNull(JsonNode node) {
        return node == null || !node.isNumber() ? null : Integer.valueOf(node.asInt());
    }

    private static Double doubleOrNull(JsonNode node) {
        return node == null ? null : Double.valueOf(node.asDouble());
    }

    private static Double costGp(JsonNode value) {
        return value == null ? null : value.asDouble() / 100.0;
    }

    private static void putStringOrNull(ObjectNode node, String field, String value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value);
        }
    }

    private static void putIntOrNull(ObjectNode node, String field, Integer value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value.intValue());
        }
    }

    private static void putDoubleOrNull(ObjectNode node, String field, Double value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value.doubleValue());
        }
    }
}
