import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Build output is rewritten by the desktop packaging while the dev server runs; watching it crashes on Windows.
    watch: { ignored: ['**/dist/**', '**/dist-desktop/**'] },
    // Same origin as the API, so the session cookie flows (adr-0008); the Host header stays localhost:5173.
    proxy: Object.fromEntries(
      ['/api', '/oauth2', '/login/oauth2', '/logout'].map((path) => [path, { target: 'http://localhost:8090', changeOrigin: false }]),
    ),
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
});
