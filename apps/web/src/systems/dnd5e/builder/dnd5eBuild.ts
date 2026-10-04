import type { BuilderStep } from '../../../builder/builderDefinition';
import type { BuildPlan, CreationChoice } from '../../../builder/draftApi';

export type Dnd5eAbilityScoreMethod = 'STANDARD_ARRAY' | 'POINT_BUY' | 'MANUAL';

export type Dnd5eBuildClass = {
  classSlug: string;
  subclassSlug: string | null;
  level: number;
};

export type Dnd5eAbilityAdjustment = {
  otherModifier: number | null;
  overrideScore: number | null;
};

export type Dnd5eBuildChoice = {
  id: string;
  selections: string[];
};

/** Mirrors the API's `Dnd5eCharacterBuild`; a draft leaves the structure null until it's chosen. */
export type Dnd5eBuild = {
  speciesSlug: string | null;
  subspeciesName: string | null;
  speciesVariantName: string | null;
  backgroundSlug: string | null;
  classes: Dnd5eBuildClass[];
  abilityScoreMethod: Dnd5eAbilityScoreMethod | null;
  baseAbilityScores: Record<string, number>;
  hitPointMethod: 'FIXED' | 'ROLLED';
  /** One result per level after the first, in the plan's row order; null for a level not rolled yet. */
  rolledHitPoints: (number | null)[];
  choices: Dnd5eBuildChoice[];
  /** Missing on drafts saved before manual corrections existed. */
  abilityScoreAdjustments?: Record<string, Dnd5eAbilityAdjustment>;
  preferences?: Dnd5ePreferences;
  /** Keys of the starting-item lines that begin worn or wielded ("dagger#1"); missing on older drafts. */
  equippedStartingItems?: string[];
};

/**
 * Mirrors the API's `Dnd5ePreferences`. `sources` null means every imported book; playtest books follow
 * `playtestContent` instead, and partnered books follow `partneredContent` and `partners` (null = every partner).
 */
export type Dnd5ePreferences = {
  sources: string[] | null;
  playtestContent: boolean;
  partneredContent: boolean;
  partners: string[] | null;
  optionalClassFeatures: boolean;
  featPrerequisites: boolean;
  multiclassPrerequisites: boolean;
  advancement: 'MILESTONE' | 'XP';
  encumbrance: 'STANDARD' | 'NONE';
  ignoreCoinWeight: boolean;
};

export const LOCKED_SOURCES = ["Player's Handbook", "Dungeon Master's Guide"];

/** Source codes of the books listed first among the optional ones, in this order. */
export const FEATURED_SOURCE_CODES = ['XGE', 'TCE', 'GGR', 'ERLW', 'SCAG'];

/** Optional books for the sources menu, playtest and partnered ones left out: the featured ones first, then the rest alphabetically. */
export function orderOptionalSources<T extends { sourceBook: string; sourceCode: string | null; playtest?: boolean; partner?: string | null }>(
  sources: T[],
): T[] {
  const rank = (source: T) => {
    const index = FEATURED_SOURCE_CODES.indexOf(source.sourceCode ?? '');
    return index === -1 ? FEATURED_SOURCE_CODES.length : index;
  };
  return sources
    .filter((source) => !LOCKED_SOURCES.includes(source.sourceBook) && !source.playtest && !source.partner)
    .sort((a, b) => rank(a) - rank(b) || a.sourceBook.localeCompare(b.sourceBook));
}

const DEFAULT_PREFERENCES: Dnd5ePreferences = {
  sources: null,
  playtestContent: false,
  partneredContent: true,
  partners: null,
  optionalClassFeatures: false,
  featPrerequisites: true,
  multiclassPrerequisites: true,
  advancement: 'MILESTONE',
  encumbrance: 'STANDARD',
  ignoreCoinWeight: true,
};

export function preferencesOf(build: Dnd5eBuild): Dnd5ePreferences {
  return { ...DEFAULT_PREFERENCES, ...build.preferences };
}

export function withPreferences(build: Dnd5eBuild, change: Partial<Dnd5ePreferences>): Dnd5eBuild {
  return { ...build, preferences: { ...preferencesOf(build), ...change } };
}

/** Turns one book on or off; `allBooks` expands "every book" into an explicit list the first time one is turned off. */
export function withSourceToggled(build: Dnd5eBuild, sourceBook: string, allBooks: string[]): Dnd5eBuild {
  const current = preferencesOf(build).sources ?? allBooks;
  const sources = current.includes(sourceBook) ? current.filter((book) => book !== sourceBook) : [...current, sourceBook];
  return withPreferences(build, { sources });
}

/**
 * Turns one partner on or off; `allPartners` expands "every partner" into an explicit list the first time one is
 * turned off, and a list holding every partner again collapses back to "every partner".
 */
