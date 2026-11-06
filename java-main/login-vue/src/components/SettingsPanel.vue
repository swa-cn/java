<template>
  <div class="sp-mask" @click.self="$emit('close')">
    <div class="sp-card" role="dialog" aria-label="设置">
      <header class="sp-head">
        <h2 class="sp-title">设置</h2>
        <button type="button" class="sp-close" aria-label="关闭" @click="$emit('close')">×</button>
      </header>

      <div class="sp-body">
        <!-- ============ 我的资料 ============ -->
        <section class="sp-sec">
          <h3 class="sp-sec-title">我的资料</h3>

          <p v-if="state.profileError" class="sp-err">{{ state.profileError }}</p>

          <label class="sp-field">
            <span class="sp-label">昵称</span>
            <input
              class="sp-input"
              type="text"
              maxlength="20"
              v-model="form.nickname"
              placeholder="给自己起个可爱名字"
            />
          </label>

          <label class="sp-field">
            <span class="sp-label">邮箱</span>
            <input
              class="sp-input"
              type="email"
              maxlength="120"
              v-model.trim="form.email"
              placeholder="例如 aurora@example.com（可留空）"
            />
          </label>

          <label class="sp-field">
            <span class="sp-label">生日</span>
            <input class="sp-input" type="date" v-model="form.birthday" />
          </label>

          <div class="sp-field">
            <span class="sp-label">个性标签</span>
            <div class="sp-chips">
              <span v-for="t in form.tags" :key="t" class="sp-chip">
                {{ t }}
                <button type="button" class="sp-chip-x" aria-label="删除标签" @click="removeTag(t)">×</button>
              </span>
              <span v-if="!form.tags.length" class="sp-chip-empty">还没有标签，加几个吧~</span>
            </div>
            <div class="sp-tag-add">
              <input
                class="sp-input"
                type="text"
                v-model="tagDraft"
                maxlength="8"
                placeholder="如：猫奴、吃货"
                @keydown.enter.prevent="addTag"
              />
              <button type="button" class="sp-add" :disabled="!canAdd" @click="addTag">添加</button>
            </div>
          </div>
        </section>

        <!-- ============ 界面字体 ============ -->
        <section class="sp-sec">
          <h3 class="sp-sec-title">界面字体</h3>
          <!-- 字体选项：横排并列，选项文字本身用对应字体呈现，只留字 -->
          <ul class="sp-list">
            <li
              v-for="opt in FONT_OPTIONS"
              :key="opt.value"
              class="sp-item"
              :class="{ active: prefs.font === opt.value }"
              @click="setFont(opt.value)"
            >
              <span class="sp-name" :style="{ fontFamily: opt.stack }">{{ opt.label }}</span>
            </li>
          </ul>
        </section>

        <!-- ============ 账号 ============ -->
        <section class="sp-sec">
          <h3 class="sp-sec-title">账号</h3>
          <button type="button" class="sp-logout" @click="$emit('logout')">退出登录</button>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { prefs, FONT_OPTIONS, setFont } from '../composables/usePersonalization'
import { useAuth, parseUserTags } from '../composables/useAuth'

defineEmits(['close', 'logout'])

const { state, updateProfile } = useAuth()
const me = state.currentUser || {}
const form = reactive({
  nickname: me.nickname || '',
  email: me.email || '',
  birthday: me.birthday || '',
  // 兼容数组 / JSON 字符串 / 逗号串，避免旧数据被当成空标签逐个拆成字符
  tags: parseUserTags(me.tags)
})
const tagDraft = ref('')

const MAX_TAGS = 6
const canAdd = computed(() => tagDraft.value.trim().length > 0 && form.tags.length < MAX_TAGS)

function addTag() {
  const v = tagDraft.value.trim()
  if (!v) return
  if (form.tags.length >= MAX_TAGS) return
  if (!form.tags.includes(v)) form.tags.push(v)
  tagDraft.value = ''
}
function removeTag(t) {
  const i = form.tags.indexOf(t)
  if (i > -1) form.tags.splice(i, 1)
}

// 任意资料变动 → 回写当前用户并持久化（本地立即生效；后端在 useAuth 内防抖落库）
watch(
  form,
  () => {
    updateProfile({
      nickname: form.nickname,
      email: form.email,
      birthday: form.birthday,
      tags: [...form.tags]
    })
  },
  { deep: true }
)
</script>

