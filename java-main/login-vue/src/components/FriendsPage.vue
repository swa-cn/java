<template>
  <div class="friends">
    <!-- ================= 聊天窗口（点好友后进入） ================= -->
    <template v-if="chatting">
      <div class="chat">
        <div class="chat-bar">
          <button type="button" class="chat-back" @click="closeChat">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            返回
          </button>
          <span
            class="chat-avatar clickable"
            :style="{ background: tintFor(chatting.peerKey) }"
            title="查看 TA 的主页"
            @click="openProfile(chatting)"
          >
            {{ letterOf({ name: chatting.peerName }) }}
          </span>
          <div class="chat-who">
            <div class="chat-name clickable" @click="openProfile(chatting)">
              {{ chatting.peerName }}
            </div>
            <div class="chat-status">好友 · 私信</div>
          </div>
          <button type="button" class="fr-btn" @click="onRemoveFriend(chatting)">删除好友</button>
        </div>

        <div ref="scrollRef" class="chat-body">
          <div v-if="!chatMessages.length" class="chat-empty">
            <PetMascot :pet="todayPet" class="empty-cat" />
            <p>和「{{ chatting.peerName }}」还没有聊过，说点什么吧~</p>
          </div>
          <ul v-else class="chat-list">
            <li
              v-for="m in chatMessages"
              :key="m.id"
              class="bubble-row"
              :class="{ mine: m.from === ownerKey }"
            >
              <span class="bubble-avatar" :style="{ background: tintFor(m.from) }">
                {{ String(m.fromName || m.from || '萌').charAt(0).toUpperCase() }}
              </span>
              <div class="bubble-wrap">
                <div class="bubble">{{ m.text }}</div>
                <div class="bubble-time">{{ formatChatTime(m.createdAt) }}</div>
              </div>
            </li>
          </ul>
        </div>

        <div class="chat-input">
          <input
            v-model="draft"
            class="chat-field"
            type="text"
            maxlength="500"
            placeholder="输入消息，回车发送…"
            @keyup.enter="onSend"
          />
          <button type="button" class="chat-send" :disabled="!draft.trim()" @click="onSend">
            发送
          </button>
        </div>
      </div>
    </template>

    <!-- ================= 列表视图 ================= -->
    <template v-else>
      <header class="fr-head">
        <div>
          <h2 class="fr-title">
            <span class="fr-dot">友</span>
            消息中心
            <span v-if="isLocal" class="mode-badge" :title="localReason">本地模式</span>
          </h2>
          <p class="fr-sub">
            共 {{ friends.length }} 位好友、{{ conversations.length }} 个会话
          </p>
        </div>
        <button type="button" class="fr-refresh" :disabled="loading" @click="load">
          {{ loading ? '刷新中…' : '刷新' }}
        </button>
      </header>

      <!-- ================= 找人：按账号 / 昵称搜索加好友 ================= -->
      <section class="fr-search">
        <div class="fr-search-box">
          <svg class="fr-search-ico" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2.4" />
            <line x1="16.2" y1="16.2" x2="21" y2="21" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
          </svg>
          <input
            v-model="keyword"
            class="fr-search-input"
            type="text"
            placeholder="搜索用户名 / 昵称，添加好友…"
            @keyup.enter="onSearch"
          />
          <button type="button" class="fr-search-btn" :disabled="searching" @click="onSearch">
            {{ searching ? '搜索中' : '搜索' }}
          </button>
        </div>

        <!-- 搜索结果 -->
        <ul v-if="searchResults.length" class="fr-list search">
          <li v-for="u in searchResults" :key="'s-' + u.key" class="fr-item">
            <span class="fr-avatar" :style="{ background: tintFor(u.key) }">{{ letterOf(u) }}</span>
            <div class="fr-main">
              <div class="fr-name">{{ u.name }}</div>
              <div class="fr-meta">@{{ u.key }}</div>
            </div>
            <span v-if="u.status === 'friend'" class="fr-tag done">✓ 已是好友</span>
            <button
              v-else-if="u.status === 'pending_out'"
              type="button"
              class="fr-btn muted"
              disabled
            >等待通过</button>
            <button
              v-else-if="u.status === 'pending_in'"
              type="button"
              class="fr-btn primary small"
              @click="onAccept(u)"
            >同意</button>
            <button
              v-else
              type="button"
              class="fr-btn primary small"
              :disabled="addingKey === u.key"
              @click="onAddFromSearch(u)"
            >{{ addingKey === u.key ? '发送中…' : '+ 加好友' }}</button>
          </li>
        </ul>
        <p v-else-if="searched && !searching" class="fr-search-empty">
          没有找到匹配的用户，换个关键词试试（用户名或昵称）
        </p>
      </section>

      <header class="fr-head sub">
        <div>
          <h3 class="fr-sub-title">我的消息</h3>
        </div>
      </header>

      <!-- 视图切换 -->
      <div class="fr-tabs" role="tablist">
        <button
          type="button"
          class="fr-tab"
          :class="{ active: tab === 'messages' }"
          @click="tab = 'messages'"
        >
          私信
          <span v-if="conversations.length" class="fr-tab-count">{{ conversations.length }}</span>
        </button>
        <button
          type="button"
          class="fr-tab"
          :class="{ active: tab === 'friends' }"
          @click="tab = 'friends'"
        >
          好友
          <span v-if="friends.length" class="fr-tab-count">{{ friends.length }}</span>
        </button>
        <button
          type="button"
          class="fr-tab"
          :class="{ active: tab === 'requests' }"
          @click="tab = 'requests'"
        >
          申请
          <span v-if="incoming.length" class="fr-tab-count warn">{{ incoming.length }}</span>
        </button>
      </div>

      <!-- ---------- 私信列表 ---------- -->
      <section v-if="tab === 'messages'" class="fr-sec">
        <div v-if="loading && !conversations.length" class="fr-tip">正在加载私信…</div>

        <ul v-else-if="conversations.length" class="fr-list">
          <li
            v-for="c in conversations"
            :key="c.peerKey"
            class="fr-item clickable"
            @click="openChat(c)"
          >
            <span class="fr-avatar" :style="{ background: tintFor(c.peerKey) }">
              {{ letterOf({ name: c.peerName }) }}
            </span>
            <div class="fr-main">
              <div class="fr-name">{{ c.peerName }}</div>
              <div class="fr-meta fr-last">{{ lastText(c) }}</div>
            </div>
            <div class="fr-side">
              <span class="fr-time">{{ formatChatTime(c.updatedAt) }}</span>
              <span v-if="unreadOf(c)" class="fr-unread">{{ unreadOf(c) }}</span>
            </div>
          </li>
        </ul>

        <div v-else class="fr-empty">
          <PetMascot :pet="todayPet" class="empty-cat" />
          <p>还没有聊天记录哦。点好友头像开始聊天，或先搜索添加个好友吧~</p>
          <button type="button" class="fr-btn primary" @click="tab = 'friends'">看看好友</button>
        </div>
      </section>

      <!-- ---------- 好友列表 ---------- -->
      <section v-else-if="tab === 'friends'" class="fr-sec">
        <div v-if="loading && !friends.length" class="fr-tip">正在加载好友…</div>

        <ul v-else-if="friends.length" class="fr-list">
          <li v-for="r in friends" :key="r.key" class="fr-item">
            <span
              class="fr-avatar clickable"
              :style="{ background: tintFor(r.key) }"
              title="查看 TA 的主页"
              @click="openProfile(r)"
            >{{ letterOf(r) }}</span>
            <div class="fr-main clickable" @click="openProfile(r)">
              <div class="fr-name">{{ r.name }}</div>
              <div class="fr-meta">
                来自{{ fromLabel(r.from) }} · {{ formatDay(r.createdAt) }} 成为好友
              </div>
            </div>
            <button type="button" class="fr-btn primary small" @click="openChatWith(r)">发消息</button>
            <button type="button" class="fr-btn" @click="onRemoveFriend(r)">删除</button>
          </li>
        </ul>

        <div v-else class="fr-empty">
          <PetMascot :pet="todayPet" class="empty-cat" />
          <p>还没有好友哦，用上面的搜索框找人，或去帖子里加同好吧~</p>
          <button type="button" class="fr-btn primary" @click="$emit('go-discover')">去逛发现</button>
        </div>
      </section>

      <!-- ---------- 好友申请 ---------- -->
      <section v-else class="fr-sec">
        <!-- 待我处理 -->
        <h3 class="fr-sec-title">
          待我处理
          <span class="fr-sec-count">{{ incoming.length }}</span>
        </h3>
        <ul v-if="incoming.length" class="fr-list compact">
          <li v-for="r in incoming" :key="'in-' + r.key" class="fr-item">
            <span class="fr-avatar" :style="{ background: tintFor(r.key) }">{{ letterOf(r) }}</span>
            <div class="fr-main">
              <div class="fr-name">{{ r.name }}</div>
              <div class="fr-meta">请求添加你为好友 · {{ formatDay(r.createdAt) }}</div>
            </div>
            <button type="button" class="fr-btn primary small" @click="onAccept(r)">同意</button>
            <button type="button" class="fr-btn ghost" @click="onReject(r)">拒绝</button>
          </li>
        </ul>
        <p v-else class="fr-tip plain">暂时没有新的好友申请</p>

        <!-- 我发出的 -->
        <h3 class="fr-sec-title mt">
          我发出的
          <span class="fr-sec-count">{{ pending.length }}</span>
        </h3>
        <ul v-if="pending.length" class="fr-list compact">
          <li v-for="r in pending" :key="'out-' + r.key" class="fr-item">
            <span class="fr-avatar" :style="{ background: tintFor(r.key) }">{{ letterOf(r) }}</span>
            <div class="fr-main">
              <div class="fr-name">{{ r.name }}</div>
              <div class="fr-meta">已发送申请，等待对方通过 · {{ formatDay(r.createdAt) }}</div>
            </div>
            <button type="button" class="fr-btn ghost" @click="onRemoveFriend(r)">撤回</button>
          </li>
        </ul>
        <p v-else class="fr-tip plain">你没有待通过的申请</p>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import PetMascot from './PetMascot.vue'
