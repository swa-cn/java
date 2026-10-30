<template>
  <div class="post-detail">
    <!-- 顶部操作条 -->
    <div class="pd-bar">
      <button type="button" class="pd-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        返回发现
      </button>
      <!-- 删除：两步确认（不用原生 confirm，避免与卡片风格割裂） -->
      <div v-if="canDelete" class="pd-del-wrap">
        <template v-if="!confirmingDelete">
          <button type="button" class="pd-del" @click="confirmingDelete = true">删除</button>
        </template>
        <template v-else>
          <span class="pd-del-ask">删除后无法恢复，确定吗？</span>
          <button type="button" class="pd-del-yes" :disabled="deleting" @click="onDelete">
            {{ deleting ? '删除中…' : '确认删除' }}
          </button>
          <button type="button" class="pd-del-no" :disabled="deleting" @click="confirmingDelete = false">
            取消
          </button>
        </template>
      </div>
    </div>

    <article class="pd-card">
      <!-- 媒体区 -->
      <div v-if="mediaList.length" class="pd-media">
        <template v-if="mediaList.length === 1">
          <div class="pd-media-one">
            <PostMedia :media="mediaList[0]" @zoom="openViewer(0)" />
          </div>
        </template>
        <div v-else class="pd-media-grid" :class="gridClass">
          <div
            v-for="(m, i) in mediaList"
            :key="m.id || m.src || i"
            class="pd-media-cell"
          >
            <PostMedia :media="m" @zoom="openViewer(i)" />
            <span v-if="m.type === 'video'" class="pd-cell-tag">视频</span>
          </div>
        </div>
      </div>

      <div class="pd-body">
        <div class="pd-head">
          <span
            class="pd-avatar"
            :class="{ clickable: canAddFriend }"
            :title="canAddFriend ? '查看 TA 的主页' : post.authorName"
            @click="onAvatarClick"
          >{{ authorLetter }}</span>
          <div class="pd-author">
            <div class="pd-author-name">{{ post.authorName }}</div>
            <div class="pd-author-time">
              {{ formatTime(post.createdAt) }} · {{ categoryName(post.category) }}
            </div>
          </div>

          <!-- 加好友：本人不显示；已是好友 / 我已申请过 时不可重复点 -->
          <button
            v-if="!isSelf"
            type="button"
            class="pd-add"
            :class="friendStateClass"
            :disabled="relation === 'friend' || relation === 'pending_out' || adding"
            @click="onAddFriend"
          >
            {{ friendBtnText }}
          </button>

          <!-- 已是好友时，可直接私信 -->
          <button
            v-if="!isSelf && relation === 'friend'"
            type="button"
            class="pd-msg"
            @click="onMessage"
          >发消息</button>

          <span class="pd-cat" :style="{ background: categoryColor(post.category) }">
            {{ categoryIcon(post.category) }} {{ categoryName(post.category) }}
          </span>
        </div>

        <h2 v-if="post.title" class="pd-title">{{ post.title }}</h2>
        <p class="pd-content">{{ post.content }}</p>

        <div v-if="(post.tags || []).length" class="pd-tags">
          <span v-for="t in post.tags" :key="t" class="pd-tag"># {{ t }}</span>
        </div>

        <!-- 互动条 -->
        <div class="pd-actions">
          <button
            type="button"
            class="pd-like"
            :class="{ active: liked }"
            @click="onLike"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M12 20s-7-4.4-7-9.2A4 4 0 0 1 12 8a4 4 0 0 1 7 2.8C19 15.6 12 20 12 20z"
                :fill="liked ? 'currentColor' : 'none'"
                stroke="currentColor"
                stroke-width="2"
                stroke-linejoin="round"
              />
            </svg>
            {{ liked ? '已点赞' : '点赞' }} {{ (post.likes || []).length }}
          </button>
          <span class="pd-comment-count">💬 {{ (post.comments || []).length }} 条评论</span>
        </div>

        <!-- 评论区 -->
        <section class="pd-comments">
          <h3 class="pd-sec-title">评论</h3>

          <div class="pd-comment-form">
            <input
              v-model="draft"
              class="pd-input"
              type="text"
              maxlength="300"
              placeholder="说点什么吧，友善交流喵~"
              @keyup.enter="submitComment"
            />
            <button type="button" class="pd-send" :disabled="!draft.trim()" @click="submitComment">
              发送
            </button>
          </div>

          <ul v-if="(post.comments || []).length" class="pd-comment-list">
            <li v-for="c in sortedComments" :key="c.id" class="pd-comment">
              <span
                class="pd-c-avatar"
                :class="{ clickable: canFriendOf(c) }"
                :title="canFriendOf(c) ? '查看 TA 的主页' : (c.authorName || '')"
                @click="onCommentAvatar(c)"
              >{{ (c.authorName || '萌').charAt(0) }}</span>
              <div class="pd-c-main">
                <div class="pd-c-head">
                  <span class="pd-c-name">{{ c.authorName }}</span>
                  <span class="pd-c-time">{{ formatTime(c.createdAt) }}</span>

                  <!-- 评论者加好友：本人不显示；按关系展示不同状态 -->
                  <button
                    v-if="canFriendOf(c)"
                    type="button"
                    class="pd-c-add"
                    :class="cState(c).cls"
                    :disabled="cState(c).disabled"
                    @click="onAddFriendOf(c)"
                  >{{ cState(c).text }}</button>
                </div>
                <p class="pd-c-text">{{ c.content }}</p>
              </div>
            </li>
          </ul>
          <p v-else class="pd-no-comment">还没有评论，来抢沙发~</p>
        </section>
      </div>
    </article>

    <!-- 大图查看（Teleport 到 body：避开父级 overflow/transform 造成的裁剪） -->
    <Teleport to="body">
      <transition name="fade">
        <div v-if="viewerIndex >= 0" class="pd-viewer" @click.self="viewerIndex = -1">
          <button type="button" class="pd-viewer-close" @click="viewerIndex = -1">×</button>
          <button
            v-if="viewerItems.length > 1"
            type="button"
            class="pd-viewer-nav prev"
            :disabled="viewerIndex === 0"
            @click="stepViewer(-1)"
          >‹</button>
          <img class="pd-viewer-img" :src="viewerUrl" alt="大图" />
          <button
            v-if="viewerItems.length > 1"
            type="button"
            class="pd-viewer-nav next"
            :disabled="viewerIndex === viewerItems.length - 1"
            @click="stepViewer(1)"
          >›</button>
          <div v-if="viewerItems.length > 1" class="pd-viewer-dots">
            {{ viewerIndex + 1 }} / {{ viewerItems.length }}
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import PostMedia from './PostMedia.vue'
import { categoryName, categoryColor, categoryIcon } from '../composables/petData'
import { formatTime, getPost, isLiked } from '../composables/postStore'
import { resolveMediaUrl } from '../composables/mediaStore'
import { deletePost, likePost, commentPost } from '../api/post'
import { addFriend, fetchRelation, acceptFriend } from '../api/friend'

