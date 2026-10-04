package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Fixtures are hand-built to match this converter's own assumptions about 5etools'
 * {@code items-base.json}/{@code items.json} shape — same precedent as
 * {@code SpellConverterTest}'s own note. Assumptions are cross-checked against the
 * owner's real downloaded data (Warhammer, Light Crossbow, Scale Mail, Shield, Cloak
 * of Elvenkind, Wand of Fireballs) in systems/dnd-5e/features/inventory-equipment-mechanics.md.
 */
class ItemConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemConverter converter = new ItemConverter(objectMapper);

    @Test
    void convertsAVersatileMartialMeleeWeapon() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Hammer","source":"PHB","page":149,"type":"M","rarity":"none",
                "weight":2,"value":1500,"weaponCategory":"martial","property":["V"],
                "dmg1":"1d8","dmgType":"B","dmg2":"1d10","weapon":true}""");

        assertThat(imported.kind()).isEqualTo(CatalogueEntryKind.ITEM);
        assertThat(imported.slug()).isEqualTo("test-hammer");
        assertThat(imported.sourceBook()).isEqualTo("PHB");
        assertThat(imported.sourcePage()).isEqualTo(149);
        assertThat(imported.description()).isEmpty();

        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("WEAPON");
        assertThat(data.get("typeLabel").asString()).isEqualTo("Melee Weapon");
        assertThat(data.get("rarity").asString()).isEqualTo("none");
        assertThat(data.get("requiresAttunement").asBoolean()).isFalse();
        assertThat(data.get("weightLb").asDouble()).isEqualTo(2);
        assertThat(data.get("costGp").asDouble()).isEqualTo(15);
        assertThat(data.get("weaponCategory").asString()).isEqualTo("MARTIAL");
        assertThat(data.get("attackType").asString()).isEqualTo("MELEE");
        assertThat(data.get("damageDiceCount").asInt()).isEqualTo(1);
        assertThat(data.get("damageDiceSides").asInt()).isEqualTo(8);
        assertThat(data.get("damageType").asString()).isEqualTo("bludgeoning");
        assertThat(data.get("versatileDamageDiceCount").asInt()).isEqualTo(1);
        assertThat(data.get("versatileDamageDiceSides").asInt()).isEqualTo(10);
        assertThat(data.get("properties")).extracting(JsonNode::asString).containsExactly("Versatile");
        assertThat(data.get("finesse").asBoolean()).isFalse();
        assertThat(data.get("normalRange").isNull()).isTrue();
        assertThat(data.get("armorCategory").isNull()).isTrue();
    }

    @Test
    void convertsAFinesseRangedWeaponWithAmmunitionAndRange() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Crossbow","source":"PHB","page":149,"type":"R","rarity":"none",
                "weight":5,"value":2500,"weaponCategory":"simple",
                "property":["A","LD","2H"],"range":"80/320","dmg1":"1d8","dmgType":"P","weapon":true}""");

        JsonNode data = imported.data();
        assertThat(data.get("attackType").asString()).isEqualTo("RANGED");
        assertThat(data.get("properties")).extracting(JsonNode::asString)
                .containsExactly("Ammunition", "Loading", "Two-Handed");
        assertThat(data.get("normalRange").asInt()).isEqualTo(80);
        assertThat(data.get("longRange").asInt()).isEqualTo(320);
    }

    @Test
    void marksFinesseWeaponsFromThePropertyCode() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Rapier","source":"PHB","page":149,"type":"M","rarity":"none",
                "weight":2,"value":2500,"weaponCategory":"martial",
                "property":["F"],"dmg1":"1d8","dmgType":"P","weapon":true}""");

        assertThat(imported.data().get("finesse").asBoolean()).isTrue();
    }

    @Test
    void convertsMediumArmorWithAStealthPenalty() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Scale Mail","source":"PHB","page":144,"type":"MA","rarity":"none",
                "weight":45,"value":5000,"ac":14,"armor":true,"stealth":true}""");

        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("ARMOR");
        assertThat(data.get("armorCategory").asString()).isEqualTo("MEDIUM");
        assertThat(data.get("baseArmorClass").asInt()).isEqualTo(14);
        assertThat(data.get("stealthDisadvantage").asBoolean()).isTrue();
        assertThat(data.get("weaponCategory").isNull()).isTrue();
    }

    @Test
    void convertsAShieldAsItsOwnKindNotArmor() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Shield","source":"PHB","page":144,"type":"S","rarity":"none",
                "weight":6,"value":1000,"ac":2}""");

        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("SHIELD");
        assertThat(data.get("baseArmorClass").asInt()).isEqualTo(2);
        assertThat(data.get("armorCategory").isNull()).isTrue();
    }

    @Test
    void convertsAWondrousItemRequiringAttunementWithNoTypeCode() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Cloak","source":"DMG","page":158,"rarity":"uncommon",
                "reqAttune":true,"wondrous":true,
                "entries":["While you wear this cloak, you gain an effect."]}""");

        assertThat(imported.description()).isEqualTo("While you wear this cloak, you gain an effect.");
        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("GEAR");
        assertThat(data.get("typeLabel").asString()).isEqualTo("Wondrous Item");
        assertThat(data.get("rarity").asString()).isEqualTo("uncommon");
        assertThat(data.get("requiresAttunement").asBoolean()).isTrue();
        assertThat(data.get("attunementRequirement").isNull()).isTrue();
    }

    @Test
    void capturesAStringAttunementRequirement() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Wand","source":"DMG","page":210,"type":"WD","rarity":"rare",
                "reqAttune":"by a spellcaster","weight":1,"charges":7,
                "recharge":"dawn","rechargeAmount":"{@dice 1d6 + 1}",
                "attachedSpells":{"charges":{"1":["fireball"]}},
                "entries":["This wand has 7 charges."]}""");

        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("GEAR");
        assertThat(data.get("typeLabel").asString()).isEqualTo("Wand");
        assertThat(data.get("requiresAttunement").asBoolean()).isTrue();
        assertThat(data.get("attunementRequirement").asString()).isEqualTo("by a spellcaster");
        assertThat(data.get("charges").asInt()).isEqualTo(7);
        assertThat(data.get("rechargeTrigger").asString()).isEqualTo("Dawn");
        assertThat(data.get("rechargeFormula").asString()).isEqualTo("1d6 + 1");

        JsonNode grantedSpells = data.get("grantedSpells");
        assertThat(grantedSpells).hasSize(1);
        assertThat(grantedSpells.get(0).get("spellSlug").asString()).isEqualTo("fireball");
        assertThat(grantedSpells.get(0).get("chargeCost").asInt()).isEqualTo(1);
    }

    @Test
    void stripsTheSourceSuffixFromASpellReferenceBeforeSlugifying() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Wand Two","source":"DMG","page":210,"type":"WD","rarity":"rare",
                "reqAttune":true,"charges":3,
                "attachedSpells":{"charges":{"2":["melfs-acid-arrow|xge"]}}}""");

        JsonNode grantedSpells = imported.data().get("grantedSpells");
        assertThat(grantedSpells.get(0).get("spellSlug").asString()).isEqualTo("melfs-acid-arrow");
        assertThat(grantedSpells.get(0).get("chargeCost").asInt()).isEqualTo(2);
    }

    @Test
    void capturesALiteralWeaponAndArmorBonusOnANamedItem() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Blade","source":"DMG","page":1,"type":"M","rarity":"rare",
                "reqAttune":true,"weaponCategory":"martial","dmg1":"1d8","dmgType":"S",
                "weapon":true,"bonusWeapon":"+1"}""");

        JsonNode data = imported.data();
        assertThat(data.get("weaponAttackBonus").asInt()).isEqualTo(1);
        assertThat(data.get("weaponDamageBonus").asInt()).isEqualTo(1);
    }

    @Test
    void fallsBackToPlainGearWhenNeitherTypedNorWondrous() {
        CatalogueEntryImport imported = convert("""
                {"name":"Test Bedroll","source":"PHB","page":1,"type":"G","rarity":"none",
                "weight":7,"value":100}""");

        JsonNode data = imported.data();
        assertThat(data.get("itemKind").asString()).isEqualTo("GEAR");
        assertThat(data.get("typeLabel").asString()).isEqualTo("Adventuring Gear");
        assertThat(data.get("costGp").asDouble()).isEqualTo(1);
    }

    private CatalogueEntryImport convert(String rawJson) {
        return converter.convert(objectMapper.readTree(rawJson));
    }
}
