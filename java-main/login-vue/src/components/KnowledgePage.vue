<template>
  <div class="knowledge">
    <!-- ================= 头部 ================= -->
    <header class="kn-head">
      <div>
        <h2 class="kn-title">
          <span class="kn-dot">知</span>
          知识课堂
        </h2>
        <p class="kn-sub">
          科学养宠小百科 · {{ articles.length }} 篇知识 + {{ totalBreeds }} 个品种速查
        </p>
      </div>
    </header>

    <!-- ================= 搜索 + 分类筛选 ================= -->
    <section class="kn-search">
      <div class="kn-search-box">
        <svg class="kn-search-ico" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2.4" />
          <line x1="16.2" y1="16.2" x2="21" y2="21" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
        </svg>
        <input
          v-model="keyword"
          class="kn-search-input"
          type="text"
          maxlength="30"
          placeholder="搜知识或品种，如：疫苗、金毛、换水"
        />
        <button v-if="keyword" type="button" class="kn-clear" @click="keyword = ''" aria-label="清空">×</button>
      </div>

      <div class="kn-chips" role="tablist">
        <button
          type="button"
          class="kn-chip"
          :class="{ active: catFilter === 'all' }"
          @click="catFilter = 'all'"
        >全部</button>
        <button
          v-for="c in petCategories"
          :key="c.id"
          type="button"
          class="kn-chip"
          :class="{ active: catFilter === c.id }"
          :style="catFilter === c.id ? { background: c.color, borderColor: 'transparent', color: '#fff' } : {}"
          @click="catFilter = catFilter === c.id ? 'all' : c.id"
        >{{ c.icon }} {{ c.name }}</button>
      </div>
    </section>

    <!-- ================= 搜索结果模式 ================= -->
    <template v-if="keyword.trim()">
      <!-- 品种速查 -->
      <section v-if="matchedBreeds.length" class="kn-sec">
        <h3 class="kn-sec-title">
          <span class="kn-sec-dot" :style="{ background: matchedBreeds[0].category.color }">品</span>
          品种速查（{{ matchedBreeds.length }}）
        </h3>
        <div class="kn-breed-grid">
          <button
            v-for="m in matchedBreeds"
            :key="m.breed.name"
            type="button"
            class="kn-breed-card"
            @click="$emit('open-breed', m)"
          >
            <img class="kn-breed-img" :src="m.breed.image" :alt="m.breed.name" loading="lazy" />
            <div class="kn-breed-main">
              <div class="kn-breed-name">{{ m.breed.name }}</div>
              <div class="kn-breed-meta">{{ m.category.icon }} {{ m.category.name }} · {{ m.breed.desc }}</div>
            </div>
            <span class="kn-breed-go">详情 →</span>
          </button>
        </div>
      </section>

      <!-- 相关知识 -->
      <section class="kn-sec">
        <h3 class="kn-sec-title">
          <span class="kn-sec-dot" :style="{ background: 'var(--pink)' }">识</span>
          相关知识（{{ visibleArticles.length }}）
        </h3>
        <div v-if="visibleArticles.length" class="kn-grid">
          <button
            v-for="a in visibleArticles"
            :key="a.id"
            type="button"
            class="kn-card"
            :style="{ '--kn-color': catColorOf(a) }"
            @click="openArticle(a)"
          >
            <span class="kn-card-icon" :style="{ background: catColorOf(a) }">{{ catIconOf(a) }}</span>
            <div class="kn-card-main">
              <div class="kn-card-title">{{ a.title }}</div>
              <p class="kn-card-sum">{{ a.summary }}</p>
              <div class="kn-card-foot">
                <div class="kn-card-tags">
                  <span v-for="t in a.tags" :key="t">{{ t }}</span>
                </div>
                <span class="kn-views" title="浏览数">👁 {{ viewsOf(a) }}</span>
              </div>
            </div>
            <span class="kn-card-go">阅读 →</span>
          </button>
        </div>
        <div v-else class="kn-empty">
          <p>没找到和「{{ keyword }}」相关的内容，换个词试试：疫苗、驱虫、换水、训练…</p>
        </div>
      </section>
    </template>

    <!-- ================= 默认浏览模式 ================= -->
    <template v-else>
      <section class="kn-sec">
        <h3 class="kn-sec-title">
          <span class="kn-sec-dot" :style="{ background: activeCat ? activeCat.color : 'var(--pink)' }">课</span>
          {{ activeCat ? activeCat.name + ' · 知识' : '全部知识' }}
        </h3>
        <div v-if="visibleArticles.length" class="kn-grid">
          <button
            v-for="a in visibleArticles"
            :key="a.id"
            type="button"
            class="kn-card"
            :style="{ '--kn-color': catColorOf(a) }"
            @click="openArticle(a)"
          >
            <span class="kn-card-icon" :style="{ background: catColorOf(a) }">{{ catIconOf(a) }}</span>
            <div class="kn-card-main">
              <div class="kn-card-title">{{ a.title }}</div>
              <p class="kn-card-sum">{{ a.summary }}</p>
              <div class="kn-card-foot">
                <div class="kn-card-tags">
                  <span v-for="t in a.tags" :key="t">{{ t }}</span>
                </div>
                <span class="kn-views" title="浏览数">👁 {{ viewsOf(a) }}</span>
              </div>
            </div>
            <span class="kn-card-go">阅读 →</span>
          </button>
        </div>
        <div v-else class="kn-empty">
          <p>该分类下暂无知识内容，去「全部」里看看吧~</p>
        </div>
      </section>

      <section class="kn-sec">
        <h3 class="kn-sec-title">
          <span class="kn-sec-dot" :style="{ background: 'var(--pink-deep)' }">品</span>
          想了解某个品种？
        </h3>
        <p class="kn-hint">在上方搜索框输入品种名（如「布偶猫」「柯基」），可以直接跳到品种详情。</p>
      </section>
    </template>

    <!-- ================= 文章详情弹层 ================= -->
    <transition name="fade">
      <div v-if="active" class="kn-mask" @click.self="active = null">
        <div class="kn-article">
          <button type="button" class="kn-close" @click="active = null" aria-label="关闭">×</button>
          <header class="kn-article-head">
            <div class="kn-article-meta">
              <span class="kn-article-cat" :style="{ background: catColorOf(active) }">
                {{ catIconOf(active) }} {{ catNameOf(active) }}
              </span>
              <span class="kn-views">👁 {{ viewsOf(active) }} 次浏览</span>
            </div>
            <h3 class="kn-article-title">{{ active.title }}</h3>
            <p class="kn-article-sum">{{ active.summary }}</p>
          </header>
          <div class="kn-article-body">
            <div v-for="s in active.sections" :key="s.h" class="kn-article-block">
              <h4 class="kn-article-h">{{ s.h }}</h4>
              <p class="kn-article-p">{{ s.p }}</p>
            </div>
            <p class="kn-article-note">内容为一般性科普，不能替代执业兽医的当面诊断。</p>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { petCategories } from '../composables/petData'
