import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { AuthProvider } from './auth/AuthProvider';
import './global.css';
import { LanguageProvider } from './language/language';
import { AppThemeProvider } from './theme/appTheme';
import './theme/theme.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <AppThemeProvider>
        <LanguageProvider>
          <App />
        </LanguageProvider>
      </AppThemeProvider>
    </AuthProvider>
  </StrictMode>,
);
