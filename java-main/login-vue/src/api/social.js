// =====================================================================
// api/social.js —— 关注 / 粉丝接口（2026-11 新增）
//
//  POST /content/users/{key}/follow            关注/取关（切换）
//  GET  /content/users/{key}/follow/counts     数量 { followers, following_count }
//  GET  /content/users/{key}/following         我关注的人
//  GET  /content/users/{key}/followers         我的粉丝
// 身份由后端会话判定；本地 mock 模式不支持关注，返回 ok:false 提示。
// =====================================================================

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')

async function parseBody(res) {
  let data = {}
  try {
    data = await res.json()
  } catch (e) {
    /* 忽略非 JSON */
  }
  if (!res.ok) {
    const err = new Error(data.message || '请求失败')
    err.code = res.status
    err.serverError = data.error
    throw err
  }
  return data
}

async function http(method, path, body) {
  const opts = { method, credentials: 'include' }
  if (body !== undefined) {
    opts.headers = { 'Content-Type': 'application/json' }
    opts.body = JSON.stringify(body)
  }
  const res = await fetch(API_BASE + path, opts)
  return parseBody(res)
}

/** 关注 / 取消关注（切换）→ { ok, following, followers, following_count } */
export function followUser(key) {
  return http('POST', `/content/users/${encodeURIComponent(key)}/follow`)
}

/** 关注/粉丝数量 */
export function fetchFollowCounts(key) {
  return http('GET', `/content/users/${encodeURIComponent(key)}/follow/counts`)
}

/** 我关注的人 */
export function fetchFollowing(key) {
  return http('GET', `/content/users/${encodeURIComponent(key)}/following`)
}

/** 我的粉丝 */
export function fetchFollowers(key) {
  return http('GET', `/content/users/${encodeURIComponent(key)}/followers`)
}
