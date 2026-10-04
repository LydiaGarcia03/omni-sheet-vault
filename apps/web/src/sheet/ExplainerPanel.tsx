import { Fragment } from 'react';
import type { ExplainerRequest } from './api';
import { RulesText } from './RulesText';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/**
 * DOM-measured against D&D Beyond's own Header.tsx + AbilityPane.tsx: a
 * preview icon (2rem, 0.313rem right margin) beside a single heading combining
 * the title with its value as a smaller trailing inline span in parens
 * (Header's own `.modifier`: 0.875rem, 0.313rem left margin, inline-flex,
 * vertical-align middle) — not a separate large number on its own line.
 */
export function ExplainerPanel({ title, icon, formattedValue, contributions, contributionsHeading, rulesText = '' }: ExplainerRequest) {
  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {icon && (
          <span style={{ display: 'inline-flex', width: '32px', height: '32px', marginRight: '5px', flexShrink: 0, color: 'var(--text-primary, #242528)' }}>
            {icon}
          </span>
        )}
        <h1
          style={{
            margin: 0,
            fontFamily: "var(--font-condensed, 'Roboto Condensed', Roboto, Helvetica, sans-serif)",
            fontSize: '18px',
            fontWeight: 700,
          }}
        >
          {title}
          <span style={{ display: 'inline-flex', verticalAlign: 'middle', marginLeft: '5px', fontSize: '14px', fontWeight: 400 }}>
            ({formattedValue})
          </span>
        </h1>
      </div>
      {contributionsHeading && (
        <div
          style={{
            marginTop: '12px',
            fontFamily: "var(--font-condensed, 'Roboto Condensed', Roboto, Helvetica, sans-serif)",
            fontSize: '12px',
            fontWeight: 700,
            textTransform: 'uppercase',
            color: 'var(--text-muted)',
          }}
        >
          {contributionsHeading}
        </div>
      )}
      {contributions.length > 0 && (
        <dl style={{ marginTop: contributionsHeading ? '4px' : '12px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {contributions.map((contribution, index) => (
            <Fragment key={index}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '8px', fontSize: '13px' }}>
                <div>{contribution.source}</div>
                <div style={{ margin: 0, fontWeight: 600 }}>{formatSigned(contribution.amount)}</div>
              </div>
            </Fragment>
          ))}
        </dl>
      )}
      <RulesText text={rulesText} />
    </div>
  );
}
