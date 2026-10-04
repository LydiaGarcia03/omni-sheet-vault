import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { TextFieldPanel } from './TextFieldPanel';

describe('TextFieldPanel', () => {
  it('saves a picked suggestion at once, appended to the text', () => {
    const onSave = vi.fn();
    render(
      <TextFieldPanel
        kind="textField"
        title="Personality Traits"
        fields={[{ label: 'Personality Traits', value: 'Brave.', multiline: true, onSave, suggestions: ['Calm.', 'Loud.'] }]}
      />,
    );

    expect(screen.getByRole('columnheader', { name: 'd2' })).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: 'Add suggestion 2' }));

    expect(onSave).toHaveBeenCalledWith('Brave.\nLoud.');
  });

  it('draws several fields as labeled inputs, with selects for fixed choices', () => {
    const onAlignment = vi.fn();
    const onFaith = vi.fn();
    render(
      <TextFieldPanel
        kind="textField"
        title="Characteristics and Details"
        fields={[
          { label: 'Alignment', value: '', options: ['Lawful Good', 'Neutral'], onSave: onAlignment },
          { label: 'Faith', value: '', onSave: onFaith },
        ]}
      />,
    );

    fireEvent.change(screen.getByLabelText('Alignment'), { target: { value: 'Neutral' } });
    fireEvent.change(screen.getByLabelText('Faith'), { target: { value: 'Tymora' } });
    fireEvent.blur(screen.getByLabelText('Faith'));

    expect(onAlignment).toHaveBeenCalledWith('Neutral');
    expect(onFaith).toHaveBeenCalledWith('Tymora');
  });
});
