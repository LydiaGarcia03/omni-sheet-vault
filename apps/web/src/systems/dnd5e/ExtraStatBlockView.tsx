import type { ExtraAbilityScore, ExtraStatBlock } from '../../sheet/api';
import { formatNumber } from '../../language/format';
import { ENGLISH_TERMS, useDnd5eTerms, type Dnd5eTerms } from './locales/dnd5eMessages';

type ExtraStatBlockViewProps = {
  name: string;
  statBlock: ExtraStatBlock;
  armorClass: number;
  maxHitPoints: number;
  speed: number;
};

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

function Attribute({ label, value }: { label: string; value: string }) {
  return (
    <div className="extra-stat-block__attribute">
      <span className="extra-stat-block__attribute-label">{label}</span>
      {value}
    </div>
  );
}

function AbilityTable({ abilities }: { abilities: ExtraAbilityScore[] }) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  return (
    <table className="extra-stat-block__ability-table">
      <thead>
        <tr>
          <th aria-label={t('extras.ability')} />
          <th aria-label={t('extras.score')} />
          <th>{t('skills.modColumn')}</th>
          <th>{t('spell.save')}</th>
        </tr>
      </thead>
      <tbody>
        {abilities.map((ability) => (
          <tr key={ability.abilityKey}>
            <th scope="row">{terms.abilityAbbreviation(ability.abilityKey)}</th>
            <td>{ability.score}</td>
            <td>{formatSigned(ability.modifier)}</td>
            <td>{formatSigned(ability.save)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function Entries({ heading, entries }: { heading: string; entries: { name: string; description: string }[] }) {
  if (entries.length === 0) {
    return null;
  }
  return (
    <>
      <h4 className="extra-stat-block__section-heading">{heading}</h4>
      {entries.map((entry) => (
        <p key={entry.name} className="extra-stat-block__entry">
          <em>
            <strong>{entry.name}.</strong>
          </em>{' '}
          {entry.description}
        </p>
      ))}
    </>
  );
}

/** An extra's creature stat block, laid out as D&D Beyond's creature block (name, meta, attributes, ability tables, traits, actions). */
export function ExtraStatBlockView({ name, statBlock, armorClass, maxHitPoints, speed }: ExtraStatBlockViewProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const skills = statBlock.skills.map((skill) => `${skill.name} ${formatSigned(skill.bonus)}`).join(', ');

  return (
    <div className="extra-stat-block">
      <h3 className="extra-stat-block__name">{name}</h3>
      <p className="extra-stat-block__subtitle">
        {statBlock.size} {statBlock.creatureType}, {statBlock.alignment}
      </p>
      <Attribute label={t('terms.labels.armorClassShort')} value={String(armorClass)} />
      <Attribute label={t('terms.labels.initiative')} value={`${formatSigned(statBlock.initiativeBonus)} (${10 + statBlock.initiativeBonus})`} />
      <Attribute label={t('terms.labels.hitPointsShort')} value={`${maxHitPoints} (${statBlock.hitDiceLabel})`} />
      <Attribute label={t('terms.labels.speed')} value={`${terms.feet(speed)}${statBlock.additionalSpeeds ? `, ${statBlock.additionalSpeeds}` : ''}`} />
      <div className="extra-stat-block__abilities">
        <AbilityTable abilities={statBlock.abilityScores.slice(0, 3)} />
        <AbilityTable abilities={statBlock.abilityScores.slice(3, 6)} />
      </div>
      {skills && <Attribute label={t('terms.labels.skills')} value={skills} />}
      <Attribute label={t('terms.labels.senses')} value={statBlock.senses} />
      <Attribute label={t('terms.labels.languages')} value={statBlock.languages} />
      <Attribute label={t('terms.labels.challengeShort')} value={challengeText(statBlock, terms)} />
      <Entries heading={t('terms.labels.traits')} entries={statBlock.traits} />
      <Entries heading={t('terms.labels.actions')} entries={statBlock.actions} />
      <Entries heading={t('terms.labels.reactions')} entries={statBlock.reactions ?? []} />
      <Entries heading={t('terms.labels.legendaryActions')} entries={statBlock.legendaryActions ?? []} />
    </div>
  );
}

/** D&D Beyond's "0 (XP 10; PB +2)"; just the rating when the server gave no XP. */
export function challengeText(statBlock: ExtraStatBlock, terms: Dnd5eTerms = ENGLISH_TERMS): string {
  const { t } = terms;
  if (statBlock.challengeXp == null) {
    return statBlock.challengeRating;
  }
  const bonus =
    statBlock.challengeProficiencyBonus == null
      ? ''
      : t('extras.proficiencyBonus', { pb: t('terms.labels.proficiencyBonusShort'), bonus: formatSigned(statBlock.challengeProficiencyBonus) });
  return t('extras.challengeXp', { rating: statBlock.challengeRating, xp: formatNumber(statBlock.challengeXp, terms.locale), bonus });
}
