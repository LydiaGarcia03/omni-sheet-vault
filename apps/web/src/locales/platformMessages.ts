import { defineMessages } from '../language/language';
import builder from './builder.en.json';
import credits from './credits.en.json';
import home from './home.en.json';
import profile from './profile.en.json';
import sheet from './sheet.en.json';
import shell from './shell.en.json';

/** Header, user menu, language menu, footer and loading texts. */
export const useShellText = defineMessages(shell, { 'pt-BR': () => import('./shell.pt-BR.json') });

/** Landing page, home, system cards, character banners and the create-character pages. */
export const useHomeText = defineMessages(home, { 'pt-BR': () => import('./home.pt-BR.json') });

/** The shared character builder's own chrome (save state, draft badge). */
export const useBuilderText = defineMessages(builder, { 'pt-BR': () => import('./builder.pt-BR.json') });

/** The shared character sheet's chrome: header, sidebar and its panels. */
export const useSheetText = defineMessages(sheet, { 'pt-BR': () => import('./sheet.pt-BR.json') });

export type SheetText = ReturnType<typeof useSheetText>;

/** The credits and licenses page. */
export const useCreditsText = defineMessages(credits, { 'pt-BR': () => import('./credits.pt-BR.json') });

/** The player's own profile page. */
export const useProfileText = defineMessages(profile, { 'pt-BR': () => import('./profile.pt-BR.json') });
