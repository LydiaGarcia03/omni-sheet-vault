import { fireEvent, render, renderHook, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ChangeAppearancePanel } from './ChangeAppearancePanel';
import { useSheetTheme } from './useSheetTheme';

const THEMES = [
  { id: 'ddb-red', label: 'DDB Red', color: '#C53131' },
  { id: 'wizard-cobalt', label: 'Wizard Cobalt', color: '#0045B7' },
];

describe('ChangeAppearancePanel', () => {
  it('shows the current theme, and applies one picked under Browse Decorations', () => {
    const onSelect = vi.fn();
    render(
      <ChangeAppearancePanel kind="changeAppearance" themes={THEMES} current="ddb-red" onSelect={onSelect} renderSample={(theme) => <span>{theme.color}</span>} />,
    );

    expect(screen.getByText('DDB Red')).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: 'Themes' }));
    expect(screen.getByRole('button', { name: /DDB Red/ })).toHaveAttribute('aria-pressed', 'true');
    fireEvent.click(screen.getByRole('button', { name: /Wizard Cobalt/ }));

    expect(onSelect).toHaveBeenCalledWith('wizard-cobalt');
  });
});

describe('useSheetTheme', () => {
  it('draws the page in the theme while mounted, and clears it after', () => {
    const { unmount } = renderHook(() => useSheetTheme('wizard-cobalt'));

    expect(document.documentElement.dataset.sheetTheme).toBe('wizard-cobalt');
    unmount();
    expect(document.documentElement.dataset.sheetTheme).toBeUndefined();
  });
});