const props = defineProps({
  post: { type: Object, required: true },
  userKey: { type: String, default: 'guest' },
  userName: { type: String, default: '萌宠用户' },
  canDelete: { type: Boolean, default: false }
})
const emit = defineEmits(['back', 'deleted', 'toast', 'friend-added', 'open-user', 'open-chat'])

const draft = ref('')
const confirmingDelete = ref(false)
const deleting = ref(false)

const mediaList = computed(() => props.post.media || [])
const authorLetter = computed(() => (props.post.authorName || '萌').charAt(0).toUpperCase())
const liked = computed(() => isLiked(props.post, props.userKey))
const sortedComments = computed(() =>
  [...(props.post.comments || [])].sort((a, b) => a.createdAt - b.createdAt)
)

// 多图时的网格样式（2 张并排 / 3 张以上九宫格）
const gridClass = computed(() => {
  const n = mediaList.value.length
  if (n === 2) return 'cols-2'
  if (n === 3) return 'cols-3'
  return 'cols-many'
})

async function onLike() {
  try {
    const res = await likePost(props.post.id, props.userKey)
    if (res && res.post) syncFrom(res.post)
  } catch (e) {
    emit('toast', (e && e.message) || '操作失败，请稍后再试')
  }
}

/** 把服务端（或本地 store）返回的最新点赞/评论同步到当前视图对象 */
function syncFrom(p) {
  if (Array.isArray(p.likes)) props.post.likes = p.likes
  if (Array.isArray(p.comments)) props.post.comments = p.comments
}

async function submitComment() {
  const text = draft.value.trim()
  if (!text) return
  try {
    const res = await commentPost(props.post.id, text, {
      author: props.userKey,
      authorName: props.userName
    })
    if (res && res.post) syncFrom(res.post)
    draft.value = ''
    emit('toast', '评论成功~')
  } catch (e) {
    emit('toast', (e && e.message) || '评论失败，请稍后再试')
  }
}

