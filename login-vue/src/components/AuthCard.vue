<template>
  <section
    class="auth-card"
    :class="{ celebrate: celebrating }"
    id="authCard"
    ref="cardEl"
  >
    <!-- Q版猫咪吉祥物：输入密码时会捂住眼睛 -->
    <CatMascot :covering="mascotCovering" />

    <header class="brand">
      <h1 class="brand-name">欢迎回来<span class="tilde">~</span></h1>
      <p class="brand-sub">{{ brandSub }}</p>
    </header>

    <div class="tabs" :data-active="activeTab" role="tablist" aria-label="登录或注册">
      <button
        class="tab"
        :class="{ 'is-active': activeTab === 'login' }"
        data-tab="login"
        role="tab"
        :aria-selected="activeTab === 'login'"
        @click="switchTab('login')"
      >
        登录
      </button>
      <button
        class="tab"
        :class="{ 'is-active': activeTab === 'register' }"
        data-tab="register"
        role="tab"
        :aria-selected="activeTab === 'register'"
        @click="switchTab('register')"
      >
        注册
      </button>
      <span class="tab-indicator" aria-hidden="true"></span>
    </div>

    <!-- 登录 -->
    <form
      v-show="activeTab === 'login'"
      id="loginForm"
      class="form"
      novalidate
      @submit.prevent="onLogin"
    >
      <div class="field" :class="{ invalid: errors.loginAccount }">
        <label for="loginAccount">邮箱或用户名</label>
        <input
          id="loginAccount"
          v-model="loginAccount"
          name="account"
          type="text"
          autocomplete="username"
          inputmode="email"
          required
          maxlength="120"
          placeholder="你注册时用的名字"
        />
        <small class="error">{{ errors.loginAccount }}</small>
      </div>

      <div class="field" :class="{ invalid: errors.loginPassword }">
        <label for="loginPassword">密码</label>
        <div class="input-wrap">
          <input
            id="loginPassword"
            v-model="loginPassword"
            name="password"
            :type="loginPwVisible ? 'text' : 'password'"
            autocomplete="current-password"
            required
            maxlength="128"
            placeholder="悄悄输入，小猫不会偷看"
            @focus="loginPwFocused = true"
            @blur="loginPwFocused = false"
          />
          <button
            type="button"
            class="toggle-pw"
            aria-label="显示或隐藏密码"
            @click="togglePw('login')"
          >
            {{ loginPwVisible ? '隐藏' : '显示' }}
          </button>
        </div>
        <small class="error">{{ errors.loginPassword }}</small>
      </div>

      <label class="checkbox">
        <input type="checkbox" id="rememberMe" v-model="rememberMe" />
        <span>30 天内记住我</span>
      </label>

      <button type="submit" class="btn-primary" :disabled="loginBtnDisabled">
        {{ loginBtnText }}
      </button>
      <p class="form-msg" :class="loginMsgClass" aria-live="polite">{{ loginMsg }}</p>
    </form>

    <!-- 注册 -->
    <form
      v-show="activeTab === 'register'"
      id="registerForm"
      class="form"
      novalidate
      @submit.prevent="onRegister"
    >
      <div class="field" :class="{ invalid: errors.regUsername }">
        <label for="regUsername">用户名</label>
        <input
          id="regUsername"
          v-model="regUsername"
          name="username"
          type="text"
          autocomplete="username"
          required
          maxlength="32"
          placeholder="3-20 位字母、数字或下划线"
        />
        <small class="error">{{ errors.regUsername }}</small>
      </div>

      <div class="field" :class="{ invalid: errors.regEmail }">
        <label for="regEmail">邮箱</label>
        <input
          id="regEmail"
          v-model="regEmail"
          name="email"
          type="email"
          autocomplete="email"
          required
          maxlength="120"
          placeholder="you@example.com"
        />
        <small class="error">{{ errors.regEmail }}</small>
      </div>

      <div class="field" :class="{ invalid: errors.regPassword }">
        <label for="regPassword">密码</label>
        <div class="input-wrap">
          <input
            id="regPassword"
            v-model="regPassword"
            name="password"
            :type="regPwVisible ? 'text' : 'password'"
            autocomplete="new-password"
            required
            maxlength="128"
            placeholder="至少 8 位，含字母和数字"
            @focus="regPwFocused = true"
            @blur="regPwFocused = false"
            @input="onRegPwInput"
          />
          <button
            type="button"
            class="toggle-pw"
            aria-label="显示或隐藏密码"
            @click="togglePw('reg')"
          >
            {{ regPwVisible ? '隐藏' : '显示' }}
          </button>
        </div>
        <div class="strength" :class="strengthClass">
          <span class="strength-bar"><i></i><i></i><i></i><i></i></span>
          <em class="strength-label">{{ strengthLabel }}</em>
        </div>
        <small class="error">{{ errors.regPassword }}</small>
      </div>

      <div class="field" :class="{ invalid: errors.regConfirm }">
        <label for="regConfirm">确认密码</label>
        <input
          id="regConfirm"
          v-model="regConfirm"
          name="confirm"
          type="password"
          autocomplete="new-password"
          required
          maxlength="128"
          placeholder="再输一遍"
        />
        <small class="error">{{ errors.regConfirm }}</small>
      </div>

      <button type="submit" class="btn-primary" :disabled="regBtnDisabled">
        {{ regBtnText }}
      </button>
      <p class="form-msg" :class="registerMsgClass" aria-live="polite">{{ registerMsg }}</p>
    </form>

    <p class="legal">
      继续即表示你同意
      <a href="javascript:void(0)">服务条款</a>
      与
      <a href="javascript:void(0)">隐私政策</a>
    </p>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import CatMascot from './CatMascot.vue'
