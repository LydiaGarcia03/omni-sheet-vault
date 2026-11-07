// Splits `pdftotext -layout` pages into reading order: left column, then right column.

const GUTTER_MIN = 50;
const GUTTER_MAX = 80;

export function splitPages(layoutText) {
  return layoutText.split('\f').map((page, index) => ({ number: index + 1, text: splitColumns(page) }));
}

export function splitColumns(page) {
  const left = [];
  const right = [];
  for (const line of page.split(/\r?\n/)) {
    const [l, r] = splitLine(line);
    left.push(l);
    if (r !== null) right.push(r);
    else right.push('');
  }
  return [...trimBlankRuns(left), '', ...trimBlankRuns(right)].join('\n');
}

function splitLine(line) {
  const trimmedStart = line.length - line.trimStart().length;
  if (line.trim() === '') return ['', null];
  if (trimmedStart >= GUTTER_MIN) return ['', line.trim()];
  const gap = findGutterGap(line);
  if (gap === null) return [line.trimEnd(), null];
  return [line.slice(0, gap.start).trimEnd(), line.slice(gap.end).trim()];
}

// The right column starts after a run of 2+ spaces that ends inside the gutter band.
function findGutterGap(line) {
  const runs = [...line.matchAll(/ {2,}/g)]
    .map(m => ({ start: m.index, end: m.index + m[0].length }))
    .filter(run => run.start > 0 && run.end >= GUTTER_MIN && run.end <= GUTTER_MAX && run.end < line.length);
  if (runs.length === 0) return null;
  return runs.reduce((widest, run) => (run.end - run.start > widest.end - widest.start ? run : widest));
}

function trimBlankRuns(lines) {
  const out = [];
  for (const line of lines) {
    if (line === '' && out.at(-1) === '') continue;
    out.push(line);
  }
  return out;
}
