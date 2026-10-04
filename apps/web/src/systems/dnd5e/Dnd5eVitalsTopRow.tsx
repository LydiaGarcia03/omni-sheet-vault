import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, ExplainHandler, MutationHandler, PaneHandler } from '../../sheet/api';
import { AbilityBox } from './AbilityBox';
import { Dnd5eAbilityPane } from './Dnd5eAbilityPane';
import { displayedMovement, Dnd5eSpeedPane } from './Dnd5eCombatPanes';
import { DeathSavesBox } from './DeathSavesBox';
import { ABILITY_ICONS } from './abilityIcons';
import { FrameIcon } from './FrameIcon';
import { HeroicInspiration } from './HeroicInspiration';
import { HitPoints } from './HitPoints';
import { ABILITY_RULES_TEXT, PROFICIENCY_BONUS_RULES_TEXT } from './rulesText';
import { StatBadge } from './StatBadge';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

const ABILITIES: { key: string; label: string }[] = [
  { key: 'strength', label: 'Strength' },
  { key: 'dexterity', label: 'Dexterity' },
  { key: 'constitution', label: 'Constitution' },
  { key: 'intelligence', label: 'Intelligence' },
  { key: 'wisdom', label: 'Wisdom' },
  { key: 'charisma', label: 'Charisma' },
];

type Dnd5eVitalsTopRowProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onExplain: ExplainHandler;
  onOpenPane: PaneHandler;
  onOpenHpManagement: () => void;
  onRollDeathSave: () => void;
};

/**
 * Six ability boxes, proficiency bonus, speed, heroic inspiration, hit points —
 * the full top row from systems/dnd-5e/sheet-ui.md. Inspiration and hit points have no
 * frame asset (frame-kit.html marks both "Not built"), so they render as plain
 * bordered boxes instead of the masked frame-box pattern — see HeroicInspiration
 * and HitPoints.
 *
 * **Direct owner request, 2026-08-17:** the standalone Hit Dice box that used
 * to sit here is gone — D&D Beyond has no such top-row section (hit dice only
 * ever surface inside its Short Rest panel, matching the real 5e rule that hit
 * dice are spent *during* a short rest, not as a free-standing action). Moved
 * into the Short Rest sidebar instead — see `CharacterSheetScreen.tsx`'s
 * `handleOpenShortRest`, which now renders `HitDice` (the pool, still spendable
 * on its own) above `ShortRestBody`'s existing "spend as part of resting"
 * stepper, rather than deleting the standalone-spend capability outright.
 *
 * **Same day, follow-up owner request:** `justifyContent: 'space-between'`
 * added alongside the existing `gap` (a floor, not the only spacing now) so
 * this row's own trailing item (Hit Points) lands flush with this row's
 * right edge instead of packing left with dead space trailing it. This row
 * is a block-level flex container with no width of its own, so it already
 * stretches to match its widest sibling (`Dnd5eVitalsColumns`' three-column
 * row below it, `CharacterSheetScreen.tsx`) — confirmed live before this fix
 * that both rows share an identical rendered width (1196px) and left edge,
 * but this row's items, packed left under plain `flex-start`, left Hit
 * Points' own right edge 108px short of the combat column's (and so the
 * tabbed section's) right edge below. `space-between` distributes that gap
 * across every item instead, so Hit Points' right edge now tracks the
 * combat column's own right edge automatically — self-correcting per
 * character, rather than a hand-tuned pixel gap that would need re-tuning
 * whenever the combat column's own natural width changes (it isn't fixed —
 * see frames.css's `.tabbed-section` comment).
 *
 * **Direct owner request, 2026-08-17:** the `-8px` top margin pulls this row up
 * into `SheetShell.tsx`'s dark header bar, matching D&D Beyond's own layout —
 * confirmed live (`getBoundingClientRect` on `.ct-character-header-desktop`
 * and `.ct-quick-info__ability`): the ability-score row there starts 8px
 * above the header's own bottom edge, not flush against it. Each ability
 * box's own opaque frame artwork (`dnd_frame.svg`'s paper layer) covers the
 * dark bar within the overlap; the sliver of dark bar visible between boxes
 * and past the row's own edges is the same effect the reference shows.
 *
 * **Corrected 2026-09-03, direct owner request:** `marginTop: '-8px'` alone
 * left this row's own bottom margin at the browser default (`0`), so it sat
 * flush against `Dnd5eVitalsColumns` below with no breathing room once that
 * sibling's own `marginTop: '16px'` was removed (`CharacterSheetScreen.tsx`)
 * — collapsed into a single `margin: '-8px 0 10px'` shorthand so this row
 * alone owns the gap to the section below it, rather than splitting that
 * spacing across two different components' inline styles.
 */
