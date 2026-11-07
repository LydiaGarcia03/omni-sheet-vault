import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useOptionalAuth } from '../auth/AuthProvider';
import {
  clearPreferenceCookie,
  PREFERENCE_COOKIES,
  readPreferenceCookie,
  savePreferences,
  writePreferenceCookie,
} from '../auth/preferences';
import { LoadingOverlay } from '../loading/LoadingOverlay';
import { setCurrentLocale } from './currentLocale';

export type Locale = 'en' | 'pt-BR';

export const LOCALES: { id: Locale; code: string; name: string }[] = [
  { id: 'en', code: 'EN', name: 'English' },
  { id: 'pt-BR', code: 'PT-BR', name: 'Português (Brasil)' },
];

const DEFAULT_LOCALE: Locale = 'en';
const LEGACY_STORAGE_KEY = 'osv.locale';
/** The overlay stays at least this long, so a fast switch reads as a deliberate change instead of a flicker. */
const MIN_SWITCH_MS = 450;

export function isLocale(value: unknown): value is Locale {
  return LOCALES.some((locale) => locale.id === value);
}

// Message bundles

export type Messages = { [key: string]: string | Messages };
type Plural = { one: string; other: string };
type MessageKey<T, Prefix extends string = ''> = {
  [K in keyof T & string]: T[K] extends string | Plural ? `${Prefix}${K}` : MessageKey<T[K], `${Prefix}${K}.`>;
}[keyof T & string];
type Params = Record<string, string | number>;
type Loader = () => Promise<{ default: Messages }>;

type Bundle = {
  english: Messages;
  loaders: Partial<Record<Locale, Loader>>;
  loaded: Map<Locale, Messages>;
};

const bundles: Bundle[] = [];

function loadBundle(bundle: Bundle, locale: Locale): Promise<void> {
  const loader = bundle.loaders[locale];
  if (!loader || bundle.loaded.has(locale)) {
    return Promise.resolve();
  }
  return loader().then((module) => {
    bundle.loaded.set(locale, module.default);
  });
}

function loadLocale(locale: Locale): Promise<void> {
  return Promise.all(bundles.map((bundle) => loadBundle(bundle, locale))).then(() => undefined);
}

function lookup(messages: Messages | undefined, key: string): string | Plural | undefined {
  let node: string | Messages | undefined = messages;
  for (const part of key.split('.')) {
    node = typeof node === 'object' ? node[part] : undefined;
  }
  return node as string | Plural | undefined;
}

function isPlural(value: unknown): value is Plural {
  return typeof value === 'object' && value !== null && 'other' in value;
}

function pick(value: string | Plural | undefined, locale: Locale, params?: Params): string | undefined {
  if (isPlural(value)) {
    const count = Number(params?.count ?? 0);
    const form = new Intl.PluralRules(locale).select(count) === 'one' ? value.one : value.other;
    return form || undefined;
  }
  return value || undefined;
}

function interpolate(text: string, params?: Params): string {
  return params ? text.replace(/\{(\w+)\}/g, (whole, name: string) => (name in params ? String(params[name]) : whole)) : text;
}

export type Translate<T> = {
  (key: MessageKey<T>, params?: Params): string;
  /** A key built at runtime (e.g. from an id); returns the fallback when the bundle has no such key. */
  dynamic: (key: string, fallback: string, params?: Params) => string;
};

function translator<T extends Messages>(bundle: Bundle, locale: Locale): Translate<T> {
  const resolve = (key: string, params?: Params) =>
    pick(lookup(bundle.loaded.get(locale), key), locale, params) ?? pick(lookup(bundle.english, key), locale, params);
  const t = ((key: string, params?: Params) => interpolate(resolve(key, params) ?? key, params)) as Translate<T>;
  t.dynamic = (key, fallback, params) => interpolate(resolve(key, params) ?? fallback, params);
  return t;
}

/**
 * Registers a message bundle: its English messages, bundled with the app, and a loader per other language, fetched on
 * demand. Returns the hook that translates with it in the current language; a missing or empty message falls back to
 * English.
 */
