<template>
  <div class="search-page">
    <!-- ================= 顶部：返回 + 搜索框 + 结果概况 ================= -->
    <header class="sp-head">
      <div class="sp-top">
        <button type="button" class="sp-back" aria-label="返回首页" @click="$emit('back')">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          返回
        </button>

        <form class="sp-form" @submit.prevent="submit">
          <svg class="sp-ico" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2.4" />
            <line x1="16.2" y1="16.2" x2="21" y2="21" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
          </svg>
          <input
            v-model="input"
            class="sp-input"
            type="text"
            placeholder="搜索萌宠 / 品种 / 用户 / 动态…"
            aria-label="搜索关键词"
          />
          <button type="submit" class="sp-btn" :disabled="loading">
            {{ loading ? '搜索中' : '搜索' }}
          </button>
        </form>
      </div>

      <!-- 搜索历史：还没搜的时候展示，点词直接搜，单条可删 -->
      <div v-if="!shownQuery && history.items.length" class="sp-history">
        <div class="sp-history-head">
          <span class="sp-history-label">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="2" />
              <path d="M12 7.5V12l3 2" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
            搜索历史
          </span>
          <button type="button" class="sp-history-clear" @click="clearHistory">清空</button>
        </div>
        <div class="sp-history-chips">
          <span v-for="h in history.items" :key="'h-' + h" class="sp-history-chip">
            <button type="button" class="sp-history-word" @click="searchFor(h)">{{ h }}</button>
            <button
              type="button"
              class="sp-history-del"
              :aria-label="'删除历史 ' + h"
              title="删除"
              @click.stop="removeHistory(h)"
            >×</button>
          </span>
        </div>
      </div>

      <div class="sp-summary">
        <h2 class="sp-title">「{{ shownQuery }}」的搜索结果</h2>
        <p class="sp-sub">
          <template v-if="loading">正在全站搜索…</template>
          <template v-else>
            共 <strong>{{ result.total }}</strong> 条 · 分类 {{ counts.categories }} · 品种 {{ counts.breeds }} ·
            动态 {{ counts.posts }} · 用户 {{ counts.users }}
          </template>
          <span v-if="!loading && result.source === 'local'" class="sp-source">本地数据</span>
        </p>
      </div>

      <!-- 按种类筛选 -->
      <ul v-if="!loading && result.total" class="sp-tabs">
        <li
          v-for="t in tabs"
          :key="t.key"
          :class="{ active: filter === t.key, muted: !t.count }"
          @click="filter = t.key"
        >
          {{ t.label }}<b v-if="t.count">{{ t.count }}</b>
        </li>
      </ul>
    </header>

    <!-- ================= 加载 ================= -->
    <div v-if="loading" class="sp-loading">
      <span class="sp-loading-dot" />
      正在搜索「{{ shownQuery }}」…
    </div>

    <template v-else-if="result.total">
      <!-- ---------- 相关分类 ---------- -->
      <section v-if="show('categories') && result.categories.length" class="sp-sec">
        <h3 class="sp-sec-title">
          <span class="sp-dot" style="background: #ff6fa8">分</span>
          相关分类
          <em>{{ result.categories.length }}</em>
        </h3>
        <div class="sp-cats">
          <button
            v-for="c in result.categories"
            :key="'cat-' + c.id"
            type="button"
            class="sp-cat"
            :style="{ '--c': c.color || '#ff6fa8' }"
            @click="$emit('open-category', c)"
          >
            <img class="sp-cat-img" :src="c.image" :alt="c.name" loading="lazy" />
            <span class="sp-cat-text">
              <strong>{{ c.name }}</strong>
              <em>{{ c.breedCount }} 个品种 · 查看图鉴 ›</em>
            </span>
          </button>
        </div>
      </section>

      <!-- ---------- 相关品种 ---------- -->
      <section v-if="show('breeds') && result.breeds.length" class="sp-sec">
        <h3 class="sp-sec-title">
          <span class="sp-dot" style="background: #8b7bff">种</span>
          相关品种
          <em>{{ result.breeds.length }}</em>
        </h3>
        <div class="sp-breeds">
          <button
            v-for="b in result.breeds"
            :key="'breed-' + (b.categoryId || '') + '-' + b.name"
            type="button"
            class="sp-breed"
            @click="$emit('open-breed', b)"
          >
            <img class="sp-breed-img" :src="b.image" :alt="b.name" loading="lazy" />
            <span class="sp-breed-body">
              <span class="sp-breed-tag" :style="{ background: b.categoryColor || '#ff6fa8' }">
                {{ b.categoryIcon || '萌' }} {{ b.categoryName || '萌宠' }}
              </span>
              <strong class="sp-breed-name">{{ b.name }}</strong>
              <span class="sp-breed-desc">{{ b.desc }}</span>
            </span>
          </button>
        </div>
      </section>

      <!-- ---------- 相关动态 ---------- -->
      <section v-if="show('posts') && result.posts.length" class="sp-sec">
        <h3 class="sp-sec-title">
          <span class="sp-dot" style="background: #ff8a5c">动</span>
          相关动态
          <em>{{ result.posts.length }}</em>
        </h3>
        <ul class="sp-posts">
          <li
            v-for="p in result.posts"
            :key="'post-' + (p.postId || p.id)"
            class="sp-post"
            @click="$emit('open-post', p.postId || p.id)"
          >
            <span class="sp-post-cover">
              <img v-if="p.image" :src="p.image" :alt="p.title" loading="lazy" />
              <template v-else>{{ p.cover || '萌' }}</template>
            </span>
            <span class="sp-post-body">
              <strong class="sp-post-title">{{ p.title }}</strong>
              <span class="sp-post-summary">{{ p.summary }}</span>
              <span class="sp-post-meta">
                <em v-if="p.categoryName" class="sp-post-cat">{{ p.categoryName }}</em>
                {{ p.authorName || p.author || '萌宠用户' }} · {{ formatTime(p.createdAt) }}
              </span>
            </span>
            <span class="sp-post-enter">查看 ›</span>
          </li>
        </ul>
      </section>

      <!-- ---------- 相关用户 ---------- -->
      <section v-if="show('users') && result.users.length" class="sp-sec">
        <h3 class="sp-sec-title">
          <span class="sp-dot" style="background: #5fd4b0">人</span>
          相关用户
          <em>{{ result.users.length }}</em>
        </h3>
        <ul class="sp-users">
          <li
            v-for="u in result.users"
            :key="'user-' + u.key"
            class="sp-user"
            @click="$emit('open-user', { key: u.key, name: u.name })"
          >
            <span class="sp-user-avatar">{{ (u.name || u.key || '萌').charAt(0).toUpperCase() }}</span>
            <span class="sp-user-body">
              <strong>{{ u.name || u.key }}</strong>
              <em>@{{ u.username || u.key }}</em>
            </span>
            <span class="sp-post-enter">看主页 ›</span>
          </li>
        </ul>
      </section>
    </template>

    <!-- ================= 空结果 ================= -->
    <div v-else class="sp-empty">
      <PetMascot :pet="todayPet" class="sp-empty-cat" />
      <p class="sp-empty-title">没有找到与「{{ shownQuery }}」相关的内容</p>
      <p v-if="result.error" class="sp-empty-sub">（{{ result.error }}）</p>
      <p class="sp-empty-sub">换个关键词试试，或者直接逛这些：</p>
      <div class="sp-suggests">
        <button v-for="s in suggests" :key="s" type="button" class="sp-suggest" @click="searchFor(s)">{{ s }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import PetMascot from './PetMascot.vue'
import { searchAll } from '../api/home'
import { formatTime } from '../composables/postStore'
import { useTodayPet } from '../composables/dailyPet'
import { searchHistory, addHistory, removeHistory, clearHistory } from '../composables/searchHistory'

// 空结果吉祥物 = 今日宠物（一天一换，点击可换表情）
const todayPet = useTodayPet()

const props = defineProps({
  /** 进入页面时的关键词（来自首页搜索框 / 热门词 / 品种详情的「搜索相关动态」） */
  keyword: { type: String, default: '' }
})
const emit = defineEmits(['back', 'search', 'open-category', 'open-breed', 'open-post', 'open-user'])

/** 输入框内容（可与当前结果不同，提交后才生效） */
const input = ref(props.keyword)
/** 当前结果对应的关键词（空结果文案、标题都用它，避免输入框还在打字时文案乱跳） */
const shownQuery = ref('')

const loading = ref(false)
const filter = ref('all')

/** 统一的结果结构，字段与 api/home.js 的 searchAll 保持一致 */
const emptyResult = {
  ok: true,
  source: 'local',
  query: '',
  total: 0,
  counts: { categories: 0, breeds: 0, posts: 0, users: 0 },
  categories: [],
  breeds: [],
  posts: [],
  users: []
}
const result = ref(emptyResult)

const counts = computed(() => result.value.counts || emptyResult.counts)

const tabs = computed(() => [
  { key: 'all', label: '全部', count: result.value.total },
  { key: 'categories', label: '分类', count: counts.value.categories },
  { key: 'breeds', label: '品种', count: counts.value.breeds },
  { key: 'posts', label: '动态', count: counts.value.posts },
  { key: 'users', label: '用户', count: counts.value.users }
])

/** 当前筛选下某组是否展示 */
function show(group) {
  if (filter.value !== 'all' && filter.value !== group) return false
  return true
}

const suggests = ['布偶猫', '柯基', '仓鼠', '鹦鹉', '金鱼']

async function run(q) {
  const kw = String(q || '').trim()
  if (!kw) return
  shownQuery.value = kw
  addHistory(kw) // 记入搜索历史（模块内自动去重置顶）
  loading.value = true
  filter.value = 'all'
  try {
    const res = await searchAll(kw)
    result.value = { ...emptyResult, ...res, counts: { ...emptyResult.counts, ...(res.counts || {}) } }
  } catch (e) {
    result.value = { ...emptyResult, query: kw, error: String((e && e.message) || e) }
  } finally {
    loading.value = false
  }
}

/** 表单提交（回车 / 点搜索）：重新搜索（同词重搜也允许），并通知父组件同步关键词 */
function submit() {
  const kw = input.value.trim()
  if (!kw) return
  const changed = kw !== shownQuery.value
  run(kw)
  if (changed) emit('search', kw)
}

/** 点推荐词 */
function searchFor(word) {
  input.value = word
  run(word)
  emit('search', word)
}

// 父组件换关键词（例如从品种详情「搜索相关动态」再次进入）时重新搜索
watch(
  () => props.keyword,
  (kw) => {
    if (!kw || kw === shownQuery.value) return
    input.value = kw
    run(kw)
  },
  { immediate: true }
)
</script>

<style scoped>
.search-page {
  animation: pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ---------- 顶部 ---------- */
.sp-head {
  padding: 20px 30px 22px;
  background:
    radial-gradient(70% 120% at 92% 0%, rgba(255, 207, 92, 0.22) 0%, transparent 62%),
    linear-gradient(180deg, var(--hero-1) 0%, var(--card) 85%);
  border-bottom: 2px dashed var(--line);
}
.sp-top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sp-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex: none;
  border: 2px solid var(--edge);
  background: rgba(255, 255, 255, 0.78);
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 14px 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 6px 14px -6px rgba(124, 78, 140, 0.35);
  transition: transform 0.14s ease, background 0.18s ease;
}
.sp-back svg { width: 16px; height: 16px; }
.sp-back:hover { background: var(--card); transform: translateX(-2px); }

.sp-form {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  padding: 5px 5px 5px 14px;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 999px;
  box-shadow: 0 8px 20px -14px rgba(124, 78, 140, 0.5);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.sp-form:focus-within {
  border-color: var(--pink);
  box-shadow: 0 10px 24px -14px rgba(255, 92, 157, 0.65);
}
.sp-ico { width: 19px; height: 19px; flex: none; color: var(--ink-dim); }
.sp-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  padding: 8px 0;
}
.sp-input::placeholder { color: var(--ink-dim); font-weight: 500; }
.sp-btn {
  flex: none;
  border: 0;
  border-radius: 999px;
  padding: 9px 20px;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: var(--on-accent);
  font-family: inherit;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: filter 0.16s ease, transform 0.12s ease;
}
.sp-btn:hover:not(:disabled) { filter: brightness(1.05); transform: translateY(-1px); }
.sp-btn:disabled { opacity: 0.72; cursor: default; }

.sp-summary { margin-top: 16px; padding-left: 4px; }
.sp-title { margin: 0; font-size: 23px; font-weight: 800; color: var(--ink); }
.sp-sub {
  margin: 6px 0 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-soft);
}
.sp-sub strong { color: var(--pink-deep); font-size: 15px; }
.sp-source {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--soft);
  border: 1px solid var(--line);
  color: var(--ink-dim);
  font-size: 11px;
  font-weight: 800;
}

