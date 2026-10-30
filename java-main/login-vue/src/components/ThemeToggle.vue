<template>
  <button
    type="button"
    class="theme-toggle"
    :class="{ 'is-floating': floating, 'is-dark': theme.isDark, 'on-nav': variant === 'nav' }"
    :title="theme.isDark ? '切换到浅色模式' : '切换到深色模式'"
    :aria-label="theme.isDark ? '切换到浅色模式' : '切换到深色模式'"
    :aria-pressed="theme.isDark"
    @click="toggleTheme"
  >
    <!-- 浅色下显示月亮（点了进深色）；深色下显示太阳（点了回浅色） -->
    <svg v-if="!theme.isDark" class="tt-ico" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M20 14.2A8.2 8.2 0 0 1 9.8 4a8.4 8.4 0 1 0 10.2 10.2z"
        fill="currentColor"
      />
      <circle cx="16.4" cy="5.6" r="1.1" fill="currentColor" opacity="0.85" />
      <circle cx="20.6" cy="9.2" r="0.9" fill="currentColor" opacity="0.7" />
    </svg>
    <svg v-else class="tt-ico" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="4.6" fill="currentColor" />
      <g stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M12 2.2v2.4" />
        <path d="M12 19.4v2.4" />
        <path d="M2.2 12h2.4" />
        <path d="M19.4 12h2.4" />
        <path d="M5.1 5.1l1.7 1.7" />
        <path d="M17.2 17.2l1.7 1.7" />
        <path d="M18.9 5.1l-1.7 1.7" />
        <path d="M6.8 17.2l-1.7 1.7" />
      </g>
    </svg>
  </button>
</template>

<script setup>
import { theme, toggleTheme } from '../composables/useTheme'

defineProps({
  /** true = 固定在右上角（用于登录页 / 后台等没有导航栏的页面） */
  floating: { type: Boolean, default: false },
  /** 'nav' = 放在彩色导航条里，用半透明白玻璃样式，跟导航融为一体 */
  variant: { type: String, default: 'plain' }
})
</script>

<style scoped>
.theme-toggle {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  padding: 0;
  border: 2px solid var(--line);
  border-radius: 50%;
  background: var(--card);
  color: var(--candy);
  cursor: pointer;
  transition: transform 0.18s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.2s ease,
    border-color 0.2s ease, box-shadow 0.2s ease;
}
.theme-toggle:hover {
  transform: scale(1.08) rotate(-12deg);
  border-color: var(--pink);
  box-shadow: 0 6px 14px -6px rgba(124, 108, 255, 0.7);
}
.theme-toggle:active { transform: scale(0.92); }

/* 深色下换成暖黄，跟夜色更搭 */
.theme-toggle.is-dark { color: var(--sun); border-color: var(--candy); }

.tt-ico { width: 20px; height: 20px; }

/* 悬浮形态：钉在视口右上角，任何页面都能切 */
.theme-toggle.is-floating {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 60;
  box-shadow: 0 8px 20px -8px rgba(0, 0, 0, 0.35);
}

/* 浅色下悬浮按钮放在彩色背景上，底色改半透明更融合 */
.theme-toggle.is-floating:not(.is-dark) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(6px);
}

/* 导航条里的形态：半透明白玻璃 + 白色图标，和「消息/我的」等导航项同一语言 */
.theme-toggle.on-nav {
  width: 34px;
  height: 34px;
  margin-right: -6px;
  background: rgba(255, 255, 255, 0.18);
  border: 1.5px solid rgba(255, 255, 255, 0.38);
  color: #fff;
  backdrop-filter: blur(4px);
}
.theme-toggle.on-nav .tt-ico { width: 18px; height: 18px; }
/* 深色下导航里的太阳用暖黄，比纯白更容易认出「点我回浅色」 */
.theme-toggle.on-nav.is-dark { color: var(--sun); }
.theme-toggle.on-nav:hover {
  background: rgba(255, 255, 255, 0.32);
  border-color: rgba(255, 255, 255, 0.7);
  box-shadow: 0 6px 14px -8px rgba(0, 0, 0, 0.45);
}
</style>