async function onDelete() {
  deleting.value = true
  try {
    await deletePost(props.post.id, props.userKey)
    emit('deleted')
  } catch (e) {
    emit('toast', '删除失败，请稍后再试')
  } finally {
    deleting.value = false
    confirmingDelete.value = false
  }
}

/* ---------------- 好友 ---------------- */
// 作者的稳定标识：优先用帖子里的 author（真实用户名），否则用昵称兜底
const authorKey = computed(() => props.post.author || '')
const isSelf = computed(() => !!authorKey.value && authorKey.value === props.userKey)
const canAddFriend = computed(() => !isSelf.value && !!authorKey.value)

// 'none' | 'pending_out'（我发的，等对方通过）| 'pending_in'（对方发我的）| 'friend'
const relation = ref('none')
const adding = ref(false)

async function loadRelation() {
  if (!canAddFriend.value) {
    relation.value = 'none'
    return
  }
  try {
    const res = await fetchRelation(props.userKey, authorKey.value)
    relation.value = (res && res.status) || 'none'
  } catch (e) {
    relation.value = 'none'
  }
}

watch([authorKey, () => props.userKey], loadRelation, { immediate: true })

// 评论列表变化（新增评论 / 切换帖子）时，刷新评论者的关系状态
watch(
  () => (props.post.comments || []).map((c) => c.id).join(','),
  loadCommentRelations,
  { immediate: true }
)

const friendBtnText = computed(() => {
  if (adding.value) return '发送中…'
  if (relation.value === 'friend') return '✓ 已是好友'
  if (relation.value === 'pending_out') return '等待通过'
  if (relation.value === 'pending_in') return '同意申请'
  return '+ 加好友'
})
const friendStateClass = computed(() => ({
  done: relation.value === 'friend',
  waiting: relation.value === 'pending_out' || relation.value === 'pending_in'
}))

async function onAddFriend() {
  if (!canAddFriend.value || adding.value) return
  if (relation.value === 'friend' || relation.value === 'pending_out') return
  adding.value = true
  try {
    // 对方已先申请：此时点击等价于「同意」
    if (relation.value === 'pending_in') {
      await acceptFriend(props.userKey, authorKey.value)
      relation.value = 'friend'
      emit('friend-added', { key: authorKey.value, name: props.post.authorName })
      emit('toast', `已和「${props.post.authorName || authorKey.value}」成为好友`)
      return
    }
    // 常规：发送申请，等对方在「消息 → 申请」里通过
    const res = await addFriend(
      props.userKey,
      { key: authorKey.value, name: props.post.authorName || authorKey.value },
      'post'
    )
    relation.value = (res && res.relation && res.relation.status) || 'pending'
    if (relation.value === 'pending') relation.value = 'pending_out'
    if (relation.value === 'friend') {
      // 对方此前已申请过，直接建立了关系
      emit('friend-added', { key: authorKey.value, name: props.post.authorName })
      emit('toast', `已和「${props.post.authorName || authorKey.value}」成为好友`)
    } else {
      emit('toast', `已发送好友申请，等待「${props.post.authorName || authorKey.value}」通过`)
    }
  } catch (e) {
    emit('toast', (e && e.message) || '添加失败，请稍后再试')
  } finally {
    adding.value = false
  }
}

/** 点头像：非本人则通知上层跳到 TA 的主页；本人则回首页 */
function onAvatarClick() {
  if (isSelf.value) {
    emit('open-user', { key: props.userKey, self: true })
    return
  }
  emit('open-user', {
    key: authorKey.value,
    name: props.post.authorName || authorKey.value,
    self: false
  })
}

/** 已是好友时：直接和作者私信 */
function onMessage() {
  emit('open-chat', {
    key: authorKey.value,
    name: props.post.authorName || authorKey.value
  })
}

/* ---------------- 评论区：给评论者加好友 ---------------- */
// 每个评论者的关系状态，key = 评论者的用户名
const commentRelations = reactive({})

/** 评论者的稳定标识：优先用 author（用户名），否则用昵称兜底 */
function keyOfComment(c) {
  return (c && (c.author || c.authorName)) || ''
}
/** 能不能给这条评论的人加好友（不是自己、有标识） */
function canFriendOf(c) {
  const k = keyOfComment(c)
  return !!k && k !== props.userKey
}