/* ---------- 种类筛选 ---------- */
.sp-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  list-style: none;
  margin: 14px 0 0;
  padding: 0;
}
.sp-tabs li {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.16s ease;
}
.sp-tabs li:hover { border-color: var(--pink); color: var(--pink-deep); }
.sp-tabs li.active {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  border-color: transparent;
  color: var(--on-accent);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.7);
}
.sp-tabs li.muted { opacity: 0.55; }
.sp-tabs b {
  font-size: 11px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(124, 78, 140, 0.1);
}
.sp-tabs li.active b { background: rgba(255, 255, 255, 0.28); }

/* ---------- 分组 ---------- */
.sp-sec { padding: 20px 30px 0; }
.sp-sec:last-of-type { padding-bottom: 30px; }
.sp-sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  padding-left: 2px;
  font-size: 16px;
  font-weight: 800;
  color: var(--ink);
}
.sp-sec-title em {
  font-style: normal;
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
}
.sp-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
  flex: none;
}

/* 分类卡 */
.sp-cats { display: flex; flex-wrap: wrap; gap: 12px; }
.sp-cat {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1 1 240px;
  max-width: 320px;
  padding: 10px 14px 10px 10px;
  border: 2px solid var(--line);
  border-radius: 20px;
  background: var(--card);
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.18s ease, box-shadow 0.2s ease;
}
.sp-cat:hover {
  transform: translateY(-3px);
  border-color: var(--c);
  box-shadow: 0 14px 24px -14px rgba(124, 78, 140, 0.5);
}
.sp-cat-img {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  object-fit: cover;
  flex: none;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.sp-cat-text { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.sp-cat-text strong { font-size: 15px; font-weight: 800; color: var(--ink); }
.sp-cat-text em { font-style: normal; font-size: 12px; font-weight: 600; color: var(--ink-soft); }

/* 品种卡 */
.sp-breeds {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(158px, 1fr));
  gap: 12px;
}
.sp-breed {
  display: flex;
  flex-direction: column;
  padding: 0 0 12px;
  border: 2px solid var(--line);
  border-radius: 18px;
  background: var(--card);
  overflow: hidden;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.18s ease, box-shadow 0.2s ease;
}
.sp-breed:hover {
  transform: translateY(-4px);
  border-color: var(--pink);
  box-shadow: 0 16px 26px -14px rgba(124, 78, 140, 0.45);
}
.sp-breed-img {
  width: 100%;
  height: 112px;
  object-fit: cover;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.sp-breed-body { display: flex; flex-direction: column; gap: 4px; padding: 9px 12px 0; }
.sp-breed-tag {
  align-self: flex-start;
  padding: 1px 8px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 11px;
  font-weight: 800;
}
.sp-breed-name { font-size: 15px; font-weight: 800; color: var(--ink); }
.sp-breed-desc {
  font-size: 12px;
  line-height: 1.45;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 动态列表 */
.sp-posts, .sp-users { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.sp-post, .sp-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px 10px 10px;
  border: 2px solid var(--line);
  border-radius: 18px;
  background: var(--card);
  cursor: pointer;
  transition: transform 0.14s ease, border-color 0.18s ease, box-shadow 0.2s ease;
}
.sp-post:hover, .sp-user:hover {
  transform: translateX(2px);
  border-color: var(--pink);
  box-shadow: 0 12px 22px -16px rgba(124, 78, 140, 0.6);
}
.sp-post-cover {
  width: 62px;
  height: 62px;
  flex: none;
  border-radius: 14px;
  display: grid;
  place-items: center;
  overflow: hidden;
  font-size: 22px;
  font-weight: 800;
  color: var(--pink-deep);
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.sp-post-cover img { width: 100%; height: 100%; object-fit: cover; }
.sp-post-body { display: flex; flex-direction: column; gap: 3px; min-width: 0; flex: 1; }
.sp-post-title { font-size: 15px; font-weight: 800; color: var(--ink); }
.sp-post-summary {
  font-size: 12.5px;
  color: var(--ink-soft);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sp-post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--ink-dim);
}
.sp-post-cat {
  font-style: normal;
  padding: 0 7px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--pink-deep);
}
.sp-post-enter { flex: none; font-size: 12px; font-weight: 800; color: var(--pink-deep); }

/* 用户行 */
.sp-user-avatar {
  width: 44px;
  height: 44px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--on-accent);
  font-size: 17px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--candy) 0%, var(--pink) 100%);
}
.sp-user-body { display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0; }
.sp-user-body strong { font-size: 15px; font-weight: 800; color: var(--ink); }
.sp-user-body em { font-style: normal; font-size: 12px; font-weight: 700; color: var(--ink-dim); }

