// =====================================================================
// messageStore —— 好友私信数据（前端本地实现）
//
// 与 postStore / friendStore 同样的思路：后端暂无私信接口，先落 localStorage，
// api/message.js 预留真实接口调用，后端就绪后切换即可，组件无需改动。
//
// 数据模型：
// conversations: {
//   [ownerKey]: {                      // 以「当前登录用户名」为命名空间
//     [peerKey]: {
//       peerKey, peerName,
//       messages: [{ id, from, fromName, text, createdAt, read }],
//       updatedAt
//     }
//   }
// }
// =====================================================================

import { reactive } from 'vue'

const STORAGE_KEY = 'mengchong.messages.v1'

export const messageState = reactive({
  conversations: {},
  loaded: false
})

function uid(prefix = 'm') {
  return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

function load() {
  if (messageState.loaded) return
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      if (data && typeof data === 'object' && data.conversations) {
        messageState.conversations = data.conversations
      }
    }
  } catch (e) {
    /* 解析失败则用空数据 */
  }
  messageState.loaded = true
}

function persist() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ conversations: messageState.conversations })
    )
  } catch (e) {
    /* 存储不可用时静默忽略 */
  }
}

function bucket(ownerKey) {
  load()
  if (!messageState.conversations[ownerKey]) messageState.conversations[ownerKey] = {}
  return messageState.conversations[ownerKey]
}

function conv(ownerKey, peerKey, peerName) {
  const b = bucket(ownerKey)
  if (!b[peerKey]) {
    b[peerKey] = { peerKey, peerName: peerName || peerKey, messages: [], updatedAt: 0 }
  }
  if (peerName) b[peerKey].peerName = peerName
  return b[peerKey]
}

/* ---------------- 对外 API ---------------- */

/** 会话列表（按最后一条消息倒序） */
export function listConversations(ownerKey) {
  const b = bucket(ownerKey)
  return Object.values(b)
    .filter((c) => c.messages && c.messages.length)
    .sort((a, b2) => b2.updatedAt - a.updatedAt)
}

/** 取某个会话（可能为空） */
export function getConversation(ownerKey, peerKey, peerName) {
  const b = bucket(ownerKey)
  if (!b[peerKey] && !peerName) return null
  return conv(ownerKey, peerKey, peerName)
}

/** 发一条消息 */
export function sendMessage(ownerKey, peerKey, peerName, text, fromName) {
  const t = String(text || '').trim()
  if (!t) return { ok: false }
  const c = conv(ownerKey, peerKey, peerName)
  const msg = {
    id: uid(),
    from: ownerKey,
    fromName: fromName || ownerKey,
    text: t,
    createdAt: Date.now(),
    read: true
  }
  c.messages.push(msg)
  c.updatedAt = msg.createdAt
  persist()
  return { ok: true, message: msg, conversation: c }
}

/** 未读数（本实现里自己发的都算已读，预留给「对方发来」的消息） */
export function unreadCount(ownerKey, peerKey) {
  const c = bucket(ownerKey)[peerKey]
  if (!c || !c.messages) return 0
  return c.messages.filter((m) => !m.read && m.from !== ownerKey).length
}

/** 总未读数（消息角标用） */
export function totalUnread(ownerKey) {
  const b = bucket(ownerKey)
  return Object.values(b).reduce(
    (n, c) => n + (c.messages || []).filter((m) => !m.read && m.from !== ownerKey).length,
    0
  )
}

/** 标记会话已读 */
export function markRead(ownerKey, peerKey) {
  const c = bucket(ownerKey)[peerKey]
  if (!c || !c.messages) return
  c.messages.forEach((m) => {
    if (m.from !== ownerKey) m.read = true
  })
  persist()
}

/** 删除整个会话 */
export function removeConversation(ownerKey, peerKey) {
  const b = bucket(ownerKey)
  if (!b[peerKey]) return { ok: false }
  delete b[peerKey]
  persist()
  return { ok: true }
}

export function newId(prefix) {
  return uid(prefix)
}
