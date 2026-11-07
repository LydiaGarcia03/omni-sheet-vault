import { CharacterPortrait } from '../characters/CharacterPortrait';
import type { Portrait } from '../characters/portrait';
import { useBuilderText } from '../locales/platformMessages';
import { ENGLISH_LABELS, type BuilderLabels } from './builderDefinition';
import type { DraftPreview } from './draftApi';

type BuilderSummaryProps = {
  name: string;
  /** One line naming what the character is so far, e.g. "Mountain Dwarf · Fighter 3 · Folk Hero". */
  description: string;
  pendingCount: number;
  preview: DraftPreview;
  portrait: Portrait | null;
  systemId: string;
  labels?: BuilderLabels;
};

const signed = (value: number) => (value >= 0 ? `+${value}` : `−${Math.abs(value)}`);

const titleCase = (text: string) => text.replace(/\b\w/g, (letter) => letter.toUpperCase());

/** The builder's right column: what the character adds up to so far, recalculated by the API on every save. */
export function BuilderSummary({ name, description, pendingCount, preview, portrait, systemId, labels = ENGLISH_LABELS }: BuilderSummaryProps) {
  const tb = useBuilderText();
  const term = labels.summaryTerm;
  const { vitals } = preview;
  const hasBase = (ability: DraftPreview['abilities'][number]) => ability.contributions.some((part) => part.source === 'Base');
  return (
    <section className="builder-frame builder-frame--side builder-summary" aria-label={tb('summary.aria')}>
      <div className="builder-frame__title">{tb('summary.title')}</div>
      <div className="builder-summary__who">
        <CharacterPortrait portrait={portrait} systemId={systemId} className="builder-summary__portrait" />
        <div>
          <div className="builder-summary__name">{name}</div>
          <div className="builder-muted">
            {vitals && tb('summary.level', { level: vitals.level })}
            {description || tb('summary.nothingYet')}
          </div>
        </div>
      </div>

      <div className="builder-summary__abilities">
        {preview.abilities.map((ability) => (
          <div key={ability.ability} title={ability.contributions.map((part) => `${part.source} ${signed(part.amount)}`).join(' · ')}>
            <span className="builder-summary__ability-key">{labels.abilityShort(ability.ability)}</span>
            <b>{hasBase(ability) ? signed(ability.modifier) : '—'}</b>
            <span className="builder-summary__ability-score">{hasBase(ability) ? ability.score : '—'}</span>
          </div>
        ))}
      </div>

      {pendingCount > 0 && <div className="builder-summary__pending">{tb('summary.choicesLeft', { count: pendingCount })}</div>}

      {vitals ? (
        <>
          <Stat label={term('hitPoints')} value={vitals.hitPoints.value} note={vitals.hitDice} />
          <Stat label={term('armorClass')} value={vitals.armorClass.value} note={vitals.armorClass.contributions.map((part) => part.source).join(' + ')} />
          <Stat label={term('speed')} value={labels.feet(vitals.speed)} />
          <Stat label={`${term('initiative')} · ${term('proficiencyBonus')}`} value={`${signed(vitals.initiative)} · ${signed(vitals.proficiencyBonus)}`} />
          <Stat
            label={term('savingThrows')}
            value={vitals.savingThrows.map((save) => `${labels.abilityShort(save.name.toLowerCase())} ${signed(save.value)}`).join(' · ') || '—'}
          />
          <Stat label={term('passivePerception')} value={vitals.passivePerception} />
          <Group title={tb('summary.attacks')} lines={vitals.attacks.map((attack) => `${attack.name} ${signed(attack.toHit)} · ${attack.damage}`)} />
          <Group title={term('features')} lines={[vitals.features.join(' · ')]} />
          <Group title={term('proficiencies')} lines={[[...vitals.skills, ...vitals.armor, ...vitals.weapons, ...vitals.tools].join(' · ')]} />
          <Group title={term('languages')} lines={[vitals.languages.join(', ')]} />
          <Group
            title={term('defensesSenses')}
            lines={[[...vitals.resistances.map((type) => `${titleCase(type)} ${term('resistance').toLowerCase()}`), ...vitals.senses].join(' · ')]}
          />
        </>
      ) : (
        <p className="builder-muted">{tb('summary.empty')}</p>
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
