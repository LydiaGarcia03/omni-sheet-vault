import { useEffect, useState, type ReactNode } from 'react';
import type { AbilityBreakdown, Contribution } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { SidebarHeader } from '../../sheet/sidebarParts';

function Signed({ value }: { value: number }) {
  return (
    <>
      <span className="ability-pane__sign">{value < 0 ? '−' : '+'}</span>
      {Math.abs(value)}
    </>
  );
}

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="ability-pane__row" role="row">
      <div className="ability-pane__label" role="rowheader">
        {label}
      </div>
      <div className="ability-pane__value" role="cell">
        {children}
      </div>
    </div>
  );
}

function SupplierRow({ source }: { source: Contribution }) {
  return (
    <div className="ability-pane__row ability-pane__row--supplier" role="row">
      <div className="ability-pane__label" role="rowheader">
        {source.source}
      </div>
      <div className="ability-pane__value" role="cell">
        (<Signed value={source.amount} />)
      </div>
    </div>
  );
}

function parseOrNull(text: string): number | null {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  return Number.isInteger(parsed) ? parsed : null;
}

type OverrideFieldProps = { label: string; value: number | null; onCommit: (value: number | null) => void };

/** One of the pane's always-visible fields, saved when it loses focus. */
function OverrideField({ label, value, onCommit }: OverrideFieldProps) {
  const [text, setText] = useState(value === null ? '' : String(value));
  useEffect(() => setText(value === null ? '' : String(value)), [value]);

  return (
    <label className="ability-pane__override">
      <span className="ability-pane__override-label">{label}</span>
      <input
        className="ability-pane__override-input"
        type="number"
        placeholder="--"
        value={text}
        onChange={(event) => setText(event.target.value)}
        onBlur={() => {
          const next = parseOrNull(text);
          if (next !== value) {
            onCommit(next);
          }
        }}
      />
    </label>
  );
}

type Dnd5eAbilityPaneProps = {
  label: string;
  icon: ReactNode;
  breakdown: AbilityBreakdown;
  onCustomize: (value: { otherModifier: number | null; overrideScore: number | null }) => void;
  rulesText: string;
};

/**
 * D&D Beyond's ability pane: the score's table (Total, Modifier, Base, Bonus with where it comes from, Set Score,
 * Stacking Bonus), then the player's Other Modifier and Override Score, then the rules text.
 */
export function Dnd5eAbilityPane({ label, icon, breakdown, onCustomize, rulesText }: Dnd5eAbilityPaneProps) {
  return (
    <div className="ability-pane">
      <SidebarHeader icon={icon} title={`${label} ${breakdown.total}`} modifier={breakdown.modifier} modifierFormat="parens" />
      <div className="ability-pane__table" role="table" aria-label={`${label} breakdown`}>
        <Row label="Total Score">{breakdown.total}</Row>
        <Row label="Modifier">
          <Signed value={breakdown.modifier} />
        </Row>
        <Row label="Base Score">{breakdown.base}</Row>
        <Row label="Bonus">
          <Signed value={breakdown.bonus} />
        </Row>
        {breakdown.bonusSources.map((source, index) => (
          <SupplierRow key={index} source={source} />
        ))}
        <Row label="Set Score">{breakdown.setScore}</Row>
        <Row label="Stacking Bonus">
          <Signed value={breakdown.stackingBonus} />
        </Row>
      </div>
      <div className="ability-pane__overrides">
        <OverrideField
          label="Other Modifier"
          value={breakdown.otherModifier}
          onCommit={(otherModifier) => onCustomize({ otherModifier, overrideScore: breakdown.overrideScore })}
        />
        <OverrideField
          label="Override Score"
          value={breakdown.overrideScore}
          onCommit={(overrideScore) => onCustomize({ otherModifier: breakdown.otherModifier, overrideScore })}
        />
      </div>
      <RulesText text={rulesText} />
    </div>
  );
}
