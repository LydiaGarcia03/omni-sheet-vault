import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { TutorialLauncher } from './TutorialLauncher';

describe('TutorialLauncher', () => {
  it("carries the tour's theme class, so each system styles its own button", () => {
    render(<TutorialLauncher onOpen={vi.fn()} themeClassName="tutorial--daggerheart" />);

    expect(screen.getByRole('button', { name: 'Open the tutorial' })).toHaveClass('tutorial-launcher', 'tutorial--daggerheart');
  });

  it('keeps the shared look without a theme', () => {
    render(<TutorialLauncher onOpen={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'Open the tutorial' })).toHaveAttribute('class', 'tutorial-launcher');
  });
});
