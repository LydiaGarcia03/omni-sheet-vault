import { BrowserRouter } from 'react-router';
import { AuthProvider } from './auth/AuthProvider';
import { AppShell } from './shell/AppShell';

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppShell />
      </BrowserRouter>
    </AuthProvider>
  );
}
