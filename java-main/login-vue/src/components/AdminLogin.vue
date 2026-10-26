<template>
  <div class="admin-login">
    <div class="login-card">
      <div class="login-head">
        <div class="shield">⚙️</div>
        <h1 class="login-title">后台管理系统</h1>
        <p class="login-sub">仅限管理员登录 · 无注册入口</p>
      </div>

      <form class="login-form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field-label">管理员账号</span>
          <input
            v-model.trim="account"
            class="field-input"
            type="text"
            name="admin-account"
            autocomplete="username"
            placeholder="请输入管理员账号"
            :disabled="loading"
          />
        </label>

        <label class="field">
          <span class="field-label">密码</span>
          <input
            v-model="password"
            class="field-input"
            type="password"
            name="admin-password"
            autocomplete="current-password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </label>

        <p v-if="error" class="form-error">{{ error }}</p>

        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '登录中…' : '登录后台' }}
        </button>
      </form>

      <button type="button" class="back-link" @click="$emit('back')">← 返回首页</button>
    </div>

    <!-- 轻提示 -->
    <transition name="fade">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminLogin } from '../api/admin'

const emit = defineEmits(['back', 'logged-in'])

const account = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const toast = ref('')
let toastTimer = null

function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

async function onSubmit() {
  error.value = ''
  if (!account.value) {
    error.value = '请输入管理员账号'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  loading.value = true
  try {
    const data = await adminLogin(account.value, password.value)
    showToast('登录成功，正在进入后台…')
    emit('logged-in', data.user || { username: account.value })
  } catch (e) {
    if (e.status === 401) {
      error.value = e.code === 'notfound' ? '账号不存在' : e.code === 'wrongpw' ? '密码错误' : (e.message || '登录失败')
    } else if (e.status === 403) {
      error.value = '该账号不是管理员账号'
    } else if (e.status === 0 || e instanceof TypeError) {
      error.value = '后端服务未启动，请先启动 springBootTest（端口 8081）'
    } else {
      error.value = e.message || '登录失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  width: 100%;
  display: grid;
  place-items: center;
  padding: 24px 0;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--card);
  border: 4px solid #fff;
  border-radius: 26px;
  box-shadow: var(--shadow);
  padding: 30px 30px 22px;
  animation: pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.login-head { text-align: center; margin-bottom: 20px; }
.shield {
  width: 58px;
  height: 58px;
  margin: 0 auto 10px;
  display: grid;
  place-items: center;
  font-size: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b7bff 0%, #6a5ae0 100%);
  border: 3px solid #fff;
  box-shadow: 0 10px 20px -8px rgba(106, 90, 224, 0.6);
}
.login-title { margin: 0; font-size: 21px; font-weight: 800; }
.login-sub { margin: 6px 0 0; font-size: 12px; font-weight: 700; color: var(--ink-dim); }

.login-form { display: grid; gap: 14px; }
.field { display: grid; gap: 6px; }
.field-label { font-size: 12.5px; font-weight: 800; color: var(--ink-soft); }
.field-input {
  border: 2px solid var(--line);
  background: #fdf6fb;
  border-radius: 14px;
  padding: 12px 14px;
  font-family: inherit;
  font-size: 14.5px;
  color: var(--ink);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}
.field-input:focus {
  border-color: #8b7bff;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(139, 123, 255, 0.16);
}
.field-input::placeholder { color: #c9bccf; }

.form-error {
  margin: 0;
  padding: 9px 12px;
  border-radius: 12px;
  background: #fff0f3;
  border: 2px solid #ffd3dd;
  color: #d6336c;
  font-size: 12.5px;
  font-weight: 700;
}

.submit-btn {
  border: 0;
  border-radius: 999px;
  padding: 13px 16px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, #8b7bff 0%, #6a5ae0 60%, #ff6fa8 140%);
  box-shadow: 0 10px 20px -8px rgba(106, 90, 224, 0.6), inset 0 -3px 0 rgba(0, 0, 0, 0.08);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.submit-btn:hover { transform: translateY(-2px); filter: brightness(1.04); }
.submit-btn:active { transform: scale(0.97); }
.submit-btn:disabled { opacity: 0.65; cursor: not-allowed; transform: none; }

.back-link {
  margin: 16px auto 0;
  display: block;
  border: 0;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-dim);
  cursor: pointer;
  transition: color 0.18s ease;
}
.back-link:hover { color: var(--pink-deep); }

.toast {
  position: fixed;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  background: rgba(74, 59, 82, 0.92);
  color: #fff;
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 13.5px;
  font-weight: 700;
  z-index: 50;
  box-shadow: 0 12px 26px -10px rgba(0, 0, 0, 0.4);
}
.fade-enter-active,
.fade-leave-active { transition: opacity 0.25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
