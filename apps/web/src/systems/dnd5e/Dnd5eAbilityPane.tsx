import { useEffect, useState, type ReactNode } from 'react';
import type { AbilityBreakdown, Contribution } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { SidebarHeader } from '../../sheet/sidebarParts';
import { useDnd5eText } from './locales/dnd5eMessages';

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
  const t = useDnd5eText();
  return (
    <div className="ability-pane">
      <SidebarHeader icon={icon} title={`${label} ${breakdown.total}`} modifier={breakdown.modifier} modifierFormat="parens" />
      <div className="ability-pane__table" role="table" aria-label={t('abilityPane.breakdown', { label })}>
        <Row label={t('abilityPane.totalScore')}>{breakdown.total}</Row>
        <Row label={t('abilityPane.modifier')}>
          <Signed value={breakdown.modifier} />
        </Row>
        <Row label={t('abilityPane.baseScore')}>{breakdown.base}</Row>
        <Row label={t('abilityPane.bonus')}>
          <Signed value={breakdown.bonus} />
        </Row>
        {breakdown.bonusSources.map((source, index) => (
          <SupplierRow key={index} source={source} />
        ))}
        <Row label={t('abilityPane.setScore')}>{breakdown.setScore}</Row>
        <Row label={t('abilityPane.stackingBonus')}>
          <Signed value={breakdown.stackingBonus} />
        </Row>
      </div>
      <div className="ability-pane__overrides">
        <OverrideField
          label={t('abilityPane.otherModifier')}
          value={breakdown.otherModifier}
          onCommit={(otherModifier) => onCustomize({ otherModifier, overrideScore: breakdown.overrideScore })}
        />
        <OverrideField
          label={t('abilityPane.overrideScore')}
          value={breakdown.overrideScore}
          onCommit={(overrideScore) => onCustomize({ otherModifier: breakdown.otherModifier, overrideScore })}
        />
      </div>
      <RulesText text={rulesText} />
    </div>
  );
}
