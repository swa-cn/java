<template>
  <div class="post-media" :class="{ 'is-video': media.type === 'video' }">
    <img
      v-if="media.type === 'image' && url"
      :src="url"
      class="pm-el"
      :alt="media.name || '图片'"
      loading="lazy"
      @click="$emit('zoom')"
    />
    <video
      v-else-if="media.type === 'video' && url"
      :src="url"
      class="pm-el"
      controls
      playsinline
      preload="metadata"
    ></video>

    <!-- 加载中 / 资源丢失兜底 -->
    <div v-else class="pm-fallback">
      <span>{{ failed ? '媒体已失效' : '加载中…' }}</span>
    </div>

    <span v-if="media.type === 'video'" class="pm-badge">视频</span>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { resolveMediaUrl } from '../composables/mediaStore'

const props = defineProps({
  // { type: 'image'|'video', src?, id?, name? }
  media: { type: Object, required: true }
})
defineEmits(['zoom'])

const url = ref('')
const failed = ref(false)

async function refresh() {
  failed.value = false
  const m = props.media || {}
  if (m.src) {
    url.value = m.src
    return
  }
  if (!m.id) {
    url.value = ''
    failed.value = true
    return
  }
  try {
    const u = await resolveMediaUrl(m.id)
    url.value = u
    failed.value = !u
  } catch (e) {
    url.value = ''
    failed.value = true
  }
}

watch(() => props.media, refresh, { immediate: true })
</script>

<style scoped>
.post-media {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ffe3c2 0%, #ffd0e4 100%);
  overflow: hidden;
}
.pm-el {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.post-media.is-video .pm-el { object-fit: contain; background: #2c2433; }
img.pm-el { cursor: zoom-in; }
.pm-fallback {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 12px;
  font-weight: 700;
  color: var(--pink-deep);
}
.pm-badge {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.6);
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  pointer-events: none;
}
</style>
