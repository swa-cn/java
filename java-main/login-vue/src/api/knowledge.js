// =====================================================================
// api/knowledge.js —— 知识课堂「读」接口（无需登录）
//
// 对接后端 PetKnowledgeController（/api/content/knowledge*）：
//   GET /content/knowledge       -> { ok, items:[Article] }
//   GET /content/knowledge/{id}  -> { ok, article: Article }（浏览数 +1）
//
// Article: { id, cat, title, summary, tags:[], sections:[{h,p}], views, createdAt }
//
// 注意：后端不可用时这里会抛异常，调用方 KnowledgePage 会回退到
// composables/knowledgeData.js 的静态内容，保证页面永远有东西可看。
// =====================================================================

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')

async function http(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    ...options
  })
  let data = {}
  try {
    data = await res.json()
  } catch (e) {
    /* 忽略非 JSON */
  }
  if (!res.ok || data.ok === false) {
    const err = new Error(data.message || '请求失败')
    err.code = data.error || res.status
    throw err
  }
  return data
}

/** 全部文章（含正文，前端本地搜索/过滤） */
export function fetchKnowledgeList() {
  return http('/content/knowledge')
}

/** 打开文章详情：后端 views +1，返回最新文章 */
export function readKnowledge(id) {
  return http('/content/knowledge/' + encodeURIComponent(id))
}
