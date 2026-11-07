import { useAuth } from '../auth/AuthProvider';
import { portraitPresetsFor } from '../characters/portraitPresets';
import { onlySystemId } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { useHomeText } from '../locales/platformMessages';
import { AppPage } from '../theme/AppPage';
import { SystemCards } from './SystemCards';
import './home.css';

const HERO_PORTRAITS: { presetId: string; left: number; top: number; tilt: number }[] = [
  { presetId: '10064', left: 20, top: 30, tilt: -6 },
  { presetId: '10076', left: 170, top: 0, tilt: 4 },
  { presetId: '10088', left: 320, top: 40, tilt: -3 },
  { presetId: '17970', left: 90, top: 150, tilt: 5 },
  { presetId: '10094', left: 250, top: 170, tilt: -5 },
];

const FEATURES = [
  { id: 'guided', icon: '🧭' },
  { id: 'play', icon: '📜' },
  { id: 'dice', icon: '🎲' },
  { id: 'roster', icon: '🖼️' },
] as const;

/** What a visitor sees before signing in: what the vault does and which games it plays. */
export function LandingPage() {
  const { signIn, signUp } = useAuth();
  const t = useHomeText();
  const presets = portraitPresetsFor('dnd-5e');
  const heroArt = HERO_PORTRAITS.flatMap((spot) => {
    const preset = presets.find((candidate) => candidate.id === spot.presetId);
    return preset ? [{ ...spot, url: preset.url }] : [];
  });

  return (
    <AppPage>
      <AppHeader userName={null} />

      <section className="landing-hero">
        <div className="app-wrap">
          <div>
            <h1 className="app-heading">
              {t('landing.titleLine1')}
              <br />
              {t('landing.titleLine2')}
            </h1>
            <p>{t('landing.lead')}</p>
            <div className="landing-hero__cta">
              <button type="button" className="app-btn app-btn--primary" onClick={() => signIn(onlySystemId() ? `/characters/new/${onlySystemId()}` : '/characters/new')}>
                {t('landing.createCharacter')}
              </button>
              <button type="button" className="app-btn" onClick={() => signUp('/')}>
                {t('landing.createAccount')}
              </button>
              <button type="button" className="app-btn" onClick={() => signIn('/')}>
                {t('landing.haveAccount')}
              </button>
            </div>
          </div>
          <div className="landing-hero__art" aria-hidden="true">
            {heroArt.map((spot) => (
              <img key={spot.presetId} src={spot.url} alt="" style={{ left: spot.left, top: spot.top, transform: `rotate(${spot.tilt}deg)` }} />
            ))}
          </div>
        </div>
      </section>

      <section className="landing-section">
        <div className="app-wrap">
          <div className="landing-section__head">
            <h2 className="app-heading">{t('landing.featuresTitle')}</h2>
          </div>
          <div className="landing-features">
            {FEATURES.map((feature) => (
              <div key={feature.id} className="landing-feature">
                <div className="landing-feature__icon" aria-hidden="true">
                  {feature.icon}
                </div>
                <h3 className="app-heading">{t(`landing.features.${feature.id}.title`)}</h3>
                <p>{t(`landing.features.${feature.id}.text`)}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="landing-section" style={{ paddingTop: 0 }}>
        <div className="app-wrap">
          <div className="landing-section__head">
            <h2 className="app-heading">{t('landing.pickGame')}</h2>
            <span className="app-muted">{t('landing.moreSoon')}</span>
          </div>
          <SystemCards onCreate={(systemId) => signIn(`/characters/new/${systemId}`)} />
        </div>
      </section>

      <AppFooter />
    </AppPage>
  );
}
