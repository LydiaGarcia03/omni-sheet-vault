import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import { getCatalogue, type CatalogueEntry } from '../../../catalogue/api';
import { SourceCodesProvider } from '../../../catalogue/SourceCodes';
import type { ProgressionTable } from '../../../builder/draftApi';
import { ProgressionPanel } from '../../../builder/ProgressionPanel';
import { cancelLevelUp, finishLevelUp, getLevelUp, saveLevelUp, startLevelUp, type LevelUp } from '../../../levelup/levelUpApi';
import { getCharacterSheet, type Experience } from '../../../sheet/api';
import { AppHeader } from '../../../shell/AppHeader';
import { AppFooter } from '../../../shell/AppFooter';
import { choiceRenderer } from '../builder/Dnd5eStepPage';
import { Dnd5eRolledHitPointsField } from '../builder/Dnd5eRolledHitPointsField';
import { withSelection, type Dnd5eBuild } from '../builder/dnd5eBuild';
import '../themes.css';
import '../../../builder/builder.css';

const AUTOSAVE_DELAY_MS = 400;
const ROLLED_HIT_POINTS = 'ROLLED_HIT_POINTS';

type Dnd5eLevelUpPageProps = { characterId: string; characterName: string; userName: string };

/** The features a class's progression lists at `level`, or null. */
function featuresAt(table: ProgressionTable | undefined, level: number): string | null {
  const column = table?.columns.indexOf('Features') ?? -1;
  const cell = column >= 0 ? table?.rows[level - 1]?.[column] : undefined;
  return cell && cell !== '—' ? cell : null;
}

/**
 * The level up page: pick the class to level (one the character has, or a new one), see what that
 * level grants, make its choices, then finish to go back to the sheet with the new level.
 */
