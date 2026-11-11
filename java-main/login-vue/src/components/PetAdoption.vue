<template>
  <div class="adopt">
    <!-- 资源条 -->
    <div class="res-bar">
      <div class="res-item">
        <span class="res-ico">🌱</span>
        <span class="res-num">{{ petGame.nutrients }}</span>
        <span class="res-label">养分</span>
      </div>
      <div class="res-item">
        <span class="res-ico">🍖</span>
        <span class="res-num">{{ petGame.food }}</span>
        <span class="res-label">食物</span>
      </div>
      <div class="res-item">
        <span class="res-ico">🥚</span>
        <span class="res-num">{{ myEggs.length }}</span>
        <span class="res-label">待孵蛋</span>
      </div>
      <div class="res-item">
        <span class="res-ico">🐾</span>
        <span class="res-num">{{ myPets.length }}</span>
        <span class="res-label">宠物</span>
      </div>
    </div>

    <!-- 分区切换 -->
    <div class="tabs">
      <button :class="{ on: tab === 'adopt' }" @click="tab = 'adopt'">领养</button>
      <button :class="{ on: tab === 'mine' }" @click="tab = 'mine'">
        我的宠物<span v-if="petGame.pets.length" class="tab-badge">{{ petGame.pets.length }}</span>
      </button>
      <button :class="{ on: tab === 'task' }" @click="tab = 'task'">任务</button>
      <button v-if="friends.length" :class="{ on: tab === 'gift' }" @click="tab = 'gift'">
        好友赠送<span v-if="petGame.gifts.count" class="tab-badge">{{ petGame.gifts.count }}/3</span>
      </button>
    </div>

    <!-- ========== 领养 ========== -->
    <section v-if="tab === 'adopt'" class="panel">
      <p class="panel-tip">挑一只喜欢的毛孩子领养回家吧～领养后会得到一枚蛋，攒够养分就能孵化。</p>
      <div class="pet-grid">
        <article
          v-for="t in petTypes"
          :key="t.id"
          class="pet-card"
          :style="{ '--c': t.color }"
        >
          <div class="pet-emoji">{{ t.emoji }}</div>
          <h3 class="pet-name">{{ t.name }}</h3>
          <p class="pet-desc">{{ t.desc }}</p>
          <p class="pet-cost">孵化需 🌱 {{ t.hatchCost }} 养分</p>
          <button class="adopt-btn" @click="onAdopt(t.id)">领养</button>
        </article>
      </div>
    </section>

    <!-- ========== 我的宠物 ========== -->
    <section v-else-if="tab === 'mine'" class="panel">
      <p v-if="!petGame.pets.length" class="empty">
        还没有宠物哦，去「领养」挑一只吧 🐾
      </p>

      <div v-else class="my-pets">
        <article
          v-for="p in petGame.pets"
          :key="p.id"
          class="my-pet"
          :class="{ hatched: p.status === 'hatched' }"
          :style="{ '--c': typeOf(p).color }"
        >
          <!-- 蛋 -->
          <div v-if="p.status === 'egg'" class="egg-box">
            <div class="egg">🥚</div>
            <h3 class="pet-name">{{ p.name }}</h3>
            <p class="egg-need">
              需 🌱 {{ typeOf(p).hatchCost }} 养分孵化
            </p>
            <p class="egg-have">现有养分 {{ petGame.nutrients }}</p>
            <button
              class="hatch-btn"
              :disabled="petGame.nutrients < typeOf(p).hatchCost"
              @click="onHatch(p.id)"
            >
              {{ petGame.nutrients < typeOf(p).hatchCost ? '养分不足' : '孵化' }}
            </button>
            <p v-if="petGame.nutrients < typeOf(p).hatchCost" class="hint">
              去做任务或领好友赠送攒养分 →
            </p>
          </div>

          <!-- 已孵化 -->
          <div v-else class="pet-box">
            <div
              class="pet-emoji big"
              :class="{ pop: popId === p.id }"
              @click="onInteract(p.id)"
              :title="'摸摸 ' + p.name"
            >{{ typeOf(p).emoji }}</div>
            <h3 class="pet-name">
              {{ p.name }}
              <button class="rename" title="改名" @click="onRename(p)">✎</button>
            </h3>

            <div class="stat">
              <span class="stat-label">亲密度</span>
              <div class="bar"><i :style="{ width: p.intimacy + '%' }"></i></div>
              <span class="stat-val">{{ p.intimacy }}</span>
            </div>
            <div class="stat">
              <span class="stat-label">饱食度</span>
              <div class="bar sat"><i :style="{ width: p.satiety + '%' }"></i></div>
              <span class="stat-val">{{ p.satiety }}</span>
            </div>

            <div class="actions">
              <button
                class="act interact"
                :disabled="!canInteractToday"
                @click="onInteract(p.id)"
              >摸摸 🤚<span v-if="!canInteractToday" class="mini">（今日已满）</span></button>
              <button
                class="act feed"
                :disabled="petGame.food <= 0"
                @click="onFeed(p.id)"
              >
                喂食 🍖<span v-if="petGame.food <= 0" class="mini">（无食物）</span>
              </button>
              <button
                class="act play"
                @click="openFunJump(typeOf(p).emoji)"
              >🎮 陪它跳</button>
            </div>
            <p class="hint">
              今日摸摸剩余 <b>{{ 5 - petGame.interact.count }}</b> 次 · 点宠物身体也能摸摸
            </p>
          </div>
        </article>
      </div>
    </section>

    <!-- ========== 任务 ========== -->
    <section v-else-if="tab === 'task'" class="panel">
      <p class="panel-tip">点「去做」打开对应的小互动，<b>真正完成操作</b>后任务才会标记完成，再去「领取」奖励：养分（每次 5~10 点）或食物（每次 1 份）。「发布一条动态」和「给好友动态点赞」会跳转到导航栏的<b>发现</b>页，真实发帖 / 点赞后自动完成。</p>
      <ul class="task-list">
        <li v-for="t in petGame.tasks" :key="t.id" class="task" :class="{ done: t.done, ready: t.completed && !t.done }">
          <div class="task-main">
            <h4 class="task-title">{{ t.title }}</h4>
            <p class="task-desc">{{ t.desc }}</p>
          </div>
          <div class="task-reward">
            <span class="reward-tag">
              {{ t.rewardType === 'food' ? '🍖 食物 ×1' : '🌱 养分 5~10' }}
            </span>
            <button
              v-if="!t.completed && !t.done"
              class="task-btn go"
              @click="openTask(t)"
            >去做 →</button>
            <button
              v-else-if="t.completed && !t.done"
              class="task-btn claim"
              @click="onTaskClaim(t.id)"
            >领取</button>
            <button v-else class="task-btn" disabled>已完成</button>
          </div>
        </li>
      </ul>
    </section>

    <!-- ========== 好友赠送 ========== -->
    <section v-else-if="tab === 'gift'" class="panel">
      <template v-if="friends.length">
        <p class="panel-tip">
          每天最多领取 <b>3</b> 次好友赠送，每次 +🌱10 养分 +🍖1 食物。当前已领
          <b>{{ petGame.gifts.count }}/3</b>。
        </p>
        <ul class="gift-list">
          <li v-for="f in friends" :key="f.key" class="gift">
            <span class="gift-ava">{{ f.name.charAt(0) }}</span>
            <div class="gift-main">
              <h4 class="gift-name">{{ f.name }}</h4>
              <p class="gift-sub">赠送你 🌱10 + 🍖1</p>
            </div>
            <button
              class="gift-btn"
              :disabled="!!petGame.gifts.claimed[f.key] || !canGiftToday"
              @click="onGift(f.key)"
            >
              {{ petGame.gifts.claimed[f.key] ? '已领取' : '领取' }}
            </button>
          </li>
        </ul>
        <p v-if="!canGiftToday" class="hint">今日赠送次数已用完，明天再来吧～</p>
      </template>
      <p v-else class="panel-tip empty">暂无好友，添加好友后即可领取好友赠送～</p>
    </section>

    <!-- 轻提示 -->
    <transition name="toast">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>

    <!-- ========== 任务互动弹窗：必须真实做完操作才算完成 ========== -->
    <transition name="modal">
      <div v-if="showTaskModal" class="modal-mask" @click.self="closeTaskModal">
        <div class="modal">
          <button class="modal-close" @click="closeTaskModal">×</button>

          <!-- 签到 -->
          <template v-if="activeTask && activeTask.id === 'sign'">
            <div class="modal-emoji">📅</div>
            <h3 class="modal-title">每日签到打卡</h3>
            <p class="modal-sub">每天点亮 1 格即完成当日打卡，打卡后就能领取奖励～</p>
            <div class="sign-card">
              <button
                v-for="n in 7"
                :key="n"
                class="sign-day"
                :class="{ on: signLit[n - 1] }"
                :disabled="signLit[n - 1] || !canSignDay(n)"
                @click="signClick(n)"
              >{{ signLit[n - 1] ? '✓' : '第' + n + '天' }}</button>
            </div>
            <button v-if="!signedToday" class="modal-btn" disabled>已点亮 {{ signCount }}/7</button>
            <button v-else class="modal-btn" @click="signDoneClick">
              {{ signCount >= 7 ? '本周打卡完成，开始新一周 🎉' : '今日打卡完成，去领取奖励 🎁' }}
            </button>
          </template>

          <!-- 阅读小知识 -->
          <template v-else-if="activeTask && activeTask.id === 'read'">
            <div class="modal-emoji">📖</div>
            <h3 class="modal-title">阅读萌宠小知识</h3>
            <p class="modal-sub">今日份 · {{ dailyFact.title }}</p>
            <div class="read-box">
              <p v-for="(line, i) in dailyFact.lines" :key="i">{{ line }}</p>
            </div>
            <label class="read-check">
              <input type="checkbox" v-model="readChecked" /> 我已认真阅读以上内容
            </label>
            <button class="modal-btn" :disabled="!readChecked" @click="finishTask">完成阅读</button>
          </template>

          <!-- 小测验 -->
          <template v-else-if="activeTask && activeTask.id === 'quiz'">
            <div class="modal-emoji">🧠</div>
            <h3 class="modal-title">萌宠小测验</h3>
            <p class="modal-sub">答对下面这道题即可完成任务：</p>
            <p class="quiz-q">{{ dailyQuiz.q }}</p>
            <div class="quiz-opts">
              <button
                v-for="o in dailyQuiz.opts"
                :key="o.k"
                class="quiz-opt"
                :class="{
                  pick: quizPicked === o.k,
                  right: quizPicked === o.k && o.right,
                  wrong: quizPicked === o.k && !o.right
                }"
                :disabled="quizSolved"
                @click="pickQuiz(o)"
              >{{ o.t }}</button>
            </div>
            <p v-if="quizWrong" class="quiz-tip">答错了，再想想 🤔</p>
            <p v-else-if="quizSolved" class="quiz-tip ok">答对啦！🎉 任务完成</p>
          </template>

          <!-- 带毛孩子散步 -->
          <template v-else-if="activeTask && activeTask.id === 'walk'">
            <div class="modal-emoji">🚶</div>
            <h3 class="modal-title">带毛孩子散步</h3>
            <div class="walk-scene">
              <div class="walk-sun">☀️</div>
              <div class="walk-pet" :class="{ run: walkStarted }">🐾</div>
              <div class="walk-ground"></div>
              <button
                v-for="it in walkItems"
                v-show="!it.got"
                :key="it.id"
                class="walk-item"
                :style="{ left: it.x + '%', top: it.y + '%' }"
                @click="collectItem(it)"
              >{{ it.emoji }}</button>
            </div>
            <p class="modal-sub">
              {{ !walkStarted ? '点击「出发」，带宠物出门溜达捡小宝物' : '已捡到 ' + walkCollected + '/3，集齐即可结束散步' }}
            </p>
            <button v-if="!walkStarted" class="modal-btn" @click="startWalk">出发散步 🐾</button>
            <button v-else class="modal-btn" :disabled="walkCollected < 3" @click="finishTask">
              {{ walkCollected < 3 ? '继续捡宝物…' : '结束散步，完成任务' }}
            </button>
          </template>

          <!-- 发布动态 -->
          <!-- 发布动态 / 好友点赞：已改为跳转导航栏「发现」页真实完成，不再用弹窗 -->

        </div>
      </div>
    </transition>

    <!-- ========== 跳障碍小游戏（独立弹窗：任务模式 + 纯娱乐模式） ========== -->
    <transition name="modal">
      <div v-if="showJumpModal" class="modal-mask" @click.self="closeJumpModal">
        <div class="modal">
          <button class="modal-close" @click="closeJumpModal">×</button>
          <div class="modal-emoji">🏃</div>
          <h3 class="modal-title">{{ jumpInfo.mode === 'fun' ? '陪宠物跳障碍 · 小游戏' : '陪宠物跳障碍' }}</h3>
          <p class="modal-sub">点击屏幕或按 <b>空格</b> 让毛孩子跳起，<b>空中还能再跳一次</b>，躲开 🌵，越过 12 个障碍即获胜！</p>
          <div class="jump-wrap">
            <canvas
              ref="jumpCanvas"
              width="340"
              height="190"
              class="jump-canvas"
              @pointerdown="onJumpTap"
            ></canvas>
            <div v-if="jumpInfo.status === 'lose'" class="jump-over">
              <p>{{ jumpInfo.petEmoji }} 撞到啦！已越过 {{ jumpInfo.passed }}/12</p>
              <button class="modal-btn" @click="startJump(jumpInfo.mode, jumpInfo.petEmoji)">再来一次 🔁</button>
            </div>
            <div v-else-if="jumpInfo.status === 'win' && jumpInfo.mode === 'fun'" class="jump-over">
              <p>{{ jumpInfo.petEmoji }} 太棒啦！🎉 配合超默契！</p>
              <p v-if="jumpInfo.newRecord" class="rec">🏆 刷新最佳纪录！</p>
              <button class="modal-btn" @click="startJump('fun', jumpInfo.petEmoji)">再来一局 🔁</button>
            </div>
          </div>
          <p class="modal-sub">已越过 <b>{{ jumpInfo.passed }}/12</b> · 点击/空格跳跃 · 支持二段跳 🪽</p>
          <p v-if="jumpInfo.mode === 'fun'" class="modal-sub fun-tag">纯娱乐模式，随时可退出，不影响任务进度～</p>
          <div v-if="jumpInfo.mode === 'fun'" class="rank-card">
            <span class="rank-ico">{{ jumpRank(bestJump).e }}</span>
            <div class="rank-main">
              <b class="rank-title">{{ jumpRank(bestJump).t }}</b>
              <span class="rank-sub">最佳越过 {{ bestJump }}/12</span>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import {
  petGame,
  myEggs,
  myPets,
  canGiftToday,
  canInteractToday,
  speciesList,
  friendsList,
  getPetType
} from '../composables/petAdoption'
import {
  loadState,
  adopt as apiAdopt,
  hatch as apiHatch,
  feed as apiFeed,
  interact as apiInteract,
  completeTask as apiCompleteTask,
  claimTask as apiClaimTask,
  claimGift as apiClaimGift,
  rename as apiRename
} from '../api/adoption'

