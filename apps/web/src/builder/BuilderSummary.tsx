import { CharacterPortrait } from '../characters/CharacterPortrait';
import type { Portrait } from '../characters/portrait';
import type { DraftPreview } from './draftApi';

type BuilderSummaryProps = {
  name: string;
  /** One line naming what the character is so far, e.g. "Mountain Dwarf · Fighter 3 · Folk Hero". */
  description: string;
  pendingCount: number;
  preview: DraftPreview;
  portrait: Portrait | null;
  systemId: string;
};

const signed = (value: number) => (value >= 0 ? `+${value}` : `−${Math.abs(value)}`);

const titleCase = (text: string) => text.replace(/\b\w/g, (letter) => letter.toUpperCase());

/** The builder's right column: what the character adds up to so far, recalculated by the API on every save. */
export function BuilderSummary({ name, description, pendingCount, preview, portrait, systemId }: BuilderSummaryProps) {
  const { vitals } = preview;
  const hasBase = (ability: DraftPreview['abilities'][number]) => ability.contributions.some((part) => part.source === 'Base');
  return (
    <section className="builder-frame builder-frame--side builder-summary" aria-label="Character summary">
      <div className="builder-frame__title">Your character</div>
      <div className="builder-summary__who">
        <CharacterPortrait portrait={portrait} systemId={systemId} className="builder-summary__portrait" />
        <div>
          <div className="builder-summary__name">{name}</div>
          <div className="builder-muted">
            {vitals && `Level ${vitals.level} · `}
            {description || 'Nothing chosen yet'}
          </div>
        </div>
      </div>

      <div className="builder-summary__abilities">
        {preview.abilities.map((ability) => (
          <div key={ability.ability} title={ability.contributions.map((part) => `${part.source} ${signed(part.amount)}`).join(' · ')}>
            <span className="builder-summary__ability-key">{ability.ability.slice(0, 3).toUpperCase()}</span>
            <b>{hasBase(ability) ? signed(ability.modifier) : '—'}</b>
            <span className="builder-summary__ability-score">{hasBase(ability) ? ability.score : '—'}</span>
          </div>
        ))}
      </div>

      {pendingCount > 0 && <div className="builder-summary__pending">{pendingCount} choices left</div>}

      {vitals ? (
        <>
          <Stat label="Hit points" value={vitals.hitPoints.value} note={vitals.hitDice} />
          <Stat label="Armor class" value={vitals.armorClass.value} note={vitals.armorClass.contributions.map((part) => part.source).join(' + ')} />
          <Stat label="Speed" value={`${vitals.speed} ft.`} />
          <Stat label="Initiative · Proficiency" value={`${signed(vitals.initiative)} · ${signed(vitals.proficiencyBonus)}`} />
          <Stat label="Saving throws" value={vitals.savingThrows.map((save) => `${save.name.slice(0, 3).toUpperCase()} ${signed(save.value)}`).join(' · ') || '—'} />
          <Stat label="Passive Perception" value={vitals.passivePerception} />
          <Group title="Attacks" lines={vitals.attacks.map((attack) => `${attack.name} ${signed(attack.toHit)} · ${attack.damage}`)} />
          <Group title="Features" lines={[vitals.features.join(' · ')]} />
          <Group
            title="Proficiencies"
            lines={[[...vitals.skills.map(titleCase), ...vitals.armor, ...vitals.weapons, ...vitals.tools].join(' · '), vitals.languages.join(', ')]}
          />
          <Group title="Defenses & senses" lines={[[...vitals.resistances.map((type) => `${titleCase(type)} resistance`), ...vitals.senses].join(' · ')]} />
        </>
      ) : (
        <p className="builder-muted">Choose a class and set your ability scores to see hit points, armor class and attacks.</p>
      )}
    </section>
  );
}

function Stat({ label, value, note }: { label: string; value: string | number; note?: string }) {
  return (
    <div className="builder-summary__stat">
      <span>{label}</span>
      <b>
        {value}
        {note && <small> {note}</small>}
      </b>
    </div>
  );
}

function Group({ title, lines }: { title: string; lines: string[] }) {
  const shown = lines.filter((line) => line.trim().length > 0);
  if (shown.length === 0) {
    return null;
  }
  return (
    <>
      <h4 className="builder-summary__group">{title}</h4>
      {shown.map((line) => (
        <div key={line} className="builder-summary__lines">
          {line}
        </div>
      ))}
    </>
  );
}
