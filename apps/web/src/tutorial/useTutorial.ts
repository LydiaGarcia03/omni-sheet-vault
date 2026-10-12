import { useCallback, useEffect, useRef, useState } from 'react';
import type { TutorialDefinition } from './tutorialDefinition';
import { getSeenTutorials, markTutorialSeen } from './tutorialApi';

/** Opens a tour on its own the first time the player meets it, records it as seen on close, and lets it be reopened. */
export function useTutorial(definition: TutorialDefinition | undefined) {
  const [open, setOpen] = useState(false);
  const seen = useRef(false);

  useEffect(() => {
    if (!definition) {
      return;
    }
    let cancelled = false;
    getSeenTutorials()
      .then((ids) => {
        seen.current = ids.includes(definition.id);
        if (!cancelled && !seen.current) {
          setOpen(true);
        }
      })
      .catch(() => {
        seen.current = true;
      });
    return () => {
      cancelled = true;
    };
  }, [definition]);

  const start = useCallback(() => setOpen(true), []);
  const close = useCallback(() => {
    setOpen(false);
    if (definition && !seen.current) {
      seen.current = true;
      markTutorialSeen(definition.id).catch(() => undefined);
    }
  }, [definition]);

  return { open: open && definition !== undefined, start, close };
}