const props = defineProps({
  user: { type: Object, required: true }
})
// go-compose：「发布一条动态」直接打开发帖页；go-friends：「给好友动态点赞」进「发现」并选中「好友」分类
const emit = defineEmits(['go-compose', 'go-friends'])

const tab = ref('adopt')
const toast = ref('')
const popId = ref('')
// 物种 / 好友列表均来自 petAdoption.js（后端数据同步或本地兜底）
const petTypes = speciesList
const friends = friendsList

const typeOf = (p) =>
  getPetType(p.typeId) ||
  speciesList.value.find((t) => t.id === p.typeId && t.emoji) ||
  { color: '#ff6fa8', emoji: '🐾', name: p.name, hatchCost: 20 }

let toastTimer = null
function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

// 进入即从后端（或本地兜底）加载当前用户的养成数据
watch(
  () => props.user?.username,
  async (u) => {
    if (u) {
      await loadState(u)
      loadSignState() // 切换用户时同步读取各自的签到进度
    }
  },
  { immediate: true }
)

async function onAdopt(typeId) {
  const r = await apiAdopt(props.user.username, typeId)
  if (r.ok) {
    showToast('领养成功！去「我的宠物」孵化它吧 🥚')
    tab.value = 'mine'
  }
}

async function onHatch(petId) {
  const r = await apiHatch(props.user.username, petId)
  if (r.ok) showToast('🎉 孵化成功，欢迎新成员！')
  else if (r.reason === 'lack') showToast('养分不足，先做任务攒养分')
}

