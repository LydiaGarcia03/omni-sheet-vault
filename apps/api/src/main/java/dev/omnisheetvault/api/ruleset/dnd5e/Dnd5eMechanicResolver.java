package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.MechanicResolver;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;
import dev.omnisheetvault.api.ruleset.UnresolvableRollException;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Phase 5's roll targets (ability checks, saving throws, skill checks, initiative —
 * every one a d20 plus a modifier the sheet calculator already produced) plus phase
 * 8's attack rolls, resolved from the same calculated vitals, plus phase 9's hit
 * die shape (one die's worth — the count spent is applied by the caller, since
 * {@link ResolvedRoll} has no room for a player-chosen multiplier). Casting a
 * spell into damage dice is also phase 9, through the same seam.
 */
@Component
public class Dnd5eMechanicResolver implements MechanicResolver {

    private static final int D20 = 20;

    @Override
    public String systemId() {
        return Dnd5eGameSystem.SYSTEM_ID;
    }

    @Override
    public ResolvedRoll resolve(VitalsZone vitals, RollKind kind, String key, Integer castAtLevel) {
        return switch (kind) {
            case ABILITY_CHECK -> fromMap(vitals.abilityModifiers(), kind, key, "check");
            case SAVING_THROW -> fromMap(vitals.savingThrows(), kind, key, "saving throw");
            case SKILL_CHECK -> skillCheck(vitals, kind, key);
            case INITIATIVE -> new ResolvedRoll(1, D20, vitals.initiative().value(), "Initiative: roll");
            case ATTACK_HIT -> attackHit(vitals, kind, key);
            case ATTACK_DAMAGE -> attackDamage(vitals, kind, key);
            case ATTACK_DAMAGE_VERSATILE -> versatileDamage(vitals, kind, key);
            case HIT_DICE -> hitDie(vitals, kind, key);
            case SPELL_ATTACK -> spellAttack(vitals, kind, key);
            case SPELL_DAMAGE -> spellDamage(vitals, kind, key, castAtLevel);
            case SPELL_HEAL -> spellHeal(vitals, kind, key, castAtLevel);
        };
    }

    /**
     * One hit die plus the Constitution modifier — per hit die spent, not the whole spend. {@code key} is the
     * die size to roll ("6"); null rolls the largest size with dice left.
     */
    private ResolvedRoll hitDie(VitalsZone vitals, RollKind kind, String key) {
        int dieSize;
        try {
            dieSize = key == null ? vitals.hitDice().defaultDieSize() : Integer.parseInt(key);
        } catch (NumberFormatException e) {
            throw new UnresolvableRollException(kind, key);
        }
        if (vitals.hitDice().pool(dieSize).isEmpty()) {
            throw new UnresolvableRollException(kind, key);
        }
        int constitutionModifier = vitals.abilityModifiers().get("constitution").value();
        return new ResolvedRoll(1, dieSize, constitutionModifier, "Hit Die (d" + dieSize + "): heal");
    }

    /** One of the eighteen skills, or a custom skill the player added (by its key). */
    private ResolvedRoll skillCheck(VitalsZone vitals, RollKind kind, String key) {
        if (key != null && !vitals.skills().containsKey(key)) {
            return vitals.customSkills().stream()
                    .filter(skill -> skill.key().equals(key))
                    .findFirst()
                    .map(skill -> new ResolvedRoll(1, D20, skill.value().value(), skill.name() + ": check"))
                    .orElseThrow(() -> new UnresolvableRollException(kind, key));
        }
        return fromMap(vitals.skills(), kind, key, "check");
    }

    private ResolvedRoll fromMap(Map<String, CalculatedValue> values, RollKind kind, String key, String label) {
        if (key == null) {
            throw new UnresolvableRollException(kind, null);
        }
        CalculatedValue value = values.get(key);
        if (value == null) {
            throw new UnresolvableRollException(kind, key);
        }
        return new ResolvedRoll(1, D20, value.value(), humanize(key) + ": " + label);
    }

    private ResolvedRoll attackHit(VitalsZone vitals, RollKind kind, String key) {
        AttackRow attack = attack(vitals, kind, key);
        return new ResolvedRoll(1, D20, attack.toHit().value(), attack.name() + ": attack roll");
    }

    private ResolvedRoll attackDamage(VitalsZone vitals, RollKind kind, String key) {
        AttackRow attack = attack(vitals, kind, key);
        return new ResolvedRoll(attack.damageDiceCount(), attack.damageDiceSides(), attack.damageModifier(),
                attack.name() + ": damage");
    }

