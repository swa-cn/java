<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <nav class="nav">
      <div class="nav-inner">
        <div class="nav-brand">
          <CatMascot class="nav-cat" />
          <span class="nav-logo">萌宠星球</span>
        </div>

        <ul class="nav-links">
          <li :class="{ active: activeNav === 'home' }" @click="goNav('home')">首页</li>
          <li :class="{ active: activeNav === 'discover' }" @click="goNav('discover')">发现</li>
          <li :class="{ active: activeNav === 'knowledge' }" @click="goNav('knowledge')">知识课堂</li>
          <li :class="{ active: activeNav === 'message' }" @click="goNav('message')">
            消息
            <span
              v-if="unreadTotal > 0"
              class="nav-badge"
              :title="unreadTotal + ' 条未读消息'"
            >{{ unreadLabel }}</span>
          </li>
          <li :class="{ active: activeNav === 'me' }" @click="goNav('me')">我的</li>
        </ul>

        <div class="nav-user">
          <!-- 通知中心：铃铛 + 未读角标 + 下拉面板 -->
          <div class="nav-bell-wrap">
            <button
              type="button"
              class="nav-bell"
              :class="{ 'has-unread': notifUnread > 0 }"
              title="通知中心"
              aria-label="通知中心"
              @click.stop="toggleNotifPanel"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="M12 3.2c-3.1 0-5.4 2.4-5.4 5.5v3.1c0 .6-.9 2-1.7 2.9-.5.5-.1 1.5.7 1.5h12.8c.8 0 1.2-1 .7-1.5-.8-.9-1.7-2.3-1.7-2.9V8.7c0-3.1-2.3-5.5-5.4-5.5z"
                  fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"
                />
                <path d="M9.8 18.6a2.3 2.3 0 0 0 4.4 0" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
              <span v-if="notifUnread > 0" class="nav-badge">{{ notifUnread > 99 ? '99+' : notifUnread }}</span>
            </button>

            <transition name="notif-pop">
              <div v-if="notifOpen" class="notif-panel" @click.stop>
                <div class="notif-head">
                  <strong>通知中心</strong>
                  <button type="button" class="notif-close" aria-label="关闭" @click="notifOpen = false">×</button>
                </div>

                <div v-if="!notifications.length" class="notif-empty">
                  <span class="notif-empty-cat">ฅ^•ﻌ•^ฅ</span>
                  <p>暂时没有通知，去逛逛动态吧~</p>
                </div>

                <ul v-else class="notif-list">
                  <li
                    v-for="n in notifications"
                    :key="n.id"
                    class="notif-item"
                    :class="{ unread: n.unread }"
                    @click="onNotifClick(n)"
                  >
                    <span class="notif-ico" :class="'t-' + n.type" aria-hidden="true">
                      <!-- 好友申请 -->
                      <svg v-if="n.type === 'friend'" viewBox="0 0 24 24">
                        <circle cx="10" cy="8" r="3.4" fill="none" stroke="currentColor" stroke-width="2" />
                        <path d="M4 19c.6-3.2 3-5 6-5s5.4 1.8 6 5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                        <path d="M17.5 8.5v5M15 11h5" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                      </svg>
                      <!-- 评论 -->
                      <svg v-else-if="n.type === 'comment'" viewBox="0 0 24 24">
                        <path d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v7a2.5 2.5 0 0 1-2.5 2.5H10l-4.2 3.4c-.7.5-1.8 0-1.8-.9V6.5z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round" />
                      </svg>
                      <!-- 点赞 -->
                      <svg v-else viewBox="0 0 24 24">
                        <path d="M12 20s-7-4.4-7-9.3A4 4 0 0 1 12 7.6 4 4 0 0 1 19 10.7c0 4.9-7 9.3-7 9.3z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round" />
                      </svg>
                    </span>
                    <span class="notif-body">
                      <span class="notif-text">
                        <template v-if="n.type === 'friend'"><b>{{ n.fromName }}</b> 请求加你为好友</template>
                        <template v-else-if="n.type === 'comment'">
                          <b>{{ n.fromName }}</b> 评论了你的动态《{{ n.postTitle }}》：<i>{{ n.text }}</i>
                        </template>
                        <template v-else-if="n.count > 0 && n.fromName">
                          <b>{{ n.fromName }}</b>{{ n.count > 1 ? ' 等 ' + n.count + ' 人' : '' }}赞了你的动态《{{ n.postTitle }}》
                        </template>
                        <template v-else-if="n.count > 0">你的动态《{{ n.postTitle }}》收到 <b>{{ n.count }}</b> 个新赞</template>
                        <template v-else>你的动态《{{ n.postTitle }}》累计收到 <b>{{ n.total }}</b> 个赞</template>
                      </span>
                      <time class="notif-time">{{ formatTime(n.at) }}</time>
                    </span>
                    <span v-if="n.unread" class="notif-dot" aria-hidden="true" />
                  </li>
                </ul>
              </div>
            </transition>
          </div>

          <ThemeToggle variant="nav" />
          <button
            type="button"
            class="nav-avatar"
            title="回到首页"
            aria-label="回到首页"
            @click="goNav('home')"
          >{{ avatarLetter }}</button>
          <button
            v-if="user?.isAdmin"
            type="button"
            class="nav-admin"
            title="仅管理员可见"
            @click="$emit('go-admin')"
          >
            <svg class="admin-ico" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" stroke-width="2" />
              <path
                d="M12 2.8l1.2 2.3 2.6.3-1.9 1.9.4 2.6L12 8.7l-2.3 1.2.4-2.6-1.9-1.9 2.6-.3L12 2.8z"
                fill="currentColor" opacity="0.9" transform="translate(0 3.4) scale(0.92)"
              />
              <path
                d="M4.6 15.5l1.7.9.2 1.9 1.9.6.8 1.8 1.9-.1 1.3 1.5 1.3-1.5 1.9.1.8-1.8 1.9-.6.2-1.9 1.7-.9"
                fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" opacity="0.55"
              />
            </svg>
            后台管理
          </button>
          <button type="button" class="nav-logout" @click="$emit('logout')">退出</button>
        </div>
      </div>
    </nav>

    <!-- ============ 用户主页（点头像进入，覆盖在导航下方） ============ -->
    <section v-if="pageUser" class="me-wrap">
      <UserProfile
        :person="pageUser"
        :user="user"
        @back="pageUser = null"
        @chat="openChatWith"
        @open-post="openUserPost"
        @friend-added="onFriendAdded"
        @toast="showToast"
      />
    </section>

    <template v-else>
    <!-- 首页：搜索 + 分类 + 动态 -->
    <template v-if="activeNav === 'home'">
      <!-- 搜索结果页：搜索框 / 热门词 / 品种详情「搜索相关动态」→ 整页跳转 -->
      <SearchPage
        v-if="pageSearch"
        :keyword="pageSearch.keyword"
        @back="closeSearch"
        @search="onSearchFromPage"
        @open-category="openSearchCategory"
        @open-breed="openBreed"
        @open-post="openMyPost"
        @open-user="openUserProfile"
      />

      <!-- 分类详情页：点击分类后整页跳转 -->
      <CategoryPage
        v-else-if="pageCategory"
        :category="pageCategory"
        @back="pageCategory = null"
        @select-breed="openBreed"
      />

      <template v-else>
      <!-- ================= 首屏：左文右猫 对称布局 ================= -->
      <section class="hero">
        <div class="hero-inner">
          <div class="hero-left">
            <span class="hero-tag">MENGCHONG PLANET</span>
            <h1 class="hero-title">
              欢迎回家，{{ displayName }}<span class="tilde">~</span>
            </h1>
            <p class="hero-sub">今天想找点什么萌物呀？</p>

            <form class="search" @submit.prevent="onSearch">
              <svg class="search-ico" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2.4" />
                <line x1="16.2" y1="16.2" x2="21" y2="21" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" />
              </svg>
              <input
                v-model="keyword"
                class="search-input"
                type="text"
                placeholder="搜索萌宠 / 用户 / 动态…"
                @focus="historyOpen = true"
                @blur="onHistoryBlur"
              />

              <!-- 搜索历史下拉：聚焦且输入为空时弹出 -->
              <transition name="hist-pop">
                <div v-if="historyOpen && searchHistory.items.length" class="search-history">
                  <div class="sh-head">
                    <span class="sh-label">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="2" />
                        <path d="M12 7.5V12l3 2" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                      </svg>
                      搜索历史
                    </span>
                    <button type="button" class="sh-clear" @mousedown.prevent="clearHistory">清空</button>
                  </div>
                  <div class="sh-chips">
                    <span v-for="h in searchHistory.items" :key="'sh-' + h" class="sh-chip">
                      <button
                        type="button"
                        class="sh-word"
                        @mousedown.prevent="pickHistory(h)"
                      >{{ h }}</button>
                      <button
                        type="button"
                        class="sh-del"
                        :aria-label="'删除历史 ' + h"
                        title="删除"
                        @mousedown.prevent="removeHistory(h)"
                      >×</button>
                    </span>
                  </div>
                </div>
              </transition>

              <button type="submit" class="search-btn">搜索</button>
            </form>

            <div class="chips">
              <span class="chips-label">热门</span>
              <button
                v-for="c in hotChips"
                :key="c"
                type="button"
                class="chip"
                @click="pickChip(c)"
              >{{ c }}</button>
            </div>
          </div>

          <div class="hero-right">
            <div class="hero-stage">
              <span class="hero-ring" aria-hidden="true"></span>
              <span class="hero-bubble">
                <i class="bubble-line"></i>
                {{ petBubble }}
              </span>
              <!-- 圆环上的漂浮小装饰 -->
              <span class="hero-deco hd-paw1" aria-hidden="true">🐾</span>
              <span class="hero-deco hd-paw2" aria-hidden="true">🐾</span>
              <span class="hero-deco hd-spark1" aria-hidden="true">✨</span>
              <span class="hero-deco hd-spark2" aria-hidden="true">✦</span>
              <span class="hero-deco hd-heart" aria-hidden="true">💗</span>
              <PetMascot :pet="todayPet" class="hero-cat" />
            </div>
          </div>
        </div>

        <!-- 数据条：四等分 完全对称 -->
        <ul class="hero-stats">
          <li v-for="s in heroStats" :key="s.label">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </li>
        </ul>
      </section>

      <!-- ================= 萌宠分类 ================= -->
      <section class="categories">
        <header class="section-head">
          <h2 class="section-title">
            <span class="section-dot">分</span>
            萌宠分类
          </h2>
          <p class="section-desc">五大类萌宠，点开看品种图鉴</p>
        </header>

        <div class="category-grid">
          <article
            v-for="cat in petCategories"
            :key="cat.id"
            class="category-card"
            :style="{ '--cat-color': cat.color }"
            @click="openCategory(cat)"
          >
            <div class="category-media">
              <img class="category-cover" :src="cat.image" :alt="cat.name" loading="lazy" />
              <span class="category-icon">{{ cat.icon }}</span>
            </div>
            <div class="category-info">
              <h3 class="category-name">{{ cat.name }}</h3>
              <p class="category-count">{{ cat.breeds.length }} 个品种</p>
              <div class="category-breeds">
                <span v-for="b in cat.breeds.slice(0, 2)" :key="b.name">{{ b.name }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- ================= 热门搜索（结果统一在独立结果页展示） ================= -->
      <section class="feed hot-search">
        <header class="section-head">
          <h2 class="section-title">
            <span class="section-dot">搜</span>
            热门搜索
          </h2>
          <p class="section-desc">点一下直接看全部相关分类、品种和动态</p>
        </header>

        <div class="chips hot-chips">
          <button
            v-for="c in hotChips"
            :key="'hot-' + c"
            type="button"
            class="chip"
            @click="pickChip(c)"
          >{{ c }}</button>
        </div>
      </section>
      </template>
    </template>

    <!-- 发现：帖子流（列表 → 详情 → 发帖，页内切换） -->
    <section v-else-if="activeNav === 'discover'" class="discover-wrap">
      <DiscoverFeed
        ref="discoverRef"
        :user="user"
        @friend-added="onFriendAdded"
        @open-user="onOpenUser"
        @open-chat="openChatWith"
      />
    </section>

    <!-- 知识课堂：养宠科普 + 搜索；点击文章整页跳转到详情 -->
    <section v-else-if="activeNav === 'knowledge'" class="feed">
      <KnowledgePage
        v-if="!pageArticle"
        @open-breed="onOpenBreedFromKnowledge"
        @open-article="openKnowledgeArticle"
      />
      <KnowledgeDetail
        v-else
        :article="pageArticle"
        @back="closeArticle"
        @open-article="openKnowledgeArticle"
      />
    </section>

    <!-- 消息：好友列表 + 待通过申请 -->
    <section v-else-if="activeNav === 'message'" class="feed">
      <FriendsPage
        ref="friendsRef"
        :user="user"
        @go-discover="goNav('discover')"
        @toast="showToast"
        @view-profile="openUserProfile"
        @messages-changed="refreshUnread"
      />
    </section>

    <!-- 我的：账号信息 + 本人发帖记录 -->
    <section v-else-if="activeNav === 'me'" class="me-wrap">
      <Dashboard
        :user="user"
        @logout="$emit('logout')"
        @open-post="openMyPost"
        @go-discover="goNav('discover')"
      />
    </section>
    </template>

    <!-- 品种详情弹层 -->
    <transition name="fade">
      <div v-if="selectedBreed" class="breed-detail-mask" @click.self="selectedBreed = null">
        <div class="breed-detail">
          <button type="button" class="detail-close" @click="selectedBreed = null" aria-label="关闭">×</button>
          <img class="detail-cover" :src="selectedBreed.image" :alt="selectedBreed.name" />
          <div class="detail-body">
            <span class="detail-cat" :style="{ background: detailCategory?.color }">{{ detailCategory?.name }}</span>
            <h3 class="detail-name">{{ selectedBreed.name }}</h3>
            <p class="detail-desc">{{ selectedBreed.desc }}</p>
            <div class="detail-info">
              <div><dt>性格</dt><dd>{{ selectedBreed.temperament || '友善亲人' }}</dd></div>
              <div><dt>饲养难度</dt><dd>{{ selectedBreed.careLevel || '中等' }}</dd></div>
              <div><dt>适合人群</dt><dd>{{ selectedBreed.suitableFor || '新手友好' }}</dd></div>
            </div>
            <p class="detail-note">后台已维护真实资料时展示实际内容，未维护时显示默认介绍。</p>
            <button type="button" class="detail-search" @click="searchFromBreed(selectedBreed.name)">搜索相关动态</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 轻提示 -->
    <transition name="fade">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import CatMascot from './CatMascot.vue'
import PetMascot from './PetMascot.vue'
import ThemeToggle from './ThemeToggle.vue'
import Dashboard from './Dashboard.vue'
import CategoryPage from './CategoryPage.vue'
import DiscoverFeed from './DiscoverFeed.vue'
import FriendsPage from './FriendsPage.vue'
import KnowledgePage from './KnowledgePage.vue'
import KnowledgeDetail from './KnowledgeDetail.vue'
import SearchPage from './SearchPage.vue'
import UserProfile from './UserProfile.vue'
import { petCategories, loadCategories } from '../composables/petData'
import { petOfTheDay, bubbleOfPet } from '../composables/dailyPet'
import { searchHistory, removeHistory, clearHistory } from '../composables/searchHistory'
import {
  buildNotifications,
  markNotificationsSeen
} from '../composables/notificationStore'
import { formatTime } from '../composables/postStore'
import { fetchPosts } from '../api/post'
import { fetchIncoming } from '../api/friend'
import { fetchConversations } from '../api/message'
import { fetchKnowledgeList } from '../api/knowledge'
import { initRouter, navigate, syncUrl, parseRoute } from '../composables/useRouter'

const props = defineProps({
  user: { type: Object, required: true }
})
defineEmits(['logout', 'go-admin'])

const displayName = computed(
  () => props.user?.nickname || props.user?.username || '朋友'
)
const avatarLetter = computed(
  () => (displayName.value.charAt(0) || '喵').toUpperCase()
)
const ownerKey = computed(() => props.user?.username || 'guest')

/* ---------------- 消息未读红点 ----------------
 * 导航栏「消息」右上角的小红点：有未读私信时显示，数字 = 未读条数（>99 显示 99+）。
 * 未读判定：别人发来的（from !== 自己）且 read 为 false。
 */
const unreadTotal = ref(0)
const unreadLabel = computed(() =>
  unreadTotal.value > 99 ? '99+' : String(unreadTotal.value)
)

async function refreshUnread() {
  try {
    const res = await fetchConversations(ownerKey.value)
    const items = (res && res.items) || []
    unreadTotal.value = items.reduce((n, c) => {
      const list = c.messages || []
      return n + list.filter((m) => !m.read && m.from !== ownerKey.value).length
    }, 0)
  } catch (e) {
    /* 拉取失败时保留上一次的数字，不打扰用户 */
  }
}

// 进页面先查一次；之后定时轮询，别人发来消息时红点能自己冒出来
let unreadTimer = null
/* ---------------- 路由：真实 URL + 浏览器前进/后退 + 刷新直达 ----------------
 * 路由表（轻量 History API，见 composables/useRouter.js）：
 *   /search?kw=xx    搜索结果页
 *   /knowledge/:id   知识文章详情
 *   /post/:id        动态详情（切到「发现」打开）
 * 其余路径一律视作首页。打开详情 = push（后退可用），关闭/切导航 = replace（不攒历史）。
 */
function applyRoute(path) {
  const { name, id, query } = parseRoute(path)
  if (name === 'knowledge' && id) {
    restoreArticle(id)
  } else if (name === 'post' && id) {
    openMyPost(id)
  } else if (name === 'search') {
    const kw = (query.kw || '').trim()
    pageUser.value = null
    pageCategory.value = null
    pageArticle.value = null
    activeNav.value = 'home'
    keyword.value = kw
    pageSearch.value = kw ? { keyword: kw } : null
  } else {
    // 首页：收起详情类页面（导航切换的 syncUrl('/') 也会走到这）
    pageArticle.value = null
    pageSearch.value = null
  }
}

/** 刷新直达 /knowledge/:id：拉文章列表找到对应文章再展示 */
async function restoreArticle(id) {
  pageUser.value = null
  pageSearch.value = null
  pageCategory.value = null
  activeNav.value = 'knowledge'
  try {
    const res = await fetchKnowledgeList()
    const items = (res && (res.items || res.articles)) || []
    pageArticle.value =
      items.find((a) => String(a.id) === String(id)) || null
  } catch (e) {
    pageArticle.value = null
  }
}

onMounted(() => {
  refreshUnread()
  refreshNotifs() // 通知数据源先拉一次
  unreadTimer = setInterval(() => {
    refreshUnread()
    refreshNotifs() // 轮询通知：别人点赞/评论/申请后角标能自己亮
  }, 20000)
  // 分类/品种重新拉一次数据库：后台或手动改库后，刷新首页即可看到最新分类
  loadCategories().catch(() => {})
  // 点空白处关闭通知面板
  document.addEventListener('click', closeNotifPanel)
  // 路由初始化：监听浏览器前进/后退，并按当前 URL 恢复页面（刷新直达）
  initRouter(applyRoute)
  applyRoute(location.pathname + location.search)
})

/* ---------------- 通知中心 ----------------
 * 数据双模式：后端在跑走 /content/posts 与好友接口，没跑自动回落本地 store
 * （api 层 withFallback 统一处理）。通知从「我的动态收到的评论/赞 + 好友申请」推导，
 * 见 notificationStore。打开面板即视为已读；20 秒轮询一次保证角标及时亮。
 */
const notifOpen = ref(false)
const notifPosts = ref([]) // 我的动态（含 likes/comments），通知的数据源
const notifRequests = ref([] // 别人发我的好友申请
)
const notifications = computed(() =>
  buildNotifications(ownerKey.value, notifPosts.value, notifRequests.value)
)
const notifUnread = computed(() => notifications.value.filter((n) => n.unread).length)

/** 拉取通知数据源（我的动态 + 好友申请），失败静默保留旧数据 */
async function refreshNotifs() {
  try {
    const res = await fetchPosts({ author: ownerKey.value })
    notifPosts.value = (res && res.items) || []
  } catch (e) {
    /* 忽略 */
  }
  try {
    const res = await fetchIncoming(ownerKey.value)
    notifRequests.value = (res && res.items) || []
  } catch (e) {
    /* 忽略 */
  }
}

function toggleNotifPanel() {
  notifOpen.value = !notifOpen.value
  if (notifOpen.value) {
    refreshNotifs().finally(() => markNotificationsSeen(notifPosts.value))
  }
}
function closeNotifPanel() {
  notifOpen.value = false
}
function onNotifClick(n) {
  notifOpen.value = false
  if (n.type === 'friend') {
    goNav('message') // 好友申请在「消息」页处理
  } else {
    openMyPost(n.postId)
  }
}

onUnmounted(() => {
  if (unreadTimer) clearInterval(unreadTimer)
  document.removeEventListener('click', closeNotifPanel)
})
watch(ownerKey, () => {
  refreshUnread()
  refreshNotifs()
})

const activeNav = ref('home')
const friendsRef = ref(null)
// 用户主页详情页：非空时覆盖在导航下方展示（点头像进入）
const pageUser = ref(null)
// 知识课堂文章详情页：非空时整页展示该文章（点击文章卡片后跳转）
const pageArticle = ref(null)
function goNav(name) {
  // 切换导航时收起「用户主页」「分类详情页」「搜索结果页」「知识文章详情页」
  pageUser.value = null
  pageSearch.value = null
  pageArticle.value = null
  if (name !== 'home') pageCategory.value = null
  activeNav.value = name
  // URL 同步回首页（replace：导航切换不产生历史记录，浏览器后退留给详情页用）
  syncUrl('/')
  // 回首页时重新拉一次分类：后台/手动改库后能立刻看到
  if (name === 'home') loadCategories().catch(() => {})
  // 每次进入「消息」都重新拉一次好友（可能在帖子里刚加过好友）
  if (name === 'message') {
    nextTick(() => {
      const fr = friendsRef.value
      if (fr && typeof fr.load === 'function') fr.load()
    })
  }
}

const keyword = ref('')
const toast = ref('')
let toastTimer = null
function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

const hotChips = ['布偶猫', '柯基', '小奶狗', '仓鼠', '鹦鹉']

/* ---------- 首屏吉祥物：宠物一天一换 + 点击换表情 ---------- */
const todayPet = computed(() => petOfTheDay(props.user?.username))
const petBubble = computed(() => bubbleOfPet(todayPet.value))

// 首屏数据条（纯展示：分类数取自数据源，其余为运营位）
const heroStats = computed(() => [
  { value: String(petCategories.length), label: '萌宠大类' },
  { value: String(petCategories.reduce((n, c) => n + c.breeds.length, 0)), label: '品种图鉴' },
  { value: '6', label: '今日动态' },
  { value: '5', label: '热门话题' }
])

// 点击分类：整页跳转到品种图鉴页（不在首页下方展开）
const pageCategory = ref(null)
const currentCategory = computed(() => pageCategory.value)
function openCategory(cat) {
  pageSearch.value = null
  pageCategory.value = cat
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 搜索结果页：输入关键词后整页跳转，按「种类」分组展示全部相关结果
const pageSearch = ref(null)

/** 首页搜索框 / 热门词提交：跳到独立结果页（请求与渲染都由 SearchPage 负责） */
function onSearch() {
  const q = keyword.value.trim()
  if (!q) {
    showToast('先输入点关键词喵~')
    return
  }
  historyOpen.value = false
  pageCategory.value = null
  pageSearch.value = { keyword: q }
  navigate('/search?kw=' + encodeURIComponent(q)) // 搜索页带真实 URL：可分享、刷新直达
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/** 关闭搜索结果页：状态收起 + URL 同步回首页（replace，不产生多余历史记录） */
function closeSearch() {
  pageSearch.value = null
  syncUrl('/')
}

/* ---------- 搜索历史（首页搜索框下拉） ---------- */
// searchHistory.items 来自 composables/searchHistory.js，与搜索页共享同一份数据
const historyOpen = ref(false)
function onHistoryBlur() {
  // 延迟一帧，保证点历史词的 mousedown 事件先于 blur 生效（词已用 @mousedown.prevent 接管）
  setTimeout(() => { historyOpen.value = false }, 120)
}
function pickHistory(word) {
  keyword.value = word
  historyOpen.value = false
  onSearch()
}

/** 结果页里换关键词重搜：同步回首页输入框，返回首页时关键词不丢 */
function onSearchFromPage(q) {
  keyword.value = q
}

/** 结果页点「分类」：补全完整分类数据（含品种）后再打开图鉴页 */
function openSearchCategory(c) {
  if (!c || !c.id) return
  const full = petCategories.find((x) => x.id === c.id)
  openCategory(full || { ...c, breeds: [] })
}

// 品种详情（前端展示，不依赖后端）
const selectedBreed = ref(null)
function openBreed(b) {
  selectedBreed.value = b
}

/** 知识课堂「品种速查」：带上分类名/主题色，复用首页品种详情弹层 */
function onOpenBreedFromKnowledge(payload) {
  if (!payload || !payload.breed) return
  selectedBreed.value = {
    ...payload.breed,
    categoryName: payload.category?.name,
    categoryColor: payload.category?.color
  }
}

/** 知识课堂点击文章：整页跳转到知识详情页（带真实 URL，可分享 / 刷新直达） */
function openKnowledgeArticle(article) {
  if (!article) return
  pageArticle.value = article
  navigate('/knowledge/' + encodeURIComponent(article.id))
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/** 关闭知识详情页：状态收起 + URL 同步回首页 */
function closeArticle() {
  pageArticle.value = null
  syncUrl('/')
}

/** 品种详情弹层顶部的分类标签：从分类页进来用当前分类，从搜索结果进来用品种自带字段 */
const detailCategory = computed(() => {
  if (currentCategory.value) return currentCategory.value
  const b = selectedBreed.value
  if (!b) return null
  return { name: b.categoryName || '萌宠', color: b.categoryColor || 'var(--pink)' }
})

function searchFromBreed(name) {
  selectedBreed.value = null
  pageCategory.value = null
  keyword.value = name
  onSearch()
}

function pickChip(c) {
  keyword.value = c
  onSearch()
}

// ------- 发现页：由 Home 负责切换导航，帖子详情/发帖在 DiscoverFeed 内部切换 -------
const discoverRef = ref(null)

/** 「我的」发帖记录 / 搜索结果里点击帖子：切到「发现」并打开该帖详情（带真实 URL） */
async function openMyPost(postId) {
  if (!postId) return
  pageCategory.value = null
  pageSearch.value = null
  activeNav.value = 'discover'
  navigate('/post/' + encodeURIComponent(postId))
  await nextTick() // 等 DiscoverFeed 挂载后再调用它暴露的 openPost
  const feed = discoverRef.value
  if (feed && typeof feed.openPost === 'function') feed.openPost(postId)
}

/** 在帖子里加了好友：消息页的好友列表需要刷新 */
function onFriendAdded() {
  const fr = friendsRef.value
  if (fr && typeof fr.load === 'function') fr.load()
}

/** 从帖子直接和好友私信：切到「消息」并打开与该好友的聊天窗口 */
async function openChatWith(payload) {
  if (!payload || !payload.key) return
  pageCategory.value = null
  activeNav.value = 'message'
  await nextTick()
  const fr = friendsRef.value
  if (fr && typeof fr.openChatWith === 'function') {
    fr.openChatWith({ key: payload.key, name: payload.name })
  }
}

/**
 * 点击作者头像 → 打开 TA 的用户主页详情页。
 * 本人则打开自己的主页（等同「我的」的公开视角）。
 */
async function onOpenUser(payload) {
  if (!payload) return
  openUserProfile({ key: payload.key, name: payload.name })
}

/** 打开用户主页详情页（覆盖在导航下方） */
function openUserProfile(payload) {
  if (!payload || !payload.key) return
  pageCategory.value = null
  pageSearch.value = null
  pageUser.value = { key: payload.key, name: payload.name || payload.key }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/** 在用户主页里点开某篇帖子：回到「发现」并打开详情 */
async function openUserPost(postId) {
  pageUser.value = null
  await openMyPost(postId)
}

</script>

<style scoped>
/* ---------- 容器（与登录卡片同款白卡 + 糖果描边） ---------- */
.home {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1000px;
  background: var(--card);
  border: 4px solid var(--edge);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ---------- 通用：左右对称的内容栅格 ---------- */
.nav-inner,
.hero-inner,
.hero-stats,
.section-head,
.categories,
.feed,
.discover-wrap,
.me-wrap {
  width: 100%;
  max-width: var(--home-max, 900px);
  margin-left: auto;
  margin-right: auto;
}
/* 各区块的水平内边距统一，保证左右完全对称 */
.hero {
  position: relative;
  padding: 30px 30px 0;
  background:
    radial-gradient(70% 120% at 92% 6%, rgba(255, 207, 92, 0.22) 0%, transparent 62%),
    radial-gradient(80% 120% at 6% 100%, rgba(124, 108, 255, 0.16) 0%, transparent 62%),
    linear-gradient(180deg, var(--hero-1) 0%, var(--card) 78%);
  border-bottom: 2px dashed var(--line);
}
/* ---------- 导航栏 ---------- */
.nav {
  padding: 0 30px;
  background: linear-gradient(135deg, #ff93c4 0%, #ff6fa8 40%, #8b7bff 100%);
  color: var(--on-accent);
}
.nav-inner {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 13px 0;
}
.nav-brand { display: flex; align-items: center; gap: 10px; flex: none; }
.nav-logo { white-space: nowrap; }
.nav-cat {
  position: static;
  width: 42px;
  height: 40px;
  margin: 0;
  filter: drop-shadow(0 4px 8px rgba(120, 40, 90, 0.3));
}
.nav-logo {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
  text-shadow: 0 2px 6px rgba(120, 40, 90, 0.25);
}
.nav-links {
  display: flex;
  justify-content: center;
  gap: 6px;
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
}
.nav-links li {
  flex: none; /* 不允许被挤压：空间不够时整行缩小间距，而不是文字竖排换行 */
  white-space: nowrap;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  opacity: 0.92;
  transition: background 0.18s ease, transform 0.12s ease;
}
.nav-links li:hover { background: rgba(255, 255, 255, 0.18); transform: translateY(-1px); }
.nav-links li.active {
  background: var(--card);
  color: var(--pink-deep);
  opacity: 1;
  box-shadow: 0 6px 14px -6px rgba(120, 40, 90, 0.4);
}
/* 未读消息红点：数字 = 未读条数 */
.nav-badge {
  display: inline-grid;
  place-items: center;
  min-width: 18px;
  height: 18px;
  margin-left: 5px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--bad);
  color: var(--on-accent);
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
  vertical-align: middle;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.85);
  animation: badge-pop 0.32s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
@keyframes badge-pop {
  from { transform: scale(0.2); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.nav-user { display: flex; align-items: center; gap: 10px; flex: none; }

/* ---------- 通知中心 ---------- */
.nav-bell-wrap { position: relative; }
.nav-bell {
  position: relative;
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  padding: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  border: 2px solid var(--edge);
  color: var(--on-accent);
  cursor: pointer;
  transition: background 0.18s ease, transform 0.15s ease;
}
.nav-bell:hover { background: rgba(255, 255, 255, 0.38); transform: translateY(-1px); }
.nav-bell svg { width: 19px; height: 19px; }
.nav-bell.has-unread { animation: bell-swing 2.6s ease-in-out infinite; }
@keyframes bell-swing {
  0%, 84%, 100% { transform: rotate(0); }
  88% { transform: rotate(11deg); }
  92% { transform: rotate(-9deg); }
  96% { transform: rotate(5deg); }
}
/* 铃铛自己的角标：叠在按钮右上角（与导航 li 里的 .nav-badge 复用配色） */
.nav-bell .nav-badge {
  position: absolute;
  top: -6px;
  right: -7px;
  margin: 0;
}

.notif-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  z-index: 50;
  width: 336px;
  max-width: 84vw;
  background: var(--card);
  border: 3px solid var(--edge);
  border-radius: 22px;
  box-shadow: var(--shadow);
  overflow: hidden;
}
.notif-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 16px 11px;
  border-bottom: 2px dashed var(--line);
  font-size: 14.5px;
}
.notif-close {
  border: 0;
  background: transparent;
  color: var(--ink-dim);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 8px;
}
.notif-close:hover { color: var(--bad); background: var(--soft); }
.notif-empty { padding: 26px 16px 30px; text-align: center; color: var(--ink-soft); }
.notif-empty-cat { display: block; font-size: 26px; margin-bottom: 8px; }
.notif-empty p { margin: 0; font-size: 13px; }
.notif-list {
  list-style: none;
  margin: 0;
  padding: 6px;
  max-height: 380px;
  overflow-y: auto;
}
.notif-item {
  position: relative;
  display: flex;
  gap: 11px;
  align-items: flex-start;
  padding: 11px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.15s ease;
}
.notif-item:hover { background: var(--soft); }
.notif-item.unread { background: var(--soft-2); }
.notif-item.unread:hover { background: var(--soft); }
.notif-ico {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--on-accent);
}
.notif-ico svg { width: 18px; height: 18px; }
.notif-ico.t-friend { background: linear-gradient(135deg, var(--candy) 0%, var(--candy-2) 100%); }
.notif-ico.t-comment { background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 100%); }
.notif-ico.t-like { background: linear-gradient(135deg, #ff8a5c 0%, var(--bad) 100%); }
.notif-body { flex: 1; min-width: 0; }
.notif-text {
  display: block;
  font-size: 13px;
  line-height: 1.5;
  color: var(--ink);
}
.notif-text b { color: var(--pink-deep); }
.notif-text i {
  font-style: normal;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notif-time { display: block; margin-top: 3px; font-size: 11.5px; color: var(--ink-dim); }
.notif-dot {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--bad);
}
.notif-pop-enter-active, .notif-pop-leave-active { transition: opacity 0.18s ease, transform 0.18s ease; }
.notif-pop-enter-from, .notif-pop-leave-to { opacity: 0; transform: translateY(-8px) scale(0.97); }

@media (max-width: 640px) {
  .notif-panel { right: -46px; }
}

.nav-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.25);
  border: 2px solid var(--edge);
  color: var(--on-accent);
  font-family: inherit;
  font-weight: 800;
  font-size: 14px;
  padding: 0;
  cursor: pointer;
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.18s ease;
}
.nav-avatar:hover { background: rgba(255, 255, 255, 0.45); transform: translateY(-2px) scale(1.06); }
.nav-avatar:active { transform: scale(0.96); }
.nav-logout {
  border: 2px solid rgba(255, 255, 255, 0.7);
  background: transparent;
  color: var(--on-accent);
  font-family: inherit;
  font-weight: 700;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.nav-logout:hover { background: rgba(255, 255, 255, 0.2); }

/* 后台管理入口：与退出按钮同款胶囊，加个齿轮图标区分 */
.nav-admin {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.16);
  color: var(--on-accent);
  font-family: inherit;
  font-weight: 700;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.14s ease;
}
.nav-admin:hover { background: rgba(255, 255, 255, 0.32); transform: translateY(-1px); }
.admin-ico { width: 14px; height: 14px; }

/* ---------- 首屏 Hero：左文右猫 对称 ---------- */
.hero {
  position: relative;
  padding: 30px 30px 0;
  background:
    radial-gradient(70% 120% at 92% 6%, rgba(255, 207, 92, 0.22) 0%, transparent 62%),
    radial-gradient(80% 120% at 6% 100%, rgba(124, 108, 255, 0.16) 0%, transparent 62%),
    linear-gradient(180deg, var(--hero-1) 0%, var(--card) 78%);
  border-bottom: 2px dashed var(--line);
}
.hero-inner {
  display: grid;
  grid-template-columns: 1fr 300px;
  align-items: center;
  gap: 34px;
  padding: 6px 0 26px;
}

.hero-left { min-width: 0; }
.hero-tag {
  display: inline-block;
  padding: 4px 12px;
  margin-bottom: 12px;
  border-radius: 999px;
  background: var(--card);
  border: 2px solid var(--line);
  color: var(--pink-deep);
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 1.6px;
}
.hero-title {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: 0.5px;
}
.tilde {
  color: var(--pink);
  display: inline-block;
  animation: wiggle 1.6s ease-in-out infinite;
  transform-origin: bottom left;
}
.hero-sub { margin: 10px 0 22px; color: var(--ink-soft); font-size: 14.5px; }

.search {
  position: relative; /* 搜索历史下拉的定位基准 */
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 520px;
  background: var(--soft);
  border: 2px solid var(--line);
  border-radius: 999px;
  padding: 6px 6px 6px 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.search:focus-within {
  border-color: var(--pink);
  background: var(--card);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.18);
}
.search-ico { flex: none; width: 22px; height: 22px; color: var(--ink-dim); }
.search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  background: transparent;
  outline: none;
  font-family: inherit;
  font-size: 15px;
  color: var(--ink);
  padding: 10px 4px;
}
.search-input::placeholder { color: var(--ink-dim); }
.search-btn {
  flex: none;
  border: 0;
  border-radius: 999px;
  padding: 11px 26px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  color: var(--on-accent);
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 55%, #ff8a5c 130%);
  box-shadow: 0 10px 20px -8px rgba(255, 92, 157, 0.6), inset 0 -3px 0 rgba(0, 0, 0, 0.08);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.search-btn:hover { transform: translateY(-2px) scale(1.02); filter: brightness(1.04); }
.search-btn:active { transform: scale(0.97); }

/* ---------- 搜索历史下拉 ---------- */
.search-history {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  right: 0;
  z-index: 20;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 12px 14px;
}
.sh-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 9px;
}
.sh-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-soft);
  letter-spacing: 0.5px;
}
.sh-label svg { width: 13px; height: 13px; }
.sh-clear {
  border: 0;
  background: transparent;
  color: var(--ink-dim);
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 999px;
}
.sh-clear:hover { color: var(--bad); background: var(--soft); }
.sh-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.sh-chip {
  display: inline-flex;
  align-items: center;
  background: var(--soft);
  border: 2px solid var(--line);
  border-radius: 999px;
  overflow: hidden;
  transition: border-color 0.15s ease;
}
.sh-chip:hover { border-color: var(--pink); }
.sh-word {
  border: 0;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  padding: 5px 4px 5px 12px;
  cursor: pointer;
}
.sh-word:hover { color: var(--pink-deep); }
.sh-del {
  border: 0;
  background: transparent;
  color: var(--ink-dim);
  font-size: 14px;
  line-height: 1;
  padding: 5px 10px 5px 4px;
  cursor: pointer;
}
.sh-del:hover { color: var(--bad); }

/* 下拉弹出动画 */
.hist-pop-enter-active, .hist-pop-leave-active { transition: opacity 0.16s ease, transform 0.16s ease; }
.hist-pop-enter-from, .hist-pop-leave-to { opacity: 0; transform: translateY(-6px) scale(0.98); }

.chips { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin: 16px 0 0; }
.chips-label {
  font-size: 11.5px;
  font-weight: 800;
  color: var(--ink-dim);
  letter-spacing: 1px;
}
.chip {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.chip:hover { background: var(--soft); border-color: var(--pink); transform: translateY(-1px); }

/* 右栏：猫咪舞台（圆环 + 气泡 + 漂浮装饰都相对同一圆心定位） */
.hero-right {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 252px;
}
.hero-stage {
  position: relative;
  width: 224px;
  height: 224px;
  display: grid;
  place-items: center;
}
.hero-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, rgba(255, 126, 179, 0.2) 0%, rgba(255, 207, 92, 0.16) 55%, transparent 72%);
  border: 2px dashed rgba(255, 126, 179, 0.35);
  animation: spin 26s linear infinite;
}
/* 内圈：反向慢转的黄色虚线环，增加层次 */
.hero-ring::after {
  content: "";
  position: absolute;
  inset: 16px;
  border-radius: 50%;
  border: 2px dashed rgba(255, 207, 92, 0.4);
  animation: spin 18s linear infinite reverse;
}
.hero-cat {
  position: relative;
  z-index: 1;
  /* 重置全局 .mascot 的 absolute 定位残留, 否则猫会贴到视口右缘被裁掉 */
  top: auto;
  left: auto;
  width: 152px;
  height: 142px;
  margin: 0;
  filter: drop-shadow(0 14px 22px rgba(124, 78, 140, 0.28));
  animation: bob 3.2s ease-in-out infinite;
}
.hero-bubble {
  position: absolute;
  top: -17px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--card);
  border: 2px solid var(--line);
  font-size: 12px;
  font-weight: 800;
  color: var(--pink-deep);
  white-space: nowrap;
  box-shadow: 0 8px 18px -10px rgba(124, 78, 140, 0.5);
  animation: bubble-bob 4s ease-in-out infinite;
}
@keyframes bubble-bob {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50%      { transform: translateX(-50%) translateY(-5px); }
}
.hero-bubble .bubble-line {
  position: absolute;
  left: 50%;
  bottom: -9px;
  width: 12px;
  height: 12px;
  margin-left: -6px;
  background: var(--card);
  border-right: 2px solid var(--line);
  border-bottom: 2px solid var(--line);
  transform: rotate(45deg);
  border-radius: 0 0 3px 0;
}

