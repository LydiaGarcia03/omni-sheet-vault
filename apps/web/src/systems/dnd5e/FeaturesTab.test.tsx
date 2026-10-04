import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet, FeatureTrait } from '../../sheet/api';
import { FeaturesTab } from './FeaturesTab';

function sheetWith(featureTraits: Partial<FeatureTrait>[]): CharacterSheet {
  return { featureTraits } as unknown as CharacterSheet;
}

const classFeature: Partial<FeatureTrait> = {
  key: 'class:wizard:1:spellcasting',
  name: 'Spellcasting',
  category: 'CLASS_FEATURE',
  source: 'Wizard',
  summary: 'You can cast spells.',
  description: '',
  maxUses: null,
  usedCount: 0,
  rechargeTrigger: null,
};

describe('FeaturesTab', () => {
  it('offers Manage Feats even when the character has no feat', () => {
    const onOpenManageFeats = vi.fn();
    render(<FeaturesTab sheet={sheetWith([classFeature])} onMutate={vi.fn()} onOpenDetail={vi.fn()} onOpenManageFeats={onOpenManageFeats} />);

    fireEvent.click(screen.getByRole('button', { name: 'Manage Feats' }));

    expect(onOpenManageFeats).toHaveBeenCalledOnce();
  });

  it('hides the Feats section when another category is selected', () => {
    render(<FeaturesTab sheet={sheetWith([classFeature])} onMutate={vi.fn()} onOpenDetail={vi.fn()} onOpenManageFeats={vi.fn()} />);

    fireEvent.click(screen.getByRole('button', { name: 'Class Features', pressed: false }));

    expect(screen.queryByRole('button', { name: 'Manage Feats' })).toBeNull();
  });

  it('lists the chosen subclass beneath the feature that grants it', () => {
    const arcaneTradition: Partial<FeatureTrait> = {
      ...classFeature,
      key: 'class:wizard:2:arcane-tradition',
      name: 'Arcane Tradition',
      choices: ['School of Evocation'],
    };
    render(<FeaturesTab sheet={sheetWith([classFeature, arcaneTradition])} onMutate={vi.fn()} onOpenDetail={vi.fn()} onOpenManageFeats={vi.fn()} />);

    expect(screen.getAllByRole('heading', { name: 'Wizard' })).toHaveLength(1);
    expect(screen.getByText('School of Evocation')).toBeTruthy();
  });
});
