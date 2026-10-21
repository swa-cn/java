<template>
  <section class="reg-card" :class="{ celebrate: celebrating }" ref="cardEl">
    <!-- ---------- 左侧品牌区 ---------- -->
    <aside class="reg-brand">
      <CatMascot class="reg-mascot" :covering="pwCovering" />

      <h1 class="reg-brand-title">加入小窝<span class="tilde">~</span></h1>
      <p class="reg-brand-sub">填几行字，马上就有自己的小天地</p>

      <ul class="reg-perks">
        <li>
          <span class="perk-dot d1">1</span>
          <span>图形验证码，一键挡住机器人</span>
        </li>
        <li>
          <span class="perk-dot d2">2</span>
          <span>密码加盐哈希，绝不明文</span>
        </li>
        <li>
          <span class="perk-dot d3">3</span>
          <span>登录可记住你 30 天</span>
        </li>
      </ul>

      <p class="reg-brand-foot">
        已经有账号了？
        <button type="button" class="link-btn" @click="backToLogin">直接登录</button>
      </p>
    </aside>

    <!-- ---------- 右侧表单区 ---------- -->
    <div class="reg-panel">
      <!-- 成功态 -->
      <div v-if="success" class="reg-done" aria-live="polite">
        <span class="done-check" aria-hidden="true">
          <svg viewBox="0 0 52 52">
            <path class="done-check-path" d="M14 27.5 L22.5 36 L38 17.5" />
          </svg>
        </span>
        <h2 class="done-title">注册成功啦</h2>
        <p class="done-sub">账号已经建好，去登录你的小窝吧</p>

        <dl class="done-info">
          <div><dt>用户名</dt><dd>{{ created.username }}</dd></div>
          <div><dt>昵称</dt><dd>{{ created.nickname || '未填写' }}</dd></div>
        </dl>

        <button type="button" class="btn-primary" @click="backToLogin">去登录</button>
        <button type="button" class="btn-ghost" @click="registerAnother">再注册一个</button>
      </div>

      <!-- 表单态 -->
      <template v-else>
        <header class="reg-head">
          <h2>创建账号</h2>
          <p>带 <i>*</i> 的为必填项，昵称之后也能改</p>
        </header>

        <form class="reg-form" novalidate @submit.prevent="onSubmit">
          <!-- 用户名 -->
          <div class="field" :class="{ invalid: errors.username }">
            <label for="regUser">用户名 <i>*</i></label>
            <div class="input-wrap has-flag">
              <input
                id="regUser"
                ref="elUsername"
                v-model="form.username"
                name="username"
                type="text"
                autocomplete="username"
                required
                maxlength="20"
                placeholder="3-20 位字母、数字或下划线"
              />
              <span class="field-flag" :class="nameStatus" aria-hidden="true"></span>
            </div>
            <small v-if="errors.username" class="error">{{ errors.username }}</small>
            <small v-else-if="nameStatus === 'taken'" class="error">这个用户名已经被占啦</small>
            <small v-else-if="nameStatus === 'ok'" class="hint ok">这个用户名可以用</small>
            <small v-else-if="nameStatus === 'checking'" class="hint">正在看看有没有人用…</small>
          </div>

          <!-- 图形验证码 -->
          <div class="field" :class="{ invalid: errors.code }">
            <label for="regCode">验证码 <i>*</i></label>
            <div class="captcha-row">
              <input
                id="regCode"
                ref="elCode"
                v-model="captchaCode"
                name="captchaCode"
                type="text"
                autocomplete="off"
                required
                maxlength="4"
                placeholder="看图输入 4 位字符"
              />
              <button
                type="button"
                class="captcha-img-btn"
                title="看不清？点一下换一张"
                aria-label="看不清？点一下换一张"
                :disabled="captchaLoading"
                @click="refreshCaptcha"
              >
                <img
                  v-if="captchaImage"
                  class="captcha-img"
                  :src="captchaImage"
                  alt="图形验证码"
                />
                <span v-else class="captcha-loading">{{ captchaLoading ? '加载中…' : '点击获取' }}</span>
              </button>
            </div>
            <small v-if="errors.code" class="error">{{ errors.code }}</small>
            <small v-else-if="codeHint" class="hint">{{ codeHint }}</small>
          </div>

          <!-- 密码 -->
          <div class="field" :class="{ invalid: errors.password }">
            <label for="regPw">密码 <i>*</i></label>
            <div class="input-wrap">
              <input
                id="regPw"
                ref="elPassword"
                v-model="form.password"
                name="password"
                :type="pwVisible ? 'text' : 'password'"
                autocomplete="new-password"
                required
                maxlength="128"
                placeholder="至少 8 位，含 2 种以上字符"
                @focus="pwFocused = true"
                @blur="pwFocused = false"
              />
              <button
                type="button"
                class="toggle-pw"
                aria-label="显示或隐藏密码"
                @click="pwVisible = !pwVisible"
              >
                {{ pwVisible ? '隐藏' : '显示' }}
              </button>
            </div>

            <div class="strength" :class="strengthClass">
              <span class="strength-bar"><i></i><i></i><i></i><i></i></span>
              <em class="strength-label">{{ strengthLabel }}</em>
            </div>

            <ul class="pw-reqs">
              <li v-for="r in rules" :key="r.key" :class="{ done: r.ok }">
                <span class="req-tick" aria-hidden="true"></span>
                {{ r.label }}
              </li>
            </ul>
            <small
              v-if="form.password && metClasses < PASSWORD_POLICY.minClasses"
              class="hint"
            >
              已满足 {{ metClasses }} 种，至少还要 {{ PASSWORD_POLICY.minClasses - metClasses }} 种字符
            </small>

            <small v-if="errors.password" class="error">{{ errors.password }}</small>
          </div>

          <!-- 确认密码 -->
          <div class="field" :class="{ invalid: errors.confirm }">
            <label for="regPw2">确认密码 <i>*</i></label>
            <div class="input-wrap has-flag">
              <input
                id="regPw2"
                ref="elConfirm"
                v-model="form.confirm"
                name="confirm"
                type="password"
                autocomplete="new-password"
                required
                maxlength="128"
                placeholder="再输一遍，确保没手滑"
              />
              <span class="field-flag" :class="confirmState" aria-hidden="true"></span>
            </div>
            <small v-if="errors.confirm" class="error">{{ errors.confirm }}</small>
            <small v-else-if="confirmState === 'bad'" class="error">两次输入的密码不一致</small>
            <small v-else-if="confirmState === 'ok'" class="hint ok">两次输入一致</small>
          </div>

          <!-- 昵称（选填） -->
          <div class="field">
            <label for="regNick">昵称（选填）</label>
            <input
              id="regNick"
              v-model="form.nickname"
              name="nickname"
              type="text"
              maxlength="24"
              placeholder="想让大家怎么叫你？"
            />
          </div>

          <!-- 条款 -->
          <label class="checkbox" :class="{ 'agree-bad': errors.agree }">
            <input type="checkbox" v-model="agreed" />
            <span>
              我已阅读并同意
              <a href="javascript:void(0)">服务条款</a>
              与
              <a href="javascript:void(0)">隐私政策</a>
            </span>
          </label>
          <small v-if="errors.agree" class="error">{{ errors.agree }}</small>

          <button type="submit" class="btn-primary" :disabled="submitting">
            {{ submitting ? '创建中…' : '创建账号' }}
          </button>

          <p class="form-msg" :class="msgClass" aria-live="polite">{{ msg }}</p>
        </form>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import CatMascot from './CatMascot.vue'
