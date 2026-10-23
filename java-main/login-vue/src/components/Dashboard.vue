<template>
  <div class="dashboard" id="dashboard">
    <!-- ============ 账号信息卡 ============ -->
    <section class="auth-card profile">
      <div class="avatar" aria-hidden="true">{{ avatarLetter }}</div>
      <h2 class="dash-title">欢迎回家~</h2>
      <p class="dash-sub">好久不见，<span>{{ displayName }}</span>！今天也超努力哦</p>

      <dl class="dash-info">
        <div><dt>用户名</dt><dd>{{ user.username }}</dd></div>
        <div><dt>邮箱</dt><dd>{{ user.email || '未填写' }}</dd></div>
        <div><dt>加入日期</dt><dd>{{ since }}</dd></div>
      </dl>

      <button type="button" class="btn-ghost" @click="$emit('logout')">退出登录</button>
    </section>

    <!-- ============ 我的发帖记录 ============ -->
    <section class="mine">
      <header class="mine-head">
        <div>
          <h3 class="mine-title">
            <span class="mine-dot">帖</span>
            我的发帖记录
          </h3>
          <p class="mine-sub">
            共 <strong>{{ myPosts.length }}</strong> 篇内容，收获
            <strong>{{ totalLikes }}</strong> 个赞、
            <strong>{{ totalComments }}</strong> 条评论
          </p>
        </div>
        <button type="button" class="mine-refresh" :disabled="loading" @click="load">
          {{ loading ? '刷新中…' : '刷新' }}
        </button>
      </header>

      <!-- 加载中 -->
      <div v-if="loading && !myPosts.length" class="mine-tip">正在加载你的帖子…</div>

      <!-- 帖子列表 -->
      <div v-else-if="myPosts.length" class="mine-list">
        <article
          v-for="p in myPosts"
          :key="p.id"
          class="mine-card"
          @click="$emit('open-post', p.id)"
        >
          <div class="mc-cover">
            <PostMedia v-if="coverOf(p)" :media="coverOf(p)" />
            <div v-else class="mc-cover-empty" :style="{ background: tintOf(p) }">
              {{ iconOf(p) }}
            </div>
            <span v-if="(p.media || []).length > 1" class="mc-count">
              共 {{ p.media.length }} 张
            </span>
          </div>

          <div class="mc-body">
            <span class="mc-cat" :style="{ background: colorOf(p) }">{{ nameOf(p) }}</span>
            <h4 class="mc-title">{{ p.title || excerpt(p.content, 18) }}</h4>
            <p class="mc-excerpt">{{ excerpt(p.content, 46) }}</p>

            <div class="mc-meta">
              <span>{{ formatTime(p.createdAt) }}</span>
              <span class="mc-stats">
                <em :class="{ liked: isLiked(p, userKey) }">♥ {{ (p.likes || []).length }}</em>
                <em>💬 {{ (p.comments || []).length }}</em>
              </span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="mine-empty">
        <PetMascot :pet="todayPet" class="empty-cat" />
        <p>你还没有发过帖子哦，去发现里晒一只吧~</p>
        <button type="button" class="btn-ghost small" @click="$emit('go-discover')">
          去发帖
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import PetMascot from './PetMascot.vue'
import PostMedia from './PostMedia.vue'
import { fetchPosts } from '../api/post'
import { excerpt, formatTime, isLiked } from '../composables/postStore'
import { categoryName, categoryColor, categoryIcon } from '../composables/petData'
import { petOfTheDay } from '../composables/dailyPet'

const props = defineProps({
  user: { type: Object, required: true }
})
const emit = defineEmits(['logout', 'open-post', 'go-discover'])

const displayName = computed(
  () => props.user?.nickname || props.user?.username || '朋友'
)
const avatarLetter = computed(
  () => (displayName.value.charAt(0) || '喵').toUpperCase()
)
// 「我的」页空状态的吉祥物 = 首页同一只今日宠物（一天一换，点击可换表情）
const todayPet = computed(() => petOfTheDay(props.user?.username))
const since = computed(() =>
  props.user?.createdAt
    ? new Date(props.user.createdAt).toLocaleDateString('zh-CN')
    : '—'
)

