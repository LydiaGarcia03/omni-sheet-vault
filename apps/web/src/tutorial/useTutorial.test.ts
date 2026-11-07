import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { TutorialDefinition } from './tutorialDefinition';
import { useTutorial } from './useTutorial';

const { getSeenTutorials, markTutorialSeen } = vi.hoisted(() => ({ getSeenTutorials: vi.fn(), markTutorialSeen: vi.fn() }));
vi.mock('./tutorialApi', () => ({ getSeenTutorials, markTutorialSeen }));

const TOUR: TutorialDefinition = { id: 'builder:dnd-5e', className: 'tutorial--dnd5e', steps: [{ title: 'Welcome', body: null }] };

describe('useTutorial', () => {
  beforeEach(() => {
    getSeenTutorials.mockReset();
    markTutorialSeen.mockReset().mockResolvedValue(undefined);
  });

  it('opens a tour the player has not seen, and records it as seen once closed', async () => {
    getSeenTutorials.mockResolvedValue(['builder:vtm-v5']);
    const { result } = renderHook(() => useTutorial(TOUR));

    await waitFor(() => expect(result.current.open).toBe(true));
    act(() => result.current.close());

    expect(result.current.open).toBe(false);
    expect(markTutorialSeen).toHaveBeenCalledWith('builder:dnd-5e');
  });

  it('stays closed for a seen tour until reopened, and does not record it again', async () => {
    getSeenTutorials.mockResolvedValue(['builder:dnd-5e']);
    const { result } = renderHook(() => useTutorial(TOUR));

    await waitFor(() => expect(getSeenTutorials).toHaveBeenCalled());
    expect(result.current.open).toBe(false);
    act(() => result.current.start());
    expect(result.current.open).toBe(true);
    act(() => result.current.close());

    expect(markTutorialSeen).not.toHaveBeenCalled();
  });

  it('does nothing for a system without a tour', () => {
    const { result } = renderHook(() => useTutorial(undefined));

    expect(result.current.open).toBe(false);
    expect(getSeenTutorials).not.toHaveBeenCalled();
  });
});
