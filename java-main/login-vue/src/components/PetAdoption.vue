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
      <p class="panel-tip">点「去做」打开对应的小互动，<b>真正完成操作</b>后任务才会标记完成，再去「领取」奖励：养分（每次 5~10 点）或食物（每次 1 份）。</p>
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
            <h3 class="modal-title">本周签到打卡</h3>
            <p class="modal-sub">依次点亮 7 天签到格，全部点亮即完成本周打卡～</p>
            <div class="sign-card">
              <button
                v-for="n in 7"
                :key="n"
                class="sign-day"
                :class="{ on: signLit[n - 1] }"
                :disabled="signLit[n - 1]"
                @click="signClick(n)"
              >{{ signLit[n - 1] ? '✓' : '第' + n + '天' }}</button>
            </div>
            <button v-if="!signAllLit" class="modal-btn" disabled>已点亮 {{ signCount }}/7</button>
            <button v-else class="modal-btn" @click="finishTask">签到完成，确认打卡 ✅</button>
          </template>

          <!-- 阅读小知识 -->
          <template v-else-if="activeTask && activeTask.id === 'read'">
            <div class="modal-emoji">📖</div>
            <h3 class="modal-title">阅读萌宠小知识</h3>
            <div class="read-box">
              <p>猫的胡须（触须）根部布满神经，是重要的「触觉雷达」：</p>
              <p>① 黑暗中感知障碍与猎物位置；</p>
              <p>② 判断缝隙能否通过（胡须宽度≈身体宽度）；</p>
              <p>③ 根部有本体感受器，帮助保持平衡。</p>
              <p>所以千万别随意剪猫咪的胡须哦～</p>
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
            <p class="quiz-q">狗狗主要靠什么方式散热降温？</p>
            <div class="quiz-opts">
              <button
                v-for="o in quizOpts"
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
          <template v-else-if="activeTask && activeTask.id === 'post'">
            <div class="modal-emoji">✍️</div>
            <h3 class="modal-title">发布一条动态</h3>
            <p class="modal-sub">分享你和毛孩子的日常，发布后即可完成任务。</p>
            <textarea
              v-model="postText"
              class="post-area"
              placeholder="说点什么吧～（例如：今天带猫主子晒太阳啦）"
            ></textarea>
            <button class="modal-btn" :disabled="!postText.trim()" @click="doPost">发布 🚀</button>
          </template>

          <!-- 给好友动态点赞 -->
          <template v-else-if="activeTask && activeTask.id === 'like'">
            <div class="modal-emoji">💗</div>
            <h3 class="modal-title">给社区动态点赞</h3>
            <p class="modal-sub">在萌宠社区挑一条动态，送出你的爱心 ❤️</p>
            <ul class="like-list">
              <li
                v-for="(p, i) in communityPosts"
                :key="i"
                class="like-item"
                :class="{ liked: likePicked === i }"
                @click="likePicked = i"
              >
                <span class="like-ava">{{ p.user.charAt(0) }}</span>
                <div class="like-main">
                  <b>{{ p.user }}</b>
                  <p>{{ p.text }}</p>
                </div>
                <span class="like-heart" :class="{ on: likePicked === i }">❤</span>
              </li>
            </ul>
            <button class="modal-btn" :disabled="likePicked === null" @click="finishTask">
              送出爱心
            </button>
          </template>

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
    if (u) await loadState(u)
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
const signLit = ref([false, false, false, false, false, false, false])
const signCount = computed(() => signLit.value.filter(Boolean).length)
const signAllLit = computed(() => signLit.value.every(Boolean))
function signClick(n) {
  if (signLit.value[n - 1]) return
  signLit.value[n - 1] = true
}
const readChecked = ref(false)
const quizPicked = ref('')
const quizWrong = ref(false)
const quizSolved = ref(false)
const quizOpts = [
  { k: 'a', t: '舌头和脚垫出汗', right: true },
  { k: 'b', t: '耳朵大幅扇动', right: false },
  { k: 'c', t: '全身毛孔排汗', right: false },
  { k: 'd', t: '尾巴左右甩动', right: false }
]
const walkStarted = ref(false)
const walkItems = ref([])
const walkCollected = ref(0)
const postText = ref('')
const likePicked = ref(null)
const communityPosts = [
  { user: '喵星人管家', text: '今天我家布偶终于肯让我梳毛了，感动！' },
  { user: '柯基铲屎官', text: '蜜桃臀今天又卡在沙发缝里了哈哈哈' },
  { user: '仓鼠囤粮王', text: '新买的磨牙棒它一口就啃秃了…' },
  { user: '鹦鹉吹哨手', text: '教了三天，它终于学会吹生日歌前两句！' }
]

function openTask(t) {
  if (t.id === 'jump') {
    // 跳障碍以独立弹窗运行：任务模式下胜利会自动完成任务
    activeTask.value = t
    showJumpModal.value = true
    nextTick(() => startJump('task'))
    return
  }
  activeTask.value = t
  signLit.value = [false, false, false, false, false, false, false]
  readChecked.value = false
  quizPicked.value = ''
  quizWrong.value = false
  quizSolved.value = false
  walkStarted.value = false
  walkItems.value = []
  walkCollected.value = 0
  postText.value = ''
  likePicked.value = null
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

function doPost() {
  if (!postText.value.trim()) return
  finishTask()
}

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
.post-area {
  width: 100%;
  min-height: 90px;
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 12px;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
  font-family: inherit;
  color: #445;
}
.post-area:focus { border-color: #b98bff; outline: none; }

/* 点赞 */
.like-list { list-style: none; padding: 0; margin: 0; text-align: left; }
.like-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  border: 2px solid #f0f0f5;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.like-item.liked { border-color: #ff6fa8; background: #fff0f6; }
.like-ava {
  width: 38px; height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b7bff, #ff6fa8);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
}
.like-main { flex: 1; }
.like-main b { font-size: 14px; color: #334; }
.like-main p { font-size: 12px; color: #889; margin: 2px 0 0; }
.like-heart { font-size: 22px; color: #ddd; transition: all 0.2s; }
.like-heart.on { color: #ff4f7b; transform: scale(1.2); }

/* 弹窗动画 */
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