// 「我的」页用登录用户名作为作者标识（与发帖时传入的 author 一致）
const userKey = computed(() => props.user?.username || 'guest')

/* ---------- 发帖记录 ---------- */
const myPosts = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await fetchPosts({ author: userKey.value })
    const items = (res && res.items) || []
    // 新的排前面
    myPosts.value = items
      .slice()
      .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
  } catch (e) {
    myPosts.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
// 切换账号（退出后再登录其它账号）时重新拉取
watch(userKey, load)

const totalLikes = computed(() =>
  myPosts.value.reduce((n, p) => n + (p.likes || []).length, 0)
)
const totalComments = computed(() =>
  myPosts.value.reduce((n, p) => n + (p.comments || []).length, 0)
)

/* ---------- 卡片展示辅助 ---------- */
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

defineExpose({ load })
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 18px; }

/* ---------- 账号信息卡（沿用原有 auth-card 视觉） ---------- */
.auth-card { max-width: 460px; margin: 0 auto; }

/* ---------- 发帖记录 ---------- */
.mine {
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 22px;
  padding: 20px 26px 24px;
  box-shadow: 0 14px 30px -22px rgba(124, 78, 140, 0.5);
}
.mine-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--line);
}
.mine-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 800;
}
.mine-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink) 0%, var(--candy) 100%);
  color: #fff;
  font-size: 11.5px;
  font-weight: 800;
}
.mine-sub { margin: 0; font-size: 12.5px; color: var(--ink-soft); }
.mine-sub strong { color: var(--pink-deep); font-size: 14px; }

.mine-refresh {
  margin-left: auto;
  flex: none;
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.mine-refresh:hover:not(:disabled) { background: #fff5f9; border-color: var(--pink); }
.mine-refresh:disabled { opacity: 0.6; cursor: default; }

.mine-tip { padding: 24px 0; text-align: center; font-size: 13px; color: var(--ink-soft); }

.mine-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  padding-top: 16px;
}
.mine-card {
  display: flex;
  gap: 12px;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 18px;
  padding: 10px;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.mine-card:hover {
  transform: translateY(-3px);
  border-color: var(--pink);
  box-shadow: 0 16px 28px -16px rgba(124, 78, 140, 0.45);
}
.mc-cover {
  position: relative;
  flex: none;
  width: 96px;
  height: 96px;
  border-radius: 13px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.mc-cover-empty {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 14px;
  font-weight: 800;
  color: var(--pink-deep);
}
.mc-count {
  position: absolute;
  right: 5px;
  bottom: 5px;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
}
.mc-body { min-width: 0; flex: 1; }
.mc-cat {
  display: inline-block;
  padding: 1px 9px;
  border-radius: 999px;
  color: #fff;
  font-size: 10.5px;
  font-weight: 800;
}
.mc-title {
  margin: 6px 0 3px;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mc-excerpt {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.mc-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-dim);
}
.mc-stats { display: flex; gap: 10px; }
.mc-stats em { font-style: normal; }
.mc-stats .liked { color: var(--pink-deep); }

.mine-empty { text-align: center; padding: 26px 16px 8px; color: var(--ink-soft); }
.mine-empty p { margin: 6px 0 14px; font-size: 13.5px; }
.empty-cat {
  position: static;
  width: 76px;
  height: 70px;
  margin: 0 auto 8px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}
.btn-ghost.small { padding: 8px 20px; font-size: 13px; }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .auth-card { max-width: 100%; }
  .mine { padding: 16px 16px 20px; border-radius: 18px; }
  .mine-list { grid-template-columns: 1fr; gap: 10px; }
  .mc-cover { width: 84px; height: 84px; }
}
</style>
