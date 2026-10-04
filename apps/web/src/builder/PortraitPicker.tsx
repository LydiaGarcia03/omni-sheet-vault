import { useRef, useState } from 'react';
import { CharacterPortrait } from '../characters/CharacterPortrait';
import {
  choosePresetPortrait,
  PORTRAIT_ACCEPTED_TYPES,
  portraitFileProblem,
  removePortrait,
  uploadPortrait,
  type Portrait,
} from '../characters/portrait';
import { portraitPresetsFor } from '../characters/portraitPresets';

type PortraitPickerProps = {
  characterId: string;
  systemId: string;
  portrait: Portrait | null;
  onChange: (portrait: Portrait | null) => void;
};

/** The character's portrait: the current one, an upload, and the system's default portraits to pick from. */
export function PortraitPicker({ characterId, systemId, portrait, onChange }: PortraitPickerProps) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showPresets, setShowPresets] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);
  const presets = portraitPresetsFor(systemId);
  const chosenPreset = portrait?.kind === 'PRESET' ? portrait.presetId : null;

  const run = async (change: () => Promise<Portrait | null>) => {
    setBusy(true);
    setError(null);
    try {
      onChange(await change());
    } catch (failure) {
      setError((failure as Error).message);
    } finally {
      setBusy(false);
    }
  };

  const upload = (file: File | undefined) => {
    if (!file) {
      return;
    }
    const problem = portraitFileProblem(file);
    if (problem) {
      setError(problem);
      return;
    }
    void run(() => uploadPortrait(characterId, file));
  };

  return (
    <div className="builder-portrait">
      <div className="builder-portrait__main">
        <span className="builder-label">Portrait</span>
        <div className="builder-row">
          <CharacterPortrait portrait={portrait} systemId={systemId} className="builder-portrait__current" alt="Current portrait" />
          <div className="builder-portrait__actions">
            <div className="builder-row">
              <button type="button" className="builder-btn" disabled={busy} onClick={() => fileInput.current?.click()}>
                Upload image
              </button>
              {presets.length > 0 && (
                <button type="button" className="builder-btn" disabled={busy} aria-expanded={showPresets} onClick={() => setShowPresets(!showPresets)}>
                  Pick a default portrait
                </button>
              )}
              {portrait && (
                <button
                  type="button"
                  className="builder-btn"
                  disabled={busy}
                  onClick={() =>
                    void run(async () => {
                      await removePortrait(characterId);
                      return null;
                    })
                  }
                >
                  Remove
                </button>
              )}
              {busy && <span className="builder-muted">Saving…</span>}
            </div>
            <span className="builder-muted">PNG or JPEG, up to 3 MB. It's cropped to a square from the centre.</span>
            <input
              ref={fileInput}
              type="file"
              hidden
              accept={PORTRAIT_ACCEPTED_TYPES.join(',')}
              aria-label="Portrait image file"
              onChange={(event) => {
                upload(event.target.files?.[0]);
                event.target.value = '';
              }}
            />
          </div>
        </div>
        {error && (
          <p className="builder-problem" role="alert">
            {error}
          </p>
        )}
      </div>
      {presets.length > 0 && showPresets && (
        <section className="builder-portrait__gallery" aria-label="Default portraits">
          <div className="builder-portrait__gallery-head">
            <span className="builder-label builder-grow">Default portraits</span>
            <button type="button" className="builder-btn" onClick={() => setShowPresets(false)}>
              Close
            </button>
          </div>
          <div className="builder-portrait__presets" role="radiogroup" aria-label="Default portraits">
            {presets.map((preset, index) => (
              <button
                key={preset.id}
                type="button"
                role="radio"
                aria-checked={preset.id === chosenPreset}
                aria-label={`Default portrait ${index + 1}`}
                className="builder-portrait__preset"
                disabled={busy}
                onClick={() => {
                  setShowPresets(false);
                  void run(() => choosePresetPortrait(characterId, preset.id));
                }}
              >
                <img src={preset.url} alt="" loading="lazy" />
              </button>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
