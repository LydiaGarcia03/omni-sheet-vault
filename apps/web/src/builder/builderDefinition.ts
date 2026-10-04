import type { ComponentType } from 'react';
import type { Portrait } from '../characters/portrait';
import type { BuildPlan, CreationChoice, DraftPreview } from './draftApi';

export type BuilderStep = {
  id: string;
  label: string;
};

export type StepPageProps = {
  characterId: string;
  systemId: string;
  stepId: string;
  /** Saved on its own endpoint, not with the build: it belongs to the character. */
  portrait: Portrait | null;
  onPortraitChange: (portrait: Portrait | null) => void;
  name: string;
  onNameChange: (name: string) => void;
  build: unknown;
  onBuildChange: (build: unknown) => void;
  plan: BuildPlan;
  preview: DraftPreview;
  /** The plan's choices this step lists, in plan order. */
  choices: CreationChoice[];
  onFinish: () => void;
  finishing: boolean;
};

/** What one game system plugs into the generic three-column builder. */
export type BuilderDefinition = {
  steps: BuilderStep[];
  /** The step whose page and checklist entry list this choice. */
  stepOf: (choice: CreationChoice) => string;
  StepPage: ComponentType<StepPageProps>;
  /** The build with answers the plan no longer offers removed, or the same build when there are none. */
  withoutStaleAnswers: (build: unknown, plan: BuildPlan) => unknown;
  /** One line naming what the character is so far, for the summary. */
  describe: (build: unknown, plan: BuildPlan) => string;
  /** The build's own answer to a choice, or undefined when the build doesn't store that choice as a pick. */
  answerOf: (build: unknown, choiceId: string) => string[] | undefined;
  /** Source books listed first in every source select, in this order; the rest follow. */
  leadingSources: string[];
  /** The checklist line a choice counts toward ("Spells", "Proficiencies"), so the sidebar lists kinds, not every pick. */
  checklistGroupOf: (choice: CreationChoice) => string;
  /** Where a checklist line sits within its step. */
  checklistGroupRank: (group: string) => number;
  /** The system's page backdrop (D&D 5e: the parchment). */
  pageClassName?: string;
};

export type ChecklistLine = { group: string; total: number; pending: number };

/** A step's required choices folded into one line per kind, in the system's order. */
export function checklistLines(definition: BuilderDefinition, choices: CreationChoice[]): ChecklistLine[] {
  const lines = new Map<string, ChecklistLine>();
  for (const choice of choices.filter((candidate) => !candidate.optional)) {
    const group = definition.checklistGroupOf(choice);
    const line = lines.get(group) ?? { group, total: 0, pending: 0 };
    line.total += 1;
    line.pending += choice.pending ? 1 : 0;
    lines.set(group, line);
  }
  return [...lines.values()].sort((a, b) => definition.checklistGroupRank(a.group) - definition.checklistGroupRank(b.group));
}

/** The plan with each choice's selection read from the local build, so a pick shows at once instead of after the next save. */
export function withLocalAnswers(definition: BuilderDefinition, plan: BuildPlan, build: unknown): BuildPlan {
  let changed = false;
  const choices = plan.choices.map((choice) => {
    const answer = definition.answerOf(build, choice.id);
    if (!answer || sameKeys(answer, choice.selected)) {
      return choice;
    }
    changed = true;
    return { ...choice, selected: answer, pending: !choice.optional && answer.length < choice.count };
  });
  return changed ? { ...plan, choices } : plan;
}

function sameKeys(a: string[], b: string[]): boolean {
  return a.length === b.length && a.every((key, index) => key === b[index]);
}

export function choicesByStep(definition: BuilderDefinition, plan: BuildPlan): Map<string, CreationChoice[]> {
  const byStep = new Map<string, CreationChoice[]>(definition.steps.map((step) => [step.id, []]));
  for (const choice of plan.choices) {
    byStep.get(definition.stepOf(choice))?.push(choice);
  }
  return byStep;
}

/** The labels of a choice's selected options, in selection order. */
export function selectedLabels(choice: CreationChoice): string[] {
  return choice.selected.map((key) => choice.options.find((option) => option.key === key)?.label ?? key);
}
