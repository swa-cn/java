<template>
  <div class="admin">
    <!-- 顶栏 -->
    <header class="admin-top">
      <div class="admin-brand">
        <span class="admin-badge">⚙️</span>
        <div>
          <h1 class="admin-title">后台管理系统</h1>
          <p class="admin-sub">萌宠分类数据管理 · 管理员：{{ adminName }}</p>
        </div>
      </div>
      <div class="admin-actions">
        <button type="button" class="top-btn ghost" @click="$emit('exit')">← 回到首页</button>
        <button type="button" class="top-btn" @click="onLogout">退出登录</button>
      </div>
    </header>

    <!-- 页签 -->
    <nav class="admin-tabs">
      <button
        type="button"
        class="tab"
        :class="{ active: tab === 'category' }"
        @click="switchTab('category')"
      >分类管理（{{ categories.length }}）</button>
      <button
        type="button"
        class="tab"
        :class="{ active: tab === 'breed' }"
        @click="switchTab('breed')"
      >品种管理（{{ breeds.length }}）</button>
      <button
        type="button"
        class="tab"
        :class="{ active: tab === 'user' }"
        @click="switchTab('user')"
      >用户管理（{{ users.length }}）</button>
      <button
        type="button"
        class="tab"
        :class="{ active: tab === 'knowledge' }"
        @click="switchTab('knowledge')"
      >知识管理（{{ knowledges.length }}）</button>
    </nav>

    <!-- ============ 分类管理 ============ -->
    <section v-if="tab === 'category'" class="panel">
      <div class="panel-bar">
        <p class="panel-tip">修改分类的名称、图标、颜色、封面、排序，前台首页立即生效</p>
        <button type="button" class="add-btn" @click="openCategoryForm(null)">＋ 新增分类</button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>排序</th>
            <th>封面</th>
            <th>ID</th>
            <th>名称</th>
            <th>图标</th>
            <th>主题色</th>
            <th>品种数</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in categories" :key="c.id">
            <td class="mono">{{ c.sortNo }}</td>
            <td>
              <img class="thumb" :src="c.image" :alt="c.name" loading="lazy" />
            </td>
            <td class="mono">{{ c.id }}</td>
            <td class="strong">{{ c.name }}</td>
            <td><span class="icon-chip" :style="{ background: c.color }">{{ c.icon }}</span></td>
            <td>
              <span class="color-chip" :style="{ background: c.color }"></span>
              <span class="mono">{{ c.color }}</span>
            </td>
            <td>{{ c.breedCount }}</td>
            <td class="ops">
              <button type="button" class="row-btn primary" @click="manageBreeds(c)">品种</button>
              <button type="button" class="row-btn" @click="openCategoryForm(c)">编辑</button>
              <button type="button" class="row-btn danger" @click="removeCategory(c)">删除</button>
            </td>
          </tr>
          <tr v-if="!categories.length && !loadingCats">
            <td colspan="8" class="empty-cell">暂无分类，点右上角「新增分类」创建</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- ============ 品种管理 ============ -->
    <section v-else-if="tab === 'breed'" class="panel">
      <div class="panel-bar">
        <label class="cat-select">
          <span>所属分类</span>
          <select v-model="breedCatId" class="select" @change="loadBreeds">
            <option v-for="c in categories" :key="c.id" :value="c.id">
              {{ c.icon }} {{ c.name }}（{{ c.breedCount }} 个品种）
            </option>
          </select>
        </label>
        <button type="button" class="add-btn" @click="openBreedForm(null)">＋ 新增品种</button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>排序</th>
            <th>图片</th>
            <th>品种名</th>
            <th>简介</th>
            <th>性格</th>
            <th>难度</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in breeds" :key="b.id">
            <td class="mono">{{ b.sortNo }}</td>
            <td><img class="thumb" :src="b.image" :alt="b.name" loading="lazy" /></td>
            <td class="strong">{{ b.name }}</td>
            <td class="desc-cell">{{ b.description || '—' }}</td>
            <td class="desc-cell small">{{ b.temperament || '—' }}</td>
            <td>
              <span v-if="b.careLevel" class="level-chip" :data-level="b.careLevel">{{ b.careLevel }}</span>
              <span v-else class="dim">—</span>
            </td>
            <td class="ops">
              <button type="button" class="row-btn" @click="openBreedForm(b)">编辑</button>
              <button type="button" class="row-btn danger" @click="removeBreed(b)">删除</button>
            </td>
          </tr>
          <tr v-if="!breeds.length && !loadingBreeds">
            <td colspan="7" class="empty-cell">该分类暂无品种，点右上角「新增品种」添加</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- ============ 用户管理 ============ -->
    <section v-else-if="tab === 'user'" class="panel">
      <div class="panel-bar">
        <label class="cat-select">
          <span>搜索</span>
          <input
            v-model="userKeyword"
            class="user-search"
            type="text"
            maxlength="40"
            placeholder="用户名 / 昵称，回车搜索"
            @keyup.enter="loadUsers"
          />
        </label>
        <button type="button" class="add-btn" @click="openUserForm(null)">
          ＋ 新增{{ isSuper ? '用户 / 管理员' : '用户' }}
        </button>
      </div>

      <p v-if="!isSuper" class="user-perm-tip">
        当前账号不是超级管理员：可以新增、编辑普通用户并重置密码；
        只有 admin 账号才能设置管理员权限（数据库直接改 role 字段的既有用法不受影响）。
      </p>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>注册时间</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td class="mono">{{ u.id }}</td>
            <td class="strong">{{ u.username }}</td>
            <td>{{ u.nickname || '—' }}</td>
            <td>
              <span class="role-chip" :class="{ admin: u.admin && !u.superAdmin, super: u.superAdmin }">
                {{ u.superAdmin ? '超级管理员' : (u.admin ? '管理员' : '普通用户') }}
              </span>
            </td>
            <td class="mono">{{ formatTime(u.createdAt) }}</td>
            <td class="ops">
              <button type="button" class="row-btn" @click="openUserForm(u)">编辑</button>
              <button
                v-if="!u.superAdmin"
                type="button"
                class="row-btn danger"
                @click="removeUser(u)"
              >删除</button>
            </td>
          </tr>
          <tr v-if="!users.length && !loadingUsers">
            <td colspan="6" class="empty-cell">
              {{ loadingUsers ? '加载中…' : '没有匹配的用户' }}
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- ============ 知识管理 ============ -->
    <section v-else-if="tab === 'knowledge'" class="panel">
      <div class="panel-bar">
        <label class="cat-select">
          <span>搜索</span>
          <input
            v-model="knowledgeKeyword"
            class="user-search"
            type="text"
            maxlength="40"
            placeholder="标题 / 摘要 / 标签 / 正文，回车搜索"
            @keyup.enter="loadKnowledges"
          />
        </label>
        <button type="button" class="add-btn" @click="openKnowledgeForm(null)">＋ 新增文章</button>
      </div>

      <p class="user-perm-tip">
        这里的文章会显示在首页导航栏「知识课堂」里，读者每打开一次详情，浏览数 +1。
      </p>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>分类</th>
            <th>标签</th>
            <th>浏览</th>
            <th>更新时间</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="k in knowledges" :key="k.id">
            <td class="mono">{{ k.id }}</td>
            <td class="strong" :title="k.summary || ''">{{ k.title }}</td>
            <td>
              <span class="role-chip" :class="{ admin: k.cat !== 'all' }">
                {{ catNameOfId(k.cat) }}
              </span>
            </td>
            <td class="kn-tags-cell">{{ (k.tags || []).join('、') || '—' }}</td>
            <td class="mono">{{ k.views || 0 }}</td>
            <td class="mono">{{ formatTime(k.updatedAt || k.createdAt) }}</td>
            <td class="ops">
              <button type="button" class="row-btn" @click="openKnowledgeForm(k)">编辑</button>
              <button type="button" class="row-btn danger" @click="removeKnowledge(k)">删除</button>
            </td>
          </tr>
          <tr v-if="!knowledges.length && !loadingKnowledges">
            <td colspan="7" class="empty-cell">没有匹配的文章，点右上角「新增文章」添加</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- ============ 弹层表单（分类 / 品种 / 用户 / 知识共用外壳） ============ -->
    <transition name="fade">
      <div v-if="modal" class="modal-mask" @click.self="modal = null">
        <div class="modal">
          <h2 class="modal-title">{{ modal.title }}</h2>

          <!-- 分类表单 -->
          <form v-if="modal.type === 'category'" class="modal-form" @submit.prevent="saveCategory">
            <label v-if="!modal.editing" class="field">
              <span class="field-label">分类 ID（英文小写，如 hamster，创建后不可改）</span>
              <input v-model.trim="catForm.id" class="field-input mono" type="text" placeholder="hamster" required />
            </label>
            <label class="field">
              <span class="field-label">分类名称</span>
              <input v-model.trim="catForm.name" class="field-input" type="text" placeholder="如：小宠" required />
            </label>
            <div class="field-row">
              <label class="field">
                <span class="field-label">单字图标</span>
                <input v-model.trim="catForm.icon" class="field-input" type="text" maxlength="2" placeholder="如：啮" />
              </label>
              <label class="field">
                <span class="field-label">主题色</span>
                <input v-model="catForm.color" class="field-input color-input" type="color" />
              </label>
              <label class="field">
                <span class="field-label">排序（小的在前）</span>
                <input v-model.number="catForm.sortNo" class="field-input" type="number" min="0" />
              </label>
            </div>
            <label class="field">
              <span class="field-label">封面图地址（可选：填 /images/pets/ 下的图片路径）</span>
              <input v-model.trim="catForm.image" class="field-input mono" type="text" list="pet-images" placeholder="/images/pets/hamster.png" />
            </label>

            <p v-if="modalError" class="form-error">{{ modalError }}</p>
            <div class="modal-ops">
              <button type="button" class="op-btn ghost" @click="modal = null">取消</button>
              <button type="submit" class="op-btn" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
            </div>
          </form>

          <!-- 品种表单 -->
          <form v-else-if="modal.type === 'breed'" class="modal-form" @submit.prevent="saveBreed">
            <label class="field">
              <span class="field-label">所属分类</span>
              <select v-model="breedForm.categoryId" class="select" :disabled="modal.editing">
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
              </select>
            </label>
            <label class="field">
              <span class="field-label">品种名称</span>
              <input v-model.trim="breedForm.name" class="field-input" type="text" placeholder="如：布偶猫" required />
            </label>
            <label class="field">
              <span class="field-label">品种简介</span>
              <textarea v-model.trim="breedForm.description" class="field-input textarea" rows="2" maxlength="200" placeholder="一句话介绍这个品种"></textarea>
            </label>
            <div class="field-row">
              <label class="field">
                <span class="field-label">性格</span>
                <input v-model.trim="breedForm.temperament" class="field-input" type="text" maxlength="100" placeholder="如：温顺黏人" />
              </label>
              <label class="field">
                <span class="field-label">饲养难度</span>
                <select v-model="breedForm.careLevel" class="select">
                  <option value="">未设置</option>
                  <option value="低">低</option>
                  <option value="中">中</option>
                  <option value="高">高</option>
                </select>
              </label>
              <label class="field">
                <span class="field-label">适合人群</span>
                <input v-model.trim="breedForm.suitableFor" class="field-input" type="text" maxlength="100" placeholder="如：新手友好" />
              </label>
            </div>
            <div class="field-row">
              <label class="field grow">
                <span class="field-label">品种图片（可选：填 /images/pets/ 下的图片路径）</span>
                <input v-model.trim="breedForm.image" class="field-input mono" type="text" list="pet-images" placeholder="/images/pets/cat.png" />
              </label>
              <label class="field">
                <span class="field-label">排序</span>
                <input v-model.number="breedForm.sortNo" class="field-input" type="number" min="0" />
              </label>
            </div>

            <p v-if="modalError" class="form-error">{{ modalError }}</p>
            <div class="modal-ops">
              <button type="button" class="op-btn ghost" @click="modal = null">取消</button>
              <button type="submit" class="op-btn" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
            </div>
          </form>

          <!-- 用户表单 -->
          <form v-else-if="modal.type === 'user'" class="modal-form" @submit.prevent="saveUser">
            <label v-if="!modal.editing" class="field">
              <span class="field-label">用户名（3-20 位字母 / 数字 / 下划线，创建后不可改）</span>
              <input v-model.trim="userForm.username" class="field-input mono" type="text" placeholder="如：xiaoming01" required />
            </label>
            <label v-else class="field">
              <span class="field-label">用户名（不可修改）</span>
              <input class="field-input mono" type="text" :value="userForm.username" disabled />
            </label>
            <label class="field">
              <span class="field-label">昵称</span>
              <input v-model.trim="userForm.nickname" class="field-input" type="text" maxlength="24" placeholder="选填" />
            </label>
            <label class="field">
              <span class="field-label">
                {{ modal.editing ? '重置密码（不填则不修改）' : '密码（至少 8 位，含字母 / 数字 / 符号至少 2 种）' }}
              </span>
              <input
                v-model="userForm.password"
                class="field-input"
                type="text"
                maxlength="128"
                :required="!modal.editing"
                :placeholder="modal.editing ? '留空表示不修改密码' : '如：pet123456'"
              />
            </label>
            <label class="field">
              <span class="field-label">角色</span>
              <select v-model="userForm.role" class="select" :disabled="!canSetRole">
                <option :value="1">普通用户</option>
                <option v-if="isSuper" :value="0">管理员（可登录后台）</option>
              </select>
              <span v-if="!canSetRole" class="field-hint">
                {{ modal.target && modal.target.superAdmin
                    ? '超级管理员账号（admin）的权限不可修改'
                    : '只有超级管理员（admin 账号）才能设置管理员权限' }}
              </span>
            </label>

            <p v-if="modalError" class="form-error">{{ modalError }}</p>
            <div class="modal-ops">
              <button type="button" class="op-btn ghost" @click="modal = null">取消</button>
              <button type="submit" class="op-btn" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
            </div>
          </form>

          <!-- 知识文章表单 -->
          <form v-else class="modal-form modal-form-wide" @submit.prevent="saveKnowledge">
            <div class="field-row">
              <label class="field grow">
                <span class="field-label">文章标题</span>
                <input v-model.trim="knowledgeForm.title" class="field-input" type="text" maxlength="80" placeholder="如：新手养猫入门清单" required />
              </label>
              <label class="field">
                <span class="field-label">所属分类</span>
                <select v-model="knowledgeForm.cat" class="select">
                  <option value="all">通用</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
                </select>
              </label>
            </div>

            <label class="field">
              <span class="field-label">摘要（显示在卡片上，一句话讲清这篇讲什么）</span>
              <input v-model.trim="knowledgeForm.summary" class="field-input" type="text" maxlength="200" placeholder="如：接猫回家前该准备什么，一篇说清。" />
            </label>

            <label class="field">
              <span class="field-label">标签（逗号或顿号分隔）</span>
              <input v-model.trim="knowledgeForm.tagsText" class="field-input" type="text" maxlength="120" placeholder="如：新手、准备、用品" />
            </label>

            <div class="field sections-field">
              <span class="field-label">正文小节（每行一段，标题 + 正文）</span>
              <div v-for="(s, i) in knowledgeForm.sections" :key="i" class="section-row">
                <input v-model.trim="s.h" class="field-input" type="text" maxlength="60" placeholder="小节标题，如：到家第一周" />
                <textarea v-model.trim="s.p" class="field-input textarea" rows="3" placeholder="小节正文…"></textarea>
                <button
                  type="button"
                  class="row-btn danger"
                  :disabled="knowledgeForm.sections.length <= 1"
                  @click="knowledgeForm.sections.splice(i, 1)"
                >删除</button>
              </div>
              <button type="button" class="op-btn ghost small" @click="knowledgeForm.sections.push({ h: '', p: '' })">
                ＋ 增加小节
              </button>
            </div>

            <p v-if="modalError" class="form-error">{{ modalError }}</p>
            <div class="modal-ops">
              <button type="button" class="op-btn ghost" @click="modal = null">取消</button>
              <button type="submit" class="op-btn" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </transition>

    <!-- 可选图片路径提示（datalist） -->
    <datalist id="pet-images">
      <option v-for="p in petImagePresets" :key="p" :value="p" />
    </datalist>

    <!-- 轻提示 -->
    <transition name="fade">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  adminListCategories, adminCreateCategory, adminUpdateCategory, adminDeleteCategory,
  adminListBreeds, adminCreateBreed, adminUpdateBreed, adminDeleteBreed, adminLogout,
  adminListUsers, adminCreateUser, adminUpdateUser, adminDeleteUser,
  adminListKnowledge, adminCreateKnowledge, adminUpdateKnowledge, adminDeleteKnowledge
} from '../api/admin'
import { loadCategories } from '../composables/petData'

