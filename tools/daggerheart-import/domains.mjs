// Parses the ten domains (Core Materials) and the domain card reference (Appendix).

import { continues, joinParagraphs, listItems, prose, titleCase } from './text.mjs';

const CARD_LEVEL = /^Level (\d+) (\w+) (Ability|Spell|Grimoire)$/;
const RECALL_COST = /^Recall Cost: (\d+)$/;

export function parseDomains(tokens) {
  const start = tokens.findIndex(token => token.type === 'heading' && token.text === 'DOMAINS');
  const domains = [];
  for (const token of tokens.slice(start + 1)) {
    if (token.type === 'heading') {
      if (/ /.test(token.text)) break;
      domains.push({ name: titleCase(token.text), page: token.page, paras: [] });
    } else if (domains.length > 0) {
      domains.at(-1).paras.push(token);
    }
  }
  return domains.map(domain => {
    const description = joinParagraphs(domain.paras.slice(0, 1));
    const access = description.match(/can be accessed by the (.+?) classes/);
    if (!access) throw new Error(`${domain.name}: class access sentence not found`);
    return { name: domain.name, page: domain.page, description, classes: listItems(access[1]) };
  });
}

export function parseDomainCards(tokens) {
  const cards = [];
  let card = null;
  for (let i = 0; i < tokens.length; i++) {
    const token = tokens[i];
    if (token.type === 'heading') {
      card = cardHeading(tokens, i) ? { name: titleCase(token.text), page: token.page, lines: [] } : null;
      if (card) cards.push(card);
      continue;
    }
    if (card) card.lines.push(...(card.lines.length > 0 ? ['', ...token.lines] : token.lines));
  }
  return cards.map(finishCard);
}

function cardHeading(tokens, index) {
  const next = tokens[index + 1];
  return next?.type === 'para' && CARD_LEVEL.test(next.lines[0]);
}

function finishCard(card) {
  const [levelLine, costLine, ...rest] = card.lines;
  const level = levelLine.match(CARD_LEVEL);
  const cost = costLine.match(RECALL_COST);
  if (!cost) throw new Error(`${card.name}: Recall Cost line not found (page ${card.page})`);
  const paragraphs = splitOnBlank(rest.filter((line, index) => !(index === 0 && line === '')));
  return {
    name: card.name,
    page: card.page,
    level: Number(level[1]),
    domain: level[2],
    type: level[3].toLowerCase(),
    recallCost: Number(cost[1]),
    text: mergeContinued(paragraphs).map(prose).reduce((text, paragraph) => (text === '' ? paragraph : `${text}${/^[•◦]/.test(paragraph) ? '\n' : '\n\n'}${paragraph}`), ''),
  };
}

function splitOnBlank(lines) {
  const out = [[]];
  for (const line of lines) {
    if (line === '') out.push([]);
    else out.at(-1).push(line);
  }
  return out.filter(paragraph => paragraph.length > 0);
}

function mergeContinued(paragraphs) {
  const out = [];
  for (const paragraph of paragraphs) {
    const previous = out.at(-1);
    if (previous && continues({ lines: previous }, { lines: paragraph })) previous.push(...paragraph);
    else out.push([...paragraph]);
  }
  return out;
}
