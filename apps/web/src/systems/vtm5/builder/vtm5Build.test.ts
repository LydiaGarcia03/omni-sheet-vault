import { describe, expect, it } from 'vitest';
import { VTM5_PORTRAIT_PRESETS } from '../portraits/vtm5PortraitPresets';
import { choiceOf, countAt, describeBuild, setFocus, VTM5_STEPS } from './vtm5Build';

describe('vtm5Build', () => {
  it('describes a build by clan, age and predator, or as a vampire', () => {
    expect(describeBuild({ clan: 'Toreador', age: 'Neonate', predator: 'Siren' })).toBe('Toreador · Neonate · Siren');
    expect(describeBuild({})).toBe('Vampire');
  });

  it('counts the traits placed at a rating', () => {
    expect(countAt({ Strength: 2, Wits: 2, Resolve: 3 }, 2)).toBe(2);
    expect(countAt(undefined, 1)).toBe(0);
  });

  it('finds a plan choice by id and lists the builder steps in order', () => {
    const plan = { choices: [{ id: 'clan' }], problems: [], pendingCount: 1 } as never;
    expect(choiceOf(plan, 'clan')).toEqual({ id: 'clan' });
    expect(VTM5_STEPS.map((step) => step.id)).toEqual(['basics', 'clan', 'attributes', 'skills', 'disciplines', 'predator', 'advantages', 'beliefs', 'review']);
    setFocus(null);
  });

  it('offers the default portraits numbered in order', () => {
    expect(VTM5_PORTRAIT_PRESETS).toHaveLength(23);
    expect(VTM5_PORTRAIT_PRESETS.slice(0, 3).map((preset) => preset.id)).toEqual(['1', '2', '3']);
  });
});
