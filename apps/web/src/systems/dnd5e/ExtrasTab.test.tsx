import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet, Extra } from '../../sheet/api';
import { movementCallout } from './ExtraRow';
import { ExtrasTab } from './ExtrasTab';

function extra(key: string, name: string, category: string, creatureType: string, additionalSpeeds: string | null = null): Extra {
  return {
    key,
    name,
    category,
    armorClass: 12,
    maxHitPoints: 2,
    currentHitPoints: 2,
    temporaryHitPoints: 0,
    speed: 40,
    statBlock: {
      size: 'Tiny',
      creatureType,
      alignment: 'unaligned',
      hitDiceLabel: '1d4',
      additionalSpeeds,
      initiativeBonus: 2,
      abilityScores: [],
      skills: [],
      senses: 'passive Perception 13',
      languages: '--',
      challengeRating: '0',
      traits: [],
      actions: [],
    },
  } as unknown as Extra;
}

const CAT = extra('cat', 'Cat', 'FAMILIAR', '(Fey, Fiend, or Celestial)', 'Climb 30 ft.');
const HORSE = extra('horse', 'Riding Horse', 'MOUNT', 'Beast');

function renderTab(extras: Extra[], onOpenPane = vi.fn()) {
  render(<ExtrasTab sheet={{ extras } as unknown as CharacterSheet} onMutate={vi.fn()} onOpenDetail={vi.fn()} onOpenPane={onOpenPane} />);
  return onOpenPane;
}

describe('ExtrasTab', () => {
  it('offers a chip only for the categories the character has, in the singular', () => {
    renderTab([CAT]);

    expect(screen.getByRole('button', { name: 'All' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Familiar' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Mount' })).toBeNull();
  });

  it('shows size with type, the speed with its other movement, and a Notes column', () => {
    renderTab([CAT]);

    expect(screen.getByText('Tiny (Fey, Fiend, or Celestial)')).toBeInTheDocument();
    expect(screen.getByText('Climbing')).toBeInTheDocument();
    expect(screen.getByText('Notes')).toBeInTheDocument();
  });

  it('searches names and types', () => {
    renderTab([CAT, HORSE]);

    fireEvent.change(screen.getByPlaceholderText('Search Names, Types, or Tags'), { target: { value: 'beast' } });

    expect(screen.getByText('Riding Horse')).toBeInTheDocument();
    expect(screen.queryByText('Cat')).toBeNull();
  });

  it('opens Manage Extras in the sidebar', () => {
    const onOpenPane = renderTab([]);

    fireEvent.click(screen.getByRole('button', { name: 'Manage Extras' }));

    expect(onOpenPane).toHaveBeenCalledWith(expect.objectContaining({ kind: 'pane' }));
  });
});

describe('movementCallout', () => {
  it('names every other way the creature moves', () => {
    expect(movementCallout('Climb 30 ft., Fly 60 ft. (hover)')).toBe('Climbing, Flying');
    expect(movementCallout(null)).toBe('');
  });
});
