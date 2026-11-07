import type { ReactNode } from 'react';
import type { BuilderFrameProps } from '../../../builder/builderDefinition';
import type { BuildPlan, ChoiceOption } from '../../../builder/draftApi';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { classArt } from '../daggerheartArt';
import { capitalize, type ArmorData, type DaggerheartCatalogue, type WeaponData } from '../daggerheartCatalogue';
import { choiceOf, signed, TRAIT_POOL, TRAITS, withClass, type DaggerheartBuild, type TraitId } from './daggerheartBuild';
import { Crest, DomainIcon, DomainLabel, domainColor, FeatureList, Group, Prose, Tile } from './DaggerheartParts';

export type StepBodyProps = Omit<BuilderFrameProps, 'build'> & {
  build: DaggerheartBuild;
  catalogue: DaggerheartCatalogue;
  /** Stores the new build; `note` becomes the summary's "last change" line. */
  change: (build: DaggerheartBuild, note?: string) => void;
  /** Previews a build in the summary without saving it; null ends the preview. */
  hover: (build: DaggerheartBuild | null) => void;
};

/** The current step's choices. */
export function DaggerheartStepBody(props: StepBodyProps) {
  const bodies: Record<string, ReactNode> = {
    basics: <BasicsStep {...props} />,
    class: <ClassStep {...props} />,
    subclass: <SubclassStep {...props} />,
    heritage: <HeritageStep {...props} />,
    traits: <TraitsStep {...props} />,
    equipment: <EquipmentStep {...props} />,
    domains: <DomainCardsStep {...props} />,
    experiences: <ExperiencesStep {...props} />,
    story: <StoryStep {...props} />,
  };
  return <>{bodies[props.stepId]}</>;
}

/** The rail's second line under each step, from what the build has so far. */
export const STEP_SUBTITLES: Record<string, (build: DaggerheartBuild, plan: BuildPlan, name: string) => string> = {
  basics: (_build, _plan, name) => name,
  class: (_build, plan) => selectedLabel(plan, 'class'),
  subclass: (_build, plan) => selectedLabel(plan, 'subclass'),
  heritage: (build, plan) => [selectedLabel(plan, 'community'), build.mixedAncestry ? build.mixedAncestry.name ?? 'Mixed' : selectedLabel(plan, 'ancestry')].filter(Boolean).join(' '),
  traits: (build) => `${Object.keys(build.traits ?? {}).length} of 6 set`,
  equipment: (_build, plan) => [selectedLabel(plan, 'primary-weapon'), selectedLabel(plan, 'armor')].filter(Boolean).join(' · '),
  domains: (_build, plan) => `${choiceOf(plan, 'domain-cards')?.selected.length ?? 0} of 2`,
  experiences: (build) => (build.experiences ?? []).filter(Boolean).join(', '),
  story: () => '',
};

/** What each step is for, shown behind the (i) button; our own words. */
export const STEP_INFO: Record<string, string> = {
  basics: 'Name, pronouns and a portrait. All three can be changed later on the sheet.',
  class: 'Your class sets your starting Evasion and Hit Points and the two domains your cards come from. It also gives a Hope feature, which costs 3 Hope, and its class features.',
  subclass: 'The subclass names the trait you Spellcast with, if any, and gives its foundation features now. Specialization and mastery come with later levels.',
  heritage: 'Ancestry is your lineage and gives two features; community is the culture you grew up in and gives one. A mixed ancestry takes the first feature of one ancestry and the second of another. A transformation is only offered when your GM allows it.',
  traits: 'Place +2, +1, +1, +0, +0 and −1 on the six traits. When you act you roll the two Duality Dice and add the trait that fits the action.',
  equipment: 'Pick Tier 1 gear: a two-handed primary weapon, or a one-handed primary and a one-handed secondary. Magic weapons need a Spellcast trait. Your armor sets your thresholds (its base plus your level) and your Armor Slots. You also start with a torch, 50 feet of rope, basic supplies, a handful of gold, a potion and a class item.',
  domains: "Take two level 1 cards from your class's two domains, one from each or both from one. Up to five cards are active in your loadout; the rest wait in the vault.",
  experiences: 'Two short phrases at +2: a background, a skill, a trait of character. Spend a Hope to add one to a roll it fits. Not too broad ("Lucky") and no powers ("Invulnerable").',
  story: 'Description, background questions and connections with the other characters. None of it changes numbers; all of it can wait.',
};

