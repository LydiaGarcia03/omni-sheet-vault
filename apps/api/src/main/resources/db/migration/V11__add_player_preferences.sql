ALTER TABLE players
    ADD COLUMN preferred_locale text CHECK (preferred_locale IN ('en', 'pt-BR')),
    ADD COLUMN preferred_theme  text CHECK (preferred_theme IN ('light', 'dark'));
