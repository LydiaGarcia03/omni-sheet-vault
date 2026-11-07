import { useMemo } from 'react';
import type { BuilderLabels, SummaryTerm } from '../../../builder/builderDefinition';
import { useDnd5eTerms, type Dnd5eTerms } from '../locales/dnd5eMessages';

function summaryTermOf({ t }: Dnd5eTerms, term: SummaryTerm): string {
  switch (term) {
    case 'hitPoints':
      return t('terms.labels.hitPoints');
    case 'armorClass':
      return t('terms.labels.armorClass');
    case 'speed':
      return t('terms.labels.speed');
    case 'initiative':
      return t('terms.labels.initiative');
    case 'proficiencyBonus':
      return t('terms.labels.proficiencyBonus');
    case 'savingThrows':
      return t('terms.labels.savingThrows');
    case 'passivePerception':
      return t('terms.labels.passivePerception');
    case 'features':
      return t('terms.tabs.features');
    case 'proficiencies':
      return t('terms.labels.proficienciesAndTraining');
    case 'languages':
      return t('terms.labels.languages');
    case 'defensesSenses':
      return `${t('terms.defenses.defenses')} · ${t('terms.labels.senses')}`;
    case 'resistance':
      return t('terms.defenses.resistance');
  }
}

/** The D&D builder's step, checklist and summary names in the player's language. */
export function useDnd5eBuilderLabels(): BuilderLabels {
  const terms = useDnd5eTerms();
  return useMemo(
    () => ({
      step: (step) => terms.t.dynamic(`terms.builderSteps.${step.id}`, step.label),
      checklistGroup: (group) => terms.t.dynamic(`terms.checklistGroups.${group}`, group),
      summaryTerm: (term) => summaryTermOf(terms, term),
      abilityShort: (ability) => terms.abilityAbbreviation(ability),
      feet: terms.feet,
    }),
    [terms],
  );
}