function selectedLabel(plan: BuildPlan, choiceId: string): string {
  const choice = choiceOf(plan, choiceId);
  return choice?.options.find((option) => option.key === choice.selected[0])?.label ?? '';
}

function options(plan: BuildPlan, choiceId: string): ChoiceOption[] {
  return choiceOf(plan, choiceId)?.options ?? [];
}

// Basics

function BasicsStep({ build, change, name, onNameChange, characterId, systemId, portrait, onPortraitChange }: StepBodyProps) {
  return (
    <div className="basics">
      <div className="basics__fields">
        <Group title="Character name">
          <input className="field" value={name} aria-label="Character name" onChange={(event) => onNameChange(event.target.value)} />
        </Group>
        <Group title="Pronouns">
          <input className="field" value={build.pronouns ?? ''} aria-label="Pronouns" onChange={(event) => change({ ...build, pronouns: event.target.value })} />
        </Group>
      </div>
      <div className="portrait-pick">
        <PortraitPicker characterId={characterId} systemId={systemId} portrait={portrait} onChange={onPortraitChange} />
      </div>
    </div>
  );
}

// Class

function ClassStep({ build, plan, catalogue, change, hover }: StepBodyProps) {
  const chosen = build.class ? catalogue.classes.get(build.class) : undefined;
  const meta = (slug: string) => {
    const data = catalogue.classes.get(slug)?.data;
    if (!data) {
      return null;
    }
    return (
      <span className="cls-meta">
        <span className="cls-meta__row">
          {data.domains.map((domain) => (
            <DomainLabel key={domain} domain={domain} />
          ))}
        </span>
        <span className="cls-meta__row">
          <span>Evasion {data.startingEvasion}</span>
          <span>HP {data.startingHitPoints}</span>
        </span>
      </span>
    );
  };
  const expansion = (slug: string) => catalogue.classes.get(slug)?.tags.includes('expansion');
  return (
    <>
      {chosen && (
        <div className="cls-hero" style={{ backgroundImage: `url(${classArt(chosen.slug).art})` }}>
          <Crest classSlug={chosen.slug} />
          {expansion(chosen.slug) && <span className="cls-hf">Hope &amp; Fear</span>}
          <div className="cls-hero__text">
            {meta(chosen.slug)}
            <dl>
              <dt>Hope feature</dt>
              <dd>{chosen.data.hopeFeature.name} · 3 Hope</dd>
              <dt>Class feature{chosen.data.features.length > 1 ? 's' : ''}</dt>
              <dd>{chosen.data.features.map((feature) => feature.name).join(' · ')}</dd>
              <dt>Class items</dt>
              <dd>{chosen.data.classItems}</dd>
            </dl>
          </div>
        </div>
      )}
      {chosen && (
        <div className="detail">
          <FeatureList features={[chosen.data.hopeFeature, ...chosen.data.features]} />
        </div>
      )}
      <div className="group__head spaced">
        <h3>{chosen ? 'Change class' : 'Choose a class'}</h3>
        <span className="note">hover to preview in the summary</span>
      </div>
      <div className="cls-grid">
        {options(plan, 'class').map((option) => {
          const { art, banner } = classArt(option.key);
          const on = build.class === option.key;
          return (
            <button
              key={option.key}
              type="button"
              className={`cls-tile${on ? ' on' : ''}`}
              aria-pressed={on}
              style={{ backgroundImage: art ? `url(${art})` : undefined }}
              onClick={() => change(withClass(build, option.key), `${option.label}: ${option.summary}.`)}
              onMouseEnter={() => hover(withClass(build, option.key))}
              onMouseLeave={() => hover(null)}
            >
              {banner && <span className="cls-tile__banner" style={{ backgroundImage: `url(${banner})` }} />}
              <span className="cls-tile__name">{option.label}</span>
              {meta(option.key)}
              {on && expansion(option.key) && <span className="cls-hf">Hope &amp; Fear</span>}
            </button>
          );
        })}
      </div>
    </>
  );
}

// Subclass

