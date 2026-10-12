import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CheckListSelect } from './CheckListSelect';

function renderSelect(onToggle = vi.fn()) {
  render(
    <CheckListSelect
      ariaLabel="Books"
      summary={<span>2 books</span>}
      groups={[
        { label: 'Always on', options: [{ key: 'phb', label: "Player's Handbook", checked: true, locked: true }] },
        { label: 'Other books', options: [{ key: 'xge', label: "Xanathar's Guide", note: '5 entries', checked: false }] },
      ]}
      onToggle={onToggle}
    />,
  );
  return onToggle;
}

describe('CheckListSelect', () => {
  it('opens a grouped list from its trigger, showing each option checked or not', () => {
    renderSelect();
    expect(screen.queryByRole('listbox')).toBeNull();

    fireEvent.click(screen.getByRole('button', { name: 'Books' }));

    expect(screen.getByText('Always on')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /^Player's Handbook/ })).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('option', { name: /^Xanathar's Guide/ })).toHaveAttribute('aria-selected', 'false');
  });

  it('toggles an option but never a locked one, and stays open for more picks', () => {
    const onToggle = renderSelect();
    fireEvent.click(screen.getByRole('button', { name: 'Books' }));

    fireEvent.click(screen.getByRole('option', { name: /^Player's Handbook/ }));
    fireEvent.click(screen.getByRole('option', { name: /^Xanathar's Guide/ }));

    expect(onToggle).toHaveBeenCalledTimes(1);
    expect(onToggle).toHaveBeenCalledWith('xge');
    expect(screen.getByRole('listbox')).toBeInTheDocument();
  });

  it('closes on Escape and on a click outside', () => {
    renderSelect();
    fireEvent.click(screen.getByRole('button', { name: 'Books' }));

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('listbox')).toBeNull();

    fireEvent.click(screen.getByRole('button', { name: 'Books' }));
    fireEvent.mouseDown(document.body);
    expect(screen.queryByRole('listbox')).toBeNull();
  });
});
