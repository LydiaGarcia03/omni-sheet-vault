import { render, screen } from '@testing-library/react';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice } from '../../../builder/draftApi';
import type { LevelUp } from '../../../levelup/levelUpApi';
import { Dnd5eLevelUpPage } from './Dnd5eLevelUpPage';

const HIT_POINTS: CreationChoice = {
  id: 'build.rolledHitPoints',
  type: 'ROLLED_HIT_POINTS',
  parentChoiceId: null,
  prompt: 'Roll one hit die per level after the first',
  sourceLabel: 'Hit Points',
  count: 3,
  optional: false,
  pending: true,
  options: [
    { key: '0', label: 'Warlock lvl 2', sourceBook: null, summary: null, data: { group: 'warlock', classLevel: 2, sides: 8, average: 5, rollLabel: 'Hit points, Warlock 2 (d8)' } },
    { key: '1', label: 'Warlock lvl 3', sourceBook: null, summary: null, data: { group: 'warlock', classLevel: 3, sides: 8, average: 5, rollLabel: 'Hit points, Warlock 3 (d8)' } },
    { key: '2', label: 'Fighter lvl 1', sourceBook: null, summary: null, data: { group: 'fighter', classLevel: 1, sides: 10, average: 6, rollLabel: 'Hit points, Fighter 1 (d10)' } },
  ],
  selected: ['7', '4'],
};

const LEVEL_UP: LevelUp = {
  id: 'c1',
  name: 'Mez',
  systemId: 'dnd-5e',
  classSlug: 'warlock',
  classLevel: 3,
  build: {
    classes: [
      { classSlug: 'warlock', subclassSlug: null, level: 3 },
      { classSlug: 'fighter', subclassSlug: null, level: 1 },
    ],
    hitPointMethod: 'ROLLED',
    rolledHitPoints: [7, null, 4],
    choices: [],
  },
  plan: { choices: [HIT_POINTS], pendingCount: 1, problems: [] },
  preview: {
    progressions: [
      {
        key: 'warlock',
        name: 'Warlock',
        currentLevel: 2,
        columns: ['Level', 'Features'],
        rows: [['1st', 'Pact Magic'], ['2nd', 'Eldritch Invocations'], ['3rd', 'Pact Boon'], ['4th', 'Ability Score Improvement']],
      },
    ],
  } as unknown as LevelUp['preview'],
  maxHitPointsBefore: 20,
  maxHitPointsAfter: 25,
};

vi.mock('../../../levelup/levelUpApi', () => ({
  getLevelUp: vi.fn(() => Promise.resolve(LEVEL_UP)),
  startLevelUp: vi.fn(),
  saveLevelUp: vi.fn(),
  cancelLevelUp: vi.fn(),
  finishLevelUp: vi.fn(),
}));
vi.mock('../../../sheet/api', () => ({ getCharacterSheet: vi.fn(() => Promise.resolve({ experience: null })) }));
vi.mock('../../../catalogue/api', () => ({ getCatalogue: vi.fn(() => Promise.resolve([])) }));
vi.mock('../../../catalogue/SourceCodes', () => ({ SourceCodesProvider: ({ children }: { children: ReactNode }) => <>{children}</> }));
vi.mock('../../../shell/AppHeader', () => ({ AppHeader: () => null }));
vi.mock('../../../shell/AppFooter', () => ({ AppFooter: () => null }));

describe('Dnd5eLevelUpPage', () => {
  const renderPage = () =>
    render(
      <MemoryRouter>
        <Dnd5eLevelUpPage characterId="c1" characterName="Mez" userName="test" />
      </MemoryRouter>,
    );

  it("asks a rolled build only for the gained level's hit die, leaving earlier levels and other classes out", async () => {
    renderPage();

    expect(await screen.findByLabelText('Hit points, Warlock lvl 3')).toHaveValue(null);
    expect(screen.queryByLabelText('Hit points, Warlock lvl 2')).toBeNull();
    expect(screen.queryByLabelText('Hit points, Fighter lvl 1')).toBeNull();
    expect(screen.getByRole('button', { name: 'Finish leveling up' })).toBeDisabled();
  });

  it('ends with the class progression, marking the level the character reaches', async () => {
    renderPage();

    expect(await screen.findByText('Class progression · Warlock')).toBeInTheDocument();
    expect(screen.getByText(/After this level up:/)).toHaveTextContent('After this level up: warlock 3, character level 4.');
    expect(screen.getByRole('row', { current: true })).toHaveTextContent('3rdPact Boon');
  });
});