/** 批量拉取所有评论者的关系状态 */
async function loadCommentRelations() {
  const list = props.post.comments || []
  for (const c of list) {
    const k = keyOfComment(c)
    if (!k || k === props.userKey) continue
    try {
      const res = await fetchRelation(props.userKey, k)
      commentRelations[k] = (res && res.status) || 'none'
    } catch (e) {
      commentRelations[k] = 'none'
    }
  }
}

/** 按钮展示状态 */
function cState(c) {
  const k = keyOfComment(c)
  const st = commentRelations[k] || 'none'
  if (st === 'friend') return { text: '✓ 好友', cls: 'done', disabled: true }
  if (st === 'pending_out') return { text: '等待通过', cls: 'waiting', disabled: true }
  if (st === 'pending_in') return { text: '同意申请', cls: 'action', disabled: false }
  return { text: '+ 加好友', cls: 'action', disabled: false }
}

async function onAddFriendOf(c) {
  const k = keyOfComment(c)
  if (!k || k === props.userKey) return
  const name = c.authorName || k
  const st = commentRelations[k] || 'none'
  if (st === 'friend' || st === 'pending_out') return
  try {
    // 对方先申请过 → 点「同意」直接建立关系
    if (st === 'pending_in') {
      await acceptFriend(props.userKey, k)
      commentRelations[k] = 'friend'
      emit('friend-added', { key: k, name })
      emit('toast', `已和「${name}」成为好友`)
      return
    }
    const res = await addFriend(props.userKey, { key: k, name }, 'comment')
    const newSt = (res && res.relation && res.relation.status) || 'pending'
    commentRelations[k] = newSt === 'friend' ? 'friend' : 'pending_out'
    if (commentRelations[k] === 'friend') {
      emit('friend-added', { key: k, name })
      emit('toast', `已和「${name}」成为好友`)
    } else {
      emit('toast', `已向「${name}」发送好友申请，等待对方通过`)
    }
  } catch (e) {
    emit('toast', (e && e.message) || '添加失败，请稍后再试')
  }
}

/** 点评论者头像 → 打开 TA 的主页 */
function onCommentAvatar(c) {
  const k = keyOfComment(c)
  if (!canFriendOf(c)) return
  emit('open-user', { key: k, name: c.authorName || k, self: false })
}

/* ---------------- 大图查看 ---------------- */
const viewerIndex = ref(-1)
const viewerItems = computed(() => mediaList.value.filter((m) => m.type === 'image'))
const viewerUrls = ref([])
const viewerUrl = computed(() => viewerUrls.value[viewerIndex.value] || '')

async function loadViewerUrls() {
  const list = []
  for (const m of viewerItems.value) {
    list.push(m.src || (await resolveMediaUrl(m.id)))
  }
  viewerUrls.value = list
}

function openViewer(mediaIdx) {
  const target = mediaList.value[mediaIdx]
  if (!target || target.type !== 'image') return
  const idx = viewerItems.value.indexOf(target)
  viewerIndex.value = idx < 0 ? 0 : idx
}

function stepViewer(delta) {
  const next = viewerIndex.value + delta
  if (next >= 0 && next < viewerItems.value.length) viewerIndex.value = next
}

watch(viewerIndex, (i) => {
  if (i >= 0 && !viewerUrls.value.length) loadViewerUrls()
})

// 帖子被移除（例如别处删除）时兜底
watch(
  () => getPost(props.post.id),
  (p) => {
    if (!p) emit('back')
  }
)
</script>

<style scoped>
.post-detail { animation: pop 0.42s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

/* ---------- 操作条 ---------- */
.pd-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 22px 30px 0;
}
.pd-back {
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
.pd-back svg { width: 16px; height: 16px; }
.pd-back:hover { background: var(--soft); border-color: var(--pink); transform: translateX(-2px); }
.pd-del {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--bad);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 800;
  padding: 7px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}
.pd-del:hover { background: var(--bad); color: var(--on-accent); border-color: transparent; }

/* 删除二次确认 */
.pd-del-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.pd-del-ask { font-size: 12px; font-weight: 700; color: var(--ink-soft); }
.pd-del-yes,
.pd-del-no {
  border: 2px solid transparent;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 800;
  padding: 7px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: filter 0.18s ease, background 0.18s ease;
}
.pd-del-yes { background: var(--bad); color: var(--on-accent); }
.pd-del-yes:hover:not(:disabled) { filter: brightness(1.06); }
.pd-del-no { background: var(--soft); color: var(--ink-soft); }
.pd-del-no:hover:not(:disabled) { background: var(--soft); }
.pd-del-yes:disabled,
.pd-del-no:disabled { opacity: 0.65; cursor: not-allowed; }