async function onFeed(petId) {
  const r = await apiFeed(props.user.username, petId)
  if (r.ok) showToast('喂食成功，亲密度 +' + r.gain + ' 💗')
  else if (r.reason === 'nofood') showToast('没有食物了，去做任务或领赠送')
}

async function onInteract(petId) {
  const r = await apiInteract(props.user.username, petId)
  if (r.ok) {
    popId.value = petId
    setTimeout(() => (popId.value = ''), 600)
    const left = 5 - petGame.interact.count
    showToast('摸摸成功，亲密度 +' + r.gain + (left > 0 ? '（今日剩 ' + left + ' 次）' : '（今日已满）'))
  } else if (r.reason === 'limit') {
    showToast('今天摸摸已经满 5 次啦，明天再来～')
  }
}

async function onRename(p) {
  const name = window.prompt('给宠物起个名字：', p.name)
  if (name !== null) {
    await apiRename(props.user.username, p.id, name)
    showToast('已改名')
  }
}

/* ---------- 任务互动弹窗：真实做完操作才算完成 ---------- */
const showTaskModal = ref(false)
const showJumpModal = ref(false)
const activeTask = ref(null)
/* ---------- 每周签到：每天只能点亮 1 格，进度跨天保留 ---------- */
const SIGN_KEY = 'mengchong.sign.v1.'
const signLit = ref([false, false, false, false, false, false, false])
const signDates = ref(['', '', '', '', '', '', '']) // 每格点亮的日期，用于限制"一天一格"
const signCount = computed(() => signLit.value.filter(Boolean).length)
const signedToday = computed(() => signDates.value.some((d) => d === todayStr()))

function signTodayStr() {
  const d = new Date()
  return (
    d.getFullYear() +
    '-' +
    String(d.getMonth() + 1).padStart(2, '0') +
    '-' +
    String(d.getDate()).padStart(2, '0')
  )
}
const todayStr = signTodayStr

function nextSignIndex() {
  return signLit.value.findIndex((v) => !v)
}

/** 第 n 天此刻是否可点：未点亮 + 按顺序 + 今天还没打过卡 */
function canSignDay(n) {
  const idx = n - 1
  if (signLit.value[idx]) return false
  if (idx !== nextSignIndex()) return false
  return !signedToday.value
}

function loadSignState() {
  try {
    const raw = localStorage.getItem(SIGN_KEY + (props.user?.username || 'guest'))
    const d = raw ? JSON.parse(raw) : null
    if (d && Array.isArray(d.dates) && d.dates.length === 7) {
      signDates.value = d.dates.map((x) => x || '')
      signLit.value = signDates.value.map((x) => !!x)
      return
    }
  } catch (e) {
    /* 解析失败则用空数据 */
  }
  signLit.value = [false, false, false, false, false, false, false]
  signDates.value = ['', '', '', '', '', '', '']
}

function persistSignState() {
  try {
    localStorage.setItem(
      SIGN_KEY + (props.user?.username || 'guest'),
      JSON.stringify({ dates: signDates.value })
    )
  } catch (e) {
    /* 存储不可用时静默忽略 */
  }
}

function signClick(n) {
  const idx = n - 1
  if (signLit.value[idx]) return
  if (idx !== nextSignIndex()) {
    showToast('要按顺序点亮哦，先点第 ' + (nextSignIndex() + 1) + ' 天')
    return
  }
  if (signedToday.value) {
    showToast('今天已经打过卡啦，明天再来～')
    return
  }
  signLit.value[idx] = true
  signDates.value[idx] = signTodayStr()
  persistSignState()
  // 当天打完卡即视为完成任务：自动标记完成，用户直接去「领取」
  finishTask()
}

