import type { Locale } from './language';

/** A whole number with the language's digit grouping: 1,000 in English, 1.000 in Portuguese. */
export function formatNumber(value: number, locale: Locale): string {
  return value.toLocaleString(locale);
}

/** A number with up to `digits` decimals: 1.5 in English, 1,5 in Portuguese. */
export function formatDecimal(value: number, digits: number, locale: Locale): string {
  return value.toLocaleString(locale, { maximumFractionDigits: digits });
}

/** A number with exactly `digits` decimals: 5.0 in English, 5,0 in Portuguese. */
export function formatFixed(value: number, digits: number, locale: Locale): string {
  return value.toLocaleString(locale, { minimumFractionDigits: digits, maximumFractionDigits: digits });
}

/** 1st, 2nd, 3rd, 4th in English; 1º, 2º in Portuguese. */
export function formatOrdinal(value: number, locale: Locale): string {
  if (locale === 'pt-BR') {
    return `${value}º`;
  }
  const lastTwo = value % 100;
  if (lastTwo >= 11 && lastTwo <= 13) {
    return `${value}th`;
  }
  const suffix = ['th', 'st', 'nd', 'rd'][value % 10] ?? 'th';
  return `${value}${suffix}`;
}
