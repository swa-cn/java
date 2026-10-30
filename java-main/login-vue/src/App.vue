<template>
  <SceneBackground />

  <!-- 主题切换：首页把按钮集成在顶部导航里，其余页面用右上角悬浮按钮 -->
  <ThemeToggle v-if="view !== 'home'" floating />

  <main class="auth-shell" :class="{ 'is-wide': view === 'register' || view === 'home' || view === 'admin' }">
    <AuthCard
      v-if="view === 'auth'"
      :prefill-account="prefillAccount"
      @logged-in="onLoggedIn"
      @go-register="onGoRegister"
    />
    <RegisterPage v-else-if="view === 'register'" @go-login="onGoLogin" />
    <AdminLogin v-else-if="view === 'adminLogin'" @back="onAdminBack" @logged-in="onAdminLoggedIn" />
    <AdminPanel v-else-if="view === 'admin'" :admin="adminUser" @exit="onAdminExit" />
    <Home v-else :user="currentUser" @logout="onLogout" @go-admin="onGoAdmin" />
  </main>
</template>

<script setup>
import { ref } from 'vue'
import SceneBackground from './components/SceneBackground.vue'
import AuthCard from './components/AuthCard.vue'
import RegisterPage from './components/RegisterPage.vue'
import Home from './components/Home.vue'
import AdminLogin from './components/AdminLogin.vue'
import AdminPanel from './components/AdminPanel.vue'
import ThemeToggle from './components/ThemeToggle.vue'
import { useAuth } from './composables/useAuth'
import { adminMe } from './api/admin'

const { state, restoreSession, logout } = useAuth()

const view = ref('auth') // 'auth' | 'register' | 'home' | 'adminLogin' | 'admin'
const currentUser = ref(null)
const prefillAccount = ref('')
// 后台管理系统：管理员信息独立于前台用户态，进后台不覆盖前台会话
const adminUser = ref(null)
// 记住从哪个页面进的后台：退出后台后原样返回
const adminFrom = ref('home')

// 启动即尝试恢复会话
restoreSession()
if (state.currentUser) {
  currentUser.value = state.currentUser
  view.value = 'home'
}

function onGoRegister() {
  prefillAccount.value = ''
  view.value = 'register'
}

function onGoLogin(payload) {
  prefillAccount.value = (payload && payload.account) || ''
  view.value = 'auth'
}

function onLoggedIn(user) {
  currentUser.value = user
  prefillAccount.value = ''
  view.value = 'home'
}

function onLogout() {
  logout()
  currentUser.value = null
  view.value = 'auth'
}

/* ---------------- 后台管理系统 ---------------- */

/**
 * 首页导航「后台管理」：
 * 管理员在前台登录时后端已授予后台会话（同一个 Cookie），
 * 因此先探测 /api/admin/me —— 已有后台会话就直接进面板，无需再次登录；
 * 会话过期等异常情况才回退到后台登录页。
 */
async function onGoAdmin() {
  adminFrom.value = 'home'
  try {
    const me = await adminMe()
    adminUser.value = me.user
    view.value = 'admin'
  } catch (e) {
    view.value = 'adminLogin'
  }
}

/** 后台登录页点「返回首页」：回到来源页（用户未登录时仍是登录页） */
function onAdminBack() {
  view.value = adminFrom.value
}

/** 后台登录成功：进入管理系统（不影响前台用户会话） */
function onAdminLoggedIn(user) {
  adminUser.value = user
  view.value = 'admin'
}

/** 退出后台 / 会话过期：清空管理员态并返回来源页 */
function onAdminExit() {
  adminUser.value = null
  view.value = adminFrom.value
}
</script>
