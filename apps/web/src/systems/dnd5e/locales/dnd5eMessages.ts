import { useMemo } from 'react';
import { formatDecimal, formatOrdinal } from '../../../language/format';
import { defineMessages, useLanguage, type Locale } from '../../../language/language';
import dnd5e from './dnd5e.en.json';

/** D&D 5e's own interface texts: the sheet, its panes, the builder steps and level up. */
export const useDnd5eText = defineMessages(dnd5e, { 'pt-BR': () => import('./dnd5e.pt-BR.json') });

export type Dnd5eText = ReturnType<typeof useDnd5eText>;

/** Meters per foot as the Brazilian books convert them: 5 ft. is 1,5 m. */
const METERS_PER_FOOT = 0.3;

/** A distance given in feet, as the player reads it: feet in English, meters in Portuguese. */
export function formatFeet(feet: number, locale: Locale, t: Dnd5eText = useDnd5eText.english): string {
  return locale === 'pt-BR'
    ? t('units.meters', { value: formatDecimal(feet * METERS_PER_FOOT, 1, locale) })
    : t('units.feet', { value: feet });
}

export type Dnd5eTerms = ReturnType<typeof termsOf>;

function capitalized(value: string): string {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

/** Game-term lookups by key; an unknown key reads as itself. */
function termsOf(t: Dnd5eText, locale: Locale) {
  return {
    t,
    locale,
    ability: (key: string) => t.dynamic(`terms.abilities.${key}`, capitalized(key)),
    abilityAbbreviation: (key: string) => t.dynamic(`terms.abilityAbbreviations.${key}`, key.slice(0, 3).toUpperCase()),
    skill: (key: string) => t.dynamic(`terms.skills.${key}`, key),
    damageType: (type: string) => t.dynamic(`terms.damageTypes.${type.toLowerCase()}`, capitalized(type)),
    school: (school: string) => t.dynamic(`terms.schools.${school.toLowerCase()}`, capitalized(school)),
    condition: (key: string) => t.dynamic(`terms.conditions.${key}`, capitalized(key)),
    theme: (id: string, fallback: string) => t.dynamic(`terms.themes.${id}`, fallback),
    feet: (feet: number) => formatFeet(feet, locale, t),
    /** The number alone, for layouts that draw the unit apart: 30 in English, 9 in Portuguese. */
    distanceValue: (feet: number) => (locale === 'pt-BR' ? formatDecimal(feet * METERS_PER_FOOT, 1, locale) : String(feet)),
    distanceUnit: locale === 'pt-BR' ? 'm' : 'ft.',
    ordinal: (value: number) => formatOrdinal(value, locale),
  };
}

/** The English terms, the default for helpers called outside React. */
export const ENGLISH_TERMS: Dnd5eTerms = termsOf(useDnd5eText.english, 'en');

/** The D&D texts plus game-term lookups and distances in the player's language. */
export function useDnd5eTerms(): Dnd5eTerms {
  const t = useDnd5eText();
  const { locale } = useLanguage();
  return useMemo(() => termsOf(t, locale), [t, locale]);
}
