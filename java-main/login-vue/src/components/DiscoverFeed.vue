<template>
  <div class="discover">
    <!-- ================= 列表页 ================= -->
    <template v-if="view === 'feed'">
      <header class="disc-head">
        <div>
          <h2 class="disc-title">
            <span class="disc-dot">发</span>
            发现萌宠
            <span v-if="postRuntime.localMode" class="mode-badge" :title="postRuntime.reason">
              本地演示模式
            </span>
          </h2>
          <p class="disc-sub">看看大家今天晒了什么萌物，也可以晒出你自己的宝贝~</p>
        </div>
        <button type="button" class="compose-btn" @click="openCompose">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" />
          </svg>
          发帖
        </button>
      </header>

      <!-- 分类筛选 -->
      <div class="filter-tabs">
        <button
          type="button"
          class="filter-tab"
          :class="{ active: activeCat === '' }"
          @click="activeCat = ''"
        >全部</button>
        <button
          v-for="c in petCategories"
          :key="c.id"
          type="button"
          class="filter-tab"
          :class="{ active: activeCat === c.id }"
          @click="activeCat = c.id"
        >{{ c.icon }} {{ c.name }}</button>
      </div>

      <!-- 帖子列表 -->
      <div v-if="posts.length" class="posts">
        <article
          v-for="p in posts"
          :key="p.id"
          class="post-card"
          @click="openPost(p.id)"
        >
          <div class="pc-cover">
            <PostMedia v-if="coverOf(p)" :media="coverOf(p)" />
            <div v-else class="pc-cover-empty" :style="{ background: tintOf(p) }">
              {{ iconOf(p) }}
            </div>
            <span v-if="(p.media || []).length > 1" class="pc-count">
              共 {{ p.media.length }} 张
            </span>
          </div>

          <div class="pc-body">
            <span class="pc-cat" :style="{ background: colorOf(p) }">{{ nameOf(p) }}</span>
            <h3 class="pc-title">{{ p.title || excerpt(p.content, 18) }}</h3>
            <p class="pc-excerpt">{{ excerpt(p.content, 44) }}</p>

            <div class="pc-meta">
              <span class="pc-author">{{ p.authorName }}</span>
              <span class="pc-time">{{ formatTime(p.createdAt) }}</span>
            </div>
            <div class="pc-stats">
              <span :class="{ liked: isLiked(p, userKey) }">
                ♥ {{ (p.likes || []).length }}
              </span>
              <span>💬 {{ (p.comments || []).length }}</span>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty">
        <CatMascot class="empty-cat" />
        <p>这个分类还没有帖子哦，来发第一条吧~</p>
      </div>
    </template>

    <!-- ================= 详情页 ================= -->
    <PostDetail
      v-else-if="view === 'detail' && activePost"
      :post="activePost"
      :user-key="userKey"
      :user-name="userName"
      :can-delete="activePost.author === user?.username"
      @back="goFeed"
      @deleted="onDeleted"
      @toast="showToast"
      @friend-added="$emit('friend-added', $event)"
      @open-user="$emit('open-user', $event)"
      @open-chat="$emit('open-chat', $event)"
    />

    <!-- ================= 发帖页 ================= -->
    <PostComposer
      v-else-if="view === 'compose'"
      :author="user?.username || 'guest'"
      :author-name="userName"
      @back="goFeed"
      @published="onPublished"
      @toast="showToast"
    />

    <!-- 轻提示（Teleport 到 body，避免被卡片 transform 影响定位） -->
    <Teleport to="body">
      <transition name="fade">
        <div v-if="toast" class="toast">{{ toast }}</div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import CatMascot from './CatMascot.vue'
import PostMedia from './PostMedia.vue'
import PostDetail from './PostDetail.vue'
import PostComposer from './PostComposer.vue'
import {
  petCategories,
  categoryName,
  categoryColor,
  categoryIcon
} from '../composables/petData'
import { excerpt, formatTime, isLiked } from '../composables/postStore'
import { postRuntime, fetchPosts } from '../api/post'

const props = defineProps({
  user: { type: Object, default: () => ({}) }
})
// friend-added：在帖子里加了好友，主页需要刷新好友列表
// open-user   ：点了作者头像，需要切到对方主页 / 回到自己的首页
// open-chat   ：已是好友，直接和作者私信
defineEmits(['friend-added', 'open-user', 'open-chat'])

const userKey = computed(() => props.user?.username || 'guest')
const userName = computed(
  () => props.user?.nickname || props.user?.username || '萌宠用户'
)

const view = ref('feed') // 'feed' | 'detail' | 'compose'
const activeCat = ref('')
// 详情页持有帖子的对象引用（而非实时查询）：删除帖子时组件不会被提前卸载，
// PostDetail 里的 emit('deleted') 才能正常回到列表页
const activePost = ref(null)
const toast = ref('')
let toastTimer = null

function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1900)
}

// 列表数据：始终经 api 层获取（真实模式=服务端；接口不可用=自动回退本地 store）
const posts = ref([])