/* ---------- 加载 / 空状态 ---------- */
.sp-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 46px 30px;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-soft);
}
.sp-loading-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--pink);
  animation: sp-pulse 0.9s ease-in-out infinite;
}
@keyframes sp-pulse {
  0%, 100% { transform: scale(0.7); opacity: 0.5; }
  50% { transform: scale(1.15); opacity: 1; }
}

.sp-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 34px 30px 44px;
}
.sp-empty-cat { position: static; width: 112px; height: 104px; margin-bottom: 6px; }
.sp-empty-title { margin: 0; font-size: 16px; font-weight: 800; color: var(--ink); }
.sp-empty-sub { margin: 6px 0 14px; font-size: 13px; font-weight: 600; color: var(--ink-soft); }
.sp-suggests { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; }
.sp-suggest {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink-soft);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.16s ease;
}
.sp-suggest:hover { border-color: var(--pink); color: var(--pink-deep); transform: translateY(-1px); }

/* ---------- 搜索历史 ---------- */
.sp-history {
  margin: 4px 30px 12px;
  padding: 12px 16px;
  background: var(--soft);
  border: 2px dashed var(--line);
  border-radius: 18px;
}
.sp-history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 9px;
}
.sp-history-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 800;
  color: var(--ink-soft);
  letter-spacing: 0.5px;
}
.sp-history-label svg { width: 14px; height: 14px; }
.sp-history-clear {
  border: 0;
  background: transparent;
  color: var(--ink-dim);
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: 999px;
  transition: color 0.15s ease, background 0.15s ease;
}
.sp-history-clear:hover { color: var(--bad); background: var(--card); }
.sp-history-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.sp-history-chip {
  display: inline-flex;
  align-items: center;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 999px;
  overflow: hidden;
  transition: border-color 0.15s ease, transform 0.15s ease;
}
.sp-history-chip:hover { border-color: var(--pink); transform: translateY(-1px); }
.sp-history-word {
  border: 0;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  padding: 6px 4px 6px 12px;
  cursor: pointer;
}
.sp-history-word:hover { color: var(--pink-deep); }
.sp-history-del {
  border: 0;
  background: transparent;
  color: var(--ink-dim);
  font-size: 14px;
  line-height: 1;
  padding: 6px 10px 6px 4px;
  cursor: pointer;
}
.sp-history-del:hover { color: var(--bad); }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .sp-head { padding: 14px 16px 18px; }
  .sp-top { flex-wrap: wrap; }
  .sp-form { flex-basis: 100%; }
  .sp-title { font-size: 19px; }
  .sp-sec { padding: 16px 16px 0; }
  .sp-cat { flex: 1 1 100%; max-width: none; }
  .sp-breeds { grid-template-columns: repeat(auto-fill, minmax(132px, 1fr)); }
  .sp-post-enter { display: none; }
}
</style>