const props = defineProps({
  admin: { type: Object, default: null }
})
const emit = defineEmits(['exit'])

const adminName = props.admin?.nickname || props.admin?.username || '管理员'
// 超级管理员：后端配置 admin.username（默认 admin）对应的账号，权限最大。
// 界面上据此显示「设置管理员」入口；真正的权限校验在后端接口里强制执行。
const isSuper = (props.admin?.username || '').toLowerCase() === 'admin'

/* ---------------- 页签 ---------------- */
const tab = ref('category')
function switchTab(t) {
  tab.value = t
  if (t === 'breed' && !breedCatId.value && categories.value.length) {
    breedCatId.value = categories.value[0].id
    loadBreeds()
  }
  if (t === 'user') loadUsers()
  if (t === 'knowledge') loadKnowledges()
}

/** 分类行点「品种」：直接切到品种管理并定位到该分类 */
function manageBreeds(c) {
  breedCatId.value = c.id
  tab.value = 'breed'
  loadBreeds()
}

/* ---------------- 数据加载 ---------------- */
const categories = ref([])
const breeds = ref([])
const loadingCats = ref(false)
const loadingBreeds = ref(false)
const breedCatId = ref('')

async function loadCategoriesAdmin() {
  loadingCats.value = true
  try {
    const data = await adminListCategories()
    categories.value = data.items || []
    if (!breedCatId.value && categories.value.length) {
      breedCatId.value = categories.value[0].id
    }
  } catch (e) {
    handleAuthError(e)
  } finally {
    loadingCats.value = false
  }
}

