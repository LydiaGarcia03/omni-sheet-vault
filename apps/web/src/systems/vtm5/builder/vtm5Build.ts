import { useSyncExternalStore } from 'react';
import type { BuilderStep } from '../../../builder/builderDefinition';
import type { BuildPlan, CreationChoice } from '../../../builder/draftApi';
import { termOf, useVtm5Content } from '../content';
import { useVtm5Text } from '../locales/vtm5Messages';

/** The VtM build document, as the API's Vtm5CharacterCreationFlow reads it. */
export type Vtm5Build = {
  concept?: string;
  chronicle?: string;
  sire?: string;
  age?: string;
  generation?: string;
  clan?: string;
  attributes?: Record<string, number>;
  distribution?: string;
  skills?: Record<string, number>;
  specialties?: Record<string, string>;
  disciplineTwo?: string;
  disciplineOne?: string;
  powers?: Record<string, string[]>;
  ritual?: string;
  thinBloodMerits?: string[];
  thinBloodFlaws?: string[];
  alchemyMethod?: string;
  loresheets?: Record<string, number[]>;
  coterieContribution?: number;
  predator?: string;
  predatorSpecialty?: number;
  predatorDiscipline?: string;
  advantages?: Record<string, number>;
  convictions?: { conviction: string; touchstone: string }[];
  ambition?: string;
  desire?: string;
};

export const VTM5_STEPS: BuilderStep[] = [
  { id: 'basics', label: 'Basics' },
  { id: 'clan', label: 'Clan' },
  { id: 'attributes', label: 'Attributes' },
  { id: 'skills', label: 'Skills' },
  { id: 'disciplines', label: 'Disciplines' },
  { id: 'predator', label: 'Predator type' },
  { id: 'advantages', label: 'Advantages & flaws' },
  { id: 'beliefs', label: 'Beliefs' },
  { id: 'review', label: 'Review' },
];

export const ATTRIBUTE_SLOTS: Record<string, number> = { '4': 1, '3': 3, '2': 4, '1': 1 };

/** A generation ("13th", "14th–16th") as the interface language writes ordinals. */
export function useGeneration(): (generation: string) => string {
  const t = useVtm5Text();
  return (generation) => (generation ? t('builder.generation', { ordinal: generation, number: generation.replace(/(st|nd|rd|th)/g, '') }) : generation);
}

/** A step's label in the interface language: game terms come from the term list, the rest from VtM's texts. */
export function useStepLabel(): (stepId: string) => string {
  const t = useVtm5Text();
  const content = useVtm5Content();
  const labels: Record<string, string> = {
    basics: t('steps.basics'),
    clan: content ? termOf(content, 'Clan') : 'Clan',
    attributes: t('terms.attributes'),
    skills: t('terms.skills'),
    disciplines: t('terms.disciplines'),
    predator: t('terms.predatorType'),
    advantages: t('terms.advantagesAndFlaws'),
    beliefs: t('terms.beliefs'),
    review: t('steps.review'),
  };
  return (stepId) => labels[stepId] ?? stepId;
}

export function describeBuild(build: Vtm5Build): string {
  return [build.clan, build.age, build.predator].filter(Boolean).join(' · ') || 'Vampire';
}

export function choiceOf(plan: BuildPlan, id: string): CreationChoice | undefined {
  return plan.choices.find((choice) => choice.id === id);
}

/** Dots placed at each value, for the slot counters. */
export function countAt(values: Record<string, number> | undefined, dots: number): number {
  return Object.values(values ?? {}).filter((value) => value === dots).length;
}

// The option the player is reading about; the step page sets it, the explanation column shows it.
export type Focus = { kind: string; key: string } | null;
let focus: Focus = null;
const listeners = new Set<() => void>();

export function setFocus(next: Focus) {
  focus = next;
  listeners.forEach((listener) => listener());
}

export function useFocus(): Focus {
  return useSyncExternalStore(
    (listener) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    () => focus,
  );
}
