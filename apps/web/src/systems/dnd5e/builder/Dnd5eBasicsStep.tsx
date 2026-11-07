import { useEffect, useState, type ReactNode } from 'react';
import { CheckListSelect } from '../../../builder/CheckListSelect';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { Segmented } from '../../../builder/Segmented';
import { SourceSelect } from '../../../builder/SourceSelect';
import type { Portrait } from '../../../characters/portrait';
import { getCataloguePartners, getCatalogueSources, type CataloguePartner, type CatalogueSource } from '../../../catalogue/api';
import { useDnd5eText } from '../locales/dnd5eMessages';
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
  const t = useDnd5eText();
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
      <section className="builder-frame" data-tutorial="basics-identity">
        <div className="builder-frame__title">{t('builder.basics.identity')}</div>
        <div className="builder-identity">
          <div className="builder-identity__name">
            <label className="builder-label" htmlFor="builder-name">
              {t('builder.basics.characterName')}
            </label>
            <input id="builder-name" className="builder-input" value={name} maxLength={120} onChange={(event) => onNameChange(event.target.value)} />
            {name.trim() === '' && <p className="builder-problem">{t('builder.basics.needsName')}</p>}
          </div>
          <PortraitPicker characterId={characterId} systemId={systemId} portrait={portrait} onChange={onPortraitChange} />
        </div>
      </section>

      <section className="builder-frame" data-tutorial="basics-sources">
        <div className="builder-frame__title">{t('builder.basics.contentSources')}</div>
        <span className="builder-label">{t('builder.basics.booksLabel')}</span>
        <CheckListSelect
          ariaLabel={t('builder.basics.booksLabel')}
          summary={
            <>
              {LOCKED_SOURCES.map((book) => (
                <span key={book} className="builder-tag is-locked">
                  {book}
                </span>
              ))}
              <span className="builder-tag">
                {preferences.sources === null
                  ? t('builder.basics.everyOtherBook', { count: optional.length })
                  : t('builder.basics.someBooks', { count: enabledOptional.length, total: optional.length })}
              </span>
            </>
          }
          message={sources === null ? t('builder.basics.loadingBooks') : undefined}
          groups={[
            {
              label: t('builder.basics.alwaysOn'),
              options: LOCKED_SOURCES.map((book) => ({ key: book, label: book, note: t('builder.basics.alwaysOnNote'), checked: true, locked: true })),
            },
            {
              label: t('builder.basics.otherBooks'),
              options: orderOptionalSources(sources ?? []).map((source) => ({
                key: source.sourceBook,
                label: source.sourceBook,
                note: t('builder.basics.entries', { count: source.entryCount }),
                checked: isOn(source.sourceBook),
              })),
            },
          ]}
          onToggle={(book) => onChange(withSourceToggled(build, book, allBooks))}
        />
        <p className="builder-muted">{t('builder.basics.coreNote')}</p>
        <div className="builder-row">
          <button type="button" className="builder-btn" onClick={() => onChange(withPreferences(build, { sources: null }))}>
            {t('builder.basics.enableAll')}
          </button>
          <button type="button" className="builder-btn" onClick={() => onChange(withPreferences(build, { sources: [] }))}>
            {t('builder.basics.coreOnly')}
          </button>
        </div>
      </section>

      <section className="builder-frame" data-tutorial="basics-extra">
        <div className="builder-frame__title">{t('builder.basics.extraTitle')}</div>
        <Setting
          title={t('terms.builder.playtestContent')}
          description={
            playtestSources.length === 0
              ? t('builder.basics.playtestNone')
              : t('builder.basics.playtestSome', { books: playtestSources.map((source) => source.sourceBook).join(', ') })
          }
        >
          <Switch label={t('terms.builder.playtestContent')} on={preferences.playtestContent} onChange={(on) => onChange(withPreferences(build, { playtestContent: on }))} />
        </Setting>
        <Setting
          title={t('terms.builder.partneredContent')}
          description={t('builder.basics.partneredDescription')}
          below={
            preferences.partneredContent && (
              <CheckListSelect
                ariaLabel={t('builder.basics.choosePartners')}
                summary={
                  <span className="builder-tag">
                    {preferences.partners === null
                      ? t('builder.basics.everyPartner', { count: allPartners.length })
                      : t('builder.basics.somePartners', { count: chosenPartners.length, total: allPartners.length })}
                  </span>
                }
                message={partners === null ? t('builder.basics.loadingPartners') : partners.length === 0 ? t('builder.basics.noPartners') : undefined}
                groups={[
                  {
                    label: t('builder.basics.partners'),
                    options: (partners ?? []).map((partner) => ({
                      key: partner.name,
                      label: partner.name,
                      note: partner.sourceBooks.join(', '),
                      checked: isPartnerOn(partner.name),
                    })),
                  },
                ]}
                onToggle={(name) => onChange(withPartnerToggled(build, name, allPartners))}
              />
            )
          }
        >
          <Switch
            label={t('builder.basics.enablePartnered')}
            on={preferences.partneredContent}
            onChange={(on) => onChange(withPreferences(build, { partneredContent: on }))}
          />
        </Setting>
      </section>

      <section className="builder-frame" data-tutorial="basics-rules">
        <div className="builder-frame__title">{t('builder.basics.rules')}</div>
        <Setting title={t('terms.builder.advancementType')} description={t('builder.basics.advancementDescription')}>
          <Segmented
            value={preferences.advancement}
            options={[
              ['MILESTONE', t('terms.builder.milestone')],
              ['XP', t('terms.builder.experiencePoints')],
            ]}
            onChange={(advancement) => onChange(withPreferences(build, { advancement }))}
          />
        </Setting>
        <Setting title={t('terms.builder.hitPointsOnLevelUp')} description={t('builder.basics.hitPointsDescription')}>
          <Segmented
            value={build.hitPointMethod}
            options={[
              ['FIXED', t('terms.builder.fixed')],
              ['ROLLED', t('terms.builder.rolled')],
            ]}
            onChange={(method) => onChange(withHitPointMethod(build, method))}
          />
        </Setting>
        <Setting title={t('terms.builder.optionalClassFeatures')} description={t('builder.basics.optionalFeaturesDescription')}>
          <Switch label={t('terms.builder.optionalClassFeatures')} on={preferences.optionalClassFeatures} onChange={(on) => onChange(withPreferences(build, { optionalClassFeatures: on }))} />
        </Setting>
        <Setting title={t('terms.builder.featPrerequisites')} description={t('builder.basics.featPrerequisitesDescription')}>
          <Switch label={t('terms.builder.featPrerequisites')} on={preferences.featPrerequisites} onChange={(on) => onChange(withPreferences(build, { featPrerequisites: on }))} />
        </Setting>
        <Setting title={t('terms.builder.multiclassPrerequisites')} description={t('builder.basics.multiclassDescription')}>
          <Switch label={t('terms.builder.multiclassPrerequisites')} on={preferences.multiclassPrerequisites} onChange={(on) => onChange(withPreferences(build, { multiclassPrerequisites: on }))} />
        </Setting>
        <Setting title={t('terms.builder.encumbrance')} description={t('builder.basics.encumbranceDescription')}>
          <div className="builder-setting__select">
            <SourceSelect
              ariaLabel={t('terms.builder.encumbrance')}
              placeholder={t('terms.builder.standardEncumbrance')}
              options={[
                { key: 'STANDARD', label: t('terms.builder.standardEncumbrance'), sourceBook: null },
                { key: 'NONE', label: t('terms.builder.noEncumbrance'), sourceBook: null },
              ]}
              value={preferences.encumbrance}
              onChange={(key) => key && onChange(withPreferences(build, { encumbrance: key as 'STANDARD' | 'NONE' }))}
            />
          </div>
        </Setting>
        <Setting title={t('builder.basics.ignoreCoinWeight')} description={t('builder.basics.ignoreCoinWeightDescription')}>
          <Switch label={t('builder.basics.ignoreCoinWeight')} on={preferences.ignoreCoinWeight} onChange={(on) => onChange(withPreferences(build, { ignoreCoinWeight: on }))} />
        </Setting>
      </section>
    </>
  );
}

/** One setting row: title and description beside its control; `below` spans the row's full width under them. */
function Setting({ title, description, children, below }: { title: string; description: string; children: ReactNode; below?: ReactNode }) {
  return (
    <div className="builder-setting">
      <div>
        <b>{title}</b>
        <div className="builder-muted">{description}</div>
      </div>
      {children}
      {below && <div className="builder-setting__below">{below}</div>}
    </div>
  );
}

function Switch({ label, on, onChange }: { label: string; on: boolean; onChange: (on: boolean) => void }) {
  return <button type="button" role="switch" aria-checked={on} aria-label={label} className={`builder-switch${on ? ' is-on' : ''}`} onClick={() => onChange(!on)} />;
}
