import type { SelectionDetail } from './draftApi';
import { SourceBadge } from './SourceSelect';

/** A fact longer than this takes a full row instead of a grid cell. */
const WIDE_FACT_LENGTH = 28;

type SelectionCardProps = {
  detail: SelectionDetail;
  grantsTitle: string;
  /** A fact or grant label as the player reads it; the API sends English labels. */
  labelOf?: (label: string) => string;
};

const asIs = (label: string) => label;

/** The chosen option's name, book and facts as a card, then what it grants under {@code grantsTitle}. */
export function SelectionCard({ detail, grantsTitle, labelOf = asIs }: SelectionCardProps) {
  return (
    <>
      {detail.facts.length > 0 && (
        <section className="builder-frame">
          <div className="builder-frame__title">
            <span>
              {detail.name} <SourceBadge sourceBook={detail.sourceBook} />
            </span>
          </div>
          <dl className="builder-facts">
            {detail.facts.map((fact) => (
              <div key={fact.label} className={`builder-facts__item${fact.text.length > WIDE_FACT_LENGTH ? ' is-wide' : ''}`}>
                <dt className="builder-label">{labelOf(fact.label)}</dt>
                <dd>{fact.text}</dd>
              </div>
            ))}
          </dl>
        </section>
      )}
      {detail.grants.length > 0 && (
        <section className="builder-frame">
          <div className="builder-frame__title">
            <span>{grantsTitle}</span>
            {detail.facts.length === 0 && <SourceBadge sourceBook={detail.sourceBook} />}
          </div>
          <div className="builder-grants">
            {detail.grants.map((grant) => (
              <div key={grant.label} className="builder-grant">
                <div className="builder-grant__title">{labelOf(grant.label)}</div>
                {grant.text && <div className="builder-grant__text">{grant.text}</div>}
              </div>
            ))}
          </div>
        </section>
      )}
    </>
  );
}
