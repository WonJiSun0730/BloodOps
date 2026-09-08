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

      '/uploads': {
        target: 'http://localhost:80',
        changeOrigin: true,
      },

      // 소셜 로그인 시작 요청을 Spring으로 전달합니다.
      '/oauth2': {
        target: 'http://127.0.0.1:80',
        changeOrigin: false,
      },

      // 소셜 로그인 완료 후 전달되는 요청도 Spring으로 전달합니다.
      '/login/oauth2': {
        target: 'http://127.0.0.1:80',
        changeOrigin: false,
      },
    },
  },
})

//React에서 /api/test 요청
//→ Vite가 http://localhost:80/api/test로 전달
//→ Spring Boot가 응답