/** 打卡完成后的按钮：万一之前完成请求没成功，再补一次；否则直接关弹窗 */
function signDoneClick() {
  if (activeTask.value && !activeTask.value.completed) {
    finishTask()
    return
  }
  closeTaskModal()
}
const readChecked = ref(false)
/* 萌宠小知识库：每天按日期轮换一篇，同一天内所有人看到的是同一篇 */
const PET_FACTS = [
  {
    title: '猫的胡须是「触觉雷达」',
    lines: [
      '猫的胡须（触须）根部布满神经，是重要的「触觉雷达」：',
      '① 黑暗中感知障碍与猎物位置；',
      '② 判断缝隙能否通过（胡须宽度≈身体宽度）；',
      '③ 根部有本体感受器，帮助保持平衡。',
      '所以千万别随意剪猫咪的胡须哦～'
    ]
  },
  {
    title: '狗狗的鼻子纹路像指纹',
    lines: [
      '每只狗的鼻纹都是独一无二的，就像人类的指纹：',
      '① 鼻纹可以用来做身份识别，准确率很高；',
      '② 狗狗嗅觉受体约有 3 亿个，是人类的 40 多倍；',
      '③ 湿润的鼻头能帮助吸附气味分子，闻得更清楚。',
      '所以别嫌它鼻子湿湿的，那是它在「认真闻」～'
    ]
  },
  {
    title: '兔子其实不会「说话」',
    lines: [
      '兔子很少发出声音，它们主要靠肢体语言表达情绪：',
      '① 开心时会原地起跳扭身，俗称「兔子蹦」；',
      '② 轻轻磨牙表示舒服，大声磨牙则可能是疼痛；',
      '③ 耳朵向后贴往往代表紧张或警告。',
      '多观察它的动作，胜过听它「说话」～'
    ]
  },
  {
    title: '仓鼠的颊囊能装下体重 20% 的食物',
    lines: [
      '仓鼠腮帮子里藏着一对可伸缩的颊囊：',
      '① 一次能搬运相当于自身体重约 20% 的食物；',
      '② 野生仓鼠靠颊囊把粮食运回洞穴储存；',
      '③ 但黏性大的食物（如巧克力）容易堵在颊囊里。',
      '所以投喂要适量，别让小仓库「超载」哦～'
    ]
  },
  {
    title: '鹦鹉的左脚可能更「利手」',
    lines: [
      '很多鹦鹉和人一样有「利手」偏好：',
      '① 多数鹦鹉习惯用左脚抓取食物；',
      '② 这与大脑两侧分工有关，个体之间有差异；',
      '③ 观察它先伸哪只脚，就能知道它的惯用爪。',
      '你家的小鸟是「左撇子」还是「右撇子」呢？'
    ]
  },
  {
    title: '金鱼的记忆不止 7 秒',
    lines: [
      '「金鱼只有 7 秒记忆」其实是误传：',
      '① 实验表明金鱼记忆可以维持数月；',
      '② 它们能学会按时间、按信号觅食；',
      '③ 还能认出经常喂它的人。',
      '所以金鱼不是「健忘」，它记得你的好～'
    ]
  },
  {
    title: '猫咪一天的睡眠可达 16 小时',
    lines: [
      '猫咪是出了名的「睡觉大师」：',
      '① 成年猫每天睡 12~16 小时很正常；',
      '② 大部分是浅睡，随时能被声音唤醒；',
      '③ 深度睡眠只占约 1/4，多在夜间和黎明前。',
      '它趴在你身边打盹，其实是放松和信任的表现～'
    ]
  },
  {
    title: '狗摇尾巴的方向有讲究',
    lines: [
      '狗狗摇尾巴不只是开心，方向也有含义：',
      '① 研究发现情绪积极时尾巴偏向右侧摇；',
      '② 遇到陌生或紧张情境时偏向左侧摇；',
      '③ 尾巴位置低、夹紧则代表害怕。',
      '观察整只狗的状态，才能读懂它的心情哦～'
    ]
  },
  {
    title: '兔子吃自己的软便是正常的',
    lines: [
      '听起来有点意外，但对兔子来说很健康：',
      '① 兔子会排出一种叫「盲肠便」的软便；',
      '② 里面富含 B 族维生素和蛋白质，吃掉能二次吸收营养；',
      '③ 这是完全正常的消化行为，不用担心。',
      '毛孩子的「怪习惯」背后，往往有科学的道理～'
    ]
  },
  {
    title: '猫的呼噜声有治愈效果',
    lines: [
      '猫咪打呼噜（咕噜声）不只是舒服：',
      '① 呼噜声频率约 25~150 赫兹，被认为有助于骨骼与组织修复；',
      '② 猫在紧张时也会打呼噜，是自我安抚的方式；',
      '③ 不少人说听猫呼噜能减压，这叫「猫疗」。',
      '所以被猫压着睡觉时，好好享受这份「白噪音」吧～'
    ]
  }
]
const dailyFact = computed(() => {
  const d = new Date()
  const dayOfYear = Math.floor(
    (d - new Date(d.getFullYear(), 0, 0)) / (24 * 60 * 60 * 1000)
  )
  return PET_FACTS[dayOfYear % PET_FACTS.length]
})
const quizPicked = ref('')
const quizWrong = ref(false)
const quizSolved = ref(false)
/* 萌宠测验题库：每天按日期轮换一题，同一天内所有人看到的是同一题 */
const PET_QUIZZES = [
  {
    q: '狗狗主要靠什么方式散热降温？',
    opts: [
      { k: 'a', t: '舌头和脚垫出汗', right: true },
      { k: 'b', t: '耳朵大幅扇动', right: false },
      { k: 'c', t: '全身毛孔排汗', right: false },
      { k: 'd', t: '尾巴左右甩动', right: false }
    ]
  },
  {
    q: '猫咪的胡须主要有什么作用？',
    opts: [
      { k: 'a', t: '纯装饰，没有用处', right: false },
      { k: 'b', t: '感知障碍和判断缝隙宽度', right: true },
      { k: 'c', t: '用来调节体温', right: false },
      { k: 'd', t: '帮助消化食物', right: false }
    ]
  },
  {
    q: '兔子开心时最典型的动作是什么？',
    opts: [
      { k: 'a', t: '原地起跳扭身（兔子蹦）', right: true },
      { k: 'b', t: '一直趴着不动', right: false },
      { k: 'c', t: '不停地刨笼子', right: false },
      { k: 'd', t: '大声叫唤', right: false }
    ]
  },
  {
    q: '仓鼠搬运食物靠的是身体的哪个部位？',
    opts: [
      { k: 'a', t: '背部驮运', right: false },
      { k: 'b', t: '尾巴卷住', right: false },
      { k: 'c', t: '腮帮子里的颊囊', right: true },
      { k: 'd', t: '前爪捧着跑回去', right: false }
    ]
  },
  {
    q: '「金鱼只有 7 秒记忆」这个说法对吗？',
    opts: [
      { k: 'a', t: '对，是科学共识', right: false },
      { k: 'b', t: '不对，金鱼记忆可达数月', right: true },
      { k: 'c', t: '对，但只对观赏金鱼成立', right: false },
      { k: 'd', t: '科学家还没研究过', right: false }
    ]
  },
  {
    q: '猫一天大约睡多久？',
    opts: [
      { k: 'a', t: '2~4 小时', right: false },
      { k: 'b', t: '6~8 小时', right: false },
      { k: 'c', t: '12~16 小时', right: true },
      { k: 'd', t: '20 小时以上', right: false }
    ]
  },
  {
    q: '狗狗情绪积极开心时，尾巴一般往哪边偏着摇？',
    opts: [
      { k: 'a', t: '偏右侧摇', right: true },
      { k: 'b', t: '偏左侧摇', right: false },
      { k: 'c', t: '只转圈不偏侧', right: false },
      { k: 'd', t: '夹紧不摇', right: false }
    ]
  },
  {
    q: '很多鹦鹉抓食物时更习惯用哪只脚？',
    opts: [
      { k: 'a', t: '右脚', right: false },
      { k: 'b', t: '左脚', right: true },
      { k: 'c', t: '双脚并用', right: false },
      { k: 'd', t: '用喙叼，不用脚', right: false }
    ]
  },
  {
    q: '猫的呼噜声频率大约在什么范围？',
    opts: [
      { k: 'a', t: '25~150 赫兹', right: true },
      { k: 'b', t: '1000~2000 赫兹', right: false },
      { k: 'c', t: '5000 赫兹以上', right: false },
      { k: 'd', t: '低于 5 赫兹', right: false }
    ]
  },
  {
    q: '狗的嗅觉受体数量大约是人类的多少倍？',
    opts: [
      { k: 'a', t: '差不多一样多', right: false },
      { k: 'b', t: '约 3 倍', right: false },
      { k: 'c', t: '约 40 倍', right: true },
      { k: 'd', t: '约 500 倍', right: false }
    ]
  }
]
const dailyQuiz = computed(() => {
  const d = new Date()
  const dayOfYear = Math.floor(
    (d - new Date(d.getFullYear(), 0, 0)) / (24 * 60 * 60 * 1000)
  )
  return PET_QUIZZES[dayOfYear % PET_QUIZZES.length]
})
const walkStarted = ref(false)
const walkItems = ref([])
const walkCollected = ref(0)

