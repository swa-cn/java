// =====================================================================
// api/home.js —— 首页相关接口（搜索）
//
// 搜索有两级数据源：
//  1) 服务端聚合搜索 GET {API_BASE}/content/search?q=<keyword>
//     一次返回四组结果（分类 / 品种 / 动态 / 用户），结果页直接分组渲染；
//  2) 接口不可用（后端未启动、404、5xx）时，用本地数据兜底：
//     分类与品种取自 composables/petData.js，动态取自 composables/postStore.js，
//     结构与服务端完全一致，保证纯前端演示、断网时搜索页依然可用。
//
// 接口约定：
//  GET /content/search?q=<keyword>&limit=20
//      200 { ok, query, total,
//            counts: { categories, breeds, posts, users },
//            categories: [{ id, name, icon, color, image, breedCount }],
//            breeds:     [{ name, image, desc, categoryId, categoryName, categoryColor, categoryIcon }],
//            posts:      [{ id, postId, title, summary, cover, image, category, categoryName,
//                           author, authorName, createdAt }],
//            users:      [{ key, name, username }] }
//
// 安全说明：搜索为只读查询，前端不缓存任何敏感数据；用户结果只含昵称与账号，不含邮箱/密码。
// =====================================================================

// 本地兜底数据源
import { searchPosts, excerpt } from '../composables/postStore'
import { petCategories } from '../composables/petData'

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
// 默认对接真实后端；只有显式设置 VITE_USE_MOCK=true 才走本地 mock。
// 注意：mock 模式数据仅存浏览器 localStorage，跨浏览器/跨设备不可见，
// 会出现「注册账号数据库查不到、加好友对方收不到」的问题（详见 .env.example）。
const USE_MOCK = String(import.meta.env.VITE_USE_MOCK) === 'true'

/** 每组默认条数，与服务端默认值保持一致 */
const LIMIT = 20

async function parseBody(res) {
  let data = {}
  try {
    data = await res.json()
  } catch (e) {
    /* 忽略非 JSON 响应 */
  }
  if (!res.ok) {
    const err = new Error(data.message || '请求失败')
    // code 必须是数字状态码（data.error 可能是 "Not Found" 这类字符串）
    err.code = res.status
    err.serverError = data.error
    throw err
  }
  return data
}

async function httpGet(path) {
  const res = await fetch(API_BASE + path, { credentials: 'include' })
  return parseBody(res)
}

/* ===================== 本地兜底搜索 ===================== */

/** 名称相关度：完全相等 10 / 前缀 8 / 包含 6 / 不命中 0（与后端 SearchServiceImpl 同口径） */
function nameScore(name, k) {
  const n = String(name || '').trim().toLowerCase()
  if (!n) return 0
  if (n === k) return 10
  if (n.startsWith(k)) return 8
  return n.includes(k) ? 6 : 0
}

function localSearchAll(q) {
  const k = q.toLowerCase()
  const categories = []
  const breeds = []

  for (const c of petCategories) {
    const catHit = nameScore(c.name, k) > 0
    if (catHit) {
      categories.push({
        id: c.id,
        name: c.name,
        icon: c.icon,
        color: c.color,
        image: c.image,
        breedCount: (c.breeds || []).length
      })
    }
    for (const b of c.breeds || []) {
      let score = nameScore(b.name, k)
      if (!score && String(b.desc || '').toLowerCase().includes(k)) score = 2
      if (!score && catHit) score = 3 // 分类命中：该分类下品种都算相关
      if (!score) continue
      breeds.push({
        score,
        item: {
          name: b.name,
          image: b.image,
          desc: b.desc,
          categoryId: c.id,
          categoryName: c.name,
          categoryColor: c.color,
          categoryIcon: c.icon
        }
      })
    }
  }
  // 稳定排序：分数高的在前，同分保持数据源顺序
  breeds.sort((a, b) => b.score - a.score)
  const breedItems = breeds.slice(0, LIMIT).map((x) => x.item)

  const posts = searchPosts(q)
    .slice(0, LIMIT)
    .map((p) => {
      const first = (p.media || [])[0]
      const cat = petCategories.find((c) => c.id === p.category)
      return {
        id: p.id,
        postId: p.id,
        title: p.title || excerpt(p.content, 18) || '萌宠动态',
        summary: excerpt(p.content, 46),
        cover: cat ? cat.icon : '萌',
        image: first && first.src ? first.src : '',
        category: p.category,
        categoryName: cat ? cat.name : '',
        author: p.author,
        authorName: p.authorName || '萌宠用户',
        createdAt: p.createdAt
      }
    })

  return withCounts(q, categories, breedItems, posts, [], 'local')
}

/* ===================== 结果组装 ===================== */

function withCounts(q, categories, breeds, posts, users, source) {
  const list = (v) => (Array.isArray(v) ? v : [])
  const c = list(categories)
  const b = list(breeds)
  const p = list(posts)
  const u = list(users)
  return {
    ok: true,
    source, // 'remote' 服务端 | 'local' 本地兜底
    query: q,
    total: c.length + b.length + p.length + u.length,
    counts: { categories: c.length, breeds: b.length, posts: p.length, users: u.length },
    categories: c,
    breeds: b,
    posts: p,
    users: u
  }
}

/** 把服务端响应规范化：字段缺失时补空数组，避免结果页拿 undefined 渲染 */
function normalize(data, q, source) {
  return withCounts(
    q,
    data?.categories,
    data?.breeds,
    data?.posts,
    data?.users,
    source
  )
}

/* ===================== 对外接口 ===================== */

/**
 * 聚合搜索：返回 { ok, source, query, total, counts, categories, breeds, posts, users }。
 * 服务端不可用时自动降级为本地搜索（source='local'），调用方无需 try/catch。
 */
export async function searchAll(keyword) {
  const q = String(keyword || '').trim()
  if (!q) return withCounts('', [], [], [], [], 'local')

  if (USE_MOCK) return localSearchAll(q)

  try {
    const data = await httpGet(
      '/content/search?q=' + encodeURIComponent(q) + '&limit=' + LIMIT
    )
    // 后端返回空结果（例如老版本只实现了 posts 字段）时，用本地数据补齐分类/品种
    const merged = normalize(data, q, 'remote')
    if (!merged.categories.length && !merged.breeds.length) {
      const local = localSearchAll(q)
      return {
        ...merged,
        counts: {
          ...merged.counts,
          categories: local.categories.length,
          breeds: local.breeds.length
        },
        total: merged.total + local.categories.length + local.breeds.length,
        categories: local.categories,
        breeds: local.breeds
      }
    }
    return merged
  } catch (e) {
    // 后端未启动 / 接口未实现 / 5xx：静默降级为本地搜索
    if (e instanceof TypeError || [404, 405, 500, 501, 502, 503, 504].includes(Number(e.code))) {
      return localSearchAll(q)
    }
    throw e
  }
}

/**
 * 兼容旧调用：只关心「动态」结果的搜索（首页旧版内联结果用）。
 * 新代码请用 searchAll。
 */
export async function searchContent(keyword) {
  const res = await searchAll(keyword)
  return {
    ok: true,
    items: (res.posts || []).map((p) => ({
      id: p.postId || p.id,
      postId: p.postId || p.id,
      title: p.title,
      cover: p.cover || '萌',
      image: p.image || '',
      summary: p.summary,
      author: p.authorName || '萌宠用户'
    }))
  }
}

export const homeApiConfig = { API_BASE, USE_MOCK }