import { RE_USER, isUsernameTaken } from '../composables/useAuth'
import {
  getCaptcha,
  registerUser,
  checkUsername,
  validatePassword,
  passwordClasses,
  PASSWORD_POLICY,
  apiConfig
} from '../api/auth'
import { burstConfetti } from '../composables/useConfetti'

const emit = defineEmits(['go-login'])

/* ---------- 表单数据 ---------- */
const form = reactive({
  username: '',
  password: '',
  confirm: '',
  nickname: ''
})
const captchaCode = ref('')

const agreed = ref(false)
const pwVisible = ref(false)
const pwFocused = ref(false)
const submitting = ref(false)
const success = ref(false)
const created = ref({ username: '', nickname: '' })
const celebrating = ref(false)

const cardEl = ref(null)
const elUsername = ref(null)
const elPassword = ref(null)
const elConfirm = ref(null)
const elCode = ref(null)

const errors = reactive({
  username: '',
  code: '',
  password: '',
  confirm: '',
  agree: ''
})

const msg = ref('')
const msgOk = ref(true)
const msgClass = computed(() => (msg.value ? (msgOk.value ? 'ok' : 'bad') : ''))

/* ---------- 吉祥物：密码聚焦且隐藏时捂眼睛 ---------- */
const pwCovering = computed(() => pwFocused.value && !pwVisible.value)

/* ---------- 用户名占用检测（防抖，避免每敲一个字就查） ---------- */
const nameStatus = ref('') // '' | 'checking' | 'ok' | 'taken'
let nameTimer = null

// mock 模式走 localStorage 的 isUsernameTaken，真实后端走 check-username 接口
async function takenUsername(u) {
  if (apiConfig.USE_MOCK) return isUsernameTaken(u)
  try {
    const r = await checkUsername(u)
    return !!r.taken
  } catch (e) {
    return false
  }
}

watch(
  () => form.username,
  (value) => {
    clearTimeout(nameTimer)
    nameStatus.value = ''
    const u = value.trim()
    if (!u || !RE_USER.test(u)) return
    nameStatus.value = 'checking'
    nameTimer = setTimeout(async () => {
      nameStatus.value = (await takenUsername(u)) ? 'taken' : 'ok'
    }, 320)
  }
)

/* ---------- 图形验证码 ---------- */
const captchaImage = ref('')
const captchaId = ref('')
const captchaLoading = ref(false)
const codeHint = ref('')

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captchaId.value = res.captchaId
    captchaImage.value = res.image
    codeHint.value = '验证码 5 分钟内有效，看不清点图换一张'
  } catch (e) {
    codeHint.value = ''
    msg.value = (e && e.message) || '验证码加载失败，点一下图片重试'
    msgOk.value = false
  } finally {
    captchaLoading.value = false
  }
}

