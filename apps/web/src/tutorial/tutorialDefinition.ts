import type { ReactNode } from 'react';

export type TutorialStep = {
  title: string;
  body: ReactNode;
  /** CSS selector of the part to spotlight; the bubble sits centred when unset or not on the page. */
  target?: string;
  /** The builder step to open before showing this one. */
  builderStep?: string;
};

/** One system's guided tour: its steps, and the class that gives the overlay that system's look. */
export type TutorialDefinition = {
  /** Stored once seen, e.g. "builder:dnd-5e". */
  id: string;
  className: string;
  steps: TutorialStep[];
};
