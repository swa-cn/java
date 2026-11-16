// =====================================================================
// chatSocket —— 私信实时推送客户端（WebSocket）
//
// 后端 /ws/chat（登录会话鉴权，握手带 Cookie）。服务端只在「有新私信落库」
// 时推一条 {type:'message', from, fromName, to, text, createdAt, self?} 事件，
// 历史消息仍走 HTTP 接口（/api/content/messages*），WS 只做「提醒」。
//
// 用法：connectChatSocket(owner, onPush)；断线自动重连（5 秒退避），
// 切换账号 / 登出时调用 reconnectChatSocket 或 disconnectChatSocket。
// =====================================================================

import { reactive } from 'vue'

export const chatSocket = reactive({ connected: false, owner: '' })

let ws = null
let onPush = null
let gen = 0 // 连接代次：断开旧连接后重连回调里的 gen 不一致即放弃，防止旧 socket 复活

/** 建立连接（重复调用会先关掉旧连接再连新的） */
export function connectChatSocket(owner, handler) {
  if (!owner) return
  onPush = handler || null
  if (ws && chatSocket.owner === owner && ws.readyState <= 1) return // 已连/连接中
  disconnectChatSocket()
  chatSocket.owner = owner
  const myGen = ++gen
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  try {
    ws = new WebSocket(`${proto}://${location.host}/ws/chat`)
  } catch (e) {
    chatSocket.connected = false
    return
  }
  ws.onopen = () => {
    if (myGen !== gen) return
    chatSocket.connected = true
  }
  ws.onmessage = (ev) => {
    if (myGen !== gen) return
    try {
      const d = JSON.parse(ev.data)
      if (d && d.type === 'message' && onPush) onPush(d)
    } catch (e) {
      /* 非 JSON 忽略 */
    }
  }
  ws.onclose = () => {
    if (myGen !== gen) return // 已被新连接取代
    chatSocket.connected = false
    // 断线重连（登录态还在且未主动断开时）
    setTimeout(() => {
      if (myGen === gen && chatSocket.owner) connectChatSocket(chatSocket.owner, onPush)
    }, 5000)
  }
  ws.onerror = () => {
    chatSocket.connected = false
  }
}

/** 切换账号：断开旧连接再连新账号 */
export function reconnectChatSocket(owner, handler) {
  disconnectChatSocket()
  connectChatSocket(owner, handler)
}

/** 主动断开（登出 / 组件卸载时可选调用） */
export function disconnectChatSocket() {
  gen++
  chatSocket.connected = false
  chatSocket.owner = ''
  if (ws) {
    try {
      ws.close()
    } catch (e) {
      /* 忽略 */
    }
    ws = null
  }
}