import { useAuth, RE_EMAIL, RE_USER } from '../composables/useAuth'
import { burstConfetti } from '../composables/useConfetti'

const emit = defineEmits(['logged-in'])
const auth = useAuth()

/* ---------- 标签页 ---------- */
const activeTab = ref('login')
const brandSub = ref('登录你的小窝，继续未完成的事')

function switchTab(name) {
  activeTab.value = name
  brandSub.value =
    name === 'login'
      ? '登录你的小窝，继续未完成的事'
      : '注册一个新账号，马上出发'
  // 切换时清空消息与焦点态
  loginMsg.value = ''
  registerMsg.value = ''
  loginPwFocused.value = false
  regPwFocused.value = false
}

/* ---------- 表单数据 ---------- */
const loginAccount = ref('')
const loginPassword = ref('')
const rememberMe = ref(false)

const regUsername = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regConfirm = ref('')

const loginPwVisible = ref(false)
const regPwVisible = ref(false)
const loginPwFocused = ref(false)
const regPwFocused = ref(false)

const errors = reactive({
  loginAccount: '',
  loginPassword: '',
  regUsername: '',
  regEmail: '',
  regPassword: '',
  regConfirm: ''
})

const loginBtnText = ref('登录')
const loginBtnDisabled = ref(false)
const registerBtnText = ref('创建账号')
const registerBtnDisabled = ref(false)

const loginMsg = ref('')
const registerMsg = ref('')

const loginMsgClass = computed(() =>
  loginMsg.value ? (loginOk.value ? 'ok' : 'bad') : ''
)
const registerMsgClass = computed(() =>
  registerMsg.value ? (registerOk.value ? 'ok' : 'bad') : ''
)
const loginOk = ref(true)
const registerOk = ref(true)

const cardEl = ref(null)
const celebrating = ref(false)

/* ---------- 密码显示/隐藏 ---------- */
function togglePw(which) {
  if (which === 'login') loginPwVisible.value = !loginPwVisible.value
  else regPwVisible.value = !regPwVisible.value
}

/* ---------- 吉祥物：输密码且隐藏时捂眼睛 ---------- */
const mascotCovering = computed(
  () =>
    (loginPwFocused.value && !loginPwVisible.value) ||
    (regPwFocused.value && !regPwVisible.value)
)

/* ---------- 密码强度 ---------- */
const STRENGTH_LABELS = ['强度', '弱', '一般', '较强', '超强']
const strengthLevel = ref(0)
const strengthClass = computed(() =>
  strengthLevel.value ? 'strength s' + strengthLevel.value : 'strength'
)
const strengthLabel = computed(() => STRENGTH_LABELS[strengthLevel.value])