import { useTodayPet } from '../composables/dailyPet'
import {
  fetchFriends,
  fetchPending,
  fetchIncoming,
  addFriend,
  acceptFriend,
  removeFriend,
  searchUsers,
  friendRuntime
} from '../api/friend'
import {
  fetchConversations,
  sendMessage,
  markConversationRead,
  messageRuntime
} from '../api/message'
import { unreadCount } from '../composables/messageStore'

const props = defineProps({
  user: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['go-discover', 'toast', 'view-profile'])

const ownerKey = computed(() => props.user?.username || 'guest')
const userName = computed(() => props.user?.nickname || props.user?.username || '我')
// 空状态吉祥物 = 今日宠物（一天一换，点击可换表情）
const todayPet = useTodayPet()

const tab = ref('messages')
const friends = ref([])
const pending = ref([])   // 我发出的
const incoming = ref([])  // 别人发我的
const conversations = ref([])
const loading = ref(false)

const isLocal = computed(() => friendRuntime.localMode || messageRuntime.localMode)
const localReason = computed(() => messageRuntime.reason || friendRuntime.reason)

async function load() {
  loading.value = true
  try {
    const [f, p, i, c] = await Promise.all([
      fetchFriends(ownerKey.value, 'friend'),
      fetchPending(ownerKey.value),
      fetchIncoming(ownerKey.value),
      fetchConversations(ownerKey.value)
    ])
    friends.value = (f && f.items) || []
    pending.value = (p && p.items) || []
    incoming.value = (i && i.items) || []
    conversations.value = (c && c.items) || []
  } catch (e) {
    friends.value = []
    pending.value = []
    incoming.value = []
    conversations.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(ownerKey, load)

/* ---------------- 找人 / 加好友 ---------------- */
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)
const searched = ref(false)
const addingKey = ref('')

async function onSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    searchResults.value = []
    searched.value = false
    return
  }
  searching.value = true
  searched.value = true
  try {
    const res = await searchUsers(ownerKey.value, kw)
    searchResults.value = (res && res.items) || []
  } catch (e) {
    searchResults.value = []
    emit('toast', (e && e.message) || '搜索失败，请稍后再试')
  } finally {
    searching.value = false
  }
}

