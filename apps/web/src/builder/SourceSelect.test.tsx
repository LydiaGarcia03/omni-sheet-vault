import { fireEvent, render, screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SourceSelect, type SourceSelectOption } from './SourceSelect';

const PHB = "Player's Handbook";
const VGM = "Volo's Guide to Monsters";
const DMG = "Dungeon Master's Guide";

vi.mock('../catalogue/SourceCodes', () => ({
  useSourceCode: () => (book: string) => ({ [PHB]: 'PHB', [VGM]: 'VGM', [DMG]: 'DMG' })[book] ?? null,
  useLeadingSources: () => [PHB],
}));

const species: SourceSelectOption[] = [
  { key: 'dwarf', label: 'Dwarf', sourceBook: PHB },
  { key: 'elf', label: 'Elf', sourceBook: PHB },
  { key: 'tiefling', label: 'Tiefling', sourceBook: PHB },
  { key: 'aasimar-dmg', label: 'Aasimar', sourceBook: DMG },
  { key: 'aasimar-vgm', label: 'Aasimar', sourceBook: VGM },
  { key: 'firbolg', label: 'Firbolg', sourceBook: VGM },
  { key: 'goliath', label: 'Goliath', sourceBook: VGM },
  { key: 'tabaxi', label: 'Tabaxi', sourceBook: VGM },
  { key: 'custom', label: 'Custom', sourceBook: null },
];

function renderSelect(value: string | null = null, onChange = vi.fn()) {
  render(<SourceSelect options={species} value={value} onChange={onChange} placeholder="— choose —" ariaLabel="Species" clearable />);
  return onChange;
}

describe('SourceSelect', () => {
  it('shows the selected entry with its book code', () => {
    renderSelect('firbolg');

    const trigger = screen.getByRole('combobox', { name: 'Species' });
    expect(trigger).toHaveTextContent('Firbolg');
    expect(trigger).toHaveTextContent('VGM');
  });

  it('groups options by book: leading books first, then largest, with unsourced entries last', () => {
    renderSelect();
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    const groups = screen.getByRole('listbox').querySelectorAll('.source-select__group');
    expect([...groups].map((group) => group.textContent)).toEqual([PHB, VGM, DMG, 'Other']);
  });

  it('tells same-named entries apart by their source', () => {
    const onChange = renderSelect();
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    fireEvent.click(screen.getByRole('option', { name: `Aasimar (${DMG})` }));

    expect(onChange).toHaveBeenCalledWith('aasimar-dmg');
  });

  it('shows the full book name when hovering its code', () => {
    renderSelect();
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    fireEvent.mouseEnter(within(screen.getByRole('option', { name: `Dwarf (${PHB})` })).getByText('PHB'));

    expect(screen.getByRole('tooltip')).toHaveTextContent(PHB);
  });

  it('filters by name or book code and picks with the keyboard', () => {
    const onChange = renderSelect();
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));
    const filter = screen.getByRole('textbox', { name: 'Filter Species' });

    fireEvent.change(filter, { target: { value: 'dmg' } });
    expect(screen.getAllByRole('option').map((option) => option.getAttribute('aria-label'))).toEqual([`Aasimar (${DMG})`]);

    fireEvent.change(filter, { target: { value: 'ti' } });
    fireEvent.keyDown(filter, { key: 'ArrowDown' });
    fireEvent.keyDown(filter, { key: 'Enter' });

    expect(onChange).toHaveBeenCalledWith('tiefling');
  });

  it('clears the selection', () => {
    const onChange = renderSelect('elf');
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    fireEvent.click(screen.getByRole('option', { name: 'Clear selection' }));

    expect(onChange).toHaveBeenCalledWith(null);
  });

  it('picks several up to the maximum, staying open, and shows the count', () => {
    const onChange = vi.fn();
    const { rerender } = render(
      <SourceSelect multiple options={species} values={['elf']} max={2} onChange={onChange} placeholder="— choose 2 —" ariaLabel="Species" />,
    );
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    fireEvent.click(screen.getByRole('option', { name: `Tabaxi (${VGM})` }));
    expect(onChange).toHaveBeenLastCalledWith(['elf', 'tabaxi']);

    rerender(<SourceSelect multiple options={species} values={['elf', 'tabaxi']} max={2} onChange={onChange} placeholder="— choose 2 —" ariaLabel="Species" />);
    expect(screen.getByRole('listbox')).toHaveAttribute('aria-multiselectable', 'true');
    expect(screen.getByRole('combobox', { name: 'Species' })).toHaveTextContent('Elf, Tabaxi');
    expect(screen.getByRole('combobox', { name: 'Species' })).toHaveTextContent('2/2');

    fireEvent.click(screen.getByRole('option', { name: `Dwarf (${PHB})` }));
    expect(onChange).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('option', { name: `Dwarf (${PHB})` })).toHaveAttribute('aria-disabled', 'true');

    fireEvent.click(screen.getByRole('option', { name: `Elf (${PHB})` }));
    expect(onChange).toHaveBeenLastCalledWith(['tabaxi']);
  });

  it('keeps the rows in place while picking several, clearing from the status line', () => {
    const onChange = vi.fn();
    render(<SourceSelect multiple options={species} values={['elf']} max={3} onChange={onChange} placeholder="— choose 3 —" ariaLabel="Species" />);
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    expect(screen.queryByRole('option', { name: 'Clear selection' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Clear' }));

    expect(onChange).toHaveBeenCalledWith([]);
  });

  it('shows no book headings when no option has a book', () => {
    render(
      <SourceSelect
        multiple
        options={[
          { key: 'athletics', label: 'Athletics', sourceBook: null },
          { key: 'history', label: 'History', sourceBook: null },
        ]}
        values={[]}
        max={2}
        onChange={vi.fn()}
        placeholder="— choose 2 —"
        ariaLabel="Skills"
      />,
    );
    fireEvent.click(screen.getByRole('combobox', { name: 'Skills' }));

    expect(screen.getByRole('listbox').querySelector('.source-select__group')).toBeNull();
    expect(screen.getAllByRole('option')).toHaveLength(2);
  });

  it('closes on Escape without choosing', () => {
    const onChange = renderSelect();
    const trigger = screen.getByRole('combobox', { name: 'Species' });
    fireEvent.click(trigger);

    fireEvent.keyDown(screen.getByRole('textbox', { name: 'Filter Species' }), { key: 'Escape' });

    expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
    expect(trigger).toHaveAttribute('aria-expanded', 'false');
    expect(onChange).not.toHaveBeenCalled();
  });

  it('shows what each option gives under its name, and finds options by it', () => {
    const withSummaries = species.map((option) =>
      option.key === 'dwarf' ? { ...option, summary: 'CON +2 · Darkvision 60 ft' } : option.key === 'tabaxi' ? { ...option, summary: 'DEX +2 · Darkvision 60 ft' } : option,
    );
    render(<SourceSelect options={withSummaries} value={null} onChange={vi.fn()} placeholder="— choose —" ariaLabel="Species" />);
    fireEvent.click(screen.getByRole('combobox', { name: 'Species' }));

    expect(screen.getByText('CON +2 · Darkvision 60 ft')).toBeInTheDocument();
    fireEvent.change(screen.getByRole('textbox', { name: 'Filter Species' }), { target: { value: 'darkvision' } });

    expect(screen.getAllByRole('option').map((option) => option.getAttribute('aria-label'))).toEqual([
      "Dwarf (Player's Handbook)",
      "Tabaxi (Volo's Guide to Monsters)",
    ]);
  });
});