function SubclassStep({ build, plan, catalogue, change, hover }: StepBodyProps) {
  const choices = options(plan, 'subclass');
  if (!build.class) {
    return <p className="muted">Pick a class first.</p>;
  }
  const chosen = build.subclass ? catalogue.subclasses.get(build.subclass) : undefined;
  return (
    <>
      <div className="tiles two">
        {choices.map((option) => {
          const entry = catalogue.subclasses.get(option.key);
          return (
            <Tile
              key={option.key}
              on={build.subclass === option.key}
              title={option.label}
              onPick={() => change({ ...build, subclass: option.key }, `${option.label}: ${option.summary}.`)}
              onHover={(on) => hover(on ? { ...build, subclass: option.key } : null)}
            >
              <Prose text={entry?.description.value} />
              <br />
              <span className="tile__facts">
                {option.summary} · {entry?.data.foundation.map((feature) => feature.name).join(' · ')}
              </span>
            </Tile>
          );
        })}
      </div>
      {chosen && (
        <div className="detail">
          <h4>{chosen.name} foundation</h4>
          <FeatureList features={chosen.data.foundation} />
        </div>
      )}
    </>
  );
}

// Heritage

function HeritageStep({ build, plan, catalogue, change, hover }: StepBodyProps) {
  const mixed = !!build.mixedAncestry;
  const ancestry = build.ancestry ? catalogue.ancestries.get(build.ancestry) : undefined;
  const community = build.community ? catalogue.communities.get(build.community) : undefined;
  const transformation = build.transformation ? catalogue.transformations.get(build.transformation) : undefined;
  const ancestries = options(plan, 'ancestry');
  const setMixed = (patch: { name?: string; top?: string; bottom?: string }) =>
    change({ ...build, ancestry: '', mixedAncestry: { ...build.mixedAncestry, ...patch } }, 'Mixed ancestry updated.');
  const mixedFeature = (slug: string | undefined, position: 0 | 1) => (slug ? catalogue.ancestries.get(slug)?.data.features[position] : undefined);
  return (
    <>
      <Group
        title="Ancestry"
        note={
          <label className="toggle">
            <input
              type="checkbox"
              checked={mixed}
              onChange={(event) => change(event.target.checked ? { ...build, ancestry: '', mixedAncestry: { name: '', top: '', bottom: '' } } : { ...build, mixedAncestry: null })}
            />
            Mixed ancestry
          </label>
        }
      >
        {mixed ? (
          <div className="mixed">
            <label>
              <span>Heritage name</span>
              <input className="field" placeholder="e.g. Goblin-Orc" value={build.mixedAncestry?.name ?? ''} onChange={(event) => setMixed({ name: event.target.value })} />
            </label>
            {(['top', 'bottom'] as const).map((part, position) => (
              <label key={part}>
                <span>{part === 'top' ? 'First feature from' : 'Second feature from'}</span>
                <select className="field" value={build.mixedAncestry?.[part] ?? ''} onChange={(event) => setMixed({ [part]: event.target.value })}>
                  <option value="">Choose an ancestry</option>
                  {ancestries.map((option) => (
                    <option key={option.key} value={option.key}>
                      {option.label} — {catalogue.ancestries.get(option.key)?.data.features[position]?.name}
                    </option>
                  ))}
                </select>
              </label>
            ))}
            <div className="detail">
              <FeatureList
                features={[mixedFeature(build.mixedAncestry?.top, 0), mixedFeature(build.mixedAncestry?.bottom, 1)].filter((feature) => feature !== undefined)}
              />
            </div>
          </div>
        ) : (
          <>
            <div className="tiles four">
              {ancestries.map((option) => (
                <Tile
                  key={option.key}
                  on={build.ancestry === option.key}
                  title={option.label}
                  onPick={() => change({ ...build, ancestry: option.key, mixedAncestry: null }, `${option.label}: ${option.summary}.`)}
                  onHover={(on) => hover(on ? { ...build, ancestry: option.key, mixedAncestry: null } : null)}
                />
              ))}
            </div>
            {ancestry && (
              <div className="detail">
                <h4>
                  {ancestry.name}
                  {ancestry.data.family && <small> · {ancestry.data.family.name}</small>}
                </h4>
                <FeatureList features={ancestry.data.features} />
              </div>
            )}
          </>
        )}
      </Group>
      <Group title="Community">
        <div className="tiles four">
          {options(plan, 'community').map((option) => (
            <Tile
              key={option.key}
              on={build.community === option.key}
              title={option.label}
              onPick={() => change({ ...build, community: option.key }, `${option.label}: ${option.summary}.`)}
              onHover={(on) => hover(on ? { ...build, community: option.key } : null)}
            />
          ))}
        </div>
        {community && (
          <div className="detail">
            <h4>{community.name}</h4>
            <FeatureList features={[community.data.feature]} />
            <p className="adjectives">Often {community.data.adjectives.join(', ')}.</p>
          </div>
        )}
      </Group>
      <Group
        title="Transformation"
        note={
          <label className="toggle">
            <input
              type="checkbox"
              checked={!!build.transformationsAllowed}
              onChange={(event) => change({ ...build, transformationsAllowed: event.target.checked, transformation: event.target.checked ? build.transformation : '' })}
            />
            GM allows transformations
          </label>
        }
      >
        {build.transformationsAllowed ? (
          <>
            <div className="tiles three">
              {options(plan, 'transformation').map((option) => (
                <Tile
                  key={option.key}
                  on={build.transformation === option.key}
                  title={option.label}
                  onPick={() =>
                    change({ ...build, transformation: build.transformation === option.key ? '' : option.key }, `${option.label}: ${option.summary}.`)
                  }
                  onHover={(on) => hover(on ? { ...build, transformation: option.key } : null)}
                >
                  {option.summary}
                </Tile>
              ))}
            </div>
            {transformation && (
              <div className="detail">
                <h4>{transformation.name}</h4>
                <FeatureList features={transformation.data.features} />
              </div>
            )}
          </>
        ) : (
          <p className="muted">Optional. A transformation (demigod, vampire, werewolf…) is a benefit with a drawback, and only with the GM's say-so.</p>
        )}
      </Group>
    </>
  );
}

