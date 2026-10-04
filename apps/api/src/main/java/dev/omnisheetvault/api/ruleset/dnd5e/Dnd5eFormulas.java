package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Optional;

/**
 * Fixed 5e formulas shared by {@link Dnd5eSheetCalculator} (which needs the full
 * derivation trace) and {@link Dnd5eSheetMutator} (which only needs the ceiling
 * healing must not exceed). Kept in one place so the two can never drift apart.
 */
final class Dnd5eFormulas {

    private Dnd5eFormulas() {
    }

    static int modifier(int score) {
        return Math.floorDiv(score - 10, 2);
    }

    /**
     * The calculated maximum plus the Max HP Modifier, or the Override Max HP in place of both; then halved when a
     * modifier says so (exhaustion 4), never below 1.
     */
    static int maxHitPoints(Dnd5eSheet sheet) {
        Dnd5eCustomizations customizations = sheet.customizationsOrEmpty();
        Integer override = customizations.hitPointsField(Dnd5eSheet.OVERRIDE_MAX_HP).value();
        Integer maxModifier = customizations.hitPointsField(Dnd5eSheet.MAX_HP_MODIFIER).value();
        int total = override != null ? override : unhalvedCalculatedMaximum(sheet) + (maxModifier == null ? 0 : maxModifier);
        return halved(sheet, total);
    }

    /** The maximum without the player's Max HP Modifier or Override Max HP. */
    static int calculatedMaxHitPoints(Dnd5eSheet sheet) {
        return halved(sheet, unhalvedCalculatedMaximum(sheet));
    }

    /**
     * With a materialized build, the stored per-level hit die results plus Constitution for every level; otherwise one
     * hit die size at its average past level 1. Adds feature hit point bonuses (Tough).
     */
    private static int unhalvedCalculatedMaximum(Dnd5eSheet sheet) {
        int constitutionModifier = modifier(withItemAbilityScores(sheet).constitution());
        int base;
        if (sheet.hitPointBase() != null) {
            base = sheet.hitPointBase() + sheet.level() * constitutionModifier;
        } else {
            int hitDieSize = sheet.hitDieSize();
            int averageRollPerLevel = hitDieSize / 2 + 1;
            base = hitDieSize + constitutionModifier + (sheet.level() - 1) * (averageRollPerLevel + constitutionModifier);
        }
        return base + new Dnd5eModifiers(sheet).hitPointBonus();
    }

    private static int halved(Dnd5eSheet sheet, int total) {
        boolean halved = new Dnd5eModifiers(sheet).hitPointMaximumHalvedBy().isPresent();
        return Math.max(1, halved ? total / 2 : total);
    }

    /**
     * The sheet with its effective scores: an Override Score replaces the score; otherwise an active item's set score
     * (Gauntlets of Ogre Power: Strength 19) applies when higher, then the Other Modifier adds. The ability
     * customizations are consumed, so an already effective sheet passes through unchanged.
     */
    static Dnd5eSheet withItemAbilityScores(Dnd5eSheet sheet) {
        return sheet.withAbilityScores(
                        effectiveScore(sheet, "strength", sheet.strength()),
                        effectiveScore(sheet, "dexterity", sheet.dexterity()),
                        effectiveScore(sheet, "constitution", sheet.constitution()),
                        effectiveScore(sheet, "intelligence", sheet.intelligence()),
                        effectiveScore(sheet, "wisdom", sheet.wisdom()),
                        effectiveScore(sheet, "charisma", sheet.charisma()))
                .withCustomizations(sheet.customizationsOrEmpty().withoutAbilities());
    }

    private static int effectiveScore(Dnd5eSheet sheet, String ability, int score) {
        Dnd5eAbilityCustomization customization = sheet.customizationsOrEmpty().ability(ability);
        if (customization.overrideScore() != null) {
            return customization.overrideScore();
        }
        int otherModifier = customization.otherModifier() == null ? 0 : customization.otherModifier();
        return Math.clamp((long) itemScore(sheet, ability, score) + otherModifier, 1, 30);
    }

    /** The score with an active item's set score applied when higher. */
    static int itemScore(Dnd5eSheet sheet, String ability, int score) {
        return itemScoreSetter(sheet, ability).map(Dnd5eModifier::value).filter(value -> value > score).orElse(score);
    }

    /** The active item setting an ability score highest, if any (its {@code source} is the item's name). */
    static Optional<Dnd5eModifier> itemScoreSetter(Dnd5eSheet sheet, String ability) {
        Dnd5eModifierTarget target = Dnd5eModifierTarget.scoreOf(ability);
        return sheet.items().stream()
                .filter(Dnd5eItem::active)
                .flatMap(item -> item.mechanics().modifiers().stream())
                .filter(modifier -> modifier.target() == target && modifier.type() == Dnd5eModifierType.SET)
                .max((a, b) -> Integer.compare(a.value(), b.value()));
    }
}
