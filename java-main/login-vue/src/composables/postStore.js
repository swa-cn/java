// =====================================================================
// postStore —— 「发现」帖子数据（前端本地实现）
//
// 当前后端只有认证接口，帖子相关接口尚未实现，因此这里用 localStorage
// 保存帖子文本数据（点赞、评论、标签等），图片/视频的二进制放在 IndexedDB
// （见 mediaStore.js），帖子记录里只留媒体元信息。
//
// 真实后端接入后：把本文件替换为 api/post.js 里的真实请求即可，
// 组件的调用方式保持不变（api/post.js 已经预留了真实接口调用）。
//
// 数据模型：
// {
//   id, title, content, category, tags: [String],
//   author, authorName, createdAt,
//   media: [{ id?, src?, type: 'image'|'video', name?, size? }],
//   likes: [userKey], comments: [{ id, author, authorName, content, createdAt }]
// }
// =====================================================================

import { reactive } from 'vue'

const STORAGE_KEY = 'mengchong.posts.v1'

export const state = reactive({
  posts: [],
  loaded: false
})

const HOUR = 3600 * 1000

function uid(prefix = 'p') {
  return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

/* ---------------- 种子数据（首次打开时写入，之后以本地数据为准） ---------------- */
function seedPosts() {
  const now = Date.now()
  return [
    {
      id: 'seed-1',
      title: '布偶猫的慵懒午后',
      content:
        '阳光正好，软垫正暖。今天的主角是摊成一张饼的布偶猫「露露」。\n它有个绝活：只要我拿起逗猫棒，它就把肚皮朝天翻过来——意思是「今天不营业，请撸我」。\n养布偶第 3 年，最大的感受是：长毛猫一定要每天梳毛，不然地毯上会开出第二只猫。',
      category: 'cat',
      tags: ['布偶猫', '撸猫日常'],
      author: 'demo',
      authorName: '猫奴小七',
      createdAt: now - 2 * HOUR,
      media: [
        { type: 'image', src: '/images/pets/cat.png', name: '布偶猫' },
        { type: 'image', src: '/images/pets/cat_british.png', name: '英国短毛猫' }
      ],
      likes: ['miao', 'wang'],
      comments: [
        { id: 'c1', author: 'corgi', authorName: '遛狗达人', content: '这也太软了吧！想 rua 一下', createdAt: now - HOUR }
      ]
    },
    {
      id: 'seed-2',
      title: '柯基的小短腿狂奔',
      content:
        '草地 + 柯基 = 快乐加倍。\n今天带「土豆」去公园，它跑起来的样子像一只飞速移动的吐司面包，路过的人都笑出了声。\n提醒各位：柯基腰长腿短，上下楼梯和沙发一定要抱，别让小家伙自己蹦，保护腰椎最重要。',
      category: 'dog',
      tags: ['柯基', '遛狗'],
      author: 'demo2',
      authorName: '遛狗达人',
      createdAt: now - 6 * HOUR,
      media: [{ type: 'image', src: '/images/pets/dog.png', name: '柯基' }],
      likes: ['miao', 'snow', 'hamster'],
      comments: [
        { id: 'c2', author: 'cat7', authorName: '猫奴小七', content: '蜜桃臀太可爱了哈哈哈', createdAt: now - 3 * HOUR }
      ]
    },
    {
      id: 'seed-3',
      title: '仓鼠的腮帮子仓库',
      content:
        '一颗瓜子引发的囤积欲。\n观察记录：小仓鼠「芝麻」在 20 分钟里往返了 14 趟，把腮帮子塞到变形，然后在窝里码得整整齐齐。\n养仓鼠的小贴士：笼子放在安静角落，垫料选无粉尘的，跑轮直径要够大，仓鼠才不容易驼背。',
      category: 'small',
      tags: ['仓鼠', '小宠'],
      author: 'demo3',
      authorName: '仓鼠观察员',
      createdAt: now - 12 * HOUR,
      media: [{ type: 'image', src: '/images/pets/hamster.png', name: '仓鼠' }],
      likes: ['rabbit'],
      comments: []
    },
    {
      id: 'seed-4',
      title: '鹦鹉的模仿秀',
      content:
        '它今天又学会了一句新的口头禅：我家的玄凤开始学我说「吃饭啦」。\n过程：先小声嘀咕，再逐渐放大音量，最后变成整点报时。\n鸟类很需要陪伴，每天固定互动 20 分钟，比放一整天零食更能让它安心。',
      category: 'bird',
      tags: ['鹦鹉', '鸟类'],
      author: 'demo4',
      authorName: '鸟语者',
      createdAt: now - 26 * HOUR,
      media: [
        { type: 'image', src: '/images/pets/bird.png', name: '鹦鹉' },
        { type: 'image', src: '/images/pets/bird_cockatiel.png', name: '玄凤' }
      ],
      likes: ['miao'],
      comments: []
    },
    {
      id: 'seed-5',
      title: '小鱼的泡泡浴',
      content:
        '咕噜咕噜，水里的慢生活。\n换水、晒水、测水温——养鱼最治愈的时刻就是看着它们追着饲料游来游去。\n给新手的话：开缸先养水 7 天，别急着下鱼；鱼少水好，永远比鱼多水浑更省心。',
      category: 'fish',
      tags: ['金鱼', '水族箱'],
      author: 'demo5',
      authorName: '观鱼人',
      createdAt: now - 3 * 24 * HOUR,
      media: [{ type: 'image', src: '/images/pets/fish.png', name: '金鱼' }],
      likes: [],
      comments: []
    },
    {
      id: 'seed-6',
      title: '垂耳兔的干饭时刻',
      content: '咔嚓咔嚓，蔬果是最好的奖赏。\n今日菜单：提摩西草无限量 + 一小块苹果。兔子的牙齿会一直生长，磨牙草不能断哦。',
      category: 'small',
      tags: ['兔子'],
      author: 'demo6',
      authorName: '兔叽',
      createdAt: now - 4 * 24 * HOUR,
      media: [{ type: 'image', src: '/images/pets/small_rabbit.png', name: '兔子' }],
      likes: ['miao', 'corgi'],
      comments: []
    }
  ]
}

/* ---------------- 读写 ---------------- */
function save() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.posts))
  } catch (e) {
    /* 存储不可用（隐私模式等）时仅内存生效 */
  }
}

