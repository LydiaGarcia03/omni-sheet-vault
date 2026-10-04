import { useEffect, useState } from 'react';
import type { CharacterSheet, MutationHandler } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { Customize, EditorBox, SidebarHeader, ValueEditorRow } from '../../sheet/sidebarParts';
import { customizationsOf, type NotedValue } from './customizations';

export const PASSIVES: { key: string; label: string }[] = [
  { key: 'passivePerception', label: 'Passive Perception' },
  { key: 'passiveInvestigation', label: 'Passive Investigation' },
  { key: 'passiveInsight', label: 'Passive Insight' },
];

const SENSE_TYPES: { key: string; label: string }[] = [
  { key: 'BLINDSIGHT', label: 'Blindsight' },
  { key: 'DARKVISION', label: 'Darkvision' },
  { key: 'TREMORSENSE', label: 'Tremorsense' },
  { key: 'TRUESIGHT', label: 'Truesight' },
];

function senseTypeLabel(type: string): string {
  return type.charAt(0) + type.slice(1).toLowerCase();
}

function parseOrNull(text: string): number | null {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  return Number.isInteger(parsed) ? parsed : null;
}

/** One row of the distance table: the sense, its distance and its source notes, saved when a field loses focus. */
function SenseDistanceRow({ label, value, onCommit }: { label: string; value: NotedValue; onCommit: (next: NotedValue) => void }) {
  const [distance, setDistance] = useState(value.value === null ? '' : String(value.value));
  const [notes, setNotes] = useState(value.notes ?? '');
  useEffect(() => setDistance(value.value === null ? '' : String(value.value)), [value.value]);
  useEffect(() => setNotes(value.notes ?? ''), [value.notes]);
  const commit = () => {
    const next = { value: parseOrNull(distance), notes: notes.trim() === '' ? null : notes };
    if (next.value !== value.value || next.notes !== value.notes) {
      onCommit(next);
    }
  };
  return (
    <div className="senses-pane__distance-row">
      <span className="senses-pane__distance-label">{label}</span>
      <span className="senses-pane__distance-input">
        <input className="sidebar-value-editor__input" type="number" aria-label={`${label} distance`} value={distance} onChange={(event) => setDistance(event.target.value)} onBlur={commit} />
      </span>
      <span className="senses-pane__distance-source">
        <input className="sidebar-value-editor__input" aria-label={`${label} source notes`} value={notes} onChange={(event) => setNotes(event.target.value)} onBlur={commit} />
      </span>
    </div>
  );
}

type Dnd5eSensesPaneProps = { sheet: CharacterSheet; onMutate: MutationHandler; rulesText: string };

/** D&D Beyond's Senses pane: the passive scores and special senses, the Customize that overrides them, the rules. */
export function Dnd5eSensesPane({ sheet, onMutate, rulesText }: Dnd5eSensesPaneProps) {
  const { passives, senses } = customizationsOf(sheet);
  const save = (group: 'passives' | 'senses', target: string, next: NotedValue) =>
    onMutate({ type: 'CUSTOMIZE', group, target, value: next });

  return (
    <div className="senses-pane">
      <SidebarHeader title="Senses" />
      <div className="senses-pane__list">
        {PASSIVES.map(({ key, label }) => (
          <div key={key} className="senses-pane__sense">
            <span className="senses-pane__sense-label">{label}:</span> <span className="senses-pane__sense-value">{sheet.senses[key].value}</span>
          </div>
        ))}
        {sheet.specialSenses.map((sense) => (
          <div key={sense.type} className="senses-pane__sense">
            <span className="senses-pane__sense-label">{senseTypeLabel(sense.type)}:</span>{' '}
            <span className="senses-pane__sense-value">{sense.rangeFeet}</span> ft.
          </div>
        ))}
      </div>
      <Customize>
        <EditorBox>
          {PASSIVES.map(({ key, label }) => (
            <ValueEditorRow
              key={key}
              label={`Override ${label}`}
              value={passives[key]?.value ?? null}
              notes={passives[key]?.notes ?? ''}
              onCommit={(value, notes) => save('passives', key, { value, notes: notes || null })}
            />
          ))}
        </EditorBox>
        <div className="senses-pane__distance-header">
          <span className="senses-pane__distance-label" />
          <span className="senses-pane__distance-input">Distance (ft.)</span>
          <span className="senses-pane__distance-source">Source/Notes</span>
        </div>
        {SENSE_TYPES.map(({ key, label }) => (
          <SenseDistanceRow key={key} label={label} value={senses[key] ?? { value: null, notes: null }} onCommit={(next) => save('senses', key, next)} />
        ))}
      </Customize>
      <RulesText text={rulesText} />
    </div>
  );
}
