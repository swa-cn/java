<template>
  <div class="composer">
    <div class="cp-bar">
      <button type="button" class="cp-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        返回
      </button>
      <h2 class="cp-head">发布萌宠动态</h2>
    </div>

    <div class="cp-card">
      <!-- 标题 -->
      <div class="cp-field">
        <span class="cp-label">标题<em>（选填）</em></span>
        <input
          v-model="form.title"
          class="cp-input"
          type="text"
          maxlength="40"
          placeholder="给这条动态起个名字，例如：柯基的周末野餐"
        />
        <span class="cp-count">{{ form.title.length }}/40</span>
      </div>

      <!-- 正文 -->
      <div class="cp-field">
        <span class="cp-label">正文<em>（必填）</em></span>
        <textarea
          v-model="form.content"
          class="cp-textarea"
          rows="5"
          maxlength="1000"
          placeholder="分享一下今天的萌宠日常吧~"
        ></textarea>
        <span class="cp-count">{{ form.content.length }}/1000</span>
      </div>

      <!-- 分类 -->
      <div class="cp-field">
        <span class="cp-label">所属分类</span>
        <div class="cp-cats">
          <button
            v-for="c in petCategories"
            :key="c.id"
            type="button"
            class="cp-cat"
            :class="{ active: form.category === c.id }"
            :style="form.category === c.id ? { background: c.color, borderColor: c.color } : {}"
            @click="form.category = c.id"
          >{{ c.icon }} {{ c.name }}</button>
        </div>
      </div>

      <!-- 标签 -->
      <div class="cp-field">
        <span class="cp-label">标签<em>（选填，用空格或逗号分隔，最多 6 个）</em></span>
        <input
          v-model="form.tags"
          class="cp-input"
          type="text"
          maxlength="60"
          placeholder="例如：布偶猫 撸猫日常"
        />
      </div>

      <!-- 图片 / 视频上传 -->
      <div class="cp-field">
        <span class="cp-label">
          图片 / 视频
          <em>（图片最多 {{ MEDIA_COUNTS.image }} 张且单张 ≤ {{ mb(MEDIA_LIMITS.image) }}MB；视频最多 {{ MEDIA_COUNTS.video }} 个且 ≤ {{ mb(MEDIA_LIMITS.video) }}MB）</em>
        </span>

        <div
          class="cp-drop"
          :class="{ over: dragOver }"
          @dragover.prevent="dragOver = true"
          @dragleave.prevent="dragOver = false"
          @drop.prevent="onDrop"
          @click="pick"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 16V4m0 0L7.5 8.5M12 4l4.5 4.5" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M4 15v3.5A1.5 1.5 0 0 0 5.5 20h13a1.5 1.5 0 0 0 1.5-1.5V15" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
          </svg>
          <p class="cp-drop-main">点击选择，或把图片 / 视频拖到这里</p>
          <p class="cp-drop-sub">支持 JPG / PNG / GIF / MP4 等常见格式</p>
          <input
            ref="fileInput"
            class="cp-file"
            type="file"
            accept="image/*,video/*"
            multiple
            @change="onPick"
          />
        </div>

        <!-- 待发布媒体预览 -->
        <ul v-if="picked.length" class="cp-previews">
          <li
            v-for="(p, i) in picked"
            :key="p.key"
            class="cp-preview"
            :class="{ 'is-video': p.kind === 'video' }"
          >
            <img v-if="p.kind === 'image'" :src="p.url" :alt="p.name" />
            <video v-else :src="p.url" controls playsinline preload="metadata"></video>
            <span class="cp-preview-tag">{{ p.kind === 'video' ? '视频' : '图片' }}</span>
            <button type="button" class="cp-preview-del" title="移除" @click.stop="removeAt(i)">×</button>
            <span class="cp-preview-size">{{ mb(p.size) }}MB</span>
          </li>
        </ul>

        <p v-if="mediaError" class="cp-error">{{ mediaError }}</p>
      </div>

      <!-- 提交 -->
      <p v-if="formError" class="cp-error cp-error-main">{{ formError }}</p>

      <div class="cp-footer">
        <button type="button" class="cp-cancel" :disabled="publishing" @click="$emit('back')">
          取消
        </button>
        <button type="button" class="cp-submit" :disabled="publishing" @click="submit">
          {{ publishing ? '发布中…' : '发布' }}
        </button>
      </div>

      <p class="cp-note">
        <template v-if="postRuntime.localMode">
          当前为本地演示模式：图文 / 视频保存在浏览器本地（IndexedDB），刷新后依然可见，不会上传到服务器。
          <br />后端 <code>/content/posts</code> 接口就绪后会自动切换到服务端存储。
        </template>
        <template v-else>发布后将上传到服务器，请确认内容合规后再提交。</template>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { petCategories } from '../composables/petData'
