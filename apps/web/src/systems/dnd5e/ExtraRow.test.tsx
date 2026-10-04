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
  it('opens the extra with its category as the parent line, hit points first and Delete last', () => {
    const onOpenDetail = vi.fn();
    const onMutate = vi.fn();
    render(<ExtraRow extra={cat} onMutate={onMutate} onOpenDetail={onOpenDetail} />);
    fireEvent.click(screen.getByRole('button', { name: 'Open Cat details' }));
    const request: EntityDetailRequest = onOpenDetail.mock.calls[0][0];

    render(<EntityDetailPanel {...request} />);

    expect(request.parent).toBe('Familiar');
    expect(request.actionsPosition).toBe('section');
    expect(screen.getByText('Hit Points 2/2')).toBeTruthy();
    expect(screen.getAllByRole('table')[0].textContent).toContain('STR');
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));
    expect(onMutate).toHaveBeenCalledWith({ type: 'REMOVE_EXTRA', extraKey: 'cat' });
  });
});
