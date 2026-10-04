import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CalculatedValue, CharacterSheet, CustomSkill, PaneRequest } from '../../sheet/api';
import { CustomSkillPane, SkillPane, type SkillPaneNavigation } from './Dnd5eSkillPanes';
import { SKILLS } from './skills';
import { SkillsPanel } from './SkillsPanel';

const two: CalculatedValue = { value: 2, contributions: [] };

const CARTOGRAPHY: CustomSkill = {
  key: 'c1',
  name: 'Cartography',
  abilityKey: null,
  proficiencyLevel: 'FULL',
  value: { value: 3, contributions: [] },
  notes: null,
  description: null,
};

function sheet(customSkills: CustomSkill[] = [], customizations: unknown = {}): CharacterSheet {
  return {
    skills: Object.fromEntries(SKILLS.map(({ key }) => [key, two])),
    skillProficiencies: {},
    skillGoverningAbilities: Object.fromEntries(SKILLS.map(({ key }) => [key, 'dexterity'])),
    rollModes: {},
    customSkills,
    provenance: { customizations },
  } as unknown as CharacterSheet;
}

const navigation = (): SkillPaneNavigation => ({ openAllSkills: vi.fn(), openEntry: vi.fn() });

describe('SkillsPanel', () => {
  it('lists the custom skills after the eighteen, and opens All Skills from the gear and Additional Skills', () => {
    const onOpenPane = vi.fn();
    const onMutate = vi.fn();
    render(<SkillsPanel sheet={sheet([CARTOGRAPHY])} onRoll={vi.fn()} onRollContextMenu={vi.fn()} onMutate={onMutate} onOpenPane={onOpenPane} />);

    expect(screen.getByRole('button', { name: 'Open Cartography details' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Manage Skills' }));
    fireEvent.click(screen.getByRole('button', { name: 'Additional Skills' }));
    expect(onOpenPane).toHaveBeenCalledTimes(2);

    render(<>{(onOpenPane.mock.calls[0][0] as PaneRequest).render(sheet([CARTOGRAPHY]))}</>);
    expect(screen.getByRole('heading', { name: 'All Skills' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Custom Skills' }));
    fireEvent.click(screen.getByRole('button', { name: '+ Add Custom Skill' }));
    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'customSkills', target: 'new', value: {} });
  });
});

describe('SkillPane', () => {
  it('steps to the next skill, and saves a stat override with the skill’s Customize', () => {
    const nav = navigation();
    const onMutate = vi.fn();
    render(<SkillPane sheet={sheet()} skill="acrobatics" onMutate={onMutate} navigation={nav} />);

    expect(screen.getByRole('button', { name: /Prev/ })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: /Next/ }));
    expect(nav.openEntry).toHaveBeenCalledWith({ kind: 'skill', key: 'animalHandling' });

    fireEvent.change(screen.getByLabelText('Skill Stat Override'), { target: { value: 'wisdom' } });
    expect(onMutate).toHaveBeenCalledWith({
      type: 'CUSTOMIZE',
      group: 'skills',
      target: 'acrobatics',
      value: expect.objectContaining({ statOverride: 'wisdom' }),
    });
  });
});

describe('CustomSkillPane', () => {
  it('is the last entry, and removes the custom skill', () => {
    const nav = navigation();
    const onMutate = vi.fn();
    const stored = { customSkills: [{ key: 'c1', name: 'Cartography', statOverride: null, proficiencyLevel: 'FULL', override: null, magicBonus: null, miscBonus: null, notes: null, description: null }] };
    render(<CustomSkillPane sheet={sheet([CARTOGRAPHY], stored)} skillKey="c1" onMutate={onMutate} navigation={nav} />);

    expect(screen.getByRole('button', { name: /Next/ })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Remove' }));

    expect(onMutate).toHaveBeenCalledWith({ type: 'REMOVE_CUSTOMIZATION', group: 'customSkills', target: 'c1' });
    expect(nav.openAllSkills).toHaveBeenCalled();
  });
});
