import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import { initTheme } from './composables/useTheme'

// 挂载前先定好主题，避免深色用户进来先闪一下白屏
initTheme()

createApp(App).mount('#app')