async function loadBreeds() {
  if (!breedCatId.value) return
  loadingBreeds.value = true
  try {
    const data = await adminListBreeds(breedCatId.value)
    breeds.value = data.items || []
  } catch (e) {
    handleAuthError(e)
  } finally {
    loadingBreeds.value = false
  }
}

/** 保存成功后同步前台首页：petData 重新拉接口，首页分类/品种原地刷新 */
function syncHome() {
  loadCategories().catch(() => {})
}

/* ---------------- 会话失效统一处理 ---------------- */
function handleAuthError(e) {
  if (e && e.status === 401) {
    showToast('登录已过期，请重新登录')
    emit('exit')
  } else {
    showToast(e.message || '请求失败')
  }
}

/* ---------------- 分类：新增 / 编辑 / 删除 ---------------- */
const modal = ref(null)
const modalError = ref('')
const saving = ref(false)

const EMPTY_CAT = { id: '', name: '', icon: '萌', color: '#ff6fa8', image: '', sortNo: 99 }
const catForm = ref({ ...EMPTY_CAT })

function openCategoryForm(c) {
  modalError.value = ''
  if (c) {
    catForm.value = {
      id: c.id, name: c.name, icon: c.icon || '', color: c.color || '#ff6fa8',
      image: c.image || '', sortNo: c.sortNo ?? 99
    }
    modal.value = { type: 'category', editing: true, title: '编辑分类：' + c.name, target: c }
  } else {
    catForm.value = { ...EMPTY_CAT, sortNo: (categories.value.at(-1)?.sortNo ?? 0) + 1 }
    modal.value = { type: 'category', editing: false, title: '新增分类' }
  }
}

