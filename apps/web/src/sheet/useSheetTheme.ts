import { useEffect } from 'react';

/** Draws the page in the sheet's theme (`data-sheet-theme` on the root element) while the sheet is open. */
export function useSheetTheme(themeId: string | null) {
  useEffect(() => {
    const root = document.documentElement;
    if (themeId) {
      root.dataset.sheetTheme = themeId;
    } else {
      delete root.dataset.sheetTheme;
    }
    return () => {
      delete root.dataset.sheetTheme;
    };
  }, [themeId]);
}
