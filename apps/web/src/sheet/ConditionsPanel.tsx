import { useState, type ReactNode } from 'react';
import type { ActiveEffect, ConditionsRequest } from './api';
import { RulesText } from './RulesText';
import { ChevronDown, SidebarHeader } from './sidebarParts';

const EXHAUSTION_LEVELS = [0, 1, 2, 3, 4, 5, 6];

/**
 * The Conditions pane, as D&D Beyond's `ct-condition-manage-pane`: one row per condition (icon, name, toggle and a
 * rules chevron), then Exhaustion in its own group with a "Level" readout and a -- / 1–6 level bar.
 */
export function ConditionsPanel({
  title,
  conditions,
  onToggle,
  exhaustionLevel,
  exhaustionIcon,
  exhaustionRulesText,
  onExhaustionChange,
  activeEffects,
  onEndEffect,
}: ConditionsRequest) {
  return (
    <div className="conditions-pane">
      <SidebarHeader title={title} />
      {activeEffects.length > 0 && (
        <section className="active-effects" aria-label="Active effects">
          <h3 className="active-effects__title">Active Effects</h3>
          {activeEffects.map((effect) => (
            <div key={effect.key} className="active-effect-row">
              <div className="active-effect-row__text">
                <span className="active-effect-row__name">{effect.name}</span>
                <span className="active-effect-row__detail">{effectDetail(effect)}</span>
              </div>
              <button type="button" className="mutate active-effect-row__end" aria-label={`End ${effect.name} ${effect.onSelf ? 'on you' : 'on an ally'}`} onClick={() => onEndEffect(effect.key)}>
                End
              </button>
            </div>
          ))}
        </section>
      )}
      <div className="conditions-pane__list">
        {conditions.map(({ key, label, active, icon, rulesText }) => (
          <ConditionItem key={key} name={label} rulesText={rulesText}>
            <ConditionToggleRow name={label} active={active} icon={icon} onToggle={() => onToggle(key)} />
          </ConditionItem>
        ))}
      </div>
      <div className="conditions-pane__list conditions-pane__list--special">
        <ConditionItem
          name="Exhaustion"
          rulesText={exhaustionRulesText}
          footer={<ExhaustionLevels level={exhaustionLevel} onChange={onExhaustionChange} />}
        >
          <div className="condition-row">
            {exhaustionIcon && <span className="condition-row__icon">{exhaustionIcon}</span>}
            <span className={exhaustionLevel > 0 ? 'condition-row__name condition-row__name--active' : 'condition-row__name'}>Exhaustion</span>
            <span className={exhaustionLevel > 0 ? 'condition-row__level condition-row__level--active' : 'condition-row__level'}>
              Level {exhaustionLevel > 0 ? exhaustionLevel : '--'}
            </span>
          </div>
        </ConditionItem>
      </div>
    </div>
  );
}

/** "On you · 8 hours · Concentration · Ends on a long rest" — only the parts the effect has. */
function effectDetail(effect: ActiveEffect): string {
  const parts: string[] = [effect.onSelf ? 'On you' : 'On an ally'];
  if (effect.durationText) {
    parts.push(effect.durationText);
  }
  if (effect.concentration) {
    parts.push('Concentration');
  }
  if (effect.endsOnRests.includes('SHORT_OR_LONG_REST')) {
    parts.push('Ends on a short or long rest');
  } else if (effect.endsOnRests.includes('LONG_REST')) {
    parts.push('Ends on a long rest');
  }
  return parts.join(' · ');
}

/** A condition's row with its rules text, which a chevron beside the row shows and hides; no chevron without text. */
function ConditionItem({ name, rulesText = '', footer, children }: { name: string; rulesText?: string; footer?: ReactNode; children: ReactNode }) {
  const [open, setOpen] = useState(false);
  const hasText = rulesText.trim() !== '';
  return (
    <div className={open ? 'condition-item is-open' : 'condition-item'}>
      <div className="condition-item__header">
        {children}
        <span className="condition-item__callout">
          {hasText && (
            <button
              type="button"
              className="condition-item__chevron"
              aria-expanded={open}
              aria-label={`${open ? 'Hide' : 'Show'} ${name} rules`}
              onClick={() => setOpen(!open)}
            >
              <ChevronDown />
            </button>
          )}
        </span>
      </div>
      {footer}
      {hasText && open && <RulesText text={rulesText} placement="plain" />}
    </div>
  );
}

function ConditionToggleRow({ name, active, icon, onToggle }: { name: string; active: boolean; icon?: ReactNode; onToggle: () => void }) {
  return (
    <div className="condition-row">
      {icon && <span className="condition-row__icon">{icon}</span>}
      <span className={active ? 'condition-row__name condition-row__name--active' : 'condition-row__name'}>{name}</span>
      <button
        type="button"
        className={active ? 'mutate condition-row__toggle condition-row__toggle--on' : 'mutate condition-row__toggle'}
        aria-pressed={active}
        aria-label={`${name}: ${active ? 'active' : 'inactive'}`}
        onClick={onToggle}
      />
    </div>
  );
}

/** D&D Beyond's exhaustion bar: "--" clears it, a level sets it; from level 1 the levels below it (and "--") are shaded. */
function ExhaustionLevels({ level, onChange }: { level: number; onChange: (level: number) => void }) {
  return (
    <div className="exhaustion-levels" role="group" aria-label="Exhaustion level">
      {EXHAUSTION_LEVELS.map((value) => {
        const active = level > 0 && value === level;
        const state = active ? ' exhaustion-levels__level--active' : level > 0 && value < level ? ' exhaustion-levels__level--implied' : '';
        return (
          <button
            key={value}
            type="button"
            className={`mutate exhaustion-levels__level${state}`}
            aria-pressed={active}
            aria-label={value === 0 ? 'No exhaustion' : `Exhaustion level ${value}`}
            onClick={() => onChange(value)}
          >
            {value === 0 ? '--' : value}
          </button>
        );
      })}
    </div>
  );
}
