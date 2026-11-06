<template>
  <div class="uprofile">
    <!-- ================= 顶部操作条 ================= -->
    <div class="up-bar">
      <button type="button" class="up-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        返回
      </button>
      <button v-if="self" type="button" class="up-settings" title="设置" @click="$emit('open-settings')">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M4 7h8.6M17.8 7H20M4 17h2.8M11.2 17H20" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
          <circle cx="15.2" cy="7" r="2.5" fill="none" stroke="currentColor" stroke-width="2.2" />
          <circle cx="8.8" cy="17" r="2.5" fill="none" stroke="currentColor" stroke-width="2.2" />
        </svg>
        设置
      </button>
    </div>

    <!-- ================= 主页封面（宠物主题装饰：纯 CSS，无图片依赖） ================= -->
    <section class="up-banner">
      <span class="up-banner-cloud cloud-a" aria-hidden="true"></span>
      <span class="up-banner-cloud cloud-b" aria-hidden="true"></span>
      <span class="up-banner-star star-a" aria-hidden="true">✦</span>
      <span class="up-banner-star star-b" aria-hidden="true">✧</span>
      <svg class="up-banner-paw paw-a" viewBox="0 0 32 32" aria-hidden="true">
        <g fill="currentColor">
          <ellipse cx="16" cy="21.5" rx="8.5" ry="6.8" />
          <circle cx="6.5" cy="13" r="3.2" />
          <circle cx="12.8" cy="8.4" r="3.2" />
          <circle cx="19.8" cy="8.4" r="3.2" />
          <circle cx="26" cy="13" r="3.2" />
        </g>
      </svg>
      <svg class="up-banner-paw paw-b" viewBox="0 0 32 32" aria-hidden="true">
        <g fill="currentColor">
          <ellipse cx="16" cy="21.5" rx="8.5" ry="6.8" />
          <circle cx="6.5" cy="13" r="3.2" />
          <circle cx="12.8" cy="8.4" r="3.2" />
          <circle cx="19.8" cy="8.4" r="3.2" />
          <circle cx="26" cy="13" r="3.2" />
        </g>
      </svg>
      <div class="up-banner-text">
        <strong>{{ self ? '欢迎回到我的小窝~' : '来 TA 的小窝串个门' }}</strong>
        <span>ฅ^•ﻌ•^ฅ　今天也要被萌到哦</span>
      </div>
    </section>

    <!-- ================= 个人名片 ================= -->
    <section class="up-card">
      <span class="up-avatar" :style="{ background: tintFor(person.key) }">{{ letter }}</span>

      <div class="up-main">
        <h2 class="up-name">{{ person.name }}</h2>
        <p class="up-id">@{{ person.key }}</p>
        <div class="up-badges">
          <span class="up-badge" :class="{ soft: !self && relation !== 'friend' }">
            {{ self ? '本人' : relation === 'friend' ? '✓ 已是好友' : relation === 'pending_out' ? '等待对方通过' : relation === 'pending_in' ? 'TA 请求加你为好友' : '还不是好友' }}
          </span>
          <span v-if="self" class="up-badge soft">这是我的主页</span>
        </div>
      </div>

      <!-- 操作按钮：本人显示「编辑资料」；非本人按好友关系显示对应动作 -->
      <div class="up-actions">
        <button
          v-if="self"
          type="button"
          class="up-btn edit"
          @click="$emit('edit-profile')"
        >编辑资料</button>
        <template v-else>
          <button
            v-if="relation === 'none'"
            type="button"
            class="up-btn primary"
            :disabled="adding"
            @click="onAddFriend"
          >{{ adding ? '发送中…' : '+ 加好友' }}</button>
          <button
            v-else-if="relation === 'pending_in'"
            type="button"
            class="up-btn primary"
            :disabled="adding"
            @click="onAcceptIncoming"
          >{{ adding ? '处理中…' : '同意申请' }}</button>
          <button
            v-else-if="relation === 'friend'"
            type="button"
            class="up-btn primary"
            @click="$emit('chat', person)"
          >发消息</button>
          <button
            v-else
            type="button"
            class="up-btn muted"
            disabled
          >等待通过</button>
        </template>
      </div>

      <!-- 公开个性标签：访客视角在名片内、徽章下方整宽展示（邮箱/生日等隐私仍仅本人可见） -->
      <div v-if="!self && profileTags.length" class="up-card-tags">
        <span v-for="t in profileTags" :key="t" class="up-info-chip">{{ t }}</span>
      </div>
    </section>

    <!-- ================= 数据统计 ================= -->
    <section class="up-stats">
      <div class="up-stat">
        <strong>{{ posts.length }}</strong>
        <span>发帖</span>
      </div>
      <div class="up-stat">
        <strong>{{ totalLikes }}</strong>
        <span>获赞</span>
      </div>
      <div class="up-stat">
        <strong>{{ totalComments }}</strong>
        <span>评论</span>
      </div>
      <div class="up-stat">
        <strong>{{ since }}</strong>
        <span>加入</span>
      </div>
    </section>

    <!-- ================= 我的资料（仅本人视角可见，不对外泄露隐私字段） ================= -->
    <section v-if="self" class="up-info-card">
      <div class="up-info-head">
        <h3 class="up-info-title">我的资料</h3>
        <span class="up-new">新增</span>
        <span class="up-info-private">仅自己可见</span>
      </div>
      <dl class="up-info-list">
        <div class="up-info-row">
          <dt>邮箱</dt>
          <dd :class="{ empty: profileEmail === '未填写' }">{{ profileEmail }}</dd>
        </div>
        <div class="up-info-row">
          <dt>生日</dt>
          <dd :class="{ empty: profileBirthday === '未填写' }">{{ profileBirthday }}</dd>
        </div>
        <div class="up-info-row">
          <dt>个性标签</dt>
          <dd v-if="profileTags.length" class="up-info-tags">
            <span v-for="t in profileTags" :key="t" class="up-info-chip">{{ t }}</span>
          </dd>
          <dd v-else class="empty">未填写</dd>
        </div>
      </dl>
      <p class="up-info-hint">点「编辑资料」或右上角「设置」可修改，保存后这里会立即更新</p>
    </section>

    <!-- ================= TA 的帖子 ================= -->
    <section class="up-sec">
      <h3 class="up-sec-title">
        {{ self ? '我的帖子' : 'TA 的帖子' }}
        <span class="up-sec-count">{{ posts.length }}</span>
      </h3>

      <div v-if="loading" class="up-tip">正在加载…</div>

      <div v-else-if="posts.length" class="up-list">
        <article
          v-for="p in posts"
          :key="p.id"
          class="up-card-item"
          @click="$emit('open-post', p.id)"
        >
          <div class="up-cover">
            <PostMedia v-if="coverOf(p)" :media="coverOf(p)" />
            <div v-else class="up-cover-empty" :style="{ background: tintOf(p) }">
              {{ iconOf(p) }}
            </div>
            <span v-if="(p.media || []).length > 1" class="up-count">
              共 {{ p.media.length }} 张
            </span>
          </div>

          <div class="up-body">
            <span class="up-cat" :style="{ background: colorOf(p) }">{{ nameOf(p) }}</span>
            <h4 class="up-title">{{ p.title || excerpt(p.content, 18) }}</h4>
            <p class="up-excerpt">{{ excerpt(p.content, 46) }}</p>
            <div class="up-meta">
              <span>{{ formatTime(p.createdAt) }}</span>
              <span class="up-stats-inline">
                <em>♥ {{ (p.likes || []).length }}</em>
                <em>💬 {{ (p.comments || []).length }}</em>
              </span>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="up-empty">
        <PetMascot :pet="todayPet" class="empty-cat" />
        <p>{{ self ? '你还没有发过帖子哦~' : 'TA 还没有发过帖子~' }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import PetMascot from './PetMascot.vue'
import PostMedia from './PostMedia.vue'
import { fetchPosts } from '../api/post'
import { addFriend, fetchRelation, acceptFriend } from '../api/friend'
import { excerpt, formatTime } from '../composables/postStore'
import { categoryName, categoryColor, categoryIcon } from '../composables/petData'
import { useTodayPet } from '../composables/dailyPet'
import { parseUserTags } from '../composables/useAuth'

// 空状态吉祥物 = 今日宠物（一天一换，点击可换表情）
const todayPet = useTodayPet()

const props = defineProps({
  // 要展示的人：{ key, name }
  person: { type: Object, required: true },
  // 当前登录用户
  user: { type: Object, default: () => ({}) },
  // 嵌入模式：「我的」tab 直接复用本组件（导航栏已在，返回键回到首页）
  embedded: { type: Boolean, default: false }
})
const emit = defineEmits(['back', 'chat', 'open-post', 'friend-added', 'toast', 'edit-profile', 'open-settings'])

const ownerKey = computed(() => props.user?.username || 'guest')
// 后端 /api/auth/login 返回的 user 没有 id 字段（mock 用户、会话恢复的精简 user 同样没有），
// 因此「本人视角」只能按登录用户名与主页 key 是否一致来判定。
const self = computed(() =>
  !!props.user?.username && !!props.person?.key && props.person.key === ownerKey.value
)

/* ---------------- 我的资料（仅本人可见，直接读响应式 props.user） ---------------- */
const profileEmail = computed(() => String(props.user?.email || '').trim() || '未填写')
const profileBirthday = computed(() => String(props.user?.birthday || '').trim() || '未填写')
const profileTags = computed(() => parseUserTags(props.user?.tags))

const letter = computed(() =>
  String(props.person?.name || props.person?.key || '萌').charAt(0).toUpperCase()
)

/* ---------------- 关系 ---------------- */
const relation = ref('none') // none / pending_out / pending_in / friend
const adding = ref(false)

async function loadRelation() {
  if (self.value || !props.person?.key) {
    relation.value = 'none'
    return
  }
  try {
    const res = await fetchRelation(ownerKey.value, props.person.key)
    relation.value = (res && res.status) || 'none'
  } catch (e) {
    relation.value = 'none'
  }
}

/** 同意对方发来的申请 → 双方成为好友 */
async function onAcceptIncoming() {
  adding.value = true
  try {
    await acceptFriend(ownerKey.value, props.person.key)
    relation.value = 'friend'
    emit('friend-added', { key: props.person.key, name: props.person.name })
    emit('toast', `已和「${props.person.name}」成为好友`)
  } catch (e) {
    emit('toast', (e && e.message) || '操作失败，请稍后再试')
  } finally {
    adding.value = false
  }
}

/** 发送好友申请，等对方通过 */
async function onAddFriend() {
  if (self.value || adding.value) return
  if (relation.value === 'friend' || relation.value === 'pending_out') return
  // 对方已先申请：点「同意」直接建立关系
  if (relation.value === 'pending_in') return onAcceptIncoming()

  adding.value = true
  try {
    const res = await addFriend(
      ownerKey.value,
      { key: props.person.key, name: props.person.name },
      'profile'
    )
    const st = (res && res.relation && res.relation.status) || 'pending'
    relation.value = st === 'friend' ? 'friend' : 'pending_out'
    if (relation.value === 'friend') {
      emit('friend-added', { key: props.person.key, name: props.person.name })
      emit('toast', `已和「${props.person.name}」成为好友`)
    } else {
      emit('toast', `已发送好友申请，等待「${props.person.name}」通过`)
    }
  } catch (e) {
    emit('toast', (e && e.message) || '添加失败，请稍后再试')
  } finally {
    adding.value = false
  }
}

/* ---------------- 帖子 ---------------- */
const posts = ref([])
const loading = ref(false)

async function loadPosts() {
  if (!props.person?.key) {
    posts.value = []
    return
  }
  loading.value = true
  try {
    const res = await fetchPosts({ author: props.person.key })
    posts.value = ((res && res.items) || [])
      .slice()
      .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
  } catch (e) {
    posts.value = []
  } finally {
    loading.value = false
  }
}

function loadAll() {
  loadRelation()
  loadPosts()
}

onMounted(loadAll)
watch(() => props.person?.key, loadAll)

const totalLikes = computed(() =>
  posts.value.reduce((n, p) => n + (p.likes || []).length, 0)
)
const totalComments = computed(() =>
  posts.value.reduce((n, p) => n + (p.comments || []).length, 0)
)
const since = computed(() => {
  if (self.value && props.user?.createdAt) {
    return new Date(props.user.createdAt).toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    })
  }
  // 非本人主页没有公开的加入日期来源，用「未公开」代替空洞的破折号
  return '未公开'
})

