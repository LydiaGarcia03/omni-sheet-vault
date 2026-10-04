import { useAuth } from '../auth/AuthProvider';
import { portraitPresetsFor } from '../characters/portraitPresets';
import { onlySystemId } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
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
  { icon: '🧭', title: 'Guided creation', text: 'Every choice shows what it changes before you make it: scores, proficiencies, spells.' },
  { icon: '📜', title: 'Sheets that play', text: 'Cast a spell, spend a slot, roll its damage. Rests, conditions and hit dice just work.' },
  { icon: '🎲', title: 'Honest dice', text: 'Rolled on the server and kept forever in a roll history your table can check.' },
  { icon: '🖼️', title: 'Your whole roster', text: 'Portraits, backstories and every character you play, across every game, in one place.' },
];

/** What a visitor sees before signing in: what the vault does and which games it plays. */
export function LandingPage() {
  const { signIn, signUp } = useAuth();
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
              Every system.
              <br />
              One vault for your heroes.
            </h1>
            <p>
              Build characters with rules that explain every choice, play them on a sheet that knows its mechanics, and roll dice with a
              history nobody can fake.
            </p>
            <div className="landing-hero__cta">
              <button type="button" className="app-btn app-btn--primary" onClick={() => signIn(onlySystemId() ? `/characters/new/${onlySystemId()}` : '/characters/new')}>
                Create character
              </button>
              <button type="button" className="app-btn" onClick={() => signUp('/')}>
                Create account
              </button>
              <button type="button" className="app-btn" onClick={() => signIn('/')}>
                I already have an account
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
            <h2 className="app-heading">What you can do</h2>
          </div>
          <div className="landing-features">
            {FEATURES.map((feature) => (
              <div key={feature.title} className="landing-feature">
                <div className="landing-feature__icon" aria-hidden="true">
                  {feature.icon}
                </div>
                <h3 className="app-heading">{feature.title}</h3>
                <p>{feature.text}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="landing-section" style={{ paddingTop: 0 }}>
        <div className="app-wrap">
          <div className="landing-section__head">
            <h2 className="app-heading">Pick your game</h2>
            <span className="app-muted">More systems are on the way.</span>
          </div>
          <SystemCards onCreate={(systemId) => signIn(`/characters/new/${systemId}`)} />
        </div>
      </section>

      <AppFooter />
    </AppPage>
  );
}
