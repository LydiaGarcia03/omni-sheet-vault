import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { EntityDetailRequest, Extra } from '../../sheet/api';
import { EntityDetailPanel } from '../../sheet/EntityDetailPanel';
import { ExtraRow } from './ExtraRow';

const cat = {
  key: 'cat',
  name: 'Cat',
  category: 'FAMILIAR',
  armorClass: 12,
  maxHitPoints: 2,
  currentHitPoints: 2,
  temporaryHitPoints: 0,
  speed: 40,
  statBlock: {
    size: 'Tiny',
    creatureType: 'beast',
    alignment: 'unaligned',
    hitDiceLabel: '1d4',
    additionalSpeeds: null,
    initiativeBonus: 2,
    abilityScores: [
      { abilityKey: 'strength', score: 3, modifier: -4, save: -4 },
      { abilityKey: 'dexterity', score: 15, modifier: 2, save: 2 },
    ],
    skills: [],
    senses: 'passive Perception 13',
    languages: '--',
    challengeRating: '0',
    traits: [{ name: 'Keen Smell', description: 'Smells well.' }],
    actions: [],
  },
} as unknown as Extra;

describe('ExtraRow', () => {
  it('opens the extra with its category over the name, its book, hit points under Customize and no Delete', () => {
    const onOpenDetail = vi.fn();
    render(<ExtraRow extra={{ ...cat, source: 'Monster Manual' }} onMutate={vi.fn()} onOpenDetail={onOpenDetail} />);
    fireEvent.click(screen.getByRole('button', { name: 'Open Cat details' }));
    const request: EntityDetailRequest = onOpenDetail.mock.calls[0][0];

    render(<EntityDetailPanel {...request} />);

    expect(request.parent).toBe('Familiar');
    expect(request.subtitle).toBe('Monster Manual');
    expect(screen.getByText('2/2').closest('summary')).toHaveTextContent('Hit Points');
    expect(screen.getAllByRole('table')[0].textContent).toContain('STR');
    expect(screen.queryByRole('button', { name: 'Delete' })).toBeNull();
  });

  it('shows the notes in the row and reads the challenge with its XP and proficiency bonus', () => {
    const onOpenDetail = vi.fn();
    const noted = { ...cat, notes: 'Scouts ahead', statBlock: { ...cat.statBlock, challengeXp: 10, challengeProficiencyBonus: 2 } };
    render(<ExtraRow extra={noted} onMutate={vi.fn()} onOpenDetail={onOpenDetail} />);

    expect(screen.getByText('Scouts ahead')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Open Cat details' }));
    render(<EntityDetailPanel {...onOpenDetail.mock.calls[0][0]} />);
    expect(screen.getByText('0 (XP 10; PB +2)')).toBeInTheDocument();
  });
});
