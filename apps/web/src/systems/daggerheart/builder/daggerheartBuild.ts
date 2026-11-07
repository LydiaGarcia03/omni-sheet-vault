import type { BuilderStep } from '../../../builder/builderDefinition';
import type { BuildPlan, CreationChoice } from '../../../builder/draftApi';

/** The Daggerheart build document, as the API's DaggerheartBuild reads it. */
export type DaggerheartBuild = {
  class?: string;
  subclass?: string;
  ancestry?: string;
  mixedAncestry?: { name?: string; top?: string; bottom?: string } | null;
  community?: string;
  transformationsAllowed?: boolean;
  transformation?: string;
  traits?: Partial<Record<TraitId, number>>;
  experiences?: string[];
  boostedExperience?: number | null;
  domainCards?: string[];
  primaryWeapon?: string;
  secondaryWeapon?: string;
  armor?: string;
  potion?: string;
  classItem?: string;
  pronouns?: string;
  story?: { description?: string; background?: string; connections?: string };
};

export type TraitId = 'agility' | 'strength' | 'finesse' | 'instinct' | 'presence' | 'knowledge';

/** The six traits with the verbs the SRD lists for each (p. 4). */
export const TRAITS: { id: TraitId; name: string; verbs: string }[] = [
  { id: 'agility', name: 'Agility', verbs: 'Sprint, Leap, Maneuver' },
  { id: 'strength', name: 'Strength', verbs: 'Lift, Smash, Grapple' },
  { id: 'finesse', name: 'Finesse', verbs: 'Control, Hide, Tinker' },
  { id: 'instinct', name: 'Instinct', verbs: 'Perceive, Sense, Navigate' },
  { id: 'presence', name: 'Presence', verbs: 'Charm, Perform, Deceive' },
  { id: 'knowledge', name: 'Knowledge', verbs: 'Recall, Analyze, Comprehend' },
];

/** The modifiers every character places on the six traits, once each. */
export const TRAIT_POOL = [2, 1, 1, 0, 0, -1];

export const DAGGERHEART_STEPS: BuilderStep[] = [
  { id: 'basics', label: 'Basics' },
  { id: 'class', label: 'Class' },
  { id: 'subclass', label: 'Subclass' },
  { id: 'heritage', label: 'Heritage' },
  { id: 'traits', label: 'Traits' },
  { id: 'equipment', label: 'Equipment' },
  { id: 'domains', label: 'Domain cards' },
  { id: 'experiences', label: 'Experiences' },
  { id: 'story', label: 'Story' },
];

/** Steps the player may leave as they are. */
export const OPTIONAL_STEPS = new Set(['story']);

/** The builder step that lists a plan choice. */
export function stepOfChoice(choice: CreationChoice): string {
  return choice.id === 'subclass' ? 'subclass' : choice.type;
}

export function choiceOf(plan: BuildPlan, id: string): CreationChoice | undefined {
  return plan.choices.find((choice) => choice.id === id);
}

/** How many required picks a step still lacks. */
export function picksLeft(plan: BuildPlan, stepId: string): number {
  return plan.choices
    .filter((choice) => stepOfChoice(choice) === stepId && !choice.optional)
    .reduce((total, choice) => total + Math.max(0, choice.count - choice.selected.length), 0);
}

export function signed(value: number): string {
  return value > 0 ? `+${value}` : value < 0 ? `−${-value}` : '+0';
}

/** The build with a class change's dependants cleared: its subclass, cards, class item and gear checks start over. */
export function withClass(build: DaggerheartBuild, classSlug: string): DaggerheartBuild {
  if (build.class === classSlug) {
    return build;
  }
  return { ...build, class: classSlug, subclass: '', domainCards: [], classItem: '' };
}

export function describeBuild(build: DaggerheartBuild, plan: BuildPlan): string {
  const label = (id: string) => {
    const choice = choiceOf(plan, id);
    return choice?.options.find((option) => option.key === choice.selected[0])?.label;
  };
  const heritage = [label('community'), build.mixedAncestry?.name || label('ancestry')].filter(Boolean).join(' ');
  return [heritage, label('class')].filter(Boolean).join(' ') || 'Adventurer';
}
