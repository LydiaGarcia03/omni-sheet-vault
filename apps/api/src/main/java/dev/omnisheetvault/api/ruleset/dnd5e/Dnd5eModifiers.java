package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.RollModeInfo;
import dev.omnisheetvault.api.ruleset.RollNote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The sheet's active modifiers (adr-0007) and the values they produce. They come from
 * feature traits, active conditions and exhaustion, active effects cast on the
 * character, active items (equipped, attuned when required), worn armor and being Overloaded.
 * Situational ones (with a {@code restriction}) are kept apart and only reported as notes.
 */
final class Dnd5eModifiers {

    static final String OVERLOADED = "Overloaded";

    private final Dnd5eSheet sheet;
    private final List<Dnd5eModifier> active;
    private final List<Dnd5eModifier> situational;

    Dnd5eModifiers(Dnd5eSheet sheet) {
        this(sheet, false);
    }

    Dnd5eModifiers(Dnd5eSheet sheet, boolean overloaded) {
        this.sheet = sheet;
        List<Dnd5eModifier> all = new ArrayList<>();
        sheet.featureTraits().forEach(trait -> all.addAll(trait.modifiersOrEmpty()));
        sheet.conditionModifiersOrEmpty().values().forEach(all::addAll);
        sheet.activeEffectsOrEmpty().stream()
                .filter(Dnd5eActiveEffect::appliesToCharacter)
                .forEach(effect -> all.addAll(effect.modifiers()));
        sheet.items().stream()
                .filter(Dnd5eItem::active)
                .forEach(item -> all.addAll(item.mechanics().modifiers()));
        sheet.items().stream()
                .filter(item -> item.equipped() && item.itemKind() == Dnd5eItemKind.ARMOR)
                .forEach(armor -> all.addAll(armorModifiers(armor, sheet.strength())));
        if (overloaded) {
            all.add(new Dnd5eModifier(Dnd5eModifierType.DISADVANTAGE, Dnd5eModifierTarget.STRENGTH_ABILITY_CHECKS, 0, null, null, OVERLOADED));
            all.add(new Dnd5eModifier(Dnd5eModifierType.DISADVANTAGE, Dnd5eModifierTarget.STRENGTH_SAVING_THROWS, 0, null, null, OVERLOADED));
        }
        this.active = all.stream().filter(modifier -> !modifier.situational()).toList();
        this.situational = all.stream().filter(Dnd5eModifier::situational).toList();
    }

    /**
     * Worn armor's own drawbacks (PHB 144–145): stealth disadvantage, and 10 ft. less speed below its
     * Strength requirement. Species that ignore the speed penalty (dwarves) aren't modeled yet.
     */
    private static List<Dnd5eModifier> armorModifiers(Dnd5eItem armor, int strength) {
        List<Dnd5eModifier> modifiers = new ArrayList<>();
        if (armor.stealthDisadvantage()) {
            modifiers.add(new Dnd5eModifier(Dnd5eModifierType.DISADVANTAGE, Dnd5eModifierTarget.STEALTH_CHECKS, 0, null, null, armor.name()));
        }
        if (armor.strengthRequirement() != null && strength < armor.strengthRequirement()) {
            modifiers.add(new Dnd5eModifier(Dnd5eModifierType.BONUS, Dnd5eModifierTarget.SPEED, -10, null, null,
                    armor.name() + " (Strength " + armor.strengthRequirement() + ")"));
        }
        return modifiers;
    }

    List<Dnd5eModifier> of(Dnd5eModifierTarget target) {
        return active.stream().filter(modifier -> modifier.target() == target).toList();
    }

    /** Every {@code BONUS} on the target, as labelled contributions; an ability-bound one adds that ability's modifier. */
    List<Contribution> bonuses(Dnd5eModifierTarget target, Map<String, Integer> abilityScores) {
        List<Contribution> contributions = new ArrayList<>();
        for (Dnd5eModifier modifier : of(target)) {
            if (modifier.type() == Dnd5eModifierType.BONUS) {
                contributions.add(new Contribution(modifier.source(), valueOf(modifier, abilityScores)));
            }
        }
        return contributions;
    }

    /** The highest {@code SET_BASE} on the target, if any. */
    Optional<Dnd5eModifier> highestBase(Dnd5eModifierTarget target, Map<String, Integer> abilityScores) {
        return of(target).stream()
                .filter(modifier -> modifier.type() == Dnd5eModifierType.SET_BASE)
                .max((a, b) -> Integer.compare(valueOf(a, abilityScores), valueOf(b, abilityScores)));
    }

    /** Hit point maximum bonuses: each per-level bonus times character levels, or its class's levels, then flat bonuses. */
    List<Contribution> hitPointBonuses() {
        List<Contribution> contributions = new ArrayList<>();
        for (Dnd5eModifier modifier : of(Dnd5eModifierTarget.HIT_POINTS_PER_LEVEL)) {
            int levels = modifier.classSlug() == null ? sheet.level() : classLevel(modifier.classSlug());
            contributions.add(new Contribution(modifier.source() + " (+" + modifier.value() + " × " + levels + ")", modifier.value() * levels));
        }
        for (Dnd5eModifier modifier : of(Dnd5eModifierTarget.HIT_POINT_MAXIMUM)) {
            if (modifier.type() == Dnd5eModifierType.BONUS) {
                contributions.add(new Contribution(modifier.source(), modifier.value()));
            }
        }
        return contributions;
    }

