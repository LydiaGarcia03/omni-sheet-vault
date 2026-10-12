import { useRef, useState, type ReactNode } from 'react';
import type { CharacterMenuRequest, Experience } from './api';
import { PencilIcon } from './sidebarIcons';
import { XpBar } from './XpBar';

const MAX_NAME_LENGTH = 128;

/** The name with D&D Beyond's pencil: a click opens an inline field, and leaving it saves a non-blank change. */
function EditableName({ name, onRename }: { name: string; onRename: (name: string) => void }) {
  const [draft, setDraft] = useState<string | null>(null);
  const cancelled = useRef(false);

  const start = () => {
    cancelled.current = false;
    setDraft(name);
  };
  const finish = () => {
    const next = (draft ?? '').trim();
    if (!cancelled.current && next !== '' && next !== name) {
      onRename(next);
    }
    setDraft(null);
  };

  if (draft !== null) {
    return (
      <div className="character-menu__name-row">
        <input
          className="character-menu__name-input"
          aria-label="Character name"
          autoFocus
          maxLength={MAX_NAME_LENGTH}
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={finish}
          onKeyDown={(event) => {
            if (event.key === 'Escape') {
              cancelled.current = true;
              setDraft(null);
            }
          }}
        />
      </div>
    );
  }
  return (
    <div className="character-menu__name-row is-editable" onClick={start}>
      <h1 className="character-menu__name">{name}</h1>
      <button type="button" className="character-menu__rename" aria-label={`Rename ${name}`}>
        <PencilIcon className="character-menu__pencil" />
      </button>
    </div>
  );
}

function MenuItem({ icon, label, onClick }: { icon: ReactNode; label: string; onClick: () => void }) {
  return (
    <li>
      <button type="button" className="character-menu__item" onClick={onClick}>
        <span className="character-menu__icon" aria-hidden="true">
          {icon}
        </span>
        {label}
      </button>
    </li>
  );
}

/** Marks the table's milestone as reached, which makes the next level available; unmarking takes it back. */
function MilestoneSwitch({ experience, onSetExperience }: { experience: Experience; onSetExperience: (points: number) => void }) {
  const reached = experience.levelUpAvailable;
  const toggle = () => onSetExperience(reached ? experience.currentLevelAt : (experience.nextLevelAt ?? experience.currentLevelAt));
  return (
    <li>
      <button type="button" role="switch" aria-checked={reached} className="character-menu__item character-menu__milestone" onClick={toggle}>
        <span className={`character-menu__milestone-box${reached ? ' is-on' : ''}`} aria-hidden="true" />
        Milestone reached
      </button>
    </li>
  );
}

/**
 * The character sidebar (D&D Beyond's "character manage" pane): portrait, name, subtitle, the
 * experience bar and class levels on a dark gradient, then the My character and Play menus.
 * Experience builds get Manage experience, milestone builds a Milestone reached switch; either
 * way Level up appears under it once a level is available.
 */
export function CharacterMenuPanel({
  name,
  portrait,
  subtitle,
  experience,
  onManageExperience,
  onSetExperience,
  onLevelUp,
  onOpenLog,
  onOpenShortRest,
  onOpenLongRest,
  onChangeAppearance,
  onRename,
  icons,
  classIcon,
}: CharacterMenuRequest) {
  const experienceBuild = experience?.advancement === 'XP';
  return (
    <div className="character-menu">
      <div className="character-menu__intro">
        <div className="character-menu__portrait">{portrait}</div>
        {onRename ? (
          <EditableName name={name} onRename={onRename} />
        ) : (
          <div className="character-menu__name-row">
            <h1 className="character-menu__name">{name}</h1>
          </div>
        )}
        {subtitle && <div className="character-menu__subtitle">{subtitle}</div>}
        {experience && experienceBuild && (
          <div className="character-menu__xp">
            <XpBar experience={experience} variant="menu" />
          </div>
        )}
        {experience && !experienceBuild && experience.classes.length > 0 && (
          <div className="character-menu__level">
            Level {experience.classes.reduce((sum, classLevel) => sum + classLevel.level, 0)}
          </div>
        )}
        {experience && experience.classes.length > 0 && (
          <ul className="character-menu__classes">
            {experience.classes.map((classLevel) => (
              <li key={classLevel.name} className="character-menu__class">
                <span className="character-menu__class-badge">
                  {classIcon?.(classLevel.name)}
                  <span className="character-menu__class-level">{classLevel.level}</span>
                </span>
                <span>
                  <span className="character-menu__class-name">{classLevel.name}</span>
                  {classLevel.subclass && <span className="character-menu__class-meta">{classLevel.subclass}</span>}
                </span>
              </li>
            ))}
          </ul>
        )}
        {onChangeAppearance && (
          <button type="button" className="character-menu__appearance" onClick={onChangeAppearance}>
            Change sheet appearance
          </button>
        )}
      </div>
      <ul className="character-menu__menu">
        <li className="character-menu__group">My character</li>
        {experienceBuild && (
          <MenuItem icon={<span className="character-menu__xp-glyph">XP</span>} label="Manage experience" onClick={onManageExperience} />
        )}
        {!experienceBuild && experience?.canLevelUp && experience.nextLevelAt !== null && onSetExperience && (
          <MilestoneSwitch experience={experience} onSetExperience={onSetExperience} />
        )}
        {experience?.levelUpAvailable && (
          <li className="character-menu__level-up-row">
            <button type="button" className="character-menu__level-up" onClick={onLevelUp}>
              Level up
            </button>
          </li>
        )}
        <li className="character-menu__group">Play</li>
        <MenuItem icon={icons.gameLog} label="Game log" onClick={onOpenLog} />
        <MenuItem icon={icons.shortRest} label="Short rest" onClick={onOpenShortRest} />
        <MenuItem icon={icons.longRest} label="Long rest" onClick={onOpenLongRest} />
      </ul>
    </div>
  );
}
