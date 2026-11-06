// =====================================================================
// petAdoption —— 萌宠领养养成（前端本地实现，localStorage 持久化）
//
// 一套自包含的「领养 → 孵化 → 喂养 → 互动」小游戏逻辑：
//   - 至少 5 种可领养宠物（这里给了 6 种，emoji 渲染，无需图片资源）
//   - 领养后是一枚蛋，孵化需要「养分」
//   - 养分来源：① 做任务（每个任务 5~10 点）；② 好友赠送（每次 +10 点）
//   - 食物/饲料来源：① 做任务（每个任务 1 份）；② 好友赠送（每次 1 份）
//   - 喂食消耗 1 份食物，并增加「亲密度」
//   - 点击宠物可「摸摸」互动：每天最多 5 次，每次 +1~5 亲密度
//   - 好友赠送：每次 +10 养分 +1 食物，同一好友每天可领 1 次，每日最多领 3 次
//
// 与 friendStore / petData 同样的思路：纯前端 localStorage，不依赖后端，
// 因此断网 / 后端未启动也能正常玩，不会把正在跑的服务搞挂。
// =====================================================================

import { reactive, computed, ref } from 'vue'
import { listFriends } from './friendStore'

const STORAGE_KEY = 'mengchong.petadoption.v1'

/* ---------------- 可领养的宠物种类（≥5 种） ---------------- */
export const PET_TYPES = [
  { id: 'cat',     name: '布偶猫',     emoji: '🐱', color: '#ff6fa8', desc: '蓝眼睛小天使，黏人又温柔',   hatchCost: 20 },
  { id: 'dog',     name: '小柯基',     emoji: '🐶', color: '#ff8a5c', desc: '蜜桃臀小短腿，快乐源泉',   hatchCost: 25 },
  { id: 'rabbit',  name: '垂耳兔',     emoji: '🐰', color: '#b98bff', desc: '长耳软萌，安静小可爱',     hatchCost: 20 },
  { id: 'hamster', name: '三线仓鼠',   emoji: '🐹', color: '#ffb35c', desc: '腮帮子仓库，囤粮小能手',   hatchCost: 15 },
  { id: 'bird',    name: '玄凤鹦鹉',   emoji: '🐦', color: '#3ec4a8', desc: '黄冠腮红，吹口哨高手',     hatchCost: 20 },
  { id: 'fish',    name: '小金鱼',     emoji: '🐠', color: '#4ab3e8', desc: '圆肚小福星，慢悠悠摆尾',   hatchCost: 15 }
]

export function getPetType(typeId) {
  return PET_TYPES.find((t) => t.id === typeId) || null
}

/* ---------------- 任务模板 ----------------
 * rewardType: 'nutrient'（养分，随机 5~10）| 'food'（食物，固定 1 份）
 */
const TASK_TEMPLATES = [
  { id: 'sign',   title: '每日签到',         desc: '打卡领养分',           rewardType: 'nutrient' },
  { id: 'read',   title: '阅读萌宠小知识',   desc: '涨知识也涨养分',       rewardType: 'nutrient' },
  { id: 'quiz',   title: '完成萌宠小测验',   desc: '考考你对毛孩子的了解', rewardType: 'nutrient' },
  { id: 'walk',   title: '带毛孩子散步',     desc: '出门溜达一圈',         rewardType: 'nutrient' },
  { id: 'post',   title: '发布一条动态',     desc: '分享萌宠得饲料',       rewardType: 'food' },
  { id: 'like',   title: '给好友动态点赞',   desc: '送出爱心得饲料',       rewardType: 'food' },
  { id: 'jump',   title: '陪宠物跳障碍',     desc: '操控毛孩子跳跃躲障碍', rewardType: 'nutrient' }
]

/* ---------------- 全局响应式状态 ---------------- */
export const petGame = reactive({
  ready: false,
  userKey: '',
  nutrients: 0, // 养分
  food: 0,     // 食物 / 饲料
  pets: [],    // 我的宠物
  tasks: [],   // 今日任务（含完成状态）
  taskDate: '',
  gifts: { date: '', claimed: {}, count: 0 }, // 好友赠送（按天重置）
  interact: { date: '', count: 0 }            // 每日摸摸次数（按天重置，上限 5）
})

/* ---------------- 派生数据 ---------------- */
export const myEggs = computed(() => petGame.pets.filter((p) => p.status === 'egg'))
export const myPets = computed(() => petGame.pets.filter((p) => p.status === 'hatched'))
export const nutrientEnough = (cost) => petGame.nutrients >= cost
export const canGiftToday = computed(() => petGame.gifts.count < 3)
export const canInteractToday = computed(() => petGame.interact.count < 5)

/* ---------------- 后端同步（petAdoption.js 同时作为本地兜底） ---------------- */

