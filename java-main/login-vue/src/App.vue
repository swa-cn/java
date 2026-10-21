<template>
  <SceneBackground />

  <main class="auth-shell" :class="{ 'is-wide': view === 'register' || view === 'home' }">
    <AuthCard
      v-if="view === 'auth'"
      :prefill-account="prefillAccount"
      @logged-in="onLoggedIn"
      @go-register="onGoRegister"
    />
    <RegisterPage v-else-if="view === 'register'" @go-login="onGoLogin" />
    <Home v-else :user="currentUser" @logout="onLogout" />
  </main>
</template>

<script setup>
import { ref } from 'vue'
import SceneBackground from './components/SceneBackground.vue'
import AuthCard from './components/AuthCard.vue'
import RegisterPage from './components/RegisterPage.vue'
import Home from './components/Home.vue'
import { useAuth } from './composables/useAuth'

const { state, restoreSession, logout } = useAuth()

const view = ref('auth') // 'auth' | 'register' | 'home'
const currentUser = ref(null)
const prefillAccount = ref('')

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
</script>
