// =====================================================================
// useAuth —— 登录 / 注册 / 会话 的状态与逻辑（模块级单例）
//
// 安全说明（务必阅读）：
//  - 本模块为纯前端演示，用户数据保存在浏览器 localStorage，仅用于展示交互与 UI。
//  - 密码使用「每用户随机盐 + SHA-256」做哈希后存储，绝不保存明文。
//  - 真实生产环境必须使用后端：HTTPS 传输、服务端加盐哈希（bcrypt/argon2）、
//    速率限制、验证码、防暴力破解、JWT/Cookie 会话等。前端哈希不能替代服务端校验。
//  - 所有用户输入均通过 Vue 的模板插值（textContent）输出，天然避免 XSS。
// =====================================================================
import { reactive } from 'vue'

const USERS_KEY = 'aurora_users_v1'
const SESSION_KEY = 'aurora_session_v1'

function getUsers() {
  try {
    const raw = localStorage.getItem(USERS_KEY)
    const arr = raw ? JSON.parse(raw) : []
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}

function saveUsers(users) {
  localStorage.setItem(USERS_KEY, JSON.stringify(users))
}

function makeSalt() {
  let bytes
  if (window.crypto && crypto.getRandomValues) {
    bytes = new Uint8Array(16)
    crypto.getRandomValues(bytes)
  } else {
    bytes = []
    for (let i = 0; i < 16; i++) bytes.push(Math.floor(Math.random() * 256))
  }
  return Array.prototype.map
    .call(bytes, (b) => ('0' + b.toString(16)).slice(-2))
    .join('')
}

async function derive(salt, password) {
  const s = salt + ':' + password
  if (window.crypto && crypto.subtle) {
    const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(s))
    return Array.prototype.map
      .call(new Uint8Array(buf), (b) => ('0' + b.toString(16)).padStart(2, '0'))
      .join('')
  }
  return fallbackHash(s)
}

function fallbackHash(str) {
  let h1 = 0xDEADBEEF
  let h2 = 0x41c6ce57
  for (let i = 0; i < str.length; i++) {
    const ch = str.charCodeAt(i)
    h1 = Math.imul(h1 ^ ch, 2654435761)
    h2 = Math.imul(h2 ^ ch, 1597334677)
  }
  h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507) ^ Math.imul(h2 ^ (h2 >>> 13), 3266489909)
  h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507) ^ Math.imul(h1 ^ (h1 >>> 13), 3266489909)
  return (h2 >>> 0).toString(16).padStart(8, '0') + (h1 >>> 0).toString(16).padStart(8, '0')
}

export const RE_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const RE_USER = /^[A-Za-z0-9_]{3,20}$/

// 模块级响应式状态：整个应用共享同一个 currentUser
const state = reactive({ currentUser: null })

async function register({ username, email, password }) {
  const users = getUsers()
  const lower = email.toLowerCase()
  for (const u of users) {
    if (u.username.toLowerCase() === username.toLowerCase()) {
      return { ok: false, error: 'username' }
    }
    if (u.email.toLowerCase() === lower) {
      return { ok: false, error: 'email' }
    }
  }
  const salt = makeSalt()
  const hash = await derive(salt, password)
  users.push({
    username,
    email,
    salt,
    pwHash: hash,
    nickname: '',
    createdAt: Date.now()
  })
  saveUsers(users)
  return { ok: true }
}

async function login({ account, password, remember }) {
  const users = getUsers()
  let match = null
  for (const u of users) {
    if (u.username === account || u.email.toLowerCase() === account.toLowerCase()) {
      match = u
      break
    }
  }
  if (!match) return { ok: false, error: 'notfound' }

  const hash = await derive(match.salt, password)
  if (hash !== match.pwHash) return { ok: false, error: 'wrongpw' }

  startSession(match.username, remember)
  state.currentUser = match
  return { ok: true, user: match }
}

function startSession(username, remember) {
  if (remember) localStorage.setItem(SESSION_KEY, username)
  else sessionStorage.setItem(SESSION_KEY, username)
}

function logout() {
  localStorage.removeItem(SESSION_KEY)
  sessionStorage.removeItem(SESSION_KEY)
  state.currentUser = null
}

function restoreSession() {
  const name = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY)
  if (!name) return
  const users = getUsers()
  for (const u of users) {
    if (u.username === name || u.email === name) {
      state.currentUser = u
      return
    }
  }
}

export function useAuth() {
  return { state, register, login, logout, restoreSession }
}
