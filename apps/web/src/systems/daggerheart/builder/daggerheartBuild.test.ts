import { describe, expect, it } from 'vitest';
import type { BuildPlan, CreationChoice } from '../../../builder/draftApi';
import { describeBuild, picksLeft, signed, stepOfChoice, withClass } from './daggerheartBuild';

function choice(id: string, type: string, count: number, selected: string[], optional = false, labels: Record<string, string> = {}): CreationChoice {
  return {
    id,
    type,
    parentChoiceId: null,
    prompt: id,
    sourceLabel: '',
    count,
    optional,
    pending: !optional && selected.length < count,
    options: Object.entries(labels).map(([key, label]) => ({ key, label, sourceBook: null, summary: null })),
    selected,
  };
}

const plan: BuildPlan = {
  choices: [
    choice('class', 'class', 1, ['ranger'], false, { ranger: 'Ranger' }),
    choice('subclass', 'class', 1, []),
    choice('ancestry', 'heritage', 1, ['fungril'], false, { fungril: 'Fungril' }),
    choice('community', 'heritage', 1, ['slyborne'], false, { slyborne: 'Slyborne' }),
    choice('transformation', 'heritage', 1, [], true),
    choice('traits', 'traits', 6, ['agility', 'strength']),
  ],
  problems: [],
  pendingCount: 2,
};

describe('daggerheartBuild', () => {
  it('files the subclass under its own step and every other choice under its type', () => {
    expect(stepOfChoice(plan.choices[1])).toBe('subclass');
    expect(stepOfChoice(plan.choices[0])).toBe('class');
    expect(stepOfChoice(plan.choices[2])).toBe('heritage');
  });

  it('counts the picks a step still lacks, leaving optional choices out', () => {
    expect(picksLeft(plan, 'heritage')).toBe(0);
    expect(picksLeft(plan, 'traits')).toBe(4);
    expect(picksLeft(plan, 'subclass')).toBe(1);
  });

  it('clears the subclass, cards and class item when the class changes', () => {
    const build = { class: 'wizard', subclass: 'school-of-war', domainCards: ['book-of-ava'], classItem: 'A book', armor: 'leather-armor' };

    expect(withClass(build, 'ranger')).toEqual({ class: 'ranger', subclass: '', domainCards: [], classItem: '', armor: 'leather-armor' });
    expect(withClass(build, 'wizard')).toBe(build);
  });

  it('describes the character as community, ancestry and class', () => {
    expect(describeBuild({}, plan)).toBe('Slyborne Fungril Ranger');
    expect(describeBuild({ mixedAncestry: { name: 'Goblin-Orc' } }, plan)).toBe('Slyborne Goblin-Orc Ranger');
  });

  it('signs trait modifiers with a true minus', () => {
    expect([2, 0, -1].map(signed)).toEqual(['+2', '+0', '−1']);
  });
});
