<template>
  <div class="kn-detail">
    <!-- ================= 顶部操作条 ================= -->
    <header class="kd-bar">
      <button type="button" class="kd-back" @click="$emit('back')" aria-label="返回知识课堂">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        返回知识课堂
      </button>
      <span class="kd-views" title="浏览数">👁 {{ views }} 次浏览</span>
    </header>

    <!-- ================= 文章主体 ================= -->
    <article class="kd-article">
      <header class="kd-head">
        <span class="kd-cat" :style="{ background: catColorOf(current) }">
          {{ catIconOf(current) }} {{ catNameOf(current) }}
        </span>
        <h2 class="kd-title">{{ current.title }}</h2>
        <p v-if="current.summary" class="kd-sum">{{ current.summary }}</p>
        <div v-if="(current.tags || []).length" class="kd-tags">
          <span v-for="t in current.tags" :key="t"># {{ t }}</span>
        </div>
      </header>

      <div class="kd-body">
        <div v-for="s in current.sections" :key="s.h" class="kd-block">
          <h3 class="kd-h">{{ s.h }}</h3>
          <p class="kd-p">{{ s.p }}</p>
        </div>
        <p class="kd-note">内容为一般性科普，不能替代执业兽医的当面诊断。</p>

        <!-- 上一篇 / 下一篇：同分类内顺序翻看 -->
        <div class="kd-nav">
          <button
            type="button"
            class="kd-nav-btn"
            :class="{ disabled: !prevArticle }"
            :disabled="!prevArticle"
            @click="go(prevArticle)"
          >
            <span class="kd-nav-dir">上一篇</span>
            <span class="kd-nav-t">{{ prevArticle ? prevArticle.title : '已经是第一篇' }}</span>
          </button>
          <button
            type="button"
            class="kd-nav-btn"
            :class="{ disabled: !nextArticle }"
            :disabled="!nextArticle"
            @click="go(nextArticle)"
          >
            <span class="kd-nav-dir">下一篇</span>
            <span class="kd-nav-t">{{ nextArticle ? nextArticle.title : '已经是最后一篇' }}</span>
          </button>
        </div>

        <!-- 相关文章：同分类推荐，不足时兜底用其他文章 -->
        <div v-if="related.length" class="kd-rel">
          <p class="kd-rel-title">
            <span class="kd-rel-dot" :style="{ background: catColorOf(current) }">相</span>
            相关文章
          </p>
          <div class="kd-rel-grid">
            <button
              type="button"
              v-for="r in related"
              :key="r.id"
              class="kd-rel-card"
              @click="go(r)"
            >
              <span class="kd-rel-cat" :style="{ background: catColorOf(r) }">{{ catIconOf(r) }}</span>
              <span class="kd-rel-name">{{ r.title }}</span>
              <span class="kd-rel-meta">{{ catNameOf(r) }} · {{ viewsOf(r) }} 浏览</span>
            </button>
          </div>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { petCategories } from '../composables/petData'
import { KNOWLEDGE_ARTICLES } from '../composables/knowledgeData'
import { fetchKnowledgeList, readKnowledge } from '../api/knowledge'

const props = defineProps({
  // 文章对象：{ id, cat, title, summary, tags:[], sections:[{h,p}], views?, createdAt? }
  article: { type: Object, required: true }
})
const emit = defineEmits(['back', 'open-article'])

// 远程返回的最新内容会覆盖 current（含正文/浏览数）；静态兜底文章则一直用传入对象
const current = ref(props.article)
const views = ref(Number(props.article.views) || 0)

// 全量文章列表：用于「相关文章」与「上一篇/下一篇」的定位
// 先用静态兜底，后端返回后整体覆盖（与 KnowledgePage 同一套逻辑）
const list = ref(KNOWLEDGE_ARTICLES)

async function loadArticle(a) {
  current.value = a
  views.value = Number(a.views) || 0
  // 静态兜底文章 id 是字符串（如 'k-cat-start'），没有远端数据，跳过请求
  if (typeof a.id !== 'number') return
  try {
    const data = await readKnowledge(a.id)
    const fresh = data && data.article
    if (!fresh) return
    current.value = fresh
    views.value = Number(fresh.views) || 0
  } catch (e) {
    /* 后端不可用：保留传入内容即可，不影响阅读 */
  }
}

async function loadList() {
  try {
    const data = await fetchKnowledgeList()
    if (Array.isArray(data.items) && data.items.length) list.value = data.items
  } catch (e) {
    /* 后端不可用：保留静态兜底列表，相关/上下篇依然可用 */
  }
}

onMounted(() => {
  // 列表与单篇内容并行拉取，互不影响
  loadList()
  loadArticle(props.article)
})

// 在详情页内跳转到另一篇时，article 这个 prop 会变（Home 复用同一个组件实例），
// 用 watch 同步更新内容与浏览数，而不是重新挂载整页
watch(
  () => props.article,
  (a) => { if (a) loadArticle(a) }
)

// 同分类的有序列表（用于定位上一篇/下一篇）；'all' 类文章之间也按此排序
const ordered = computed(() => list.value.filter((a) => a.cat === current.value.cat))
const curIndex = computed(() => ordered.value.findIndex((a) => a.id === current.value.id))
const prevArticle = computed(() =>
  curIndex.value > 0 ? ordered.value[curIndex.value - 1] : null
)
const nextArticle = computed(() =>
  curIndex.value >= 0 && curIndex.value < ordered.value.length - 1
    ? ordered.value[curIndex.value + 1]
    : null
)