async function saveCategory() {
  modalError.value = ''
  saving.value = true
  try {
    if (modal.value.editing) {
      const { id, ...patch } = catForm.value
      await adminUpdateCategory(id, patch)
      showToast('分类已更新')
    } else {
      await adminCreateCategory({ ...catForm.value })
      showToast('分类已创建')
    }
    modal.value = null
    await loadCategoriesAdmin()
    syncHome()
  } catch (e) {
    modalError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeCategory(c) {
  if (!window.confirm(`确定删除分类「${c.name}」吗？\n其下 ${c.breedCount} 个品种会一并删除，且不可恢复！`)) return
  try {
    await adminDeleteCategory(c.id)
    showToast('分类已删除')
    await loadCategoriesAdmin()
    if (breedCatId.value === c.id) {
      breedCatId.value = categories.value[0]?.id || ''
      breeds.value = []
      if (tab.value === 'breed' && breedCatId.value) loadBreeds()
    }
    syncHome()
  } catch (e) {
    handleAuthError(e)
  }
}

/* ---------------- 品种：新增 / 编辑 / 删除 ---------------- */
const EMPTY_BREED = { categoryId: '', name: '', image: '', description: '', temperament: '', careLevel: '', suitableFor: '', sortNo: 99 }
const breedForm = ref({ ...EMPTY_BREED })

function openBreedForm(b) {
  modalError.value = ''
  if (!categories.value.length) {
    showToast('请先创建分类')
    return
  }
  if (!breedCatId.value) breedCatId.value = categories.value[0].id
  if (b) {
    breedForm.value = {
      categoryId: b.categoryId, name: b.name, image: b.image || '',
      description: b.description || '', temperament: b.temperament || '',
      careLevel: b.careLevel || '', suitableFor: b.suitableFor || '',
      sortNo: b.sortNo ?? 99
    }
    modal.value = { type: 'breed', editing: true, title: '编辑品种：' + b.name, target: b }
  } else {
    breedForm.value = { ...EMPTY_BREED, categoryId: breedCatId.value, sortNo: (breeds.value.at(-1)?.sortNo ?? 0) + 1 }
    modal.value = { type: 'breed', editing: false, title: '新增品种' }
  }
}

async function saveBreed() {
  modalError.value = ''
  saving.value = true
  try {
    if (modal.value.editing) {
      const { id } = modal.value.target
      await adminUpdateBreed(id, { ...breedForm.value })
      showToast('品种已更新')
    } else {
      await adminCreateBreed({ ...breedForm.value })
      showToast('品种已新增')
    }
    modal.value = null
    await loadBreeds()
    await loadCategoriesAdmin()
    syncHome()
  } catch (e) {
    modalError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeBreed(b) {
  if (!window.confirm(`确定删除品种「${b.name}」吗？`)) return
  try {
    await adminDeleteBreed(b.id)
    showToast('品种已删除')
    await loadBreeds()
    await loadCategoriesAdmin()
    syncHome()
  } catch (e) {
    handleAuthError(e)
  }
}

/* ---------------- 用户：列表 / 新增 / 编辑 / 删除 ---------------- */
const users = ref([])
const loadingUsers = ref(false)
const userKeyword = ref('')

async function loadUsers() {
  loadingUsers.value = true
  try {
    const data = await adminListUsers(userKeyword.value.trim())
    users.value = data.items || []
  } catch (e) {
    handleAuthError(e)
  } finally {
    loadingUsers.value = false
  }
}

const EMPTY_USER = { username: '', nickname: '', password: '', role: 1 }
const userForm = ref({ ...EMPTY_USER })

/** 是否允许修改角色下拉：超级管理员才可设置管理员，且 admin 账号自身权限锁定 */
const canSetRole = computed(() =>
  isSuper && !(modal.value?.target && modal.value.target.superAdmin)
)

function openUserForm(u) {
  modalError.value = ''
  if (u) {
    userForm.value = {
      username: u.username,
      nickname: u.nickname || '',
      password: '',
      role: u.admin ? 0 : 1
    }
    modal.value = { type: 'user', editing: true, title: '编辑用户：' + u.username, target: u }
  } else {
    userForm.value = { ...EMPTY_USER }
    modal.value = {
      type: 'user', editing: false, target: null,
      title: isSuper ? '新增用户 / 管理员' : '新增用户'
    }
  }
}

async function saveUser() {
  modalError.value = ''
  saving.value = true
  try {
    if (modal.value.editing) {
      const patch = { nickname: userForm.value.nickname, role: userForm.value.role }
      if (userForm.value.password) patch.password = userForm.value.password
      await adminUpdateUser(modal.value.target.id, patch)
      showToast('用户已更新')
    } else {
      await adminCreateUser({ ...userForm.value })
      showToast('用户已创建')
    }
    modal.value = null
    await loadUsers()
  } catch (e) {
    modalError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeUser(u) {
  const roleName = u.admin ? '管理员' : '用户'
  if (!window.confirm(`确定删除${roleName}「${u.username}」吗？该账号将无法登录，此操作不可恢复！`)) return
  try {
    await adminDeleteUser(u.id)
    showToast('用户已删除')
    await loadUsers()
  } catch (e) {
    handleAuthError(e)
  }
}

/** 后台表格用的时间格式：'2026-10-26 11:04:18' */
function formatTime(ts) {
  if (!ts) return '—'
  return String(ts).replace('T', ' ').slice(0, 19)
}

/* ---------------- 知识课堂：列表 / 新增 / 编辑 / 删除 ---------------- */
const knowledges = ref([])
const loadingKnowledges = ref(false)
const knowledgeKeyword = ref('')

async function loadKnowledges() {
  loadingKnowledges.value = true
  try {
    const data = await adminListKnowledge(knowledgeKeyword.value.trim())
    knowledges.value = data.items || []
  } catch (e) {
    handleAuthError(e)
  } finally {
    loadingKnowledges.value = false
  }
}

/** 文章列表里把 cat id 显示成中文名（'all' 显示「通用」） */
function catNameOfId(id) {
  if (!id || id === 'all') return '通用'
  return categories.value.find((c) => c.id === id)?.name || id
}

const EMPTY_KNOWLEDGE_SECTION = { h: '', p: '' }
const knowledgeForm = ref({ cat: 'cat', title: '', summary: '', tagsText: '', sections: [{ ...EMPTY_KNOWLEDGE_SECTION }] })

function openKnowledgeForm(k) {
  modalError.value = ''
  if (k) {
    knowledgeForm.value = {
      cat: k.cat || 'all',
      title: k.title || '',
      summary: k.summary || '',
      tagsText: (k.tags || []).join('、'),
      sections: (k.sections || []).length
        ? (k.sections || []).map((s) => ({ h: s.h || '', p: s.p || '' }))
        : [{ ...EMPTY_KNOWLEDGE_SECTION }]
    }
    modal.value = { type: 'knowledge', editing: true, title: '编辑文章：' + k.title, target: k }
  } else {
    knowledgeForm.value = {
      cat: categories.value[0]?.id || 'all',
      title: '', summary: '', tagsText: '',
      sections: [{ ...EMPTY_KNOWLEDGE_SECTION }]
    }
    modal.value = { type: 'knowledge', editing: false, title: '新增知识文章', target: null }
  }
}

/** 表单 -> 接口 body：标签文本拆数组，剔除空小节 */
function buildKnowledgeBody() {
  const tags = knowledgeForm.value.tagsText
    .split(/[,，、]/).map((s) => s.trim()).filter(Boolean)
  const sections = knowledgeForm.value.sections
    .filter((s) => (s.h || '').trim() || (s.p || '').trim())
    .map((s) => ({ h: s.h || '', p: s.p || '' }))
  return {
    cat: knowledgeForm.value.cat,
    title: knowledgeForm.value.title,
    summary: knowledgeForm.value.summary,
    tags,
    sections
  }
}

async function saveKnowledge() {
  modalError.value = ''
  if (!knowledgeForm.value.sections.some((s) => (s.p || '').trim())) {
    modalError.value = '至少要写一段正文内容'
    return
  }
  saving.value = true
  try {
    if (modal.value.editing) {
      await adminUpdateKnowledge(modal.value.target.id, buildKnowledgeBody())
      showToast('文章已更新')
    } else {
      await adminCreateKnowledge(buildKnowledgeBody())
      showToast('文章已创建')
    }
    modal.value = null
    await loadKnowledges()
  } catch (e) {
    modalError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeKnowledge(k) {
  if (!window.confirm(`确定删除文章「${k.title}」吗？此操作不可恢复！`)) return
  try {
    await adminDeleteKnowledge(k.id)
    showToast('文章已删除')
    await loadKnowledges()
  } catch (e) {
    handleAuthError(e)
  }
}

/* ---------------- 退出 ---------------- */
async function onLogout() {
  try {
    await adminLogout()
  } catch (e) { /* 会话已失效也视为退出成功 */ }
  showToast('已退出后台')
  emit('exit')
}

/* ---------------- 图片路径预置（public/images/pets 下现有素材） ---------------- */
const petImagePresets = [
  '/images/pets/cat.png', '/images/pets/cat_british.png', '/images/pets/cat_maine.png',
  '/images/pets/cat_orange.png', '/images/pets/cat_siamese.png', '/images/pets/dog.png',
  '/images/pets/dog_border.png', '/images/pets/dog_golden.png', '/images/pets/dog_husky.png',
  '/images/pets/dog_shiba.png', '/images/pets/bird.png', '/images/pets/bird_bunting.png',
  '/images/pets/bird_cockatiel.png', '/images/pets/bird_pigeon.png', '/images/pets/fish.png',
  '/images/pets/fish_betta.png', '/images/pets/fish_koi.png', '/images/pets/fish_tropical.png',
  '/images/pets/hamster.png', '/images/pets/small_chinchilla.png', '/images/pets/small_guinea.png',
  '/images/pets/small_rabbit.png', '/images/pets/paw-default.png'
]

/* ---------------- toast ---------------- */
const toast = ref('')
let toastTimer = null
function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

onMounted(() => {
  loadCategoriesAdmin()
})
</script>

<style scoped>
.admin {
  width: 100%;
  background: var(--card);
  border: 4px solid var(--edge);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ---------- 顶栏 ---------- */
.admin-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #8b7bff 0%, #6a5ae0 45%, #ff6fa8 130%);
  color: var(--on-accent);
}
.admin-brand { display: flex; align-items: center; gap: 12px; }
.admin-badge {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  font-size: 22px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  border: 2px solid rgba(255, 255, 255, 0.6);
}
.admin-title { margin: 0; font-size: 18px; font-weight: 800; }
.admin-sub { margin: 2px 0 0; font-size: 11.5px; font-weight: 700; opacity: 0.85; }
.admin-actions { display: flex; gap: 8px; }
.top-btn {
  border: 2px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.16);
  color: var(--on-accent);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 7px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.top-btn:hover { background: rgba(255, 255, 255, 0.3); }
.top-btn.ghost { background: transparent; }

/* ---------- 页签 ---------- */
.admin-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 24px 0;
  border-bottom: 2px solid var(--line);
}
.tab {
  border: 2px solid transparent;
  border-bottom: 0;
  background: transparent;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--ink-dim);
  padding: 9px 16px;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}
.tab:hover { color: var(--pink-deep); }
.tab.active {
  background: var(--soft);
  border-color: var(--line);
  color: var(--pink-deep);
}

/* ---------- 面板 ---------- */
.panel { padding: 14px 24px 26px; }
.panel-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}
.panel-tip { margin: 0; font-size: 12px; font-weight: 700; color: var(--ink-dim); }
.add-btn {
  border: 0;
  border-radius: 999px;
  padding: 9px 18px;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--on-accent);
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 60%);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.add-btn:hover { transform: translateY(-2px); filter: brightness(1.04); }
.add-btn:active { transform: scale(0.97); }

/* ---------- 表格 ---------- */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border: 2px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
  font-size: 13px;
}
.table th, .table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--line);
}
.table thead th {
  background: var(--soft);
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
  white-space: nowrap;
}
.table tbody tr:last-child td { border-bottom: 0; }
.table tbody tr:hover { background: var(--soft); }
.strong { font-weight: 800; }
.mono { font-family: Consolas, monospace; font-size: 12.5px; color: var(--ink-soft); }
.th-ops { width: 130px; }
.ops { display: flex; gap: 6px; white-space: nowrap; }
.row-btn {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12px;
  font-weight: 800;
  padding: 4px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}
.row-btn:hover { background: var(--soft); border-color: var(--pink); }
.row-btn.primary { color: var(--candy); border-color: var(--line); }
.row-btn.primary:hover { background: var(--soft-2); border-color: #8b7bff; }
.row-btn.danger { color: var(--pink-deep); border-color: var(--line); }
.row-btn.danger:hover { background: var(--soft); border-color: #ffb3c8; }

.thumb {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 10px;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
  display: block;
}
.icon-chip {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
  border: 2px solid var(--edge);
  box-shadow: 0 3px 8px -3px rgba(120, 40, 90, 0.5);
}
.color-chip {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: 6px;
  border-radius: 5px;
  vertical-align: -3px;
  border: 2px solid var(--edge);
  box-shadow: 0 0 0 1px var(--line);
}
.desc-cell { max-width: 320px; color: var(--ink-soft); }
.desc-cell.small { max-width: 150px; font-size: 12px; }
.level-chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 800;
  color: var(--on-accent);
  background: #ffb02e;
}
.level-chip[data-level='低'] { background: #53bc81; }
.level-chip[data-level='高'] { background: #e85d75; }
.dim { color: var(--ink-dim); }
.empty-cell { text-align: center; color: var(--ink-dim); padding: 26px 10px; font-weight: 700; }

/* ---------- 弹层 ---------- */
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(74, 59, 82, 0.36);
  backdrop-filter: blur(3px);
}
.modal {
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  background: var(--card);
  border: 4px solid var(--edge);
  border-radius: 22px;
  box-shadow: var(--shadow);
  padding: 22px 24px;
  animation: pop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
.modal-title { margin: 0 0 16px; font-size: 17px; font-weight: 800; }
.modal-form { display: grid; gap: 13px; }
.modal-form-wide { min-width: min(560px, 88vw); }
/* 文章正文小节：一行 = 标题 + 正文 + 删除 */
.sections-field { gap: 8px; }
.section-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 6px 8px;
  padding: 10px;
  border: 2px dashed var(--line);
  border-radius: 12px;
  background: var(--soft);
}
.section-row textarea { grid-column: 1; }
.section-row .row-btn { grid-row: 1 / span 2; align-self: start; }
.op-btn.small { padding: 6px 14px; font-size: 12px; align-self: start; }
.kn-tags-cell { max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.field { display: grid; gap: 5px; min-width: 0; }
.field.grow { flex: 1; }
.field-label { font-size: 12px; font-weight: 800; color: var(--ink-soft); }
.field-row { display: flex; gap: 12px; }
.field-row .field { flex: 1; }
.field-input {
  border: 2px solid var(--line);
  background: var(--soft);
  border-radius: 12px;
  padding: 10px 12px;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
  outline: none;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}
.field-input:focus {
  border-color: var(--pink);
  background: var(--card);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.15);
}
.field-input.mono { font-family: Consolas, monospace; }
.color-input { padding: 4px 6px; height: 42px; cursor: pointer; }
.textarea { resize: vertical; min-height: 62px; }
.select {
  border: 2px solid var(--line);
  background: var(--soft);
  border-radius: 12px;
  padding: 10px 12px;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
  outline: none;
  cursor: pointer;
}
.cat-select { display: flex; align-items: center; gap: 10px; font-size: 12.5px; font-weight: 800; color: var(--ink-soft); }
.cat-select .select { min-width: 220px; }

/* ---------- 用户管理 ---------- */
.user-search {
  min-width: 220px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 7px 12px;
  font-family: inherit;
  font-size: 13px;
  color: var(--ink);
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.user-search:focus {
  border-color: var(--pink);
  box-shadow: 0 0 0 3px rgba(255, 126, 179, 0.15);
}
.user-perm-tip {
  margin: 0 0 12px;
  padding: 8px 14px;
  border-radius: 12px;
  background: var(--warn-bg);
  color: var(--warn-ink);
  font-size: 12px;
  font-weight: 700;
}
.role-chip {
  padding: 3px 11px;
  border-radius: 999px;
  background: var(--soft-2);
  color: var(--info-ink);
  font-size: 11.5px;
  font-weight: 800;
  white-space: nowrap;
}
.role-chip.admin { background: var(--soft-2); color: var(--candy); }
.role-chip.super { background: var(--soft); color: var(--pink-deep); }
.field-hint {
  display: block;
  margin-top: 5px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--warn-ink);
}

.form-error {
  margin: 0;
  padding: 9px 12px;
  border-radius: 12px;
  background: var(--soft);
  border: 2px solid var(--line);
  color: var(--pink-deep);
  font-size: 12.5px;
  font-weight: 700;
}
.modal-ops { display: flex; justify-content: flex-end; gap: 10px; margin-top: 4px; }
.op-btn {
  border: 0;
  border-radius: 999px;
  padding: 10px 22px;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  cursor: pointer;
  color: var(--on-accent);
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 60%);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
  transition: transform 0.15s ease, filter 0.2s ease;
}
.op-btn:hover { transform: translateY(-1px); filter: brightness(1.04); }
.op-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
.op-btn.ghost {
  background: var(--card);
  color: var(--ink-soft);
  border: 2px solid var(--line);
  box-shadow: none;
}
.op-btn.ghost:hover { background: var(--soft); }

/* ---------- toast ---------- */
.toast {
  position: fixed;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  background: rgba(74, 59, 82, 0.92);
  color: var(--on-accent);
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 700;
  z-index: 70;
  box-shadow: 0 12px 26px -10px rgba(0, 0, 0, 0.4);
}
.fade-enter-active,
.fade-leave-active { transition: opacity 0.22s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .admin-top { padding: 14px 16px; }
  .admin-tabs, .panel { padding-left: 14px; padding-right: 14px; }
  .field-row { flex-direction: column; }
  .desc-cell { max-width: 160px; }
  .table th:nth-child(1), .table td:nth-child(1) { display: none; }
}
</style>
