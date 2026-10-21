// =====================================================================
// friendStore —— 好友关系数据（前端本地实现，接口不可用时的兜底）
//
// 与 postStore 同样的思路：api/friend.js 优先走真实接口，
// 接口未就绪时回退到这里（localStorage），组件调用方式完全一致。
//
// 数据模型：
// relations: {
//   [ownerKey]: {
//     [friendKey]: {
//       key,               // 对方用户名
//       name,              // 对方昵称
//       status,            // 'friend' | 'pending'
//       incoming,          // true = 别人发给我的申请；false = 我发出的申请
//       from,              // 'post' | 'search' | 'profile' | 'manual'
//       createdAt
//     }
//   }
// }
// =====================================================================

import { reactive } from 'vue'

const STORAGE_KEY = 'mengchong.friends.v2'

export const friendState = reactive({
  relations: {},
  loaded: false
})

function uid(prefix = 'f') {
  return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

/* ---------------- 持久化 ---------------- */
function load() {
  if (friendState.loaded) return
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      if (data && typeof data === 'object' && data.relations) {
        friendState.relations = data.relations
      }
    }
  } catch (e) {
    /* 解析失败则用空数据 */
  }
  friendState.loaded = true
}

function persist() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ relations: friendState.relations })
    )
  } catch (e) {
    /* 存储不可用时静默忽略 */
  }
}

function bucket(ownerKey) {
  load()
  if (!friendState.relations[ownerKey]) friendState.relations[ownerKey] = {}
  return friendState.relations[ownerKey]
}

/* ---------------- 对外 API ---------------- */

/** 好友列表。status='friend' 只返回好友；'' 返回全部（含待处理申请） */
export function listFriends(ownerKey, status = '') {
  const b = bucket(ownerKey)
  return Object.values(b)
    .filter((r) => (status ? r.status === status : true))
    .sort((a, b2) => b2.createdAt - a.createdAt)
}

/** 我发出的、等待对方通过的申请 */
export function listPending(ownerKey) {
  return Object.values(bucket(ownerKey))
    .filter((r) => r.status === 'pending' && !r.incoming)
    .map((r) => ({ ...r, incoming: false }))
    .sort((a, b2) => b2.createdAt - a.createdAt)
}

/** 别人发给我、等待我处理的申请 */
export function listIncoming(ownerKey) {
  return Object.values(bucket(ownerKey))
    .filter((r) => r.status === 'pending' && r.incoming)
    .map((r) => ({ ...r, incoming: true }))
    .sort((a, b2) => b2.createdAt - a.createdAt)
}

/** 与某人的关系状态 */
export function relationWith(ownerKey, friendKey) {
  if (!ownerKey || !friendKey) return 'none'
  const r = bucket(ownerKey)[friendKey]
  if (!r) return 'none'
  if (r.status === 'friend') return 'friend'
  return r.incoming ? 'pending_in' : 'pending_out'
}

export function isFriend(ownerKey, friendKey) {
  return relationWith(ownerKey, friendKey) === 'friend'
}

/**
 * 发送好友申请（本地模式：只写我这边一条 pending，等对方通过）。
 * person: { key, name }
 */
export function addFriend(ownerKey, person, from = 'manual') {
  const key = (person && person.key) || ''
  if (!ownerKey || !key || key === ownerKey) return { ok: false, reason: 'invalid' }
  const b = bucket(ownerKey)
  const exist = b[key]
  if (exist) {
    return {
      ok: false,
      reason: exist.status === 'friend' ? 'already' : 'pending',
      relation: exist
    }
  }
  b[key] = {
    key,
    name: (person && person.name) || key,
    status: 'pending',
    incoming: false,
    from,
    createdAt: Date.now()
  }
  persist()
  return { ok: true, relation: b[key] }
}

/** 同意好友申请：本地模式下把关系置为 friend */
export function acceptFriend(ownerKey, friendKey) {
  const b = bucket(ownerKey)
  const r = b[friendKey]
  if (!r) return { ok: false }
  r.status = 'friend'
  r.incoming = false
  r.acceptedAt = Date.now()
  persist()
  return { ok: true, relation: r }
}

/** 删除好友 / 撤回申请 / 拒绝申请 */
export function removeFriend(ownerKey, friendKey) {
  const b = bucket(ownerKey)
  if (!b[friendKey]) return { ok: false }
  delete b[friendKey]
  persist()
  return { ok: true }
}

/**
 * 本地搜索用户。
 * 本地模式没有用户库，只能从「已注册账号 + 帖子作者」里找，
 * 因此这里返回候选为空由上层提示；真实后端会走 app_user 模糊查询。
 */
export function searchLocalUsers(ownerKey, keyword) {
  const kw = String(keyword || '').trim().toLowerCase()
  if (!kw) return []
  const seen = new Map()
  // 1) 已在好友关系里的人
  for (const r of Object.values(bucket(ownerKey))) {
    if (String(r.key).toLowerCase().includes(kw) || String(r.name).toLowerCase().includes(kw)) {
      seen.set(r.key, { key: r.key, name: r.name, status: relationWith(ownerKey, r.key) })
    }
  }
  // 2) 本地帖子作者
  try {
    const raw = localStorage.getItem('mengchong.posts.v1')
    if (raw) {
      const posts = JSON.parse(raw)
      for (const p of posts || []) {
        const key = p && p.author
        if (!key || key === ownerKey) continue
        if (String(key).toLowerCase().includes(kw) ||
            String(p.authorName || '').toLowerCase().includes(kw)) {
          seen.set(key, {
            key,
            name: p.authorName || key,
            status: relationWith(ownerKey, key)
          })
        }
      }
    }
  } catch (e) {
    /* 忽略 */
  }
  return Array.from(seen.values())
}

export function newId(prefix) {
  return uid(prefix)
}