/* 圆环上的漂浮小装饰（--r 为各自基础旋转角） */
.hero-deco {
  position: absolute;
  z-index: 2;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 3px 6px rgba(124, 78, 140, 0.28));
  animation: hero-float 4.4s ease-in-out infinite;
}
@keyframes hero-float {
  0%, 100% { transform: translateY(0) rotate(var(--r, -6deg)); }
  50%      { transform: translateY(-11px) rotate(calc(var(--r, -6deg) + 10deg)); }
}
.hd-paw1   { top: -4px;  right: 0;    font-size: 19px; --r: 16deg; }
.hd-paw2   { top: 30px;  left: -4px;  font-size: 13px; --r: -22deg; opacity: 0.85; animation-delay: -2.2s; }
.hd-spark1 { top: 42%;   left: -12px; font-size: 15px; color: #ffcf5c; animation: twinkle 3s ease-in-out infinite; }
.hd-heart  { bottom: 10px; left: 15%; font-size: 16px; --r: -8deg; animation-delay: -1.4s; }
.hd-spark2 { bottom: 22%; right: -8px; font-size: 12px; color: #ff9ec4; animation: twinkle 3.6s ease-in-out infinite -1.6s; }

/* 数据条：四等分，首尾对齐，视觉完全对称 */
.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  list-style: none;
  margin: 0;
  padding: 0;
  border-top: 2px dashed var(--line);
}
.hero-stats li {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 15px 8px 17px;
  border-left: 2px dashed var(--line);
}
.hero-stats li:first-child { border-left: 0; }
.hero-stats strong { font-size: 19px; font-weight: 800; color: var(--pink-deep); line-height: 1; }
.hero-stats span { font-size: 11.5px; font-weight: 700; color: var(--ink-dim); }

/* ---------- 区块标题（左标题 + 右说明，两端对齐） ---------- */
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 800;
}
.section-desc { margin: 0; font-size: 12px; font-weight: 700; color: var(--ink-dim); }
.section-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink) 0%, var(--candy) 100%);
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
}

