import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ProficiencyDot } from './ProficiencyDot';

describe('ProficiencyDot', () => {
  it('draws the empty circle for no proficiency', () => {
    const { container } = render(<ProficiencyDot level="NONE" className="dot" />);

    expect(container.querySelector('.dot')).not.toHaveAttribute('data-proficiency');
    expect(container.querySelector('.dot--icon')).toBeNull();
  });

  it.each(['FULL', 'HALF', 'EXPERT'] as const)('draws the %s icon', (level) => {
    const { container } = render(<ProficiencyDot level={level} className="dot" />);

    const dot = container.querySelector('.dot--icon');
    expect(dot).toHaveAttribute('data-proficiency', level);
    expect(dot?.querySelector('svg')).toBeInTheDocument();
  });

  it('marks expertise with a ring around the dot', () => {
    const { container } = render(<ProficiencyDot level="EXPERT" className="dot" />);

    expect(container.querySelectorAll('svg path, svg circle')).toHaveLength(2);
  });
});
