// =====================================================================
// api/admin.js —— 后台管理系统接口
//
// 对接后端 AdminController（/api/admin/*），仅真实后端模式（不做 mock：
// 后台管理的是数据库真实数据，本地 mock 没有意义）。
//
// 登录：POST /admin/login { account, password }
//   成功 200 { ok, user:{ username, nickname } }，会话用 Cookie（JSESSIONID）
//   失败 401 { error:'notfound'|'wrongpw' } / 403 { error:'forbidden' }（非管理员）
//
// 分类（pet_category 表）：
//   GET    /admin/categories            -> { ok, items:[{id,name,icon,color,image,sortNo,breedCount}] }
//   POST   /admin/categories            新增 { id, name, icon, color, image, sortNo }
//   PUT    /admin/categories/{id}       修改属性
//   DELETE /admin/categories/{id}       删除（连带其下品种）
//
// 品种（pet_breed 表）：
//   GET    /admin/breeds?categoryId=xx  -> { ok, items:[{id,categoryId,name,image,description,sortNo}] }
//   POST   /admin/breeds                新增 { categoryId, name, image, description, sortNo }
//   PUT    /admin/breeds/{id}           修改
//   DELETE /admin/breeds/{id}           删除
//
// 修改成功后，前端调用 petData.js 的 loadCategories() 让首页立即生效。
// =====================================================================

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')

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
    err.status = res.status
    throw err
  }
  return data
}

async function http(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    credentials: 'include', // 携带 JSESSIONID 会话 Cookie
    headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    ...options
  })
  return parseBody(res)
}

/* ---------------- 登录态 ---------------- */

export function adminLogin(account, password) {
  return http('/admin/login', {
    method: 'POST',
    body: JSON.stringify({ account, password })
  })
}

export function adminMe() {
  return http('/admin/me')
}

export function adminLogout() {
  return http('/admin/logout', { method: 'POST' })
}

/* ---------------- 分类管理 ---------------- */

export function adminListCategories() {
  return http('/admin/categories')
}

export function adminCreateCategory(payload) {
  return http('/admin/categories', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function adminUpdateCategory(id, patch) {
  return http('/admin/categories/' + encodeURIComponent(id), {
    method: 'PUT',
    body: JSON.stringify(patch)
  })
}

export function adminDeleteCategory(id) {
  return http('/admin/categories/' + encodeURIComponent(id), { method: 'DELETE' })
}

/* ---------------- 品种管理 ---------------- */

export function adminListBreeds(categoryId) {
  return http('/admin/breeds?categoryId=' + encodeURIComponent(categoryId))
}

export function adminCreateBreed(payload) {
  return http('/admin/breeds', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function adminUpdateBreed(id, patch) {
  return http('/admin/breeds/' + encodeURIComponent(id), {
    method: 'PUT',
    body: JSON.stringify(patch)
  })
}

export function adminDeleteBreed(id) {
  return http('/admin/breeds/' + encodeURIComponent(id), { method: 'DELETE' })
}

/* ---------------- 用户管理 ---------------- */
// 权限模型：admin 账号（后端配置 admin.username）为超级管理员，
// 只有它能新增/提升/删除「管理员」；其他管理员只能管理普通用户。

export function adminListUsers(keyword = '') {
  const q = keyword ? '?keyword=' + encodeURIComponent(keyword) : ''
  return http('/admin/users' + q)
}

/** 新增用户/管理员 { username, nickname, password, role }（role: 0=管理员, 1=普通） */
export function adminCreateUser(payload) {
  return http('/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

/** 修改用户（部分字段）{ nickname?, password?, role? } */
export function adminUpdateUser(id, patch) {
  return http('/admin/users/' + encodeURIComponent(id), {
    method: 'PUT',
    body: JSON.stringify(patch)
  })
}

export function adminDeleteUser(id) {
  return http('/admin/users/' + encodeURIComponent(id), { method: 'DELETE' })
}

/* ---------------- 知识课堂管理 ---------------- */
// pet_knowledge 表：文章标题/摘要/标签/正文小节。
// 新增/编辑后前台「知识课堂」页下次进入即可看到（列表每次进页面都重新拉取）。

export function adminListKnowledge(keyword = '') {
  const q = keyword ? '?keyword=' + encodeURIComponent(keyword) : ''
  return http('/admin/knowledge' + q)
}

/** 新增文章 { cat, title, summary, tags:[]|'a,b', sections:[{h,p}] } */
export function adminCreateKnowledge(payload) {
  return http('/admin/knowledge', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

/** 修改文章（部分字段） */
export function adminUpdateKnowledge(id, patch) {
  return http('/admin/knowledge/' + encodeURIComponent(id), {
    method: 'PUT',
    body: JSON.stringify(patch)
  })
}

export function adminDeleteKnowledge(id) {
  return http('/admin/knowledge/' + encodeURIComponent(id), { method: 'DELETE' })
}
