<template>
  <SceneBackground />

  <main class="auth-shell">
    <AuthCard v-if="view === 'auth'" @logged-in="onLoggedIn" />
    <Dashboard v-else :user="currentUser" @logout="onLogout" />
  </main>
</template>

<script setup>
import { ref } from 'vue'
import SceneBackground from './components/SceneBackground.vue'
import AuthCard from './components/AuthCard.vue'
import Dashboard from './components/Dashboard.vue'
import { useAuth } from './composables/useAuth'

const { state, restoreSession, logout } = useAuth()

const view = ref('auth')
const currentUser = ref(null)

// 启动即尝试恢复会话
restoreSession()
if (state.currentUser) {
  currentUser.value = state.currentUser
  view.value = 'dashboard'
}

function onLoggedIn(user) {
  currentUser.value = user
  view.value = 'dashboard'
}

function onLogout() {
  logout()
  currentUser.value = null
  view.value = 'auth'
}
</script>
