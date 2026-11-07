import { useState } from 'react';
import type { CharacterSheet, HitDice, HitDiceBySize, HitDicePool } from '../../sheet/api';
import { MarkBox } from '../../sheet/MarkBox';
import { ENGLISH_TERMS, useDnd5eTerms, type Dnd5eText, type Dnd5eTerms } from './locales/dnd5eMessages';

type RestKind = 'short' | 'long';

/** The names of what a rest brings back, in D&D Beyond's RECOVER line order: dice and slots, then features. */
export function restRecovery(sheet: CharacterSheet, kind: RestKind, { t }: Dnd5eTerms = ENGLISH_TERMS): string[] {
  const triggers = kind === 'short' ? ['SHORT_OR_LONG_REST'] : ['SHORT_OR_LONG_REST', 'LONG_REST'];
  const features = [...sheet.featureTraits, ...sheet.featureActions]
    .filter((feature) => feature.maxUses != null && triggers.includes(feature.rechargeTrigger ?? ''))
    .map((feature) => feature.name);
  const pact = sheet.spellSlots.some((slot) => slot.pact) ? [t('terms.labels.pactMagic')] : [];
  if (kind === 'short') {
    return [...new Set([...pact, ...features])];
  }
  const slots = sheet.spellSlots.filter((slot) => !slot.pact).reduce((sum, slot) => sum + slot.maxSlots, 0);
  const recoverable = Math.min(sheet.hitDice.longRestRecoveryMax, sheet.hitDice.max);
  return [
    ...new Set([
      t('rest.upToHitDice', { count: recoverable }),
      ...(slots > 0 ? [t('rest.spellSlots', { count: slots })] : []),
      ...pact,
      ...features,
    ]),
  ];
}

function Recover({ sources }: { sources: string[] }) {
  const { t } = useDnd5eTerms();
  if (sources.length === 0) {
    return null;
  }
  return (
    <section className="rest-pane__recover">
      <h2 className="rest-pane__section-heading">{t('rest.recover')}</h2>
      <p className="rest-pane__recover-sources">{sources.join(', ')}</p>
    </section>
  );
}

function formatBonus(bonus: number): string {
  if (bonus === 0) {
    return '';
  }
  return bonus > 0 ? `+${bonus}` : `−${Math.abs(bonus)}`;
}

type HitDieBoxesProps = {
  pool: HitDicePool;
  marked: number;
  firstOpen: number;
  verb: 'spend' | 'recover';
  onSelect: (count: number) => void;
};

/** The pool's dice as D&D Beyond's slot boxes; boxes before `firstOpen` are locked, the rest toggle the selection. */
function HitDieBoxes({ pool, marked, firstOpen, verb, onSelect }: HitDieBoxesProps) {
  const { t } = useDnd5eTerms();
  return (
    <div className="rest-pane__hitdie-boxes">
      {Array.from({ length: pool.max }, (_, index) => {
        const locked = index < firstOpen;
        const selectedCount = index - firstOpen + 1;
        return (
          <button
            key={index}
            type="button"
            className="mark-box-button"
            disabled={locked}
            aria-label={
              locked
                ? t('rest.dieAria', { size: pool.dieSize, number: index + 1 })
                : t(verb === 'spend' ? 'rest.spend' : 'rest.recoverDice', { count: selectedCount, size: pool.dieSize })
            }
            onClick={() => onSelect(index < marked ? selectedCount - 1 : selectedCount)}
          >
            <MarkBox marked={index < marked} />
          </button>
        );
      })}
    </div>
  );
}

function poolHeading(pool: HitDicePool, conModifier: number, t: Dnd5eText) {
  const name = pool.classNames && pool.classNames.length > 0 ? pool.classNames.join(' / ') : `d${pool.dieSize}`;
  return (
    <h3 className="rest-pane__hitdie-heading">
      <span className="rest-pane__hitdie-class">{name}</span>
      {t('rest.poolHeading', { hitDie: t('terms.labels.hitDie'), size: pool.dieSize, bonus: formatBonus(conModifier), total: pool.max })}
    </h3>
  );
}

function RestActions({ label, onTake, onReset }: { label: string; onTake: () => void; onReset: () => void }) {
  const { t } = useDnd5eTerms();
  return (
    <div className="rest-pane__actions">
      <button type="button" className="mutate rest-pane__button rest-pane__button--primary" onClick={onTake}>
        {label}
      </button>
      <button type="button" className="rest-pane__button" onClick={onReset}>
        {t('rest.reset')}
      </button>
    </div>
  );
}

