<template>
  <div
    class="mascot pet-mascot"
    :class="{ boing }"
    role="img"
    :aria-label="`今天是${d.label}，${EXPR_NAMES[expr]}表情`"
    title="点我换个表情~"
    @click="nextExpr(true)"
  >
    <svg viewBox="0 0 120 112">
      <g :key="pet + '-' + expr" class="pet-face">
        <!-- ============ 各宠物基础脸型 ============ -->
        <!-- 猫猫 -->
        <template v-if="pet === 0">
          <path d="M24 40 Q16 10 44 24 Z" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" stroke-linejoin="round" />
          <path d="M96 40 Q104 10 76 24 Z" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" stroke-linejoin="round" />
          <path d="M28 34 Q24 20 38 26 Z" fill="#ffb3c8" />
          <path d="M92 34 Q96 20 82 26 Z" fill="#ffb3c8" />
          <ellipse cx="60" cy="64" rx="42" ry="38" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="28" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
          <ellipse cx="92" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
          <path d="M14 62 h12 M14 70 h11" stroke="#4a3540" stroke-width="2.4" stroke-linecap="round" />
          <path d="M106 62 h-12 M106 70 h-11" stroke="#4a3540" stroke-width="2.4" stroke-linecap="round" />
        </template>

        <!-- 小狗 -->
        <template v-else-if="pet === 1">
          <ellipse cx="22" cy="58" rx="14" ry="24" fill="#c98d5f" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="98" cy="58" rx="14" ry="24" fill="#c98d5f" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="60" cy="66" rx="40" ry="36" fill="#f0d6ae" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="76" cy="56" rx="10" ry="9" fill="#c98d5f" opacity="0.55" />
          <ellipse cx="28" cy="76" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
          <ellipse cx="92" cy="76" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
          <ellipse cx="60" cy="72" rx="6.5" ry="5" fill="#4a3540" />
        </template>

        <!-- 兔兔 -->
        <template v-else-if="pet === 2">
          <ellipse cx="42" cy="26" rx="10" ry="25" fill="#fff" stroke="#4a3540" stroke-width="4" transform="rotate(-9 42 26)" />
          <ellipse cx="78" cy="26" rx="10" ry="25" fill="#fff" stroke="#4a3540" stroke-width="4" transform="rotate(9 78 26)" />
          <ellipse cx="42" cy="29" rx="4.5" ry="16" fill="#ffb3c8" transform="rotate(-9 42 29)" />
          <ellipse cx="78" cy="29" rx="4.5" ry="16" fill="#ffb3c8" transform="rotate(9 78 29)" />
          <ellipse cx="60" cy="70" rx="38" ry="34" fill="#fff" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="28" cy="78" rx="8" ry="5" fill="#ffb3c8" opacity="0.9" />
          <ellipse cx="92" cy="78" rx="8" ry="5" fill="#ffb3c8" opacity="0.9" />
          <ellipse cx="60" cy="74" rx="4.5" ry="3.4" fill="#ff8fae" />
        </template>

        <!-- 小熊 -->
        <template v-else>
          <circle cx="30" cy="34" r="13" fill="#c99b6a" stroke="#4a3540" stroke-width="4" />
          <circle cx="90" cy="34" r="13" fill="#c99b6a" stroke="#4a3540" stroke-width="4" />
          <circle cx="30" cy="34" r="5.5" fill="#ffb3c8" />
          <circle cx="90" cy="34" r="5.5" fill="#ffb3c8" />
          <ellipse cx="60" cy="68" rx="40" ry="36" fill="#c99b6a" stroke="#4a3540" stroke-width="4" />
          <ellipse cx="28" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.75" />
          <ellipse cx="92" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.75" />
          <ellipse cx="60" cy="79" rx="17" ry="12.5" fill="#f0d9b8" stroke="#4a3540" stroke-width="3" />
        </template>

        <!-- ============ 眼睛（3 种表情通用绘制） ============ -->
        <g class="eyes">
          <!-- 眯眯笑 ^ ^ -->
          <template v-if="expr === 1">
            <path :d="happyEye(d.lx)" fill="none" stroke="#4a3540" stroke-width="3.4" stroke-linecap="round" />
            <path :d="happyEye(d.rx)" fill="none" stroke="#4a3540" stroke-width="3.4" stroke-linecap="round" />
          </template>
          <!-- 惊讶 ⊙ ⊙ -->
          <template v-else-if="expr === 2">
            <circle :cx="d.lx" :cy="d.ey" r="8" fill="#fff" stroke="#4a3540" stroke-width="3" />
            <circle :cx="d.lx" :cy="d.ey" r="3.2" fill="#4a3540" />
            <circle :cx="d.rx" :cy="d.ey" r="8" fill="#fff" stroke="#4a3540" stroke-width="3" />
            <circle :cx="d.rx" :cy="d.ey" r="3.2" fill="#4a3540" />
          </template>
          <!-- 普通圆圆眼 -->
          <template v-else>
            <ellipse :cx="d.lx" :cy="d.ey" rx="6.5" ry="9" fill="#4a3540" />
            <circle :cx="d.lx + 2.5" :cy="d.ey - 3.5" r="2.4" fill="#fff" />
            <ellipse :cx="d.rx" :cy="d.ey" rx="6.5" ry="9" fill="#4a3540" />
            <circle :cx="d.rx + 2.5" :cy="d.ey - 3.5" r="2.4" fill="#fff" />
          </template>
        </g>

        <!-- ============ 嘴巴 ============ -->
        <!-- 微笑（默认）：各宠物专属嘴型 -->
        <template v-if="expr === 0">
          <template v-if="pet === 0">
            <path d="M53 76 q3.5 5 7 0 q3.5 5 7 0" fill="none" stroke="#4a3540" stroke-width="3" stroke-linecap="round" />
          </template>
          <template v-else-if="pet === 1">
            <path d="M54 80 q6 5 12 0" fill="none" stroke="#4a3540" stroke-width="3" stroke-linecap="round" />
            <path d="M56 82 q4 9 8 1 q-2 7 -8 6 q-4 -1 0 -7" fill="#ff8fae" stroke="#4a3540" stroke-width="2.4" stroke-linejoin="round" />
          </template>
          <template v-else-if="pet === 2">
            <rect x="55.5" y="78.5" width="4.4" height="7.5" rx="2.2" fill="#fff" stroke="#4a3540" stroke-width="2" />
            <rect x="60.1" y="78.5" width="4.4" height="7.5" rx="2.2" fill="#fff" stroke="#4a3540" stroke-width="2" />
          </template>
          <template v-else>
            <path d="M60 79 v3 M54.5 84 q5.5 4.5 11 0" fill="none" stroke="#4a3540" stroke-width="2.8" stroke-linecap="round" />
          </template>
        </template>
        <!-- 眯眯笑：开心大笑嘴 -->
        <template v-else-if="expr === 1">
          <path
            :d="`M ${d.mx - 8} ${d.my - 2} Q ${d.mx} ${d.my + 7} ${d.mx + 8} ${d.my - 2} Z`"
            fill="#ff8fae" stroke="#4a3540" stroke-width="2.6" stroke-linejoin="round"
          />
        </template>
        <!-- 惊讶：小圆嘴 -->
        <template v-else>
          <ellipse :cx="d.mx" :cy="d.my" rx="4.2" ry="5.6" fill="#ff8fae" stroke="#4a3540" stroke-width="2.6" />
        </template>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

