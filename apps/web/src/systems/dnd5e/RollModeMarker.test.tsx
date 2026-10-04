import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { RollModeMarker } from './RollModeMarker';
import { SkillRow } from './SkillRow';

const noop = () => {};

describe('RollModeMarker', () => {
  it('renders nothing without a forced mode', () => {
    const { container } = render(<RollModeMarker rollMode={undefined} className="marker" />);

    expect(container).toBeEmptyDOMElement();
  });

  it('renders nothing when opposing sources cancel', () => {
    const { container } = render(
      <RollModeMarker rollMode={{ mode: 'NORMAL', advantageSources: ['Invisible'], disadvantageSources: ['Poisoned'] }} className="marker" />,
    );

    expect(container).toBeEmptyDOMElement();
  });

  it('names the disadvantage sources', () => {
    render(<RollModeMarker rollMode={{ mode: 'DISADVANTAGE', advantageSources: [], disadvantageSources: ['Poisoned', 'Exhaustion 1'] }} className="marker" />);

    expect(screen.getByLabelText('Disadvantage: Poisoned, Exhaustion 1')).toHaveAttribute('title', 'Disadvantage: Poisoned, Exhaustion 1');
  });

  it('draws the advantage icon', () => {
    const { container } = render(<RollModeMarker rollMode={{ mode: 'ADVANTAGE', advantageSources: ['Invisible'], disadvantageSources: [] }} className="marker" />);

    expect(screen.getByLabelText('Advantage: Invisible')).toBeInTheDocument();
    expect(container.querySelector('.marker svg')).toBeInTheDocument();
  });
});

describe('SkillRow roll mode column', () => {
  const skillRow = (rollMode?: Parameters<typeof SkillRow>[0]['rollMode']) => (
    <SkillRow
      name="Stealth"
      abilityAbbreviation="DEX"
      proficiency="NONE"
      modifier={2}
      rollMode={rollMode}
      onRoll={noop}
      onRollContextMenu={noop}
      onOpenExplainer={noop}
    />
  );

  it('adds the adjustments column when a mode is forced', () => {
    const { container } = render(skillRow({ mode: 'DISADVANTAGE', advantageSources: [], disadvantageSources: ['Poisoned'] }));

    expect(container.querySelector('.skill-row__adjustments')).toBeInTheDocument();
  });

  it('leaves the column out otherwise', () => {
    const { container } = render(skillRow());

    expect(container.querySelector('.skill-row__adjustments')).toBeNull();
  });
});
