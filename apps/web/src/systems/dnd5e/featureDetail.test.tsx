import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet, FeatureAction, FeatureTrait } from '../../sheet/api';
import { EntityDetailPanel } from '../../sheet/EntityDetailPanel';
import { actionTypeLabel, featureActionDetailRequest, featureTraitDetailRequest } from './featureDetail';

const secondWindTrait: FeatureTrait = {
  key: 'class:fighter:1:second-wind',
  name: 'Second Wind',
  category: 'CLASS_FEATURE',
  source: 'Fighter',
  summary: '',
  description: 'You have a limited well of stamina.\nYou can use it again after a rest.',
  maxUses: null,
  usedCount: 0,
  rechargeTrigger: null,
};

const secondWindAction: FeatureAction = {
  key: 'second-wind',
  name: 'Second Wind',
  actionType: 'BONUS_ACTION',
  description: 'Regain hit points.',
  maxUses: 1,
  usedCount: 0,
  rechargeTrigger: 'SHORT_OR_LONG_REST',
  parentName: 'Second Wind',
  parentKey: secondWindTrait.key,
};

const sheet = { featureTraits: [secondWindTrait], featureActions: [secondWindAction] } as unknown as CharacterSheet;

describe('featureDetail', () => {
  it('words action types the way D&D Beyond does', () => {
    expect(actionTypeLabel('ACTION')).toBe('1 Action');
    expect(actionTypeLabel('BONUS_ACTION')).toBe('1 Bonus Action');
    expect(actionTypeLabel('REACTION')).toBe('1 Reaction');
  });

  it('shows Limited Use under the header and opens the granting feature from the parent line', () => {
    const onOpenDetail = vi.fn();
    render(<EntityDetailPanel {...featureActionDetailRequest(secondWindAction, sheet, vi.fn(), onOpenDetail)} />);

    expect(screen.getByText('Limited Use')).toBeTruthy();
    expect(screen.getByText('1 Bonus Action')).toBeTruthy();
    expect(screen.queryByText('Recharge')).toBeNull();

    fireEvent.click(screen.getByRole('button', { name: 'Second Wind' }));

    expect(onOpenDetail).toHaveBeenCalledWith(expect.objectContaining({ entityKey: `feature-trait:${secondWindTrait.key}` }));
  });

  it('lists the actions a feature grants, with their uses', () => {
    const onMutate = vi.fn();
    render(<EntityDetailPanel {...featureTraitDetailRequest(secondWindTrait, sheet, onMutate, vi.fn())} />);

    expect(screen.getByText('Fighter')).toBeTruthy();
    expect(screen.getByText('You can use it again after a rest.')).toBeTruthy();
    expect(screen.getByText('Uses')).toBeTruthy();

    fireEvent.click(screen.getByRole('button', { name: 'Spend use 1' }));

    expect(onMutate).toHaveBeenCalledWith({ type: 'USE_FEATURE_ACTION', featureKey: 'second-wind' });
  });
});
