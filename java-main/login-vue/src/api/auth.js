// =====================================================================
// api/auth.js —— 认证后端接口
//
// 这是前端调用后端的唯一入口。当前提供两种运行模式：
//  1) USE_MOCK = false（默认，对接真实后端）：通过 fetch 请求 API_BASE 下的 REST 接口。
//     图形验证码用 canvas 在本地生成。
//  2) USE_MOCK = true（本地演示）：用浏览器 localStorage 模拟后端，数据不落库、
//
// 2026-10 起：注册不再收集邮箱；人机校验改为图形验证码。
// 接口约定：
//  GET  /auth/captcha            -> { ok, captchaId, image(data URI), ttl }
//  POST /auth/register           { username, password, nickname?, captchaId, captchaCode }
//       成功 200 { ok, user }；失败 { error: 'username'|'password'|'code', message }，冲突 409
//  POST /auth/check-username     { username }   -> { ok, taken }
//  POST /auth/login              { account, password, remember }
//       成功 200 { ok, user }；失败 401 { error: 'notfound'|'wrongpw', message }
//
// ⚠️ 安全：真实环境密码必须由后端加盐哈希（bcrypt/argon2），
//    前端只透传明文密码 + 验证码到 HTTPS，信任服务端返回结果。
// =====================================================================

import { authStore } from '../composables/authStore'
import { RE_USER, isUsernameTaken } from '../composables/useAuth'

/* ---------------- 运行时配置 ---------------- */
const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
// 默认对接真实后端；只有显式设置 VITE_USE_MOCK=true 才走本地 mock。
// 注意：mock 模式数据仅存浏览器 localStorage，跨浏览器/跨设备不可见，
// 会出现「注册账号数据库查不到、加好友对方收不到」的问题（详见 .env.example）。
const USE_MOCK = String(import.meta.env.VITE_USE_MOCK) === 'true'

/* ---------------- 密码策略（前后端共用同一套规则） ---------------- */
export const PASSWORD_POLICY = {
  minLength: 8, // 长度不能低于 8 位
  minClasses: 2 // 至少包含 2 种字符：小写 / 大写 / 数字 / 符号
}

// 字符种类：用于前端「要求清单」动态展示与后端策略校验
export const CHAR_CLASSES = [
  { key: 'lower', label: '小写字母', re: /[a-z]/ },
  { key: 'upper', label: '大写字母', re: /[A-Z]/ },
  { key: 'digit', label: '数字', re: /[0-9]/ },
  { key: 'symbol', label: '符号（如 !@#$%）', re: /[^A-Za-z0-9]/ }
]

export function passwordClasses(pw) {
  const v = String(pw || '')
  return CHAR_CLASSES.map((c) => ({ ...c, ok: c.re.test(v) }))
}

// 返回 { ok, message }，前端与（真实）后端都应遵循同一规则
export function validatePassword(pw) {
  const v = String(pw || '')
  if (v.length < PASSWORD_POLICY.minLength) {
    return { ok: false, message: `密码至少 ${PASSWORD_POLICY.minLength} 位` }
  }
  if (v.length > 128) {
    return { ok: false, message: '密码过长（最多 128 位）' }
  }
  const hit = passwordClasses(v).filter((c) => c.ok).length
  if (hit < PASSWORD_POLICY.minClasses) {
    return {
      ok: false,
      message: `密码至少包含 ${PASSWORD_POLICY.minClasses} 种字符（小写 / 大写 / 数字 / 符号）`
    }
  }
  return { ok: true }
}

/* ---------------- 真实后端请求封装 ---------------- */
async function parseBody(res) {
  let data = {}
  try {
    data = await res.json()
  } catch (e) {
    /* 忽略非 JSON 响应 */
  }
  if (!res.ok) {
    const err = new Error(data.message || '请求失败')
    err.code = data.error || res.status
    throw err
  }
  return data
}

async function httpPost(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // 允许携带 Cookie（JWT/Cookie 会话）
    body: JSON.stringify(body)
  })
  return parseBody(res)
}

async function httpGet(path) {
  const res = await fetch(API_BASE + path, { credentials: 'include' })
  return parseBody(res)
}

/* =====================================================================
 * 图形验证码
 * ===================================================================== */

/** 与后端一致的候选集：去掉易混淆的 I/L/O/0/1/2/Z 和 S/5 */
const CAPTCHA_CHARS = 'ABCDEFGHJKMNPQRSTUVWXY346789'
const CAPTCHA_LENGTH = 4
const CAPTCHA_TTL = 5 * 60 * 1000 // 5 分钟有效

/** mock 模式下当前这张验证码（真实模式的验证码在服务端内存里，前端只拿 id+图） */
let mockCaptcha = { id: '', code: '', expiresAt: 0 }

function randomCaptchaCode() {
  let s = ''
  for (let i = 0; i < CAPTCHA_LENGTH; i++) {
    s += CAPTCHA_CHARS[Math.floor(Math.random() * CAPTCHA_CHARS.length)]
  }
  return s
}

