// =====================================================================
// searchHistory —— 搜索历史（模块级单例，localStorage 持久化）
//
// 设计要点：
//  1. 所有页面共享一份历史（首页搜索框 / 搜索页共用同一个模块状态）。
//  2. 去重 + 最新的排前面 + 上限 8 条：反复搜同一个词只留一条，且置顶。
//  3. 存 localStorage（key 前缀 mengchong-，与项目其他存储保持一致）。
//  4. 只存关键词文本，不存时间戳 —— 演示项目保持简单，够用。
// =====================================================================
import { reactive } from 'vue'

const STORAGE_KEY = 'mengchong-search-history'
const MAX = 8

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const arr = raw ? JSON.parse(raw) : []
    // 兜底：只接受字符串数组，防止脏数据把页面搞挂
    if (!Array.isArray(arr)) return []
    return arr.filter((s) => typeof s === 'string' && s.trim()).slice(0, MAX)
  } catch (e) {
    return []
  }
}

/** 全局共享的响应式历史列表（最新在前） */
export const searchHistory = reactive({ items: load() })

function save() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(searchHistory.items))
  } catch (e) {
    /* 隐私模式下 localStorage 可能不可用，忽略即可 */
  }
}

/** 记录一次搜索：去重置顶，超限截断 */
export function addHistory(kw) {
  const word = String(kw || '').trim()
  if (!word) return
  const rest = searchHistory.items.filter((s) => s !== word)
  searchHistory.items = [word, ...rest].slice(0, MAX)
  save()
}

/** 删除单条历史 */
export function removeHistory(kw) {
  searchHistory.items = searchHistory.items.filter((s) => s !== kw)
  save()
}

/** 清空全部历史 */
export function clearHistory() {
  searchHistory.items = []
  save()
}
