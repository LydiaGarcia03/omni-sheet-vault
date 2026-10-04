import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { customActionDetailRequest } from '../systems/dnd5e/customActionDetail';
import type { CustomAction } from './api';
import { EntityDetailPanel } from './EntityDetailPanel';
import { blankCustomAction, ManageCustomActionsPanel } from './ManageCustomActionsPanel';

const tailSwipe: CustomAction = { ...blankCustomAction('GENERAL', []), key: 'a1', name: 'Tail Swipe' };

describe('ManageCustomActionsPanel', () => {
  it('creates a blank action as soon as a template is picked', () => {
    const onAdd = vi.fn();
    render(<ManageCustomActionsPanel kind="manageCustomActions" customActions={[tailSwipe]} onAdd={onAdd} onRemove={vi.fn()} onOpenAction={vi.fn()} />);

    fireEvent.change(screen.getByLabelText('Add a custom action'), { target: { value: 'WEAPON' } });

    expect(onAdd).toHaveBeenCalledWith(expect.objectContaining({ template: 'WEAPON', name: 'Custom Action 2', activationType: null }));
  });

  it('lists actions by template, opening or removing each', () => {
    const onRemove = vi.fn();
    const onOpenAction = vi.fn();
    render(<ManageCustomActionsPanel kind="manageCustomActions" customActions={[tailSwipe]} onAdd={vi.fn()} onRemove={onRemove} onOpenAction={onOpenAction} />);

    expect(screen.getByRole('heading', { name: 'General' })).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: 'Tail Swipe' }));
    fireEvent.click(screen.getByRole('button', { name: 'Remove Tail Swipe' }));

    expect(onOpenAction).toHaveBeenCalledWith(tailSwipe);
    expect(onRemove).toHaveBeenCalledWith('a1');
  });
});

describe('customActionDetailRequest', () => {
  it('saves an Edit change with every other field kept', () => {
    const onUpdate = vi.fn();
    render(<EntityDetailPanel {...customActionDetailRequest(tailSwipe, onUpdate)} />);

    fireEvent.click(screen.getByText('Edit'));
    fireEvent.change(screen.getByLabelText('Activation Type'), { target: { value: 'BONUS_ACTION' } });

    expect(onUpdate).toHaveBeenCalledWith('a1', expect.objectContaining({ name: 'Tail Swipe', activationType: 'BONUS_ACTION', template: 'GENERAL' }));
    expect(onUpdate.mock.calls[0][1]).not.toHaveProperty('key');
  });
});
