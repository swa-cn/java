// =====================================================================
// useTheme —— 深色 / 浅色主题（模块级单例，全应用共享）
//
// 设计要点：
//  1. 切换只做一件事：给 <html> 加/去 data-theme="dark"
//     真正的配色差异全部写在 style.css 的 CSS 变量覆盖层里，
//     组件不需要感知主题，也不需要在每个组件里写 dark: 样式。
//  2. 用户手动选择过 → 存 localStorage，之后一直用它；
//     没选过 → 跟随操作系统（prefers-color-scheme），并实时响应系统切换。
//  3. 主题状态是模块级 reactive，任何组件 import 后都能直接读/改，
//     不会出现「A 组件切了、B 组件的图标没跟着变」这种不同步问题。
// =====================================================================
import { reactive, watch } from 'vue'

const STORAGE_KEY = 'mengchong-theme' // 'light' | 'dark' | 'system'

/** 主题状态：mode = 用户的选择，isDark = 当前实际生效的深浅 */
export const theme = reactive({
  mode: 'system', // 'light' | 'dark' | 'system'
  isDark: false
})

const mql =
  typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-color-scheme: dark)')
    : null

function systemPrefersDark() {
  return !!mql && mql.matches
}

/** 把当前 mode 解析成实际深浅，并同步到 <html data-theme> */
function apply() {
  const dark = theme.mode === 'dark' || (theme.mode === 'system' && systemPrefersDark())
  theme.isDark = dark
  const root = document.documentElement
  if (dark) root.setAttribute('data-theme', 'dark')
  else root.removeAttribute('data-theme')
}

/** 手动切换：在浅/深之间直接翻转（会从 system 变成明确选择） */
export function toggleTheme() {
  setMode(theme.isDark ? 'light' : 'dark')
}

/** 设置模式，并持久化 */
export function setMode(mode) {
  theme.mode = mode
  try {
    localStorage.setItem(STORAGE_KEY, mode)
  } catch (e) {
    /* 隐私模式下 localStorage 可能不可用，忽略即可 */
  }
  apply()
}

/** 应用启动时调用一次：恢复用户选择 + 监听系统变化 */
export function initTheme() {
  let saved = null
  try {
    saved = localStorage.getItem(STORAGE_KEY)
  } catch (e) {
    /* 忽略 */
  }
  if (saved === 'light' || saved === 'dark' || saved === 'system') theme.mode = saved
  else theme.mode = 'system'

  apply()

  // 仅在「跟随系统」时响应系统切换
  if (mql) {
    const onChange = () => {
      if (theme.mode === 'system') apply()
    }
    if (mql.addEventListener) mql.addEventListener('change', onChange)
    else if (mql.addListener) mql.addListener(onChange)
  }
}

// 兜底：任何地方直接改 theme.mode 也能生效
watch(() => theme.mode, apply)
