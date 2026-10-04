import type { ForcedRollMode, ProficiencyLevel } from '../../sheet/api';
import { ProficiencyDot } from './ProficiencyDot';
import { RollModeMarker } from './RollModeMarker';

type SkillRowProps = {
  name: string;
  abilityAbbreviation: string;
  proficiency: ProficiencyLevel;
  modifier: number;
  rollMode?: ForcedRollMode;
  onRoll: () => void;
  onRollContextMenu: (x: number, y: number) => void;
  onOpenExplainer: () => void;
};

function formatModifier(modifier: number): string {
  return modifier >= 0 ? `+${modifier}` : `−${Math.abs(modifier)}`;
}

/**
 * No dedicated row-level frame asset exists for a skill row — unlike the saving
 * throw row's dnd_frame_saving_throw_attribute.svg, no equivalent was cut for a
 * full skill name. This row follows the frame-box architecture's conventions
 * (semantic buttons, hover only on the roll target) without a row mask, sitting
 * inside the already-framed skills panel. The proficiency marker uses the same
 * unchecked-circle icon as the saving throw row.
 *
 * **General re-verification, 2026-08-16:** column widths/paddings/font-sizes
 * ported from D&D Beyond's own live DOM (`.ct-skills__item`, inspected via
 * computed styles, not guessed) — see frames.css's `.skill-row` block for the
 * measurements. `.skill-row__prof-cell` is a fixed-width wrapper (D&D Beyond's
 * own 30px prof column) around the small circle icon itself, which stays its
 * own natural size — the column and the icon are different things there,
 * unlike this row's earlier version where one element played both roles.
 *
 * **Direct owner request, 2026-08-17:** the skill name and bonus columns each
 * gained a wrapping `<div>` around their own `<button>`, matching D&D
 * Beyond's own structure (confirmed live: `.ct-skills__col--skill`/
 * `.ct-skills__col--modifier` are plain `<div>` column wrappers; the inner
 * `<button>` inside the modifier column carries its own separate border for
 * its own badge look). `.skill-row__name`/`.skill-row__mod` — the column's
 * own flex sizing and the `#d8d8d8` divider — moved onto these new divs;
 * `.skill-row__name-button`/`.skill-row__mod-button` are new classes for the
 * buttons themselves, which just fill their own column now. The buttons stay
 * real `<button>`s either way — this project's own non-negotiable is "every
 * clickable is a button" (`systems/dnd-5e/sheet-build.md`), the div only wraps it,
 * never replaces it.
 */
export function SkillRow({ name, abilityAbbreviation, proficiency, modifier, rollMode, onRoll, onRollContextMenu, onOpenExplainer }: SkillRowProps) {
  const formattedModifier = formatModifier(modifier);

  return (
    <div className="skill-row">
      <div className="skill-row__prof-cell">
        <ProficiencyDot level={proficiency} className="skill-row__prof" />
      </div>
      <div className="skill-row__ability">{abilityAbbreviation}</div>
      <div className="skill-row__name">
        <button
          type="button"
          className="reveal skill-row__name-button"
          aria-label={`Open ${name} details`}
          onClick={onOpenExplainer}
        >
          {name}
        </button>
      </div>
      {rollMode && rollMode.mode !== 'NORMAL' && (
        <div className="skill-row__adjustments">
          <RollModeMarker rollMode={rollMode} className="skill-row__adjustment" />
        </div>
      )}
      <div className="skill-row__mod">
        <button
          type="button"
          className="roll roll--modifier skill-row__mod-button"
          aria-label={`Roll ${name}, ${formattedModifier}`}
          onClick={onRoll}
          onContextMenu={(event) => {
            event.preventDefault();
            onRollContextMenu(event.clientX, event.clientY);
          }}
        >
          {formattedModifier}
        </button>
      </div>
    </div>
  );
}
