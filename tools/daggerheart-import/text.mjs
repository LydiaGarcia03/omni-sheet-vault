// Turns column-ordered SRD text into headings and paragraphs, and paragraphs into prose.

const FOOTER = /^\s*(\d+\s+)?Daggerheart SRD(\s+\d+)?\s*$/;
const PAGE_MARKER = /^\u0000PAGE (\d+)$/;
const LABEL_LINE = /^[A-Z][A-Z ]+ – /;
const BULLET = /^[•◦]/;
const TERMINAL = /[.!?:”")]$/;

export function sectionText(pages, firstPage, lastPage) {
  return pages
    .filter(page => page.number >= firstPage && page.number <= lastPage)
    .map(page => `\u0000PAGE ${page.number}\n${page.text}`)
    .join('\n');
}

export function normalize(text) {
  return text
    .replace(/\r/g, '')
    .replace(/‑/g, '-')
    .replace(/ /g, ' ')
    .replace(/[ \t]+$/gm, '');
}

export function isHeading(line) {
  const text = line.trim();
  if (!/^[A-Z0-9][A-Z0-9 ’'&:–,().-]*$/.test(text)) return false;
  return (text.match(/[A-Z]/g) ?? []).length >= 2;
}

function headingText(text) {
  const tokens = text.split(' ').filter(Boolean);
  const singleLetters = tokens.filter(token => token.length <= 2).length;
  return singleLetters >= 5 ? tokens.join('') : tokens.join(' ');
}

// Tokens: { type: 'heading', text, page } and { type: 'para', lines, page }.
export function tokenize(text) {
  const tokens = [];
  let page = null;
  let current = null;
  const close = () => {
    if (current) tokens.push(current);
    current = null;
  };
  for (const raw of normalize(text).split('\n')) {
    const marker = raw.match(PAGE_MARKER);
    if (marker) {
      page = Number(marker[1]);
      continue;
    }
    const line = raw.trim().replace(/^-{2,}(?=[A-Z])/, '');
    if (line === '' || FOOTER.test(line)) {
      close();
      continue;
    }
    if (isHeading(line) && !LABEL_LINE.test(line)) {
      if (current?.type === 'heading' && !current.text.includes(' – ')) {
        current.text = headingText(`${current.text} ${line}`);
      } else {
        close();
        current = { type: 'heading', text: headingText(line), page };
      }
      continue;
    }
    if (current?.type !== 'para' || LABEL_LINE.test(line)) {
      close();
      current = { type: 'para', lines: [], page };
    }
    current.lines.push(line);
  }
  close();
  return mergeBrokenParagraphs(tokens);
}

// Rejoins a paragraph that a column or page break split mid-sentence.
function mergeBrokenParagraphs(tokens) {
  const merged = [];
  for (const token of tokens) {
    const previous = merged.at(-1);
    if (token.type === 'para' && previous?.type === 'para' && continues(previous, token)) {
      previous.lines.push(...token.lines);
    } else {
      merged.push(token);
    }
  }
  return merged;
}

export function continues(previous, next) {
  const last = previous.lines.at(-1);
  const first = next.lines[0];
  if (TERMINAL.test(last) || BULLET.test(first) || LABEL_LINE.test(first)) return false;
  return /^[a-z(]/.test(first) || !featureStart(first);
}

export function prose(lines) {
  let out = '';
  for (const line of lines) {
    if (out === '') out = line;
    else if (BULLET.test(line)) out += `\n${line}`;
    else if (/[-—]$/.test(out) && !/ [-—]$/.test(out)) out += line;
    else out += ` ${line}`;
  }
  return out.replace(/^([•◦])\s+/gm, '$1 ');
}

export function paragraphText(token) {
  return prose(token.lines);
}

export function joinParagraphs(tokens) {
  return tokens.map(paragraphText).join('\n\n');
}

// Splits a paragraph's lines into bullet items ("• ...").
export function bulletItems(token) {
  const items = [];
  for (const line of token.lines) {
    if (/^•/.test(line)) items.push([line.replace(/^•\s*/, '')]);
    else if (items.length > 0) items.at(-1).push(line);
  }
  return items.map(prose);
}

const FEATURE_START = /^([A-Z][A-Za-z’'!,&-]*(?: [A-Za-z’'!,&-]+){0,5}): (.+)$/;
const FEATURE_START_PERIOD = /^([A-Z][A-Za-z’'-]*(?: [A-Za-z’'-]+){0,3})\. (.+)$/;

// Asides printed inside a feature, not features of their own.
const ASIDES = new Set(['Tip', 'Note']);

export function featureStart(line) {
  const match = line.match(FEATURE_START);
  return match && !ASIDES.has(match[1]) ? { name: match[1], rest: match[2] } : null;
}

// Splits a paragraph where a new "Name: text" line follows a finished sentence.
function splitFeatureParagraphs(token) {
  const parts = [];
  for (const line of token.lines) {
    const last = parts.at(-1);
    if (last && featureStart(line) && TERMINAL.test(last.lines.at(-1))) parts.push({ ...token, lines: [line] });
    else if (last) last.lines.push(line);
    else parts.push({ ...token, lines: [line] });
  }
  return parts;
}

const ROLL_TABLE_HEADER = /^Roll( \w+)? Effect$/;
const ROW_LABEL = /^(\d+(?:–\d+)?)\s+(.*)$/;

// Rebuilds a small "Roll | Effect" table printed inside a feature as one "◦ roll: effect" line per row.
function rollTables(paragraphs) {
  const out = [];
  for (let i = 0; i < paragraphs.length; i++) {
    const header = paragraphs[i].lines[0].match(ROLL_TABLE_HEADER);
    if (!header) {
      out.push(paragraphs[i]);
      continue;
    }
    const lines = paragraphs[i].lines.slice(1);
    while (i + 1 < paragraphs.length && paragraphs[i + 1].lines.some(line => ROW_LABEL.test(line))) lines.push(...paragraphs[++i].lines);
    // A table whose rows each start with their roll ends at the first line without one.
    const end = ROW_LABEL.test(lines[0]) ? lines.findIndex(line => !ROW_LABEL.test(line)) : -1;
    const rows = end < 0 ? lines : lines.slice(0, end);
    out.push({ ...paragraphs[i], lines: tableRows(rows, Boolean(header[1])) });
    if (end >= 0) out.push({ ...paragraphs[i], lines: lines.slice(end) });
  }
  return out;
}

function tableRows(lines, hasMiddleColumn) {
  const labelled = lines.every(line => ROW_LABEL.test(line));
  const labels = lines.filter(line => ROW_LABEL.test(line)).map(line => line.match(ROW_LABEL)[1]);
  const texts = labelled
    ? lines.map(line => line.match(ROW_LABEL)[2])
    : prose(lines.map(line => line.replace(ROW_LABEL, '$2'))).split(/(?<=\.)\s+/);
  if (texts.length !== labels.length) throw new Error(`Roll table rows do not line up: ${lines.join(' | ')}`);
  return texts.map((text, index) => {
    if (!hasMiddleColumn) return `◦ ${labels[index]}: ${text.trim()}`;
    const [middle, ...rest] = text.trim().split(/\s+/);
    return `◦ ${labels[index]} (${middle}): ${rest.join(' ')}`;
  });
}

// Paragraphs that open with "Name: text" start a feature; any other paragraph continues the last one.
// A section's first paragraph may also use "Name. text", as a few SRD features do.
export function features(paragraphs) {
  const out = [];
  for (const token of rollTables(paragraphs.flatMap(splitFeatureParagraphs))) {
    const periodMatch = out.length === 0 ? token.lines[0].match(FEATURE_START_PERIOD) : null;
    const start = featureStart(token.lines[0]) ?? (periodMatch ? { name: periodMatch[1], rest: periodMatch[2] } : null);
    if (start) {
      out.push({ name: start.name, text: prose([start.rest, ...token.lines.slice(1)]) });
    } else if (out.length > 0) {
      out.at(-1).text += `${BULLET.test(token.lines[0]) ? '\n' : '\n\n'}${paragraphText(token)}`;
    } else {
      throw new Error(`Feature text without a feature name: "${token.lines[0]}" (page ${token.page})`);
    }
  }
  return out;
}

export function slugify(name) {
  return name
    .normalize('NFKD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[’']/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
}

export function titleCase(heading) {
  return heading
    .toLowerCase()
    .replace(/(^|[\s(-])([a-z])/g, (_, before, letter) => before + letter.toUpperCase())
    .replace(/\b(Of|The|And|A|An|In|On|To|For)\b/g, (word, _w, offset) => (offset === 0 ? word : word.toLowerCase()));
}

// Splits "a, b, c, and d" into its items.
export function listItems(text) {
  return text
    .replace(/\.$/, '')
    .split(/,\s*(?:and\s+)?|\s+and\s+/)
    .map(item => item.trim())
    .filter(Boolean);
}
