import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { getCatalogueSources } from './api';

type SourceCodeLookup = (sourceBook: string) => string | null;

const NO_CODES: SourceCodeLookup = () => null;
const SourceCodesContext = createContext<SourceCodeLookup>(NO_CODES);
const LeadingSourcesContext = createContext<string[]>([]);
const cache = new Map<string, Promise<Map<string, string>>>();

function codesFor(systemId: string): Promise<Map<string, string>> {
  let codes = cache.get(systemId);
  if (!codes) {
    codes = getCatalogueSources(systemId)
      .then((sources) => new Map(sources.flatMap((source) => (source.sourceCode ? [[source.sourceBook, source.sourceCode] as const] : []))))
      .catch(() => new Map<string, string>())
      .then((loaded) => {
        if (loaded.size === 0) {
          cache.delete(systemId);
        }
        return loaded;
      });
    cache.set(systemId, codes);
  }
  return codes;
}

type SourceCodesProviderProps = {
  systemId: string;
  /** Books every source select lists first, in this order. */
  leadingSources?: string[];
  children: ReactNode;
};

/** Loads a system's source-book codes once and hands them, with its leading books, to every source select below it. */
export function SourceCodesProvider({ systemId, leadingSources = [], children }: SourceCodesProviderProps) {
  const [lookup, setLookup] = useState<SourceCodeLookup>(() => NO_CODES);
  useEffect(() => {
    let active = true;
    codesFor(systemId).then((codes) => active && setLookup(() => (book: string) => codes.get(book) ?? null));
    return () => {
      active = false;
    };
  }, [systemId]);
  return (
    <SourceCodesContext.Provider value={lookup}>
      <LeadingSourcesContext.Provider value={leadingSources}>{children}</LeadingSourcesContext.Provider>
    </SourceCodesContext.Provider>
  );
}

/** The short code for a full source-book name ("Player's Handbook" → "PHB"), or null when unknown. */
export function useSourceCode(): SourceCodeLookup {
  return useContext(SourceCodesContext);
}

/** The books a source select lists before every other one. */
export function useLeadingSources(): string[] {
  return useContext(LeadingSourcesContext);
}
