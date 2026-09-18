<template>
  <section class="auth-card dashboard" id="dashboard">
    <div class="avatar" aria-hidden="true">{{ avatarLetter }}</div>
    <h2 class="dash-title">欢迎回家~</h2>
    <p class="dash-sub">好久不见，<span>{{ displayName }}</span>！今天也超努力哦</p>

    <dl class="dash-info">
      <div><dt>用户名</dt><dd>{{ user.username }}</dd></div>
      <div><dt>邮箱</dt><dd>{{ user.email }}</dd></div>
      <div><dt>加入日期</dt><dd>{{ since }}</dd></div>
    </dl>

    <button type="button" class="btn-ghost" @click="$emit('logout')">退出登录</button>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: { type: Object, required: true }
})
defineEmits(['logout'])

const displayName = computed(
  () => props.user?.nickname || props.user?.username || '朋友'
)
const avatarLetter = computed(
  () => (displayName.value.charAt(0) || '喵').toUpperCase()
)
const since = computed(() =>
  props.user?.createdAt
    ? new Date(props.user.createdAt).toLocaleDateString('zh-CN')
    : '—'
)
</script>
