import { useEffect, useRef, useState } from 'react';
import { useShellText } from '../locales/platformMessages';
import { LOCALES, useLanguage } from './language';
import './language.css';

/** The header's language button: the globe icon and the current code, opening the list of languages. */
export function LanguageMenu() {
  const { locale, setLocale } = useLanguage();
  const t = useShellText();
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const current = LOCALES.find((candidate) => candidate.id === locale) ?? LOCALES[0];

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const closeOnEscape = (event: KeyboardEvent) => event.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [open]);

  return (
    <div className="app-language" ref={root}>
      <button
        type="button"
        className="app-language__button"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label={t('language.button', { name: current.name })}
        onClick={() => setOpen(!open)}
      >
        <span className="material-symbols-outlined app-language__icon" aria-hidden="true">
          language
        </span>
        <span className="app-language__code">{current.code}</span>
        <span className="app-language__caret" aria-hidden="true">
          ▾
        </span>
      </button>
      {open && (
        <div className="app-language__menu" role="menu" aria-label={t('language.menu')}>
          {LOCALES.map((option) => (
            <button
              key={option.id}
              type="button"
              role="menuitemradio"
              aria-checked={option.id === locale}
              className="app-language__option"
              onClick={() => {
                setOpen(false);
                setLocale(option.id);
              }}
            >
              <span className="app-language__option-code">{option.code}</span>
              <span className="app-language__option-name">{option.name}</span>
              <span className="app-language__check" aria-hidden="true">
                {option.id === locale ? '✓' : ''}
              </span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
