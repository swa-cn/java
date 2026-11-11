// =====================================================================
// api/adoption.js —— 萌宠领养养成接口层
//
// 与 api/friend.js、api/post.js 保持一致的双模式设计：
//  1) USE_MOCK = false（默认）：走后端接口 /api/adoption/*，失败自动回退本地
//  2) USE_MOCK = true：直接读写 petAdoption.js（localStorage）
//
// 接口约定（见 springBootTest PetAdoptionController）：
//  GET  /adoption/state?owner=                 -> dashboard
//  POST /adoption/adopt      { owner, speciesId, name? }
//  POST /adoption/hatch      { owner, petId }
//  POST /adoption/feed       { owner, petId }
//  POST /adoption/interact   { owner, petId }
//  POST /adoption/task/complete { owner, taskId }
//  POST /adoption/task/claim    { owner, taskId }
//  POST /adoption/gift/send     { owner, friendKey }   -> 我赠送好友（落 pending）
//  POST /adoption/gift/claim    { owner, giftId }      -> 领取好友的赠送
//  POST /adoption/rename        { owner, petId, name }
//
// dashboard 字段：{ species[], nutrients, food, pets[], tasks[],
//   interact{count,limit,canInteract},
//   gifts{ sentCount, sendLimit, canSend, sentTo{}, claimCount, claimLimit, canClaim, pending[{id,fromKey}] },
//   friends[] }
// 写操作返回最新 dashboard（+ gain / reward）。
// =====================================================================

import * as local from '../composables/petAdoption'
import { reactive } from 'vue'

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
// 默认对接真实后端；显式 VITE_USE_MOCK=true 才走本地。
const USE_MOCK = String(import.meta.env.VITE_USE_MOCK) === 'true'

/** 养成功能运行态（供 UI 提示） */
export const adoptionRuntime = reactive({ localMode: USE_MOCK, reason: '' })

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
    adoptionRuntime.localMode = true
    adoptionRuntime.reason = '后端领养接口尚未就绪，已自动切换为本地模式'
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
 * 读取
 * ===================================================================== */

/** 加载面板数据：后端优先，本地回退（并刷新 speciesList / friendsList） */
export async function loadState(owner) {
  return withFallback(
    async () => {
      const d = await httpGet('/adoption/state?owner=' + encodeURIComponent(owner))
      local.setRemoteState(d)
      return d
    },
    () => {
      local.loadPetGame(owner)
      local.speciesList.value = [...local.PET_TYPES]
      local.friendsList.value = local.giftFriends(owner)
      return null
    }
  )
}

/* =====================================================================
 * 写操作（远程成功会把返回的最新 dashboard 同步进 petGame）
 * 统一返回 { ok, gain?, reward?, reason? }
 * ===================================================================== */

function normalizeRemote(d) {
  if (!d || !d.ok) return { ok: false, reason: d && d.reason }
  local.setRemoteState(d)
  return { ok: true, gain: d.gain, reward: d.reward }
}

export async function adopt(owner, speciesId, name) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/adopt', { owner, speciesId, name })),
    () => local.adoptPet(speciesId, name)
  )
}

export async function hatch(owner, petId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/hatch', { owner, petId })),
    () => local.hatchPet(petId)
  )
}

export async function feed(owner, petId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/feed', { owner, petId })),
    () => local.feedPet(petId)
  )
}

export async function interact(owner, petId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/interact', { owner, petId })),
    () => local.interactPet(petId)
  )
}

export async function completeTask(owner, taskId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/task/complete', { owner, taskId })),
    () => local.markTaskComplete(taskId)
  )
}

export async function claimTask(owner, taskId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/task/claim', { owner, taskId })),
    () => local.claimTaskReward(taskId)
  )
}

export async function sendGift(owner, friendKey) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/gift/send', { owner, friendKey })),
    () => local.sendGift(owner, friendKey)
  )
}

export async function claimGift(owner, giftId) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/gift/claim', { owner, giftId })),
    () => local.claimGift(owner, giftId)
  )
}

export async function rename(owner, petId, name) {
  return withFallback(
    async () => normalizeRemote(await httpPost('/adoption/rename', { owner, petId, name })),
    () => local.renamePet(petId, name)
  )
}

export const adoptionApiConfig = {
  API_BASE,
  USE_MOCK,
  isLocalMode: () => adoptionRuntime.localMode
}