function load() {
  if (state.loaded) return
  state.loaded = true
  let raw = null
  try {
    raw = localStorage.getItem(STORAGE_KEY)
  } catch (e) {
    raw = null
  }
  if (raw === null) {
    state.posts = seedPosts()
    save()
    return
  }
  try {
    const arr = JSON.parse(raw)
    state.posts = Array.isArray(arr) ? arr : []
  } catch (e) {
    state.posts = seedPosts()
    save()
  }
}

/* ---------------- 查询 ---------------- */
export function listPosts(category = '') {
  load()
  const arr = category ? state.posts.filter((p) => p.category === category) : state.posts
  return [...arr].sort((a, b) => b.createdAt - a.createdAt)
}

export function getPost(id) {
  load()
  return state.posts.find((p) => p.id === id) || null
}

export function searchPosts(keyword) {
  load()
  const q = String(keyword || '').trim().toLowerCase()
  if (!q) return []
  return listPosts().filter((p) => {
    const hay = [p.title, p.content, p.authorName, ...(p.tags || [])].join(' ').toLowerCase()
    return hay.includes(q)
  })
}

/* ---------------- 写入 ---------------- */
/**
 * 发布帖子。payload: { title, content, category, tags, media, author, authorName }
 * 返回新建的帖子对象。
 */
export function createPost(payload) {
  load()
  const post = {
    id: uid('p'),
    title: String((payload.title || '').trim()),
    content: String((payload.content || '').trim()),
    category: payload.category || 'cat',
    tags: (payload.tags || []).map((t) => String(t).trim()).filter(Boolean).slice(0, 6),
    author: payload.author || 'guest',
    authorName: payload.authorName || '萌宠用户',
    createdAt: Date.now(),
    media: payload.media || [],
    likes: [],
    comments: []
  }
  state.posts.unshift(post)
  save()
  return post
}

export function removePost(id) {
  load()
  const idx = state.posts.findIndex((p) => p.id === id)
  if (idx < 0) return false
  state.posts.splice(idx, 1)
  save()
  return true
}

/** 点赞 / 取消点赞；userKey 用当前登录用户的 username，保证一人一赞 */
export function toggleLike(id, userKey) {
  load()
  const post = state.posts.find((p) => p.id === id)
  if (!post) return null
  if (!Array.isArray(post.likes)) post.likes = []
  const key = userKey || 'guest'
  const i = post.likes.indexOf(key)
  if (i >= 0) post.likes.splice(i, 1)
  else post.likes.push(key)
  save()
  return post
}

export function isLiked(post, userKey) {
  return !!post && Array.isArray(post.likes) && post.likes.includes(userKey || 'guest')
}

/** 评论（按时间正序展示） */
export function addComment(id, { author, authorName, content }) {
  load()
  const post = state.posts.find((p) => p.id === id)
  if (!post) return null
  if (!Array.isArray(post.comments)) post.comments = []
  const text = String(content || '').trim()
  if (!text) return null
  post.comments.push({
    id: uid('c'),
    author: author || 'guest',
    authorName: authorName || '萌宠用户',
    content: text.slice(0, 300),
    createdAt: Date.now()
  })
  save()
  return post
}

/* ---------------- 工具 ---------------- */
/** 相对时间：刚刚 / N 分钟前 / N 小时前 / N 天前 / YYYY-MM-DD */
export function formatTime(ts) {
  const t = Number(ts) || 0
  if (!t) return ''
  const diff = Date.now() - t
  if (diff < 60 * 1000) return '刚刚'
  if (diff < HOUR) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 24 * HOUR) return Math.floor(diff / HOUR) + ' 小时前'
  if (diff < 30 * 24 * HOUR) return Math.floor(diff / (24 * HOUR)) + ' 天前'
  const d = new Date(t)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

/** 正文摘要（列表卡片用） */
export function excerpt(text, len = 46) {
  const s = String(text || '').replace(/\s+/g, ' ').trim()
  return s.length > len ? s.slice(0, len) + '…' : s
}

/** 帖子卡片/详情顶部展示的封面媒体（没有媒体时返回 null） */
export function coverMedia(post) {
  if (!post || !Array.isArray(post.media) || !post.media.length) return null
  return post.media[0]
}
