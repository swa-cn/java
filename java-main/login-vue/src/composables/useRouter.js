// =====================================================================
// useRouter —— 轻量路由（History API，无依赖）
//
// 为什么不用 vue-router：项目只有少量「整页级」视图需要真实 URL
// （知识详情 / 动态详情 / 搜索页），用 30 行的原生 History API 就够了：
//   - push(path)          ：跳转并写进浏览器历史（前进/后退可用）
//   - replace(path)       ：替换当前历史记录（返回时跳过中间态）
//   - route.path          ：响应式当前路径，模板/组件可直接 watch
//   - initRouter(onChange)：初始化 + 监听浏览器前进/后退
//
// 与 vue-router 等价的心智模型：
//   navigate = router.push；popstate = 路由变化事件；route.path = route.fullPath。
// 之后页面多了想换 vue-router，只需替换本文件内部实现，调用方不变。
// =====================================================================
import { reactive } from 'vue'

/** 当前路由（响应式）：path 含查询串，如 /search?kw=布偶猫 */
export const route = reactive({ path: '/' })

function normalize(path) {
  return path || '/'
}

/** 程序化跳转：写入浏览器历史并同步 route.path */
export function navigate(path, { replace = false } = {}) {
  const p = normalize(path)
  if (replace) history.replaceState({ path: p }, '', p)
  else history.pushState({ path: p }, '', p)
  route.path = p
}

/** 只同步 URL 不动历史（用于「用户点了导航」这类状态已变、只补地址的场景） */
export function syncUrl(path) {
  const p = normalize(path)
  history.replaceState({ path: p }, '', p)
  route.path = p
}

/** 应用启动时调用一次：恢复当前 URL + 监听前进/后退 */
export function initRouter(onChange) {
  route.path = location.pathname + location.search
  window.addEventListener('popstate', () => {
    route.path = location.pathname + location.search
    if (onChange) onChange(route.path)
  })
}

/** 解析路径：返回 { name, id, query }。例：/knowledge/k1?x=1 -> { name:'knowledge', id:'k1', query:{x:'1'} } */
export function parseRoute(path) {
  const [pathname, search = ''] = String(path || '/').split('?')
  const query = {}
  new URLSearchParams(search).forEach((v, k) => { query[k] = v })
  const seg = pathname.split('/').filter(Boolean)
  const name = seg[0] || 'home'
  return { name, id: seg[1] ? decodeURIComponent(seg[1]) : '', query }
}
