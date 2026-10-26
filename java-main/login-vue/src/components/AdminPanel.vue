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
    <section v-else class="panel">
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

    <!-- ============ 弹层表单（分类 / 品种共用外壳） ============ -->
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
          <form v-else class="modal-form" @submit.prevent="saveBreed">
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
import { ref, onMounted } from 'vue'
import {
  adminListCategories, adminCreateCategory, adminUpdateCategory, adminDeleteCategory,
  adminListBreeds, adminCreateBreed, adminUpdateBreed, adminDeleteBreed, adminLogout
} from '../api/admin'
import { loadCategories } from '../composables/petData'

const props = defineProps({
  admin: { type: Object, default: null }
})
const emit = defineEmits(['exit'])

const adminName = props.admin?.nickname || props.admin?.username || '管理员'

/* ---------------- 页签 ---------------- */
const tab = ref('category')
function switchTab(t) {
  tab.value = t
  if (t === 'breed' && !breedCatId.value && categories.value.length) {
    breedCatId.value = categories.value[0].id
    loadBreeds()
  }
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
  '/images/pets/small_rabbit.png'
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
  border: 4px solid #fff;
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
  color: #fff;
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
  color: #fff;
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
  background: #fdf6fb;
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
  color: #fff;
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
  background: #fdf6fb;
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
  white-space: nowrap;
}
.table tbody tr:last-child td { border-bottom: 0; }
.table tbody tr:hover { background: #fffafc; }
.strong { font-weight: 800; }
.mono { font-family: Consolas, monospace; font-size: 12.5px; color: var(--ink-soft); }
.th-ops { width: 130px; }
.ops { display: flex; gap: 6px; white-space: nowrap; }
.row-btn {
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12px;
  font-weight: 800;
  padding: 4px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}
.row-btn:hover { background: #fff5f9; border-color: var(--pink); }
.row-btn.primary { color: #6a5ae0; border-color: #d8d0ff; }
.row-btn.primary:hover { background: #f5f2ff; border-color: #8b7bff; }
.row-btn.danger { color: #d6336c; border-color: #ffd3dd; }
.row-btn.danger:hover { background: #fff0f3; border-color: #ffb3c8; }

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
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  border: 2px solid #fff;
  box-shadow: 0 3px 8px -3px rgba(120, 40, 90, 0.5);
}
.color-chip {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: 6px;
  border-radius: 5px;
  vertical-align: -3px;
  border: 2px solid #fff;
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
  color: #fff;
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
  border: 4px solid #fff;
  border-radius: 22px;
  box-shadow: var(--shadow);
  padding: 22px 24px;
  animation: pop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
.modal-title { margin: 0 0 16px; font-size: 17px; font-weight: 800; }
.modal-form { display: grid; gap: 13px; }
.field { display: grid; gap: 5px; min-width: 0; }
.field.grow { flex: 1; }
.field-label { font-size: 12px; font-weight: 800; color: var(--ink-soft); }
.field-row { display: flex; gap: 12px; }
.field-row .field { flex: 1; }
.field-input {
  border: 2px solid var(--line);
  background: #fdf6fb;
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
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.15);
}
.field-input.mono { font-family: Consolas, monospace; }
.color-input { padding: 4px 6px; height: 42px; cursor: pointer; }
.textarea { resize: vertical; min-height: 62px; }
.select {
  border: 2px solid var(--line);
  background: #fdf6fb;
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

.form-error {
  margin: 0;
  padding: 9px 12px;
  border-radius: 12px;
  background: #fff0f3;
  border: 2px solid #ffd3dd;
  color: #d6336c;
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
  color: #fff;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 60%);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
  transition: transform 0.15s ease, filter 0.2s ease;
}
.op-btn:hover { transform: translateY(-1px); filter: brightness(1.04); }
.op-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
.op-btn.ghost {
  background: #fff;
  color: var(--ink-soft);
  border: 2px solid var(--line);
  box-shadow: none;
}
.op-btn.ghost:hover { background: #fdf6fb; }

/* ---------- toast ---------- */
.toast {
  position: fixed;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  background: rgba(74, 59, 82, 0.92);
  color: #fff;
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