/** 物种列表（后端返回时覆盖；否则用内置 PET_TYPES） */
export const speciesList = ref([...PET_TYPES])

/** 真实好友列表（后端返回；用于好友赠送入口与列表） */
export const friendsList = ref([])

/**
 * 把后端 dashboard 映射到本地 petGame，使组件渲染层无需感知数据来源。
 * 后端任务用 claimed 表示已领取，这里统一映射为 done 以兼容组件逻辑。
 */
export function setRemoteState(d) {
  if (!d) return
  speciesList.value = Array.isArray(d.species) && d.species.length ? d.species : [...PET_TYPES]
  friendsList.value = Array.isArray(d.friends) ? d.friends : []
  petGame.nutrients = d.nutrients || 0
  petGame.food = d.food || 0
  petGame.pets = Array.isArray(d.pets) ? d.pets.map((p) => ({ ...p })) : []
  petGame.tasks = (Array.isArray(d.tasks) ? d.tasks : []).map((t) => ({
    id: t.id,
    title: t.title,
    desc: t.desc,
    rewardType: t.rewardType,
    completed: !!t.completed,
    done: !!t.claimed
  }))
  const today = todayStr()
  petGame.interact = { date: today, count: (d.interact && d.interact.count) || 0 }
  petGame.gifts = {
    date: today,
    count: (d.gifts && d.gifts.count) || 0,
    claimed: (d.gifts && d.gifts.claimed) || {}
  }
}

/* ---------------- 工具函数 ---------------- */
function todayStr() {
  const d = new Date()
  return (
    d.getFullYear() +
    '-' +
    String(d.getMonth() + 1).padStart(2, '0') +
    '-' +
    String(d.getDate()).padStart(2, '0')
  )
}

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function uid(prefix = 'pet') {
  return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

function clamp(v, min, max) {
  return Math.max(min, Math.min(max, v))
}

/* ---------------- 持久化 ---------------- */
function persist() {
  if (!petGame.ready || !petGame.userKey) return
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        userKey: petGame.userKey,
        nutrients: petGame.nutrients,
        food: petGame.food,
        pets: petGame.pets,
        tasks: petGame.tasks.map((t) => ({ id: t.id, done: t.done, completed: t.completed })),
        taskDate: petGame.taskDate,
        gifts: petGame.gifts,
        interact: petGame.interact
      })
    )
  } catch (e) {
    /* 存储不可用时静默忽略 */
  }
}

/** 按天重置：任务完成状态 + 好友赠送次数 */
function ensureDailyReset() {
  const today = todayStr()
  if (petGame.taskDate !== today) {
    petGame.taskDate = today
    petGame.tasks = TASK_TEMPLATES.map((t) => ({ ...t, done: false, completed: false }))
  }
  if (petGame.gifts.date !== today) {
    petGame.gifts = { date: today, claimed: {}, count: 0 }
  }
  if (petGame.interact.date !== today) {
    petGame.interact = { date: today, count: 0 }
  }
}

/** 读取某用户的数据（首次进入页面时调用一次） */
export function loadPetGame(userKey) {
  petGame.userKey = userKey || 'guest'
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const d = JSON.parse(raw)
      if (d && d.userKey === petGame.userKey) {
        petGame.nutrients = d.nutrients || 0
        petGame.food = d.food || 0
        petGame.pets = Array.isArray(d.pets) ? d.pets : []
        petGame.taskDate = d.taskDate || ''
        petGame.gifts = d.gifts || { date: '', claimed: {}, count: 0 }
        petGame.interact = d.interact || { date: '', count: 0 }
        // 任务：以模板为准，仅恢复 done / completed 标记
        const savedDone = {}
        const savedCompleted = {}
        ;(d.tasks || []).forEach((t) => {
          if (t && t.id) {
            savedDone[t.id] = !!t.done
            savedCompleted[t.id] = !!t.completed
          }
        })
        petGame.tasks = TASK_TEMPLATES.map((t) => ({
          ...t,
          done: !!savedDone[t.id],
          completed: !!savedCompleted[t.id]
        }))
      }
    }
  } catch (e) {
    /* 解析失败则用空数据 */
  }
  if (!petGame.tasks.length) {
    petGame.tasks = TASK_TEMPLATES.map((t) => ({ ...t, done: false, completed: false }))
  }
  ensureDailyReset()
  petGame.ready = true
  return petGame
}

/* ---------------- 领养 / 孵化 / 喂养 / 互动 ---------------- */

/** 领养一枚蛋（petTypeId 对应 PET_TYPES 的 id） */
export function adoptPet(typeId) {
  const type = getPetType(typeId)
  if (!type) return { ok: false, reason: 'unknown' }
  petGame.pets.push({
    id: uid(),
    typeId,
    name: type.name,
    status: 'egg',
    intimacy: 0, // 亲密度 0~100
    satiety: 60, // 饱食度 0~100
    mood: 80,    // 心情 0~100
    createdAt: Date.now(),
    hatchedAt: null,
    lastFed: 0,
    lastInteract: 0
  })
  persist()
  return { ok: true }
}

