// =====================================================================
// api/post.js —— 「发现」帖子接口层
//
// 与 api/auth.js、api/home.js 保持同样的双模式设计：
//  1) USE_MOCK = true（默认，本地演示）：直接读写本地 postStore（localStorage
//     + IndexedDB 存图片/视频），页面刷新数据仍在，可完整演示发帖流程。
//  2) USE_MOCK = false（对接真实后端）：走下面的接口约定。
//
// 接口约定（待后端实现）：
//  GET  /content/posts?category=<id>
//       -> { ok, items: [Post] }
//  GET  /content/posts/{id}
//       -> { ok, post: Post }
//  POST /content/posts
//       Content-Type: multipart/form-data
//       fields: title, content, category, tags(逗号分隔), files(可多个)
//       -> { ok, post: Post }（媒体为服务端返回的 URL，不再需要 IndexedDB）
//  POST /content/posts/{id}/like          -> { ok, likes: [userKey] }
//  POST /content/posts/{id}/comments      { content } -> { ok, comment: Comment }
//  DELETE /content/posts/{id}             -> { ok }
//
// Post: { id, title, content, category, tags[], author, authorName, createdAt,
//         media: [{ type:'image'|'video', src?, id? }], likes[], comments[] }
//
// 安全说明：所有文本以纯文本渲染（Vue 插值，天然转义），不拼接 HTML；
// 媒体上传在真实环境必须做服务端类型/体积校验与病毒扫描。
// =====================================================================

import * as store from '../composables/postStore'
import { saveFiles, deleteMedia } from '../composables/mediaStore'
import { reactive } from 'vue'

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
const USE_MOCK =
  import.meta.env.VITE_USE_MOCK === undefined
    ? true
    : String(import.meta.env.VITE_USE_MOCK) !== 'false'

/**
 * 帖子功能的运行态（响应式，供 UI 显示提示）：
 * - localMode = true   ：帖子走本地实现（localStorage + IndexedDB）
 * - localMode = false  ：帖子走后端接口
 * 说明：项目 .env 里默认关闭 mock（VITE_USE_MOCK=false）走真实后端，但后端目前
 * 只实现了认证接口、没有 /content/posts。因此当接口不可用（未实现 404/500 或服务
 * 未启动）时，本层会自动回退到本地实现，保证「发现 / 发帖」在两种环境下都能用。
 */
export const postRuntime = reactive({ localMode: USE_MOCK, reason: '' })

/** 只有「接口不存在 / 服务不可用」才回退；业务错误（400/401/403/409/413/422）照常抛出 */
const FALLBACK_CODES = [404, 405, 500, 501, 502, 503, 504]
function shouldFallback(e) {
  // 网络层错误（后端没启动时 fetch 直接抛 TypeError）
  if (e instanceof TypeError) return true
  return FALLBACK_CODES.includes(Number(e && e.code))
}

/** 带本地回退的远程调用：后端就绪时用接口，未就绪时用本地，并标记运行态 */
async function withFallback(remoteFn, localFn) {
  if (USE_MOCK) return localFn()
  try {
    return await remoteFn()
  } catch (e) {
    if (!shouldFallback(e)) throw e
    postRuntime.localMode = true
    postRuntime.reason = '后端帖子接口尚未就绪，已自动切换为本地演示模式'
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
    // code 必须是数字状态码：回退判断依赖它（data.error 可能是 "Not Found" 这类字符串）
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

async function httpPost(path, body, isForm) {
  const opts = { method: 'POST', credentials: 'include' }
  if (isForm) {
    opts.body = body
  } else {
    opts.headers = { 'Content-Type': 'application/json' }
    opts.body = JSON.stringify(body || {})
  }
  const res = await fetch(API_BASE + path, opts)
  return parseBody(res)
}

/* =====================================================================
 * 帖子列表 / 详情
 * ===================================================================== */

/**
 * 帖子列表。
 * category：按萌宠分类筛选（空 = 全部）
 * author  ：按作者用户名筛选（用于「我的」页展示本账号的发帖记录）
 * 说明：后端 /content/posts 目前只认 category 参数，因此 author 在前端本地过滤，
 * 后端后续若支持 author 查询参数，只需把 author 拼进 query 即可无缝切换。
 */
export async function fetchPosts({ category = '', author = '' } = {}) {
  const res = await withFallback(
    () =>
      httpGet('/content/posts' + (category ? '?category=' + encodeURIComponent(category) : '')),
    () => ({ ok: true, items: store.listPosts(category) })
  )
  if (!author) return res
  const items = ((res && res.items) || []).filter((p) => p && p.author === author)
  return { ...res, items }
}

export async function fetchPostDetail(id) {
  return withFallback(
    () => httpGet('/content/posts/' + encodeURIComponent(id)),
    () => ({ ok: true, post: store.getPost(id) })
  )
}

/* =====================================================================
 * 发帖：文字 + 图片 + 视频
 * ===================================================================== */

/**
 * 发帖。
 * payload: { title, content, category, tags: string[]|string, files: File[], author, authorName }
 * mock 模式：把 files 存入 IndexedDB，帖子记录里只保留媒体元信息。
 * 真实模式：打包为 multipart/form-data，由服务端落对象存储并返回 URL。
 */
export async function publishPost(payload) {
  const files = payload.files || []
  const localPublish = async () => {
    const media = await saveFiles(files)
    const post = store.createPost({ ...payload, media })
    return { ok: true, post, local: true }
  }
  return withFallback(() => {
    const form = new FormData()
    form.append('title', payload.title || '')
    form.append('content', payload.content || '')
    form.append('category', payload.category || '')
    form.append('tags', Array.isArray(payload.tags) ? payload.tags.join(',') : payload.tags || '')
    form.append('author', payload.author || 'guest')
    form.append('authorName', payload.authorName || '')
    files.forEach((f) => form.append('files', f))
    return httpPost('/content/posts', form, true)
  }, localPublish)
}

export async function deletePost(id, author) {
  const localDelete = async () => {
    const post = store.getPost(id)
    // 顺带清理该帖在 IndexedDB 里的媒体（静态示例图没有 id，跳过）
    if (post && Array.isArray(post.media)) {
      await Promise.all(
        post.media.filter((m) => m && m.id).map((m) => deleteMedia(m.id))
      )
    }
    return { ok: store.removePost(id) }
  }
  return withFallback(async () => {
    const qs = author ? '?author=' + encodeURIComponent(author) : ''
    const res = await fetch(API_BASE + '/content/posts/' + encodeURIComponent(id) + qs, {
      method: 'DELETE',
      credentials: 'include'
    })
    return parseBody(res)
  }, localDelete)
}

/* =====================================================================
 * 点赞 / 评论
 * ===================================================================== */

export async function likePost(id, userKey) {
  return withFallback(
    () =>
      httpPost(
        `/content/posts/${encodeURIComponent(id)}/like?userKey=` + encodeURIComponent(userKey || ''),
        {}
      ),
    () => ({ ok: true, post: store.toggleLike(id, userKey) })
  )
}

export async function commentPost(id, content, author) {
  return withFallback(
    () =>
      httpPost(`/content/posts/${encodeURIComponent(id)}/comments`, {
        content,
        author: author && author.author,
        authorName: author && author.authorName
      }),
    () => ({ ok: true, post: store.addComment(id, { ...author, content }) })
  )
}

export const postApiConfig = { API_BASE, USE_MOCK, isLocalMode: () => postRuntime.localMode }
