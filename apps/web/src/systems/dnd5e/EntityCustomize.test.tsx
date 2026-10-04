import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { EntityDetailPanel } from '../../sheet/EntityDetailPanel';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';

const FIELDS: CustomizeField[] = [
  { key: 'toHitBonus', label: 'To Hit Bonus', kind: 'number' },
  { key: 'weightOverride', label: 'Weight Override (kg)', kind: 'number', decimal: true, scale: 0.5 },
  { key: 'silvered', label: 'Silvered', kind: 'checkbox' },
  { key: 'name', label: 'Name', kind: 'text', name: true },
];

const EMPTY = { toHitBonus: null, weightOverride: null, silvered: false, name: null };

afterEach(() => vi.useRealTimers());

describe('EntityCustomizeEditor', () => {
  it('saves every value when one field changes, converting a scaled number back', () => {
    const onSave = vi.fn();
    render(<EntityCustomizeEditor fields={FIELDS} values={EMPTY} customized={false} onSave={onSave} onReset={vi.fn()} />);

    fireEvent.change(screen.getByLabelText('To Hit Bonus'), { target: { value: '2' } });
    fireEvent.blur(screen.getByLabelText('To Hit Bonus'));
    fireEvent.change(screen.getByLabelText('Weight Override (kg)'), { target: { value: '1.5' } });
    fireEvent.blur(screen.getByLabelText('Weight Override (kg)'));
    fireEvent.click(screen.getByLabelText('Silvered'));

    expect(onSave).toHaveBeenCalledWith({ ...EMPTY, toHitBonus: 2 });
    expect(onSave).toHaveBeenCalledWith({ ...EMPTY, weightOverride: 3 });
    expect(onSave).toHaveBeenCalledWith({ ...EMPTY, silvered: true });
    expect(screen.getByRole('button', { name: 'No Customizations' })).toBeDisabled();
  });

  it('asks to confirm the reset for three seconds before resetting', () => {
    vi.useFakeTimers();
    const onReset = vi.fn();
    render(<EntityCustomizeEditor fields={FIELDS} values={{ ...EMPTY, toHitBonus: 1 }} customized onSave={vi.fn()} onReset={onReset} />);

    fireEvent.click(screen.getByRole('button', { name: 'Remove Customizations' }));
    expect(screen.getByRole('button', { name: 'Confirm (3)' })).toBeInTheDocument();
    for (let second = 0; second < 3; second++) {
      act(() => vi.advanceTimersByTime(1000));
    }
    expect(screen.getByRole('button', { name: 'Remove Customizations' })).toBeInTheDocument();
    expect(onReset).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: 'Remove Customizations' }));
    fireEvent.click(screen.getByRole('button', { name: 'Confirm (3)' }));
    expect(onReset).toHaveBeenCalledOnce();
  });
});

describe('EntityDetailPanel customize', () => {
  it('marks a customized entity with an asterisk and opens Customize on the name field from the pencil', () => {
    render(
      <EntityDetailPanel
        kind="entityDetail"
        name="Longsword"
        metadata={[]}
        description=""
        customize={{
          customized: true,
          editor: <EntityCustomizeEditor fields={FIELDS} values={EMPTY} customized onSave={vi.fn()} onReset={vi.fn()} />,
        }}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Longsword*' })).toBeInTheDocument();
    expect(screen.getByText('Customize*')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Rename Longsword' }));

    expect(screen.getByLabelText('Name')).toHaveFocus();
  });
});