function openTask(t) {
  // 这两个任务关联到「发现」页：发帖任务直开发帖页，点赞任务进「好友」分类，完成后自动同步任务状态
  if (t.id === 'post' || t.id === 'like') {
    emit(t.id === 'post' ? 'go-compose' : 'go-friends')
    return
  }
  if (t.id === 'jump') {
    // 跳障碍以独立弹窗运行：任务模式下胜利会自动完成任务
    activeTask.value = t
    showJumpModal.value = true
    nextTick(() => startJump('task'))
    return
  }
  activeTask.value = t
  if (t.id === 'sign') {
    // 恢复本周签到进度（跨天保留），而不是每次打开都清零
    loadSignState()
  }
  readChecked.value = false
  quizPicked.value = ''
  quizWrong.value = false
  quizSolved.value = false
  walkStarted.value = false
  walkItems.value = []
  walkCollected.value = 0
  showTaskModal.value = true
}

function closeTaskModal() {
  stopJump()
  showTaskModal.value = false
}

// 纯娱乐入口：在「我的宠物」点「🎮 陪它跳」打开，胜利不影响任务进度
function openFunJump(emoji) {
  showJumpModal.value = true
  nextTick(() => startJump('fun', emoji))
}

function closeJumpModal() {
  stopJump()
  showJumpModal.value = false
}

async function finishTask() {
  stopJump()
  const r = await apiCompleteTask(props.user.username, activeTask.value.id)
  if (r.ok) {
    // 签到：只有 7 天全部点亮（本周一轮结束）才清空记录，每天完成不能清
    if (activeTask.value.id === 'sign' && signCount.value >= 7) {
      signLit.value = [false, false, false, false, false, false, false]
      signDates.value = ['', '', '', '', '', '', '']
      persistSignState()
    }
    showTaskModal.value = false
    showJumpModal.value = false
    showToast('任务完成，记得来领取奖励 🎁')
  } else if (r.reason === 'done') {
    showToast('这个任务已经完成啦')
    showTaskModal.value = false
    showJumpModal.value = false
  }
}

// 签到改为多步点亮（见 signClick / signAllLit），不再需要 doSign

// 发布动态 / 好友点赞已改为跳转「发现」页，doPost 不再需要

function pickQuiz(o) {
  if (quizSolved.value) return
  quizPicked.value = o.k
  if (o.right) {
    quizSolved.value = true
    setTimeout(finishTask, 600)
  } else {
    quizWrong.value = true
  }
}

function startWalk() {
  walkStarted.value = true
  walkCollected.value = 0
  const emojis = ['🦴', '🍂', '⭐', '🌸', '🍃']
  walkItems.value = [0, 1, 2].map((i) => ({
    id: i,
    emoji: emojis[Math.floor(Math.random() * emojis.length)],
    x: 12 + i * 26 + Math.random() * 6,
    y: 25 + Math.random() * 45,
    got: false
  }))
}

function collectItem(it) {
  if (it.got) return
  it.got = true
  walkCollected.value += 1
}