export function defineMessages<T extends Messages>(english: T, loaders: Partial<Record<Exclude<Locale, 'en'>, Loader>>) {
  const bundle: Bundle = { english, loaders, loaded: new Map() };
  bundles.push(bundle);
  function useMessages(): Translate<T> {
    const { locale } = useLanguage();
    const [, setLoaded] = useState(0);
    const missing = locale !== DEFAULT_LOCALE && bundle.loaders[locale] !== undefined && !bundle.loaded.has(locale);
    useEffect(() => {
      if (missing) {
        void loadBundle(bundle, locale).then(() => setLoaded((count) => count + 1));
      }
    }, [missing, locale]);
    return useMemo(() => translator<T>(bundle, locale), [locale, missing]);
  }
  /** The English messages outside React, the default for helpers that take a translator. */
  useMessages.english = translator<T>(bundle, DEFAULT_LOCALE);
  return useMessages;
}

// The player's language

type LanguageState = {
  locale: Locale;
  /** True while a new language loads; pages show the loading overlay meanwhile. */
  switching: boolean;
  setLocale: (locale: Locale) => void;
};

const LanguageContext = createContext<LanguageState | null>(null);

/** This browser's choice: the cookie shared with the login pages, or the older local storage entry. */
function storedLocale(): Locale | null {
  const cookie = readPreferenceCookie(PREFERENCE_COOKIES.locale);
  if (isLocale(cookie)) {
    return cookie;
  }
  try {
    const value = window.localStorage.getItem(LEGACY_STORAGE_KEY);
    return isLocale(value) ? value : null;
  } catch {
    return null;
  }
}

function storeLocale(locale: Locale) {
  writePreferenceCookie(PREFERENCE_COOKIES.locale, locale);
}

/** True once when the player picked a language on the login page; that choice then beats the saved one. */
function takeLoginPagePick(): boolean {
  const picked = readPreferenceCookie(PREFERENCE_COOKIES.localePicked) !== null;
  clearPreferenceCookie(PREFERENCE_COOKIES.localePicked);
  return picked;
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** The interface language: the signed-in player's saved choice, else this browser's, else English. */
export function LanguageProvider({ children }: { children: ReactNode }) {
  const auth = useOptionalAuth();
  const signedIn = auth?.status === 'signed-in';
  const saved = auth?.user?.preferences?.locale;
  const [locale, setCurrent] = useState<Locale>(DEFAULT_LOCALE);
  const [ready, setReady] = useState(false);
  const [switching, setSwitching] = useState(false);
  const target = useRef<Locale>(DEFAULT_LOCALE);

  const apply = useCallback(async (next: Locale, withOverlay: boolean) => {
    target.current = next;
    if (withOverlay) {
      setSwitching(true);
    }
    await Promise.all([loadLocale(next).catch(() => undefined), withOverlay ? delay(MIN_SWITCH_MS) : undefined]);
    if (target.current === next) {
      setCurrentLocale(next);
      setCurrent(next);
      document.documentElement.lang = next;
    }
    setSwitching(false);
  }, []);

  useEffect(() => {
    void apply(storedLocale() ?? DEFAULT_LOCALE, false).then(() => setReady(true));
  }, [apply]);

  const status = auth?.status;
  useEffect(() => {
    if (!ready || status === 'loading') {
      return;
    }
    if (takeLoginPagePick()) {
      if (signedIn && saved !== target.current) {
        void savePreferences({ locale: target.current });
      }
      return;
    }
    if (isLocale(saved) && saved !== target.current) {
      storeLocale(saved);
      void apply(saved, true);
    }
  }, [ready, status, signedIn, saved, apply]);

  const setLocale = useCallback(
    (next: Locale) => {
      if (next === target.current) {
        return;
      }
      storeLocale(next);
      if (signedIn) {
        void savePreferences({ locale: next });
      }
      void apply(next, true);
    },
    [signedIn, apply],
  );

  const value = useMemo(() => ({ locale, switching, setLocale }), [locale, switching, setLocale]);
  if (!ready) {
    return <LoadingOverlay />;
  }
  return (
    <LanguageContext.Provider value={value}>
      {children}
      {switching && <LoadingOverlay />}
    </LanguageContext.Provider>
  );
}

const ENGLISH_ONLY: LanguageState = { locale: DEFAULT_LOCALE, switching: false, setLocale: () => {} };

export function useLanguage(): LanguageState {
  return useContext(LanguageContext) ?? ENGLISH_ONLY;
}