/* ---------- 萌宠分类 ---------- */
.categories { padding: 26px 30px 8px; }
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.category-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  text-align: center;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 32px -14px rgba(124, 78, 140, 0.4);
  border-color: var(--cat-color, var(--pink));
}
.category-media { position: relative; }
.category-cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  height: auto;
  object-fit: cover;
  display: block;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.category-icon {
  position: absolute;
  left: 50%;
  bottom: -15px;
  width: 30px;
  height: 30px;
  margin-left: -15px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--cat-color, var(--pink));
  color: var(--on-accent);
  font-size: 13px;
  font-weight: 800;
  border: 2px solid var(--edge);
  box-shadow: 0 5px 12px -4px rgba(120, 40, 90, 0.45);
}
.category-info { padding: 22px 10px 14px; }
.category-name { margin: 0; font-size: 15px; font-weight: 800; }
.category-count { margin: 3px 0 0; font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }
.category-breeds {
  display: flex;
  justify-content: center;
  gap: 5px;
  margin: 9px 0 0;
  padding-top: 9px;
  border-top: 1px dashed var(--line);
}
.category-breeds span {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--ink-soft);
  font-size: 10.5px;
  font-weight: 700;
  white-space: nowrap;
}

/* 品种详情弹层 */
.breed-detail-mask {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(74, 59, 82, 0.32);
  backdrop-filter: blur(3px);
}
.breed-detail {
  position: relative;
  width: 100%;
  max-width: 420px;
  max-height: 92%;
  overflow-y: auto;
  background: var(--card);
  border: 4px solid var(--edge);
  border-radius: 24px;
  box-shadow: var(--shadow);
  animation: pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
.detail-close {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 4px 10px -3px rgba(124, 78, 140, 0.4);
  transition: background 0.18s ease, transform 0.12s ease;
}
.detail-close:hover { background: var(--card); transform: scale(1.06); }
.detail-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
  border-radius: 20px 20px 0 0;
  display: block;
}
.detail-body { padding: 14px 20px 20px; text-align: center; }
.detail-cat {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
  margin-bottom: 8px;
}
.detail-name { margin: 0 0 6px; font-size: 21px; font-weight: 800; }
.detail-desc { margin: 0 0 14px; font-size: 13.5px; line-height: 1.5; color: var(--ink-soft); }
.detail-info {
  display: grid;
  gap: 9px;
  text-align: left;
  background: var(--soft);
  border: 2px dashed var(--line);
  border-radius: 16px;
  padding: 12px 16px;
  margin-bottom: 10px;
}
.detail-info div { display: flex; justify-content: space-between; gap: 12px; }
.detail-info dt { font-size: 12.5px; font-weight: 700; color: var(--ink-dim); }
.detail-info dd { margin: 0; font-size: 12.5px; font-weight: 800; }
.detail-note { margin: 0 0 14px; font-size: 11.5px; color: var(--ink-dim); }
.detail-search {
  width: 100%;
  border: 0;
  border-radius: 999px;
  padding: 12px 16px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  color: var(--on-accent);
  cursor: pointer;
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 55%, #ff8a5c 130%);
  box-shadow: 0 10px 20px -8px rgba(255, 92, 157, 0.6), inset 0 -3px 0 rgba(0, 0, 0, 0.08);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.detail-search:hover { transform: translateY(-2px); filter: brightness(1.04); }
.detail-search:active { transform: scale(0.97); }

/* ---------- 内容区 ---------- */
.feed { padding: 22px 30px 34px; min-height: 140px; }
/* 热门搜索：首页只留入口，结果统一在 SearchPage 展示 */
.hot-search { min-height: 0; }
.hot-search .hot-chips { margin: 14px 0 0; }
.feed-tip,
.empty { text-align: center; color: var(--ink-soft); padding: 24px 10px; }
.empty-cat {
  position: static;
  width: 84px;
  height: 78px;
  margin: 0 auto 10px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}
.empty p { margin: 6px 0; font-size: 14px; }
.empty-sub { font-size: 12px; color: var(--ink-dim); }

.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.card {
  background: var(--soft);
  border: 2px solid var(--line);
  border-radius: 18px;
  padding: 14px;
  text-align: left;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}
.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 26px -12px rgba(124, 78, 140, 0.4);
}
/* 可点击的搜索结果（帖子） */
.card-link { cursor: pointer; border-color: var(--pink); }
.card-enter {
  display: inline-block;
  margin-top: 8px;
  font-size: 11.5px;
  font-weight: 800;
  color: var(--pink-deep);
}