/* ---------- 跳障碍小游戏 ---------- */
const jumpCanvas = ref(null)
const jumpInfo = ref({ status: 'playing', passed: 0, mode: 'fun' })
// 纯娱乐模式的最佳成绩（本地存，刷新不丢）
const bestJump = ref(Number(localStorage.getItem('pet_jump_best') || 0))
function recordJumpBest() {
  const passed = jumpInfo.value.passed
  if (passed > bestJump.value) {
    bestJump.value = passed
    localStorage.setItem('pet_jump_best', String(passed))
    jumpInfo.value.newRecord = true
  }
}
// 程序化音效（Web Audio，无需外部文件）
let actx = null
function ensureAudio() {
  if (!actx) {
    try {
      actx = new (window.AudioContext || window.webkitAudioContext)()
    } catch (e) {
      actx = null
    }
  }
  if (actx && actx.state === 'suspended') actx.resume()
}
function sfx(type) {
  if (!actx) return
  const now = actx.currentTime
  const beep = (freq, start, dur, wave, vol) => {
    const o = actx.createOscillator()
    const g = actx.createGain()
    o.type = wave
    o.frequency.setValueAtTime(freq, now + start)
    g.gain.setValueAtTime(0.0001, now + start)
    g.gain.exponentialRampToValueAtTime(vol, now + start + 0.01)
    g.gain.exponentialRampToValueAtTime(0.0001, now + start + dur)
    o.connect(g)
    g.connect(actx.destination)
    o.start(now + start)
    o.stop(now + start + dur + 0.02)
  }
  if (type === 'jump') {
    const o = actx.createOscillator()
    const g = actx.createGain()
    o.type = 'sine'
    o.frequency.setValueAtTime(520, now)
    o.frequency.exponentialRampToValueAtTime(900, now + 0.09)
    g.gain.setValueAtTime(0.0001, now)
    g.gain.exponentialRampToValueAtTime(0.16, now + 0.01)
    g.gain.exponentialRampToValueAtTime(0.0001, now + 0.13)
    o.connect(g)
    g.connect(actx.destination)
    o.start(now)
    o.stop(now + 0.15)
  } else if (type === 'hit') {
    beep(150, 0, 0.2, 'square', 0.2)
    beep(80, 0.02, 0.22, 'square', 0.16)
  } else if (type === 'win') {
    beep(660, 0, 0.12, 'triangle', 0.2)
    beep(880, 0.13, 0.18, 'triangle', 0.2)
  }
}
// 最佳成绩称号
function jumpRank(best) {
  if (best >= 12) return { t: '传奇驯兽师', e: '👑' }
  if (best >= 9) return { t: '跳跃大师', e: '🥇' }
  if (best >= 6) return { t: '灵巧好手', e: '🥈' }
  if (best >= 3) return { t: '见习骑士', e: '🥉' }
  return { t: '萌新训练生', e: '🐣' }
}
let jctx = null
let jraf = 0
let jstate = null

function onJumpKey(e) {
  if (e.code === 'Space' || e.key === ' ') {
    e.preventDefault()
    onJumpTap()
  }
}
function onJumpTap() {
  ensureAudio()
  if (!jstate || !jstate.running) return
  const s = jstate
  if (s.onGround) {
    s.vy = -9.2
    s.onGround = false
    sfx('jump')
  } else if (s.airJumps > 0) {
    // 二段跳：跳早了也能在空中补一次，容错更宽松
    s.airJumps--
    s.vy = -9.2
    sfx('jump')
  } else {
    // 空中提前按也算：落地瞬间自动补跳，连跳更顺手
    s.jumpBuf = 8
  }
}
function stopJump() {
  if (jraf) {
    cancelAnimationFrame(jraf)
    jraf = 0
  }
  window.removeEventListener('keydown', onJumpKey)
}
function startJump(mode, emoji) {
  stopJump()
  const cv = jumpCanvas.value
  if (!cv) return
  jctx = cv.getContext('2d')
  // 把本局宠物记进状态，重开时从状态回传，避免回落到默认 🐱
  jumpInfo.value = { status: 'playing', passed: 0, mode: mode || 'fun', newRecord: false, petEmoji: emoji || '🐱' }
  jstate = {
    petY: 156,
    vy: 0,
    onGround: true,
    jumpBuf: 0,
    airJumps: 1,
    obstacles: [],
    spawnTimer: 0,
    spawnGap: 66,
    speed: 2.7,
    passed: 0,
    running: true,
    t: 0,
    petEmoji: jumpInfo.value.petEmoji
  }
  const W = 340,
    H = 190,
    groundY = 156,
    petX = 46,
    petW = 30,
    petH = 30
  window.addEventListener('keydown', onJumpKey)
  const loop = () => {
    if (!jstate || !jstate.running) return
    const s = jstate
    s.t = (s.t || 0) + 1
    // 物理
    s.vy += 0.5
    s.petY += s.vy
    if (s.petY >= groundY) {
      s.petY = groundY
      s.vy = 0
      s.onGround = true
      s.airJumps = 1
      // 落地瞬间若有预输入，自动补跳（避免「按早了没反应」）
      if (s.jumpBuf > 0) {
        s.jumpBuf = 0
        s.vy = -9.2
        s.onGround = false
        sfx('jump')
      }
    }
    if (s.jumpBuf > 0) s.jumpBuf--
    // 生成障碍（难度温和递增：间隔保底 48 帧，保证落地后有余量再起跳）
    s.spawnTimer++
    if (s.spawnTimer >= s.spawnGap) {
      s.spawnTimer = 0
      const base = Math.max(48, 66 - s.passed * 1.2)
      s.spawnGap = base + Math.floor(Math.random() * 10)
      const h = 20 + Math.floor(Math.random() * (6 + Math.min(8, s.passed)))
      s.obstacles.push({ x: W, w: 16 + Math.floor(Math.random() * 6), h, passed: false })
    }
    // 移动 / 计分 / 碰撞
    for (const o of s.obstacles) {
      o.x -= s.speed
      if (!o.passed && o.x + o.w < petX) {
        o.passed = true
        s.passed++
        // 难度温和递增：速度上限 4.4，保证障碍间距始终大于滞空位移
        s.speed = Math.min(4.4, 2.7 + s.passed * 0.12)
        jumpInfo.value.passed = s.passed
      }
      // 判定框各内缩几像素，给玩家一点擦边宽容
      const x1 = petX + 4,
        y1 = s.petY - petH + 4,
        w1 = petW - 8,
        h1 = petH - 6
      const x2 = o.x + 3,
        y2 = groundY - o.h + 3,
        w2 = o.w - 6,
        h2 = o.h - 3
      if (x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2) {
        s.running = false
        jumpInfo.value.status = 'lose'
        sfx('hit')
        if (jumpInfo.value.mode === 'fun') recordJumpBest()
      }
    }
    s.obstacles = s.obstacles.filter((o) => o.x > -40)
    // 胜利：越过 12 个障碍
    if (s.passed >= 12) {
      s.running = false
      jumpInfo.value.status = 'win'
      stopJump()
      sfx('win')
      // 任务模式：自动完成 jump 任务；娱乐模式：仅显示胜利并记录最佳，不完成任务
      if (jumpInfo.value.mode === 'fun') recordJumpBest()
      else finishTask()
      return
    }
    drawJump(W, H, groundY, petX, petW, petH)
    jraf = requestAnimationFrame(loop)
  }
  jraf = requestAnimationFrame(loop)
}
function drawJump(W, H, groundY, petX, petW, petH) {
  const c = jctx
  c.clearRect(0, 0, W, H)
  // 天空
  c.fillStyle = '#dff1ff'
  c.fillRect(0, 0, W, H)
  // 太阳
  c.fillStyle = '#ffe08a'
  c.beginPath()
  c.arc(W - 30, 28, 16, 0, Math.PI * 2)
  c.fill()
  // 地面
  c.fillStyle = '#bfe6a8'
  c.fillRect(0, groundY, W, H - groundY)
  c.strokeStyle = '#8fcf6f'
  c.lineWidth = 2
  c.beginPath()
  c.moveTo(0, groundY)
  c.lineTo(W, groundY)
  c.stroke()
  // 障碍
  c.textAlign = 'center'
  c.textBaseline = 'bottom'
  for (const o of jstate.obstacles) {
    c.font = Math.round(o.h * 0.92) + 'px serif'
    c.fillText('🌵', o.x + o.w / 2, groundY)
  }
  // 宠物（动态：待机轻微浮动 + 跳跃挤压拉伸）
  c.save()
  const pcx = petX + petW / 2
  const pcy = jstate.petY
  const tt = jstate.t || 0
  let psx = 1,
    psy = 1
  if (jstate.onGround && jstate.vy === 0) {
    const bob = Math.sin(tt * 0.15) * 1.6
    c.translate(pcx, pcy + bob)
  } else {
    const stretch = Math.max(-0.25, Math.min(0.32, -jstate.vy * 0.03))
    psy = 1 + stretch
    psx = 1 - stretch * 0.6
    c.translate(pcx, pcy)
  }
  c.scale(psx, psy)
  c.textAlign = 'center'
  c.textBaseline = 'bottom'
  c.font = '30px serif'
  c.fillText(jstate.petEmoji || '🐱', 0, 0)
  c.restore()
  // 计分
  c.fillStyle = '#557'
  c.font = '13px sans-serif'
  c.textAlign = 'left'
  c.textBaseline = 'top'
  c.fillText('越过 ' + jstate.passed + '/12', 8, 8)
}

