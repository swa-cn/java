<template>
  <section class="latest">
    <header class="latest-head">
      <div>
        <h2 class="latest-title">
          <span class="latest-dot">动</span>
          最新动态
        </h2>
        <p class="latest-desc">大家最近晒的萌物，点开看更多</p>
      </div>
      <button type="button" class="latest-more" @click="$emit('more')">去发现更多 →</button>
    </header>

    <div v-if="loading" class="latest-tip">正在加载动态…</div>

    <div v-else-if="posts.length" class="latest-grid">
      <PostCard
        v-for="p in posts"
        :key="p.id"
        :post="p"
        :user-key="userKey"
        @open="$emit('open', $event)"
      />
    </div>

    <div v-else class="latest-empty">
      <PetMascot :pet="todayPet" class="latest-cat" />
      <p>还没有动态哦，去「发现」发第一条吧~</p>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PostCard from './PostCard.vue'
import PetMascot from './PetMascot.vue'
import { fetchPosts } from '../api/post'
import { useTodayPet } from '../composables/dailyPet'

const props = defineProps({
  userKey: { type: String, default: '' }
})
defineEmits(['more', 'open'])

// 首页只展示最近 6 条，完整流在「发现」页
const MAX = 6
const posts = ref([])
const loading = ref(true)
const todayPet = useTodayPet()

onMounted(async () => {
  try {
    const res = await fetchPosts({})
    posts.value = ((res && res.items) || []).slice(0, MAX)
  } catch (e) {
    posts.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.latest { padding: 10px 30px 6px; }
.latest-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.latest-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 800;
}
.latest-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink) 0%, var(--candy) 100%);
  color: var(--on-accent);
  font-size: 12px;
  font-weight: 800;
}
.latest-desc { margin: 0; font-size: 12px; font-weight: 700; color: var(--ink-dim); }
.latest-more {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.latest-more:hover { background: var(--soft); border-color: var(--pink); transform: translateY(-1px); }
.latest-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.latest-tip,
.latest-empty { text-align: center; color: var(--ink-soft); padding: 24px 10px 30px; }
.latest-cat {
  position: static;
  width: 84px;
  height: 78px;
  margin: 0 auto 10px;
  filter: drop-shadow(0 8px 14px rgba(124, 78, 140, 0.3));
}
.latest-empty p { margin: 6px 0; font-size: 14px; }

@media (max-width: 900px) {
  .latest-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .latest { padding: 10px 16px 4px; }
  .latest-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>
