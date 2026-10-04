import type { SelectionDetail } from './draftApi';
import { SourceBadge } from './SourceSelect';

/** The chosen option's name, book and facts as a card, then what it grants under {@code grantsTitle}. */
export function SelectionCard({ detail, grantsTitle }: { detail: SelectionDetail; grantsTitle: string }) {
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
              <div key={fact.label} className="builder-facts__item">
                <dt className="builder-label">{fact.label}</dt>
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
                <div className="builder-grant__title">{grant.label}</div>
                {grant.text && <div className="builder-grant__text">{grant.text}</div>}
              </div>
            ))}
          </div>
        </section>
      )}
    </>
  );
}