async function onAddFromSearch(u) {
  addingKey.value = u.key
  try {
    await addFriend(ownerKey.value, { key: u.key, name: u.name }, 'search')
    u.status = 'pending_out'
    emit('toast', `已向「${u.name}」发送好友申请，等待对方通过`)
    await load()
  } catch (e) {
    emit('toast', (e && e.message) || '发送失败，请稍后再试')
  } finally {
    addingKey.value = ''
  }
}

/** 同意别人发来的申请 → 双方成为好友 */
async function onAccept(r) {
  try {
    await acceptFriend(ownerKey.value, r.key)
    emit('toast', `已和「${r.name}」成为好友`)
    await load()
  } catch (e) {
    emit('toast', (e && e.message) || '操作失败，请稍后再试')
  }
}

/** 拒绝别人发来的申请 */
async function onReject(r) {
  try {
    await removeFriend(ownerKey.value, r.key)
    emit('toast', '已拒绝该申请')
    await load()
  } catch (e) {
    emit('toast', (e && e.message) || '操作失败，请稍后再试')
  }
}

/* ---------------- 删除好友 / 撤回 ---------------- */
async function onRemoveFriend(r) {
  try {
    await removeFriend(ownerKey.value, r.key)
    emit('toast', r.status === 'pending' ? '已撤回申请' : '已删除好友')
    if (chatting.value && chatting.value.peerKey === r.key) closeChat()
    await load()
  } catch (e) {
    emit('toast', (e && e.message) || '操作失败，请稍后再试')
  }
}

