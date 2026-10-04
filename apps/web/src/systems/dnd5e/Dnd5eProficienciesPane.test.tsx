import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CatalogueEntry } from '../../catalogue/api';
import type { CharacterSheet } from '../../sheet/api';
import { Dnd5eProficienciesPane, proficiencyOptions } from './Dnd5eProficienciesPane';

function entry(kind: string, name: string, sourceBook: string, data: unknown = {}): CatalogueEntry {
  return {
    id: `${kind}-${name}`,
    systemId: 'dnd-5e',
    kind,
    slug: name.toLowerCase(),
    name,
    sourceBook,
    sourcePage: null,
    tags: [],
    description: { value: null, redacted: false },
    data,
  };
}

const LANGUAGES = [entry('LANGUAGE', 'Elvish', "Player's Handbook"), entry('LANGUAGE', 'Aquan', 'Monster Manual')];
const ITEMS = [
  entry('ITEM', 'Longsword', 'PHB', { itemKind: 'WEAPON', weaponCategory: 'Martial', rarity: 'none' }),
  entry('ITEM', 'Club', 'PHB', { itemKind: 'WEAPON', weaponCategory: 'Simple', rarity: 'none' }),
  entry('ITEM', 'Flame Tongue', 'DMG', { itemKind: 'WEAPON', weaponCategory: 'Martial', rarity: 'rare' }),
  entry('ITEM', 'Chain Mail', 'PHB', { itemKind: 'ARMOR', typeLabel: 'Heavy Armor', rarity: 'none' }),
  entry('ITEM', "Thieves' Tools", 'PHB', { itemKind: 'GEAR', typeLabel: 'Tool', rarity: 'none' }),
];

vi.mock('../../catalogue/api', () => ({
  getCatalogue: (_systemId: string, kind: string) => Promise.resolve(kind === 'LANGUAGE' ? LANGUAGES : ITEMS),
}));

function sheet(proficiencies: unknown[] = []): CharacterSheet {
  return {
    armorProficiencies: ['Light Armor'],
    weaponProficiencies: [],
    toolProficiencies: [],
    languages: ['Common', 'Elvish'],
    provenance: { customizations: { proficiencies } },
  } as unknown as CharacterSheet;
}

describe('proficiencyOptions', () => {
  it('offers mundane weapons grouped Simple then Martial, each with its source', () => {
    const groups = proficiencyOptions('WEAPON', LANGUAGES, ITEMS);

    expect(groups.map((group) => group.label)).toEqual(['Simple', 'Martial']);
    expect(groups[1].options).toEqual([{ value: 'Longsword', label: 'Longsword (PHB)' }]);
  });

  it('offers languages grouped by source with the Player’s Handbook first, and tools by their type', () => {
    expect(proficiencyOptions('LANGUAGE', LANGUAGES, ITEMS).map((group) => group.label)).toEqual(["Player's Handbook", 'Monster Manual']);
    expect(proficiencyOptions('TOOL', LANGUAGES, ITEMS)[0].options[0].value).toBe("Thieves' Tools");
  });
});

describe('Dnd5eProficienciesPane', () => {
  it('adds an existing proficiency picked from the catalogue', async () => {
    const onMutate = vi.fn();
    render(<Dnd5eProficienciesPane sheet={sheet()} onMutate={onMutate} />);

    fireEvent.change(screen.getByLabelText('Proficiency Type'), { target: { value: 'existing:LANGUAGE' } });
    await waitFor(() => expect(screen.getByRole('option', { name: "Elvish (Player's Handbook)" })).toBeInTheDocument());
    fireEvent.change(screen.getByLabelText('Available Proficiencies'), { target: { value: 'Elvish' } });

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'proficiencies', target: 'new', value: { type: 'LANGUAGE', name: 'Elvish' } });
  });

  it('adds a custom proficiency at once', () => {
    const onMutate = vi.fn();
    render(<Dnd5eProficienciesPane sheet={sheet()} onMutate={onMutate} />);

    fireEvent.change(screen.getByLabelText('Proficiency Type'), { target: { value: 'custom:TOOL' } });

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'proficiencies', target: 'new', value: { type: 'TOOL', custom: true } });
  });

  it('lists hand-added proficiencies with Remove, a name field for custom ones, and the notes', () => {
    const onMutate = vi.fn();
    render(
      <Dnd5eProficienciesPane
        sheet={sheet([
          { key: 'a', type: 'LANGUAGE', name: 'Elvish', custom: false, notes: null },
          { key: 'b', type: 'TOOL', name: '', custom: true, notes: null },
        ])}
        onMutate={onMutate}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Languages' })).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Custom proficiency name'), { target: { value: "Glassblower's Tools" } });
    fireEvent.blur(screen.getByLabelText('Custom proficiency name'));
    fireEvent.click(screen.getByRole('button', { name: 'Remove Elvish' }));

    expect(onMutate).toHaveBeenCalledWith({
      type: 'CUSTOMIZE',
      group: 'proficiencies',
      target: 'b',
      value: { name: "Glassblower's Tools", notes: null },
    });
    expect(onMutate).toHaveBeenCalledWith({ type: 'REMOVE_CUSTOMIZATION', group: 'proficiencies', target: 'a' });
  });

  it('ends with every proficiency the character has', () => {
    render(<Dnd5eProficienciesPane sheet={sheet()} onMutate={vi.fn()} />);

    expect(screen.getByText('Languages').nextElementSibling).toHaveTextContent('Common, Elvish');
  });
});