// 相关文章：优先同分类（排除自己），不足 3 条时用其他文章兜底
const related = computed(() => {
  const cur = current.value
  const sameCat = list.value.filter((a) => a.id !== cur.id && a.cat === cur.cat)
  const pool = sameCat.length ? sameCat : list.value.filter((a) => a.id !== cur.id)
  return pool.slice(0, 3)
})

// 在详情页内跳转：通知上层（Home）把 pageArticle 换成目标文章并滚动置顶
function go(a) {
  if (a) emit('open-article', a)
}

/* ---------------- 展示辅助（与知识列表共用同一套分类映射） ---------------- */
const FALLBACK = { name: '通用', icon: '知', color: 'var(--pink)' }

function catInfoOf(a) {
  return petCategories.find((c) => c.id === a.cat) || FALLBACK
}
function catNameOf(a) {
  return a.cat === 'all' ? '通用' : catInfoOf(a).name
}
function catIconOf(a) {
  return a.cat === 'all' ? '知' : catInfoOf(a).icon
}
function catColorOf(a) {
  return a.cat === 'all' ? 'var(--pink)' : catInfoOf(a).color
}
function viewsOf(a) {
  return Number(a && a.views) || 0
}
</script>

<style scoped>
.kn-detail { animation: pop 0.42s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 操作条 ---------- */
.kd-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.kd-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 7px 15px 7px 11px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.14s ease, background 0.18s ease, border-color 0.18s ease;
}
.kd-back svg { width: 16px; height: 16px; }
.kd-back:hover { background: var(--soft); border-color: var(--pink); transform: translateX(-2px); }
.kd-views {
  margin-left: auto;
  font-size: 11px;
  font-weight: 800;
  color: var(--ink-dim);
  opacity: 0.85;
}

/* ---------- 文章卡片 ---------- */
.kd-article {
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 18px 40px -20px rgba(124, 78, 140, 0.35);
}
.kd-head {
  padding: 24px 26px 18px;
  background:
    radial-gradient(80% 120% at 92% 0%, rgba(255, 207, 92, 0.18) 0%, transparent 60%),
    linear-gradient(180deg, var(--hero-1) 0%, var(--card) 80%);
  border-bottom: 2px dashed var(--line);
}
.kd-cat {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 11.5px;
  font-weight: 800;
}
.kd-title { margin: 10px 0 0; font-size: 22px; font-weight: 800; line-height: 1.35; }
.kd-sum { margin: 8px 0 0; font-size: 13.5px; font-weight: 700; color: var(--ink-soft); line-height: 1.6; }
.kd-tags { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 12px; }
.kd-tags span {
  padding: 2px 11px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--pink-deep);
  font-size: 11px;
  font-weight: 800;
}

.kd-body {
  padding: 18px 26px 22px;
}
.kd-block { margin-bottom: 16px; }
.kd-h {
  margin: 0 0 6px;
  font-size: 15.5px;
  font-weight: 800;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 7px;
}
.kd-h::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--pink);
  flex: none;
}
.kd-p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink);
  word-break: break-word;
}
.kd-note {
  margin: 20px 0 0;
  padding: 9px 13px;
  border-radius: 12px;
  background: var(--warn-bg);
  color: var(--warn-ink);
  font-size: 11.5px;
  font-weight: 700;
}

/* ---------- 上一篇 / 下一篇 ---------- */
.kd-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 18px;
}
.kd-nav-btn {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
  border: 2px solid var(--line);
  background: var(--card);
  border-radius: 16px;
  padding: 11px 14px;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.18s ease, transform 0.14s ease, box-shadow 0.2s ease;
}
.kd-nav-btn:hover:not(.disabled) {
  border-color: var(--pink);
  transform: translateY(-2px);
  box-shadow: 0 12px 22px -14px rgba(124, 78, 140, 0.4);
}
.kd-nav-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.kd-nav-dir {
  font-size: 11px;
  font-weight: 800;
  color: var(--pink-deep);
}
.kd-nav-t {
  font-size: 13px;
  font-weight: 800;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- 相关文章 ---------- */
.kd-rel { margin-top: 22px; }
.kd-rel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 800;
}
.kd-rel-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
}
.kd-rel-grid { display: grid; gap: 10px; }
.kd-rel-card {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  border: 2px solid var(--line);
  background: var(--card);
  border-radius: 16px;
  padding: 11px 14px;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.18s ease, transform 0.14s ease, box-shadow 0.2s ease;
}
.kd-rel-card:hover {
  border-color: var(--pink);
  transform: translateY(-2px);
  box-shadow: 0 14px 26px -16px rgba(124, 78, 140, 0.42);
}
.kd-rel-cat {
  width: 36px;
  height: 36px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 12px;
  color: var(--on-accent);
  font-size: 15px;
  font-weight: 800;
}
.kd-rel-name {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 800;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kd-rel-meta {
  flex: none;
  font-size: 11px;
  font-weight: 800;
  color: var(--ink-dim);
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .kd-head { padding: 20px 16px 16px; }
  .kd-title { font-size: 19px; }
  .kd-body { padding: 16px 16px 18px; }
  .kd-nav { grid-template-columns: 1fr; }
}
</style>