import { KNOWLEDGE_ARTICLES, searchIn } from '../composables/knowledgeData'
import { fetchKnowledgeList, readKnowledge } from '../api/knowledge'

defineEmits(['open-breed'])

const keyword = ref('')
const catFilter = ref('all')
const active = ref(null)

// 先用静态内容兜底渲染，后端返回后整体覆盖（含 views 浏览数）
const articles = ref(KNOWLEDGE_ARTICLES)
const remoteReady = ref(false)

async function loadArticles() {
  try {
    const data = await fetchKnowledgeList()
    if (Array.isArray(data.items)) {
      // 后端没有数据（比如刚建库没灌种子）时保留静态兜底，避免页面空白
      articles.value = data.items.length ? data.items : KNOWLEDGE_ARTICLES
      remoteReady.value = data.items.length > 0
    }
  } catch (e) {
    remoteReady.value = false // 后端不可用，保持静态兜底
  }
}
onMounted(loadArticles)

/** 打开详情：顺带通知后端 views +1，并把列表里的浏览数同步更新 */
async function openArticle(a) {
  active.value = a
  if (!remoteReady.value || typeof a.id !== 'number') return
  try {
    const data = await readKnowledge(a.id)
    const fresh = data.article
    if (!fresh) return
    active.value = fresh
    const i = articles.value.findIndex((x) => x.id === fresh.id)
    if (i >= 0) articles.value[i] = fresh
  } catch (e) {
    /* 浏览数统计失败不影响阅读 */
  }
}

