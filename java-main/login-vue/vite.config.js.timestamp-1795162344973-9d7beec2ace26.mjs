// vite.config.js
import { defineConfig } from "file:///D:/workSpace/java/java-main/login-vue/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/workSpace/java/java-main/login-vue/node_modules/@vitejs/plugin-vue/dist/index.mjs";
var vite_config_default = defineConfig({
  plugins: [vue()],
  server: {
    open: true,
    proxy: {
      // 把前端对 /api/* 的请求代理到 Spring Boot 后端（8081），避免跨域
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true
      },
      // WebSocket 私信推送（/ws/chat）：必须开 ws:true，否则握手 502
      "/ws": {
        target: "http://localhost:8081",
        changeOrigin: true,
        ws: true
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcuanMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFx3b3JrU3BhY2VcXFxcamF2YVxcXFxqYXZhLW1haW5cXFxcbG9naW4tdnVlXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJEOlxcXFx3b3JrU3BhY2VcXFxcamF2YVxcXFxqYXZhLW1haW5cXFxcbG9naW4tdnVlXFxcXHZpdGUuY29uZmlnLmpzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9EOi93b3JrU3BhY2UvamF2YS9qYXZhLW1haW4vbG9naW4tdnVlL3ZpdGUuY29uZmlnLmpzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSdcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gIHBsdWdpbnM6IFt2dWUoKV0sXHJcbiAgc2VydmVyOiB7XHJcbiAgICBvcGVuOiB0cnVlLFxyXG4gICAgcHJveHk6IHtcclxuICAgICAgLy8gXHU2MjhBXHU1MjREXHU3QUVGXHU1QkY5IC9hcGkvKiBcdTc2ODRcdThCRjdcdTZDNDJcdTRFRTNcdTc0MDZcdTUyMzAgU3ByaW5nIEJvb3QgXHU1NDBFXHU3QUVGXHVGRjA4ODA4MVx1RkYwOVx1RkYwQ1x1OTA3Rlx1NTE0RFx1OERFOFx1NTdERlxyXG4gICAgICAnL2FwaSc6IHtcclxuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODEnLFxyXG4gICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZVxyXG4gICAgICB9LFxyXG4gICAgICAvLyBXZWJTb2NrZXQgXHU3OUMxXHU0RkUxXHU2M0E4XHU5MDAxXHVGRjA4L3dzL2NoYXRcdUZGMDlcdUZGMUFcdTVGQzVcdTk4N0JcdTVGMDAgd3M6dHJ1ZVx1RkYwQ1x1NTQyNlx1NTIxOVx1NjNFMVx1NjI0QiA1MDJcclxuICAgICAgJy93cyc6IHtcclxuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODEnLFxyXG4gICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZSxcclxuICAgICAgICB3czogdHJ1ZVxyXG4gICAgICB9XHJcbiAgICB9XHJcbiAgfVxyXG59KVxyXG4iXSwKICAibWFwcGluZ3MiOiAiO0FBQTJTLFNBQVMsb0JBQW9CO0FBQ3hVLE9BQU8sU0FBUztBQUdoQixJQUFPLHNCQUFRLGFBQWE7QUFBQSxFQUMxQixTQUFTLENBQUMsSUFBSSxDQUFDO0FBQUEsRUFDZixRQUFRO0FBQUEsSUFDTixNQUFNO0FBQUEsSUFDTixPQUFPO0FBQUE7QUFBQSxNQUVMLFFBQVE7QUFBQSxRQUNOLFFBQVE7QUFBQSxRQUNSLGNBQWM7QUFBQSxNQUNoQjtBQUFBO0FBQUEsTUFFQSxPQUFPO0FBQUEsUUFDTCxRQUFRO0FBQUEsUUFDUixjQUFjO0FBQUEsUUFDZCxJQUFJO0FBQUEsTUFDTjtBQUFBLElBQ0Y7QUFBQSxFQUNGO0FBQ0YsQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