    int hitPointBonus() {
        return hitPointBonuses().stream().mapToInt(Contribution::amount).sum();
    }

    /** The first source halving the hit point maximum, if any (exhaustion level 4). */
    Optional<String> hitPointMaximumHalvedBy() {
        return halvedBy(Dnd5eModifierTarget.HIT_POINT_MAXIMUM);
    }

    /** Walking speed after {@code BONUS} (armor too heavy: −10), then {@code SET} (the lowest wins, e.g. grappled's 0) and {@code HALVE}. */
    int speed(int baseSpeed) {
        int adjusted = Math.max(0, baseSpeed + speedBonus());
        int speed = of(Dnd5eModifierTarget.SPEED).stream()
                .filter(modifier -> modifier.type() == Dnd5eModifierType.SET)
                .mapToInt(Dnd5eModifier::value)
                .min()
                .orElse(adjusted);
        return halvedBy(Dnd5eModifierTarget.SPEED).isPresent() ? speed / 2 : speed;
    }

    private int speedBonus() {
        return of(Dnd5eModifierTarget.SPEED).stream()
                .filter(modifier -> modifier.type() == Dnd5eModifierType.BONUS)
                .mapToInt(Dnd5eModifier::value)
                .sum();
    }

    /** How {@link #speed} changes the base: the lowest {@code SET} and then any {@code HALVE}, each labelled with its source. */
    List<Contribution> speedChanges(int baseSpeed) {
        List<Contribution> changes = new ArrayList<>();
        for (Dnd5eModifier bonus : of(Dnd5eModifierTarget.SPEED)) {
            if (bonus.type() == Dnd5eModifierType.BONUS) {
                changes.add(new Contribution(bonus.source(), bonus.value()));
            }
        }
        int speed = Math.max(0, baseSpeed + speedBonus());
        Optional<Dnd5eModifier> lowestSet = of(Dnd5eModifierTarget.SPEED).stream()
                .filter(modifier -> modifier.type() == Dnd5eModifierType.SET)
                .min((a, b) -> Integer.compare(a.value(), b.value()));
        if (lowestSet.isPresent() && lowestSet.get().value() != speed) {
            changes.add(new Contribution(lowestSet.get().source(), lowestSet.get().value() - speed));
            speed = lowestSet.get().value();
        }
        Optional<String> halvedBy = halvedBy(Dnd5eModifierTarget.SPEED);
        if (halvedBy.isPresent() && speed / 2 != speed) {
            changes.add(new Contribution(halvedBy.get() + " (halved)", speed / 2 - speed));
        }
        return changes;
    }

    /** Extra attacks when taking the Attack action: the highest {@code SET}, else none. */
    int extraAttacks() {
        return of(Dnd5eModifierTarget.EXTRA_ATTACKS).stream()
                .filter(modifier -> modifier.type() == Dnd5eModifierType.SET)
                .mapToInt(Dnd5eModifier::value)
                .max()
                .orElse(0);
    }

    /** The advantage and disadvantage sources acting on a roll governed by any of the targets. */
    RollModeInfo rollMode(Collection<Dnd5eModifierTarget> targets) {
        Set<String> advantage = new LinkedHashSet<>();
        Set<String> disadvantage = new LinkedHashSet<>();
        for (Dnd5eModifier modifier : active) {
            if (!targets.contains(modifier.target())) {
                continue;
            }
            if (modifier.type() == Dnd5eModifierType.ADVANTAGE) {
                advantage.add(modifier.source());
            } else if (modifier.type() == Dnd5eModifierType.DISADVANTAGE) {
                disadvantage.add(modifier.source());
            }
        }
        return new RollModeInfo(List.copyOf(advantage), List.copyOf(disadvantage));
    }

    /** Situational modifiers ("advantage on saving throws against poison"): shown as notes, never applied. */
    List<RollNote> rollNotes() {
        return situational.stream()
                .map(modifier -> new RollNote(modifier.type().name(), modifier.target().name(), modifier.restriction(), modifier.source()))
                .distinct()
                .toList();
    }

    static int valueOf(Dnd5eModifier modifier, Map<String, Integer> abilityScores) {
        if (modifier.ability() == null) {
            return modifier.value();
        }
        return modifier.value() + Dnd5eFormulas.modifier(abilityScores.getOrDefault(modifier.ability(), 10));
    }

    private Optional<String> halvedBy(Dnd5eModifierTarget target) {
        return of(target).stream().filter(modifier -> modifier.type() == Dnd5eModifierType.HALVE).map(Dnd5eModifier::source).findFirst();
    }

    private int classLevel(String classSlug) {
        return sheet.classLevelsOrEmpty().stream()
                .filter(classLevel -> classLevel.classSlug().equals(classSlug))
                .mapToInt(Dnd5eClassLevel::level)
                .sum();
    }
}