/* ---------- 卡片 ---------- */
.pd-card {
  margin: 14px 30px 34px;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 18px 40px -20px rgba(124, 78, 140, 0.35);
}

/* ---------- 媒体 ---------- */
.pd-media { background: var(--soft); }
.pd-media-one { height: 320px; }
.pd-media-grid { display: grid; gap: 6px; padding: 6px; }
.pd-media-grid.cols-2 { grid-template-columns: repeat(2, 1fr); }
.pd-media-grid.cols-3 { grid-template-columns: repeat(3, 1fr); }
.pd-media-grid.cols-many { grid-template-columns: repeat(3, 1fr); }
.pd-media-cell {
  position: relative;
  height: 168px;
  border-radius: 12px;
  overflow: hidden;
}
.pd-media-grid.cols-2 .pd-media-cell { height: 230px; }
.pd-cell-tag {
  position: absolute;
  right: 7px;
  top: 7px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: var(--on-accent);
  font-size: 10.5px;
  font-weight: 800;
  pointer-events: none;
}

/* ---------- 正文 ---------- */
.pd-body { padding: 16px 22px 22px; }
.pd-head { display: flex; align-items: center; gap: 10px; }
.pd-avatar {
  width: 38px;
  height: 38px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink) 0%, var(--candy) 100%);
  color: var(--on-accent);
  font-weight: 800;
  font-size: 15px;
}
.pd-avatar.clickable { cursor: pointer; transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.2s ease; }
.pd-avatar.clickable:hover {
  transform: translateY(-2px) scale(1.06);
  box-shadow: 0 8px 16px -8px rgba(255, 92, 157, 0.8);
}
.pd-author-name { font-size: 14.5px; font-weight: 800; }
.pd-author-time { font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }

/* 加好友按钮 */
.pd-add {
  margin-left: auto;
  flex: none;
  border: 2px solid transparent;
  border-radius: 999px;
  padding: 6px 14px;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 800;
  color: var(--on-accent);
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  box-shadow: 0 8px 16px -10px rgba(255, 92, 157, 0.8);
  transition: filter 0.18s ease, transform 0.14s ease;
}
.pd-add:hover:not(:disabled) { filter: brightness(1.05); transform: translateY(-1px); }
.pd-add.done {
  background: var(--soft-2);
  color: var(--ok);
  box-shadow: none;
  cursor: default;
}
.pd-add.waiting {
  background: var(--warn-bg);
  color: var(--warn-ink);
  box-shadow: none;
  cursor: default;
}
.pd-add:disabled { opacity: 0.9; }

/* 已是好友时的私信入口 */
.pd-msg {
  margin-left: auto;
  flex: none;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 800;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.14s ease;
}
.pd-msg:hover { background: var(--soft); border-color: var(--pink); transform: translateY(-1px); }

.pd-cat {
  margin-left: 0;
  flex: none;
  padding: 3px 12px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 11.5px;
  font-weight: 800;
}
.pd-title { margin: 16px 0 8px; font-size: 21px; font-weight: 800; line-height: 1.35; }
.pd-content {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.75;
  color: var(--ink);
  white-space: pre-wrap;
  word-break: break-word;
}
.pd-tags { display: flex; flex-wrap: wrap; gap: 8px; margin: 14px 0 0; }
.pd-tag {
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--pink-deep);
  font-size: 12px;
  font-weight: 700;
}

/* ---------- 互动 ---------- */
.pd-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 18px 0 4px;
  padding-top: 16px;
  border-top: 1px dashed var(--line);
}
.pd-like {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink-soft);
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  padding: 8px 18px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease;
}
.pd-like svg { width: 17px; height: 17px; }
.pd-like:hover { border-color: var(--pink); color: var(--pink-deep); transform: translateY(-1px); }
.pd-like.active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: var(--on-accent);
  box-shadow: 0 10px 20px -10px rgba(255, 92, 157, 0.7);
}
.pd-comment-count { font-size: 12.5px; font-weight: 700; color: var(--ink-dim); }