async function onTaskClaim(taskId) {
  const r = await apiClaimTask(props.user.username, taskId)
  if (r.ok) {
    if (r.reward.type === 'food') showToast('获得 🍖 食物 ×1')
    else showToast('获得 🌱 养分 +' + r.reward.amount)
  } else if (r.reason === 'incomplete') {
    showToast('请先完成任务再领取')
  } else if (r.reason === 'done') {
    showToast('这个奖励今天已经领取啦')
  }
}

async function onGift(friendKey) {
  const r = await apiClaimGift(props.user.username, friendKey)
  if (r.ok) showToast('领取成功：🌱+10 🍖+1')
  else if (r.reason === 'limit') showToast('今日赠送次数已用完')
  else if (r.reason === 'already') showToast('这位好友今天已领取过')
  else if (r.reason === 'notfriend') showToast('对方不是你的好友')
}
</script>

<style scoped>
.adopt {
  max-width: 960px;
  margin: 0 auto;
  padding: 18px 16px 60px;
}

/* 资源条 */
.res-bar {
  display: flex;
  gap: 10px;
  background: #fff;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 6px 22px rgba(255, 111, 168, 0.12);
}
.res-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.res-ico { font-size: 20px; }
.res-num { font-size: 20px; font-weight: 800; color: #ff4f9a; }
.res-label { font-size: 12px; color: #9aa; }

/* 选项卡 */
.tabs {
  display: flex;
  gap: 8px;
  margin: 16px 0;
  flex-wrap: wrap;
}
.tabs button {
  flex: 1;
  min-width: 90px;
  border: none;
  background: #fff;
  color: #667;
  padding: 10px 8px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  position: relative;
  transition: all 0.18s;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.04);
}
.tabs button.on {
  background: linear-gradient(135deg, #ff6fa8, #b98bff);
  color: #fff;
}
.tab-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ff3b6b;
  color: #fff;
  font-size: 11px;
  border-radius: 10px;
  padding: 1px 6px;
}

.panel-tip {
  font-size: 13px;
  color: #889;
  margin: 4px 2px 14px;
  line-height: 1.6;
}
.empty {
  text-align: center;
  color: #aab;
  padding: 50px 0;
  font-size: 15px;
}

/* 领养网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
}
.pet-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 12px;
  text-align: center;
  border: 2px solid transparent;
  transition: transform 0.18s, border-color 0.18s;
}
.pet-card:hover {
  transform: translateY(-4px);
  border-color: var(--c);
}
.pet-emoji { font-size: 46px; }
.pet-name { font-size: 16px; margin: 8px 0 4px; color: #334; }
.pet-desc { font-size: 12px; color: #99a; min-height: 32px; margin: 0 0 8px; }
.pet-cost { font-size: 12px; color: #ff7aa8; margin: 0 0 10px; font-weight: 700; }
.adopt-btn {
  width: 100%;
  border: none;
  background: var(--c);
  color: #fff;
  padding: 9px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: filter 0.15s;
}
.adopt-btn:hover { filter: brightness(1.06); }

/* 我的宠物 */
.my-pets {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.my-pet {
  background: #fff;
  border-radius: 18px;
  padding: 18px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
  border-top: 4px solid var(--c);
}
.egg {
  font-size: 60px;
  animation: eggFloat 2.4s ease-in-out infinite;
}
@keyframes eggFloat {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-8px) rotate(3deg); }
}
.egg-need { font-size: 13px; color: #ff7aa8; font-weight: 700; margin: 8px 0 2px; }
.egg-have { font-size: 12px; color: #99a; margin: 0 0 12px; }
.hatch-btn {
  border: none;
  background: linear-gradient(135deg, #ffb35c, #ff8a5c);
  color: #fff;
  padding: 10px 22px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 15px;
  cursor: pointer;
}
.hatch-btn:disabled {
  background: #ddd;
  color: #999;
  cursor: not-allowed;
}
.hint { font-size: 12px; color: #aab; margin: 10px 0 0; }

.pet-emoji.big {
  font-size: 72px;
  cursor: pointer;
  user-select: none;
  display: inline-block;
}
.pet-emoji.big.pop { animation: pop 0.6s ease; }
@keyframes pop {
  0% { transform: scale(1); }
  30% { transform: scale(1.35) rotate(-8deg); }
  60% { transform: scale(0.92) rotate(8deg); }
  100% { transform: scale(1); }
}
.rename {
  border: none;
  background: #f0f0f5;
  color: #889;
  border-radius: 8px;
  padding: 2px 6px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 4px;
}
.stat {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  font-size: 12px;
  color: #778;
}
.stat-label { width: 44px; text-align: right; }
.stat-val { width: 28px; text-align: left; font-weight: 700; color: #ff6fa8; }
.bar {
  flex: 1;
  height: 8px;
  background: #ffe1ee;
  border-radius: 6px;
  overflow: hidden;
}
.bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #ff6fa8, #ff9ec7);
  transition: width 0.4s;
}
.bar.sat i { background: linear-gradient(90deg, #ffb35c, #ffd28a); }

.actions { display: flex; gap: 10px; margin-top: 12px; }
.act {
  flex: 1;
  border: none;
  border-radius: 12px;
  padding: 10px;
  font-weight: 700;
  cursor: pointer;
  font-size: 14px;
}
.act.interact { background: #eef0ff; color: #6a5cff; }
.act.feed { background: #fff0e0; color: #ff8a3c; }
.act:disabled { opacity: 0.5; cursor: not-allowed; }
.act.play { background: #e6f9ef; color: #1c8f5b; }
.mini { font-size: 11px; font-weight: 400; opacity: 0.8; }

/* 任务 */
.task-list, .gift-list { list-style: none; padding: 0; margin: 0; }
.task, .gift {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 10px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.04);
}
.task.done { opacity: 0.6; }
.task-main { flex: 1; }
.task-title { font-size: 15px; margin: 0 0 3px; color: #334; }
.task-desc { font-size: 12px; color: #99a; margin: 0; }
.task-reward { text-align: right; }
.reward-tag {
  display: block;
  font-size: 12px;
  color: #ff7aa8;
  font-weight: 700;
  margin-bottom: 6px;
}
.task-btn, .gift-btn {
  border: none;
  background: linear-gradient(135deg, #ff6fa8, #b98bff);
  color: #fff;
  padding: 8px 18px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}
.task-btn:disabled, .gift-btn:disabled {
  background: #ddd;
  color: #999;
  cursor: not-allowed;
}

/* 好友赠送 */
.gift-ava {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b7bff, #ff6fa8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  margin-right: 12px;
}
.gift-main { flex: 1; }
.gift-name { font-size: 15px; margin: 0 0 3px; color: #334; }
.gift-sub { font-size: 12px; color: #99a; margin: 0; }

/* 提示 */
.toast {
  position: fixed;
  left: 50%;
  bottom: 80px;
  transform: translateX(-50%);
  background: rgba(40, 30, 50, 0.92);
  color: #fff;
  padding: 12px 22px;
  border-radius: 24px;
  font-size: 14px;
  z-index: 999;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}
.toast-enter-active, .toast-leave-active { transition: all 0.25s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, 12px); }

/* 「去做」按钮区别于「领取」 */
.task-btn.go { background: linear-gradient(135deg, #8b7bff, #6a5cff); }

/* 任务互动弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(30, 20, 40, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  width: min(420px, 92vw);
  max-height: 86vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 20px;
  padding: 26px 22px 22px;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  text-align: center;
}
.modal-close {
  position: absolute;
  top: 10px;
  right: 14px;
  border: none;
  background: transparent;
  font-size: 26px;
  line-height: 1;
  color: #bbb;
  cursor: pointer;
}
.modal-emoji { font-size: 48px; }
.modal-title { font-size: 18px; color: #334; margin: 8px 0 6px; }
.modal-sub { font-size: 13px; color: #99a; margin: 0 0 14px; line-height: 1.6; }
.modal-btn {
  margin-top: 16px;
  width: 100%;
  border: none;
  background: linear-gradient(135deg, #ff6fa8, #b98bff);
  color: #fff;
  padding: 12px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 15px;
  cursor: pointer;
}
.modal-btn:disabled {
  background: #ddd;
  color: #999;
  cursor: not-allowed;
}

/* 签到 */
.sign-card {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin: 6px 0;
}
.sign-day {
  font-size: 11px;
  padding: 10px 2px;
  border-radius: 10px;
  background: #f1f1f6;
  color: #99a;
  font-weight: 700;
  border: none;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;
}
.sign-day.on {
  background: linear-gradient(135deg, #ffb35c, #ff8a5c);
  color: #fff;
  cursor: default;
}

/* 阅读 */
.read-box {
  text-align: left;
  background: #faf7ff;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 13px;
  color: #556;
  line-height: 1.8;
}
.read-box p { margin: 2px 0; }
.read-check {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  margin-top: 12px;
  font-size: 13px;
  color: #667;
  cursor: pointer;
}

/* 测验 */
.quiz-q { font-size: 15px; color: #334; font-weight: 700; margin: 4px 0 12px; }
.quiz-opts { display: flex; flex-direction: column; gap: 8px; }
.quiz-opt {
  border: 2px solid #eee;
  background: #fff;
  border-radius: 12px;
  padding: 11px;
  font-size: 14px;
  color: #445;
  cursor: pointer;
  transition: all 0.15s;
}
.quiz-opt.pick { border-color: #b98bff; }
.quiz-opt.right { border-color: #2ec27e; background: #e9faf1; color: #1c8f5b; }
.quiz-opt.wrong { border-color: #ff5b7b; background: #fff0f3; color: #d6336c; }
.quiz-opt:disabled { cursor: default; }
.quiz-tip { font-size: 13px; margin-top: 10px; color: #ff5b7b; }
.quiz-tip.ok { color: #1c8f5b; }

/* 散步 */
.walk-scene {
  position: relative;
  height: 120px;
  background: linear-gradient(180deg, #dff1ff, #f6fbff);
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 8px;
}
.walk-sun { position: absolute; top: 10px; right: 14px; font-size: 26px; }
.walk-ground {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 28px;
  background: #cdeccd;
}
.walk-item {
  position: absolute;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
  font-size: 18px;
  line-height: 34px;
  text-align: center;
  padding: 0;
  cursor: pointer;
  animation: itemPop 0.3s ease;
}
@keyframes itemPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

/* 跳障碍小游戏 */
.jump-wrap {
  position: relative;
  margin: 0 auto;
  max-width: 340px;
}
.jump-canvas {
  width: 100%;
  border-radius: 14px;
  display: block;
  background: #dff1ff;
  touch-action: manipulation;
}
.jump-over {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(30, 20, 40, 0.45);
  border-radius: 14px;
  color: #fff;
}
.jump-over p {
  font-size: 15px;
  font-weight: 700;
  margin: 0 0 12px;
}
.fun-tag { color: #ffb35c; font-weight: 700; }
.rank-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #fff3e0, #ffe9f3);
  border: 1px solid #ffd9ec;
  border-radius: 14px;
  padding: 10px 14px;
  margin: 4px 2px 0;
}
.rank-ico { font-size: 30px; line-height: 1; }
.rank-main { text-align: left; }
.rank-title { display: block; font-size: 15px; color: #ff5fa2; }
.rank-sub { font-size: 12px; color: #9aa886; }
.rec {
  color: #ffd54a;
  font-size: 16px;
  font-weight: 800;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
  margin: 0 0 8px;
}
.walk-pet {
  position: absolute;
  bottom: 26px;
  left: 30px;
  font-size: 38px;
}
.walk-pet.run { animation: walkMove 2.4s ease-in-out infinite alternate; }
@keyframes walkMove {
  0% { transform: translateX(0) rotate(0); }
  100% { transform: translateX(220px) rotate(-6deg); }
}

/* 发布动态 */
/* 弹窗动画 */
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
