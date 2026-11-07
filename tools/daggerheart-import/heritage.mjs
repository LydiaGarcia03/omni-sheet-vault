// Parses ancestries, communities and transformations: a heading, prose, then a features heading.

import { bulletItems, features, joinParagraphs, listItems, paragraphText, titleCase } from './text.mjs';

const FEATURES_HEADING = /^(ANCESTRY|COMMUNITY|TRANSFORMATION) FEATURES?$/;

// Splits a chapter into entries: each heading followed (after prose) by a features heading.
function entries(tokens, chapterHeading, nextChapterHeading) {
  const start = tokens.findIndex(token => token.type === 'heading' && token.text === chapterHeading);
  if (start < 0) throw new Error(`Chapter ${chapterHeading} not found`);
  const blocks = [];
  let current = null;
  for (const token of tokens.slice(start + 1)) {
    if (token.type === 'heading' && token.text === nextChapterHeading) break;
    if (!current && token.type === 'heading' && FEATURES_HEADING.test(token.text)) continue;
    if (token.type === 'heading' && !FEATURES_HEADING.test(token.text) && token.text !== 'TRANSFORMATION QUESTIONS') {
      current = { heading: token.text, page: token.page, prose: [], features: [], questions: [], section: 'prose' };
      blocks.push(current);
    } else if (token.type === 'heading') {
      current.section = FEATURES_HEADING.test(token.text) ? 'features' : 'questions';
    } else if (current) {
      current[current.section].push(token);
    }
  }
  return blocks;
}

export function parseAncestries(tokens) {
  const ancestries = [];
  let family = null;
  for (const block of entries(tokens, 'ANCESTRIES', 'COMMUNITIES')) {
    if (block.heading === 'MIXED ANCESTRY' || block.heading === 'ANCESTRY FEATURES') continue;
    if (block.features.length === 0) {
      family = { name: titleCase(block.heading), suffix: block.heading.split(' ').at(-1), description: joinParagraphs(block.prose) };
      continue;
    }
    if (family && !block.heading.endsWith(family.suffix)) family = null;
    ancestries.push({
      name: titleCase(block.heading),
      page: block.page,
      description: joinParagraphs(block.prose),
      family: family ? { name: family.name, description: family.description } : null,
      features: features(block.features),
    });
  }
  return ancestries;
}

export function parseCommunities(tokens) {
  return entries(tokens, 'COMMUNITIES', 'TRANSFORMATIONS').map(block => {
    const name = titleCase(block.heading);
    const adjectiveIndex = block.prose.findIndex(token => paragraphText(token).startsWith(`${name} are often `));
    if (adjectiveIndex < 0) throw new Error(`${name}: adjectives sentence not found`);
    const adjectives = listItems(paragraphText(block.prose[adjectiveIndex]).slice(`${name} are often `.length));
    return {
      name,
      page: block.page,
      description: joinParagraphs(block.prose.filter((_, index) => index !== adjectiveIndex)),
      adjectives,
      features: features(block.features),
    };
  });
}

export function parseTransformations(tokens) {
  return entries(tokens, 'TRANSFORMATIONS', 'CORE MECHANICS')
    .filter(block => block.features.length > 0)
    .map(block => ({
      name: titleCase(block.heading),
      page: block.page,
      description: joinParagraphs(block.prose),
      features: features(block.features),
      questions: block.questions.flatMap(bulletItems),
    }));
}

// The SRD's "Core Set includes only the following …: A, B, and C." sentence for one chapter.
export function coreSetNames(tokens, chapterHeading) {
  const start = tokens.findIndex(token => token.type === 'heading' && token.text === chapterHeading);
  for (const token of tokens.slice(start + 1)) {
    if (token.type === 'heading') break;
    const match = paragraphText(token).match(/Core Set includes only the following[^:]*: ([^.]+)\./);
    if (match) return listItems(match[1]);
  }
  throw new Error(`No Core Set list under ${chapterHeading}`);
}