export function Dnd5eLevelUpPage({ characterId, characterName, userName }: Dnd5eLevelUpPageProps) {
  const navigate = useNavigate();
  const [levelUp, setLevelUp] = useState<LevelUp | null | undefined>(undefined);
  const [experience, setExperience] = useState<Experience | null>(null);
  const [classes, setClasses] = useState<CatalogueEntry[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const saveTimer = useRef<number | undefined>(undefined);

  useEffect(() => {
    Promise.all([getLevelUp(characterId), getCharacterSheet(characterId), getCatalogue('dnd-5e', 'CLASS')])
      .then(([current, sheet, catalogueClasses]) => {
        setLevelUp(current);
        setExperience(sheet.experience ?? null);
        setClasses(catalogueClasses);
      })
      .catch((cause: Error) => setError(cause.message));
  }, [characterId]);

  const backToSheet = useCallback(() => navigate(`/characters/${characterId}`), [navigate, characterId]);

  const run = async (action: () => Promise<void>) => {
    setBusy(true);
    setError(null);
    try {
      await action();
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setBusy(false);
    }
  };

  const start = (classSlug: string) => run(async () => setLevelUp(await startLevelUp(characterId, classSlug)));
  const cancel = () => run(async () => {
    await cancelLevelUp(characterId);
    backToSheet();
  });
  const changeClass = () => run(async () => {
    await cancelLevelUp(characterId);
    setLevelUp(null);
  });
  const finish = () => run(async () => {
    window.clearTimeout(saveTimer.current);
    if (levelUp) {
      await saveLevelUp(characterId, levelUp.build);
    }
    await finishLevelUp(characterId);
    backToSheet();
  });

  const changeBuild = (build: Dnd5eBuild) => {
    setLevelUp((current) => (current ? { ...current, build } : current));
    window.clearTimeout(saveTimer.current);
    saveTimer.current = window.setTimeout(() => {
      saveLevelUp(characterId, build)
        .then(setLevelUp)
        .catch((cause: Error) => setError(cause.message));
    }, AUTOSAVE_DELAY_MS);
  };

  return (
    <div className="builder-page level-up-page">
      <AppHeader userName={userName} compact systemId="dnd-5e" />
      <main className="level-up">
        <div className="builder-step-head">
          <h1>Level up · {characterName}</h1>
          <p>Add a level to a class you have, or start a new one. Make the level's choices, then finish.</p>
        </div>
        {error && (
          <div className="builder-error" role="alert">
            {error}
          </div>
        )}
        {levelUp === undefined && !error && <p className="builder-message">Loading…</p>}
        {levelUp === null && (
          <ClassPicker experience={experience} classes={classes} busy={busy} onPick={start} onCancel={backToSheet} />
        )}
        {levelUp && (
          <SourceCodesProvider systemId="dnd-5e">
            <LevelUpEditor
              levelUp={levelUp}
              classes={classes}
              busy={busy}
              onBuildChange={changeBuild}
              onFinish={finish}
              onChangeClass={changeClass}
              onCancel={cancel}
            />
          </SourceCodesProvider>
        )}
      </main>
      <AppFooter />
    </div>
  );
}

type ClassPickerProps = {
  experience: Experience | null;
  classes: CatalogueEntry[];
  busy: boolean;
  onPick: (classSlug: string) => void;
  onCancel: () => void;
};

function ClassPicker({ experience, classes, busy, onPick, onCancel }: ClassPickerProps) {
  const [newClass, setNewClass] = useState('');
  const taken = experience?.classes ?? [];
  const slugOf = (name: string) => classes.find((entry) => entry.name === name)?.slug;
  const others = classes.filter((entry) => !taken.some((classLevel) => classLevel.name === entry.name));

  return (
    <>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>Add a level to a class you have</span>
        </div>
        <div className="level-up__classes">
          {taken.map((classLevel) => {
            const slug = slugOf(classLevel.name);
            return (
              <button
                key={classLevel.name}
                type="button"
                className="builder-btn builder-btn--primary"
                disabled={busy || !slug}
                onClick={() => slug && onPick(slug)}
              >
                {classLevel.name} {classLevel.level} → {classLevel.level + 1}
              </button>
            );
          })}
        </div>
      </section>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>Or start a new class (multiclass)</span>
        </div>
        <div className="level-up__classes">
          <select className="level-up__select" value={newClass} onChange={(event) => setNewClass(event.target.value)} aria-label="New class">
            <option value="">Choose a class…</option>
            {others.map((entry) => (
              <option key={entry.slug} value={entry.slug}>
                {entry.name}
                {entry.sourceBook ? ` (${entry.sourceBook})` : ''}
              </option>
            ))}
          </select>
          <button type="button" className="builder-btn" disabled={busy || !newClass} onClick={() => onPick(newClass)}>
            Start at level 1
          </button>
        </div>
      </section>
      <div className="level-up__actions">
        <button type="button" className="builder-btn" onClick={onCancel}>
          Back to the sheet
        </button>
      </div>
    </>
  );
}

type LevelUpEditorProps = {
  levelUp: LevelUp;
  classes: CatalogueEntry[];
  busy: boolean;
  onBuildChange: (build: Dnd5eBuild) => void;
  onFinish: () => void;
  onChangeClass: () => void;
  onCancel: () => void;
};

function LevelUpEditor({ levelUp, classes, busy, onBuildChange, onFinish, onChangeClass, onCancel }: LevelUpEditorProps) {
  const build = levelUp.build as Dnd5eBuild;
  const className = classes.find((entry) => entry.slug === levelUp.classSlug)?.name ?? levelUp.classSlug;
  const progression = levelUp.preview.progressions?.find((table) => table.key === levelUp.classSlug);
  const features = featuresAt(progression, levelUp.classLevel);
  const choices = levelUp.plan.choices;
  const { renderChoice, isNested } = choiceRenderer({
    characterId: levelUp.id,
    choices,
    build,
    onAnswer: (choiceId, selected) => onBuildChange(withSelection(build, choiceId, selected)),
    onBuildChange,
  });
  const pending = choices.filter((choice) => choice.pending).length;
  const canFinish = pending === 0 && levelUp.plan.problems.length === 0 && !busy;
  const gained = levelUp.maxHitPointsAfter === null ? null : levelUp.maxHitPointsAfter - levelUp.maxHitPointsBefore;

  return (
    <>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>
            {className} {levelUp.classLevel}
          </span>
          {pending > 0 ? (
            <span className="builder-badge builder-badge--pending">{pending} left</span>
          ) : (
            <span className="builder-badge builder-badge--done">done</span>
          )}
        </div>
        <p className="builder-level__gains">
          <b>This level grants:</b> {features ?? 'no new features.'}
        </p>
        {gained !== null && (
          <p className="builder-level__gains">
            <b>Hit points:</b> {levelUp.maxHitPointsBefore} → {levelUp.maxHitPointsAfter} ({gained >= 0 ? '+' : ''}
            {gained}). Current hit points rise by the same amount.
          </p>
        )}
      </section>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>Choices</span>
        </div>
        {choices.length === 0 ? (
          <p className="builder-muted">Nothing to choose at this level.</p>
        ) : (
          choices
            .filter((choice) => !isNested(choice))
            .map((choice) =>
              choice.type === ROLLED_HIT_POINTS ? (
                <Dnd5eRolledHitPointsField
                  key={choice.id}
                  characterId={levelUp.id}
                  choice={choice}
                  build={build}
                  onChange={onBuildChange}
                  group={levelUp.classSlug}
                  classLevel={levelUp.classLevel}
                />
              ) : (
                renderChoice(choice)
              ),
            )
        )}
      </section>
      {progression && (
        <ProgressionPanel
          table={{ ...progression, currentLevel: levelUp.classLevel }}
          defaultOpen
          caption={
            <>
              After this level up: <b>{className} {levelUp.classLevel}</b>, character level{' '}
              {build.classes.reduce((total, entry) => total + entry.level, 0)}.
            </>
          }
        />
      )}
      {levelUp.plan.problems.length > 0 && (
        <div className="builder-error" role="alert">
          {levelUp.plan.problems.join(' ')}
        </div>
      )}
      <div className="level-up__actions">
        <button type="button" className="builder-btn builder-btn--primary" disabled={!canFinish} onClick={onFinish}>
          Finish leveling up
        </button>
        <button type="button" className="builder-btn" disabled={busy} onClick={onChangeClass}>
          Change class
        </button>
        <button type="button" className="builder-btn" disabled={busy} onClick={onCancel}>
          Cancel
        </button>
      </div>
    </>
  );
}