const activeCat = computed(() => petCategories.find((c) => c.id === catFilter.value) || null)
const totalBreeds = computed(() =>
  petCategories.reduce((n, c) => n + (c.breeds || []).length, 0)
)

/** 搜索模式下命中的文章 */
const matchedArticles = computed(() => searchIn(articles.value, keyword.value))

/** 当前展示的文章列表：搜索模式走全文搜索，浏览模式走分类过滤 */
const visibleArticles = computed(() => {
  if (keyword.value.trim()) {
    // 搜索结果里同时应用分类筛选（默认全部）
    return catFilter.value === 'all'
      ? matchedArticles.value
      : matchedArticles.value.filter((a) => a.cat === catFilter.value || a.cat === 'all')
  }
  return catFilter.value === 'all'
    ? articles.value
    : articles.value.filter((a) => a.cat === catFilter.value || a.cat === 'all')
})

/** 搜索模式下命中的品种（来自数据库分类/品种数据） */
const matchedBreeds = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return []
  const out = []
  for (const c of petCategories) {
    for (const b of c.breeds || []) {
      const hay = [b.name, b.desc, b.temperament, b.careLevel, b.suitableFor, c.name]
        .filter(Boolean).join(' ').toLowerCase()
      if (hay.includes(k)) out.push({ breed: b, category: c })
    }
  }
  return out.slice(0, 8)
})

/* ---------------- 展示辅助 ---------------- */
const FALLBACK = { name: '通用', icon: '知', color: 'var(--pink)' }

/** 浏览数：静态兜底文章没有该字段，显示 0 即可 */
function viewsOf(a) {
  return Number(a && a.views) || 0
}
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
</script>

<style scoped>
.knowledge { animation: pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 头部 ---------- */
.kn-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px 30px 4px;
}
.kn-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 800;
}
.kn-dot {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink) 0%, var(--candy) 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}
.kn-sub { margin: 0; font-size: 13px; color: var(--ink-soft); }

