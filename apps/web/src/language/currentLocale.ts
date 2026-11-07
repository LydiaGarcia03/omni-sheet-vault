let current = 'en';

/** The interface language in use, for code outside React (the API client's Accept-Language header). */
export function currentLocale(): string {
  return current;
}

export function setCurrentLocale(locale: string): void {
  current = locale;
}
