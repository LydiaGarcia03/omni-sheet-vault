import type { ExtraAbilityScore, ExtraStatBlock } from '../../sheet/api';

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

const ABILITY_ABBREVIATIONS: Record<string, string> = {
  strength: 'STR',
  dexterity: 'DEX',
  constitution: 'CON',
  intelligence: 'INT',
  wisdom: 'WIS',
  charisma: 'CHA',
};

function Attribute({ label, value }: { label: string; value: string }) {
  return (
    <div className="extra-stat-block__attribute">
      <span className="extra-stat-block__attribute-label">{label}</span> {value}
    </div>
  );
}

function AbilityTable({ abilities }: { abilities: ExtraAbilityScore[] }) {
  return (
    <table className="extra-stat-block__ability-table">
      <thead>
        <tr>
          <th aria-label="Ability" />
          <th aria-label="Score" />
          <th>Mod</th>
          <th>Save</th>
        </tr>
      </thead>
      <tbody>
        {abilities.map((ability) => (
          <tr key={ability.abilityKey}>
            <th scope="row">{ABILITY_ABBREVIATIONS[ability.abilityKey]}</th>
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
  const skills = statBlock.skills.map((skill) => `${skill.name} ${formatSigned(skill.bonus)}`).join(', ');

  return (
    <div className="extra-stat-block">
      <h3 className="extra-stat-block__name">{name}</h3>
      <p className="extra-stat-block__subtitle">
        {statBlock.size} {statBlock.creatureType}, {statBlock.alignment}
      </p>
      <Attribute label="AC" value={String(armorClass)} />
      <Attribute label="Initiative" value={`${formatSigned(statBlock.initiativeBonus)} (${10 + statBlock.initiativeBonus})`} />
      <Attribute label="HP" value={`${maxHitPoints} (${statBlock.hitDiceLabel})`} />
      <Attribute label="Speed" value={`${speed} ft.${statBlock.additionalSpeeds ? `, ${statBlock.additionalSpeeds}` : ''}`} />
      <div className="extra-stat-block__abilities">
        <AbilityTable abilities={statBlock.abilityScores.slice(0, 3)} />
        <AbilityTable abilities={statBlock.abilityScores.slice(3, 6)} />
      </div>
      {skills && <Attribute label="Skills" value={skills} />}
      <Attribute label="Senses" value={statBlock.senses} />
      <Attribute label="Languages" value={statBlock.languages} />
      <Attribute label="CR" value={statBlock.challengeRating} />
      <Entries heading="Traits" entries={statBlock.traits} />
      <Entries heading="Actions" entries={statBlock.actions} />
    </div>
  );
}