/* ---------- 搜索 ---------- */
.kn-search { padding: 16px 30px 0; }
.kn-search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 999px;
  padding: 6px 8px 6px 16px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.kn-search-box:focus-within {
  border-color: var(--pink);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.16);
}
.kn-search-ico { width: 18px; height: 18px; flex: none; color: var(--ink-dim); }
.kn-search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
}
.kn-clear {
  flex: none;
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: #f6eef4;
  color: var(--ink-soft);
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.kn-clear:hover { background: #ffd6de; color: var(--bad); }

.kn-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
.kn-chip {
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 700;
  padding: 6px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.kn-chip:hover { background: #fff5f9; border-color: var(--pink); transform: translateY(-1px); }
.kn-chip.active {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
}

/* ---------- 区块 ---------- */
.kn-sec { padding: 20px 30px 0; }
.kn-sec:last-of-type { padding-bottom: 34px; }
.kn-sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  font-size: 15.5px;
  font-weight: 800;
}
.kn-sec-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}
.kn-hint { margin: 0; font-size: 12.5px; color: var(--ink-dim); font-weight: 700; }

/* ---------- 知识卡片 ---------- */
.kn-grid { display: grid; gap: 12px; }
.kn-card {
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
  border: 2px solid var(--line);
  background: #fff;
  border-radius: 18px;
  padding: 14px 16px;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.18s ease, box-shadow 0.2s ease, transform 0.14s ease;
}
.kn-card:hover {
  border-color: var(--kn-color, var(--pink));
  box-shadow: 0 16px 28px -16px rgba(124, 78, 140, 0.45);
  transform: translateY(-2px);
}
.kn-card-icon {
  width: 44px;
  height: 44px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  font-size: 17px;
  font-weight: 800;
  border: 2px solid #fff;
  box-shadow: 0 6px 14px -6px rgba(120, 40, 90, 0.35);
}
.kn-card-main { flex: 1; min-width: 0; }
.kn-card-title { font-size: 14.5px; font-weight: 800; color: var(--ink); }
.kn-card-sum {
  margin: 3px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--ink-soft);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kn-card-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}
.kn-card-tags { display: flex; flex-wrap: wrap; gap: 5px; flex: 1; min-width: 0; }
.kn-card-tags span {
  padding: 1px 9px;
  border-radius: 999px;
  background: #fff0f6;
  color: var(--pink-deep);
  font-size: 10.5px;
  font-weight: 800;
}
.kn-views {
  flex: none;
  font-size: 11px;
  font-weight: 800;
  color: var(--ink-dim);
  opacity: 0.85;
}
.kn-card-go {
  flex: none;
  font-size: 11.5px;
  font-weight: 800;
  color: var(--ink-dim);
}
.kn-card:hover .kn-card-go { color: var(--kn-color, var(--pink)); }

/* ---------- 品种速查 ---------- */
.kn-breed-grid { display: grid; gap: 10px; }
.kn-breed-card {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  border: 2px solid var(--line);
  background: #fff;
  border-radius: 16px;
  padding: 10px 14px;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.18s ease, box-shadow 0.2s ease, transform 0.14s ease;
}
.kn-breed-card:hover {
  border-color: var(--pink);
  box-shadow: 0 14px 26px -16px rgba(124, 78, 140, 0.45);
  transform: translateY(-2px);
}
.kn-breed-img {
  width: 44px;
  height: 44px;
  flex: none;
  border-radius: 12px;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 6px 12px -6px rgba(120, 40, 90, 0.3);
  background: #ffe3c2;
}
.kn-breed-main { flex: 1; min-width: 0; }
.kn-breed-name { font-size: 14px; font-weight: 800; color: var(--ink); }
.kn-breed-meta {
  margin-top: 2px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--ink-dim);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kn-breed-go { flex: none; font-size: 11.5px; font-weight: 800; color: var(--ink-dim); }
.kn-breed-card:hover .kn-breed-go { color: var(--pink-deep); }

/* ---------- 空状态 ---------- */
.kn-empty {
  text-align: center;
  padding: 28px 16px 10px;
  color: var(--ink-soft);
}
.kn-empty p { margin: 0; font-size: 13px; line-height: 1.6; }

/* ---------- 文章详情弹层 ---------- */
.kn-mask {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  background: rgba(80, 40, 90, 0.35);
  backdrop-filter: blur(3px);
  padding: 20px;
}
.kn-article {
  position: relative;
  width: min(640px, 100%);
  max-height: 82vh;
  overflow-y: auto;
  background: #fff;
  border: 4px solid #fff;
  border-radius: 24px;
  box-shadow: 0 30px 60px -20px rgba(90, 40, 110, 0.5);
  padding: 26px 26px 20px;
  animation: pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
.kn-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: #f6eef4;
  color: var(--ink-soft);
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
}
.kn-close:hover { background: #ffd6de; color: var(--bad); }
.kn-article-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.kn-article-cat {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  color: #fff;
  font-size: 11.5px;
  font-weight: 800;
}
.kn-article-title { margin: 10px 0 0; font-size: 20px; font-weight: 800; }
.kn-article-sum { margin: 6px 0 0; font-size: 13px; font-weight: 700; color: var(--ink-soft); }
.kn-article-body {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 2px dashed var(--line);
}
.kn-article-block { margin-bottom: 14px; }
.kn-article-h {
  margin: 0 0 6px;
  font-size: 14.5px;
  font-weight: 800;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 7px;
}
.kn-article-h::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--pink);
  flex: none;
}
.kn-article-p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--ink);
  word-break: break-word;
}
.kn-article-note {
  margin: 18px 0 0;
  padding: 8px 12px;
  border-radius: 12px;
  background: #fff4e2;
  color: #b8792a;
  font-size: 11.5px;
  font-weight: 700;
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .kn-head { padding: 18px 16px 4px; }
  .kn-search { padding: 12px 16px 0; }
  .kn-sec { padding: 16px 16px 0; }
  .kn-sec:last-of-type { padding-bottom: 24px; }
  .kn-card { flex-wrap: wrap; padding: 12px; }
  .kn-card-sum { white-space: normal; }
  .kn-article { padding: 20px 16px 16px; }
}
</style>
