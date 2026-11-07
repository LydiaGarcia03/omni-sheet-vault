import { defineMessages } from '../../../language/language';
import vtm5 from './vtm5.en.json';

/** VtM 5e's own interface texts: builder, explanation column, sheet, dice and footer. */
export const useVtm5Text = defineMessages(vtm5, { 'pt-BR': () => import('./vtm5.pt-BR.json') });