export function Dnd5eVitalsTopRow({
  sheet,
  onRoll,
  onRollContextMenu,
  onMutate,
  onExplain,
  onOpenPane,
  onOpenHpManagement,
  onRollDeathSave,
}: Dnd5eVitalsTopRowProps) {
  const movement = displayedMovement(sheet);
  return (
    <div style={{ display: 'flex', gap: '5px', justifyContent: 'space-between', alignItems: 'flex-start', margin: '-8px 0 10px' }}>
      {ABILITIES.map(({ key, label }) => (
        <AbilityBox
          key={key}
          name={label}
          score={sheet.abilityScores[key]}
          modifier={sheet.abilityModifiers[key].value}
          onRoll={() => onRoll('ABILITY_CHECK', key)}
          onRollContextMenu={(x, y) => onRollContextMenu(x, y, 'ABILITY_CHECK', key, formatSigned(sheet.abilityModifiers[key].value))}
          onOpenExplainer={() =>
            onOpenPane({
              kind: 'pane',
              render: (liveSheet) =>
                liveSheet.provenance ? (
                  <Dnd5eAbilityPane
                    label={label}
                    icon={<FrameIcon svg={ABILITY_ICONS[key]} className="explainer-icon" aria-hidden />}
                    breakdown={liveSheet.provenance.abilityBreakdowns[key]}
                    onCustomize={(value) => onMutate({ type: 'CUSTOMIZE', group: 'abilities', target: key, value })}
                    rulesText={ABILITY_RULES_TEXT[key]}
                  />
                ) : null,
            })
          }
        />
      ))}
      <StatBadge
        heading="Proficiency"
        headingClassName="text-bold-uppercase"
        value={sheet.proficiencyBonus.value >= 0 ? `+${sheet.proficiencyBonus.value}` : `${sheet.proficiencyBonus.value}`}
        caption="Bonus"
        ariaLabel={`Proficiency bonus ${sheet.proficiencyBonus.value}, open details`}
        onOpenExplainer={() =>
          onExplain({
            kind: 'explainer',
            title: 'Proficiency Bonus',
            formattedValue: formatSigned(sheet.proficiencyBonus.value),
            contributions: sheet.proficiencyBonus.contributions,
            rulesText: PROFICIENCY_BONUS_RULES_TEXT,
          })
        }
      />
      <StatBadge
        heading={movement.label}
        value={`${movement.speed} ft.`}
        caption="Speed"
        ariaLabel={`${movement.label} speed ${movement.speed} feet, open details`}
        onOpenExplainer={() =>
          onOpenPane({ kind: 'pane', render: (liveSheet) => <Dnd5eSpeedPane sheet={liveSheet} onMutate={onMutate} /> })
        }
      />
      <HeroicInspiration inspired={sheet.heroicInspiration} onToggle={() => onMutate({ type: 'TOGGLE_INSPIRATION' })} />
      {sheet.deathSaves?.dying ? (
        <DeathSavesBox
          saves={sheet.deathSaves}
          onSet={(successes, failures) => onMutate({ type: 'SET_DEATH_SAVES', successes, failures })}
          onRoll={onRollDeathSave}
          onOpenManagement={onOpenHpManagement}
        />
      ) : (
        <HitPoints
          current={sheet.currentHitPoints}
          max={sheet.hitPoints.value}
          temporary={sheet.temporaryHitPoints}
          onDamage={(amount) => onMutate({ type: 'DAMAGE', amount })}
          onHeal={(amount) => onMutate({ type: 'HEAL', amount })}
          onSetTemporary={(amount) => onMutate({ type: 'TEMPORARY_HIT_POINTS', amount })}
          onOpenManagement={onOpenHpManagement}
        />
      )}
    </div>
  );
}
