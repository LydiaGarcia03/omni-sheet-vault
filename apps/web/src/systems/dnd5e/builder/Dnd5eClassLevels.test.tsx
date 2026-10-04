import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { CreationChoice, ProgressionTable } from '../../../builder/draftApi';
import type { Dnd5eBuild } from './dnd5eBuild';
import { Dnd5eClassLevels } from './Dnd5eClassLevels';

function choice(id: string, group: string, level: number | null, pending: boolean, parentChoiceId: string | null = null): CreationChoice {
  return { id, type: 'X', parentChoiceId, prompt: id, sourceLabel: '', count: 1, optional: false, pending, options: [], selected: pending ? [] : ['x'], group, level };
}

const BUILD: Dnd5eBuild = {
  speciesSlug: null,
  subspeciesName: null,
  speciesVariantName: null,
  backgroundSlug: null,
  classes: [
    { classSlug: 'fighter', subclassSlug: null, level: 3 },
    { classSlug: 'wizard', subclassSlug: null, level: 1 },
  ],
  abilityScoreMethod: null,
  baseAbilityScores: {},
  hitPointMethod: 'FIXED',
  rolledHitPoints: [],
  choices: [],
};

const FIGHTER_TABLE: ProgressionTable = {
  key: 'fighter',
  name: 'Fighter',
  currentLevel: 3,
  columns: ['Level', 'Proficiency Bonus', 'Features'],
  rows: [
    ['1st', '+2', 'Fighting Style, Second Wind'],
    ['2nd', '+2', 'Action Surge'],
    ['3rd', '+2', 'Martial Archetype'],
  ],
};

const WIZARD_TABLE: ProgressionTable = {
  key: 'wizard',
  name: 'Wizard',
  currentLevel: 1,
  columns: ['Level', 'Proficiency Bonus', 'Features'],
  rows: [['1st', '+2', 'Spellcasting, Arcane Recovery']],
};

const CHOICES = [
  choice('fighter-skills', 'fighter', 1, false),
  choice('fighting-style', 'fighter', 1, false),
  choice('subclass', 'fighter', 3, true),
  choice('wizard-spellbook', 'wizard', null, true),
];

function renderLevels(build = BUILD) {
  return render(
    <Dnd5eClassLevels
      build={build}
      classLabels={{ fighter: 'Fighter', wizard: 'Wizard' }}
      choices={CHOICES}
      progressions={[FIGHTER_TABLE, WIZARD_TABLE]}
      renderChoice={(item) => <div key={item.id}>choice {item.id}</div>}
      isNested={(item) => item.parentChoiceId !== null}
    />,
  );
}

describe('Dnd5eClassLevels', () => {
  it('shows one section per level, each with its features and choices, open only where a choice is left', () => {
    const { container } = renderLevels();

    const sections = [...container.querySelectorAll('details.builder-level')];
    expect(sections).toHaveLength(3);
    expect(sections.map((section) => section.hasAttribute('open'))).toEqual([false, false, true]);
    expect(sections[0].textContent).toContain('Fighting Style, Second Wind');
    expect(sections[0].textContent).toContain('choice fighter-skills');
    expect(sections[1].textContent).toContain('Nothing to choose at this level.');
    expect(sections[2].textContent).toContain('1 choice');
    expect(screen.getByText('Fighter · levels 1–3')).toBeInTheDocument();
  });

  it('gives each class a tab and files choices of the whole class after its levels', () => {
    renderLevels();

    const tabs = screen.getAllByRole('tab');
    expect(tabs.map((tab) => tab.textContent)).toEqual(['Fighter 31', 'Wizard 11']);
    fireEvent.click(tabs[1]);

    expect(screen.getByText('Wizard · level 1')).toBeInTheDocument();
    expect(screen.getByText('Every level')).toBeInTheDocument();
    expect(screen.getByText('choice wizard-spellbook')).toBeInTheDocument();
  });

  it('shows only the selected class progression, and switches it with the tab', () => {
    renderLevels();

    expect(screen.getByText('Class progression · Fighter')).toBeInTheDocument();
    expect(screen.queryByText('Class progression · Wizard')).toBeNull();

    fireEvent.click(screen.getAllByRole('tab')[1]);

    expect(screen.getByText('Class progression · Wizard')).toBeInTheDocument();
    expect(screen.queryByText('Class progression · Fighter')).toBeNull();
  });

  it('puts the class progression last, after any other sections', () => {
    const { container } = render(
      <Dnd5eClassLevels
        build={BUILD}
        classLabels={{ fighter: 'Fighter', wizard: 'Wizard' }}
        choices={CHOICES}
        progressions={[FIGHTER_TABLE, WIZARD_TABLE]}
        renderChoice={(item) => <div key={item.id}>choice {item.id}</div>}
        isNested={() => false}
        beforeProgression={<section className="other-frame">Other class choices</section>}
      />,
    );

    const sections = [...container.children];
    expect(sections.at(-1)?.matches('details.builder-progression')).toBe(true);
    expect(sections.at(-2)?.textContent).toBe('Other class choices');
  });

  it("gives the selected class its own hit points section, counting its unrolled levels as pending", () => {
    render(
      <Dnd5eClassLevels
        build={BUILD}
        classLabels={{ fighter: 'Fighter', wizard: 'Wizard' }}
        choices={CHOICES}
        progressions={[FIGHTER_TABLE, WIZARD_TABLE]}
        renderChoice={(item) => <div key={item.id}>choice {item.id}</div>}
        isNested={() => false}
        renderHitPoints={(slug) => (slug === 'fighter' ? <div>fighter rows</div> : null)}
        hitPointsLeftIn={(slug) => (slug === 'fighter' ? 2 : 0)}
      />,
    );

    expect(screen.getByText('Hit points · Fighter')).toBeInTheDocument();
    expect(screen.getByText('fighter rows')).toBeInTheDocument();
    expect(screen.getAllByRole('tab')[0].textContent).toBe('Fighter 33');
    fireEvent.click(screen.getAllByRole('tab')[1]);

    expect(screen.queryByText(/^Hit points ·/)).toBeNull();
  });

  it('has no tabs for a single class', () => {
    renderLevels({ ...BUILD, classes: [BUILD.classes[0]] });

    expect(screen.queryByRole('tablist')).toBeNull();
  });
});
