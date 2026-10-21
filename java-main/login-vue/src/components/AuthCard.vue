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
      <p class="brand-sub">登录你的小窝，继续未完成的事</p>
    </header>

    <form id="loginForm" class="form" novalidate @submit.prevent="onLogin">
      <div class="field" :class="{ invalid: errors.loginAccount }">
        <label for="loginAccount">邮箱或用户名</label>
        <input
          id="loginAccount"
          ref="elAccount"
          v-model="loginAccount"
          name="account"
          type="text"
          autocomplete="username"
          inputmode="email"
          required
          maxlength="120"
          placeholder="你注册时用的名字"
        />
        <small v-if="errors.loginAccount" class="error">{{ errors.loginAccount }}</small>
      </div>

      <div class="field" :class="{ invalid: errors.loginPassword }">
        <label for="loginPassword">密码</label>
        <div class="input-wrap">
          <input
            id="loginPassword"
            ref="elPassword"
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
            @click="loginPwVisible = !loginPwVisible"
          >
            {{ loginPwVisible ? '隐藏' : '显示' }}
          </button>
        </div>
        <small v-if="errors.loginPassword" class="error">{{ errors.loginPassword }}</small>
      </div>

      <label class="checkbox">
        <input type="checkbox" v-model="rememberMe" />
        <span>30 天内记住我</span>
      </label>

      <button type="submit" class="btn-primary" :disabled="loginBtnDisabled">
        {{ loginBtnText }}
      </button>
      <p class="form-msg" :class="loginMsgClass" aria-live="polite">{{ loginMsg }}</p>

      <!-- 注册入口：跳转到独立注册页 -->
      <p class="alt-line">
        还没有账号？
        <button type="button" class="link-btn" @click="$emit('go-register')">立即注册</button>
      </p>
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
import { computed, nextTick, reactive, ref } from 'vue'
import CatMascot from './CatMascot.vue'
import { useAuth } from '../composables/useAuth'
import { loginUser, apiConfig } from '../api/auth'
import { burstConfetti } from '../composables/useConfetti'

const emit = defineEmits(['logged-in', 'go-register'])
const props = defineProps({
  // 从注册页返回时，把用户名带过来直接填好
  prefillAccount: { type: String, default: '' }
})
const auth = useAuth()

/* ---------- 表单数据 ---------- */
const loginAccount = ref(props.prefillAccount || '')
const loginPassword = ref('')
const rememberMe = ref(false)

const loginPwVisible = ref(false)
const loginPwFocused = ref(false)

const elAccount = ref(null)
const elPassword = ref(null)

const errors = reactive({
  loginAccount: '',
  loginPassword: ''
})

const loginBtnText = ref('登录')
const loginBtnDisabled = ref(false)
const loginMsg = ref('')
const loginOk = ref(true)
const loginMsgClass = computed(() =>
  loginMsg.value ? (loginOk.value ? 'ok' : 'bad') : ''
)

const cardEl = ref(null)
const celebrating = ref(false)

/* ---------- 吉祥物：密码聚焦且隐藏时捂眼睛 ---------- */
const mascotCovering = computed(() => loginPwFocused.value && !loginPwVisible.value)

/* ---------- 校验 ---------- */
function validateLogin(account, password) {
  if (!account) return { field: 'loginAccount', message: '请输入邮箱或用户名' }
  if (!password) return { field: 'loginPassword', message: '请输入密码' }
  if (password.length > 128) return { field: 'loginPassword', message: '密码长度异常' }
  return null
}

async function focusField(elRef) {
  await nextTick()
  const el = elRef?.value
  if (el && typeof el.focus === 'function') el.focus()
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
    errors[general.field] = general.message
    focusField(general.field === 'loginAccount' ? elAccount : elPassword)
    return
  }

  loginBtnDisabled.value = true
  loginBtnText.value = '登录中…'
  try {
    let user
    if (apiConfig.USE_MOCK) {
      const res = await auth.login({ account, password, remember: rememberMe.value })
      if (!res.ok) {
        if (res.error === 'notfound') {
          errors.loginAccount = '没有找到这个账号哦'
          focusField(elAccount)
        } else if (res.error === 'wrongpw') {
          errors.loginPassword = '密码不对，再想想~'
          focusField(elPassword)
        } else {
          loginMsg.value = '出了点小状况，请重试'
          loginOk.value = false
        }
        return
      }
      user = res.user
    } else {
      const data = await loginUser({ account, password, remember: rememberMe.value })
      auth.setCurrentUser(data.user, rememberMe.value)
      user = data.user
    }
    loginBtnDisabled.value = false
    loginBtnText.value = '登录'
    celebrate()
    emit('logged-in', user)
  } catch (e) {
    loginBtnDisabled.value = false
    loginBtnText.value = '登录'
    const code = e && e.code
    if (code === 'notfound') {
      errors.loginAccount = '没有找到这个账号哦'
      focusField(elAccount)
    } else if (code === 'wrongpw') {
      errors.loginPassword = '密码不对，再想想~'
      focusField(elPassword)
    } else {
      loginMsg.value = (e && e.message) || '出了点小状况，请重试'
      loginOk.value = false
    }
  }
}
</script>
