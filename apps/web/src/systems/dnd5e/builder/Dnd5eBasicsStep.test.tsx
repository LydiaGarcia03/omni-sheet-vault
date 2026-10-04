import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { preferencesOf, type Dnd5eBuild } from './dnd5eBuild';
import { Dnd5eBasicsStep } from './Dnd5eBasicsStep';

vi.mock('../../../builder/PortraitPicker', () => ({ PortraitPicker: () => null }));
vi.mock('../../../catalogue/api', () => ({
  getCatalogueSources: () =>
    Promise.resolve([
      { sourceBook: "Player's Handbook", sourceCode: 'PHB', entryCount: 10, playtest: false, partner: null },
      { sourceBook: "Xanathar's Guide to Everything", sourceCode: 'XGE', entryCount: 5, playtest: false, partner: null },
      { sourceBook: 'UA: The Mystic Class', sourceCode: 'UATheMysticClass', entryCount: 7, playtest: true, partner: null },
      { sourceBook: "Explorer's Guide to Wildemount", sourceCode: 'EGW', entryCount: 114, playtest: false, partner: 'Critical Role' },
      { sourceBook: 'Critical Role: Call of the Netherdeep', sourceCode: 'CRCotN', entryCount: 14, playtest: false, partner: 'Critical Role' },
      { sourceBook: 'The Lost Dungeon of Rickedness: Big Rick Energy', sourceCode: 'RMBRE', entryCount: 1, playtest: false, partner: 'Rick and Morty' },
    ]),
  getCataloguePartners: () =>
    Promise.resolve([
      { name: 'Critical Role', sourceBooks: ['Critical Role: Call of the Netherdeep', "Explorer's Guide to Wildemount"] },
      { name: 'Rick and Morty', sourceBooks: ['The Lost Dungeon of Rickedness: Big Rick Energy'] },
    ]),
}));

const BUILD: Dnd5eBuild = {
  speciesSlug: null,
  subspeciesName: null,
  speciesVariantName: null,
  backgroundSlug: null,
  classes: [],
  abilityScoreMethod: null,
  baseAbilityScores: {},
  hitPointMethod: 'FIXED',
  rolledHitPoints: [],
  choices: [],
};

function renderBasics(onChange = vi.fn(), build: Dnd5eBuild = BUILD) {
  render(
    <Dnd5eBasicsStep
      characterId="c1"
      systemId="dnd-5e"
      name="Mez"
      onNameChange={() => {}}
      portrait={null}
      onPortraitChange={() => {}}
      build={build}
      onChange={onChange}
    />,
  );
  return onChange;
}

describe('Dnd5eBasicsStep', () => {
  it('lists playtest sources under their own switch, off by default, not among the books', async () => {
    renderBasics();

    expect(await screen.findByText(/UA: The Mystic Class\./)).toBeInTheDocument();
    expect(screen.getByRole('switch', { name: 'Playtest content' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.getByText('Every other book (1)')).toBeInTheDocument();
    expect(screen.queryByRole('checkbox', { name: /UA: The Mystic Class/ })).toBeNull();
  });

  it('turns playtest content on through the switch', async () => {
    const onChange = renderBasics();
    await screen.findByText(/UA: The Mystic Class\./);

    fireEvent.click(screen.getByRole('switch', { name: 'Playtest content' }));

    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ preferences: expect.objectContaining({ playtestContent: true }) }));
  });

  it('lists the partners with imported books in the partner dropdown, every one chosen by default', async () => {
    renderBasics();

    expect(await screen.findByRole('checkbox', { name: /Rick and Morty/ })).toBeChecked();
    expect(screen.getByRole('switch', { name: 'Enable partnered content' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByText('Every partner (2)')).toBeInTheDocument();
    expect(screen.getByRole('checkbox', { name: /Critical Role/ })).toBeChecked();
    expect(screen.queryByRole('checkbox', { name: /^Explorer's Guide to Wildemount/ })).toBeNull();
  });

  it('unchecks one partner, keeping the others', async () => {
    const onChange = renderBasics();

    fireEvent.click(await screen.findByRole('checkbox', { name: /Rick and Morty/ }));

    expect(onChange).toHaveBeenCalledWith(
      expect.objectContaining({ preferences: expect.objectContaining({ partners: ['Critical Role'] }) }),
    );
  });

  it('hides the partner dropdown while partnered content is off', async () => {
    const off = { ...BUILD, preferences: { ...preferencesOf(BUILD), partneredContent: false } };
    renderBasics(vi.fn(), off);

    expect(await screen.findByRole('switch', { name: 'Enable partnered content' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.queryByRole('checkbox', { name: /Critical Role/ })).toBeNull();
  });
});