import { MEDIA_LIMITS, MEDIA_COUNTS, mediaKind } from '../composables/mediaStore'
import { publishPost, postRuntime } from '../api/post'

const props = defineProps({
  author: { type: String, default: 'guest' },
  authorName: { type: String, default: '萌宠用户' }
})
const emit = defineEmits(['back', 'published', 'toast'])

const form = reactive({
  title: '',
  content: '',
  category: petCategories[0].id,
  tags: ''
})

const fileInput = ref(null)
const picked = ref([]) // { key, file, kind, url, name, size }
const dragOver = ref(false)
const mediaError = ref('')
const formError = ref('')
const publishing = ref(false)

function mb(bytes) {
  return (Number(bytes || 0) / 1024 / 1024).toFixed(1)
}

function parseTags(text) {
  return String(text || '')
    .split(/[\s,，、]+/)
    .map((t) => t.replace(/^#/, '').trim())
    .filter(Boolean)
    .slice(0, 6)
}

function pick() {
  if (fileInput.value) fileInput.value.click()
}

function onPick(e) {
  addFiles(Array.from(e.target.files || []))
  e.target.value = '' // 允许重复选择同一个文件
}

function onDrop(e) {
  dragOver.value = false
  addFiles(Array.from((e.dataTransfer && e.dataTransfer.files) || []))
}

function addFiles(files) {
  mediaError.value = ''
  for (const f of files) {
    const kind = mediaKind(f)
    if (!kind) {
      mediaError.value = `「${f.name}」不是图片或视频，已跳过`
      continue
    }
    if (f.size > MEDIA_LIMITS[kind]) {
      mediaError.value = `「${f.name}」超过 ${mb(MEDIA_LIMITS[kind])}MB 限制，已跳过`
      continue
    }
    const sameCount = picked.value.filter((p) => p.kind === kind).length
    if (sameCount >= MEDIA_COUNTS[kind]) {
      mediaError.value = `最多只能上传 ${MEDIA_COUNTS[kind]} 个${kind === 'image' ? '图片' : '视频'}`
      continue
    }
    picked.value.push({
      key: 'k' + Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
      file: f,
      kind,
      url: URL.createObjectURL(f),
      name: f.name,
      size: f.size
    })
  }
}

function removeAt(i) {
  const item = picked.value[i]
  if (item && item.url) URL.revokeObjectURL(item.url)
  picked.value.splice(i, 1)
  mediaError.value = ''
}

function reset() {
  picked.value.forEach((p) => p.url && URL.revokeObjectURL(p.url))
  picked.value = []
  form.title = ''
  form.content = ''
  form.tags = ''
  form.category = petCategories[0].id
  formError.value = ''
  mediaError.value = ''
}

async function submit() {
  formError.value = ''
  const content = form.content.trim()
  if (!content && !picked.value.length) {
    formError.value = '写点文字，或者选一张图片 / 一个视频再发布吧~'
    return
  }
  publishing.value = true
  try {
    const res = await publishPost({
      title: form.title.trim(),
      content,
      category: form.category,
      tags: parseTags(form.tags),
      files: picked.value.map((p) => p.file),
      author: props.author,
      authorName: props.authorName
    })
    reset()
    emit('published', res && res.post)
  } catch (e) {
    formError.value = (e && e.message) || '发布失败，请稍后再试'
  } finally {
    publishing.value = false
  }
}

onBeforeUnmount(() => {
  picked.value.forEach((p) => p.url && URL.revokeObjectURL(p.url))
})
</script>

<style scoped>
.composer { animation: pop 0.42s cubic-bezier(0.34, 1.56, 0.64, 1) both; }

.cp-bar { display: flex; align-items: center; gap: 12px; padding: 22px 30px 0; }
.cp-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--ink);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 7px 15px 7px 11px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.14s ease, background 0.18s ease, border-color 0.18s ease;
}
.cp-back svg { width: 16px; height: 16px; }
.cp-back:hover { background: var(--soft); border-color: var(--pink); transform: translateX(-2px); }
.cp-head { margin: 0; font-size: 18px; font-weight: 800; }

.cp-card {
  margin: 14px 30px 34px;
  padding: 20px 22px 18px;
  background: var(--card);
  border: 2px solid var(--line);
  border-radius: 24px;
  box-shadow: 0 18px 40px -20px rgba(124, 78, 140, 0.35);
}

.cp-field { display: block; position: relative; margin-bottom: 16px; }
.cp-label {
  display: block;
  margin-bottom: 7px;
  font-size: 13.5px;
  font-weight: 800;
}
.cp-label em { font-style: normal; font-size: 11.5px; font-weight: 700; color: var(--ink-dim); }

