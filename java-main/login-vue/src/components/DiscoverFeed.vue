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
              本地数据
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
          type="button"
          class="filter-tab"
          :class="{ active: activeCat === 'friends' }"
          @click="activeCat = 'friends'"
        >👥 好友</button>
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
        <PostCard
          v-for="p in posts"
          :key="p.id"
          :post="p"
          :user-key="userKey"
          @open="openPost"
        />
      </div>

      <div v-else class="empty">
        <PetMascot :pet="todayPet" class="empty-cat" />
        <p v-if="activeCat === 'friends'">好友还没有发过动态哦～去「全部」逛逛，或认识更多铲屎官吧！</p>
        <p v-else>这个分类还没有帖子哦，来发第一条吧~</p>
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
import PetMascot from './PetMascot.vue'
import PostDetail from './PostDetail.vue'
import PostComposer from './PostComposer.vue'
import PostCard from './PostCard.vue'
import { petCategories } from '../composables/petData'
import { postRuntime, fetchPosts } from '../api/post'
import { completeTask as completeAdoptionTask } from '../api/adoption'
import { listFriends } from '../composables/friendStore'
import { friendsList } from '../composables/petAdoption'
import { useTodayPet } from '../composables/dailyPet'

const props = defineProps({
  user: { type: Object, default: () => ({}) },
  // 进入「发现」时的初始视图：'feed'（默认，看全部动态）| 'compose'（直接打开发帖页）| 'friends'（选中"好友"分类，只看好友帖子）
  initialView: { type: String, default: 'feed' }
})
// 空状态吉祥物 = 今日宠物（一天一换，点击可换表情）
const todayPet = useTodayPet()
// friend-added：在帖子里加了好友，主页需要刷新好友列表
// open-user   ：点了作者头像，需要切到对方主页 / 回到自己的首页
// open-chat   ：已是好友，直接和作者私信
defineEmits(['friend-added', 'open-user', 'open-chat'])

const userKey = computed(() => props.user?.username || 'guest')
const userName = computed(
  () => props.user?.nickname || props.user?.username || '萌宠用户'
)

const view = ref('feed') // 'feed' | 'detail' | 'compose'
// 任务入口可带 initial-view：'compose' 直接打开发帖页；'friends' 初始选中「好友」分类
if (props.initialView === 'compose') view.value = 'compose'
const activeCat = ref(props.initialView === 'friends' ? 'friends' : '')
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

/* 好友 key 集合：好友关系以 friendStore（本地）为准，叠加萌宠模块同步来的好友列表 */
const friendKeys = computed(() => {
  const keys = new Set()
  listFriends(userKey.value, 'friend').forEach((f) => f.key && keys.add(String(f.key)))
  ;(friendsList.value || []).forEach((f) => f.key && keys.add(String(f.key)))
  return keys
})

async function refresh() {
  try {
    // 「好友」分类不是服务端分类：先拉全量，再按好友 key 过滤
    const res = await fetchPosts({ category: activeCat.value === 'friends' ? '' : activeCat.value })
    let items = (res && res.items) || []
    if (activeCat.value === 'friends') {
      items = items.filter((p) => friendKeys.value.has(String(p.author)))
    }
    posts.value = items
  } catch (e) {
    posts.value = []
    showToast((e && e.message) || '加载失败，请稍后再试')
  }
}

// 分类切换 / 组件挂载时刷新
watch(activeCat, refresh)
onMounted(refresh)

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

async function onPublished() {
  activeCat.value = ''
  view.value = 'feed'
  refresh()
  showToast(
    postRuntime.localMode ? '发布成功（已保存到本地演示数据）' : '发布成功，快去看看吧~'
  )
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 关联萌宠任务：真实发布一条动态 = 完成「发布一条动态」任务（已领过/后端不可用时静默忽略）
  try {
    const r = await completeAdoptionTask(userKey.value, 'post')
    if (r && r.ok) showToast('萌宠任务「发布一条动态」已完成，去领奖励吧 🎁')
  } catch (e) {
    /* 任务同步失败不影响发帖 */
  }
}

function onDeleted() {
  goFeed()
  refresh()
  showToast('帖子已删除')
}

// 供父组件（首页搜索结果 / 萌宠任务）跳转到指定帖子详情或直接打开发帖页
defineExpose({ openPost, openCompose })
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
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
}
.disc-sub { margin: 0; font-size: 13px; color: var(--ink-soft); }
.mode-badge {
  margin-left: 8px;
  padding: 1px 8px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: transparent;
  color: var(--ink-dim);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.3px;
  vertical-align: middle;
  opacity: 0.7;
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
  color: var(--on-accent);
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
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.filter-tab:hover { background: var(--soft); border-color: var(--pink); transform: translateY(-1px); }
.filter-tab.active {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  border-color: transparent;
  color: var(--on-accent);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
}

/* ---------- 帖子卡片 ---------- */
.posts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 0 30px 34px;
}
/* 帖子卡片样式已抽到 PostCard.vue（首页与发现页共用） */

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
  color: var(--on-accent);
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
