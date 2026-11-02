import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import { initTheme } from './composables/useTheme'
import { initPersonalization } from './composables/usePersonalization'

// 圆体字体（站酷快乐体 / ZCOOL KuaiLe）：只引入「拉丁 + 简体中文」两个精选子集，
// 字库完整、覆盖全部简体汉字，按需加载（选中圆润体时才下载 woff2），不污染构建。
import '@fontsource/zcool-kuaile/latin.css'
import '@fontsource/zcool-kuaile/chinese-simplified.css'

// 挂载前先定好主题与字体偏好，避免用户进来先闪一下白屏/默认字体
initTheme()
initPersonalization()

createApp(App).mount('#app')
