// =====================================================================
// api/friend.js —— 好友接口层
//
// 与 api/post.js、api/auth.js 保持一致的双模式设计：
//  1) USE_MOCK = true（默认）：直接读写 friendStore（localStorage）
//  2) USE_MOCK = false：走后端接口，接口不可用时自动回退本地
//
// 接口约定（见 springBootTest FriendController）：
//  GET    /content/friends?owner=&status=           -> { ok, items: [Relation] }
//  GET    /content/friends/incoming?owner=          -> { ok, items: [Relation] }
//  GET    /content/friends/relation?owner=&target=  -> { ok, status }
//  GET    /content/friends/search?owner=&keyword=   -> { ok, items: [User] }
//  POST   /content/friends                          { owner, target, name, from } -> { ok, relation }
//  POST   /content/friends/accept                   { owner, target } -> { ok, relation }
//  DELETE /content/friends?owner=&target=            -> { ok }
//
// 关系状态 status：'none' | 'pending_out'（我发的，等对方通过）
//                        | 'pending_in'（别人发我的，等我处理） | 'friend'
// =====================================================================

import * as store from '../composables/friendStore'
import { reactive } from 'vue'

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
const USE_MOCK =
  import.meta.env.VITE_USE_MOCK === undefined
    ? true
    : String(import.meta.env.VITE_USE_MOCK) !== 'false'

/** 好友功能运行态（供 UI 提示） */
export const friendRuntime = reactive({ localMode: USE_MOCK, reason: '' })

const FALLBACK_CODES = [404, 405, 500, 501, 502, 503, 504]
function shouldFallback(e) {
  if (e instanceof TypeError) return true
  return FALLBACK_CODES.includes(Number(e && e.code))
}

async function withFallback(remoteFn, localFn) {
  if (USE_MOCK) return localFn()
  try {
    return await remoteFn()
  } catch (e) {
    if (!shouldFallback(e)) throw e
    friendRuntime.localMode = true
    friendRuntime.reason = '后端好友接口尚未就绪，已自动切换为本地模式'
    return localFn()
  }
}

async function parseBody(res) {
  let data = {}
  try {
    data = await res.json()
  } catch (e) {
    /* 忽略非 JSON 响应 */
  }
  if (!res.ok) {
    const err = new Error(data.message || '请求失败')
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

async function httpPost(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body || {})
  })
  return parseBody(res)
}

/* =====================================================================
 * 查询
 * ===================================================================== */

/** 好友列表（status 可选：friend / pending） */
export async function fetchFriends(ownerKey, status = '') {
  return withFallback(
    () =>
      httpGet(
        '/content/friends?owner=' +
          encodeURIComponent(ownerKey) +
          (status ? '&status=' + encodeURIComponent(status) : '')
      ),
    () => ({ ok: true, items: store.listFriends(ownerKey, status) })
  )
}

/** 我发出的、等待通过的申请 */
export async function fetchPending(ownerKey) {
  return withFallback(
    () => httpGet('/content/friends?owner=' + encodeURIComponent(ownerKey) + '&status=pending'),
    () => ({ ok: true, items: store.listPending(ownerKey) })
  )
}

/** 别人发给我、等待我处理的申请 */
export async function fetchIncoming(ownerKey) {
  return withFallback(
    () => httpGet('/content/friends/incoming?owner=' + encodeURIComponent(ownerKey)),
    () => ({ ok: true, items: store.listIncoming(ownerKey) })
  )
}

/** 与某人的关系：'none' | 'pending_out' | 'pending_in' | 'friend' */
export async function fetchRelation(ownerKey, targetKey) {
  return withFallback(
    () =>
      httpGet(
        '/content/friends/relation?owner=' +
          encodeURIComponent(ownerKey) +
          '&target=' +
          encodeURIComponent(targetKey)
      ),
    () => ({ ok: true, status: store.relationWith(ownerKey, targetKey) })
  )
}

/** 按用户名 / 昵称搜索用户（加好友用） */
export async function searchUsers(ownerKey, keyword) {
  return withFallback(
    () =>
      httpGet(
        '/content/friends/search?owner=' +
          encodeURIComponent(ownerKey) +
          '&keyword=' +
          encodeURIComponent(keyword)
      ),
    () => ({ ok: true, items: store.searchLocalUsers(ownerKey, keyword) })
  )
}

/* =====================================================================
 * 变更
 * ===================================================================== */

/** 发送好友申请。from 记录来源：'post' | 'search' | 'profile' | 'manual' */
export async function addFriend(ownerKey, person, from = 'manual') {
  return withFallback(
    () =>
      httpPost('/content/friends', {
        owner: ownerKey,
        target: person && person.key,
        name: person && person.name,
        from
      }),
    () => {
      const r = store.addFriend(ownerKey, person, from)
      if (!r.ok) {
        const err = new Error(
          r.reason === 'already'
            ? '你们已经是好友啦'
            : r.reason === 'pending'
              ? '申请已发送，等待对方通过'
              : '无法添加该用户'
        )
        err.code = 409
        throw err
      }
      return { ok: true, relation: r.relation }
    }
  )
}

/** 同意好友申请（对方发来 → 我通过，双方成为好友） */
export async function acceptFriend(ownerKey, targetKey) {
  return withFallback(
    () => httpPost('/content/friends/accept', { owner: ownerKey, target: targetKey }),
    () => store.acceptFriend(ownerKey, targetKey)
  )
}

/** 删除好友 / 撤回申请 / 拒绝申请 */
export async function removeFriend(ownerKey, targetKey) {
  return withFallback(
    async () => {
      const res = await fetch(
        API_BASE +
          '/content/friends?owner=' +
          encodeURIComponent(ownerKey) +
          '&target=' +
          encodeURIComponent(targetKey),
        { method: 'DELETE', credentials: 'include' }
      )
      return parseBody(res)
    },
    () => store.removeFriend(ownerKey, targetKey)
  )
}

export const friendApiConfig = { API_BASE, USE_MOCK, isLocalMode: () => friendRuntime.localMode }
