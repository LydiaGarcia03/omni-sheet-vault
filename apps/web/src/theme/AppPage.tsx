import type { ReactNode } from 'react';
import { useAppTheme } from './appTheme';

/** The root of a page drawn with the app theme (landing, home, credits), in the player's light or dark theme. */
export function AppPage({ className, children }: { className?: string; children: ReactNode }) {
  const { theme } = useAppTheme();
  return (
    <div className={className ? `app-page ${className}` : 'app-page'} data-app-theme={theme}>
      {children}
    </div>
  );
}