/* ---------- 评论 ---------- */
.pd-comments { margin-top: 20px; }
.pd-sec-title { margin: 0 0 12px; font-size: 15px; font-weight: 800; }
.pd-comment-form { display: flex; gap: 8px; }
.pd-input {
  flex: 1;
  min-width: 0;
  border: 2px solid var(--line);
  border-radius: 999px;
  padding: 11px 16px;
  font-family: inherit;
  font-size: 13.5px;
  color: var(--ink);
  background: var(--soft);
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.pd-input:focus { border-color: var(--pink); box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.16); background: var(--card); }
.pd-send {
  flex: none;
  border: 0;
  border-radius: 999px;
  padding: 0 22px;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--on-accent);
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  box-shadow: 0 10px 18px -10px rgba(255, 92, 157, 0.7);
  transition: filter 0.2s ease, transform 0.14s ease;
}
.pd-send:hover:not(:disabled) { filter: brightness(1.05); transform: translateY(-1px); }
.pd-send:disabled { background: var(--soft); color: var(--ink-dim); box-shadow: none; cursor: not-allowed; }

.pd-comment-list { list-style: none; margin: 16px 0 0; padding: 0; display: grid; gap: 14px; }
.pd-comment { display: flex; gap: 10px; }
.pd-c-avatar {
  width: 32px;
  height: 32px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--soft);
  color: var(--pink-deep);
  font-size: 13px;
  font-weight: 800;
}
.pd-c-avatar.clickable { cursor: pointer; transition: transform 0.16s ease, box-shadow 0.2s ease; }
.pd-c-avatar.clickable:hover {
  transform: translateY(-1px) scale(1.08);
  box-shadow: 0 6px 12px -6px rgba(255, 92, 157, 0.8);
}
.pd-c-main { flex: 1; min-width: 0; background: var(--soft); border-radius: 16px; padding: 9px 14px; }
.pd-c-head { display: flex; align-items: center; gap: 8px; }
.pd-c-name { font-size: 12.5px; font-weight: 800; }
.pd-c-time { font-size: 11px; color: var(--ink-dim); font-weight: 700; }

/* 评论者加好友按钮 */
.pd-c-add {
  margin-left: auto;
  flex: none;
  border: 2px solid transparent;
  border-radius: 999px;
  padding: 3px 11px;
  font-family: inherit;
  font-size: 11.5px;
  font-weight: 800;
  cursor: pointer;
  transition: filter 0.18s ease, background 0.18s ease;
}
.pd-c-add.action {
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%);
  color: var(--on-accent);
  box-shadow: 0 6px 12px -8px rgba(255, 92, 157, 0.9);
}
.pd-c-add.action:hover { filter: brightness(1.06); }
.pd-c-add.done { background: var(--soft-2); color: var(--ok); cursor: default; }
.pd-c-add.waiting { background: var(--warn-bg); color: var(--warn-ink); cursor: default; }
.pd-c-add:disabled { opacity: 0.9; }
.pd-c-text { margin: 4px 0 0; font-size: 13px; line-height: 1.55; word-break: break-word; }
.pd-no-comment { margin: 14px 0 0; font-size: 12.5px; color: var(--ink-dim); font-weight: 700; }

/* ---------- 大图查看 ---------- */
.pd-viewer {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 22, 36, 0.86);
  backdrop-filter: blur(4px);
  padding: 40px 24px;
}
.pd-viewer-img {
  max-width: 92%;
  max-height: 86%;
  border-radius: 18px;
  box-shadow: 0 30px 60px -20px rgba(0, 0, 0, 0.6);
  background: var(--card);
}
.pd-viewer-close {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--ink);
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  cursor: pointer;
}
.pd-viewer-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 46px;
  height: 46px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.86);
  color: var(--ink);
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  cursor: pointer;
}
.pd-viewer-nav:disabled { opacity: 0.35; cursor: not-allowed; }
.pd-viewer-nav.prev { left: 20px; }
.pd-viewer-nav.next { right: 20px; }
.pd-viewer-dots {
  position: absolute;
  bottom: 26px;
  left: 50%;
  transform: translateX(-50%);
  padding: 5px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 800;
}
.fade-enter-active,
.fade-leave-active { transition: opacity 0.22s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .pd-bar { padding: 14px 16px 0; }
  .pd-card { margin: 12px 16px 24px; border-radius: 20px; }
  .pd-media-one { height: 220px; }
  .pd-media-cell { height: 112px; }
  .pd-media-grid.cols-2 .pd-media-cell { height: 150px; }
  .pd-body { padding: 14px 16px 18px; }
  .pd-comment-form { flex-wrap: wrap; }
  .pd-send { padding: 12px 22px; }
}
</style>