// Traits

function TraitsStep({ build, change, hover }: StepBodyProps) {
  const traits = build.traits ?? {};
  const left = [...TRAIT_POOL];
  Object.values(traits).forEach((value) => {
    const at = left.indexOf(value as number);
    if (at >= 0) {
      left.splice(at, 1);
    }
  });
  const available = (value: number) => left.includes(value);
  const withTrait = (id: TraitId, value: number): DaggerheartBuild => {
    const next = { ...traits };
    if (next[id] === value) {
      delete next[id];
    } else {
      next[id] = value;
    }
    return { ...build, traits: next };
  };
  const placed = (value: number) => Object.values(traits).filter((assigned) => assigned === value).length;
  const seen = new Map<number, number>();
  return (
    <>
      <div className="pool">
        To place
        {TRAIT_POOL.map((value, index) => {
          seen.set(value, (seen.get(value) ?? 0) + 1);
          const used = (seen.get(value) ?? 0) <= placed(value);
          return (
            <span key={index} className={used ? 'used' : ''}>
              {signed(value)}
            </span>
          );
        })}
      </div>
      <div className="trait-panel">
        {TRAITS.map((trait) => (
          <div className="trow" key={trait.id}>
            <div>
              <b>{trait.name}</b>
              <small>{trait.verbs}</small>
            </div>
            <div className="seg" role="group" aria-label={trait.name}>
              {[2, 1, 0, -1].map((value) => {
                const on = traits[trait.id] === value;
                return (
                  <button
                    key={value}
                    type="button"
                    className={on ? 'on' : ''}
                    aria-pressed={on}
                    disabled={!on && !available(value)}
                    onClick={() => change(withTrait(trait.id, value), `${trait.name} ${on ? 'cleared' : `set to ${signed(value)}`}.`)}
                    onMouseEnter={() => hover(withTrait(trait.id, value))}
                    onMouseLeave={() => hover(null)}
                  >
                    {signed(value)}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

// Equipment

function weaponLine(data: WeaponData): string {
  const bonus = data.damage.bonus > 0 ? `+${data.damage.bonus}` : '';
  return `${data.trait} · ${data.range} · ${data.damage.die}${bonus} ${data.damage.type === 'physical' ? 'phy' : 'mag'} · ${data.burden === 2 ? 'two-handed' : 'one-handed'}`;
}

/** Physical and magic weapons as the SRD's tables split them; magic ones only appear for a spellcaster. */
function WeaponGroups({ physical, magic }: { physical: ReactNode[]; magic: ReactNode[] }) {
  return (
    <>
      <div className="weapon-kind">Physical</div>
      <div className="tiles two">{physical}</div>
      {magic.length > 0 && (
        <>
          <div className="weapon-kind">Magic</div>
          <div className="tiles two">{magic}</div>
        </>
      )}
    </>
  );
}

function EquipmentStep({ build, plan, catalogue, change, hover }: StepBodyProps) {
  const itemData = <T,>(slug: string) => catalogue.items.get(slug)?.data as T | undefined;
  const pick = (field: keyof DaggerheartBuild, key: string, clear: Partial<DaggerheartBuild> = {}) => ({ ...build, ...clear, [field]: build[field] === key ? '' : key });
  const weaponTiles = (choiceId: string, field: 'primaryWeapon' | 'secondaryWeapon', magic: boolean) =>
    options(plan, choiceId)
      .filter((option) => (itemData<WeaponData>(option.key)?.damage.type !== 'physical') === magic)
      .map((option) => {
      const data = itemData<WeaponData>(option.key);
      const clear = field === 'primaryWeapon' && data?.burden === 2 ? { secondaryWeapon: '' } : {};
      return (
        <Tile
          key={option.key}
          on={build[field] === option.key}
          title={option.label}
          onPick={() => change(pick(field, option.key, clear), `${option.label}: ${data ? weaponLine(data) : option.summary}.`)}
          onHover={(on) => hover(on ? pick(field, option.key, clear) : null)}
        >
          {data ? weaponLine(data) : option.summary}
          {data?.feature && (
            <span className="tile__facts tile__clamp" title={`${data.feature.name}: ${data.feature.text}`}>
              {data.feature.name}: {data.feature.text}
            </span>
          )}
        </Tile>
      );
    });
  const primary = choiceOf(plan, 'primary-weapon');
  const secondary = choiceOf(plan, 'secondary-weapon');
  return (
    <>
      <Group title="Primary weapon" note={primary?.optional ? 'optional: unarmed, you fight with your class feature' : 'Tier 1'}>
        <WeaponGroups physical={weaponTiles('primary-weapon', 'primaryWeapon', false)} magic={weaponTiles('primary-weapon', 'primaryWeapon', true)} />
      </Group>
      {secondary && (
        <Group title="Secondary weapon" note="your primary is one-handed">
          <WeaponGroups physical={weaponTiles('secondary-weapon', 'secondaryWeapon', false)} magic={weaponTiles('secondary-weapon', 'secondaryWeapon', true)} />
        </Group>
      )}
      <Group title="Armor" note="Tier 1">
        <div className="tiles two">
          {options(plan, 'armor').map((option) => {
            const data = itemData<ArmorData>(option.key);
            return (
              <Tile
                key={option.key}
                on={build.armor === option.key}
                title={option.label}
                onPick={() => change(pick('armor', option.key), `${option.label}: ${option.summary}.`)}
                onHover={(on) => hover(on ? pick('armor', option.key) : null)}
              >
                {option.summary}
                {data?.feature && ` · ${data.feature.name}: ${data.feature.text}`}
              </Tile>
            );
          })}
        </div>
      </Group>
      <Group title="Potion" note="pick one">
        <div className="tiles two">
          {options(plan, 'potion').map((option) => (
            <Tile key={option.key} on={build.potion === option.key} title={option.label} onPick={() => change({ ...build, potion: option.key }, `${option.label} packed.`)}>
              <Prose text={option.summary} />
            </Tile>
          ))}
        </div>
      </Group>
      <Group title="Class item" note="pick one">
        {options(plan, 'class-item').length === 0 ? (
          <p className="muted">Pick a class first.</p>
        ) : (
          <div className="tiles two">
            {options(plan, 'class-item').map((option) => (
              <Tile key={option.key} on={build.classItem === option.key} title={option.label} onPick={() => change({ ...build, classItem: option.key }, `${option.label} packed.`)} />
            ))}
          </div>
        )}
      </Group>
      <p className="muted">Also packed: a torch, 50 feet of rope, basic supplies and a handful of gold.</p>
    </>
  );
}

// Domain cards

function DomainCardsStep({ build, plan, catalogue, change, hover }: StepBodyProps) {
  if (!build.class) {
    return <p className="muted">Pick a class first.</p>;
  }
  const picked = build.domainCards ?? [];
  const toggle = (key: string) => {
    const has = picked.includes(key);
    if (!has && picked.length >= 2) {
      return null;
    }
    return { ...build, domainCards: has ? picked.filter((card) => card !== key) : [...picked, key] };
  };
  return (
    <div className="cards3">
      {options(plan, 'domain-cards').map((option) => {
        const entry = catalogue.domainCards.get(option.key);
        const data = entry?.data;
        const on = picked.includes(option.key);
        const next = toggle(option.key);
        return (
          <button
            key={option.key}
            type="button"
            className={`dcard${on ? ' is-selected' : ''}`}
            aria-pressed={on}
            disabled={!next}
            onClick={() => next && change(next, `${option.label} ${on ? 'left' : 'is now in'} your loadout.`)}
            onMouseEnter={() => next && hover(next)}
            onMouseLeave={() => hover(null)}
          >
            {data && (
              <span className="dcard__head" style={{ background: domainColor(data.domain) }}>
                <DomainIcon domain={data.domain} /> {capitalize(data.domain)} · Lv {data.level}
                <span className="recall" title="Recall Cost">
                  ⟲ {data.recallCost}
                </span>
              </span>
            )}
            <span className="pick">{on ? '✓' : '+'}</span>
            <span className="dcard__name">{option.label}</span>
            <span className="dcard__type">{data?.type}</span>
            <span className="dcard__text">
              <Prose text={entry?.description.value} />
            </span>
          </button>
        );
      })}
    </div>
  );
}

// Experiences

function ExperiencesStep({ build, plan, change }: StepBodyProps) {
  const experiences = [...(build.experiences ?? []), '', ''].slice(0, 2);
  const boosted = choiceOf(plan, 'boosted-experience');
  const setExperience = (index: number, value: string) => {
    const next = [...experiences];
    next[index] = value;
    change({ ...build, experiences: next });
  };
  return (
    <>
      {experiences.map((experience, index) => (
        <Group key={index} title={`Experience ${index + 1}`} note={<span className={`dh-badge${experience.trim() ? ' done' : ''}`}>+{build.boostedExperience === index && boosted ? 3 : 2}</span>}>
          <input
            className="field"
            aria-label={`Experience ${index + 1}`}
            placeholder={index === 0 ? 'e.g. Bounty Hunter, Field Medic' : 'e.g. Silver Tongue, I’ve Got Your Back'}
            value={experience}
            onChange={(event) => setExperience(index, event.target.value)}
          />
        </Group>
      ))}
      {boosted && (
        <Group title={boosted.prompt} note="your ancestry's feature adds +1 to one">
          <div className="tiles two">
            {experiences.map((experience, index) => (
              <Tile key={index} on={build.boostedExperience === index} title={experience || `Experience ${index + 1}`} disabled={!experience.trim()} onPick={() => change({ ...build, boostedExperience: index })} />
            ))}
          </div>
        </Group>
      )}
    </>
  );
}

// Story

function StoryStep({ build, catalogue, change }: StepBodyProps) {
  const story = build.story ?? {};
  const playerClass = build.class ? catalogue.classes.get(build.class) : undefined;
  const questions = playerClass?.data ?? { backgroundQuestions: [], connectionQuestions: [] };
  const field = (key: 'description' | 'background' | 'connections', label: string, rows: number, prompts?: string[]) => (
    <Group title={label}>
      {prompts && prompts.length > 0 && (
        <ul className="prompts">
          {prompts.map((prompt) => (
            <li key={prompt}>{prompt}</li>
          ))}
        </ul>
      )}
      <textarea className="field" rows={rows} aria-label={label} value={story[key] ?? ''} onChange={(event) => change({ ...build, story: { ...story, [key]: event.target.value } })} />
    </Group>
  );
  return (
    <>
      {field('description', 'Description', 2)}
      {field('background', 'Background questions', 4, questions.backgroundQuestions)}
      {field('connections', 'Connections', 3, questions.connectionQuestions)}
    </>
  );
}
