import type { BuilderDefinition } from '../../../builder/builderDefinition';
import {
  answerOf,
  DND5E_STEPS,
  describeBuild,
  dnd5eChecklistGroupOf,
  dnd5eChecklistGroupRank,
  dnd5eStepOf,
  withoutStaleAnswers,
  type Dnd5eBuild,
} from './dnd5eBuild';
import { Dnd5eStepPage } from './Dnd5eStepPage';

export const DND5E_BUILDER: BuilderDefinition = {
  steps: DND5E_STEPS,
  stepOf: dnd5eStepOf,
  StepPage: Dnd5eStepPage,
  withoutStaleAnswers: (build, plan) => withoutStaleAnswers(build as Dnd5eBuild, plan),
  describe: (build, plan) => describeBuild(build as Dnd5eBuild, plan),
  answerOf: (build, choiceId) => answerOf(build as Dnd5eBuild, choiceId),
  leadingSources: ["Player's Handbook"],
  checklistGroupOf: dnd5eChecklistGroupOf,
  checklistGroupRank: dnd5eChecklistGroupRank,
  pageClassName: 'parchment-page',
};