export function withPartnerToggled(build: Dnd5eBuild, partner: string, allPartners: string[]): Dnd5eBuild {
  const current = preferencesOf(build).partners ?? allPartners;
  const partners = current.includes(partner) ? current.filter((name) => name !== partner) : [...current, partner];
  return withPreferences(build, { partners: allPartners.every((name) => partners.includes(name)) ? null : partners });
}

/** Sets one level's rolled hit points (null clears it), keeping the list exactly `levels` long. */
export function withRolledHitPoint(build: Dnd5eBuild, index: number, value: number | null, levels: number): Dnd5eBuild {
  const rolled = Array.from({ length: levels }, (_, row) => build.rolledHitPoints[row] ?? null);
  rolled[index] = value;
  return { ...build, rolledHitPoints: rolled };
}

/** Makes one starting-item line, by its key, begin equipped or not. */
export function withStartingItemEquipped(build: Dnd5eBuild, lineKey: string, equipped: boolean): Dnd5eBuild {
  const current = (build.equippedStartingItems ?? []).filter((key) => key !== lineKey);
  return { ...build, equippedStartingItems: equipped ? [...current, lineKey] : current };
}

export function withHitPointMethod(build: Dnd5eBuild, method: 'FIXED' | 'ROLLED'): Dnd5eBuild {
  return { ...build, hitPointMethod: method };
}

export const ABILITIES = ['strength', 'dexterity', 'constitution', 'intelligence', 'wisdom', 'charisma'] as const;

export const DND5E_STEPS: BuilderStep[] = [
  { id: 'basics', label: 'Basics' },
  { id: 'species', label: 'Species' },
  { id: 'classes', label: 'Classes' },
  { id: 'background', label: 'Background' },
  { id: 'abilities', label: 'Abilities' },
  { id: 'equipment', label: 'Equipment' },
  { id: 'review', label: 'Review' },
];

const SPECIES_STRUCTURE = new Set(['build.species', 'build.subspecies', 'build.variant']);

const CHECKLIST_GROUPS: [string, string[]][] = [
  ['Species', ['SPECIES', 'SUBSPECIES', 'SPECIES_VARIANT', 'SIZE']],
  ['Class', ['CLASSES']],
  ['Subclass', ['SUBCLASS']],
  ['Background', ['BACKGROUND']],
  ['Ability scores', ['ABILITY_SCORES']],
  ['Hit points', ['ROLLED_HIT_POINTS']],
  ['Ability increases & feats', ['ASI_OR_FEAT', 'ABILITY_SCORE', 'FEAT']],
  ['Proficiencies', ['SKILL', 'TOOL', 'LANGUAGE', 'WEAPON', 'ARMOR', 'SAVING_THROW', 'EXPERTISE', 'SKILL_TOOL_LANGUAGE', 'RESISTANCE']],
  ['Class features', ['FEATURE_OPTION', 'OPTIONAL_FEATURE', 'ALTERNATIVE']],
  ['Spells', ['CANTRIP', 'SPELL', 'SPELLBOOK', 'PREPARED_SPELL', 'SPELL_SET', 'SPELLCASTING_ABILITY']],
  ['Starting equipment', ['EQUIPMENT_METHOD', 'EQUIPMENT', 'EQUIPMENT_ITEM']],
];
const GROUP_BY_TYPE = new Map(CHECKLIST_GROUPS.flatMap(([group, types]) => types.map((type) => [type, group] as const)));
const GROUP_ORDER = CHECKLIST_GROUPS.map(([group]) => group);

/** The checklist line a choice counts toward, e.g. every cantrip, spell and spellbook pick is one "Spells" line. */
export function dnd5eChecklistGroupOf(choice: CreationChoice): string {
  return GROUP_BY_TYPE.get(choice.type) ?? 'Other choices';
}

/** Checklist lines in a fixed order, so "Class" always comes before "Spells". */
export function dnd5eChecklistGroupRank(group: string): number {
  const rank = GROUP_ORDER.indexOf(group);
  return rank >= 0 ? rank : GROUP_ORDER.length;
}

export function dnd5eStepOf(choice: CreationChoice): string {
  const { id } = choice;
  if (id === 'build.abilityScores') {
    return 'abilities';
  }
  if (id.includes(':equipment')) {
    return 'equipment';
  }
  if (SPECIES_STRUCTURE.has(id) || id.startsWith('species:')) {
    return 'species';
  }
  if (id === 'build.background' || id.startsWith('background:')) {
    return 'background';
  }
  return 'classes';
}

const SUBCLASS_CHOICE = /^build\.class\.(.+)\.subclass$/;

/** The build's own answer to a choice, read the way `withSelection` writes it; undefined for choices the build doesn't store as a pick. */
export function answerOf(build: Dnd5eBuild, choiceId: string): string[] | undefined {
  const field = (value: string | null) => (value ? [value] : []);
  switch (choiceId) {
    case 'build.species':
      return field(build.speciesSlug);
    case 'build.subspecies':
      return field(build.subspeciesName);
    case 'build.variant':
      return field(build.speciesVariantName);
    case 'build.background':
      return field(build.backgroundSlug);
  }
  const subclass = SUBCLASS_CHOICE.exec(choiceId);
  if (subclass) {
    const entry = build.classes.find((candidate) => candidate.classSlug === subclass[1]);
    return entry ? field(entry.subclassSlug) : undefined;
  }
  if (choiceId.startsWith('build.')) {
    return undefined;
  }
  return build.choices.find((choice) => choice.id === choiceId)?.selections ?? [];
}

