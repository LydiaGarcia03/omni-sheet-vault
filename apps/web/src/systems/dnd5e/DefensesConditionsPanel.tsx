import type { CharacterSheet } from '../../sheet/api';
import { DamageTypeIcon } from './DamageTypeIcon';
import { defenseLabels } from './Dnd5eCombatPanes';
import frameSvg from './frames/dnd_frame_defenses_conditions.svg?raw';
import frameInkSvg from './frames/dnd_frame_defenses_conditions_ink.svg?raw';
import immunitySvg from './frames/dnd_icon_immunity.svg?raw';
import resistanceSvg from './frames/dnd_icon_resistance.svg?raw';
import vulnerabilitySvg from './frames/dnd_icon_vulnerability.svg?raw';
import { SectionPanel } from './SectionPanel';

export const CONDITIONS: { key: string; label: string }[] = [
  { key: 'blinded', label: 'Blinded' },
  { key: 'charmed', label: 'Charmed' },
  { key: 'deafened', label: 'Deafened' },
  { key: 'frightened', label: 'Frightened' },
  { key: 'grappled', label: 'Grappled' },
  { key: 'incapacitated', label: 'Incapacitated' },
  { key: 'invisible', label: 'Invisible' },
  { key: 'paralyzed', label: 'Paralyzed' },
  { key: 'petrified', label: 'Petrified' },
  { key: 'poisoned', label: 'Poisoned' },
  { key: 'prone', label: 'Prone' },
  { key: 'restrained', label: 'Restrained' },
  { key: 'stunned', label: 'Stunned' },
  { key: 'unconscious', label: 'Unconscious' },
];

type DefensesConditionsPanelProps = {
  sheet: CharacterSheet;
  onOpenConditions: () => void;
  onOpenDefenses: () => void;
};

/**
 * Defenses and Conditions share one frame with a divider between them, matching
 * the reference — instead of two separate panels side by side. Uses its own
 * dedicated frame (dnd_frame_defenses_conditions.svg, phase 10 asset refactor)
 * rather than a generic stretchy panel.
 *
 * Resistances, Immunities and Vulnerabilities each get D&D Beyond's own
 * shield icon (`DamageTypeIcon`) — Resistance/Immunity confirmed live in the
 * same green (`--defense-badge`); Vulnerability isn't confirmed live (this
 * app's reference character has neither) so it uses `--status-negative`
 * instead, a deliberate choice pending real confirmation. Condition
 * Immunities is a different concept (immune to a condition, not a damage
 * type) and stays plain text under its own heading, no icon.
 *
 * The "Defenses"/"Conditions" column titles sit at the top, confirmed live
 * against D&D Beyond (`getBoundingClientRect` on `.ct-combat__summary-label`:
 * y=10 inside a 95px-tall container) — an earlier version of this component
 * pinned them to the bottom via `margin-top: auto`, on an unverified
 * assumption that turned out wrong once actually measured.
 *
 * Phase 10 slice 4: the always-visible fourteen-item checklist moved into the
 * sidebar's Conditions mold (confirmed live against D&D Beyond — clicking
 * "Add Active Conditions" opens exactly that). This column now shows a
 * compact `.reveal` trigger instead: "Add Active Conditions" when none are
 * active, or a comma-joined summary of the active ones, matching the
 * reference. `CONDITIONS` (the fourteen-item list) is exported for
 * `CharacterSheetScreen.tsx` to build the sidebar's `ConditionsRequest` from.
 *
 * Exhaustion tracks a level (0-6), not a boolean like the other fourteen —
 * it joins the same summary line ("Exhaustion (Level 2)", as D&D Beyond writes it) when nonzero, and its level bar
 * sits at the bottom of the sidebar's Conditions pane.
 *
 * Active effects (a cast Mage Armor) join the same summary line; the sidebar
 * lists them above the conditions, each with an End button.
 */
export function DefensesConditionsPanel({ sheet, onOpenConditions, onOpenDefenses }: DefensesConditionsPanelProps) {
  const resistances = defenseLabels(sheet, 'RESISTANCE', sheet.damageResistances);
  const immunities = defenseLabels(sheet, 'IMMUNITY', sheet.damageImmunities);
  const vulnerabilities = defenseLabels(sheet, 'VULNERABILITY', sheet.damageVulnerabilities);
  const plainCategories = [
    { label: 'Condition Immunities', items: defenseLabels(sheet, 'CONDITION_IMMUNITY', sheet.conditionImmunities) },
  ].filter((category) => category.items.length > 0);

  const activeLabels = CONDITIONS.filter((condition) => sheet.activeConditions.includes(condition.key)).map(
    (condition) => condition.label,
  );
  if (sheet.exhaustionLevel > 0) {
    activeLabels.push(`Exhaustion (Level ${sheet.exhaustionLevel})`);
  }
  activeLabels.push(...sheet.activeEffects.map((effect) => (effect.onSelf ? effect.name : `${effect.name} (Ally)`)));

  return (
    <SectionPanel frameClass="defenses-conditions-panel" svg={frameSvg} inkSvg={frameInkSvg} contentClassName="defenses-conditions">
      <div
        className="defenses-conditions__col defenses-conditions__defenses reveal"
        role="button"
        tabIndex={0}
        aria-label="Defenses, open details"
        onClick={onOpenDefenses}
        onKeyDown={(event) => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            onOpenDefenses();
          }
        }}
      >
        <div className="panel__title">Defenses</div>
        {resistances.length > 0 && (
          <div className="panel__rows">
            <div className="damage-item-row">
              <DamageTypeIcon svg={resistanceSvg} variant="resistance" label="Resistance" />
              <span>{resistances.join(', ')}</span>
            </div>
          </div>
        )}
        {immunities.length > 0 && (
          <div className="panel__rows">
            <div className="damage-item-row">
              <DamageTypeIcon svg={immunitySvg} variant="immunity" label="Immunity" />
              <span>{immunities.join(', ')}</span>
            </div>
          </div>
        )}
        {vulnerabilities.length > 0 && (
          <div className="panel__rows">
            <div className="damage-item-row">
              <DamageTypeIcon svg={vulnerabilitySvg} variant="vulnerability" label="Vulnerability" />
              <span>{vulnerabilities.join(', ')}</span>
            </div>
          </div>
        )}
        {plainCategories.length > 0 && (
          <div className="panel__list" role="list">
            {plainCategories.map(({ label, items }) => (
              <div key={label} className="panel__list-item" role="listitem">
                <div className="panel__list-label">{label}</div>
                <div className="panel__list-value">{items.join(', ')}</div>
              </div>
            ))}
          </div>
        )}
      </div>
      <div className="defenses-conditions__divider" aria-hidden="true" />
      <div className="defenses-conditions__col">
        <div className="panel__title">Conditions</div>
        <button type="button" className="reveal defenses-conditions__trigger" onClick={onOpenConditions}>
          {activeLabels.length > 0 ? activeLabels.join(', ') : 'Add Active Conditions'}
        </button>
      </div>
    </SectionPanel>
  );
}