function strengthOf(pw) {
  if (!pw) return 0
  let score = 0
  if (pw.length >= 8) score++
  if (pw.length >= 12) score++
  if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++
  if (/[0-9]/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++
  return Math.min(score, 4)
}
function onRegPwInput() {
  strengthLevel.value = strengthOf(regPassword.value)
}

/* ---------- 校验 ---------- */
function validateLogin(account, password) {
  if (!account) return '请输入邮箱或用户名'
  if (!password) return '请输入密码'
  if (password.length > 128) return '密码长度异常'
  return ''
}
function validateRegister(u, email, pw, confirm) {
  if (!RE_USER.test(u)) return '用户名需为 3-20 位字母、数字或下划线'
  if (!RE_EMAIL.test(email)) return '邮箱格式不正确'
  if (pw.length < 8) return '密码至少 8 位'
  if (!(/[A-Za-z]/.test(pw) && /[0-9]/.test(pw))) return '密码需同时包含字母和数字'
  if (pw.length > 128) return '密码过长'
  if (pw !== confirm) return '两次输入的密码不一致'
  return ''
}

/* ---------- 彩带庆祝 ---------- */
function celebrate() {
  const el = cardEl.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  burstConfetti(rect.left + rect.width / 2, rect.top + rect.height * 0.3, 42)
  celebrating.value = false
  // 强制重排以便重新触发 jelly 动画
  void el.offsetWidth
  celebrating.value = true
  setTimeout(() => {
    celebrating.value = false
  }, 750)
}

/* ---------- 登录 ---------- */
async function onLogin() {
  errors.loginAccount = ''
  errors.loginPassword = ''
  loginMsg.value = ''
  loginOk.value = true

  const account = loginAccount.value.trim()
  const password = loginPassword.value
  const general = validateLogin(account, password)
  if (general) {
    loginMsg.value = general
    loginOk.value = false
    return
  }

  loginBtnDisabled.value = true
  loginBtnText.value = '登录中…'
  try {
    const res = await auth.login({
      account,
      password,
      remember: rememberMe.value
    })
    loginBtnDisabled.value = false
    loginBtnText.value = '登录'
    if (!res.ok) {
      if (res.error === 'notfound') {
        loginMsg.value = '没有找到这个账号哦'
      } else if (res.error === 'wrongpw') {
        loginMsg.value = '密码不对，再想想~'
      }
      loginOk.value = false
      return
    }
    celebrate()
    emit('logged-in', res.user)
  } catch (e) {
    loginBtnDisabled.value = false
    loginBtnText.value = '登录'
    loginMsg.value = '出了点小状况，请重试'
    loginOk.value = false
  }
}

/* ---------- 注册 ---------- */
async function onRegister() {
  errors.regUsername = ''
  errors.regEmail = ''
  errors.regPassword = ''
  errors.regConfirm = ''
  registerMsg.value = ''
  registerOk.value = true

  const u = regUsername.value.trim()
  const email = regEmail.value.trim()
  const pw = regPassword.value
  const confirm = regConfirm.value

  const general = validateRegister(u, email, pw, confirm)
  if (general) {
    registerMsg.value = general
    registerOk.value = false
    return
  }

  registerBtnDisabled.value = true
  registerBtnText.value = '创建中…'
  try {
    const res = await auth.register({ username: u, email, password: pw })
    registerBtnDisabled.value = false
    registerBtnText.value = '创建账号'
    if (!res.ok) {
      if (res.error === 'username') {
        errors.regUsername = '这个用户名已经被占啦'
      } else if (res.error === 'email') {
        errors.regEmail = '这个邮箱已经注册过啦'
      }
      return
    }
    registerMsg.value = '注册成功，快去登录吧！'
    registerOk.value = true
    resetRegister()
    strengthLevel.value = 0
    celebrate()
    switchTab('login')
  } catch (e) {
    registerBtnDisabled.value = false
    registerBtnText.value = '创建账号'
    registerMsg.value = '出了点小状况，请重试'
    registerOk.value = false
  }
}

function resetRegister() {
  regUsername.value = ''
  regEmail.value = ''
  regPassword.value = ''
  regConfirm.value = ''
  regPwVisible.value = false
  regPwFocused.value = false
}
</script>
