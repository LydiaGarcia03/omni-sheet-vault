import type { Experience } from './api';

export function formatPoints(points: number): string {
  return points.toLocaleString('en-US');
}

/** The level a total reaches in the API's threshold table (level 1 first). */
export function levelForPoints(points: number, thresholds: number[]): number {
  let level = 1;
  while (level < thresholds.length && points >= thresholds[level]) {
    level++;
  }
  return level;
}

/** How far the points are between the current level's threshold and the next one, from 0 to 1. */
export function levelProgress(experience: Experience): number {
  if (experience.nextLevelAt === null) {
    return 1;
  }
  const span = experience.nextLevelAt - experience.currentLevelAt;
  return Math.min(1, Math.max(0, (experience.points - experience.currentLevelAt) / span));
}

type XpBarProps = {
  experience: Experience;
  /** `header`: under the name; `menu`: the character sidebar; `panel`: Manage experience, on a light background. */
  variant: 'header' | 'menu' | 'panel';
  /** Overrides the points shown (the pending total while managing experience). */
  points?: number;
};

/** "LVL n" on each side of a thin bar in the theme color, and the points below. */
export function XpBar({ experience, variant, points = experience.points }: XpBarProps) {
  const shown = { ...experience, points };
  const nextLevel = Math.min(experience.level + 1, experience.thresholds.length);
  const progress = levelProgress(shown);
  const total = experience.nextLevelAt === null ? formatPoints(points) : `${formatPoints(points)} / ${formatPoints(experience.nextLevelAt)}`;

  return (
    <div className={`xp-bar xp-bar--${variant}`}>
      <div className="xp-bar__row">
        <span className="xp-bar__label">LVL {experience.level}</span>
        <span className="xp-bar__track">
          <span className="xp-bar__fill" style={{ width: `${progress * 100}%` }} />
          {variant === 'panel' && (
            <span className="xp-bar__marker" style={{ left: `${progress * 100}%` }}>
              <span className="xp-bar__marker-amount">{formatPoints(points)}</span>
            </span>
          )}
        </span>
        <span className="xp-bar__label">LVL {nextLevel}</span>
      </div>
      {variant === 'panel' ? (
        <div className="xp-bar__ends">
          <span>{formatPoints(experience.currentLevelAt)}</span>
          <span>{experience.nextLevelAt === null ? '' : formatPoints(experience.nextLevelAt)}</span>
        </div>
      ) : (
        <div className="xp-bar__data">{total} XP</div>
      )}
    </div>
  );
}
