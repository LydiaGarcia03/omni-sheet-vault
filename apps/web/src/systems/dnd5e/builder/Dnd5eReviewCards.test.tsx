import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it } from 'vitest';
import type { BuildPlan, CreationChoice, DraftPreview } from '../../../builder/draftApi';
import type { Dnd5eBuild } from './dnd5eBuild';
import { Dnd5eReviewCards } from './Dnd5eReviewCards';

function choice(id: string, options: [string, string][], selected: string[], pending = false): CreationChoice {
  return {
    id,
    type: 'X',
    parentChoiceId: null,
    prompt: id,
    sourceLabel: '',
    count: 1,
    optional: false,
    pending,
    options: options.map(([key, label]) => ({ key, label, sourceBook: null, summary: null })),
    selected,
  };
}

const BUILD: Dnd5eBuild = {
  speciesSlug: 'dwarf',
  subspeciesName: 'Mountain',
  speciesVariantName: null,
  backgroundSlug: 'soldier',
  classes: [{ classSlug: 'fighter', subclassSlug: 'fighter-champion', level: 3 }],
  abilityScoreMethod: 'POINT_BUY',
  baseAbilityScores: { strength: 15, dexterity: 13, constitution: 14, intelligence: 8, wisdom: 10, charisma: 12 },
  hitPointMethod: 'FIXED',
  rolledHitPoints: [],
  choices: [],
};

const PLAN: BuildPlan = {
  choices: [
    choice('build.classes', [['fighter', 'Fighter']], ['fighter']),
    choice('build.class.fighter.subclass', [['fighter-champion', 'Champion']], ['fighter-champion']),
    choice('build.abilityScores', [['POINT_BUY', 'Point buy']], ['POINT_BUY']),
    choice('class:fighter:1:fighting-style', [], [], true),
  ],
  problems: [],
  pendingCount: 1,
};

const PREVIEW: DraftPreview = {
  abilities: ['strength', 'dexterity', 'constitution', 'intelligence', 'wisdom', 'charisma'].map((ability) => ({
    ability,
    score: 10,
    modifier: 0,
    contributions: [],
  })),
  vitals: null,
  selections: {
    species: { name: 'Mountain Dwarf', sourceBook: null, facts: [{ label: 'Ability bonuses', text: 'CON +2, STR +2' }], grants: [] },
    background: { name: 'Soldier', sourceBook: null, facts: [], grants: [{ label: 'Skill proficiencies', text: 'Athletics, Intimidation' }] },
  },
  startingEquipment: {
    inventory: [
      { key: 'chain-mail', name: 'Chain Mail', quantity: 1, equipAction: 'WEAR', equipped: true },
      { key: 'longsword', name: 'Longsword', quantity: 1, equipAction: 'WIELD', equipped: false },
    ],
    gold: 10,
    silver: 0,
    copper: 0,
  },
};

function renderCards() {
  return render(
    <MemoryRouter>
      <Dnd5eReviewCards characterId="c1" name="Thorin" build={BUILD} plan={PLAN} preview={PREVIEW} />
    </MemoryRouter>,
  );
}

describe('Dnd5eReviewCards', () => {
  it('sums up each step with what the draft holds', () => {
    renderCards();

    expect(screen.getByText('Thorin')).toBeInTheDocument();
    expect(screen.getByText('Mountain Dwarf')).toBeInTheDocument();
    expect(screen.getByText('CON +2, STR +2')).toBeInTheDocument();
    expect(screen.getByText('Fighter 3')).toBeInTheDocument();
    expect(screen.getByText('Champion')).toBeInTheDocument();
    expect(screen.getByText('Athletics, Intimidation')).toBeInTheDocument();
    expect(screen.getByText('Point buy')).toBeInTheDocument();
    expect(screen.getByText('Chain Mail')).toBeInTheDocument();
    expect(screen.getByText('10 gp')).toBeInTheDocument();
  });

  it('says when rolled hit points are still the average of levels not rolled yet', () => {
    const rolledBuild: Dnd5eBuild = { ...BUILD, hitPointMethod: 'ROLLED', rolledHitPoints: [7, null] };
    const rolledPlan: BuildPlan = { ...PLAN, choices: [...PLAN.choices, { ...choice('build.rolledHitPoints', [], ['7'], true), count: 2 }] };
    const withVitals = { ...PREVIEW, vitals: { hitPoints: { value: 28, contributions: [] } } as unknown as DraftPreview['vitals'] };
    render(
      <MemoryRouter>
        <Dnd5eReviewCards characterId="c1" name="Thorin" build={rolledBuild} plan={rolledPlan} preview={withVitals} />
      </MemoryRouter>,
    );

    expect(screen.getByText('28 (average for 1 of 2 levels not rolled yet)')).toBeInTheDocument();
  });

  it('marks the steps with choices left and links each card back to its step', () => {
    const { container } = renderCards();

    const classes = [...container.querySelectorAll('.builder-review-card')].find((card) => card.textContent?.startsWith('Classes'));
    expect(classes?.textContent).toContain('1 left');
    expect(screen.getAllByRole('link', { name: 'Edit' }).map((link) => link.getAttribute('href'))).toEqual([
      '/characters/c1/build/basics',
      '/characters/c1/build/species',
      '/characters/c1/build/classes',
      '/characters/c1/build/background',
      '/characters/c1/build/abilities',
      '/characters/c1/build/equipment',
    ]);
  });
});
