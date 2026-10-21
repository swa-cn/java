// =====================================================================
// mediaStore —— 图片 / 视频二进制文件本地存储（IndexedDB）
//
// 为什么用 IndexedDB 而不是 localStorage：
//  - localStorage 只支持字符串且有 5MB 左右上限，视频/大图塞不进去；
//  - IndexedDB 可以直接存 Blob，容量大得多，适合"发帖带图/带视频"的演示。
//
// 数据模型：{ id, type: 'image'|'video', name, size, blob }
// 页面展示时通过 resolveMediaUrl(id) 拿到 objectURL（带缓存，避免重复创建）。
//
// 真实后端接入后：改为先把文件 POST 到对象存储（multipart/form-data），
// 由服务端返回 URL，帖子只保存 URL，不再需要本模块。
// =====================================================================

const DB_NAME = 'mengchong-media'
const DB_VERSION = 1
const STORE = 'media'

/** 单个文件的体积上限（演示用）：图片 5MB、视频 60MB */
export const MEDIA_LIMITS = {
  image: 5 * 1024 * 1024,
  video: 60 * 1024 * 1024
}

/** 一次发帖可携带的媒体数量上限 */
export const MEDIA_COUNTS = { image: 9, video: 1 }

let dbPromise = null

function openDb() {
  if (dbPromise) return dbPromise
  dbPromise = new Promise((resolve, reject) => {
    if (typeof indexedDB === 'undefined') {
      reject(new Error('当前浏览器不支持 IndexedDB'))
      return
    }
    const req = indexedDB.open(DB_NAME, DB_VERSION)
    req.onupgradeneeded = () => {
      const db = req.result
      if (!db.objectStoreNames.contains(STORE)) {
        db.createObjectStore(STORE, { keyPath: 'id' })
      }
    }
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
  })
  return dbPromise
}

function withStore(mode, fn) {
  return openDb().then(
    (db) =>
      new Promise((resolve, reject) => {
        const tx = db.transaction(STORE, mode)
        const req = fn(tx.objectStore(STORE))
        tx.oncomplete = () => resolve(req && req.result)
        tx.onerror = () => reject(tx.error)
        tx.onabort = () => reject(tx.error)
      })
  )
}

function newId() {
  return 'm' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

/** 判断文件类型：'image' | 'video' | ''（不支持） */
export function mediaKind(file) {
  const t = String((file && file.type) || '')
  if (t.startsWith('image/')) return 'image'
  if (t.startsWith('video/')) return 'video'
  return ''
}

/**
 * 保存一个文件，返回媒体的元信息（不含 blob，便于写入帖子记录）。
 * 返回 { id, type, name, size }
 */
export async function saveMediaFile(file) {
  const type = mediaKind(file)
  if (!type) throw new Error('只支持图片或视频文件')
  const limit = MEDIA_LIMITS[type]
  if (file.size > limit) {
    throw new Error(
      `${file.name} 超过 ${Math.round(limit / 1024 / 1024)}MB 限制`
    )
  }
  const id = newId()
  await withStore('readwrite', (store) =>
    store.put({
      id,
      type,
      name: file.name || (type === 'video' ? '视频' : '图片'),
      size: file.size || 0,
      blob: file
    })
  )
  return { id, type, name: file.name || '', size: file.size || 0 }
}

/** 读取 Blob；找不到（例如换了浏览器）返回 null */
export async function getMediaBlob(id) {
  if (!id) return null
  try {
    const rec = await withStore('readonly', (store) => store.get(id))
    return (rec && rec.blob) || null
  } catch (e) {
    return null
  }
}

/* ---------------- objectURL 缓存 ---------------- */
const urlCache = new Map()

/** 取媒体的可展示地址（objectURL），带缓存 */
export async function resolveMediaUrl(id) {
  if (!id) return ''
  if (urlCache.has(id)) return urlCache.get(id)
  const blob = await getMediaBlob(id)
  if (!blob) return ''
  const url = URL.createObjectURL(blob)
  urlCache.set(id, url)
  return url
}

/** 删除媒体（同时释放缓存的 objectURL） */
export async function deleteMedia(id) {
  if (!id) return
  const cached = urlCache.get(id)
  if (cached) {
    URL.revokeObjectURL(cached)
    urlCache.delete(id)
  }
  try {
    await withStore('readwrite', (store) => store.delete(id))
  } catch (e) {
    /* 忽略 */
  }
}

/**
 * 统一把「文件 + 已有 media 记录」解析成帖子里的媒体数组。
 * 说明：帖子记录里只存元信息 { id, type, name, size } 或静态 src，
 * 二进制始终留在 IndexedDB，避免 localStorage 被塞爆。
 */
export async function saveFiles(files) {
  const out = []
  for (const f of files || []) {
    out.push(await saveMediaFile(f))
  }
  return out
}