/* 发现页容器（详情/发帖为页内切换） */
.discover-wrap { min-height: 260px; }

.card-cover {
  height: 96px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 34px;
  font-weight: 800;
  color: var(--pink-deep);
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
  margin-bottom: 10px;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}
.card-title { margin: 0 0 4px; font-size: 15px; font-weight: 800; }
.card-summary { margin: 0 0 8px; font-size: 12.5px; color: var(--ink-soft); line-height: 1.4; }
.card-author { font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }

.me-wrap { padding: 22px 0 34px; }

/* ---------- 轻提示 ---------- */
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
/* 中等宽度：导航横向空间紧张，压缩间距保证五个导航项永远单行横排 */
@media (max-width: 900px) {
  .nav { padding: 0 18px; }
  .nav-inner { gap: 10px; }
  .nav-links { gap: 2px; }
  .nav-links li { padding: 7px 11px; font-size: 13.5px; }
  .nav-cat { width: 36px; height: 34px; }
  .nav-logo { font-size: 17px; }
  .nav-user { gap: 8px; }

  .hero-inner { grid-template-columns: 1fr; gap: 18px; padding-bottom: 20px; text-align: center; }
  .hero-left { display: flex; flex-direction: column; align-items: center; }
  .search { margin: 0 auto; }
  .chips { justify-content: center; }
  .hero-right { order: -1; min-height: 208px; }
  .hero-stage { width: 186px; height: 186px; }
  .hero-cat { width: 126px; height: 118px; }
  .hero-bubble { top: -15px; font-size: 11px; }
  .hd-paw1 { font-size: 16px; }
  .hd-spark1 { display: none; }
  .category-grid { grid-template-columns: repeat(3, 1fr); gap: 12px; }
  .cards { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 640px) {
  .nav { padding: 0 16px; }
  .nav-inner { flex-wrap: wrap; gap: 10px 12px; padding: 11px 0; }
  .nav-links { order: 3; flex: 1 1 100%; gap: 2px; }
  .nav-links li { padding: 7px 10px; font-size: 13px; }
  .home { max-width: 100%; border-radius: 24px; }
  .hero { padding: 22px 16px 0; }
  .hero-title { font-size: 25px; }
  .hero-sub { margin: 8px 0 18px; }
  .hero-tag { margin-bottom: 10px; }
  .category-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .categories { padding: 20px 16px 6px; }
  .feed { padding: 18px 16px 28px; }
  .cards { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .hero-stats li { padding: 12px 4px 14px; }
  .hero-stats strong { font-size: 17px; }
  .section-head { flex-direction: column; align-items: flex-start; gap: 4px; }
}
</style>
