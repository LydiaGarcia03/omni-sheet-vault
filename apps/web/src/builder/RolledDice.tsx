import type { RollResult } from '../dice/api';
import { useBuilderText } from '../locales/platformMessages';

/** A roll's dice, the dropped ones struck through: "5 2 6 3" for 4d6 drop lowest. */
export function RolledDice({ roll }: { roll: RollResult }) {
  const tb = useBuilderText();
  const dropped = new Set(roll.dropped ?? []);
  return (
    <span className="builder-dice" aria-label={tb('rolled', { dice: roll.results.join(', ') })}>
      {roll.results.map((result, index) => (
        <span key={index} className={`builder-die${dropped.has(index) ? ' is-dropped' : ''}`}>
          {result}
        </span>
      ))}
    </span>
  );
}
