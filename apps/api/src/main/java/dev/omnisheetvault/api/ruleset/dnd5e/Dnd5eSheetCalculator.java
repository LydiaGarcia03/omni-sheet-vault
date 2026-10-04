package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.ActiveEffectInfo;
import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.Background;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Coins;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.ClassLevelInfo;
import dev.omnisheetvault.api.ruleset.CustomAction;
import dev.omnisheetvault.api.ruleset.DeathSaves;
import dev.omnisheetvault.api.ruleset.DefenseEntry;
import dev.omnisheetvault.api.ruleset.Experience;
import dev.omnisheetvault.api.ruleset.Encumbrance;
import dev.omnisheetvault.api.ruleset.Extra;
import dev.omnisheetvault.api.ruleset.AbilityScoreBreakdown;
import dev.omnisheetvault.api.ruleset.CustomSkill;
import dev.omnisheetvault.api.ruleset.ExtraAbilityScore;
import dev.omnisheetvault.api.ruleset.ExtraSkill;
import dev.omnisheetvault.api.ruleset.ExtraStatBlock;
import dev.omnisheetvault.api.ruleset.ExtraStatEntry;
import dev.omnisheetvault.api.ruleset.FeatureAction;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.ItemGrantedSpell;
import dev.omnisheetvault.api.ruleset.Provenance;
import dev.omnisheetvault.api.ruleset.RollModeInfo;
import dev.omnisheetvault.api.ruleset.SheetCalculator;
import dev.omnisheetvault.api.ruleset.SpecialSense;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellUsage;
import dev.omnisheetvault.api.ruleset.SpellAdjustments;
import dev.omnisheetvault.api.ruleset.SpellSlotLevel;
import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;
import dev.omnisheetvault.api.ruleset.StorageSection;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class Dnd5eSheetCalculator implements SheetCalculator {

    private static final Map<String, String> SKILL_ABILITIES = Dnd5eSkills.ABILITIES;
    private static final String UNARMED_STRIKE = "Unarmed Strike";
    private static final String RESISTANCE = "RESISTANCE";
    private static final String IMMUNITY = "IMMUNITY";
    private static final String VULNERABILITY = "VULNERABILITY";
    private static final String CONDITION_IMMUNITY = "CONDITION_IMMUNITY";

    private final ObjectMapper objectMapper;

    public Dnd5eSheetCalculator(ObjectMapper objectMapper) {
        this.objectMapper = Dnd5eSheetJsonMapper.lenient(objectMapper);
    }

    @Override
    public String systemId() {
        return Dnd5eGameSystem.SYSTEM_ID;
    }

    @Override
    public VitalsZone calculateVitals(String sheetJson) {
        Dnd5eSheet stored = objectMapper.readValue(sheetJson, Dnd5eSheet.class);
        Dnd5eSheet sheet = withProficiencyCustomizations(withItemCustomizations(Dnd5eFormulas.withItemAbilityScores(stored)));
        Dnd5eProvenance provenance = new Dnd5eProvenance(stored);

        Map<String, Integer> abilityScores = Map.of(
                "strength", sheet.strength(),
                "dexterity", sheet.dexterity(),
                "constitution", sheet.constitution(),
                "intelligence", sheet.intelligence(),
                "wisdom", sheet.wisdom(),
                "charisma", sheet.charisma());

        Map<String, CalculatedValue> abilityModifiers = Map.of(
                "strength", modifierOf("Strength", sheet.strength()),
                "dexterity", modifierOf("Dexterity", sheet.dexterity()),
                "constitution", modifierOf("Constitution", sheet.constitution()),
                "intelligence", modifierOf("Intelligence", sheet.intelligence()),
                "wisdom", modifierOf("Wisdom", sheet.wisdom()),
                "charisma", modifierOf("Charisma", sheet.charisma()));

        int dexterityModifier = modifier(sheet.dexterity());
        int constitutionModifier = modifier(sheet.constitution());
        CalculatedValue proficiencyBonus = proficiencyBonus(sheet.level());
        Encumbrance encumbrance = encumbrance(sheet);
        Dnd5eModifiers modifiers = new Dnd5eModifiers(sheet, encumbrance.overloaded());
        Map<String, CalculatedValue> skills = skills(sheet, abilityScores, proficiencyBonus.value(), provenance);
        List<DefenseEntry> defenses = defenses(sheet, provenance);

        return new VitalsZone(
                abilityScores,
                abilityModifiers,
                proficiencyBonus,
                armorClass(sheet, dexterityModifier, modifiers, abilityScores),
                initiative(dexterityModifier),
                hitPoints(sheet, constitutionModifier, modifiers, provenance),
                walkingSpeed(sheet, modifiers),
                sheet.level(),
                savingThrows(sheet, abilityScores, proficiencyBonus.value(), modifiers, provenance),
                proficiencyFlags(sheet),
                senses(sheet, skills),
                sheet.armorProficiencies(),
                sheet.weaponProficiencies(),
                sheet.toolProficiencies(),
                sheet.languages(),
                skills,
                skillProficiencyFlags(sheet),
                skillGoverningAbilities(sheet),
                defenseNames(defenses, RESISTANCE),
                defenseNames(defenses, IMMUNITY),
                defenseNames(defenses, VULNERABILITY),
                defenseNames(defenses, CONDITION_IMMUNITY),
                sheet.activeConditions(),
                sheet.exhaustionLevel(),
                sheet.currentHitPoints(),
                sheet.temporaryHitPoints(),
                Dnd5eFormulas.calculatedMaxHitPoints(sheet),
                sheet.heroicInspiration(),
                attacks(sheet, abilityScores, proficiencyBonus.value()),
                featureActions(sheet),
                spellcasting(sheet, abilityScores, proficiencyBonus.value(), modifiers),
                spells(sheet),
                items(sheet),
                coins(sheet),
                featureTraits(sheet),
                background(sheet),
                extras(sheet),
                hitDice(sheet),
                spellSlots(sheet),
                specialSenses(sheet),
                customActions(sheet),
                encumbrance,
                1 + modifiers.extraAttacks(),
                rollModes(modifiers),
                activeEffects(sheet),
                new Provenance(abilityScoreDetails(stored, sheet, provenance), speedDetails(sheet, modifiers, provenance),
                        provenance.proficiencySources(), abilityBreakdowns(stored, sheet, provenance),
                        stored.customizationsOrEmpty()),
                modifiers.rollNotes(),
                deathSaves(sheet),
                experience(sheet),
                sheet.appearanceOrDefault().theme(),
                customSkills(sheet, abilityScores, proficiencyBonus.value()),
                defenses);
    }

    /** The names of one type's defenses, each once. */
    private static List<String> defenseNames(List<DefenseEntry> defenses, String type) {
        return defenses.stream().filter(defense -> defense.type().equals(type)).map(DefenseEntry::name).distinct().toList();
    }

    /** The sheet's own defenses with their build source, then those of active items, then the player's own. */
    private static List<DefenseEntry> defenses(Dnd5eSheet sheet, Dnd5eProvenance provenance) {
        List<DefenseEntry> defenses = new ArrayList<>();
        addDefenses(defenses, sheet, provenance, RESISTANCE, "damageResistances", sheet.damageResistances(),
                Dnd5eItemMechanics::damageResistances);
        addDefenses(defenses, sheet, provenance, IMMUNITY, "damageImmunities", sheet.damageImmunities(),
                Dnd5eItemMechanics::damageImmunities);
        addDefenses(defenses, sheet, provenance, VULNERABILITY, "damageVulnerabilities", sheet.damageVulnerabilities(),
                Dnd5eItemMechanics::damageVulnerabilities);
        addDefenses(defenses, sheet, provenance, CONDITION_IMMUNITY, "conditionImmunities", sheet.conditionImmunities(),
                Dnd5eItemMechanics::conditionImmunities);
        for (Dnd5eCustomDefense custom : sheet.customizationsOrEmpty().defenses()) {
            String type = custom.isCondition() ? CONDITION_IMMUNITY : custom.type().name();
            defenses.add(new DefenseEntry(custom.key(), type, Dnd5eChoiceOptions.capitalize(custom.subtype()), null, true,
                    custom.notes()));
        }
        return List.copyOf(defenses);
    }

    private static void addDefenses(List<DefenseEntry> defenses, Dnd5eSheet sheet, Dnd5eProvenance provenance, String type,
            String field, List<String> own, Function<Dnd5eItemMechanics, List<String>> itemDefenses) {
        own.forEach(name -> defenses.add(new DefenseEntry(null, type, name, provenance.grantSource("DEFENSE", field + ":" + name),
                false, null)));
        sheet.items().stream().filter(Dnd5eItem::active).forEach(item -> itemDefenses.apply(item.mechanics())
                .forEach(name -> defenses.add(new DefenseEntry(null, type, name, item.name(), false, null))));
    }

    /** The build's advancement and the character's experience points against the PHB table. */
    private static Experience experience(Dnd5eSheet sheet) {
        Dnd5ePreferences.Dnd5eAdvancement advancement = sheet.build() == null
                ? Dnd5ePreferences.Dnd5eAdvancement.MILESTONE
                : sheet.build().preferences().advancement();
        int points = sheet.experiencePointsOrDefault();
        int level = sheet.level();
        boolean experienceBuild = advancement == Dnd5ePreferences.Dnd5eAdvancement.XP;
        Integer nextLevelAt = level < Dnd5eExperience.MAX_LEVEL ? Dnd5eExperience.threshold(level + 1) : null;
        return new Experience(advancement.name(), points, level, Dnd5eExperience.levelFor(points),
                Dnd5eExperience.threshold(level), nextLevelAt,
                experienceBuild && Dnd5eExperience.levelUpAvailable(points, level),
                sheet.build() != null && level < Dnd5eExperience.MAX_LEVEL,
                Dnd5eExperience.THRESHOLDS,
                sheet.classLevelsOrEmpty().stream()
                        .map(classLevel -> new ClassLevelInfo(classLevel.className(), classLevel.subclassName(), classLevel.level()))
                        .toList());
    }

    /** At 0 hit points the sheet shows the death saves: dying until three successes (stable) or three failures (dead). */
    private static DeathSaves deathSaves(Dnd5eSheet sheet) {
        if (sheet.currentHitPoints() > 0) {
            return DeathSaves.NOT_DYING;
        }
        Dnd5eDeathSaves saves = sheet.deathSavesOrDefault();
        return new DeathSaves(saves.successes(), saves.failures(), true, saves.stable(), saves.dead());
    }

    private static final List<String> ABILITIES = List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");

    /**
     * Each score's contributions: an item that sets a higher score adds the difference under its own name, the Other
     * Modifier adds its own line, and an Override Score stands alone.
     */
    private static Map<String, CalculatedValue> abilityScoreDetails(Dnd5eSheet stored, Dnd5eSheet effective, Dnd5eProvenance provenance) {
        Map<String, CalculatedValue> details = new HashMap<>();
        for (String ability : ABILITIES) {
            int storedScore = scoreOf(stored, ability);
            int effectiveScore = scoreOf(effective, ability);
            Dnd5eAbilityCustomization customization = stored.customizationsOrEmpty().ability(ability);
            List<Contribution> contributions = new ArrayList<>();
            if (customization.overrideScore() != null) {
                contributions.add(new Contribution("Override Score", customization.overrideScore()));
            } else {
                contributions.addAll(provenance.abilityScore(ability, storedScore));
                int itemScore = Dnd5eFormulas.itemScore(stored, ability, storedScore);
                if (itemScore > storedScore) {
                    String setter = Dnd5eFormulas.itemScoreSetter(stored, ability).map(Dnd5eModifier::source).orElse("Item");
                    contributions.add(new Contribution(setter + " (Set Score " + itemScore + ")", itemScore - storedScore));
                }
                if (customization.otherModifier() != null && customization.otherModifier() != 0) {
                    contributions.add(new Contribution("Other Modifier", customization.otherModifier()));
                }
            }
            details.put(ability, new CalculatedValue(effectiveScore, List.copyOf(contributions)));
        }
        return Map.copyOf(details);
    }

    /** Each score as D&D Beyond's ability pane lays it out (Total, Modifier, Base, Bonus, Set Score, Stacking Bonus). */
    private static Map<String, AbilityScoreBreakdown> abilityBreakdowns(Dnd5eSheet stored, Dnd5eSheet effective, Dnd5eProvenance provenance) {
        Map<String, AbilityScoreBreakdown> breakdowns = new HashMap<>();
        for (String ability : ABILITIES) {
            int storedScore = scoreOf(stored, ability);
            int total = scoreOf(effective, ability);
            List<Contribution> parts = provenance.abilityScore(ability, storedScore);
            int base = parts.isEmpty() ? storedScore : parts.getFirst().amount();
            int itemScore = Dnd5eFormulas.itemScore(stored, ability, storedScore);
            Dnd5eAbilityCustomization customization = stored.customizationsOrEmpty().ability(ability);
            int otherModifier = customization.otherModifier() == null ? 0 : customization.otherModifier();
            breakdowns.put(ability, new AbilityScoreBreakdown(
                    total, Dnd5eFormulas.modifier(total), base, storedScore - base, parts.stream().skip(1).toList(),
                    itemScore > storedScore ? itemScore : 0, otherModifier,
                    customization.otherModifier(), customization.overrideScore()));
        }
        return Map.copyOf(breakdowns);
    }

    private static int scoreOf(Dnd5eSheet sheet, String ability) {
        return switch (ability) {
            case "strength" -> sheet.strength();
            case "dexterity" -> sheet.dexterity();
            case "constitution" -> sheet.constitution();
            case "intelligence" -> sheet.intelligence();
            case "wisdom" -> sheet.wisdom();
            default -> sheet.charisma();
        };
    }

    private static CalculatedValue speedDetails(Dnd5eSheet sheet, Dnd5eModifiers modifiers, Dnd5eProvenance provenance) {
        Integer override = sheet.customizationsOrEmpty().speed("walking").value();
        if (override != null) {
            return new CalculatedValue(override, List.of(new Contribution("Walking Override", override)));
        }
        List<Contribution> contributions = new ArrayList<>(provenance.speed(sheet.speed()));
        contributions.addAll(modifiers.speedChanges(sheet.speed()));
        return new CalculatedValue(modifiers.speed(sheet.speed()), List.copyOf(contributions));
    }

    /** The walking speed: the player's Walking Override when set, else the stored speed with its modifiers. */
    private static int walkingSpeed(Dnd5eSheet sheet, Dnd5eModifiers modifiers) {
        Integer override = sheet.customizationsOrEmpty().speed("walking").value();
        return override != null ? override : modifiers.speed(sheet.speed());
    }

    private static List<ActiveEffectInfo> activeEffects(Dnd5eSheet sheet) {
        return sheet.activeEffectsOrEmpty().stream()
                .map(effect -> new ActiveEffectInfo(effect.key(), effect.name(), effect.castAtLevel(), effect.concentration(),
                        effect.endsOnRests().stream().map(Enum::name).sorted().toList(), effect.durationText(),
                        effect.appliesToCharacter()))
                .toList();
    }

    /** Every d20 roll the sheet forces a mode on, keyed as {@code VitalsZone.rollModes} documents; skills are checks of their ability. */
    private Map<String, RollModeInfo> rollModes(Dnd5eModifiers modifiers) {
        Map<String, RollModeInfo> rollModes = new LinkedHashMap<>();
        putIfForced(rollModes, "ATTACK", modifiers.rollMode(List.of(Dnd5eModifierTarget.ATTACK_ROLLS)));
        putIfForced(rollModes, "INITIATIVE", modifiers.rollMode(List.of(Dnd5eModifierTarget.ABILITY_CHECKS)));
        for (String ability : List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma")) {
            putIfForced(rollModes, "ABILITY_CHECK:" + ability, modifiers.rollMode(checkTargets(ability)));
            putIfForced(rollModes, "SAVING_THROW:" + ability, modifiers.rollMode(saveTargets(ability)));
        }
        SKILL_ABILITIES.forEach((skill, ability) -> putIfForced(rollModes, "SKILL_CHECK:" + skill, modifiers.rollMode(skillTargets(skill, ability))));
        return rollModes;
    }

    private static List<Dnd5eModifierTarget> checkTargets(String ability) {
        return "strength".equals(ability)
                ? List.of(Dnd5eModifierTarget.ABILITY_CHECKS, Dnd5eModifierTarget.STRENGTH_ABILITY_CHECKS)
                : List.of(Dnd5eModifierTarget.ABILITY_CHECKS);
    }

    /** A skill check is a check of its ability, plus the skill's own target when the vocabulary has one (Stealth). */
    private static List<Dnd5eModifierTarget> skillTargets(String skill, String ability) {
        List<Dnd5eModifierTarget> targets = new ArrayList<>(checkTargets(ability));
        Dnd5eModifierTarget.checksOf(skill).ifPresent(targets::add);
        return targets;
    }

    private static List<Dnd5eModifierTarget> saveTargets(String ability) {
        return switch (ability) {
            case "strength" -> List.of(Dnd5eModifierTarget.SAVING_THROWS, Dnd5eModifierTarget.STRENGTH_SAVING_THROWS);
            case "dexterity" -> List.of(Dnd5eModifierTarget.SAVING_THROWS, Dnd5eModifierTarget.DEXTERITY_SAVING_THROWS);
            default -> List.of(Dnd5eModifierTarget.SAVING_THROWS);
        };
    }

    private static void putIfForced(Map<String, RollModeInfo> rollModes, String key, RollModeInfo info) {
        if (!info.advantageSources().isEmpty() || !info.disadvantageSources().isEmpty()) {
            rollModes.put(key, info);
        }
    }

    private HitDice hitDice(Dnd5eSheet sheet) {
        return Dnd5eHitDicePools.of(sheet);
    }

    private List<SpellSlotLevel> spellSlots(Dnd5eSheet sheet) {
        return sheet.spellSlots().stream()
                .map(slot -> new SpellSlotLevel(slot.level(), slot.maxSlots(), slot.usedSlots(), slot.pact(), slot.className()))
                .toList();
    }

    private List<Extra> extras(Dnd5eSheet sheet) {
        return sheet.extras().stream()
                .map(extra -> new Extra(
                        extra.key(), extra.name(), extra.category().name(), extra.armorClass(), extra.maxHitPoints(),
                        extra.currentHitPoints(), extra.temporaryHitPoints(), extra.speed(),
                        extraStatBlock(extra.statBlock())))
                .toList();
    }

    private ExtraStatBlock extraStatBlock(Dnd5eExtraStatBlock statBlock) {
        List<ExtraAbilityScore> abilityScores = statBlock.abilityScores().stream()
                .map(abilityScore -> new ExtraAbilityScore(
                        abilityScore.abilityKey(), abilityScore.score(), modifier(abilityScore.score()),
                        abilityScore.save()))
                .toList();
        int initiativeBonus = abilityScores.stream()
                .filter(abilityScore -> abilityScore.abilityKey().equals("dexterity"))
                .findFirst()
                .map(ExtraAbilityScore::modifier)
                .orElse(0);
        return new ExtraStatBlock(
                statBlock.size(), statBlock.creatureType(), statBlock.alignment(), initiativeBonus,
                statBlock.hitDiceLabel(), statBlock.additionalSpeeds(), abilityScores,
                statBlock.skills().stream().map(skill -> new ExtraSkill(skill.name(), skill.bonus())).toList(),
                statBlock.senses(), statBlock.languages(), statBlock.challengeRating(),
                statBlock.traits().stream().map(trait -> new ExtraStatEntry(trait.name(), trait.description())).toList(),
                statBlock.actions().stream().map(action -> new ExtraStatEntry(action.name(), action.description())).toList());
    }

    private Background background(Dnd5eSheet sheet) {
        Dnd5eBackground background = sheet.background();
        return new Background(
                background.name(), background.featureName(), background.featureDescription(),
                background.alignment(), background.personalityTraits(), background.ideals(), background.bonds(),
                background.flaws(), background.appearance(), background.organizations(), background.allies(),
                background.enemies(), background.backstory(), background.other(), background.gender(),
                background.eyes(), background.size(), background.height(), background.faith(), background.hair(),
                background.skin(), background.age(), background.weight(), background.lifestyle());
    }

    private List<FeatureTrait> featureTraits(Dnd5eSheet sheet) {
        return sheet.featureTraits().stream()
                .map(feature -> new FeatureTrait(
                        feature.key(), feature.name(), feature.category().name(), feature.source(),
                        feature.summary(), feature.description(), feature.maxUses(), feature.usedCount(),
                        rechargeTriggerName(feature.rechargeTrigger()),
                        feature.choices() == null ? List.of() : feature.choices()))
                .toList();
    }

    /** Every item with its customized name, notes, cost and weight, so inventory, encumbrance and attacks agree. */
    private static Dnd5eSheet withItemCustomizations(Dnd5eSheet sheet) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        if (customizations.items().isEmpty()) {
            return sheet;
        }
        return sheet.withItems(sheet.items().stream().map(item -> item.customizedAs(customizations.item(item.key()))).toList());
    }

    /** The hand-added proficiencies joined to the build's, so the lists and weapon attacks both count them. */
    private static Dnd5eSheet withProficiencyCustomizations(Dnd5eSheet sheet) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        if (customizations.proficiencies().isEmpty()) {
            return sheet;
        }
        return sheet.withProficiencies(
                joined(sheet.armorProficiencies(), customizations.proficiencyNames(Dnd5eCustomProficiency.Type.ARMOR)),
                joined(sheet.weaponProficiencies(), customizations.proficiencyNames(Dnd5eCustomProficiency.Type.WEAPON)),
                joined(sheet.toolProficiencies(), customizations.proficiencyNames(Dnd5eCustomProficiency.Type.TOOL)),
                joined(sheet.languages(), customizations.proficiencyNames(Dnd5eCustomProficiency.Type.LANGUAGE)));
    }

    /** {@code granted} followed by each added name it doesn't already hold (ignoring case). */
    private static List<String> joined(List<String> granted, List<String> added) {
        List<String> result = new ArrayList<>(granted);
        for (String name : added) {
            if (result.stream().noneMatch(existing -> existing.equalsIgnoreCase(name))) {
                result.add(name);
            }
        }
        return result;
    }

    private List<Item> items(Dnd5eSheet sheet) {
        return sheet.items().stream().map(this::toItem).toList();
    }

    private Item toItem(Dnd5eItem item) {
        return new Item(
                item.key(), item.name(), item.quantity(), item.cost(), item.notes(), item.equipped(),
                item.attuned(), item.requiresAttunement(), item.catalogueSlug(), item.itemKind().name(),
                item.typeLabel(), item.rarity(), item.attunementRequirement(), item.weightLb(), item.costGp(),
                enumName(item.weaponCategory()), enumName(item.rangeCategory()), item.damageDiceCount(),
                item.damageDiceSides(), item.damageType(), item.versatileDamageDiceCount(),
                item.versatileDamageDiceSides(), propertyLabels(item), item.finesse(), item.normalRange(),
                item.longRange(), enumName(item.armorCategory()), item.baseArmorClass(),
                item.stealthDisadvantage(), item.strengthRequirement(), item.weaponAttackBonus(),
                item.weaponDamageBonus(), item.armorClassBonus(), item.charges(), item.rechargeTrigger(),
                item.rechargeFormula(), toItemGrantedSpells(item.grantedSpells()), item.chargesUsed(),
                item.storageLocation().name(), item.source());
    }

    /** The item's properties as the PHB names them, with the versatile die: "Versatile (1d8)". */
    private static List<String> propertyLabels(Dnd5eItem item) {
        if (item.versatileDamageDiceCount() == null || item.versatileDamageDiceSides() == null) {
            return item.properties();
        }
        String versatile = "Versatile (" + item.versatileDamageDiceCount() + "d" + item.versatileDamageDiceSides() + ")";
        return item.properties().stream().map(property -> property.equalsIgnoreCase("Versatile") ? versatile : property).toList();
    }

    private List<ItemGrantedSpell> toItemGrantedSpells(List<Dnd5eGrantedSpell> grantedSpells) {
        return grantedSpells.stream()
                .map(granted -> new ItemGrantedSpell(
                        granted.spellSlug(), granted.spellName(), granted.chargeCost(), granted.fixedSaveDc(),
                        granted.level(), granted.school(), granted.castingTime(), granted.range(),
                        granted.concentration(), granted.ritual(), granted.attackRoll(), granted.damageDiceCount(),
                        granted.damageDiceSides(), granted.damageType(), granted.notes(), granted.effectSummary(),
                        granted.description(), granted.saveAbility(), granted.components(),
                        granted.materialComponent(), granted.duration()))
                .toList();
    }

    private Coins coins(Dnd5eSheet sheet) {
        return new Coins(
                sheet.copperPieces(), sheet.silverPieces(), sheet.electrumPieces(), sheet.goldPieces(),
                sheet.platinumPieces());
    }

    private List<SpellcastingClassInfo> spellcasting(
            Dnd5eSheet sheet, Map<String, Integer> abilityScores, int proficiencyBonusValue, Dnd5eModifiers modifiers) {
        List<Contribution> attackBonuses = modifiers.bonuses(Dnd5eModifierTarget.SPELL_ATTACKS, abilityScores);
        List<Contribution> saveDcBonuses = modifiers.bonuses(Dnd5eModifierTarget.SPELL_SAVE_DC, abilityScores);
        List<SpellcastingClassInfo> result = new ArrayList<>();
        for (Dnd5eSpellcastingClass spellcastingClass : sheet.spellcastingClasses()) {
            String abilityLabel = capitalize(spellcastingClass.abilityKey());
            int abilityModifier = modifier(abilityScores.get(spellcastingClass.abilityKey()));

            CalculatedValue spellcastingModifier = new CalculatedValue(abilityModifier, List.of(
                    new Contribution(abilityLabel + " modifier", abilityModifier)));
            CalculatedValue spellAttackBonus = withBonuses(List.of(
                    new Contribution(abilityLabel + " modifier", abilityModifier),
                    new Contribution("Proficiency bonus", proficiencyBonusValue)), attackBonuses);
            CalculatedValue spellSaveDc = withBonuses(List.of(
                    new Contribution("Base", 8),
                    new Contribution(abilityLabel + " modifier", abilityModifier),
                    new Contribution("Proficiency bonus", proficiencyBonusValue)), saveDcBonuses);

            result.add(new SpellcastingClassInfo(
                    spellcastingClass.className(), spellcastingClass.abilityKey(), spellcastingModifier,
                    spellAttackBonus, spellSaveDc, spellcastingClass.castingType().name(),
                    spellcastingClass.cantripsKnownMax(), spellcastingClass.spellsKnownMax(),
                    spellcastingClass.spellsPreparedMax(), isClassCaster(sheet, spellcastingClass.className())));
        }
        return List.copyOf(result);
    }

    private static CalculatedValue withBonuses(List<Contribution> base, List<Contribution> bonuses) {
        List<Contribution> contributions = new ArrayList<>(base);
        contributions.addAll(bonuses);
        return new CalculatedValue(contributions.stream().mapToInt(Contribution::amount).sum(), List.copyOf(contributions));
    }

    /** A sheet without class levels predates builds, so every entry on it is a class. */
    private static boolean isClassCaster(Dnd5eSheet sheet, String casterName) {
        List<Dnd5eClassLevel> classLevels = sheet.classLevelsOrEmpty();
        return classLevels.isEmpty() || classLevels.stream().anyMatch(classLevel -> classLevel.className().equals(casterName));
    }

    /**
     * An item-granted spell (slice 7) only appears while its own item is
     * currently active — equipped, and attuned too if the item requires it —
     * same "equip and attune both needed" rule an item's own stat contributions
     * already follow (see {@code Dnd5eItem}'s doc comment); confirmed live
     * against D&D Beyond's own rules-engine (features/inventory-equipment-
     * mechanics.md's research). A normal class-known spell has no owning item
     * and is always included.
     */
    private List<Spell> spells(Dnd5eSheet sheet) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        return sheet.spells().stream()
                .filter(spell -> isGrantedSpellCurrentlyActive(sheet, spell))
                .map(spell -> toSpell(spell, customizations.spell(spell.key())))
                .toList();
    }

    private static Spell toSpell(Dnd5eSpell spell, Dnd5eSpellCustomization customization) {
        return new Spell(
                spell.key(), customization.name() != null ? customization.name() : spell.name(), spell.className(),
                spell.level(), spell.school(), spell.castingTime(), spell.range(), spell.concentration(), spell.ritual(),
                spell.attackRoll(), spell.damageDiceCount(), spell.damageDiceSides(), spell.damageType(),
                customization.notes() != null ? customization.notes() : spell.notes(), spell.effectSummary(),
                spell.prepared(), spell.alwaysPrepared(), spell.description(), spell.saveAbility(), spell.components(),
                spell.materialComponent(), spell.duration(), spell.higherLevelsDescription(),
                spell.higherLevelsDamageDiceCount(), spell.higherLevelsDamageDiceSides(), spell.grantedByItemKey(),
                spell.chargeCost(), spell.fixedSaveDc(), adjustments(customization), usage(spell.usage()));
    }

    private static SpellUsage usage(Dnd5eSpellUsage usage) {
        if (usage == null) {
            return null;
        }
        return new SpellUsage(usage.mode().name(), usage.maxUses(), usage.usedUses(),
                usage.recharge() == null ? null : usage.recharge().name(), usage.castLevel(), usage.selfOnly());
    }

    private static SpellAdjustments adjustments(Dnd5eSpellCustomization customization) {
        return new SpellAdjustments(customization.toHitOverride(), orZero(customization.toHitBonus()),
                orZero(customization.damageBonus()), customization.dcOverride(), orZero(customization.dcBonus()),
                customization.displayAsAttack());
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isGrantedSpellCurrentlyActive(Dnd5eSheet sheet, Dnd5eSpell spell) {
        if (spell.grantedByItemKey() == null) {
            return true;
        }
        Dnd5eItem owningItem = sheet.items().stream()
                .filter(item -> item.key().equals(spell.grantedByItemKey()))
                .findFirst()
                .orElse(null);
        if (owningItem == null || !owningItem.equipped()) {
            return false;
        }
        return !owningItem.requiresAttunement() || owningItem.attuned();
    }

    /**
     * {@code LinkedHashMap}, not the plain {@code HashMap} every other map in
     * this class uses — those are keyed collections with no meaningful order,
     * but the frontend renders this one as an ordered table, iterating
     * {@code Object.entries(sheet.attacks)} directly. A plain {@code HashMap}
     * silently reordered rows by hash bucket instead of by
     * {@code Dnd5eAttack} authoring order (confirmed: adding a third attack
     * moved it to the front, not the back, of the rendered table). Ordering
     * itself is still just "whatever order {@code sheet.attacks()} lists
     * them in" — authors should list a universal, always-available attack
     * like Unarmed Strike last, per the owner's own convention.
     */
    private Map<String, AttackRow> attacks(Dnd5eSheet sheet, Map<String, Integer> abilityScores, int proficiencyBonusValue) {
        Map<String, AttackRow> result = new LinkedHashMap<>();
        for (Dnd5eAttack attack : sheet.attacks()) {
            int abilityModifier = modifier(abilityScores.get(attack.abilityModifierKey()));
            CalculatedValue toHit = new CalculatedValue(abilityModifier + proficiencyBonusValue, List.of(
                    new Contribution(capitalize(attack.abilityModifierKey()) + " modifier", abilityModifier),
                    new Contribution("Proficiency bonus", proficiencyBonusValue)));
            result.put(attack.key(), new AttackRow(
                    attack.name(), attack.range(), toHit, attack.damageDiceCount(), attack.damageDiceSides(),
                    abilityModifier, attack.damageType(), attack.category(), attack.notes(), Dnd5eActionType.ACTION.name()));
        }
        // systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 3: an equipped weapon-shaped
        // item folds in here too, same table, same rule the custom-action Weapon
        // template already follows for proficiency (assumed for a hand-authored
        // Dnd5eAttack, but checked here since a real item can be one the character
        // was never trained with).
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        for (Dnd5eItem item : sheet.items()) {
            Dnd5eItemCustomization customization = customizations.item(item.key());
            if (item.itemKind() == Dnd5eItemKind.WEAPON && (item.equipped() || customization.displayAsAttack())) {
                result.put(item.key(), itemAttackRow(item, customization, sheet.weaponProficiencies(), abilityScores, proficiencyBonusValue));
            }
        }
        // A combat spell (attack-roll or save-based damage) is NOT folded in here —
        // reverted, systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 5 originally
        // did this, but the frontend already has a complete, independently-built
        // mechanism for exactly this (`isCombatSpell`/`SpellAttackRow.tsx`, live-
        // confirmed against D&D Beyond back on 2026-09-03/04/10/15, before this
        // initiative existed) that reads `sheet.spells`/`sheet.spellcasting` directly
        // and handles attack-roll, save-based AND healing display shapes — richer
        // than this map's own `AttackRow` shape, which has no DC slot at all. Adding
        // it here duplicated Fire Bolt/Chill Touch in the Actions tab; see
        // changelog.md's own correction entry.
        // Only a to-hit-capable custom action (stat set) can fold in here — a
        // save-based one has nowhere to go in this shape, which has no DC slot; see
        // Dnd5eCustomAction's own doc comment for why that stays a deliberate limit.
        for (Dnd5eCustomAction action : sheet.customActions()) {
            if (action.displayAsAttack() && action.stat() != null && action.activationType() != null) {
                result.put(action.key(), customActionAttackRow(action, abilityScores, proficiencyBonusValue));
            }
        }
        if (sheet.attacks().stream().noneMatch(attack -> attack.name().equalsIgnoreCase(UNARMED_STRIKE))) {
            result.put("unarmed-strike", unarmedStrike(abilityScores, proficiencyBonusValue));
        }
        // Not Map.copyOf: the JDK's immutable Map implementation has an unspecified
        // (in practice randomized-per-JVM-run) iteration order, which silently threw
        // away the LinkedHashMap ordering this method exists to guarantee — the exact
        // same bug class as the HashMap this method's own doc comment already warns
        // about, just reintroduced one line later. Collections.unmodifiableMap wraps
        // without rebuilding, so it preserves the LinkedHashMap's real order.
        return Collections.unmodifiableMap(result);
    }


    /** Every creature's unarmed strike (PHB 195): proficient, Strength to hit, 1 + Strength modifier bludgeoning, no dice. */
    private AttackRow unarmedStrike(Map<String, Integer> abilityScores, int proficiencyBonusValue) {
        int strengthModifier = modifier(abilityScores.get("strength"));
        CalculatedValue toHit = new CalculatedValue(strengthModifier + proficiencyBonusValue, List.of(
                new Contribution("Strength modifier", strengthModifier),
                new Contribution("Proficiency bonus", proficiencyBonusValue)));
        return new AttackRow(UNARMED_STRIKE, "5 ft.", toHit, 0, 1, 1 + strengthModifier, "bludgeoning", "Melee Attack", "",
                Dnd5eActionType.ACTION.name());
    }

    /**
     * To-hit is the stat's modifier plus the proficiency bonus only when
     * {@code proficient} is set — confirmed live, unlike a catalog {@link Dnd5eAttack},
     * where proficiency is always assumed. Damage is the stat's modifier automatically
     * plus {@code fixedValue} as an extra flat bonus, same as confirmed live.
     */
    private AttackRow customActionAttackRow(Dnd5eCustomAction action, Map<String, Integer> abilityScores, int proficiencyBonusValue) {
        int abilityModifier = modifier(abilityScores.get(action.stat()));
        List<Contribution> hitContributions = new ArrayList<>();
        hitContributions.add(new Contribution(capitalize(action.stat()) + " modifier", abilityModifier));
        int hitTotal = abilityModifier;
        if (action.proficient()) {
            hitContributions.add(new Contribution("Proficiency bonus", proficiencyBonusValue));
            hitTotal += proficiencyBonusValue;
        }
        CalculatedValue toHit = new CalculatedValue(hitTotal, hitContributions);
        int fixedValue = action.fixedValue() != null ? action.fixedValue() : 0;
        int diceCount = action.diceCount() != null ? action.diceCount() : 0;
        int dieType = action.dieType() != null ? action.dieType() : 1;
        String range = action.rangeFeet() != null ? action.rangeFeet() + " ft." : "";
        String category = action.rangeCategory() != null ? capitalize(action.rangeCategory().name().toLowerCase()) + " Attack" : "";
        return new AttackRow(
                action.name(), range, toHit, diceCount, dieType, abilityModifier + fixedValue,
                action.damageType() != null ? action.damageType() : "", category, "", action.activationType().toActionType().name());
    }

    /**
     * Proficiency is checked (unlike a catalog {@link Dnd5eAttack}, always assumed —
     * see its own doc comment) because a real item can be one the character was
     * never trained with, same reasoning as {@link #customActionAttackRow}'s own
     * {@code proficient} flag. {@code weaponAttackBonus}/{@code weaponDamageBonus}
     * are a rare named magic item's own literal {@code +N} — see {@code Dnd5eItem}'s
     * own doc comment.
     */
    private AttackRow itemAttackRow(Dnd5eItem item, Dnd5eItemCustomization customization, List<String> weaponProficiencies,
            Map<String, Integer> abilityScores, int proficiencyBonusValue) {
        String abilityKey = weaponAbilityKey(item, abilityScores);
        int abilityModifier = modifier(abilityScores.get(abilityKey));

        List<Contribution> hitContributions = new ArrayList<>();
        hitContributions.add(new Contribution(capitalize(abilityKey) + " modifier", abilityModifier));
        int hitTotal = abilityModifier;
        if (isProficientWithWeapon(item, weaponProficiencies)) {
            hitContributions.add(new Contribution("Proficiency bonus", proficiencyBonusValue));
            hitTotal += proficiencyBonusValue;
        }
        if (item.weaponAttackBonus() != null) {
            hitContributions.add(new Contribution(item.name() + " bonus", item.weaponAttackBonus()));
            hitTotal += item.weaponAttackBonus();
        }
        if (customization.toHitBonus() != null) {
            hitContributions.add(new Contribution("To Hit Bonus", customization.toHitBonus()));
            hitTotal += customization.toHitBonus();
        }
        CalculatedValue toHit = customization.toHitOverride() != null
                ? new CalculatedValue(customization.toHitOverride(), List.of(new Contribution("To Hit Override", customization.toHitOverride())))
                : new CalculatedValue(hitTotal, hitContributions);

        int damageModifier = abilityModifier + (item.weaponDamageBonus() != null ? item.weaponDamageBonus() : 0)
                + orZero(customization.damageBonus());
        int damageDiceCount = item.damageDiceCount() != null ? item.damageDiceCount() : 0;
        int damageDiceSides = item.damageDiceSides() != null ? item.damageDiceSides() : 1;

        return new AttackRow(
                item.name(), weaponRange(item), toHit, damageDiceCount, damageDiceSides, damageModifier,
                item.damageType() != null ? item.damageType() : "",
                item.typeLabel() != null ? item.typeLabel() : "", customization.notes() != null ? customization.notes() : "",
                Dnd5eActionType.ACTION.name(), item.versatileDamageDiceCount(), item.versatileDamageDiceSides());
    }

    /**
     * Ranged always uses dexterity (PHB). Melee uses strength, unless the weapon has
     * the finesse property, in which case the character's own better modifier is
     * used — this app has no per-attack "which ability did you pick" choice for a
     * finesse weapon, so it assumes the optimal one, same simplification digital
     * tools commonly make.
     */
    private String weaponAbilityKey(Dnd5eItem item, Map<String, Integer> abilityScores) {
        if (item.rangeCategory() == Dnd5eRangeCategory.RANGED) {
            return "dexterity";
        }
        if (item.finesse() && modifier(abilityScores.get("dexterity")) > modifier(abilityScores.get("strength"))) {
            return "dexterity";
        }
        return "strength";
    }

    /** A broad category match ("Simple"/"Martial", this app's own free-text convention) or an exact weapon name match — both are real PHB ways to gain weapon proficiency. */
    /** By category, or by the weapon itself: its name, or its catalogue slug when the player renamed it. */
    private boolean isProficientWithWeapon(Dnd5eItem item, List<String> weaponProficiencies) {
        String categoryName = item.weaponCategory() != null ? item.weaponCategory().name() : null;
        String catalogueName = item.catalogueSlug() != null ? item.catalogueSlug().replace('-', ' ') : null;
        return weaponProficiencies.stream().anyMatch(proficiency -> proficiency.equalsIgnoreCase(categoryName)
                || proficiency.equalsIgnoreCase(item.name()) || proficiency.equalsIgnoreCase(catalogueName));
    }

    private String weaponRange(Dnd5eItem item) {
        if (item.rangeCategory() == Dnd5eRangeCategory.RANGED) {
            if (item.normalRange() != null && item.longRange() != null) {
                return item.normalRange() + "/" + item.longRange() + " ft.";
            }
            return item.normalRange() != null ? item.normalRange() + " ft." : "";
        }
        boolean reach = item.properties().stream().anyMatch(property -> property.equalsIgnoreCase("Reach"));
        return reach ? "10 ft. Reach" : "5 ft. Reach";
    }

    private List<CustomAction> customActions(Dnd5eSheet sheet) {
        return sheet.customActions().stream()
                .map(action -> new CustomAction(
                        action.key(), action.template().name(), action.name(), action.snippet(), action.description(),
                        enumName(action.rangeCategory()), action.rangeFeet(), action.stat(), action.diceCount(),
                        action.dieType(), action.fixedValue(), action.damageType(), action.saveType(),
                        action.fixedSaveDc(), enumName(action.spellRangeType()), enumName(action.aoeType()),
                        action.aoeSize(), enumName(action.activationType()), action.activationTime(),
                        action.affectedByMartialArts(), action.proficient(), action.displayAsAttack(),
                        enumName(action.weaponAttackType()), action.longRange(), action.dualWield(), action.silvered()))
                .toList();
    }

    private String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    /** A trait-linked action shows its trait's counter, so both tabs agree. */
    private List<FeatureAction> featureActions(Dnd5eSheet sheet) {
        Map<String, Dnd5eFeatureTrait> traitsByKey = new HashMap<>();
        sheet.featureTraits().forEach(trait -> traitsByKey.put(trait.key(), trait));
        return sheet.featureActions().stream()
                .map(feature -> {
                    Dnd5eFeatureTrait trait = feature.traitKey() == null ? null : traitsByKey.get(feature.traitKey());
                    return trait == null
                            ? new FeatureAction(feature.key(), feature.name(), feature.actionType().name(), feature.description(),
                                    feature.maxUses(), feature.usedCount(), rechargeTriggerName(feature.rechargeTrigger()))
                            : new FeatureAction(feature.key(), feature.name(), feature.actionType().name(), feature.description(),
                                    trait.maxUses(), trait.usedCount(), rechargeTriggerName(trait.rechargeTrigger()), trait.name(),
                                    trait.key());
                })
                .toList();
    }

    private String rechargeTriggerName(Dnd5eRechargeTrigger trigger) {
        return trigger != null ? trigger.name() : null;
    }

    private CalculatedValue modifierOf(String abilityName, int score) {
        int value = modifier(score);
        return new CalculatedValue(value, List.of(new Contribution(abilityName + " score " + score, value)));
    }

    private int modifier(int score) {
        return Dnd5eFormulas.modifier(score);
    }

    private CalculatedValue proficiencyBonus(int level) {
        int bonus = 2 + (level - 1) / 4;
        return new CalculatedValue(bonus, List.of(new Contribution("Level " + level, bonus)));
    }

    /**
     * systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 4: an equipped armour item's
     * dexterity cap (light: none, medium: +2, heavy: none allowed — a standard PHB
     * rule applied here, not stored on the item, see {@code Dnd5eArmorCategory}'s own
     * doc comment) and its base AC replace the flat unarmored formula; an equipped
     * shield adds its own flat AC on top, independent of armour or its absence. The
     * dexterity contribution is always shown, even at {@code 0} for heavy armour —
     * same "show every real factor" transparency the unarmored formula already gave.
     * Multiple equipped body-armour items (a player error nothing currently prevents)
     * take the first one found, not a documented cap; unarmoured-defense class
     * features (barbarian/monk) are not modelled, same pre-existing simplification
     * as before this slice.
     */
    /**
     * D&D Beyond's AC Customize over the calculated armor class: Override AC replaces it; Override Base Armor + DEX
     * replaces its first two lines (the base and the Dexterity modifier); the magic and misc bonuses add.
     */
    private CalculatedValue armorClass(Dnd5eSheet sheet, int dexterityModifier, Dnd5eModifiers modifiers, Map<String, Integer> abilityScores) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        Integer override = customizations.armorClassField("override").value();
        if (override != null) {
            return new CalculatedValue(override, List.of(new Contribution("Override AC", override)));
        }
        CalculatedValue calculated = calculatedArmorClass(sheet, dexterityModifier, modifiers, abilityScores);
        List<Contribution> contributions = new ArrayList<>(calculated.contributions());
        Integer baseArmorDex = customizations.armorClassField("baseArmorDex").value();
        if (baseArmorDex != null) {
            contributions.subList(0, Math.min(2, contributions.size())).clear();
            contributions.addFirst(new Contribution("Override Base Armor + DEX", baseArmorDex));
        }
        addArmorClassBonus(contributions, "Additional Magic Bonus", customizations.armorClassField("magicBonus").value());
        addArmorClassBonus(contributions, "Additional Misc Bonus", customizations.armorClassField("miscBonus").value());
        return new CalculatedValue(contributions.stream().mapToInt(Contribution::amount).sum(), List.copyOf(contributions));
    }

    private static void addArmorClassBonus(List<Contribution> contributions, String label, Integer bonus) {
        if (bonus != null && bonus != 0) {
            contributions.add(new Contribution(label, bonus));
        }
    }

    private CalculatedValue calculatedArmorClass(
            Dnd5eSheet sheet, int dexterityModifier, Dnd5eModifiers modifiers, Map<String, Integer> abilityScores) {
        List<Dnd5eItem> equippedItems = sheet.items().stream().filter(Dnd5eItem::equipped).toList();
        Dnd5eItem armor = equippedItems.stream()
                .filter(item -> item.itemKind() == Dnd5eItemKind.ARMOR)
                .findFirst()
                .orElse(null);

        List<Contribution> contributions = new ArrayList<>();
        int total;
        if (armor == null) {
            Dnd5eModifier unarmoredBase = modifiers.highestBase(Dnd5eModifierTarget.UNARMORED_ARMOR_CLASS, abilityScores)
                    .filter(base -> Dnd5eModifiers.valueOf(base, abilityScores) > 10)
                    .orElse(null);
            int base = unarmoredBase == null ? 10 : Dnd5eModifiers.valueOf(unarmoredBase, abilityScores);
            total = base + dexterityModifier;
            contributions.add(new Contribution(unarmoredBase == null ? "Base (unarmored)" : "Base (" + unarmoredBase.source() + ")", base));
            contributions.add(new Contribution("Dexterity modifier", dexterityModifier));
        } else {
            int baseArmorClass = armor.baseArmorClass() != null ? armor.baseArmorClass() : 10;
            int cappedDexterityModifier = cappedDexterityModifier(armor.armorCategory(), dexterityModifier);
            total = baseArmorClass + cappedDexterityModifier;
            contributions.add(new Contribution("Base (" + armor.name() + ")", baseArmorClass));
            contributions.add(new Contribution("Dexterity modifier", cappedDexterityModifier));
            if (armor.armorClassBonus() != null) {
                contributions.add(new Contribution(armor.name() + " bonus", armor.armorClassBonus()));
                total += armor.armorClassBonus();
            }
            for (Contribution armored : modifiers.bonuses(Dnd5eModifierTarget.ARMORED_ARMOR_CLASS, abilityScores)) {
                contributions.add(armored);
                total += armored.amount();
            }
        }
        for (Contribution bonus : modifiers.bonuses(Dnd5eModifierTarget.ARMOR_CLASS, abilityScores)) {
            contributions.add(bonus);
            total += bonus.amount();
        }

        for (Dnd5eItem shield : equippedItems) {
            if (shield.itemKind() != Dnd5eItemKind.SHIELD) {
                continue;
            }
            int shieldBonus = shield.baseArmorClass() != null ? shield.baseArmorClass() : 2;
            contributions.add(new Contribution(shield.name(), shieldBonus));
            total += shieldBonus;
            if (shield.armorClassBonus() != null) {
                contributions.add(new Contribution(shield.name() + " bonus", shield.armorClassBonus()));
                total += shield.armorClassBonus();
            }
        }

        return new CalculatedValue(total, contributions);
    }

    private static final double POUNDS_TO_KILOGRAMS = 0.453592;
    private static final double CARRYING_CAPACITY_LB_PER_STRENGTH_POINT = 15;
    private static final double BACKPACK_CAPACITY_LB = 30;
    private static final double BAG_OF_HOLDING_CAPACITY_LB = 500;
    private static final double COINS_PER_POUND = 50;
    private static final String BAG_OF_HOLDING_NAME = "Bag of Holding";

    /**
     * Weight/encumbrance initiative: {@code carriedWeightKg} sums Equipment, Backpack
     * and coins (50 coins = 1 lb, PHB) only — a Bag of Holding's own contents and
     * Other Possessions are never physically carried, confirmed live against D&D
     * Beyond (systems/dnd-5e/features/inventory-equipment-mechanics.md's research). A Bag of Holding
     * section only appears when the character owns one, equipped — matched by name,
     * the same specific-item special case this app already makes for the Wand of
     * Fireballs mechanic, not a generic "weightless container" flag (5etools' own
     * item data has no such structured field to key off yet). Gated on
     * {@code equipped}, not {@code attuned}: the real catalogue entry (verified live)
     * has {@code requiresAttunement: false}, matching the actual PHB item — attuned
     * would never be settable for it at all.
     */
    private Encumbrance encumbrance(Dnd5eSheet sheet) {
        boolean bagOfHoldingPresent = sheet.items().stream()
                .anyMatch(item -> item.equipped() && BAG_OF_HOLDING_NAME.equalsIgnoreCase(item.name()));

        double equipmentWeightLb = weightLbInLocation(sheet, Dnd5eStorageLocation.EQUIPMENT);
        double backpackWeightLb = weightLbInLocation(sheet, Dnd5eStorageLocation.BACKPACK);
        double coinsWeightLb = totalCoins(sheet) / COINS_PER_POUND;
        double carriedWeightLb = equipmentWeightLb + backpackWeightLb + coinsWeightLb;
        double capacityLb = sheet.strength() * CARRYING_CAPACITY_LB_PER_STRENGTH_POINT;

        List<StorageSection> sections = new ArrayList<>();
        sections.add(storageSection(sheet, Dnd5eStorageLocation.EQUIPMENT, null));
        sections.add(storageSection(sheet, Dnd5eStorageLocation.BACKPACK, BACKPACK_CAPACITY_LB));
        if (bagOfHoldingPresent) {
            sections.add(storageSection(sheet, Dnd5eStorageLocation.BAG_OF_HOLDING, BAG_OF_HOLDING_CAPACITY_LB));
        }
        sections.add(storageSection(sheet, Dnd5eStorageLocation.OTHER_POSSESSIONS, null));

        boolean overloaded = sheet.trackEncumbrance() && carriedWeightLb > capacityLb;
        return new Encumbrance(
                sheet.trackEncumbrance(), toKg(carriedWeightLb), toKg(capacityLb), overloaded, List.copyOf(sections));
    }

    private StorageSection storageSection(Dnd5eSheet sheet, Dnd5eStorageLocation location, Double capacityLb) {
        List<Dnd5eItem> itemsHere = sheet.items().stream().filter(item -> item.storageLocation() == location).toList();
        double weightLb = itemsHere.stream()
                .mapToDouble(item -> (item.weightLb() != null ? item.weightLb() : 0) * item.quantity())
                .sum();
        return new StorageSection(location.name(), itemsHere.size(), toKg(weightLb), capacityLb != null ? toKg(capacityLb) : null);
    }

    private double weightLbInLocation(Dnd5eSheet sheet, Dnd5eStorageLocation location) {
        return sheet.items().stream()
                .filter(item -> item.storageLocation() == location)
                .mapToDouble(item -> (item.weightLb() != null ? item.weightLb() : 0) * item.quantity())
                .sum();
    }

    private int totalCoins(Dnd5eSheet sheet) {
        return sheet.copperPieces() + sheet.silverPieces() + sheet.electrumPieces() + sheet.goldPieces() + sheet.platinumPieces();
    }

    private double toKg(double lb) {
        return Math.round(lb * POUNDS_TO_KILOGRAMS * 10) / 10.0;
    }

    private int cappedDexterityModifier(Dnd5eArmorCategory armorCategory, int dexterityModifier) {
        return switch (armorCategory) {
            case LIGHT -> dexterityModifier;
            case MEDIUM -> Math.min(dexterityModifier, 2);
            case HEAVY -> 0;
        };
    }

    private CalculatedValue initiative(int dexterityModifier) {
        return new CalculatedValue(dexterityModifier,
                List.of(new Contribution("Dexterity modifier", dexterityModifier)));
    }

    private CalculatedValue hitPoints(Dnd5eSheet sheet, int constitutionModifier, Dnd5eModifiers modifiers, Dnd5eProvenance provenance) {
        if (sheet.hitPointBase() != null) {
            List<Contribution> contributions = new ArrayList<>();
            List<Contribution> perLevel = provenance.hitDice(sheet.hitPointBase());
            if (perLevel != null) {
                contributions.addAll(perLevel);
            } else {
                contributions.add(new Contribution("Hit dice (levels 1-" + sheet.level() + ")", sheet.hitPointBase()));
            }
            contributions.add(new Contribution("Constitution modifier (×" + sheet.level() + ")", sheet.level() * constitutionModifier));
            contributions.addAll(modifiers.hitPointBonuses());
            return withHalving(sheet, modifiers, withMaxHitPointsCustomizations(sheet, contributions));
        }
        int hitDieSize = sheet.hitDieSize();
        int level = sheet.level();
        int averageRollPerLevel = hitDieSize / 2 + 1;
        int remainingLevels = level - 1;

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(new Contribution("Hit die (level 1)", hitDieSize));
        contributions.add(new Contribution("Constitution modifier (level 1)", constitutionModifier));
        if (remainingLevels > 0) {
            contributions.add(new Contribution(
                    "Hit die (levels 2-" + level + ", average)", remainingLevels * averageRollPerLevel));
            contributions.add(new Contribution(
                    "Constitution modifier (levels 2-" + level + ")", remainingLevels * constitutionModifier));
        }
        contributions.addAll(modifiers.hitPointBonuses());
        return withHalving(sheet, modifiers, withMaxHitPointsCustomizations(sheet, contributions));
    }

    /** D&D Beyond's Override Max HP replaces the calculated lines; otherwise its Max HP Modifier adds one. */
    private static List<Contribution> withMaxHitPointsCustomizations(Dnd5eSheet sheet, List<Contribution> calculated) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        Integer override = customizations.hitPointsField(Dnd5eSheet.OVERRIDE_MAX_HP).value();
        if (override != null) {
            return new ArrayList<>(List.of(new Contribution("Override Max HP", override)));
        }
        Integer maxModifier = customizations.hitPointsField(Dnd5eSheet.MAX_HP_MODIFIER).value();
        if (maxModifier != null && maxModifier != 0) {
            calculated.add(new Contribution("Max HP Modifier", maxModifier));
        }
        return calculated;
    }

    /** The maximum from {@link Dnd5eFormulas}; a halving source (exhaustion 4) appears as a negative contribution. */
    private CalculatedValue withHalving(Dnd5eSheet sheet, Dnd5eModifiers modifiers, List<Contribution> contributions) {
        int maximum = Dnd5eFormulas.maxHitPoints(sheet);
        int beforeHalving = contributions.stream().mapToInt(Contribution::amount).sum();
        modifiers.hitPointMaximumHalvedBy().ifPresent(source -> contributions.add(new Contribution(source + " (halved)", maximum - beforeHalving)));
        return new CalculatedValue(maximum, List.copyOf(contributions));
    }

    private Map<String, CalculatedValue> savingThrows(Dnd5eSheet sheet, Map<String, Integer> abilityScores, int proficiencyBonusValue,
            Dnd5eModifiers modifiers, Dnd5eProvenance provenance) {
        List<Contribution> itemBonuses = modifiers.bonuses(Dnd5eModifierTarget.SAVING_THROWS, abilityScores);
        Map<String, CalculatedValue> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : abilityScores.entrySet()) {
            String ability = entry.getKey();
            Dnd5eCheckCustomization customization = sheet.customizationsOrEmpty().savingThrow(ability);
            if (customization.override() != null) {
                result.put(ability, new CalculatedValue(customization.override(),
                        List.of(new Contribution("Saving Throw Override", customization.override()))));
                continue;
            }
            int abilityModifier = modifier(entry.getValue());
            Dnd5eProficiencyLevel level = savingThrowLevel(sheet, ability);

            List<Contribution> contributions = new ArrayList<>();
            contributions.add(new Contribution(capitalize(ability) + " modifier", abilityModifier));
            int total = abilityModifier + addProficiency(contributions, level, proficiencyBonusValue,
                    provenance.grantSource("SAVING_THROW", ability));
            contributions.addAll(itemBonuses);
            total += itemBonuses.stream().mapToInt(Contribution::amount).sum();
            total += addCustomBonuses(contributions, customization);
            result.put(ability, new CalculatedValue(total, List.copyOf(contributions)));
        }
        return Map.copyOf(result);
    }

    /** Adds a customization's magic and misc bonuses as their own contributions, and returns their sum. */
    private static int addCustomBonuses(List<Contribution> contributions, Dnd5eCheckCustomization customization) {
        if (customization.magicBonus() != null && customization.magicBonus() != 0) {
            contributions.add(new Contribution("Magic Bonus", customization.magicBonus()));
        }
        if (customization.miscBonus() != null && customization.miscBonus() != 0) {
            contributions.add(new Contribution("Misc Bonus", customization.miscBonus()));
        }
        return customization.addedBonus();
    }

    private Map<String, String> proficiencyFlags(Dnd5eSheet sheet) {
        Map<String, String> levels = new HashMap<>();
        for (String ability : List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma")) {
            levels.put(ability, savingThrowLevel(sheet, ability).name());
        }
        return Map.copyOf(levels);
    }

    /** The customized proficiency level when set, else full proficiency for a proficient save. */
    private static Dnd5eProficiencyLevel savingThrowLevel(Dnd5eSheet sheet, String ability) {
        Dnd5eProficiencyLevel customized = sheet.customizationsOrEmpty().savingThrow(ability).proficiencyLevel();
        if (customized != null) {
            return customized;
        }
        return sheet.savingThrowProficiencies().contains(ability) ? Dnd5eProficiencyLevel.FULL : Dnd5eProficiencyLevel.NONE;
    }

    /** The customized level when set; else expertise over proficiency over nothing (HALF comes from feature modifiers). */
    private static Dnd5eProficiencyLevel skillLevel(Dnd5eSheet sheet, String skill) {
        Dnd5eProficiencyLevel customized = sheet.customizationsOrEmpty().skill(skill).proficiencyLevel();
        if (customized != null) {
            return customized;
        }
        if (sheet.skillExpertiseOrEmpty().contains(skill)) {
            return Dnd5eProficiencyLevel.EXPERT;
        }
        return sheet.skillProficiencies().contains(skill) ? Dnd5eProficiencyLevel.FULL : Dnd5eProficiencyLevel.NONE;
    }

    /** Adds the level's share of the proficiency bonus as its own contribution, naming its source when known, and returns it. */
    private static int addProficiency(
            List<Contribution> contributions, Dnd5eProficiencyLevel level, int proficiencyBonusValue, String source) {
        int amount = level.bonus(proficiencyBonusValue, false);
        if (level != Dnd5eProficiencyLevel.NONE) {
            contributions.add(new Contribution(withSource(level.contributionLabel(), source), amount));
        }
        return amount;
    }

    /** "Proficiency bonus" + "Rogue 1" → "Proficiency bonus (Rogue 1)"; "… (Expertise)" → "… (Expertise, Rogue 1)". */
    private static String withSource(String label, String source) {
        if (source == null) {
            return label;
        }
        return label.endsWith(")") ? label.substring(0, label.length() - 1) + ", " + source + ")" : label + " (" + source + ")";
    }

    /** PHB 2014, "Passive Checks": 10 + the skill's own bonus, so a customized skill carries over; an override replaces it. */
    private static Map<String, CalculatedValue> senses(Dnd5eSheet sheet, Map<String, CalculatedValue> skills) {
        return Map.of(
                "passivePerception", passiveScore(sheet, "passivePerception", "Perception", skills.get("perception")),
                "passiveInvestigation", passiveScore(sheet, "passiveInvestigation", "Investigation", skills.get("investigation")),
                "passiveInsight", passiveScore(sheet, "passiveInsight", "Insight", skills.get("insight")));
    }

    private static CalculatedValue passiveScore(Dnd5eSheet sheet, String passive, String skillLabel, CalculatedValue skill) {
        Integer override = sheet.customizationsOrEmpty().passive(passive).value();
        if (override != null) {
            return new CalculatedValue(override, List.of(new Contribution("Override Passive " + skillLabel, override)));
        }
        List<Contribution> contributions = new ArrayList<>();
        contributions.add(new Contribution("Base", 10));
        contributions.addAll(skill.contributions());
        return new CalculatedValue(10 + skill.value(), List.copyOf(contributions));
    }

    /** The native senses, each with its customized distance when set, then any sense the customization adds. */
    private static List<SpecialSense> specialSenses(Dnd5eSheet sheet) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        List<SpecialSense> senses = new ArrayList<>();
        for (Dnd5eSpecialSense sense : sheet.specialSenses()) {
            Integer distance = customizations.sense(sense.type().name()).value();
            senses.add(specialSense(sense.type(), distance != null ? distance : sense.rangeFeet()));
        }
        for (Dnd5eSenseType type : Dnd5eSenseType.values()) {
            Integer distance = customizations.sense(type.name()).value();
            boolean isNative = sheet.specialSenses().stream().anyMatch(sense -> sense.type() == type);
            if (distance != null && !isNative) {
                senses.add(specialSense(type, distance));
            }
        }
        return List.copyOf(senses);
    }

    private static SpecialSense specialSense(Dnd5eSenseType type, int rangeFeet) {
        String name = switch (type) {
            case DARKVISION -> "Darkvision";
            case BLINDSIGHT -> "Blindsight";
            case TREMORSENSE -> "Tremorsense";
            case TRUESIGHT -> "Truesight";
        };
        return new SpecialSense(type.name(), rangeFeet, name + " " + rangeFeet + " ft.");
    }

    private Map<String, CalculatedValue> skills(
            Dnd5eSheet sheet, Map<String, Integer> abilityScores, int proficiencyBonusValue, Dnd5eProvenance provenance) {
        Map<String, CalculatedValue> result = new HashMap<>();
        for (String skill : SKILL_ABILITIES.keySet()) {
            Dnd5eCheckCustomization customization = sheet.customizationsOrEmpty().skill(skill);
            if (customization.override() != null) {
                result.put(skill, new CalculatedValue(customization.override(),
                        List.of(new Contribution("Skill Override", customization.override()))));
                continue;
            }
            String governingAbility = governingAbility(sheet, skill);
            int abilityModifier = modifier(abilityScores.get(governingAbility));

            List<Contribution> contributions = new ArrayList<>();
            contributions.add(new Contribution(capitalize(governingAbility) + " modifier", abilityModifier));
            Dnd5eProficiencyLevel level = skillLevel(sheet, skill);
            String source = level == Dnd5eProficiencyLevel.EXPERT
                    ? provenance.grantSource("EXPERTISE", skill)
                    : provenance.grantSource("SKILL", skill);
            int total = abilityModifier + addProficiency(contributions, level, proficiencyBonusValue, source);
            total += addCustomBonuses(contributions, customization);
            result.put(skill, new CalculatedValue(total, List.copyOf(contributions)));
        }
        return Map.copyOf(result);
    }

    /** The ability a skill uses: the Stat Override when set, else the skill's own (PHB 2014). */
    private static String governingAbility(Dnd5eSheet sheet, String skill) {
        String statOverride = sheet.customizationsOrEmpty().skill(skill).statOverride();
        return statOverride != null ? statOverride : SKILL_ABILITIES.get(skill);
    }

    private static Map<String, String> skillGoverningAbilities(Dnd5eSheet sheet) {
        Map<String, String> abilities = new HashMap<>();
        for (String skill : SKILL_ABILITIES.keySet()) {
            abilities.put(skill, governingAbility(sheet, skill));
        }
        return Map.copyOf(abilities);
    }

    private Map<String, String> skillProficiencyFlags(Dnd5eSheet sheet) {
        Map<String, String> levels = new HashMap<>();
        for (String skill : SKILL_ABILITIES.keySet()) {
            levels.put(skill, skillLevel(sheet, skill).name());
        }
        return Map.copyOf(levels);
    }

    /** The player's custom skills: their ability's modifier (none without one), proficiency and bonuses, or the override. */
    private static List<CustomSkill> customSkills(Dnd5eSheet sheet, Map<String, Integer> abilityScores, int proficiencyBonusValue) {
        return sheet.customizationsOrEmpty().customSkills().stream().map(skill -> {
            CalculatedValue value;
            if (skill.override() != null) {
                value = new CalculatedValue(skill.override(), List.of(new Contribution("Skill Override", skill.override())));
            } else {
                List<Contribution> contributions = new ArrayList<>();
                int total = 0;
                if (skill.statOverride() != null) {
                    int abilityModifier = Dnd5eFormulas.modifier(abilityScores.get(skill.statOverride()));
                    contributions.add(new Contribution(
                            Character.toUpperCase(skill.statOverride().charAt(0)) + skill.statOverride().substring(1) + " modifier",
                            abilityModifier));
                    total += abilityModifier;
                }
                total += addProficiency(contributions, skill.proficiencyLevel(), proficiencyBonusValue, null);
                total += addCustomBonuses(contributions, new Dnd5eCheckCustomization(
                        null, null, skill.magicBonus(), null, skill.miscBonus(), null, null, null, null, null));
                value = new CalculatedValue(total, List.copyOf(contributions));
            }
            return new CustomSkill(skill.key(), skill.name(), skill.statOverride(), skill.proficiencyLevel().name(), value,
                    skill.notes(), skill.description());
        }).toList();
    }

    private String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
