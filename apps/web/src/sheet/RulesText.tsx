import { Fragment, type ReactNode } from 'react';

const INLINE_MARK = /(\*\*\*[^*]+\*\*\*|\*\*[^*]+\*\*|\*[^*\s][^*]*\*)/g;
const HEADING = /^(#{1,6})\s+(.*)$/;
const BULLET = /^[-*+]\s+(.*)$/;
const NUMBERED = /^\d+[.)]\s+(.*)$/;
const RULE = /^(-{3,}|\*{3,}|_{3,})$/;
const TABLE_ROW = /^\|.*\|$/;
const TABLE_SEPARATOR = /^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)*\|?$/;
const QUOTE_START = 'blockquote';
const QUOTE_END = 'fim-blockquote';

type Block =
  | { kind: 'heading'; level: number; text: string }
  | { kind: 'paragraph'; lines: string[] }
  | { kind: 'list'; ordered: boolean; items: string[] }
  | { kind: 'rule' }
  | { kind: 'table'; header: string[]; rows: string[][] }
  | { kind: 'quote'; blocks: Block[] };

function isQuoteMarker(line: string): boolean {
  const marker = line.toLowerCase();
  return marker === QUOTE_START || marker === QUOTE_END;
}

function inline(text: string): ReactNode[] {
  return text.split(INLINE_MARK).map((part, index) => {
    if (part.length > 6 && part.startsWith('***') && part.endsWith('***')) {
      return (
        <strong key={index}>
          <em>{part.slice(3, -3)}</em>
        </strong>
      );
    }
    if (part.length > 4 && part.startsWith('**') && part.endsWith('**')) {
      return <strong key={index}>{part.slice(2, -2)}</strong>;
    }
    if (part.length > 2 && part.startsWith('*') && part.endsWith('*')) {
      return <em key={index}>{part.slice(1, -1)}</em>;
    }
    return part;
  });
}

function cells(row: string): string[] {
  return row
    .replace(/^\|/, '')
    .replace(/\|$/, '')
    .split('|')
    .map((cell) => cell.trim());
}

/** Reads the text line by line into blocks: headings, paragraphs, lists, rules, tables and quotes. */
function parse(text: string): Block[] {
  return parseLines(text.replace(/\r\n/g, '\n').trim().split('\n'));
}

function parseLines(lines: string[]): Block[] {
  const blocks: Block[] = [];
  let index = 0;
  while (index < lines.length) {
    const line = lines[index].trim();
    const heading = HEADING.exec(line);
    if (line === '' || line.toLowerCase() === QUOTE_END) {
      index += 1;
    } else if (line.toLowerCase() === QUOTE_START) {
      const inner: string[] = [];
      index += 1;
      while (index < lines.length && lines[index].trim().toLowerCase() !== QUOTE_END) {
        inner.push(lines[index]);
        index += 1;
      }
      blocks.push({ kind: 'quote', blocks: parseLines(inner) });
      index += 1;
    } else if (heading) {
      blocks.push({ kind: 'heading', level: heading[1].length, text: heading[2] });
      index += 1;
    } else if (RULE.test(line)) {
      blocks.push({ kind: 'rule' });
      index += 1;
    } else if (TABLE_ROW.test(line) && index + 1 < lines.length && TABLE_SEPARATOR.test(lines[index + 1].trim())) {
      const rows: string[][] = [];
      index += 2;
      while (index < lines.length && TABLE_ROW.test(lines[index].trim())) {
        rows.push(cells(lines[index].trim()));
        index += 1;
      }
      blocks.push({ kind: 'table', header: cells(line), rows });
    } else if (BULLET.test(line) || NUMBERED.test(line)) {
      const ordered = !BULLET.test(line);
      const pattern = ordered ? NUMBERED : BULLET;
      const items: string[] = [];
      while (index < lines.length && pattern.test(lines[index].trim())) {
        items.push(pattern.exec(lines[index].trim())![1]);
        index += 1;
      }
      blocks.push({ kind: 'list', ordered, items });
    } else {
      const paragraph: string[] = [];
      while (index < lines.length) {
        const next = lines[index].trim();
        if (
          next === '' ||
          isQuoteMarker(next) ||
          HEADING.test(next) ||
          RULE.test(next) ||
          BULLET.test(next) ||
          NUMBERED.test(next) ||
          TABLE_ROW.test(next)
        ) {
          break;
        }
        paragraph.push(next);
        index += 1;
      }
      blocks.push({ kind: 'paragraph', lines: paragraph });
    }
  }
  return blocks;
}

/** A table cell; the word "checkbox" alone draws D&D Beyond's (display only) checkbox. */
function Cell({ text }: { text: string }) {
  return text.toLowerCase() === 'checkbox' ? <input type="checkbox" disabled aria-label="Applied" /> : <>{inline(text)}</>;
}

function render(block: Block, index: number): ReactNode {
  switch (block.kind) {
    case 'quote':
      return <blockquote key={index}>{block.blocks.map(render)}</blockquote>;
    case 'heading': {
      const Tag = `h${Math.min(Math.max(block.level, 2), 6)}` as 'h2';
      return <Tag key={index}>{inline(block.text)}</Tag>;
    }
    case 'rule':
      return <hr key={index} />;
    case 'list': {
      const Tag = block.ordered ? 'ol' : 'ul';
      return (
        <Tag key={index}>
          {block.items.map((item, itemIndex) => (
            <li key={itemIndex}>{inline(item)}</li>
          ))}
        </Tag>
      );
    }
    case 'table':
      return (
        <table key={index}>
          <thead>
            <tr>
              {block.header.map((cell, cellIndex) => (
                <th key={cellIndex}>
                  <Cell text={cell} />
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {block.rows.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {row.map((cell, cellIndex) => (
                  <td key={cellIndex}>
                    <Cell text={cell} />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      );
    case 'paragraph':
      return (
        <p key={index}>
          {block.lines.map((line, lineIndex) => (
            <Fragment key={lineIndex}>
              {lineIndex > 0 && <br />}
              {inline(line)}
            </Fragment>
          ))}
        </p>
      );
  }
}

/**
 * Rules text written in Markdown, drawn like D&D Beyond's (ddbc-html-content): headings (#–######), paragraphs (a
 * single line break stays one), bullet and numbered lists, --- rules, pipe tables, a boxed quote between a
 * "blockquote" line and a "fim-blockquote" line, and ***bold italic***, **bold**, *italic*. Nothing renders for an
 * empty text. `end` closes a pane, `intro` sits above its controls, `plain` has no
 * spacing of its own (an expanded condition, a section under its own subheading).
 */
export function RulesText({ text, placement = 'end' }: { text: string; placement?: 'end' | 'intro' | 'plain' }) {
  const blocks = parse(text);
  if (blocks.length === 0) {
    return null;
  }
  return <div className={`rules-text rules-text--${placement}`}>{blocks.map(render)}</div>;
}
