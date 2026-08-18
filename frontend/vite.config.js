import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:80',
        changeOrigin: true,
      },
    },
  },
})

//React에서 /api/test 요청
//→ Vite가 http://localhost:80/api/test로 전달
//→ Spring Boot가 응답