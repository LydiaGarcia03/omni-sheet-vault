import { useState, type ReactNode } from 'react';
import type { CreationChoice, ProgressionTable } from '../../../builder/draftApi';
import { ProgressionPanel } from '../../../builder/ProgressionPanel';
import type { Dnd5eBuild } from './dnd5eBuild';

type Dnd5eClassLevelsProps = {
  build: Dnd5eBuild;
  classLabels: Record<string, string>;
  choices: CreationChoice[];
  progressions: ProgressionTable[];
  renderChoice: (choice: CreationChoice) => ReactNode;
  isNested: (choice: CreationChoice) => boolean;
  /** Sections drawn after the selected class's levels and before its progression, which always comes last. */
  beforeProgression?: ReactNode;
  /** A class's own hit point rows, or null when it has none to set. */
  renderHitPoints?: (classSlug: string) => ReactNode;
  /** A class's hit point levels still without a value, counted with its pending choices. */
  hitPointsLeftIn?: (classSlug: string) => number;
};

/**
 * One tab per class, and in it one section per level reached (features gained, choices made), then the class's own
 * hit points; the class's progression comes last.
 */
export function Dnd5eClassLevels({
  build,
  classLabels,
  choices,
  progressions,
  renderChoice,
  isNested,
  beforeProgression,
  renderHitPoints,
  hitPointsLeftIn,
}: Dnd5eClassLevelsProps) {
  const pendingIn = (slug: string) =>
    choices.filter((choice) => choice.group === slug && choice.pending).length + (hitPointsLeftIn ? hitPointsLeftIn(slug) : 0);
  const firstPending = build.classes.find((entry) => pendingIn(entry.classSlug) > 0);
  const [tab, setTab] = useState<string | null>(null);
  const current = build.classes.find((entry) => entry.classSlug === tab) ?? firstPending ?? build.classes[0];
  if (!current) {
    return null;
  }
  const progression = progressions.find((table) => table.key === current.classSlug);
  const hitPoints = renderHitPoints ? renderHitPoints(current.classSlug) : null;
  const label = classLabels[current.classSlug] ?? current.classSlug;

  return (
    <>
      {build.classes.length > 1 && (
        <div className="builder-tabs" role="tablist" aria-label="Classes">
          {build.classes.map((entry) => (
            <button
              key={entry.classSlug}
              type="button"
              role="tab"
              aria-selected={entry.classSlug === current.classSlug}
              className={`builder-tab${entry.classSlug === current.classSlug ? ' is-on' : ''}`}
              onClick={() => setTab(entry.classSlug)}
            >
              {classLabels[entry.classSlug] ?? entry.classSlug} {entry.level}
              {pendingIn(entry.classSlug) > 0 && <span className="builder-badge builder-badge--pending">{pendingIn(entry.classSlug)}</span>}
            </button>
          ))}
        </div>
      )}
      <ClassPanel
        key={current.classSlug}
        label={label}
        level={current.level}
        choices={choices.filter((choice) => choice.group === current.classSlug && !isNested(choice))}
        progression={progression}
        renderChoice={renderChoice}
      />
      {hitPoints && (
        <section className="builder-frame">
          <div className="builder-frame__title">
            <span>Hit points · {label}</span>
            {hitPointsLeftIn && hitPointsLeftIn(current.classSlug) > 0 ? (
              <span className="builder-badge builder-badge--pending">{hitPointsLeftIn(current.classSlug)} left</span>
            ) : (
              <span className="builder-badge builder-badge--done">done</span>
            )}
          </div>
          {hitPoints}
        </section>
      )}
      {beforeProgression}
      {progression && <ProgressionPanel key={`progression-${current.classSlug}`} table={progression} />}
    </>
  );
}

type ClassPanelProps = {
  label: string;
  level: number;
  choices: CreationChoice[];
  progression: ProgressionTable | undefined;
  renderChoice: (choice: CreationChoice) => ReactNode;
};

function ClassPanel({ label, level, choices, progression, renderChoice }: ClassPanelProps) {
  const pending = choices.filter((choice) => choice.pending).length;
  const featuresColumn = progression?.columns.indexOf('Features') ?? -1;
  const featuresAt = (classLevel: number) => {
    const cell = featuresColumn >= 0 ? progression?.rows[classLevel - 1]?.[featuresColumn] : undefined;
    return cell && cell !== '—' ? cell : null;
  };
  const wholeClass = choices.filter((choice) => choice.level === null || choice.level === undefined);

  return (
    <section className="builder-frame">
      <div className="builder-frame__title">
        <span>
          {label} · {level === 1 ? 'level 1' : `levels 1–${level}`}
        </span>
        {pending > 0 ? (
          <span className="builder-badge builder-badge--pending">{pending} left</span>
        ) : (
          <span className="builder-badge builder-badge--done">done</span>
        )}
      </div>
      {Array.from({ length: level }, (_, index) => index + 1).map((classLevel) => (
        <LevelSection
          key={classLevel}
          level={classLevel}
          features={featuresAt(classLevel)}
          choices={choices.filter((choice) => choice.level === classLevel)}
          renderChoice={renderChoice}
        />
      ))}
      {wholeClass.length > 0 && (
        <div className="builder-level__whole">
          <div className="builder-label">Every level</div>
          {wholeClass.map(renderChoice)}
        </div>
      )}
    </section>
  );
}

type LevelSectionProps = {
  level: number;
  features: string | null;
  choices: CreationChoice[];
  renderChoice: (choice: CreationChoice) => ReactNode;
};

/** A level's section starts open while it still has a choice to make; the player opens or closes it after that. */
function LevelSection({ level, features, choices, renderChoice }: LevelSectionProps) {
  const pending = choices.filter((choice) => choice.pending).length;
  const [open, setOpen] = useState(pending > 0);
  const badge =
    choices.length === 0 ? (
      <span className="builder-muted">no choices</span>
    ) : pending > 0 ? (
      <span className="builder-badge builder-badge--pending">
        {pending} {pending === 1 ? 'choice' : 'choices'}
      </span>
    ) : (
      <span className="builder-badge builder-badge--done">done</span>
    );

  return (
    <details className="builder-level" open={open} onToggle={(event) => setOpen(event.currentTarget.open)}>
      <summary className="builder-level__summary">
        <span className="builder-level__number">Level {level}</span>
        {badge}
        {features && <span className="builder-level__features">{features}</span>}
      </summary>
      <div className="builder-level__body">
        {features && (
          <p className="builder-level__gains">
            <b>Features:</b> {features}
          </p>
        )}
        {choices.length === 0 ? <p className="builder-muted">Nothing to choose at this level.</p> : choices.map(renderChoice)}
      </div>
    </details>
  );
}
