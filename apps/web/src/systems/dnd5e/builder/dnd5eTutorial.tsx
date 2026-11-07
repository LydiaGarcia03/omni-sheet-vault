import type { ReactNode } from 'react';
import type { TutorialDefinition, TutorialStep } from '../../../tutorial/tutorialDefinition';
import { useDnd5eText } from '../locales/dnd5eMessages';
import './dnd5eTutorial.css';

type TourStep = 'welcome' | 'progress' | 'identity' | 'books' | 'extra' | 'tableRules' | 'species' | 'classes' | 'pending' | 'background' | 'abilityMethod' | 'abilityTotals' | 'equipment' | 'summary' | 'review' | 'end';

/** "**Label:** text" with the label in bold. */
function withBold(text: string): ReactNode {
  return text.split(/\*\*(.+?)\*\*/).map((part, index) => (index % 2 === 1 ? <b key={index}>{part}</b> : part));
}

function TourTitle({ step }: { step: TourStep }) {
  const t = useDnd5eText();
  return <>{t.dynamic(`tutorial.${step}.title`, step)}</>;
}

/** A tour step's paragraphs (`p1`, `p2`), its fine-print `note`, or its list items (`li1`…), in the player's language. */
function TourBody({ step }: { step: TourStep }) {
  const t = useDnd5eText();
  const text = (part: string) => t.dynamic(`tutorial.${step}.${part}`, '');
  const items = ['li1', 'li2', 'li3', 'li4', 'li5'].map(text).filter(Boolean);
  return (
    <>
      {['p1', 'p2'].map(text).filter(Boolean).map((paragraph) => (
        <p key={paragraph}>{paragraph}</p>
      ))}
      {items.length > 0 && (
        <ul>
          {items.map((item) => (
            <li key={item}>{withBold(item)}</li>
          ))}
        </ul>
      )}
      {text('note') && <p className="tutorial__note">{text('note')}</p>}
    </>
  );
}

function tourStep(step: TourStep, target?: string, builderStep?: string): TutorialStep {
  return { title: <TourTitle step={step} />, body: <TourBody step={step} />, target, builderStep };
}

const STEP_FRAME = '[data-tutorial="step"] .builder-frame';

/** The D&D 5e builder tour: one stop per part of the page and per step, with the rule basics each step asks about. */
export const DND5E_TUTORIAL: TutorialDefinition = {
  id: 'builder:dnd-5e',
  className: 'tutorial--dnd5e',
  steps: [
    tourStep('welcome'),
    tourStep('progress', '[data-tutorial="checklist"]', 'basics'),
    tourStep('identity', '[data-tutorial="basics-identity"]', 'basics'),
    tourStep('books', '[data-tutorial="basics-sources"]', 'basics'),
    tourStep('extra', '[data-tutorial="basics-extra"]', 'basics'),
    tourStep('tableRules', '[data-tutorial="basics-rules"]', 'basics'),
    tourStep('species', STEP_FRAME, 'species'),
    tourStep('classes', STEP_FRAME, 'classes'),
    tourStep('pending', '.builder-layout__main .builder-pending-mark', 'classes'),
    tourStep('background', STEP_FRAME, 'background'),
    tourStep('abilityMethod', '[data-tutorial="ability-method"]', 'abilities'),
    tourStep('abilityTotals', '[data-tutorial="ability-scores"]', 'abilities'),
    tourStep('equipment', STEP_FRAME, 'equipment'),
    tourStep('summary', '[data-tutorial="summary"]'),
    tourStep('review', STEP_FRAME, 'review'),
    tourStep('end', '[data-tutorial="help"]'),
  ],
};
