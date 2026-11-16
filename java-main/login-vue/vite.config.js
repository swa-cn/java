import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    open: true,
    proxy: {
      // 把前端对 /api/* 的请求代理到 Spring Boot 后端（8081），避免跨域
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // WebSocket 私信推送（/ws/chat）：必须开 ws:true，否则握手 502
      '/ws': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