/* ---------------- 聊天 ---------------- */
const chatting = ref(null)
const draft = ref('')
const scrollRef = ref(null)

const chatMessages = computed(() => {
  const c = conversations.value.find((x) => x.peerKey === chatting.value?.peerKey)
  return (c && c.messages) || []
})

function openChatWith(r) {
  openChat({ peerKey: r.key, peerName: r.name })
}

/** 点好友头像 / 昵称：打开 TA 的主页详情页 */
function openProfile(r) {
  emit('view-profile', { key: r.key || r.peerKey, name: r.name || r.peerName })
}

async function openChat(c) {
  chatting.value = { peerKey: c.peerKey, peerName: c.peerName }
  tab.value = 'messages'
  try {
    await markConversationRead(ownerKey.value, c.peerKey)
  } catch (e) {
    /* 已读失败不影响聊天 */
  }
  await load()
  scrollToBottom()
}

function closeChat() {
  chatting.value = null
  draft.value = ''
  load()
}

async function onSend() {
  const text = draft.value.trim()
  if (!text || !chatting.value) return
  try {
    await sendMessage(
      ownerKey.value,
      chatting.value.peerKey,
      chatting.value.peerName,
      text,
      userName.value
    )
    draft.value = ''
    await load()
    scrollToBottom()
  } catch (e) {
    emit('toast', (e && e.message) || '发送失败，请稍后再试')
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = scrollRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function unreadOf(c) {
  return unreadCount(ownerKey.value, c.peerKey)
}
function lastText(c) {
  const list = c.messages || []
  const last = list[list.length - 1]
  if (!last) return '还没有消息'
  const prefix = last.from === ownerKey.value ? '我：' : ''
  return prefix + last.text
}

/* ---------------- 展示辅助 ---------------- */
function letterOf(r) {
  return String(r.name || r.key || '萌').charAt(0).toUpperCase()
}
function fromLabel(from) {
  if (from === 'post') return '帖子'
  if (from === 'comment') return '评论'
  if (from === 'search') return '搜索'
  if (from === 'profile') return '主页'
  return '手动添加'
}
function formatDay(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}
function formatChatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  if (sameDay) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
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

defineExpose({ load, openChatWith })
</script>

<style scoped>
.friends { animation: pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 头部 ---------- */
.fr-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 24px 30px 4px;
}
.fr-head.sub { padding: 20px 30px 0; }
.fr-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 800;
}
.fr-sub-title { margin: 0; font-size: 15.5px; font-weight: 800; }
.fr-dot {
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
.fr-sub { margin: 0; font-size: 13px; color: var(--ink-soft); }
.mode-badge {
  margin-left: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  background: #fff4e2;
  color: #b8792a;
  font-size: 11px;
  font-weight: 800;
}
.fr-refresh {
  margin-left: auto;
  flex: none;
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 700;
  padding: 7px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.fr-refresh:hover:not(:disabled) { background: #fff5f9; border-color: var(--pink); }
.fr-refresh:disabled { opacity: 0.6; cursor: default; }

/* ---------- 找人搜索 ---------- */
.fr-search { padding: 16px 30px 0; }
.fr-search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 999px;
  padding: 6px 8px 6px 16px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.fr-search-box:focus-within {
  border-color: var(--pink);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.16);
}
.fr-search-ico { width: 18px; height: 18px; flex: none; color: var(--ink-dim); }
.fr-search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
}
.fr-search-btn {
  flex: none;
  border: 0;
  border-radius: 999px;
  padding: 8px 20px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  transition: filter 0.18s ease;
}
.fr-search-btn:hover:not(:disabled) { filter: brightness(1.05); }
.fr-search-btn:disabled { opacity: 0.7; cursor: default; }
.fr-search-empty {
  margin: 12px 0 0;
  font-size: 12.5px;
  color: var(--ink-soft);
  text-align: center;
}
.fr-list.search { margin-top: 12px; }

/* ---------- 视图切换 ---------- */
.fr-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 30px 0;
}
.fr-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 2px solid var(--line);
  background: #fff;
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 7px 18px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.fr-tab:hover { background: #fff5f9; border-color: var(--pink); transform: translateY(-1px); }
.fr-tab.active {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.6);
}
.fr-tab-count {
  padding: 0 7px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.28);
  font-size: 11px;
}
.fr-tab:not(.active) .fr-tab-count { background: #fff0f6; }
.fr-tab-count.warn { background: var(--bad); color: #fff; }

/* ---------- 区块 ---------- */
.fr-sec { padding: 16px 30px 0; }
.fr-sec:last-of-type { padding-bottom: 34px; }
.fr-sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  font-size: 14.5px;
  font-weight: 800;
}
.fr-sec-title.mt { margin-top: 22px; }
.fr-sec-count {
  padding: 1px 9px;
  border-radius: 999px;
  background: #fff0f6;
  color: var(--pink-deep);
  font-size: 11.5px;
}

/* ---------- 列表 ---------- */
.fr-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.fr-list.compact { gap: 8px; }
.fr-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 18px;
  padding: 12px 16px;
  transition: border-color 0.18s ease, box-shadow 0.2s ease, transform 0.14s ease;
}
.fr-item.clickable { cursor: pointer; }
.fr-item:hover {
  border-color: var(--pink);
  box-shadow: 0 14px 26px -18px rgba(124, 78, 140, 0.45);
}
.fr-item.clickable:hover { transform: translateY(-2px); }
.fr-avatar {
  width: 42px;
  height: 42px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
}
.fr-avatar.clickable { cursor: pointer; transition: transform 0.16s ease; }
.fr-avatar.clickable:hover { transform: scale(1.08); }
.fr-main { flex: 1; min-width: 0; }
.fr-name {
  font-size: 14px;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.fr-meta { margin-top: 2px; font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }
.fr-last {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--ink-soft);
}
.fr-side {
  flex: none;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}
