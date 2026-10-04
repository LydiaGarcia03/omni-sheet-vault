import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CatalogueEntry } from '../catalogue/api';
import type { FeatureTrait } from './api';
import { FeatManagementPanel } from './FeatManagementPanel';

const catalogue = [
  { id: 'f1', slug: 'alert', name: 'Alert', sourceBook: "Player's Handbook", data: {} },
  { id: 'f2', slug: 'actor', name: 'Actor', sourceBook: "Player's Handbook", data: {} },
  { id: 'f3', slug: 'squat-nimbleness', name: 'Squat Nimbleness', sourceBook: "Xanathar's Guide to Everything", data: {} },
] as unknown as CatalogueEntry[];

const knownFeats = [{ key: 'alert', name: 'Alert', category: 'FEAT' }] as unknown as FeatureTrait[];

describe('FeatManagementPanel', () => {
  it('groups feats by source with Add, or Remove for one already taken', () => {
    const onLearn = vi.fn();
    const onRemove = vi.fn();
    render(<FeatManagementPanel kind="featManagement" knownFeats={knownFeats} catalogue={catalogue} onLearn={onLearn} onRemove={onRemove} />);

    expect(screen.getByRole('heading', { name: "Player's Handbook" })).toBeTruthy();
    expect(screen.getByRole('heading', { name: "Xanathar's Guide to Everything" })).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: /Learn Actor/ }));
    fireEvent.click(screen.getByRole('button', { name: 'Remove Alert' }));

    expect(onLearn).toHaveBeenCalledWith('f2');
    expect(onRemove).toHaveBeenCalledWith('alert');
  });
});
