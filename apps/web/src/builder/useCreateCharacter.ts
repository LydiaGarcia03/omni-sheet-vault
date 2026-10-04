import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router';
import { createDraft } from './draftApi';

/** The builder's Basics step renames it; a draft needs a name from the start. */
const DEFAULT_DRAFT_NAME = 'New character';

/** Starts a character in a game system: creates its draft and opens the builder, with no page in between. */
export function useCreateCharacter() {
  const navigate = useNavigate();
  const [creating, setCreating] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const create = useCallback(
    async (systemId: string, options: { replace?: boolean } = {}) => {
      setCreating(systemId);
      setError(null);
      try {
        const draft = await createDraft(DEFAULT_DRAFT_NAME, systemId);
        navigate(`/characters/${draft.id}/build`, { replace: options.replace ?? false });
      } catch (createError) {
        setError((createError as Error).message);
        setCreating(null);
      }
    },
    [navigate],
  );

  return { create, creating, error };
}
