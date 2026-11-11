import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento, as chamadas para /api vão para o Spring Boot (porta 8080 por padrão).
// Se o backend estiver em outra porta: API_URL=http://localhost:8081 npm run dev
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': process.env.API_URL ?? 'http://localhost:8080',
    },
  },
});
