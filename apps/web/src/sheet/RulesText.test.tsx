import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { RulesText } from './RulesText';

describe('RulesText', () => {
  it('renders nothing for an empty text', () => {
    const { container } = render(<RulesText text={'  \n '} />);

    expect(container).toBeEmptyDOMElement();
  });

  it('splits paragraphs, keeps single line breaks and marks bold and italic words', () => {
    const { container } = render(<RulesText text={'**Lead In.** First line\nsecond line\n\n*Aside* text'} />);

    const paragraphs = container.querySelectorAll('p');
    expect(paragraphs).toHaveLength(2);
    expect(paragraphs[0].querySelector('strong')).toHaveTextContent('Lead In.');
    expect(paragraphs[0].querySelectorAll('br')).toHaveLength(1);
    expect(paragraphs[1].querySelector('em')).toHaveTextContent('Aside');
  });

  it('turns a block of "- " lines into a bullet list', () => {
    const { container } = render(<RulesText text={'Intro\n\n- One\n- **Two**'} />);

    expect(container.querySelectorAll('li')).toHaveLength(2);
    expect(container.querySelector('li strong')).toHaveTextContent('Two');
  });

  it('turns a "### " line into a heading and a "---" line into a rule', () => {
    const { container } = render(<RulesText text={'Intro\n\n---\n\n### Condition Rules\n\n- One'} />);

    expect(container.querySelector('hr')).toBeInTheDocument();
    expect(container.querySelector('h3')).toHaveTextContent('Condition Rules');
  });

  it('separates a heading from the paragraph on the next line, and reads "* " bullets', () => {
    const { container } = render(<RulesText text={'#### Strength Checks\nA Strength check can model...\n\n* Hold your breath\n* Go without sleep'} />);

    expect(container.querySelector('h4')).toHaveTextContent('Strength Checks');
    expect(container.querySelector('p')).toHaveTextContent('A Strength check can model...');
    expect(container.querySelectorAll('li')).toHaveLength(2);
  });

  it('draws a pipe table, with "checkbox" cells as checkboxes', () => {
    const { container } = render(<RulesText text={'|Applied|Level|Effect|\n|---|---|---|\n| checkbox |1| Disadvantage on ability checks |'} />);

    expect(container.querySelectorAll('th')).toHaveLength(3);
    expect(container.querySelector('td input[type="checkbox"]')).toBeInTheDocument();
    expect(container.querySelector('tbody')).toHaveTextContent('Disadvantage on ability checks');
  });

  it('marks ***bold italic*** words and numbered lists', () => {
    const { container } = render(<RulesText text={'***Lead In.*** text\n\n1. First\n2. Second'} />);

    expect(container.querySelector('strong em')).toHaveTextContent('Lead In.');
    expect(container.querySelectorAll('ol li')).toHaveLength(2);
  });

  it('boxes the text between "blockquote" and "fim-blockquote" lines', () => {
    const { container } = render(
      <RulesText text={'Before.\n\nblockquote\n#### HIDING\n\nFirst.\n\nSecond.\nfim-blockquote\n\nAfter.'} />,
    );

    const quote = container.querySelector('blockquote');
    expect(quote?.querySelector('h4')).toHaveTextContent('HIDING');
    expect(quote?.querySelectorAll('p')).toHaveLength(2);
    expect(container.textContent).not.toContain('blockquote');
    expect(container.querySelectorAll(':scope > .rules-text > p')).toHaveLength(2);
  });

  it('uses the intro variant above a pane’s controls', () => {
    const { container } = render(<RulesText text="Intro" placement="intro" />);

    expect(container.firstChild).toHaveClass('rules-text--intro');
  });
});
