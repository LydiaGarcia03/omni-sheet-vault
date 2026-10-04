import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { Customize, SidebarHeader, ValueEditorRow } from './sidebarParts';

describe('SidebarHeader', () => {
  it('shows the parent as a way back, and the signed modifier after the title', () => {
    const onOpenParent = vi.fn();
    render(<SidebarHeader parent="Saving Throws" onOpenParent={onOpenParent} title="Strength Saving Throw" modifier={-1} />);

    fireEvent.click(screen.getByRole('button', { name: 'Saving Throws' }));
    expect(onOpenParent).toHaveBeenCalled();
    expect(screen.getByRole('heading')).toHaveTextContent('Strength Saving Throw−1');
  });
});

describe('ValueEditorRow', () => {
  it('saves the value and the notes when a field loses focus, an empty value as none', () => {
    const onCommit = vi.fn();
    render(
      <Customize>
        <ValueEditorRow label="Override AC" value={15} notes="" onCommit={onCommit} />
      </Customize>,
    );

    fireEvent.change(screen.getByLabelText('Override AC'), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText('Override AC source notes'), { target: { value: 'Mage Armor' } });
    fireEvent.blur(screen.getByLabelText('Override AC source notes'));

    expect(onCommit).toHaveBeenCalledWith(null, 'Mage Armor');
  });

  it('saves nothing when nothing changed', () => {
    const onCommit = vi.fn();
    render(<ValueEditorRow label="Magic Bonus" value={null} notes="" onCommit={onCommit} />);

    fireEvent.blur(screen.getByLabelText('Magic Bonus'));

    expect(onCommit).not.toHaveBeenCalled();
  });
});
