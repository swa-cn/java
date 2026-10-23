// =====================================================================
// dailyPet —— 「今日宠物」统一规则（模块级共享）
//
// 需求约定：
//  - 宠物一天一换：同一用户当天看到的宠物固定不变，第二天再变
//  - 每个用户当天随机：按「用户名 + 日期」哈希，不同用户/不同天结果不同
//  - 点击只换表情，不换宠物（表情逻辑在 PetMascot.vue 内部）
//
// 使用：
//   有 user 对象时：const pet = petOfTheDay(user?.username)
//   无 user 时（空状态等）：const pet = useTodayPet()  // 自动读当前登录用户
//   0=猫猫 1=小狗 2=兔兔 3=小熊
// =====================================================================
import { computed } from 'vue'
import { useAuth } from './useAuth'

export const PET_LABELS = ['猫猫', '小狗', '兔兔', '小熊']

// 各宠物对应的招呼语（气泡文案）
export const PET_BUBBLES = [
  '今天也要被萌到哦',   // 猫猫
  '汪！今天也要开心哦', // 小狗
  '蹦蹦跳跳元气满满~',  // 兔兔
  '今天也要软乎乎哦'    // 小熊
]

/** 简单的字符串哈希（FNV 变体），保证同输入同结果 */
function hashKey(key) {
  let h = 2166136261
  for (let i = 0; i < key.length; i++) {
    h ^= key.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

/**
 * 今日宠物索引：同一用户当天稳定，不同用户/不同天不同
 * @param {string} username 用户名（缺省用 guest，保证未登录也能展示）
 * @param {Date} [date] 指定日期（测试用，默认今天）
 * @returns {number} 0..3
 */
export function petOfTheDay(username, date = new Date()) {
  const who = String(username || 'guest').trim().toLowerCase()
  const day = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
  return hashKey(`${who}|${day}`) % PET_LABELS.length
}

/** 今日宠物对应的气泡文案 */
export function bubbleOfPet(petIndex) {
  return PET_BUBBLES[petIndex] || PET_BUBBLES[0]
}

/**
 * 响应式的「今日宠物」：自动读取当前登录用户（无登录则用 guest）
 * 供没有 user prop 的组件（空状态吉祥物等）直接使用
 */
export function useTodayPet() {
  const { state } = useAuth()
  return computed(() => petOfTheDay(state.currentUser?.username))
}