/* ---------------- 展示辅助 ---------------- */
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

const TINTS = [
  'linear-gradient(135deg, #ff9ec4 0%, #ffd6e8 100%)',
  'linear-gradient(135deg, #ffc38a 0%, #ffe3c2 100%)',
  'linear-gradient(135deg, #a5d8ff 0%, #d6ecff 100%)',
  'linear-gradient(135deg, #b8e8c8 0%, #dcf7e5 100%)',
  'linear-gradient(135deg, #c9b6f5 0%, #e5dcfc 100%)'
]
function tintFor(key) {
  const s = String(key || '')
  let h = 0
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) % 997
  return TINTS[h % TINTS.length]
}

defineExpose({ reload: loadAll })
</script>

<style scoped>
.uprofile { animation: pop 0.42s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 操作条 ---------- */
.up-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 22px 30px 0;
}
.up-back {
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
.up-back svg { width: 16px; height: 16px; }
.up-back:hover { background: var(--soft); border-color: var(--pink); transform: translateX(-2px); }
/* 右上角「设置」入口：本人视角才显示，弹起 Home 统一持有的设置面板 */
.up-settings {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 7px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.14s ease, background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.up-settings svg { width: 15px; height: 15px; }
.up-settings:hover {
  background: var(--soft);
  border-color: var(--pink);
  color: var(--pink-deep);
  transform: translateY(-1px);
}

/* ---------- 主页封面（宠物主题装饰，全用主题变量，深浅色自适应） ---------- */
.up-banner {
  position: relative;
  display: flex;
  align-items: center;
  overflow: hidden;
  height: 96px;
  margin: 14px 30px 0;
  padding: 0 26px;
  border: 2px solid var(--line);
  border-radius: 20px;
  background: linear-gradient(115deg, var(--soft) 0%, var(--soft-2) 48%, var(--hero-1) 100%);
}
.up-banner-text { position: relative; z-index: 1; display: grid; gap: 5px; }
.up-banner-text strong { font-size: 16px; font-weight: 800; color: var(--pink-deep); }
.up-banner-text span { font-size: 11.5px; font-weight: 700; color: var(--ink-dim); }
.up-banner-cloud {
  position: absolute;
  width: 118px;
  height: 30px;
  border-radius: 999px;
  background: var(--card);
  opacity: 0.45;
}
.up-banner-cloud::before,
.up-banner-cloud::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: inherit;
}
.up-banner-cloud::before { width: 52px; height: 52px; left: 16px; top: -26px; }
.up-banner-cloud::after { width: 36px; height: 36px; left: 64px; top: -16px; }
.cloud-a { right: -20px; top: 34px; }
.cloud-b { right: 104px; top: 58px; width: 84px; height: 22px; opacity: 0.3; }
.up-banner-paw {
  position: absolute;
  width: 48px;
  height: 48px;
  color: var(--pink);
  opacity: 0.32;
}
.paw-a { right: 48px; bottom: 6px; transform: rotate(-16deg); }
.paw-b { right: 150px; top: 8px; width: 30px; height: 30px; opacity: 0.22; transform: rotate(16deg); }
.up-banner-star {
  position: absolute;
  color: var(--pink-deep);
  font-size: 17px;
  font-weight: 800;
  opacity: 0.55;
}
.star-a { right: 210px; top: 16px; }
.star-b { right: 26px; top: 14px; font-size: 12px; opacity: 0.4; }

/* ---------- 个人名片 ---------- */
.up-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
  margin: 14px 30px 0;
  padding: 22px 24px;
  background: linear-gradient(135deg, var(--hero-1) 0%, var(--card) 60%, var(--hero-1) 100%);
  border: 2px solid var(--line);
  border-radius: 24px;
  box-shadow: 0 18px 40px -26px rgba(124, 78, 140, 0.5);
}
/* 名片内整宽一行：访客可见的公开个性标签（位于徽章 / 按钮下方） */
.up-card-tags {
  flex-basis: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 2px;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
}
.up-avatar {
  width: 72px;
  height: 72px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 3px solid var(--edge);
  color: var(--on-accent);
  font-size: 28px;
  font-weight: 800;
  box-shadow: 0 10px 22px -12px rgba(124, 78, 140, 0.7);
}
.up-main { flex: 1; min-width: 0; }
.up-name { margin: 0; font-size: 22px; font-weight: 800; }
.up-id { margin: 3px 0 8px; font-size: 12.5px; color: var(--ink-dim); font-weight: 700; }
.up-badges { display: flex; flex-wrap: wrap; gap: 8px; }
.up-badge {
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--soft-2);
  color: var(--ok);
  font-size: 11.5px;
  font-weight: 800;
}
.up-badge.soft { background: var(--soft); color: var(--ink-soft); }

