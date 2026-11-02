// =====================================================================
// usePersonalization —— 用户个性化偏好（模块级单例，全应用共享）
//
// 设计要点（与 useTheme 同构，保证全站同步、可持久化）：
//  1. 偏好只存一份：模块级 reactive 的 prefs。任何组件 import 后直接读写，
//     不会出现「A 处改了字体、B 处没跟着变」的不同步问题。
//  2. 切换方式：把当前字体选择写到 <html data-font="..."> 上，
//     真正的字体差异全部写在 style.css 的 html[data-font=...] 覆盖层里，
//     组件不需要感知字体，也不需要在每个组件里写 font-family。
//  3. 用户选过 → 存 localStorage，之后一直用；没选过 → 用默认（标准黑体）。
//  4. 纯前端演示：偏好只存在浏览器本地，不依赖后端，刷新/重开仍生效。
//
// 为什么不用「把字体名直接写进 CSS 变量」：
//  浏览器是按「字」回退的——旧字体（如系统幼圆）字库不全时，
//  缺的字会单独换成下一档字体，导致同一行里混进两种字体
//  （就是之前「狗」字看着怪的根因）。所以这里只提供「字库完整的字体」：
//  圆润体用的是字库完整的站酷快乐体（ZCOOL KuaiLe），不会再出现逐字混排。
// =====================================================================
import { reactive, watch } from 'vue'

const STORAGE_KEY = 'mengchong-prefs' // 存 JSON：{ font: 'default' | ... }

/** 字体选项（设置面板与 style.css 的 html[data-font] 规则一一对应）
 *  - value  ：写进 <html data-font> 的值，也是持久化的取值
 *  - label  ：展示名
 *  - desc   ：一句话说明
 *  - stack  ：该选项在「预览卡」里用的 font-family（与 style.css 同款写法）
 */
export const FONT_OPTIONS = [
  {
    value: 'default',
    label: '标准黑体',
    desc: '系统默认无衬线，清晰统一',
    stack: 'var(--font-round)'
  },
  {
    value: 'rounded',
    label: '圆润体',
    desc: '站酷快乐体，可爱俏皮',
    stack: '"ZCOOL KuaiLe", "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", sans-serif'
  },
  {
    value: 'serif',
    label: '文艺宋体',
    desc: '衬线字体，温润书卷气',
    stack: '"Songti SC", "STSong", "SimSun", "Noto Serif CJK SC", serif'
  },
  {
    value: 'system',
    label: '跟随系统',
    desc: '使用操作系统的界面字体',
    stack: 'system-ui, -apple-system, "Segoe UI", sans-serif'
  }
]

const VALID = FONT_OPTIONS.map((o) => o.value)

/** 偏好状态：目前只有字体，后续可在此扩展（如主题色等） */
export const prefs = reactive({
  font: 'default' // 'default' | 'rounded' | 'serif' | 'system'
})

/** 把当前字体选择同步到 <html data-font> */
function apply() {
  const root = document.documentElement
  // default 用 :root 的默认变量，不需要 data-font；其余写到属性上
  if (prefs.font && prefs.font !== 'default') {
    root.setAttribute('data-font', prefs.font)
  } else {
    root.removeAttribute('data-font')
  }
}

/** 设置字体并持久化（点击设置面板即调用） */
export function setFont(font) {
  if (!VALID.includes(font)) font = 'default'
  prefs.font = font
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ font: prefs.font }))
  } catch (e) {
    /* 隐私模式下 localStorage 可能不可用，忽略即可 */
  }
  apply()
}

/** 应用启动时调用一次：恢复用户选择 */
export function initPersonalization() {
  let saved = null
  try {
    saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
  } catch (e) {
    /* 忽略损坏数据 */
  }
  if (saved && VALID.includes(saved.font)) prefs.font = saved.font
  else prefs.font = 'default'

  apply()

  // 兜底：任何地方直接改 prefs.font 也能生效
  watch(() => prefs.font, apply)
}
