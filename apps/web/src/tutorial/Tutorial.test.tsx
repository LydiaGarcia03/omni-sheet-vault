import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { Tutorial } from './Tutorial';
import type { TutorialDefinition } from './tutorialDefinition';

const TOUR: TutorialDefinition = {
  id: 'builder:test',
  className: 'tutorial--test',
  steps: [
    { title: 'Welcome', body: <p>Read the books.</p> },
    { title: 'Progress', body: <p>Every step.</p>, target: '[data-tutorial="checklist"]', builderStep: 'basics' },
    { title: 'Done', body: <p>Bye.</p> },
  ],
};

function renderTour(onNavigate = vi.fn(), onClose = vi.fn()) {
  render(
    <>
      <aside data-tutorial="checklist">Checklist</aside>
      <Tutorial definition={TOUR} onNavigate={onNavigate} onClose={onClose} />
    </>,
  );
  return { onNavigate, onClose };
}

describe('Tutorial', () => {
  it('starts on the first step with its count, themed by the system class', () => {
    renderTour();

    expect(screen.getByRole('dialog', { name: 'Welcome' })).toBeInTheDocument();
    expect(screen.getByText('1 of 3')).toBeInTheDocument();
    expect(document.querySelector('.tutorial.tutorial--test')).not.toBeNull();
    expect(screen.queryByRole('button', { name: 'Back' })).toBeNull();
  });

  it('moves forward and back, opening the builder step a step lives on and spotlighting its target', async () => {
    const { onNavigate } = renderTour();

    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    expect(screen.getByRole('dialog', { name: 'Progress' })).toBeInTheDocument();
    expect(onNavigate).toHaveBeenCalledWith('basics');
    await waitFor(() => expect(document.querySelector('.tutorial__ring')).not.toBeNull());

    fireEvent.click(screen.getByRole('button', { name: 'Back' }));
    expect(screen.getByRole('dialog', { name: 'Welcome' })).toBeInTheDocument();
  });

  it('closes on Skip, on Escape and on Finish', () => {
    const { onClose } = renderTour();

    fireEvent.click(screen.getByRole('button', { name: 'Skip tutorial' }));
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));

    expect(screen.queryByRole('button', { name: 'Skip tutorial' })).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: 'Finish' }));
    expect(onClose).toHaveBeenCalledTimes(3);
  });

  it('calls onFinish, not onClose, when Finish is clicked on the last step', () => {
    const onClose = vi.fn();
    const onFinish = vi.fn();
    render(<Tutorial definition={TOUR} onNavigate={vi.fn()} onClose={onClose} onFinish={onFinish} />);

    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Finish' }));

    expect(onFinish).toHaveBeenCalledTimes(1);
    expect(onClose).not.toHaveBeenCalled();
  });
});