/** Applies a pick to the build: `build.*` choices set a build field, every other choice is stored as an answer. */
export function withSelection(build: Dnd5eBuild, choiceId: string, selections: string[]): Dnd5eBuild {
  const first = selections[0] ?? null;
  switch (choiceId) {
    case 'build.species':
      return { ...build, speciesSlug: first, subspeciesName: null, speciesVariantName: null };
    case 'build.subspecies':
      return { ...build, subspeciesName: first, speciesVariantName: null };
    case 'build.variant':
      return { ...build, speciesVariantName: first };
    case 'build.background':
      return { ...build, backgroundSlug: first };
  }
  const subclass = SUBCLASS_CHOICE.exec(choiceId);
  if (subclass) {
    return {
      ...build,
      classes: build.classes.map((entry) => (entry.classSlug === subclass[1] ? { ...entry, subclassSlug: first } : entry)),
    };
  }
  const others = build.choices.filter((choice) => choice.id !== choiceId);
  return { ...build, choices: selections.length === 0 ? others : [...others, { id: choiceId, selections }] };
}

/** Drops answers to choices the plan no longer offers, e.g. after the species or a class changed. */
export function withoutStaleAnswers(build: Dnd5eBuild, plan: BuildPlan): Dnd5eBuild {
  const offered = new Set(plan.choices.map((choice) => choice.id));
  const kept = build.choices.filter((choice) => offered.has(choice.id));
  return kept.length === build.choices.length ? build : { ...build, choices: kept };
}

export function withClassAdded(build: Dnd5eBuild, classSlug: string): Dnd5eBuild {
  if (build.classes.some((entry) => entry.classSlug === classSlug)) {
    return build;
  }
  return { ...build, classes: [...build.classes, { classSlug, subclassSlug: null, level: 1 }] };
}

export function withClassRemoved(build: Dnd5eBuild, classSlug: string): Dnd5eBuild {
  return { ...build, classes: build.classes.filter((entry) => entry.classSlug !== classSlug) };
}

export function withClassLevel(build: Dnd5eBuild, classSlug: string, level: number): Dnd5eBuild {
  return { ...build, classes: build.classes.map((entry) => (entry.classSlug === classSlug ? { ...entry, level } : entry)) };
}

/** Switching method starts the scores over, every ability at `startingScore` (point buy's cheapest) or unset. */
export function withAbilityScoreMethod(build: Dnd5eBuild, method: Dnd5eAbilityScoreMethod | null, startingScore: number | null): Dnd5eBuild {
  const baseAbilityScores = startingScore === null ? {} : Object.fromEntries(ABILITIES.map((ability) => [ability, startingScore]));
  return { ...build, abilityScoreMethod: method, baseAbilityScores };
}

export function withAbilityAdjustment(build: Dnd5eBuild, ability: string, adjustment: Dnd5eAbilityAdjustment): Dnd5eBuild {
  const adjustments = { ...(build.abilityScoreAdjustments ?? {}) };
  if (adjustment.otherModifier === null && adjustment.overrideScore === null) {
    delete adjustments[ability];
  } else {
    adjustments[ability] = adjustment;
  }
  return { ...build, abilityScoreAdjustments: adjustments };
}

export function withAbilityScore(build: Dnd5eBuild, ability: string, score: number | null): Dnd5eBuild {
  const scores = { ...build.baseAbilityScores };
  if (score === null) {
    delete scores[ability];
  } else {
    scores[ability] = score;
  }
  return { ...build, baseAbilityScores: scores };
}

/** "Mountain Dwarf · Fighter 3 / Rogue 1 · Folk Hero", from whatever is chosen so far. */
export function describeBuild(build: Dnd5eBuild, plan: BuildPlan): string {
  const label = (choiceId: string, key: string | null) =>
    key === null ? null : (plan.choices.find((choice) => choice.id === choiceId)?.options.find((option) => option.key === key)?.label ?? key);
  const species = label('build.species', build.speciesSlug);
  const subspecies = label('build.subspecies', build.subspeciesName);
  const classes = build.classes.map((entry) => `${label('build.classes', entry.classSlug)} ${entry.level}`).join(' / ');
  const parts = [
    species && (subspecies && subspecies !== 'Standard' ? `${subspecies} ${species}` : species),
    classes,
    label('build.background', build.backgroundSlug),
  ];
  return parts.filter(Boolean).join(' · ');
}

export function characterLevel(build: Dnd5eBuild): number {
  return build.classes.reduce((total, entry) => total + entry.level, 0);
}