/** 用 canvas 画一张验证码图片，返回 data URL（仅 mock 模式使用） */
function drawCaptchaDataUrl(code) {
  const W = 116
  const H = 40
  const canvas = document.createElement('canvas')
  canvas.width = W
  canvas.height = H
  const ctx = canvas.getContext('2d')
  if (!ctx) return ''

  // 背景
  ctx.fillStyle = '#FFF7FA'
  ctx.fillRect(0, 0, W, H)

  // 背景装饰圆点
  ctx.fillStyle = '#FFE3EE'
  for (let i = 0; i < 12; i++) {
    const r = 3 + Math.random() * 5
    ctx.beginPath()
    ctx.arc(Math.random() * W, Math.random() * H, r, 0, Math.PI * 2)
    ctx.fill()
  }

  // 糖果色系
  const candy = ['#F78FB3', '#7E6BC4', '#63B3ED', '#53BC81', '#F6AD55', '#E85D75']

  // 字符（独立颜色 + 小旋转 + 上下偏移），与后端 CaptchaService 保持同风格
  const step = (W - 12) / CAPTCHA_LENGTH
  for (let i = 0; i < CAPTCHA_LENGTH; i++) {
    ctx.save()
    ctx.font = 'bold 30px sans-serif'
    ctx.fillStyle = candy[Math.floor(Math.random() * candy.length)]
    const x = 8 + i * step + Math.random() * 3
    const y = H / 2 + 6 + (Math.random() * 5 - 2)
    ctx.translate(x, y)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillText(code[i], -8, -10)
    ctx.restore()
  }

  // 干扰线（2 条半透明，轻干扰）
  ctx.lineWidth = 1.2
  for (let i = 0; i < 2; i++) {
    ctx.strokeStyle = candy[Math.floor(Math.random() * candy.length)] + '5a'
    ctx.beginPath()
    ctx.moveTo(Math.random() * (W / 2), Math.random() * H)
    ctx.lineTo(W / 2 + Math.random() * (W / 2), Math.random() * H)
    ctx.stroke()
  }

  // 噪点（少量）
  ctx.fillStyle = '#8E7CC3'
  for (let i = 0; i < 10; i++) {
    ctx.fillRect(Math.random() * W, Math.random() * H, 1, 1)
  }

  return canvas.toDataURL('image/png')
}

async function getCaptchaMock() {
  const id = 'mock-' + Date.now() + '-' + Math.floor(Math.random() * 1e6)
  const code = randomCaptchaCode()
  mockCaptcha = { id, code, expiresAt: Date.now() + CAPTCHA_TTL }
  return { ok: true, captchaId: id, image: drawCaptchaDataUrl(code), ttl: CAPTCHA_TTL }
}

/**
 * 获取一张图形验证码（一次性，校验一次即作废，5 分钟有效）。
 * 成功: { ok, captchaId, image(data URI), ttl }
 */
export async function getCaptcha() {
  if (USE_MOCK) return getCaptchaMock()
  return httpGet('/auth/captcha')
}

/* =====================================================================
 * 注册（用户名 + 密码 + 图形验证码，不收集邮箱）
 * ===================================================================== */

async function registerUserMock({ username, password, nickname = '', captchaId, captchaCode }) {
  const u = String(username || '').trim()
  const pw = String(password || '')

  if (!RE_USER.test(u)) return { ok: false, error: 'username', message: '用户名需为 3-20 位字母、数字或下划线' }
  if (isUsernameTaken(u)) return { ok: false, error: 'username', message: '该用户名已被占用' }

  const pv = validatePassword(pw)
  if (!pv.ok) return { ok: false, error: 'password', message: pv.message }

  const c = String(captchaCode || '').trim()
  if (!c) return { ok: false, error: 'code', message: '请输入图形验证码' }
  if (!captchaId || captchaId !== mockCaptcha.id || Date.now() > mockCaptcha.expiresAt) {
    return { ok: false, error: 'code', message: '验证码已失效，请刷新后重试' }
  }
  if (c.toUpperCase() !== mockCaptcha.code) {
    // 一次性：输错即作废，防止在同一张图上反复试
    mockCaptcha = { id: '', code: '', expiresAt: 0 }
    return { ok: false, error: 'code', message: '验证码不正确，请重新输入' }
  }
  mockCaptcha = { id: '', code: '', expiresAt: 0 }

  const user = await authStore.createUser({ username: u, email: '', password: pw, nickname })
  return { ok: true, user: { username: user.username, nickname: user.nickname } }
}

export async function registerUser(payload) {
  if (USE_MOCK) return registerUserMock(payload)
  return httpPost('/auth/register', payload)
}

/* =====================================================================
 * 登录
 * ===================================================================== */

async function loginUserMock(payload) {
  const match = authStore.findUserByAccount(payload.account)
  if (!match) return { ok: false, error: 'notfound' }
  const ok = await authStore.verifyPassword(match, payload.password)
  if (!ok) return { ok: false, error: 'wrongpw' }
  return { ok: true, user: match }
}

export async function loginUser(payload) {
  if (USE_MOCK) return loginUserMock(payload)
  return httpPost('/auth/login', payload)
}

/* =====================================================================
 * 用户名占用检测
 * ===================================================================== */
export async function checkUsername(username) {
  if (USE_MOCK) return { ok: true, taken: isUsernameTaken(username) }
  return httpPost('/auth/check-username', { username })
}

export const apiConfig = { API_BASE, USE_MOCK }
