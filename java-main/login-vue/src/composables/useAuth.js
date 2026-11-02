// =====================================================================
// useAuth —— 登录 / 会话 的状态与逻辑（模块级单例）
//
// 职责分工：
//  - 密码哈希与用户表读写 → authStore.js（纯存储层）
//  - 注册 / 验证码 / 发送验证码 → api/auth.js（预留后端接口，含本地 mock）
//  - 本文件只管：会话状态(state)、登录、登出、会话恢复、占用检测
//
// 安全说明（务必阅读）：
//  - 本模块为纯前端演示，数据保存在浏览器 localStorage，仅用于展示交互与 UI。
//  - 真实生产环境必须由后端负责校验：HTTPS 传输、服务端加盐哈希（bcrypt/argon2）、
//    速率限制、验证码、防暴力破解、JWT/Cookie 会话等。前端哈希不能替代服务端校验。
//  - 所有用户输入均通过 Vue 的模板插值（textContent）输出，天然避免 XSS。
// =====================================================================
import { reactive } from 'vue'
import { authStore, SESSION_KEY } from './authStore'

export const RE_USER = /^[A-Za-z0-9_]{3,20}$/

// 模块级响应式状态：整个应用共享同一个 currentUser
const state = reactive({ currentUser: null })

// 实时占用检测（供注册页做即时提示；仅本地演示用）
export function isUsernameTaken(username) {
  const target = String(username || '').trim().toLowerCase()
  if (!target) return false
  return !!authStore.findUserByUsername(target)
}

async function login({ account, password, remember }) {
  const match = authStore.findUserByAccount(account)
  if (!match) return { ok: false, error: 'notfound' }

  const ok = await authStore.verifyPassword(match, password)
  if (!ok) return { ok: false, error: 'wrongpw' }

  persistSession(match, remember)
  state.currentUser = mergeLocalProfile(match)
  return { ok: true, user: match }
}

// 持久化会话：mock 模式存 localStorage 的 user 记录名；真实后端模式额外存一份精简 user 以便刷新恢复
const SESSION_USER_KEY = 'aurora_session_user'

function persistSession(user, remember) {
  const store = remember ? localStorage : sessionStorage
  store.setItem(SESSION_KEY, user.username)
  store.setItem(
    SESSION_USER_KEY,
    JSON.stringify({
      username: user.username,
      email: user.email,
      nickname: user.nickname,
      // 个性化资料字段：昵称/生日/标签（缺省时 undefined 无害）
      birthday: user.birthday || null,
      tags: Array.isArray(user.tags) ? user.tags : null,
      createdAt: user.createdAt || null,
      // 管理员标记：仅控制「后台管理」入口显隐（后端接口另有会话级校验）
      isAdmin: !!user.isAdmin
    })
  )
}

/**
 * 更新当前用户的个性化资料（昵称 / 生日 / 个性标签 等）。
 * 三处同步：① 响应式 state（驱动全站显示）② localStorage 用户表（aurora_users_v1，
 *    本地持久化的唯一可靠来源，退出登录后仍能保留）③ 精简会话 JSON（刷新恢复用）。
 * 注意：username 视为不可改主键，不在 patch 内。
 */
function updateProfile(patch) {
  const u = state.currentUser
  if (!u || typeof u !== 'object') return
  const username = u.username
  if (!username) return

  // ① 响应式 state（驱动全站显示）
  Object.assign(u, patch)

  // ② 写回用户数据表（aurora_users_v1）：按 username upsert。
  //    真实后端模式下本地表里原本没有当前用户，这里找不到就新建一条，
  //    以 username 为主键 —— 这样即使退出登录（清掉会话）再登录，资料也不会丢。
  const users = authStore.getUsers()
  const target = users.find((x) => x.username === username)
  if (target) {
    Object.assign(target, patch)
  } else {
    users.push({
      username,
      email: u.email || '',
      nickname: u.nickname || '',
      birthday: u.birthday || null,
      tags: Array.isArray(u.tags) ? [...u.tags] : [],
      createdAt: u.createdAt || null
    })
  }
  authStore.saveUsers(users)

  // ③ 同步精简会话（刷新恢复用，确保新字段带得上）
  const store = localStorage.getItem(SESSION_KEY)
    ? localStorage
    : sessionStorage.getItem(SESSION_KEY)
      ? sessionStorage
      : null
  if (store) {
    const raw = store.getItem(SESSION_USER_KEY)
    if (raw) {
      try {
        const s = JSON.parse(raw)
        Object.assign(s, patch)
        store.setItem(SESSION_USER_KEY, JSON.stringify(s))
      } catch (e) {
        /* 忽略损坏数据 */
      }
    }
  }
}

/**
 * 把本地用户数据表里保存的昵称 / 生日 / 标签合并回当前用户。
 * 用于：真实后端模式登录（data.user 不含本地资料）或会话恢复时，
 * 让「退出登录再登录」后资料依然在。仅覆盖本地确实存过的字段。
 */
function mergeLocalProfile(user) {
  if (!user || !user.username) return user
  const local = authStore.getUsers().find((x) => x.username === user.username)
  if (!local) return user
  if (local.nickname != null && local.nickname !== '') user.nickname = local.nickname
  if (local.birthday != null && local.birthday !== '') user.birthday = local.birthday
  if (Array.isArray(local.tags) && local.tags.length) user.tags = local.tags
  return user
}

// 真实后端登录成功后，由 AuthCard 调用，设置当前用户并持久化会话
export function setCurrentUser(user, remember) {
  state.currentUser = mergeLocalProfile(user)
  persistSession(state.currentUser, remember)
}

function logout() {
  localStorage.removeItem(SESSION_KEY)
  sessionStorage.removeItem(SESSION_KEY)
  localStorage.removeItem(SESSION_USER_KEY)
  sessionStorage.removeItem(SESSION_USER_KEY)
  state.currentUser = null
}

function restoreSession() {
  const name = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY)
  if (!name) return
  const match = authStore.findUserByAccount(name)
  if (match) {
    state.currentUser = match
    return
  }
  // 真实后端模式：本地没有 mock 用户记录，尝试从持久化的精简 user 恢复
  const raw = localStorage.getItem(SESSION_USER_KEY) || sessionStorage.getItem(SESSION_USER_KEY)
  if (raw) {
    try {
      state.currentUser = JSON.parse(raw)
    } catch (e) {
      /* 忽略损坏数据 */
    }
  }
}

export function useAuth() {
  return {
    state,
    login,
    logout,
    restoreSession,
    setCurrentUser,
    updateProfile,
    isUsernameTaken
  }
}