/** 孵化：消耗 hatchCost 养分，把蛋变成宠物 */
export function hatchPet(petId) {
  const pet = petGame.pets.find((p) => p.id === petId)
  if (!pet) return { ok: false, reason: 'notfound' }
  if (pet.status !== 'egg') return { ok: false, reason: 'already' }
  const type = getPetType(pet.typeId)
  const cost = type ? type.hatchCost : 20
  if (petGame.nutrients < cost) return { ok: false, reason: 'lack', need: cost }
  petGame.nutrients -= cost
  pet.status = 'hatched'
  pet.hatchedAt = Date.now()
  persist()
  return { ok: true, cost }
}

/** 喂食：消耗 1 份食物，增加亲密度与饱食度 */
export function feedPet(petId) {
  const pet = petGame.pets.find((p) => p.id === petId)
  if (!pet) return { ok: false, reason: 'notfound' }
  if (pet.status !== 'hatched') return { ok: false, reason: 'notegg' }
  if (petGame.food <= 0) return { ok: false, reason: 'nofood' }
  petGame.food -= 1
  const gain = randInt(4, 8)
  pet.intimacy = clamp(pet.intimacy + gain, 0, 100)
  pet.satiety = clamp(pet.satiety + 25, 0, 100)
  pet.mood = clamp(pet.mood + 5, 0, 100)
  pet.lastFed = Date.now()
  persist()
  return { ok: true, gain }
}

/** 互动（摸摸 / 点击）：每天最多 5 次，每次 +1~5 亲密度 */
export function interactPet(petId) {
  const pet = petGame.pets.find((p) => p.id === petId)
  if (!pet) return { ok: false, reason: 'notfound' }
  if (pet.status !== 'hatched') return { ok: false, reason: 'notegg' }
  ensureDailyReset()
  if (petGame.interact.count >= 5) return { ok: false, reason: 'limit' }
  petGame.interact.count += 1
  const gain = randInt(1, 5)
  pet.intimacy = clamp(pet.intimacy + gain, 0, 100)
  pet.mood = clamp(pet.mood + 2, 0, 100)
  pet.lastInteract = Date.now()
  persist()
  return { ok: true, gain }
}

/** 改名 */
export function renamePet(petId, name) {
  const pet = petGame.pets.find((p) => p.id === petId)
  if (!pet) return { ok: false }
  pet.name = (String(name || '').trim()) || pet.name
  persist()
  return { ok: true }
}

/* ---------------- 任务 ---------------- */

/** 标记任务已完成（必须先完成，才能领取奖励） */
export function markTaskComplete(taskId) {
  const task = petGame.tasks.find((t) => t.id === taskId)
  if (!task) return { ok: false, reason: 'notfound' }
  if (task.done) return { ok: false, reason: 'done' }
  task.completed = true
  persist()
  return { ok: true }
}

/** 领取任务奖励：nutrient 随机 5~10，food 固定 1 份（必须已完成任务） */
export function claimTaskReward(taskId) {
  const task = petGame.tasks.find((t) => t.id === taskId)
  if (!task) return { ok: false, reason: 'notfound' }
  if (!task.completed) return { ok: false, reason: 'incomplete' }
  if (task.done) return { ok: false, reason: 'done' }
  task.done = true
  let reward
  if (task.rewardType === 'food') {
    petGame.food += 1
    reward = { type: 'food', amount: 1 }
  } else {
    const n = randInt(5, 10)
    petGame.nutrients += n
    reward = { type: 'nutrient', amount: n }
  }
  persist()
  return { ok: true, reward }
}

/* ---------------- 好友赠送 ---------------- */

/** 当前可用于赠送的好友列表（仅返回真实好友；无则为空，决定「好友赠送」是否显示） */
export function giftFriends(userKey) {
  return listFriends(userKey || petGame.userKey, 'friend').map((r) => ({ key: r.key, name: r.name }))
}

/** 是否拥有真实好友：为 true 时「好友赠送」入口才显示 */
export function hasRealFriends(userKey) {
  return giftFriends(userKey || petGame.userKey).length > 0
}

/** 领取某好友的赠送：+10 养分 +1 食物；同好友每天 1 次，每日最多 3 次 */
export function claimGift(friendKey) {
  ensureDailyReset()
  if (petGame.gifts.count >= 3) return { ok: false, reason: 'limit' }
  if (petGame.gifts.claimed[friendKey]) return { ok: false, reason: 'already' }
  petGame.gifts.claimed[friendKey] = true
  petGame.gifts.count += 1
  petGame.nutrients += 10
  petGame.food += 1
  persist()
  return { ok: true }
}