.up-actions { flex: none; display: flex; gap: 10px; }
.up-btn {
  border: 2px solid transparent;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  padding: 10px 24px;
  border-radius: 999px;
  cursor: pointer;
  transition: filter 0.18s ease, transform 0.14s ease;
}
.up-btn.primary {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: var(--on-accent);
  box-shadow: 0 12px 22px -12px rgba(255, 92, 157, 0.8);
}
.up-btn.primary:hover:not(:disabled) { filter: brightness(1.05); transform: translateY(-1px); }
.up-btn.muted { background: var(--warn-bg); color: var(--warn-ink); cursor: default; }
.up-btn:disabled { opacity: 0.9; }
.up-btn.edit {
  background: var(--soft);
  border-color: var(--pink);
  color: var(--pink-deep);
}
.up-btn.edit:hover { background: var(--soft-2); transform: translateY(-1px); }

/* ---------- 统计 ---------- */
.up-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 14px 30px 0;
}
.up-stat {
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 18px;
  padding: 14px 10px;
  text-align: center;
}
.up-stat strong { display: block; font-size: 20px; font-weight: 800; color: var(--pink-deep); }
.up-stat span { font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }

/* ---------- 我的资料卡（与名片 / 统计同款白卡 + 糖果描边） ---------- */
.up-info-card {
  margin: 14px 30px 0;
  padding: 18px 22px;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 18px;
  box-shadow: 0 14px 30px -24px rgba(124, 78, 140, 0.5);
}
.up-info-head { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.up-info-title { margin: 0; font-size: 15.5px; font-weight: 800; }
.up-new {
  padding: 2px 9px;
  border-radius: 999px;
  background: var(--soft-2);
  color: var(--pink-deep);
  font-size: 10.5px;
  font-weight: 800;
}
.up-info-private { font-size: 11.5px; font-weight: 700; color: var(--ink-dim); }
.up-info-list { margin: 0; display: grid; gap: 10px; }
.up-info-row { display: flex; align-items: flex-start; gap: 14px; }
.up-info-row dt {
  flex: none;
  width: 64px;
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
  line-height: 1.7;
}
.up-info-row dd {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.7;
  word-break: break-all;
}
.up-info-row dd.empty { color: var(--ink-dim); }
.up-info-tags { display: flex; flex-wrap: wrap; gap: 7px; }
.up-info-chip {
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--pink-deep);
  font-size: 12px;
  font-weight: 800;
}
.up-info-hint { margin: 12px 0 0; font-size: 11.5px; font-weight: 700; color: var(--ink-dim); }