<style scoped>
.sp-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(74, 59, 82, 0.38);
  backdrop-filter: blur(3px);
  animation: sp-fade 0.18s ease both;
}
@keyframes sp-fade { from { opacity: 0; } to { opacity: 1; } }

.sp-card {
  width: 100%;
  max-width: 420px;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  background: var(--card);
  border: 3px solid var(--edge);
  border-radius: 20px;
  box-shadow: var(--shadow);
  overflow: hidden;
  animation: sp-pop 0.28s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
@keyframes sp-pop { from { transform: scale(0.94) translateY(6px); opacity: 0; } to { transform: none; opacity: 1; } }

.sp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 12px;
  border-bottom: 1px dashed var(--line);
}
.sp-title {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: var(--ink);
}
.sp-close {
  width: 26px;
  height: 26px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--ink-dim);
  font-size: 19px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.sp-close:hover { background: var(--soft); color: var(--bad); }

/* 内容区可滚动，表单变长也不溢出 */
.sp-body {
  padding: 14px 16px 16px;
  overflow-y: auto;
}

.sp-sec { margin-bottom: 18px; }
.sp-sec:last-child { margin-bottom: 0; }
.sp-sec-title {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 800;
  color: var(--pink-deep);
  letter-spacing: 0.5px;
}

.sp-err {
  margin: 0 0 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(232, 93, 117, 0.1);
  color: #c0392b;
  font-size: 12px;
  font-weight: 700;
}

.sp-field { display: block; margin-bottom: 14px; }
.sp-field:last-child { margin-bottom: 0; }
.sp-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-dim);
}
.sp-input {
  width: 100%;
  box-sizing: border-box;
  padding: 9px 12px;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink);
  background: var(--soft);
  border: 2px solid var(--line);
  border-radius: 12px;
  outline: none;
  transition: border-color 0.15s ease, background 0.15s ease;
}
.sp-input::placeholder { color: var(--ink-dim); opacity: 0.7; }
.sp-input:focus { border-color: var(--pink); background: var(--card); }
/* 让 type=date 的日历图标在浅色背景下可见 */
.sp-input::-webkit-calendar-picker-indicator { cursor: pointer; opacity: 0.65; }

/* 个性标签 chips */
.sp-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.sp-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--soft-2);
  color: var(--pink-deep);
  font-size: 13px;
  font-weight: 800;
  line-height: 1.5;
}
.sp-chip-x {
  border: 0;
  background: transparent;
  color: var(--pink-deep);
  font-size: 14px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
  opacity: 0.6;
}
.sp-chip-x:hover { opacity: 1; }
.sp-chip-empty { color: var(--ink-dim); font-size: 12px; font-weight: 600; }

.sp-tag-add { display: flex; gap: 8px; }
.sp-tag-add .sp-input { flex: 1; }
.sp-add {
  flex: 0 0 auto;
  padding: 0 16px;
  border: 2px solid var(--pink);
  border-radius: 12px;
  background: var(--pink-deep);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: opacity 0.15s ease;
}
.sp-add:disabled { opacity: 0.4; cursor: not-allowed; }

/* ---------- 横排并列：四个字体选项一行 ---------- */
.sp-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  gap: 8px;
}
.sp-item {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 6px;
  border: 2px solid var(--line);
  border-radius: 12px;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease, transform 0.12s ease;
}
.sp-item:hover { transform: translateY(-2px); border-color: var(--pink); }
.sp-item.active {
  border-color: var(--pink-deep);
  background: var(--soft);
  box-shadow: 0 0 0 2px rgba(255, 126, 179, 0.18);
}
.sp-name {
  font-size: 15px;
  color: var(--ink);
  line-height: 1.25;
  text-align: center;
}

/* ---------- 退出登录 ---------- */
.sp-logout {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 16px;
  border: 2px solid var(--line);
  border-radius: 14px;
  background: var(--card);
  color: var(--bad);
  font-family: inherit;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.12s ease;
}
.sp-logout:hover { background: rgba(232, 93, 117, 0.08); border-color: var(--bad); }
.sp-logout:active { transform: scale(0.98); }

/* 很窄的屏：放不下一行就换行（2×2） */
@media (max-width: 360px) {
  .sp-list { flex-wrap: wrap; }
  .sp-item { flex: 1 1 40%; }
}
</style>
