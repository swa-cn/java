<template>
  <div class="category-page">
    <!-- 顶部横幅 -->
    <header class="cp-hero" :style="{ background: heroBg }">
      <button type="button" class="cp-back" @click="$emit('back')" aria-label="返回首页">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        返回
      </button>

      <div class="cp-hero-body">
        <img class="cp-hero-cover" :src="category.image" :alt="category.name" />
        <div class="cp-hero-text">
          <span class="cp-hero-icon" :style="{ background: category.color }">{{ category.icon }}</span>
          <h2 class="cp-hero-name">{{ category.name }}</h2>
          <p class="cp-hero-count">共 {{ category.breeds.length }} 个品种 · 点击查看详情</p>
        </div>
      </div>
    </header>

    <!-- 品种图鉴 -->
    <section class="cp-breeds">
      <h3 class="cp-section-title">
        <span class="cp-dot" :style="{ background: category.color }">{{ category.icon }}</span>
        {{ category.name }} · 品种图鉴
      </h3>

      <div class="cp-grid">
        <button
          v-for="b in category.breeds"
          :key="b.name"
          type="button"
          class="cp-card"
          @click="$emit('select-breed', b)"
        >
          <img class="cp-card-cover" :src="b.image" :alt="b.name" loading="lazy" />
          <span class="cp-card-name">{{ b.name }}</span>
          <p class="cp-card-desc">{{ b.desc }}</p>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  category: { type: Object, required: true }
})
defineEmits(['back', 'select-breed'])

// 以分类主题色生成柔和渐变横幅
const heroBg = computed(
  () => `linear-gradient(135deg, ${props.category.color}22 0%, ${props.category.color}44 55%, #fff0f7 100%)`
)
</script>

<style scoped>
.category-page {
  animation: pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* ---------- 顶部横幅 ---------- */
.cp-hero {
  position: relative;
  padding: 22px 30px 24px;
  border-bottom: 2px dashed var(--line);
}
.cp-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 2px solid #fff;
  background: rgba(255, 255, 255, 0.75);
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 14px 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 6px 14px -6px rgba(124, 78, 140, 0.35);
  transition: transform 0.14s ease, background 0.18s ease;
}
.cp-back svg { width: 16px; height: 16px; }
.cp-back:hover { background: #fff; transform: translateX(-2px); }

.cp-hero-body {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 16px;
}
.cp-hero-cover {
  width: 108px;
  height: 108px;
  border-radius: 26px;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 14px 26px -10px rgba(124, 78, 140, 0.45);
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.cp-hero-icon {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  border: 2px solid #fff;
  box-shadow: 0 4px 10px -3px rgba(120, 40, 90, 0.3);
  margin-bottom: 6px;
}
.cp-hero-name { margin: 0; font-size: 26px; font-weight: 800; }
.cp-hero-count { margin: 6px 0 0; font-size: 13px; font-weight: 700; color: var(--ink-soft); }

/* ---------- 品种网格 ---------- */
.cp-breeds { padding: 18px 30px 34px; }
.cp-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  padding-left: 4px;
  font-size: 17px;
  font-weight: 800;
}
.cp-dot {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
}
.cp-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}
.cp-card {
  display: flex;
  flex-direction: column;
  text-align: left;
  border: 2px solid var(--line);
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  padding: 0 0 12px;
  font-family: inherit;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.cp-card:hover {
  transform: translateY(-4px);
  border-color: var(--pink);
  box-shadow: 0 16px 28px -12px rgba(124, 78, 140, 0.4);
}
.cp-card-cover {
  width: 100%;
  height: 120px;
  object-fit: cover;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.cp-card-name {
  margin: 10px 12px 2px;
  font-size: 15px;
  font-weight: 800;
  color: var(--ink);
}
.cp-card-desc {
  margin: 0 12px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--ink-soft);
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .cp-hero { padding: 14px 16px 20px; }
  .cp-hero-cover { width: 84px; height: 84px; border-radius: 20px; }
  .cp-hero-name { font-size: 22px; }
  .cp-breeds { padding: 14px 16px 24px; }
  .cp-grid { grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); gap: 10px; }
  .cp-card-cover { height: 100px; }
}
</style>
