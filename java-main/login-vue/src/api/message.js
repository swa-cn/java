// =====================================================================
// api/message.js —— 好友私信接口层
//
// 与 api/post.js、api/friend.js 一致的双模式设计：
//  1) USE_MOCK = false（默认）：走后端接口，接口不可用时自动回退本地
//  2) USE_MOCK = true：直接读写 messageStore（localStorage，跨浏览器不可见）
//
// 接口约定（待后端实现）：
//  GET  /content/messages/conversations?owner=<key>     -> { ok, items: [Conversation] }
//  GET  /content/messages?owner=<key>&peer=<key>        -> { ok, conversation }
//  POST /content/messages                               { owner, peer, peerName, text, fromName } -> { ok, message }
//  POST /content/messages/read                          { owner, peer } -> { ok }
//  DELETE /content/messages?owner=<key>&peer=<key>       -> { ok }
//
// Conversation: { peerKey, peerName, messages: [Message], updatedAt }
// Message     : { id, from, fromName, text, createdAt, read }
// =====================================================================

import * as store from '../composables/messageStore'
import { reactive } from 'vue'

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
// 默认对接真实后端；只有显式设置 VITE_USE_MOCK=true 才走本地 mock。
// 注意：mock 模式数据仅存浏览器 localStorage，跨浏览器/跨设备不可见，
// 会出现「注册账号数据库查不到、加好友对方收不到」的问题（详见 .env.example）。
const USE_MOCK = String(import.meta.env.VITE_USE_MOCK) === 'true'

/** 私信功能运行态（供 UI 提示） */
export const messageRuntime = reactive({ localMode: USE_MOCK, reason: '' })

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
    messageRuntime.localMode = true
    messageRuntime.reason = '后端私信接口尚未就绪，已自动切换为本地模式'
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

/** 会话列表 */
export async function fetchConversations(ownerKey) {
  return withFallback(
    () => httpGet('/content/messages/conversations?owner=' + encodeURIComponent(ownerKey)),
    () => ({ ok: true, items: store.listConversations(ownerKey) })
  )
}

/** 单个会话（与某人的聊天记录） */
export async function fetchConversation(ownerKey, peerKey, peerName) {
  return withFallback(
    () =>
      httpGet(
        '/content/messages?owner=' +
          encodeURIComponent(ownerKey) +
          '&peer=' +
          encodeURIComponent(peerKey)
      ),
    () => ({
      ok: true,
      conversation: store.getConversation(ownerKey, peerKey, peerName)
    })
  )
}

/* =====================================================================
 * 变更
 * ===================================================================== */

/** 发送消息 */
export async function sendMessage(ownerKey, peerKey, peerName, text, fromName) {
  return withFallback(
    () =>
      httpPost('/content/messages', {
        owner: ownerKey,
        peer: peerKey,
        peerName,
        text,
        fromName
      }),
    () => store.sendMessage(ownerKey, peerKey, peerName, text, fromName)
  )
}

/** 标记会话已读 */
export async function markConversationRead(ownerKey, peerKey) {
  return withFallback(
    () => httpPost('/content/messages/read', { owner: ownerKey, peer: peerKey }),
    () => store.markRead(ownerKey, peerKey)
  )
}

/** 删除会话 */
export async function removeConversation(ownerKey, peerKey) {
  return withFallback(
    async () => {
      const res = await fetch(
        API_BASE +
          '/content/messages?owner=' +
          encodeURIComponent(ownerKey) +
          '&peer=' +
          encodeURIComponent(peerKey),
        { method: 'DELETE', credentials: 'include' }
      )
      return parseBody(res)
    },
    () => store.removeConversation(ownerKey, peerKey)
  )
}

export const messageApiConfig = { API_BASE, USE_MOCK, isLocalMode: () => messageRuntime.localMode }