async function refresh() {
  try {
    const res = await fetchPosts({ category: activeCat.value })
    posts.value = (res && res.items) || []
  } catch (e) {
    posts.value = []
    showToast((e && e.message) || '加载失败，请稍后再试')
  }
}

// 分类切换 / 组件挂载时刷新
watch(activeCat, refresh)
onMounted(refresh)

function coverOf(p) {
  return (p.media || [])[0] || null
}
function nameOf(p) {
  return categoryName(p.category)
}
function colorOf(p) {
  return categoryColor(p.category)
}
function iconOf(p) {
  return categoryIcon(p.category)
}
function tintOf(p) {
  const c = categoryColor(p.category)
  return `linear-gradient(135deg, ${c}33 0%, #ffe9f3 100%)`
}

function goFeed() {
  view.value = 'feed'
  activePost.value = null
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function openPost(id) {
  // 兼容外部跳转（如首页搜索结果）：列表未加载时先刷新一次再找
  let p = posts.value.find((x) => String(x.id) === String(id))
  if (!p) {
    await refresh()
    p = posts.value.find((x) => String(x.id) === String(id))
  }
  if (!p) {
    showToast('帖子不存在或已删除')
    return
  }
  activePost.value = p
  view.value = 'detail'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function openCompose() {
  view.value = 'compose'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onPublished() {
  activeCat.value = ''
  view.value = 'feed'
  refresh()
  showToast(
    postRuntime.localMode ? '发布成功（已保存到本地演示数据）' : '发布成功，快去看看吧~'
  )
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onDeleted() {
  goFeed()
  refresh()
  showToast('帖子已删除')
}

// 供父组件（首页搜索结果）跳到指定帖子详情
defineExpose({ openPost })
</script>

<style scoped>
.discover { animation: pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 头部 ---------- */
.disc-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px 30px 4px;
}
.disc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 800;
}
.disc-dot {
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
.disc-sub { margin: 0; font-size: 13px; color: var(--ink-soft); }
.mode-badge {
  margin-left: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  background: #fff4e2;
  color: #b8792a;
  font-size: 11px;
  font-weight: 800;
  vertical-align: middle;
}

.compose-btn {
  margin-left: auto;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  min-width: 112px;
  border: 0;
  border-radius: 999px;
  padding: 11px 22px;
  font-family: inherit;
  font-size: 14.5px;
  font-weight: 800;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 55%, #ff8a5c 130%);
  box-shadow: 0 10px 20px -8px rgba(255, 92, 157, 0.6), inset 0 -3px 0 rgba(0, 0, 0, 0.08);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.compose-btn svg { width: 17px; height: 17px; }
.compose-btn:hover { transform: translateY(-2px) scale(1.02); filter: brightness(1.04); }
.compose-btn:active { transform: scale(0.97); }

/* ---------- 分类筛选 ---------- */
.filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 30px 16px;
}
.filter-tab {
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.filter-tab:hover { background: #fff5f9; border-color: var(--pink); transform: translateY(-1px); }
.filter-tab.active {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
}

/* ---------- 帖子卡片 ---------- */
.posts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 0 30px 34px;
}
.post-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.post-card:hover {
  transform: translateY(-4px);
  border-color: var(--pink);
  box-shadow: 0 18px 32px -14px rgba(124, 78, 140, 0.45);
}
.pc-cover {
  position: relative;
  height: 146px;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.pc-cover-empty {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 15px;
  font-weight: 800;
  color: var(--pink-deep);
}
.pc-count {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
}
.pc-body { padding: 11px 13px 13px; }
.pc-cat {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
}
.pc-title {
  margin: 7px 0 4px;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.35;
}
.pc-excerpt {
  margin: 0 0 10px;
  font-size: 12.5px;
  line-height: 1.5;
  color: var(--ink-soft);
  min-height: 38px;
}
.pc-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 11.5px;
  color: var(--ink-dim);
  font-weight: 700;
}
.pc-stats {
  display: flex;
  gap: 14px;
  margin-top: 7px;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
}
.pc-stats .liked { color: var(--pink-deep); }

/* ---------- 空状态 / 提示 ---------- */
.empty { text-align: center; color: var(--ink-soft); padding: 30px 16px 44px; }
.empty p { margin: 8px 0; font-size: 14px; }
.empty-cat {
  position: static;
  width: 84px;
  height: 78px;
  margin: 0 auto 10px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}

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
  z-index: 50;
  box-shadow: 0 12px 26px -10px rgba(0, 0, 0, 0.4);
}
.fade-enter-active,
.fade-leave-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; transform: translate(-50%, 10px); }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .disc-head { padding: 18px 16px 4px; flex-wrap: wrap; }
  .compose-btn { margin-left: 0; }
  .filter-tabs { padding: 12px 16px 14px; }
  .posts { grid-template-columns: repeat(2, 1fr); gap: 12px; padding: 0 16px 24px; }
  .pc-cover { height: 116px; }
}
</style>
