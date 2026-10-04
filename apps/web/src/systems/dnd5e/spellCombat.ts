import type { Spell } from '../../sheet/api';

/**
 * A spell that deals damage (attack-roll or save-based) — renders as a row in
 * the Actions tab's attack table (`SpellAttackRow`), alongside weapon attacks.
 * Direct owner request, 2026-09-03, confirmed live against D&D Beyond: Helga
 * Flinthand's (dndbeyond.com/characters/50149479) Guiding Bolt and Inflict
 * Wounds, and Copy of Raya's (.../characters/56008841) Fire Bolt, Ray of Frost
 * and Ice Knife, all sit in the Actions tab's attack table, not just the
 * Spells tab. A pure-utility spell (Mage Armor, Detect Magic, Fog Cloud,
 * Charm Person, Minor Illusion, Mending) stays Spells-tab-only.
 *
 * **Corrected 2026-09-20**: healing no longer counts. The original
 * `isCombatSpell` included `isHealingSpell(spell)`, but D&D Beyond's own
 * Actions tab never shows a healing spell there (re-confirmed live against
 * Helga's own sheet, filtering both "All" and "Action" — Cure Wounds never
 * appears), so that inclusion was wrong, not just a stale assumption.
 * `damageDiceCount != null` alone isn't enough to exclude it either — a
 * healing spell's own heal-roll dice live in that same field (see
 * `Dnd5eSpell`'s own doc comment: a healing spell's roll is stored the exact
 * same way a damage roll is, only `damageType` stays null) — so `!isHealingSpell`
 * is checked explicitly.
 */
export function isCombatSpell(spell: Spell): boolean {
  return spell.adjustments?.displayAsAttack === true || (!isHealingSpell(spell) && (spell.attackRoll || spell.damageDiceCount != null));
}

/**
 * The spell's dice ("1d10"), scaled to {@code castLevel} when given, with a customized damage bonus ("1d10+3"); null
 * without dice.
 */
export function spellDamageLabel(spell: Spell, castLevel?: number): string | null {
  const count = castLevel == null ? spell.damageDiceCount : scaleDiceCount(spell, castLevel);
  if (count == null || spell.damageDiceSides == null) {
    return null;
  }
  const bonus = spell.adjustments?.damageBonus ?? 0;
  const dice = `${count}d${spell.damageDiceSides}`;
  return bonus === 0 ? dice : `${dice}${bonus > 0 ? '+' : '−'}${Math.abs(bonus)}`;
}

/**
 * PHB upcasting math, mirrored from `Dnd5eMechanicResolver#scaledDiceCount` — kept
 * in sync manually since this is a display-only preview, not the roll itself (the
 * server always resolves the actual roll independently). Falls back to the base
 * count when the spell doesn't scale this way, or the scaling die doesn't match
 * the base die's own sides (this app's simplification: one die size per roll).
 */
export function scaleDiceCount(spell: Spell, castAtLevel: number): number | null {
  if (spell.damageDiceCount == null) {
    return null;
  }
  if (spell.higherLevelsDamageDiceCount == null || spell.higherLevelsDamageDiceSides !== spell.damageDiceSides) {
    return spell.damageDiceCount;
  }
  const levelsAboveBase = Math.max(0, castAtLevel - spell.level);
  return spell.damageDiceCount + spell.higherLevelsDamageDiceCount * levelsAboveBase;
}

export function isHealingSpell(spell: Spell): boolean {
  return spell.effectSummary === 'Healing';
}

/**
 * Which of the Actions tab's four sections a spell's own casting time buckets
 * into: each section (Action/Bonus Action/Reaction/Other) shows a "Spells"
 * line listing the character's own spells whose casting time matches, plus
 * a separate "Ritual Spells" line for ritual-castable ones in that same
 * bucket (`ActionsTab.tsx`). Mirrors `castingTimeAbbreviation`
 * (`SpellRow.tsx`)'s own parsing of the same `Dnd5eSpell.castingTime` text
 * ("1 Action"/"1 Bonus Action"/"1 Reaction"/"1 Minute"/"1 Hour"), not a new
 * stored field.
 */
export type SpellActionType = 'ACTION' | 'BONUS_ACTION' | 'REACTION' | 'OTHER';

export function spellActionType(castingTime: string): SpellActionType {
  const match = castingTime.match(/^\d+\s+(.+)$/);
  const unit = (match ? match[1] : castingTime).toLowerCase();
  if (unit.startsWith('bonus action')) return 'BONUS_ACTION';
  if (unit.startsWith('reaction')) return 'REACTION';
  if (unit.startsWith('action')) return 'ACTION';
  return 'OTHER';
}