.fr-time { font-size: 11px; color: var(--ink-dim); font-weight: 700; }
.fr-unread {
  min-width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--bad);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
}
.fr-tag {
  flex: none;
  padding: 5px 13px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}
.fr-tag.done { background: #eaf9f0; color: #2f9e5f; }

.fr-btn {
  flex: none;
  border: 2px solid #ffd6de;
  background: #fff;
  color: var(--bad);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 800;
  padding: 6px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, border-color 0.18s ease;
}
.fr-btn:hover:not(:disabled) { background: var(--bad); color: #fff; border-color: transparent; }
.fr-btn.ghost { border-color: var(--line); color: var(--ink-soft); }
.fr-btn.ghost:hover { background: #f6eef4; color: var(--ink); border-color: transparent; }
.fr-btn.primary {
  border-color: transparent;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: #fff;
  box-shadow: 0 10px 20px -10px rgba(255, 92, 157, 0.7);
}
.fr-btn.primary:hover:not(:disabled) { filter: brightness(1.05); }
.fr-btn.primary.small { padding: 6px 15px; font-size: 12.5px; }
.fr-btn.muted { background: #fff4e2; color: #b8792a; border-color: transparent; cursor: default; }

.fr-tip { padding: 22px 0; text-align: center; font-size: 13px; color: var(--ink-soft); }
.fr-tip.plain { padding: 6px 0 0; text-align: left; font-size: 12.5px; }

/* ---------- 空状态 ---------- */
.fr-empty { text-align: center; padding: 26px 16px 8px; color: var(--ink-soft); }
.fr-empty p { margin: 6px 0 14px; font-size: 13.5px; line-height: 1.6; }
.empty-cat {
  position: static;
  width: 84px;
  height: 78px;
  margin: 0 auto 10px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}

/* ---------- 聊天窗口 ---------- */
.chat {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 150px);
  min-height: 420px;
  padding: 20px 30px 24px;
}
.chat-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--line);
}
.chat-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 2px solid var(--line);
  background: #fff;
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 7px 15px 7px 11px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.14s ease, background 0.18s ease, border-color 0.18s ease;
}
.chat-back svg { width: 16px; height: 16px; }
.chat-back:hover { background: #fff5f9; border-color: var(--pink); transform: translateX(-2px); }
.chat-avatar {
  width: 40px;
  height: 40px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 15.5px;
  font-weight: 800;
}
.chat-who { flex: 1; min-width: 0; }
.chat-name { font-size: 15px; font-weight: 800; }
.chat-name.clickable { cursor: pointer; display: inline-block; }
.chat-name.clickable:hover { color: var(--pink-deep); }
.chat-avatar.clickable { cursor: pointer; transition: transform 0.16s ease; }
.chat-avatar.clickable:hover { transform: scale(1.08); }
.fr-main.clickable { cursor: pointer; }
.fr-main.clickable:hover .fr-name { color: var(--pink-deep); }
.chat-status { font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 4px;
  background: #fdf6fb;
  border-radius: 18px;
  margin: 14px 0;
}
.chat-empty { text-align: center; color: var(--ink-soft); padding: 30px 16px; }
.chat-empty p { margin: 4px 0; font-size: 13.5px; }
.chat-list { list-style: none; margin: 0; padding: 0 14px; display: grid; gap: 14px; }
.bubble-row { display: flex; gap: 9px; align-items: flex-end; }
.bubble-row.mine { flex-direction: row-reverse; }
.bubble-avatar {
  width: 30px;
  height: 30px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 12.5px;
  font-weight: 800;
}
.bubble-wrap { max-width: 68%; }
.bubble {
  padding: 9px 14px;
  border-radius: 16px 16px 16px 4px;
  background: #fff;
  border: 2px solid var(--line);
  font-size: 13.5px;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
}
.bubble-row.mine .bubble {
  border-radius: 16px 16px 4px 16px;
  border-color: transparent;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: #fff;
}
.bubble-time {
  margin-top: 4px;
  font-size: 10.5px;
  color: var(--ink-dim);
  font-weight: 700;
}
.bubble-row.mine .bubble-time { text-align: right; }

.chat-input { display: flex; gap: 8px; }
.chat-field {
  flex: 1;
  min-width: 0;
  border: 2px solid var(--line);
  border-radius: 999px;
  padding: 12px 18px;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
  background: #fdf6fb;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.chat-field:focus {
  border-color: var(--pink);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.16);
  background: #fff;
}
.chat-send {
  flex: none;
  border: 0;
  border-radius: 999px;
  padding: 0 24px;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  box-shadow: 0 10px 18px -10px rgba(255, 92, 157, 0.7);
  transition: filter 0.2s ease, transform 0.14s ease;
}
.chat-send:hover:not(:disabled) { filter: brightness(1.05); transform: translateY(-1px); }
.chat-send:disabled { background: #f0e6ee; color: var(--ink-dim); box-shadow: none; cursor: not-allowed; }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .fr-head { padding: 18px 16px 4px; flex-wrap: wrap; }
  .fr-head.sub { padding: 16px 16px 0; }
  .fr-refresh { margin-left: 0; }
  .fr-search { padding: 12px 16px 0; }
  .fr-tabs { padding: 12px 16px 0; }
  .fr-sec { padding: 14px 16px 0; }
  .fr-sec:last-of-type { padding-bottom: 24px; }
  .fr-item { padding: 10px 12px; border-radius: 16px; flex-wrap: wrap; }
  .fr-avatar { width: 38px; height: 38px; font-size: 14.5px; }
  .chat { padding: 14px 16px 18px; height: calc(100vh - 190px); }
  .chat-bar { flex-wrap: wrap; }
  .bubble-wrap { max-width: 78%; }
}
</style>