function refreshCaptcha() {
  captchaCode.value = ''
  loadCaptcha()
}

onMounted(loadCaptcha)

/* ---------- 密码强度 + 动态要求清单 ---------- */
const STRENGTH_LABELS = ['强度', '弱', '一般', '较强', '超强']
const strengthLevel = computed(() => {
  const v = form.password
  if (!v) return 0
  let score = 0
  if (v.length >= 8) score++
  if (v.length >= 12) score++
  if (/[A-Za-z]/.test(v) && /[0-9]/.test(v)) score++
  if (/[^A-Za-z0-9]/.test(v)) score++
  return Math.min(score, 4)
})
const strengthClass = computed(() =>
  strengthLevel.value ? 'strength s' + strengthLevel.value : 'strength'
)
const strengthLabel = computed(() => STRENGTH_LABELS[strengthLevel.value])

// 要求清单直接复用后端同一套策略（小写/大写/数字/符号）
const rules = computed(() => passwordClasses(form.password))
const metClasses = computed(() => rules.value.filter((r) => r.ok).length)

const confirmState = computed(() => {
  if (!form.confirm) return ''
  return form.confirm === form.password ? 'ok' : 'bad'
})

/* ---------- 交互 ---------- */
function celebrate() {
  const el = cardEl.value
  if (el) {
    const rect = el.getBoundingClientRect()
    burstConfetti(rect.left + rect.width / 2, rect.top + rect.height * 0.28, 46)
    celebrating.value = false
    void el.offsetWidth
    celebrating.value = true
    setTimeout(() => {
      celebrating.value = false
    }, 750)
  }
}

async function focusField(elRef) {
  await nextTick()
  const el = elRef?.value
  if (el && typeof el.focus === 'function') el.focus()
}

function fail(key, message, elRef) {
  errors[key] = message
  msg.value = ''
  focusField(elRef)
}

function clearErrors() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  msg.value = ''
  msgOk.value = true
}

/**
 * 把错误统一映射到对应字段上（红字提示 + 聚焦）。
 * 兼容两种形态：
 *   1) mock 模式：接口返回 { ok:false, error, message }
 *   2) 真实后端：httpPost 在 !res.ok 时抛 Error，错误码挂在 e.code 上
 * 所以错误码统一从 err.error ?? err.code 取。
 */
function applyAuthError(err) {
  const code = err && (err.error || err.code)
  const message = err && err.message
  switch (code) {
    case 'username':
      return fail('username', message || '该用户名已被占用', elUsername)
    case 'password':
      return fail('password', message || '密码不符合要求', elPassword)
    case 'code':
      // 验证码错误时这张图已作废，自动换一张新的
      refreshCaptcha()
      return fail('code', message || '验证码不正确', elCode)
    default:
      msg.value = message || '出了点小状况，请重试'
      msgOk.value = false
  }
}

async function onSubmit() {
  clearErrors()

  const u = form.username.trim()
  const nickname = form.nickname.trim()
  const pw = form.password

  if (!u) return fail('username', '请输入用户名', elUsername)
  if (!RE_USER.test(u)) return fail('username', '用户名需为 3-20 位字母、数字或下划线', elUsername)
  if (await takenUsername(u)) return fail('username', '这个用户名已经被占啦', elUsername)

  if (!captchaCode.value.trim()) return fail('code', '请输入图形验证码', elCode)
  if (!captchaId.value) return fail('code', '验证码还没加载好，点一下图片', elCode)

  if (!pw) return fail('password', '请输入密码', elPassword)
  const pv = validatePassword(pw)
  if (!pv.ok) return fail('password', pv.message, elPassword)

  if (!form.confirm) return fail('confirm', '请再输一遍密码', elConfirm)
  if (form.confirm !== pw) return fail('confirm', '两次输入的密码不一致', elConfirm)

  if (!agreed.value) {
    errors.agree = '请先阅读并同意服务条款'
    return
  }

  submitting.value = true
  try {
    const res = await registerUser({
      username: u,
      password: pw,
      nickname,
      captchaId: captchaId.value,
      captchaCode: captchaCode.value.trim()
    })
    if (!res.ok) {
      applyAuthError(res)
      return
    }
    created.value = { username: u, nickname }
    success.value = true
    celebrate()
  } catch (e) {
    // 非 mock 模式下后端报错会走这里，同样要落到对应字段
    applyAuthError(e)
  } finally {
    submitting.value = false
  }
}

function backToLogin() {
  emit('go-login', {
    account: created.value.username || form.username.trim()
  })
}

function registerAnother() {
  success.value = false
  created.value = { username: '', nickname: '' }
  clearErrors()
  form.username = ''
  form.password = ''
  form.confirm = ''
  form.nickname = ''
  agreed.value = false
  pwVisible.value = false
  nameStatus.value = ''
  refreshCaptcha()
  focusField(elUsername)
}

onBeforeUnmount(() => {
  clearTimeout(nameTimer)
})
</script>
