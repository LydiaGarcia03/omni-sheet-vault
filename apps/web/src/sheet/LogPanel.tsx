import { useEffect, useRef, useState } from 'react';
import type { RollResult } from '../dice/api';
import { useSheetText, type SheetText } from '../locales/platformMessages';
import d4IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d4.svg?raw';
import d6IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d6.svg?raw';
import d8IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d8.svg?raw';
import d10IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d10.svg?raw';
import d12IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d12.svg?raw';
import d20IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d20.svg?raw';
import d100IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d100.svg?raw';
import { FrameIcon } from '../systems/dnd5e/FrameIcon';
import type { LogRequest } from './api';
import { SidebarHeader } from './sidebarParts';

const DIE_ICONS: Record<string, string> = {
  '4': d4IconSvg,
  '6': d6IconSvg,
  '8': d8IconSvg,
  '10': d10IconSvg,
  '12': d12IconSvg,
  '20': d20IconSvg,
  '100': d100IconSvg,
};

const ROLL_TYPES: Record<string, { label: 'log.check' | 'log.save' | 'log.toHit' | 'log.damage' | 'log.heal'; tone: string }> = {
  check: { label: 'log.check', tone: 'check' },
  'saving throw': { label: 'log.save', tone: 'save' },
  'attack roll': { label: 'log.toHit', tone: 'to-hit' },
  damage: { label: 'log.damage', tone: 'damage' },
  heal: { label: 'log.heal', tone: 'heal' },
  healing: { label: 'log.heal', tone: 'heal' },
};

const ROLL_MODES: Record<string, 'log.advantage' | 'log.disadvantage'> = {
  '(advantage)': 'log.advantage',
  '(disadvantage)': 'log.disadvantage',
};

/** Splits "Longsword: attack roll (advantage)" into D&D Beyond's action and roll type. */
export function describeRoll(context: string, t: SheetText = useSheetText.english): { action: string; type: string | null; tone: string } {
  const separator = context.indexOf(': ');
  if (separator < 0) {
    return { action: context, type: null, tone: 'roll' };
  }
  const action = context.slice(0, separator);
  const rawType = context.slice(separator + 2);
  const baseType = rawType.replace(/\s*\(.*\)$/, '');
  const known = ROLL_TYPES[baseType.toLowerCase()];
  const rawMode = rawType.slice(baseType.length).trim();
  const mode = ROLL_MODES[rawMode] ? `(${t(ROLL_MODES[rawMode])})` : rawMode;
  return { action, type: `${known ? t(known.label) : baseType}${mode ? ` ${mode}` : ''}`, tone: known?.tone ?? 'roll' };
}

/** D&D Beyond's relative stamp: "now", "< 1 min ago", "3 mins ago", then hours and days. */
export function timeAgo(rolledAt: string, now: number = Date.now(), t: SheetText = useSheetText.english): string {
  const seconds = Math.max(0, Math.floor((now - new Date(rolledAt).getTime()) / 1000));
  if (seconds < 10) return t('log.now');
  if (seconds < 60) return t('log.underAMinute');
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return t('log.minutesAgo', { count: minutes });
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return t('log.hoursAgo', { count: hours });
  return t('log.daysAgo', { count: Math.floor(hours / 24) });
}

function breakdown(roll: RollResult): string {
  const diceTotal = roll.results.reduce((sum, value) => sum + value, 0);
  const modifier = roll.total - diceTotal;
  const parts = roll.results.map(String);
  if (modifier !== 0) {
    parts.push(String(Math.abs(modifier)));
  }
  return parts.join(' + ').replace(/ \+ (\d+)$/, modifier < 0 ? ' − $1' : ' + $1');
}

function dieIcon(expression: string) {
  const sides = /d(\d+)/i.exec(expression)?.[1];
  const svg = sides ? DIE_ICONS[sides] : undefined;
  return svg ? <FrameIcon svg={svg} className="game-log-entry__die" aria-hidden /> : null;
}

type EntryProps = { roll: RollResult; sender: string; expanded: boolean; onToggle: () => void };

function LogEntry({ roll, sender, expanded, onToggle }: EntryProps) {
  const t = useSheetText();
  const { action, type, tone } = describeRoll(roll.context, t);
  return (
    <li className="game-log-entry">
      <div className="game-log-entry__sender">{sender}</div>
      <button
        type="button"
        className={expanded ? 'game-log-entry__bubble is-expanded' : 'game-log-entry__bubble'}
        aria-expanded={expanded}
        onClick={onToggle}
      >
        <span className="game-log-entry__result">
          <span className="game-log-entry__title">
            <span className="game-log-entry__action">{action}</span>
            {type && (
              <>
                : <span className={`game-log-entry__type game-log-entry__type--${tone}`}>{type}</span>
              </>
            )}
          </span>
          {expanded && (
            <>
              <span className="game-log-entry__breakdown">
                {dieIcon(roll.expression)}
                {breakdown(roll)}
              </span>
              <span className="game-log-entry__notation">{roll.expression}</span>
            </>
          )}
        </span>
        <span className="game-log-entry__total-container">
          {expanded && <span className="game-log-entry__equals">=</span>}
          <span className="game-log-entry__total">{roll.total}</span>
        </span>
      </button>
      <div className="game-log-entry__time">{timeAgo(roll.rolledAt, Date.now(), t)}</div>
    </li>
  );
}

/**
 * The Log mold (ui-design-system.md) as D&D Beyond's game log: chat bubbles with the newest at the bottom, the
 * newest expanded (dice, notation, total) and the rest collapsed until clicked. Opens scrolled to the newest.
 */
export function LogPanel({ entries, senderName }: LogRequest) {
  const t = useSheetText();
  const [toggled, setToggled] = useState<Record<string, boolean>>({});
  const paneRef = useRef<HTMLDivElement>(null);
  const newestId = entries[0]?.id;

  useEffect(() => {
    const scroller = paneRef.current?.parentElement;
    if (scroller) {
      scroller.scrollTop = scroller.scrollHeight;
    }
  }, [newestId]);

  return (
    <div ref={paneRef} className="log-pane">
      <SidebarHeader title={t('log.title')} />
      {entries.length === 0 ? (
        <div className="log-pane__empty">
          <p className="log-pane__empty-title">{t('log.emptyTitle')}</p>
          <p className="log-pane__empty-text">{t('log.emptyText')}</p>
        </div>
      ) : (
        <ul className="log-pane__entries">
          {entries.map((roll) => {
            const expanded = toggled[roll.id] ?? roll.id === newestId;
            return (
              <LogEntry
                key={roll.id}
                roll={roll}
                sender={senderName ?? t('log.you')}
                expanded={expanded}
                onToggle={() => setToggled({ ...toggled, [roll.id]: !expanded })}
              />
            );
          })}
        </ul>
      )}
    </div>
  );
}
