import { useEffect, useState, type ReactNode } from 'react';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { Segmented } from '../../../builder/Segmented';
import type { Portrait } from '../../../characters/portrait';
import { getCataloguePartners, getCatalogueSources, type CataloguePartner, type CatalogueSource } from '../../../catalogue/api';
import {
  LOCKED_SOURCES,
  orderOptionalSources,
  preferencesOf,
  withHitPointMethod,
  withPartnerToggled,
  withPreferences,
  withSourceToggled,
  type Dnd5eBuild,
} from './dnd5eBuild';

type Dnd5eBasicsStepProps = {
  characterId: string;
  systemId: string;
  name: string;
  onNameChange: (name: string) => void;
  portrait: Portrait | null;
  onPortraitChange: (portrait: Portrait | null) => void;
  build: Dnd5eBuild;
  onChange: (build: Dnd5eBuild) => void;
};

/** Identity, the books the builder draws from, and the table rules this character follows. */
export function Dnd5eBasicsStep({ characterId, systemId, name, onNameChange, portrait, onPortraitChange, build, onChange }: Dnd5eBasicsStepProps) {
  const [sources, setSources] = useState<CatalogueSource[] | null>(null);
  useEffect(() => {
    getCatalogueSources('dnd-5e')
      .then(setSources)
      .catch(() => setSources([]));
  }, []);
  const [partners, setPartners] = useState<CataloguePartner[] | null>(null);
  useEffect(() => {
    getCataloguePartners('dnd-5e')
      .then(setPartners)
      .catch(() => setPartners([]));
  }, []);
  const preferences = preferencesOf(build);
  const books = (sources ?? []).filter((source) => !source.playtest && !source.partner);
  const playtestSources = (sources ?? []).filter((source) => source.playtest);
  const allPartners = (partners ?? []).map((partner) => partner.name);
  const isPartnerOn = (partner: string) => preferences.partners === null || preferences.partners.includes(partner);
  const chosenPartners = allPartners.filter(isPartnerOn);
  const allBooks = books.map((source) => source.sourceBook);
  const isOn = (book: string) => LOCKED_SOURCES.includes(book) || preferences.sources === null || preferences.sources.includes(book);
  const optional = allBooks.filter((book) => !LOCKED_SOURCES.includes(book));
  const enabledOptional = optional.filter(isOn);

  return (
    <>
      <section className="builder-frame">
        <div className="builder-frame__title">Identity</div>
        <div className="builder-identity">
          <div className="builder-identity__name">
            <label className="builder-label" htmlFor="builder-name">
              Character name
            </label>
            <input id="builder-name" className="builder-input" value={name} maxLength={120} onChange={(event) => onNameChange(event.target.value)} />
            {name.trim() === '' && <p className="builder-problem">A character needs a name.</p>}
          </div>
          <PortraitPicker characterId={characterId} systemId={systemId} portrait={portrait} onChange={onPortraitChange} />
        </div>
      </section>

      <section className="builder-frame">
        <div className="builder-frame__title">Content sources</div>
        <span className="builder-label">Books whose options appear in this builder</span>
        <details className="builder-multi">
          <summary className="builder-input builder-multi__summary">
            {LOCKED_SOURCES.map((book) => (
              <span key={book} className="builder-tag is-locked">
                {book}
              </span>
            ))}
            {preferences.sources === null ? (
              <span className="builder-tag">Every other book ({optional.length})</span>
            ) : (
              <span className="builder-tag">
                {enabledOptional.length} of {optional.length} other books
              </span>
            )}
          </summary>
          <div className="builder-multi__menu">
            {sources === null && <p className="builder-muted">Loading books…</p>}
            <div className="builder-label builder-multi__group">Always on</div>
            {LOCKED_SOURCES.map((book) => (
              <label key={book} className="builder-multi__option is-locked">
                <input type="checkbox" checked disabled /> {book}
                <small>always on</small>
              </label>
            ))}
            <div className="builder-label builder-multi__group">Other books</div>
            {orderOptionalSources(sources ?? []).map((source) => (
              <label key={source.sourceBook} className="builder-multi__option">
                <input type="checkbox" checked={isOn(source.sourceBook)} onChange={() => onChange(withSourceToggled(build, source.sourceBook, allBooks))} />{' '}
                {source.sourceBook}
                <small>{source.entryCount} entries</small>
              </label>
            ))}
          </div>
        </details>
        <p className="builder-muted">
          The 2014 Player's Handbook and Dungeon Master's Guide can't be turned off: every other book builds on them. A book turned off stops
          appearing in species, classes, backgrounds, feats and spells.
        </p>
        <div className="builder-row">
          <button type="button" className="builder-btn" onClick={() => onChange(withPreferences(build, { sources: null }))}>
            Enable all
          </button>
          <button type="button" className="builder-btn" onClick={() => onChange(withPreferences(build, { sources: [] }))}>
            Core only
          </button>
        </div>
      </section>

      <section className="builder-frame">
        <div className="builder-frame__title">Partnered &amp; playtest content</div>
        <Setting
          title="Playtest content"
          description={
            playtestSources.length === 0
              ? 'Unearthed Arcana: rules still being tested. None are imported yet.'
              : `Unearthed Arcana: rules still being tested. ${playtestSources.map((source) => source.sourceBook).join(', ')}.`
          }
        >
          <Switch label="Playtest content" on={preferences.playtestContent} onChange={(on) => onChange(withPreferences(build, { playtestContent: on }))} />
        </Setting>
        <Setting title="Partnered content" description="Content developed by partner publishers and brands. Choose which partners to allow.">
          <Switch
            label="Enable partnered content"
            on={preferences.partneredContent}
            onChange={(on) => onChange(withPreferences(build, { partneredContent: on }))}
          />
        </Setting>
        {preferences.partneredContent && (
          <details className="builder-multi">
            <summary className="builder-input builder-multi__summary" aria-label="Choose partners">
              <span className="builder-tag">
                {preferences.partners === null ? `Every partner (${allPartners.length})` : `${chosenPartners.length} of ${allPartners.length} partners`}
              </span>
            </summary>
            <div className="builder-multi__menu">
              {partners === null && <p className="builder-muted">Loading partners…</p>}
              {partners?.length === 0 && <p className="builder-muted">No partnered books are available yet.</p>}
              {(partners ?? []).map((partner) => (
                <label key={partner.name} className="builder-multi__option" title={partner.sourceBooks.join(', ')}>
                  <input type="checkbox" checked={isPartnerOn(partner.name)} onChange={() => onChange(withPartnerToggled(build, partner.name, allPartners))} />{' '}
                  {partner.name}
                  <small>{partner.sourceBooks.join(', ')}</small>
                </label>
              ))}
            </div>
          </details>
        )}
      </section>

      <section className="builder-frame">
        <div className="builder-frame__title">Rules</div>
        <Setting title="Advancement type" description="Level up at story milestones, or by earning experience points.">
          <Segmented
            value={preferences.advancement}
            options={[
              ['MILESTONE', 'Milestone'],
              ['XP', 'XP'],
            ]}
            onChange={(advancement) => onChange(withPreferences(build, { advancement }))}
          />
        </Setting>
        <Setting title="Hit points on level up" description="Take the class average, or roll and type the result.">
          <Segmented
            value={build.hitPointMethod}
            options={[
              ['FIXED', 'Fixed'],
              ['ROLLED', 'Rolled'],
            ]}
            onChange={(method) => onChange(withHitPointMethod(build, method))}
          />
        </Setting>
        <Setting title="Optional class features" description="Tasha's optional and replacement class features.">
          <Switch label="Optional class features" on={preferences.optionalClassFeatures} onChange={(on) => onChange(withPreferences(build, { optionalClassFeatures: on }))} />
        </Setting>
        <Setting title="Feat prerequisites" description="Only offer feats whose requirements are met.">
          <Switch label="Feat prerequisites" on={preferences.featPrerequisites} onChange={(on) => onChange(withPreferences(build, { featPrerequisites: on }))} />
        </Setting>
        <Setting title="Multiclass prerequisites" description="Require 13 in the abilities each class asks for.">
          <Switch label="Multiclass prerequisites" on={preferences.multiclassPrerequisites} onChange={(on) => onChange(withPreferences(build, { multiclassPrerequisites: on }))} />
        </Setting>
        <Setting title="Encumbrance" description="Standard: carry up to Strength × 15 pounds. Off: no weight tracking.">
          <select
            className="builder-input"
            style={{ width: 180 }}
            aria-label="Encumbrance"
            value={preferences.encumbrance}
            onChange={(event) => onChange(withPreferences(build, { encumbrance: event.target.value as 'STANDARD' | 'NONE' }))}
          >
            <option value="STANDARD">Standard</option>
            <option value="NONE">No encumbrance</option>
          </select>
        </Setting>
        <Setting title="Ignore coin weight" description="Coins don't count toward the weight you carry (50 coins = 1 lb).">
          <Switch label="Ignore coin weight" on={preferences.ignoreCoinWeight} onChange={(on) => onChange(withPreferences(build, { ignoreCoinWeight: on }))} />
        </Setting>
      </section>
    </>
  );
}

function Setting({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return (
    <div className="builder-setting">
      <div>
        <b>{title}</b>
        <div className="builder-muted">{description}</div>
      </div>
      {children}
    </div>
  );
}

function Switch({ label, on, onChange }: { label: string; on: boolean; onChange: (on: boolean) => void }) {
  return <button type="button" role="switch" aria-checked={on} aria-label={label} className={`builder-switch${on ? ' is-on' : ''}`} onClick={() => onChange(!on)} />;
}
