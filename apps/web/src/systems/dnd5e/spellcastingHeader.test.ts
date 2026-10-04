import { describe, expect, it } from 'vitest';
import type { SpellcastingClassInfo } from '../../sheet/api';
import { groupHeaderValues } from './spellcastingHeader';

function caster(className: string, modifier: number, classCaster = true): SpellcastingClassInfo {
  const value = (amount: number) => ({ value: amount, contributions: [] });
  return {
    className,
    spellcastingAbility: 'charisma',
    spellcastingModifier: value(modifier),
    spellAttackBonus: value(modifier + 3),
    spellSaveDc: value(modifier + 11),
    castingType: 'KNOWN',
    cantripsKnownMax: 0,
    spellsKnownMax: null,
    spellsPreparedMax: null,
    classCaster,
  };
}

describe('groupHeaderValues', () => {
  it('merges classes that share a value into one entry', () => {
    const values = groupHeaderValues([caster('Bard', 4), caster('Paladin', 4)], (info) => info.spellcastingModifier.value);

    expect(values).toEqual([{ value: 4, sources: ['Bard', 'Paladin'] }]);
  });

  it('keeps different values apart', () => {
    const values = groupHeaderValues([caster('Wizard', 4), caster('Cleric', 2)], (info) => info.spellSaveDc.value);

    expect(values.map((entry) => entry.value)).toEqual([15, 13]);
  });

  it('leaves out non-class casters when a class casts', () => {
    const values = groupHeaderValues([caster('Wizard', 4), caster('High Elf', 4, false)], (info) => info.spellcastingModifier.value);

    expect(values).toEqual([{ value: 4, sources: ['Wizard'] }]);
  });

  it('shows a granted caster when no class casts', () => {
    const values = groupHeaderValues([caster('Tiefling', 3, false)], (info) => info.spellcastingModifier.value);

    expect(values).toEqual([{ value: 3, sources: ['Tiefling'] }]);
  });
});