.cp-input,
.cp-textarea {
  width: 100%;
  border: 2px solid var(--line);
  border-radius: 16px;
  padding: 11px 14px;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink);
  background: var(--soft);
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}
.cp-textarea { resize: vertical; line-height: 1.6; min-height: 118px; }
.cp-input:focus,
.cp-textarea:focus {
  border-color: var(--pink);
  background: var(--card);
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.16);
}
.cp-input::placeholder,
.cp-textarea::placeholder { color: var(--ink-dim); }
.cp-count {
  position: absolute;
  right: 10px;
  bottom: -2px;
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-dim);
}

/* 分类选择 */
.cp-cats { display: flex; flex-wrap: wrap; gap: 8px; }
.cp-cat {
  border: 2px solid var(--line);
  background: var(--card);
  color: var(--pink-deep);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 7px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease;
}
.cp-cat:hover { background: var(--soft); border-color: var(--pink); transform: translateY(-1px); }
.cp-cat.active { color: var(--on-accent); box-shadow: 0 10px 18px -10px rgba(255, 92, 157, 0.7); }

/* 上传区 */
.cp-drop {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 22px 16px;
  border: 2px dashed var(--line);
  border-radius: 18px;
  background: var(--soft);
  cursor: pointer;
  text-align: center;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.14s ease;
}
.cp-drop:hover,
.cp-drop.over { border-color: var(--pink); background: var(--soft); }
.cp-drop.over { transform: scale(1.01); }
.cp-drop svg { width: 30px; height: 30px; color: var(--pink); }
.cp-drop-main { margin: 6px 0 0; font-size: 13.5px; font-weight: 800; color: var(--ink); }
.cp-drop-sub { margin: 0; font-size: 11.5px; color: var(--ink-dim); font-weight: 700; }
.cp-file { display: none; }

/* 预览 */
.cp-previews {
  list-style: none;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
  gap: 10px;
  margin: 12px 0 0;
  padding: 0;
}
.cp-preview {
  position: relative;
  height: 112px;
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid var(--line);
  background: #2c2433;
}
.cp-preview img,
.cp-preview video { width: 100%; height: 100%; object-fit: cover; display: block; }
.cp-preview.is-video video { object-fit: contain; }
.cp-preview-tag,
.cp-preview-size {
  position: absolute;
  bottom: 6px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(44, 36, 51, 0.66);
  color: var(--on-accent);
  font-size: 10.5px;
  font-weight: 800;
}
.cp-preview-tag { left: 6px; }
.cp-preview-size { right: 6px; }
.cp-preview-del {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--ink);
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
  cursor: pointer;
  transition: background 0.16s ease, transform 0.14s ease;
}
.cp-preview-del:hover { background: var(--card); transform: scale(1.08); }

.cp-error {
  margin: 10px 0 0;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--bad);
}
.cp-error-main {
  margin: 0 0 14px;
  padding: 10px 14px;
  border-radius: 14px;
  background: var(--soft);
}

/* 底部按钮 */
.cp-footer { display: flex; gap: 10px; }
.cp-cancel,
.cp-submit {
  flex: 1;
  border: 0;
  border-radius: 999px;
  padding: 13px 18px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.2s ease;
}
.cp-cancel { background: var(--soft); color: var(--ink-soft); }
.cp-cancel:hover:not(:disabled) { background: var(--soft); }
.cp-submit {
  flex: 2;
  color: var(--on-accent);
  background: linear-gradient(135deg, var(--pink) 0%, var(--pink-deep) 55%, #ff8a5c 130%);
  box-shadow: 0 12px 22px -10px rgba(255, 92, 157, 0.7), inset 0 -3px 0 rgba(0, 0, 0, 0.08);
}
.cp-submit:hover:not(:disabled) { transform: translateY(-2px); filter: brightness(1.04); }
.cp-submit:disabled,
.cp-cancel:disabled { opacity: 0.65; cursor: not-allowed; }

.cp-note {
  margin: 14px 0 0;
  font-size: 11.5px;
  line-height: 1.6;
  color: var(--ink-dim);
  font-weight: 700;
  text-align: center;
}
.cp-note code {
  font-family: Consolas, "Courier New", monospace;
  background: var(--soft);
  color: var(--pink-deep);
  padding: 1px 5px;
  border-radius: 6px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .cp-bar { padding: 14px 16px 0; }
  .cp-card { margin: 12px 16px 24px; padding: 16px 16px 14px; border-radius: 20px; }
  .cp-previews { grid-template-columns: repeat(auto-fill, minmax(92px, 1fr)); }
  .cp-preview { height: 92px; }
}
</style>
