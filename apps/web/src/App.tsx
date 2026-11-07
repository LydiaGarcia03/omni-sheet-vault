import { BrowserRouter } from 'react-router';
import { AppShell } from './shell/AppShell';

export function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  );
}
