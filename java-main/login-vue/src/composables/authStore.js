// =====================================================================
// authStore —— 用户数据 & 密码哈希的「纯存储层」（无网络、无会话状态）
//
// 这一层只负责：读写 localStorage 里的用户表、为密码做「随机盐 + SHA-256」哈希。
// 登录/注册/验证码等业务逻辑放在 api/auth.js（预留后端接口），
// 会话状态放在 useAuth.js。三者互不循环依赖。
//
// 安全说明（务必阅读）：
//  - 本层仅为纯前端演示；真实生产环境必须由「后端」负责密码哈希与校验，
//    前端只传明文密码 + 验证码到 HTTPS 接口，绝不在前端信任任何校验结果。
//  - 用户数据保存在浏览器 localStorage，仅用于展示交互与 UI。
// =====================================================================

export const USERS_KEY = 'aurora_users_v1'
export const SESSION_KEY = 'aurora_session_v1'

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

export function findUserByAccount(account) {
  const target = String(account || '').trim().toLowerCase()
  return getUsers().find(
    (u) => u.username.toLowerCase() === target || String(u.email || '').toLowerCase() === target
  )
}

export function findUserByUsername(username) {
  const target = String(username || '').trim().toLowerCase()
  return getUsers().find((u) => u.username.toLowerCase() === target)
}

export function findUserByEmail(email) {
  const target = String(email || '').trim().toLowerCase()
  if (!target) return undefined
  return getUsers().find((u) => String(u.email || '').toLowerCase() === target)
}

// 创建用户：加盐哈希后落库（真实后端等价操作，这里仅本地演示）
export async function createUser({ username, email, password, nickname }) {
  const salt = makeSalt()
  const pwHash = await derive(salt, password)
  const user = {
    username,
    email,
    salt,
    pwHash,
    nickname: String(nickname || '').trim(),
    createdAt: Date.now()
  }
  const users = getUsers()
  users.push(user)
  saveUsers(users)
  return user
}

export function verifyPassword(user, password) {
  // 注意：mock 模式用同步断言；真实场景应在后端用 timing-safe 比较
  return derive(user.salt, password).then((hash) => hash === user.pwHash)
}

export const authStore = {
  USERS_KEY,
  SESSION_KEY,
  getUsers,
  saveUsers,
  makeSalt,
  derive,
  fallbackHash,
  findUserByAccount,
  findUserByUsername,
  findUserByEmail,
  createUser,
  verifyPassword
}
