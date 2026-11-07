import type { BuildPlan, DraftPreview } from '../../../builder/draftApi';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import { capitalize, type DaggerheartCatalogue } from '../daggerheartCatalogue';
import { choiceOf, TRAITS, type DaggerheartBuild } from './daggerheartBuild';
import { Crest, DomainIcon, DomainLabel, domainColor } from './DaggerheartParts';

type SummaryProps = {
  build: DaggerheartBuild;
  plan: BuildPlan;
  preview: DraftPreview;
  /** The preview of the option under the pointer; its differences show in purple. */
  hoverPreview: DraftPreview | null;
  name: string;
  portrait: Portrait | null;
  systemId: string;
  catalogue: DaggerheartCatalogue | null;
  lastChange: string | null;
};

/** A preview's summary facts by label ("Evasion" → "13"); a repeated label keeps its first value. */
function facts(preview: DraftPreview | null): Map<string, string> {
  const map = new Map<string, string>();
  preview?.selections?.summary?.facts.forEach((fact) => {
    if (!map.has(fact.label)) {
      map.set(fact.label, fact.text);
    }
  });
  return map;
}

/** The leading number of a fact's text ("7 / 14" → 7, "−1" → -1); null when it has none. */
function numberIn(text: string | undefined, position = 0): number | null {
  const match = text?.replace('−', '-').match(/-?\+?\d+/g)?.[position];
  return match === undefined ? null : Number(match);
}

/** The character so far: crest and name, defenses and resources, traits, the card hand and the gear. */
export function DaggerheartSummary({ build, plan, preview, hoverPreview, name, portrait, systemId, catalogue, lastChange }: SummaryProps) {
  const current = facts(preview);
  const shown = hoverPreview ? facts(hoverPreview) : current;
  const value = (label: string, position = 0) => numberIn(shown.get(label), position);
  const delta = (label: string, position = 0) => {
    if (!hoverPreview) {
      return <div className="delta" />;
    }
    const before = numberIn(current.get(label), position);
    const after = numberIn(shown.get(label), position);
    if (before === null || after === null || before === after) {
      return <div className="delta" />;
    }
    const difference = after - before;
    return (
      <div className="delta">
        <i>
          {difference > 0 ? '+' : '−'}
          {Math.abs(difference)}
        </i>
      </div>
    );
  };
  const stat = (label: string, plate: string, kind = '', position = 0) => (
    <div className="sum-stat">
      <div className={`stat ${kind}`}>{value(label, position) ?? '–'}</div>
      <span className="plate">{plate}</span>
      {delta(label, position)}
    </div>
  );
  const label = (id: string) => {
    const choice = choiceOf(plan, id);
    return choice?.options.find((option) => option.key === choice.selected[0])?.label;
  };
  const playerClass = build.class ? catalogue?.classes.get(build.class) : undefined;
  const ancestryName = build.mixedAncestry ? build.mixedAncestry.name || 'Mixed ancestry' : label('ancestry');
  const subtitle = [label('community'), ancestryName, label('class')].filter(Boolean).join(' ');
  const cards = (build.domainCards ?? []).map((slug) => catalogue?.domainCards.get(slug)).filter((card) => card !== undefined);

  return (
    <aside className="dh-frame navy summary" aria-label="Character summary" data-tutorial="summary">
      <div className="summary__scroll">
        <div className="sum-top">
          {build.class ? <Crest classSlug={build.class} /> : <div className="sum-banner empty" />}
          <div className="sum-side">
            <div className="sum-who">
              <CharacterPortrait portrait={portrait} systemId={systemId} className="pt" />
              <div>
                <h3>{name || 'Unnamed'}</h3>
                <div className="sub">
                  {subtitle || 'No class yet'}
                  {label('subclass') && ` (${label('subclass')})`}
                </div>
              </div>
            </div>
            <div className="sum-doms">
              {playerClass?.data.domains.map((domain) => (
                <DomainLabel key={domain} domain={domain} />
              ))}
            </div>
          </div>
        </div>
        <div className="sum-body">
          <div className="dh-title">
            <span>Defenses &amp; resources</span>
          </div>
          <div className="sum-stats">
            {stat('Evasion', 'Evasion', 'evasion')}
            {stat('Hit Points', 'HP')}
            {stat('Stress', 'Stress')}
            {stat('Hope', 'Hope')}
            {stat('Armor Score', 'Armor', 'armor')}
          </div>
          <div className="sum-thr" aria-label="Damage thresholds">
            <span>Minor</span>
            <b>{value('Damage thresholds', 0) ?? '–'}</b>
            <span>Major</span>
            <b>{value('Damage thresholds', 1) ?? '–'}</b>
            <span>Severe</span>
          </div>
          <div className="dh-title">
            <span>Traits</span>
          </div>
          <div className="sum-traits">
            {TRAITS.map((trait) => (
              <div key={trait.id}>
                <small>{trait.name.slice(0, 3)}</small>
                <div className="stat">{shown.get(trait.name) ?? '–'}</div>
                {delta(trait.name)}
              </div>
            ))}
          </div>
          <div className="dh-title">
            <span>
              Cards <em>{cards.length}/2</em>
            </span>
          </div>
          <div className="sum-hand">
            {(
              [
                ['Class', label('class')],
                ['Subclass', label('subclass')],
                ['Ancestry', ancestryName],
                ['Community', label('community')],
              ] as const
            ).map(([kind, value]) =>
              value ? (
                <div className="mini" key={kind}>
                  <div className="mini__head">{kind}</div>
                  <b>{value}</b>
                </div>
              ) : (
                <div className="mini empty" key={kind}>
                  {kind}
                </div>
              ),
            )}
            {[0, 1].map((index) => {
              const card = cards[index];
              if (!card) {
                return (
                  <div className="mini empty" key={`card-${index}`}>
                    Domain card
                  </div>
                );
              }
              return (
                <div className="mini" key={card.slug} style={{ ['--c' as string]: domainColor(card.data.domain) }}>
                  <div className="mini__head">
                    <DomainIcon domain={card.data.domain} /> {capitalize(card.data.domain)} {card.data.level}
                  </div>
                  <b>{card.name}</b>
                </div>
              );
            })}
          </div>
          <div className="sum-gear">
            <div>
              <span>Weapon</span>
              <b>{[label('primary-weapon'), label('secondary-weapon')].filter(Boolean).join(' + ') || '–'}</b>
            </div>
            <div>
              <span>Armor</span>
              <b>{label('armor') ?? '–'}</b>
            </div>
            {[...shown.entries()]
              .filter(([factLabel]) => factLabel === 'Experience')
              .map(([factLabel, text]) => (
                <div key={factLabel}>
                  <span>Experiences</span>
                  <b>{preview.selections?.summary?.facts.filter((fact) => fact.label === 'Experience').map((fact) => fact.text).join(', ') || text}</b>
                </div>
              ))}
          </div>
          <div className={`last${hoverPreview ? ' preview' : ''}`}>
            {hoverPreview ? (
              <>
                <b>Preview.</b> Purple numbers show what this option would change. Click to pick it.
              </>
            ) : (
              lastChange ?? 'Your picks show up here as you make them.'
            )}
          </div>
        </div>
      </div>
    </aside>
  );
}
