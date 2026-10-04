import { DND5E_BUILDER } from '../systems/dnd5e/builder/dnd5eBuilderDefinition';
import type { BuilderDefinition } from './builderDefinition';

const BUILDERS: Record<string, BuilderDefinition> = {
  'dnd-5e': DND5E_BUILDER,
};

export function builderFor(systemId: string): BuilderDefinition {
  const builder = BUILDERS[systemId];
  if (!builder) {
    throw new Error(`No character builder for game system ${systemId}`);
  }
  return builder;
}