/* ---------- 帖子 ---------- */
.up-sec { padding: 22px 30px 34px; }
.up-sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 15.5px;
  font-weight: 800;
}
.up-sec-count {
  padding: 1px 9px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--pink-deep);
  font-size: 11.5px;
}
.up-tip { padding: 22px 0; text-align: center; font-size: 13px; color: var(--ink-soft); }

.up-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.up-card-item {
  display: flex;
  gap: 12px;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 18px;
  padding: 10px;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.up-card-item:hover {
  transform: translateY(-3px);
  border-color: var(--pink);
  box-shadow: 0 16px 28px -16px rgba(124, 78, 140, 0.45);
}
.up-cover {
  position: relative;
  flex: none;
  width: 100px;
  height: 100px;
  border-radius: 13px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.up-cover-empty {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 14px;
  font-weight: 800;
  color: var(--pink-deep);
}
.up-count {
  position: absolute;
  right: 5px;
  bottom: 5px;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: var(--on-accent);
  font-size: 10px;
  font-weight: 800;
}
.up-body { min-width: 0; flex: 1; }
.up-cat {
  display: inline-block;
  padding: 1px 9px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 10.5px;
  font-weight: 800;
}
.up-title {
  margin: 6px 0 3px;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.up-excerpt {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.up-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-dim);
}
.up-stats-inline { display: flex; gap: 10px; }
.up-stats-inline em { font-style: normal; }

/* ---------- 空状态 ---------- */
.up-empty { text-align: center; padding: 26px 16px 8px; color: var(--ink-soft); }
.up-empty p { margin: 6px 0; font-size: 13.5px; }
.empty-cat {
  position: static;
  width: 84px;
  height: 78px;
  margin: 0 auto 10px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .up-bar { padding: 14px 16px 0; }
  .up-banner { height: 80px; margin: 12px 16px 0; padding: 0 18px; border-radius: 16px; }
  .up-banner-text strong { font-size: 14px; }
  .up-banner-text span { font-size: 10.5px; }
  .up-banner-cloud, .up-banner-paw.paw-b, .up-banner-star.star-a { display: none; }
  .up-banner-paw.paw-a { width: 36px; height: 36px; right: 16px; }
  .up-card { margin: 12px 16px 0; padding: 16px; gap: 14px; flex-wrap: wrap; border-radius: 20px; }
  .up-avatar { width: 60px; height: 60px; font-size: 23px; }
  .up-name { font-size: 19px; }
  .up-actions { width: 100%; }
  .up-btn { flex: 1; }
  .up-stats { margin: 12px 16px 0; gap: 8px; }
  .up-stat { padding: 10px 6px; border-radius: 14px; }
  .up-stat strong { font-size: 17px; }
  .up-info-card { margin: 12px 16px 0; padding: 14px 16px; }
  .up-info-row dt { width: 56px; }
  .up-sec { padding: 18px 16px 24px; }
  .up-list { grid-template-columns: 1fr; gap: 10px; }
  .up-cover { width: 86px; height: 86px; }
}
</style>
