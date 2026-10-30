// =====================================================================
// notificationStore —— 通知中心（模块级单例，双模式）
//
// 数据来源（由 Home 传入，兼容「真实后端」与「本地 mock」两种模式）：
//  - posts            ：我的动态列表（api/post.js fetchPosts({author})，
//                       后端在跑走 /content/posts，否则走本地 postStore）
//  - incomingRequests ：别人发给我的好友申请（api/friend.js fetchIncoming）
//
// 通知类型：
//  - comment：我的动态收到评论（评论自带 createdAt，按时间判未读）
//  - like   ：我的动态收到赞。两种模式的 likes 都只是 key 数组（无时间），
//             所以用「已见赞数快照」检测新赞：localStorage 记每个帖子上次
//             打开面板时的赞数，数量涨了 → 有新赞；涨了几条就提示几条。
//  - friend ：好友申请
//
// 「已读」= 打开面板时把 seenAt 推进到现在、并把赞数快照刷新为当前值。
// =====================================================================
import { reactive } from 'vue'

const SEEN_KEY = 'mengchong-notification-seen'
const LIKE_SEEN_KEY = 'mengchong-like-seen'

function loadJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    const v = raw ? JSON.parse(raw) : null
    return v && typeof v === 'object' ? v : fallback
  } catch (e) {
    return fallback
  }
}

export const notificationState = reactive({
  seenAt: Number(loadJson(SEEN_KEY, { v: 0 }).v) || 0,
  /** 帖子 id -> 上次已读时的赞数 */
  likeSeen: loadJson(LIKE_SEEN_KEY, {})
})

function persistSeen() {
  try {
    localStorage.setItem(SEEN_KEY, JSON.stringify({ v: notificationState.seenAt }))
    localStorage.setItem(LIKE_SEEN_KEY, JSON.stringify(notificationState.likeSeen))
  } catch (e) {
    /* 忽略 */
  }
}

/** 打开面板时调用：全部标记为已读（时间戳 + 赞数快照一起推进） */
export function markNotificationsSeen(posts = []) {
  notificationState.seenAt = Date.now()
  for (const p of posts) {
    if (p && p.id != null) {
      notificationState.likeSeen[String(p.id)] = likeCount(p)
    }
  }
  persistSeen()
}

function likeCount(post) {
  return (Array.isArray(post.likes) ? post.likes : []).length
}

/**
 * 生成通知列表（最新在前）。
 * @param {string} ownerKey  当前用户名
 * @param {Array}  posts     我的动态（含 likes/comments）
 * @param {Array}  incoming  好友申请列表（可选；元素需有 key/name/createdAt）
 */
export function buildNotifications(ownerKey, posts = [], incoming = []) {
  if (!ownerKey) return []
  const seen = notificationState.seenAt
  const list = []

  // 1) 好友申请
  for (const r of incoming) {
    const at = r.createdAt || 0
    list.push({
      id: 'friend-' + r.key,
      type: 'friend',
      at,
      unread: at > seen,
      fromKey: r.key,
      fromName: r.name || r.key
    })
  }

  for (const p of posts) {
    if (!p) continue

    // 2) 评论：每条一个通知（有时间和人名）
    for (const c of p.comments || []) {
      if (c.author === ownerKey) continue // 自己回自己不算
      const at = c.createdAt ? new Date(c.createdAt).getTime() : 0
      list.push({
        id: 'comment-' + (c.id || p.id + '-' + at),
        type: 'comment',
        at,
        unread: at > seen,
        postId: p.id,
        postTitle: p.title,
        fromKey: c.author,
        fromName: c.authorName || c.author
      })
    }

    // 3) 赞：与快照比数量，涨了就是新赞
    const cur = likeCount(p)
    const old = Number(notificationState.likeSeen[String(p.id)] ?? cur) // 首次见到=静默初始化，不报历史赞
    const fresh = cur - old
    if (fresh > 0) {
      // 本地模式有 likeLog 时能显示是谁；后端模式只报数量
      const names = (p.likeLog || [])
        .filter((l) => l && l.key !== ownerKey && l.name)
        .map((l) => l.name)
      list.push({
        id: 'like-' + p.id + '-' + cur,
        type: 'like',
        at: notificationState.seenAt || Date.now(),
        unread: true,
        postId: p.id,
        postTitle: p.title,
        count: fresh,
        total: cur,
        fromName: names[0] || ''
      })
    } else if (cur > 0) {
      // 历史赞：显示为已读的汇总，保证面板不空
      list.push({
        id: 'like-' + p.id + '-' + cur,
        type: 'like',
        at: 0,
        unread: false,
        postId: p.id,
        postTitle: p.title,
        count: 0,
        total: cur,
        fromName: ''
      })
    }
  }

  return list.sort((a, b) => b.at - a.at)
}
