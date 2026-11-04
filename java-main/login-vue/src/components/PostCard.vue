<template>
  <article class="post-card" @click="$emit('open', post.id)">
    <div class="pc-cover">
      <PostMedia v-if="coverOf(post)" :media="coverOf(post)" />
      <div v-else class="pc-cover-empty" :style="{ background: tintOf(post) }">
        {{ iconOf(post) }}
      </div>
      <span v-if="(post.media || []).length > 1" class="pc-count">
        共 {{ post.media.length }} 张
      </span>
    </div>

    <div class="pc-body">
      <span class="pc-cat" :style="{ background: colorOf(post) }">{{ nameOf(post) }}</span>
      <h3 class="pc-title">{{ post.title || excerpt(post.content, 18) }}</h3>
      <p class="pc-excerpt">{{ excerpt(post.content, 44) }}</p>

      <div class="pc-meta">
        <span class="pc-author">{{ post.authorName }}</span>
        <span class="pc-time">{{ formatTime(post.createdAt) }}</span>
      </div>
      <div class="pc-stats">
        <span :class="{ liked: isLiked(post, userKey) }">
          &#9829; {{ (post.likes || []).length }}
        </span>
        <span>&#128172; {{ (post.comments || []).length }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import PostMedia from './PostMedia.vue'
import { excerpt, formatTime, isLiked } from '../composables/postStore'
import { categoryName, categoryColor, categoryIcon } from '../composables/petData'

const props = defineProps({
  post: { type: Object, required: true },
  userKey: { type: String, default: '' }
})
defineEmits(['open'])

function coverOf(p) {
  return (p.media || [])[0] || null
}
function nameOf(p) {
  return categoryName(p.category)
}
function colorOf(p) {
  return categoryColor(p.category)
}
function iconOf(p) {
  return categoryIcon(p.category)
}
function tintOf(p) {
  const c = categoryColor(p.category)
  return `linear-gradient(135deg, ${c}33 0%, #ffe9f3 100%)`
}
</script>

<style scoped>
.post-card {
  display: flex;
  flex-direction: column;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.2s ease, border-color 0.18s ease;
}
.post-card:hover {
  transform: translateY(-4px);
  border-color: var(--pink);
  box-shadow: 0 18px 32px -14px rgba(124, 78, 140, 0.45);
}
.pc-cover {
  position: relative;
  height: 146px;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
}
.pc-cover-empty {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 15px;
  font-weight: 800;
  color: var(--pink-deep);
}
.pc-count {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: var(--on-accent);
  font-size: 11px;
  font-weight: 800;
}
.pc-body { padding: 11px 13px 13px; }
.pc-cat {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  color: var(--on-accent);
  font-size: 11px;
  font-weight: 800;
}
.pc-title {
  margin: 7px 0 4px;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.35;
}
.pc-excerpt {
  margin: 0 0 10px;
  font-size: 12.5px;
  line-height: 1.5;
  color: var(--ink-soft);
  min-height: 38px;
}
.pc-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 11.5px;
  color: var(--ink-dim);
  font-weight: 700;
}
.pc-stats {
  display: flex;
  gap: 14px;
  margin-top: 7px;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
  font-size: 12px;
  font-weight: 800;
  color: var(--ink-dim);
}
.pc-stats .liked { color: var(--pink-deep); }
</style>
