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
import { updateUserProfile } from '../api/auth'

export const RE_USER = /^[A-Za-z0-9_]{3,20}$/

// 模块级响应式状态：整个应用共享同一个 currentUser
// profileError：资料保存到后端失败时的提示（设置面板展示用）
const state = reactive({ currentUser: null, profileError: '' })

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
 * 更新当前用户的个性化资料（昵称 / 邮箱 / 生日 / 个性标签 等）。
 * 三处同步：① 响应式 state（驱动全站显示）② localStorage 用户表（aurora_users_v1，
 *    本地持久化的唯一可靠来源，退出登录后仍能保留）③ 精简会话 JSON（刷新恢复用）。
 * ④ 后端落库（app_user 表）：真实后端模式下防抖 600ms 后 POST /auth/profile，
 *    退出登录 / 换浏览器 / 换设备资料仍在。失败时写入 state.profileError 供设置面板提示。
 * 注意：username 视为不可改主键，不在 patch 内。
 */
let profileSyncTimer = null

function syncProfileToServer(username) {
  clearTimeout(profileSyncTimer)
  profileSyncTimer = setTimeout(async () => {
    const u = state.currentUser
    if (!u || u.username !== username) return
    state.profileError = ''
    try {
      const res = await updateUserProfile({
        username,
        nickname: u.nickname || '',
        email: u.email || '',
        birthday: u.birthday || '',
        tags: Array.isArray(u.tags) ? u.tags : []
      })
      // 以后端落库结果为准（邮箱等字段可能被规范化）
      if (res && res.user && state.currentUser === u) {
        Object.assign(u, {
          nickname: res.user.nickname ?? u.nickname,
          email: res.user.email ?? u.email,
          birthday: res.user.birthday ?? u.birthday,
          tags: Array.isArray(res.user.tags) ? res.user.tags : u.tags
        })
      }
    } catch (e) {
      state.profileError = e.message || '资料保存失败，请稍后重试'
    }
  }, 600)
}

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

  // ④ 后端落库（防抖，避免每敲一个字就发一次请求）
  syncProfileToServer(username)
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
  clearTimeout(profileSyncTimer)
  localStorage.removeItem(SESSION_KEY)
  sessionStorage.removeItem(SESSION_KEY)
  localStorage.removeItem(SESSION_USER_KEY)
  sessionStorage.removeItem(SESSION_USER_KEY)
  state.currentUser = null
  state.profileError = ''
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

/**
 * 统一解析「个性标签」，兼容三种历史数据形态：
 *  - 数组（后端 /api/auth/login、/api/auth/profile 已把库里的 JSON 串解析成 List）
 *  - JSON 数组字符串（app_user.tags 的原始存储形态，旧会话恢复时可能直接带到前端）
 *  - 逗号 / 顿号 / 分号分隔的字符串（更早期的本地数据）
 * 返回去重后的字符串数组；脏数据一律返回空数组，避免把字符串逐字符渲染成 chips。
 */
export function parseUserTags(value) {
  let list = value
  if (typeof value === 'string') {
    const s = value.trim()
    if (!s) return []
    if (s.startsWith('[')) {
      try {
        const parsed = JSON.parse(s)
        if (!Array.isArray(parsed)) return []
        list = parsed
      } catch (e) {
        // 非标准 JSON（如 [猫奴,吃货]）：退化为按分隔符拆
        list = s.replace(/^\[|\]$/g, '').split(/[,，、;；]+/)
      }
    } else {
      list = s.split(/[,，、;；]+/)
    }
  }
  if (!Array.isArray(list)) return []
  const out = []
  for (const item of list) {
    if (typeof item !== 'string' && typeof item !== 'number') continue
    const t = String(item).trim().replace(/^["']|["']$/g, '')
    if (t && !out.includes(t)) out.push(t)
  }
  return out
}