type ShortRestBodyProps = {
  hitDice: HitDice;
  conModifier: number;
  recover: string[];
  onConfirm: (hitDiceBySize: HitDiceBySize) => void;
};

/**
 * D&D Beyond's short rest pane below the intro: RECOVER, then a box row per hit die pool where the
 * player marks the dice to spend (spent dice stay locked), then Take Short Rest / Reset.
 */
export function ShortRestBody({ hitDice, conModifier, recover, onConfirm }: ShortRestBodyProps) {
  const { t } = useDnd5eTerms();
  const [counts, setCounts] = useState<HitDiceBySize>({});

  return (
    <>
      <Recover sources={recover} />
      <section className="rest-pane__hitdice">
        {hitDice.pools.map((pool) => {
          const selected = counts[pool.dieSize] ?? 0;
          return (
            <div key={pool.dieSize} className="rest-pane__hitdie">
              {poolHeading(pool, conModifier, t)}
              <HitDieBoxes
                pool={pool}
                marked={pool.used + selected}
                firstOpen={pool.used}
                verb="spend"
                onSelect={(count) => setCounts({ ...counts, [pool.dieSize]: count })}
              />
            </div>
          );
        })}
      </section>
      <RestActions label={t('rest.takeShortRest')} onTake={() => onConfirm(counts)} onReset={() => setCounts({})} />
    </>
  );
}

type LongRestBodyProps = {
  hitDice: HitDice;
  conModifier: number;
  recover: string[];
  onConfirm: (hitDiceRecovered?: HitDiceBySize) => void;
};

/** Whether the player has a real choice: spent dice of several sizes, and more spent than the rest returns. */
export function hasHitDiceRecoveryChoice(hitDice: HitDice): boolean {
  const spentPools = hitDice.pools.filter((pool) => pool.used > 0);
  return spentPools.length > 1 && hitDice.used > hitDice.longRestRecoveryMax;
}

/** The largest dice first, up to the rest's limit — the same default the server applies. */
function defaultRecovery(hitDice: HitDice): HitDiceBySize {
  let remaining = hitDice.longRestRecoveryMax;
  const recovered: HitDiceBySize = {};
  for (const pool of hitDice.pools) {
    const amount = Math.min(remaining, pool.used);
    recovered[pool.dieSize] = amount;
    remaining -= amount;
  }
  return recovered;
}

/**
 * D&D Beyond's long rest pane below the intro: RECOVER, then Take Long Rest / Reset. When spent dice of several
 * sizes exceed what the rest returns, the player marks which come back (owner decision, C2b), one box row per pool.
 */
export function LongRestBody({ hitDice, conModifier, recover, onConfirm }: LongRestBodyProps) {
  const { t } = useDnd5eTerms();
  const choosing = hasHitDiceRecoveryChoice(hitDice);
  const [recovered, setRecovered] = useState<HitDiceBySize>(() => defaultRecovery(hitDice));
  const chosen = Object.values(recovered).reduce((sum, amount) => sum + amount, 0);

  function setAmount(pool: HitDicePool, value: number) {
    const others = chosen - (recovered[pool.dieSize] ?? 0);
    const limit = Math.min(pool.used, hitDice.longRestRecoveryMax - others);
    setRecovered({ ...recovered, [pool.dieSize]: Math.min(limit, Math.max(0, value)) });
  }

  return (
    <>
      <Recover sources={recover} />
      {choosing && (
        <section className="rest-pane__hitdice">
          <p className="rest-pane__hint">
            {t('rest.markUpTo', { max: hitDice.longRestRecoveryMax, left: hitDice.longRestRecoveryMax - chosen })}
          </p>
          {hitDice.pools
            .filter((pool) => pool.used > 0)
            .map((pool) => (
              <div key={pool.dieSize} className="rest-pane__hitdie">
                {poolHeading(pool, conModifier, t)}
                <HitDieBoxes
                  pool={{ ...pool, max: pool.used }}
                  marked={recovered[pool.dieSize] ?? 0}
                  firstOpen={0}
                  verb="recover"
                  onSelect={(count) => setAmount(pool, count)}
                />
              </div>
            ))}
        </section>
      )}
      <RestActions
        label={t('rest.takeLongRest')}
        onTake={() => onConfirm(choosing ? recovered : undefined)}
        onReset={() => setRecovered(defaultRecovery(hitDice))}
      />
    </>
  );
}
