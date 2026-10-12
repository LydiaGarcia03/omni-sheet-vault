import { useRef } from 'react';
import { SUPPORTED_SYSTEMS, UPCOMING_SYSTEMS } from '../characters/supportedSystems';

type SystemCardsProps = {
  /** Smaller cards: the home's "Start something new" strip. */
  compact?: boolean;
  /** The system whose character is being created right now: its button shows "Creating…" and every button waits. */
  creating?: string | null;
  onCreate: (systemId: string) => void;
};

/** Every game system the vault offers: the playable ones with "Create character", the rest marked "Coming soon". */
export function SystemCards({ compact = false, creating = null, onCreate }: SystemCardsProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scroll = (direction: 'left' | 'right') => {
    const container = scrollRef.current;

    if (!container) return;

    const scrollAmount = container.clientWidth * 0.7;

    container.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth',
    });
  };

  return (
      <div className="system-carousel">
        <button
            type="button"
            className="system-carousel__arrow system-carousel__arrow--left"
            onClick={() => scroll('left')}
            aria-label="Previous systems"
        >
          ‹
        </button>

        <div
            ref={scrollRef}
            className={`system-cards${compact ? ' is-compact' : ''}`}
        >
          {SUPPORTED_SYSTEMS.map((system) => (
              <div
                  key={system.id}
                  className="system-card"
                  style={{ background: system.cardBackground }}
              >
            <span className="system-card__badge is-live">
              Available
            </span>

                <div className="system-card__name">
                  {system.label}
                </div>

                <div className="system-card__tag">
                  {system.tagline}
                </div>

                <button
                    type="button"
                    className="app-btn app-btn--primary app-btn--small system-card__action"
                    disabled={creating !== null}
                    onClick={() => onCreate(system.id)}
                >
                  {creating === system.id
                      ? 'Creating…'
                      : 'Create character'}
                </button>
              </div>
          ))}

          {UPCOMING_SYSTEMS.map((system) => (
              <div
                  key={system.label}
                  className="system-card is-soon"
                  style={{ background: system.cardBackground }}
              >
            <span className="system-card__badge">
              Coming soon
            </span>

                <div className="system-card__name">
                  {system.label}
                </div>

                <div className="system-card__tag">
                  {system.tagline}
                </div>
              </div>
          ))}
        </div>

        <button
            type="button"
            className="system-carousel__arrow system-carousel__arrow--right"
            onClick={() => scroll('right')}
            aria-label="Next systems"
        >
          ›
        </button>
      </div>
  );
}