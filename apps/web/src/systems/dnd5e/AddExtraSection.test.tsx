import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AddExtraSection } from './AddExtraSection';
import { challengeValue } from './useCreatureCatalogue';

const { getCatalogue } = vi.hoisted(() => ({ getCatalogue: vi.fn() }));
vi.mock('../../catalogue/api', () => ({ getCatalogue }));

function creature(id: string, name: string, creatureType: string, challengeRating: string, environments: string[] = []) {
  return {
    id,
    systemId: 'dnd-5e',
    kind: 'CREATURE',
    slug: id,
    name,
    sourceBook: 'Monster Manual',
    sourcePage: 1,
    tags: environments,
    description: { value: null, redacted: false },
    data: {
      size: 'Tiny',
      creatureType,
      alignment: 'unaligned',
      armorClass: 12,
      armorClassText: '12',
      hitPoints: 2,
      hitDice: '1d4',
      speed: 40,
      additionalSpeeds: null,
      challengeRating,
      familiar: true,
      environments,
    },
  };
}

describe('AddExtraSection', () => {
  beforeEach(() => {
    getCatalogue.mockResolvedValue([creature('owl', 'Owl', 'Beast', '0', ['forest']), creature('ogre', 'Ogre', 'Giant', '2', ['hill'])]);
  });

  it('lists the catalogue creatures once a category is chosen, a familiar showing Find Familiar’s type', async () => {
    render(<AddExtraSection onMutate={vi.fn()} />);

    fireEvent.change(screen.getByRole('combobox', { name: 'Choose a category' }), { target: { value: 'FAMILIAR' } });

    expect(await screen.findByText('Owl')).toBeInTheDocument();
    expect(screen.getByText(/A spirit that serves you/)).toBeInTheDocument();
    expect(screen.getAllByText(/Tiny \(Fey, Fiend, or Celestial\)/)).toHaveLength(2);
  });

  it('filters by name, type or habitat and by challenge range', async () => {
    render(<AddExtraSection onMutate={vi.fn()} />);
    fireEvent.change(screen.getByRole('combobox', { name: 'Choose a category' }), { target: { value: 'SUMMONED_CREATURE' } });
    await screen.findByText('Owl');

    fireEvent.change(screen.getByRole('searchbox', { name: 'Filter creatures' }), { target: { value: 'forest' } });
    expect(screen.queryByText('Ogre')).toBeNull();

    fireEvent.change(screen.getByRole('searchbox', { name: 'Filter creatures' }), { target: { value: '' } });
    fireEvent.change(screen.getByRole('combobox', { name: 'Lowest challenge' }), { target: { value: '1' } });
    expect(screen.queryByText('Owl')).toBeNull();
    expect(screen.getByText('Ogre')).toBeInTheDocument();
  });

  it('adds one at once, or several from the preview', async () => {
    const onMutate = vi.fn();
    render(<AddExtraSection onMutate={onMutate} />);
    fireEvent.change(screen.getByRole('combobox', { name: 'Choose a category' }), { target: { value: 'MOUNT' } });
    await screen.findByText('Owl');

    fireEvent.click(screen.getByRole('button', { name: 'Add Owl' }));
    fireEvent.click(screen.getByRole('button', { name: 'Preview Ogre' }));
    fireEvent.click(screen.getByRole('button', { name: 'More' }));
    fireEvent.click(screen.getByRole('button', { name: 'Add 2' }));

    expect(onMutate).toHaveBeenNthCalledWith(1, { type: 'ADD_CATALOGUE_EXTRA', catalogueEntryId: 'owl', category: 'MOUNT', quantity: 1 });
    expect(onMutate).toHaveBeenNthCalledWith(2, { type: 'ADD_CATALOGUE_EXTRA', catalogueEntryId: 'ogre', category: 'MOUNT', quantity: 2 });
  });
});

describe('challengeValue', () => {
  it('reads fractions', () => {
    expect(challengeValue('1/4')).toBe(0.25);
    expect(challengeValue('17')).toBe(17);
  });
});