/**
 * 多宠物吉祥物：宠物种类由父组件按「用户 + 日期」指定（一天一换），
 * 每种宠物自带 3 种表情（微笑 / 眯眯笑 / 惊讶）。
 * 表情不自动变化，必须点击才切换。
 */
const props = defineProps({
  pet: { type: Number, default: 0 } // 0猫 1狗 2兔 3熊
})
const emit = defineEmits(['change'])

const EXPR_NAMES = ['微笑', '眯眯笑', '惊讶']
// 各宠物眼睛/嘴巴坐标（耳朵、脸型在模板里按 pet 写死）
const PET_DEFS = [
  { label: '猫猫', lx: 44, rx: 76, ey: 60, mx: 60, my: 80 },
  { label: '小狗', lx: 46, rx: 76, ey: 60, mx: 60, my: 84 },
  { label: '兔兔', lx: 46, rx: 76, ey: 64, mx: 60, my: 86 },
  { label: '小熊', lx: 45, rx: 75, ey: 62, mx: 60, my: 84 }
]
const d = computed(() => PET_DEFS[props.pet] || PET_DEFS[0])

function happyEye(x) {
  const y = d.value.ey
  return `M ${x - 7} ${y + 3} Q ${x} ${y - 7} ${x + 7} ${y + 3}`
}

const expr = ref(0)
const boing = ref(false)

function nextExpr() {
  expr.value = (expr.value + 1) % EXPR_NAMES.length
  emit('change', props.pet, expr.value)
  boing.value = false
  requestAnimationFrame(() => { boing.value = true })
}

// 宠物被切换（跨天/换用户）时表情归零
watch(() => props.pet, () => {
  expr.value = 0
  emit('change', props.pet, expr.value)
})
</script>

<style scoped>
/* 点击时开心地弹一下 */
.pet-mascot { cursor: pointer; }
.pet-mascot.boing { animation: boing 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes boing {
  0%   { transform: scale(1, 1); }
  30%  { transform: scale(1.12, 0.86); }
  55%  { transform: scale(0.92, 1.08); }
  75%  { transform: scale(1.04, 0.97); }
  100% { transform: scale(1, 1); }
}

/* 表情切换时小弹出动画（transform-box 让 SVG 组以自身为中心缩放） */
.pet-face {
  transform-box: fill-box;
  transform-origin: center;
  animation: petpop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
@keyframes petpop {
  from { opacity: 0; transform: scale(0.55); }
  to   { opacity: 1; transform: scale(1); }
}
</style>
