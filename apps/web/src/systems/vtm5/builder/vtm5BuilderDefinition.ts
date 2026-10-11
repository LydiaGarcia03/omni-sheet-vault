import type { BuilderDefinition } from '../../../builder/builderDefinition';
import { describeBuild, VTM5_STEPS, type Vtm5Build } from './vtm5Build';
import { Vtm5BuilderFrame } from './Vtm5BuilderFrame';
import { Vtm5StepPage } from './Vtm5StepPage';

export const VTM5_BUILDER: BuilderDefinition = {
  steps: VTM5_STEPS,
  stepOf: (choice) => choice.type,
  StepPage: Vtm5StepPage,
  withoutStaleAnswers: (build) => build,
  describe: (build) => describeBuild((build ?? {}) as Vtm5Build),
  answerOf: () => undefined,
  leadingSources: [],
  checklistGroupOf: (choice) => choice.prompt,
  checklistGroupRank: () => 0,
  Frame: Vtm5BuilderFrame,
};
