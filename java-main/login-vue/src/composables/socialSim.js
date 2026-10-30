// =====================================================================
// socialSim —— 星球居民模拟互动（纯前端演示用）
//
// 解决什么问题：本地演示只有你一个真实用户，自己发的动态永远没人点赞/评论，
// 通知中心永远是空的。这里让几位「星球居民」（取自种子动态的作者人设）
// 在你发帖后陆续来互动 —— 走 postStore 正规的 toggleLike / addComment 通道，
// 数据真实落库，通知中心、动态详情、点赞数全部联动，不是假弹窗。
//
// 真实接入后端后：把 publishPost 里对 scheduleSocialFeedback 的调用删掉即可，
// 通知将来自服务端推送。
// =====================================================================
import { toggleLike, addComment } from './postStore'

/** 星球居民人设（与种子动态作者一致，保证世界观统一） */
const RESIDENTS = [
  { key: 'miao', name: '猫奴小七' },
  { key: 'wang', name: '遛狗达人' },
  { key: 'snow', name: '仓鼠观察员' },
  { key: 'rabbit', name: '鸟语者' },
  { key: 'hamster', name: '观鱼人' }
]

const COMMENTS = [
  '也太可爱了吧，想 rua！',
  '拍得真好，求分享经验~',
  '已收藏，周末就照着试试！',
  '哇，这是我见过最上镜的小家伙',
  '哈哈哈这个姿势绝了',
  '感谢分享，学到了！',
  '这也太治愈了吧'
]

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

/**
 * 给一篇新动态安排居民互动。
 * 约 5 秒后第一位来点赞；50% 概率约 12 秒后第二位来评论。
 * 返回清理函数（组件卸载时可调用，避免测试时残留定时器）。
 */
export function scheduleSocialFeedback(postId) {
  if (!postId) return () => {}
  const timers = []

  // 第一位：点赞
  timers.push(
    setTimeout(() => {
      const r = pick(RESIDENTS)
      toggleLike(postId, r.key, r.name)
    }, 5000)
  )

  // 第二位（一半概率）：评论
  if (Math.random() < 0.5) {
    timers.push(
      setTimeout(() => {
        const r = pick(RESIDENTS)
        addComment(postId, {
          author: r.key,
          authorName: r.name,
          content: pick(COMMENTS)
        })
      }, 12000)
    )
  }

  return () => timers.forEach(clearTimeout)
}
