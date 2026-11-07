import type { BuilderDefinition } from '../../../builder/builderDefinition';
import { DAGGERHEART_STEPS, describeBuild, stepOfChoice, type DaggerheartBuild } from './daggerheartBuild';
import { DaggerheartBuilderFrame } from './DaggerheartBuilderFrame';
import { DAGGERHEART_TUTORIAL } from './daggerheartTutorial';

/** Daggerheart's builder draws its own page (builder v3); the shared step page is never shown. */
export const DAGGERHEART_BUILDER: BuilderDefinition = {
  steps: DAGGERHEART_STEPS,
  stepOf: stepOfChoice,
  StepPage: () => null,
  withoutStaleAnswers: (build) => build,
  describe: (build, plan) => describeBuild((build ?? {}) as DaggerheartBuild, plan),
  answerOf: () => undefined,
  leadingSources: [],
  checklistGroupOf: (choice) => choice.prompt,
  checklistGroupRank: () => 0,
  Frame: DaggerheartBuilderFrame,
  tutorial: DAGGERHEART_TUTORIAL,
};