    /** PHB "Versatile": the weapon's larger die when wielded with two hands, same modifier. */
    private ResolvedRoll versatileDamage(VitalsZone vitals, RollKind kind, String key) {
        AttackRow attack = attack(vitals, kind, key);
        if (attack.versatileDiceCount() == null || attack.versatileDiceSides() == null) {
            throw new UnresolvableRollException(kind, key);
        }
        return new ResolvedRoll(attack.versatileDiceCount(), attack.versatileDiceSides(), attack.damageModifier(),
                attack.name() + ": two-handed damage");
    }

    private AttackRow attack(VitalsZone vitals, RollKind kind, String key) {
        if (key == null) {
            throw new UnresolvableRollException(kind, null);
        }
        AttackRow attack = vitals.attacks().get(key);
        if (attack == null) {
            throw new UnresolvableRollException(kind, key);
        }
        return attack;
    }

    /** Rejected if the spell's own {@code attackRoll} flag is false — not every spell needs one. */
    private ResolvedRoll spellAttack(VitalsZone vitals, RollKind kind, String key) {
        Spell spell = spell(vitals, kind, key);
        if (!spell.attackRoll()) {
            throw new UnresolvableRollException(kind, key);
        }
        SpellcastingClassInfo info = spellcastingInfo(vitals, spell.className(), kind, key);
        return new ResolvedRoll(1, D20, spell.adjustments().attack(info.spellAttackBonus().value()), spell.name() + ": spell attack roll");
    }

    /** No ability modifier added — PHB: spell damage dice are fixed, unlike a weapon attack's; only a customized damage bonus. */
    private ResolvedRoll spellDamage(VitalsZone vitals, RollKind kind, String key, Integer castAtLevel) {
        Spell spell = spell(vitals, kind, key);
        if (spell.damageDiceCount() == null || spell.damageDiceSides() == null) {
            throw new UnresolvableRollException(kind, key);
        }
        int diceCount = scaledDiceCount(spell, castAtLevel);
        return new ResolvedRoll(diceCount, spell.damageDiceSides(), spell.adjustments().damageBonus(), spell.name() + ": damage");
    }

    /** PHB: a healing spell (e.g. Cure Wounds) adds the spellcasting ability modifier, unlike {@link #spellDamage}. */
    private ResolvedRoll spellHeal(VitalsZone vitals, RollKind kind, String key, Integer castAtLevel) {
        Spell spell = spell(vitals, kind, key);
        if (spell.damageDiceCount() == null || spell.damageDiceSides() == null) {
            throw new UnresolvableRollException(kind, key);
        }
        SpellcastingClassInfo info = spellcastingInfo(vitals, spell.className(), kind, key);
        int diceCount = scaledDiceCount(spell, castAtLevel);
        return new ResolvedRoll(diceCount, spell.damageDiceSides(), info.spellcastingModifier().value(),
                spell.name() + ": healing");
    }

    /**
     * PHB upcasting: a spell cast using a slot above its own level adds
     * {@code higherLevelsDamageDiceCount} dice for every level above base, when the
     * spell's own 5etools data models that scaling (null together for a spell that
     * doesn't scale this way, e.g. Fire Bolt — a cantrip scales by character level,
     * a different mechanic, not modeled here). {@code castAtLevel} null or at/below
     * the spell's own level means no scaling — the row's own quick-roll target
     * never sends a level at all, and casting at the spell's base level is not an
     * upcast. {@link ResolvedRoll} can only notate one die size, so this only scales
     * when {@code higherLevelsDamageDiceSides} matches the base die — true for every
     * real spell checked so far (5e's own scaling convention: more of the same die,
     * never a different one), but a future mismatch falls back to the unscaled base
     * count rather than silently rolling the wrong die size.
     */
    private int scaledDiceCount(Spell spell, Integer castAtLevel) {
        if (castAtLevel == null || spell.higherLevelsDamageDiceCount() == null
                || !spell.higherLevelsDamageDiceSides().equals(spell.damageDiceSides())) {
            return spell.damageDiceCount();
        }
        int levelsAboveBase = Math.max(0, castAtLevel - spell.level());
        return spell.damageDiceCount() + spell.higherLevelsDamageDiceCount() * levelsAboveBase;
    }

    private Spell spell(VitalsZone vitals, RollKind kind, String key) {
        if (key == null) {
            throw new UnresolvableRollException(kind, null);
        }
        return vitals.spells().stream()
                .filter(candidate -> candidate.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new UnresolvableRollException(kind, key));
    }

    private SpellcastingClassInfo spellcastingInfo(VitalsZone vitals, String className, RollKind kind, String key) {
        return vitals.spellcasting().stream()
                .filter(info -> info.className().equals(className))
                .findFirst()
                .orElseThrow(() -> new UnresolvableRollException(kind, key));
    }

    private String humanize(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                result.append(' ').append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
