import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { SidebarContent } from './api';
import { Sidebar } from './Sidebar';

const LOG: SidebarContent = { kind: 'log', entries: [] };

function renderSidebar(overrides: Partial<Parameters<typeof Sidebar>[0]> = {}) {
  const props = { content: LOG, hidden: false, onHide: vi.fn(), onShow: vi.fn(), ...overrides };
  render(
    <>
      <p>Sheet</p>
      <Sidebar {...props} />
    </>,
  );
  return props;
}

describe('Sidebar', () => {
  beforeEach(() => localStorage.clear());

  it('hides from its control', () => {
    const props = renderSidebar();
    fireEvent.click(screen.getByRole('button', { name: 'Hide sidebar' }));
    expect(props.onHide).toHaveBeenCalled();
  });

  it('shows only the show control while hidden', () => {
    const props = renderSidebar({ hidden: true });
    fireEvent.click(screen.getByRole('button', { name: 'Show sidebar' }));
    expect(props.onShow).toHaveBeenCalled();
    expect(screen.queryByRole('button', { name: 'Unlocked' })).toBeNull();
  });

  it('hides on Escape and on a click outside while unlocked', () => {
    const props = renderSidebar();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.mouseDown(screen.getByText('Sheet'));
    expect(props.onHide).toHaveBeenCalledTimes(2);
  });

  it('stays open when locked, without a hide control, and remembers the lock', () => {
    const props = renderSidebar();
    fireEvent.click(screen.getByRole('button', { name: 'Unlocked' }));

    expect(screen.queryByRole('button', { name: 'Hide sidebar' })).toBeNull();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.mouseDown(screen.getByText('Sheet'));
    expect(props.onHide).not.toHaveBeenCalled();
    expect(localStorage.getItem('osv.sheet.sidebarLocked')).toBe('true');
  });

  it('invites a selection when no panel was opened yet', () => {
    renderSidebar({ content: null });

    expect(screen.getByText('Select elements on the character sheet to display more information about')).toBeInTheDocument();
  });
});
