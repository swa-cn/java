<template>
  <Teleport to="body">
    <div class="p3d-mask" @click.self="$emit('close')">
      <div class="p3d-card" role="dialog" aria-label="宠物3D互动">
        <header class="p3d-head">
          <div class="p3d-heading">
            <span class="p3d-badge">3D</span>
            <div>
              <h2 class="p3d-title">{{ pet?.name || '宠物' }}</h2>
              <p class="p3d-subtitle">{{ moodText }} · 正在陪你玩</p>
            </div>
          </div>
          <button type="button" class="p3d-close" aria-label="关闭" @click="$emit('close')">×</button>
        </header>

        <div class="p3d-stats">
          <div class="p3d-stat">
            <span class="p3d-stat-label">💗 亲密度</span>
            <span class="p3d-stat-value">{{ intimacy }}</span>
            <i class="p3d-bar"><b class="bar-love" :style="{ width: intimacy + '%' }"></b></i>
          </div>
          <div class="p3d-stat">
            <span class="p3d-stat-label">🍖 饱腹度</span>
            <span class="p3d-stat-value">{{ satiety }}</span>
            <i class="p3d-bar"><b class="bar-food" :style="{ width: satiety + '%' }"></b></i>
          </div>
          <div class="p3d-stat">
            <span class="p3d-stat-label">✨ 心情值</span>
            <span class="p3d-stat-value">{{ mood }}</span>
            <i class="p3d-bar"><b class="bar-mood" :style="{ width: mood + '%' }"></b></i>
          </div>
        </div>

        <div class="p3d-stage-wrap">
          <div ref="mountEl" class="p3d-stage"></div>
          <span class="p3d-hint">拖动旋转 · 滚轮缩放 · 点击宠物摸摸</span>
          <button type="button" class="p3d-reset" @click="resetView">↺ 复位视角</button>
          <i class="p3d-deco deco-a">✦</i>
          <i class="p3d-deco deco-b">✧</i>
          <i class="p3d-deco deco-c">✦</i>
        </div>

        <div class="p3d-actions">
          <button type="button" class="p3d-btn touch" :disabled="!canTouch" @click="doTouch">
            <span>👋</span>{{ canTouch ? '摸摸' : '今日已满' }}
          </button>
          <button type="button" class="p3d-btn feed" :disabled="!canFeed || feeding" @click="doFeed">
            <span>🍖</span>{{ feeding ? '干饭中…' : (canFeed ? '喂食' : '没食物') }}
          </button>
          <button type="button" class="p3d-btn jump" @click="doJump"><span>🦘</span>跳一下</button>
        </div>

        <transition name="p3d-toast">
          <p v-if="toast" class="p3d-toast">{{ toast }}</p>
        </transition>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
// =====================================================================
// Pet3DPlayground —— 宠物 3D 互动弹窗（Three.js 纯代码建模，无需模型文件）
//
// 升级版：分层宠物模型 + 五官/耳朵/爪子/项圈 + 柔和舞台 + 三点布光 + 阴影
// 待机：呼吸、点头、眨眼、耳朵抖、尾巴摆、星光漂浮
// 互动：点击冒爱心、喂食落食团+咀嚼碎屑、跳跃挤压回弹、拖拽旋转/滚轮缩放
// 父组件接口保持不变：close / interact / feed
// =====================================================================

import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

const props = defineProps({
  pet: { type: Object, required: true },
  canTouch: { type: Boolean, default: true },
  canFeed: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'interact', 'feed'])

const mountEl = ref(null)
const feeding = ref(false)
const toast = ref('')
let toastTimer = null

function showToast(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1600)
}

function clamp(n, min, max) {
  const v = Number(n)
  return Number.isFinite(v) ? Math.min(max, Math.max(min, v)) : min
}

const intimacy = computed(() => clamp(props.pet?.intimacy, 0, 100))
const satiety = computed(() => clamp(props.pet?.satiety, 0, 100))
const mood = computed(() => clamp(props.pet?.mood, 0, 100))
const moodText = computed(() => {
  const v = mood.value
  if (v >= 85) return '开心到转圈圈'
  if (v >= 65) return '心情不错'
  if (v >= 40) return '有点困啦'
  return '想让你抱抱'
})

/* ---------------- 物种配色 ---------------- */
const SPECIES = {
  cat:    { main: 0xffb7d3, accent: 0xff6fa8, belly: 0xfff3f8, inner: 0xff8fb5, dark: 0xd85687 },
  dog:    { main: 0xffc59a, accent: 0xff9a5c, belly: 0xfff6ec, inner: 0xe88251, dark: 0xd97b43 },
  rabbit: { main: 0xe6d4ff, accent: 0xb98bff, belly: 0xfaf5ff, inner: 0xffb3d1, dark: 0xa879e8 },
  hamster:{ main: 0xffd79a, accent: 0xffa94d, belly: 0xfff8e9, inner: 0xff9fb6, dark: 0xe09137 },
  bird:   { main: 0x9ce8dc, accent: 0x4fc4b0, belly: 0xf0fffb, inner: 0xffd47a, dark: 0x39a894 },
  fish:   { main: 0xffb347, accent: 0xff7b3d, belly: 0xfff0d6, inner: 0xffd166, dark: 0xd96a2b }
}
const FALLBACK = { main: 0xffb7d3, accent: 0xff8fb5, belly: 0xfff3f8, inner: 0xff9fb8, dark: 0xe06f9c }

function palette() {
  return SPECIES[String(props.pet?.typeId || '').toLowerCase()] || FALLBACK
}

/* ---------------- Three.js 场景状态 ---------------- */
let renderer = null
let scene = null
let camera = null
let controls = null
let clock = null
let rafId = 0
let petGroup = null
let parts = {}
let resizeObserver = null
let stageEl = null

let jumpT = -1
let squishT = -1
let pelletT = -1
let blinkT = -1
let nextBlink = 2
let foodPellet = null
let pointerStart = null
let heartTex = null

const hearts = []
const crumbs = []
const bubbles = []
const sparkles = []
const disposables = new Set()
let nextBubble = 0

/* ---------------- 基础建模工具 ---------------- */
function makeMat(color, extra = {}) {
  return new THREE.MeshStandardMaterial({
    color,
    roughness: 0.72,
    metalness: 0.03,
    ...extra
  })
}

function ball(r, color, sx = 1, sy = 1, sz = 1, extra = {}) {
  const m = new THREE.Mesh(new THREE.SphereGeometry(r, 32, 24), makeMat(color, extra))
  m.scale.set(sx, sy, sz)
  m.castShadow = true
  return m
}

function capsule(r, len, color, extra = {}) {
  const m = new THREE.Mesh(new THREE.CapsuleGeometry(r, len, 6, 16), makeMat(color, extra))
  m.castShadow = true
  return m
}

function cone(r, h, color, seg = 4, extra = {}) {
  const m = new THREE.Mesh(new THREE.ConeGeometry(r, h, seg), makeMat(color, extra))
  m.castShadow = true
  return m
}

function addAt(parent, mesh, x, y, z) {
  mesh.position.set(x, y, z)
  parent.add(mesh)
  return mesh
}

/** 光滑高光眼睛（瞳孔），单独用 Physical 材质 */
function shinyEye() {
  return new THREE.Mesh(
    new THREE.SphereGeometry(0.068, 20, 16),
    new THREE.MeshPhysicalMaterial({
      color: 0x241b2b,
      roughness: 0.12,
      metalness: 0.05,
      clearcoat: 1,
      clearcoatRoughness: 0.08
    })
  )
}

/** 心形贴图（粒子用） */
function heartTexture() {
  if (heartTex) return heartTex
  const c = document.createElement('canvas')
  c.width = c.height = 64
  const ctx = c.getContext('2d')
  ctx.fillStyle = '#fff'
  ctx.beginPath()
  ctx.moveTo(32, 56)
  ctx.bezierCurveTo(6, 36, 8, 14, 24, 14)
  ctx.bezierCurveTo(30, 14, 32, 19, 32, 22)
  ctx.bezierCurveTo(32, 19, 34, 14, 40, 14)
  ctx.bezierCurveTo(56, 14, 58, 36, 32, 56)
  ctx.fill()
  heartTex = new THREE.CanvasTexture(c)
  return heartTex
}

/** 圆形柔光阴影贴图（代替真实地面阴影，更柔和） */
function shadowTexture() {
  const c = document.createElement('canvas')
  c.width = c.height = 128
  const ctx = c.getContext('2d')
  const g = ctx.createRadialGradient(64, 64, 4, 64, 64, 62)
  g.addColorStop(0, 'rgba(112,62,92,0.34)')
  g.addColorStop(0.5, 'rgba(112,62,92,0.12)')
  g.addColorStop(1, 'rgba(112,62,92,0)')
  ctx.fillStyle = g
  ctx.fillRect(0, 0, 128, 128)
  return new THREE.CanvasTexture(c)
}

/* ---------------- 侧身小金鱼 ---------------- */
function buildFish(p) {
  const group = new THREE.Group()
  const bodyGroup = new THREE.Group()
  group.add(bodyGroup)

  const finMat = new THREE.MeshStandardMaterial({
    color: p.accent,
    roughness: 0.62,
    metalness: 0.06,
    side: THREE.DoubleSide,
    transparent: true,
    opacity: 0.94
  })

  // 横向纺锤身体：头在 +X，尾在 -X，侧身朝向镜头
  const body = addAt(
    bodyGroup,
    ball(0.62, p.main, 1.52, 0.86, 0.68, { roughness: 0.64, metalness: 0.08 }),
    0, 0.78, 0
  )
  addAt(
    bodyGroup,
    ball(0.5, p.belly, 1.42, 0.6, 0.56, { roughness: 0.9, transparent: true, opacity: 0.82 }),
    0.06, 0.6, 0.16
  )

  // 鱼鳞：身体侧面的小亮片
  for (let row = 0; row < 3; row++) {
    for (let col = 0; col < 5; col++) {
      const x = -0.82 + col * 0.34 + (row % 2) * 0.17
      const y = 0.98 - row * 0.23
      const z = 0.55 - Math.abs(x) * 0.16 - row * 0.02
      const s = addAt(
        bodyGroup,
        ball(0.078, p.belly, 1.15, 1.15, 0.2, { roughness: 0.45, metalness: 0.16, transparent: true, opacity: 0.58 }),
        x, y, z
      )
      s.rotation.z = 0.35
    }
  }

  // 尾鳍：上下分叉的扇形
  const tailShape = new THREE.Shape()
  tailShape.moveTo(0, 0)
  tailShape.quadraticCurveTo(-0.32, 0.16, -0.66, 0.52)
  tailShape.quadraticCurveTo(-0.44, 0, -0.66, -0.52)
  tailShape.quadraticCurveTo(-0.32, -0.16, 0, 0)
  const tail = new THREE.Mesh(new THREE.ShapeGeometry(tailShape), finMat)
  tail.position.set(-1.28, 0.78, 0)
  bodyGroup.add(tail)

  // 背鳍
  const dorsalShape = new THREE.Shape()
  dorsalShape.moveTo(-0.5, 0)
  dorsalShape.quadraticCurveTo(-0.12, 0.52, 0.34, 0)
  dorsalShape.closePath()
  const dorsal = new THREE.Mesh(new THREE.ShapeGeometry(dorsalShape), finMat)
  dorsal.position.set(-0.06, 1.32, 0)
  bodyGroup.add(dorsal)

  // 腹鳍 / 臀鳍
  const analShape = new THREE.Shape()
  analShape.moveTo(-0.25, 0)
  analShape.quadraticCurveTo(-0.42, -0.3, 0.05, -0.18)
  analShape.closePath()
  const anal = new THREE.Mesh(new THREE.ShapeGeometry(analShape), finMat)
  anal.position.set(-0.18, 0.42, 0)
  bodyGroup.add(anal)

  // 胸鳍：轻微扇动
  const pecShape = new THREE.Shape()
  pecShape.moveTo(0, 0)
  pecShape.quadraticCurveTo(0.22, 0.2, 0.46, -0.02)
  pecShape.quadraticCurveTo(0.22, -0.14, 0, 0)
  const wingL = new THREE.Mesh(new THREE.ShapeGeometry(pecShape), finMat)
  wingL.position.set(0.32, 0.68, 0.58)
  wingL.rotation.y = 0.35
  const wingR = new THREE.Mesh(new THREE.ShapeGeometry(pecShape), finMat)
  wingR.position.set(0.32, 0.68, -0.58)
  wingR.rotation.y = -0.35
  bodyGroup.add(wingL, wingR)

  // 小圆嘴
  const mouth = new THREE.Mesh(
    new THREE.TorusGeometry(0.095, 0.026, 10, 24),
    makeMat(0xe8849d, { roughness: 0.7 })
  )
  mouth.rotation.y = Math.PI / 2
  mouth.position.set(1.0, 0.74, 0.12)
  bodyGroup.add(mouth)

  // 眼睛：侧身视角下两侧各一只，眨眼仍按 eyeL/eyeR 缩放
  const eyeL = new THREE.Group()
  eyeL.position.set(0.72, 0.96, 0.52)
  const eyeR = new THREE.Group()
  eyeR.position.set(0.72, 0.96, -0.52)
  bodyGroup.add(eyeL, eyeR)
  for (const eye of [eyeL, eyeR]) {
    eye.add(ball(0.12, 0xffffff, 1, 1.06, 0.5, { roughness: 0.35 }))
    const pupil = shinyEye()
    pupil.position.set(0.028, 0, 0.075)
    eye.add(pupil)
    const glint = ball(0.026, 0xffffff, 1, 1, 1, { emissive: 0xffffff, emissiveIntensity: 0.6 })
    glint.position.set(0.055, 0.04, 0.12)
    eye.add(glint)
  }

  // 鳃盖弧线
  const gill = new THREE.Mesh(
    new THREE.TorusGeometry(0.26, 0.012, 8, 24, Math.PI * 0.9),
    makeMat(p.dark, { transparent: true, opacity: 0.5, roughness: 0.9 })
  )
  gill.rotation.z = Math.PI * 0.55
  gill.position.set(0.52, 0.78, 0.61)
  bodyGroup.add(gill)

  const fishParts = {
    bodyGroup,
    headGroup: bodyGroup,
    body,
    eyeL,
    eyeR,
    tail,
    wingL,
    wingR,
    footL: null,
    footR: null,
    isFish: true
  }
  return { group, parts: fishParts }
}

/* ---------------- 宠物模型 ---------------- */
function buildPet() {
  const p = palette()
  const t = String(props.pet?.typeId || '').toLowerCase()
  if (t === 'fish') {
    const fish = buildFish(p)
    parts = fish.parts
    return fish.group
  }
  const group = new THREE.Group()

  /* 身体层：方便整体呼吸缩放 */
  const bodyGroup = new THREE.Group()
  group.add(bodyGroup)

  const body = addAt(bodyGroup, ball(0.62, p.main, 0.94, 1.04, 0.88, { roughness: 0.8 }), 0, 0.62, 0)
  addAt(bodyGroup, ball(0.42, p.belly, 0.9, 1.02, 0.38, { roughness: 0.95 }), 0, 0.53, 0.52)

  const armL = addAt(bodyGroup, ball(0.16, p.main, 0.72, 1.25, 0.72), -0.56, 0.68, 0.1)
  const armR = addAt(bodyGroup, ball(0.16, p.main, 0.72, 1.25, 0.72), 0.56, 0.68, 0.1)
  armL.rotation.z = 0.32
  armR.rotation.z = -0.32

  const footL = addAt(bodyGroup, ball(0.19, p.accent, 1, 0.6, 1.25), -0.3, 0.08, 0.26)
  const footR = addAt(bodyGroup, ball(0.19, p.accent, 1, 0.6, 1.25), 0.3, 0.08, 0.26)

  /* 项圈 + 小吊牌 */
  const collar = new THREE.Mesh(new THREE.TorusGeometry(0.4, 0.045, 12, 48), makeMat(p.accent, { roughness: 0.55, metalness: 0.12 }))
  collar.rotation.x = Math.PI / 2
  collar.position.set(0, 1.02, 0)
  bodyGroup.add(collar)
  const tag = addAt(bodyGroup, ball(0.075, 0xffd166, 1, 1, 0.45, { metalness: 0.45, roughness: 0.3 }), 0, 0.96, 0.42)

  /* 头部层：点头、歪头都作用在这里 */
  const headGroup = new THREE.Group()
  headGroup.position.set(0, 1.4, 0)
  group.add(headGroup)

  addAt(headGroup, ball(0.52, p.main, 1, 0.98, 0.95, { roughness: 0.78 }), 0, 0, 0)
  addAt(headGroup, ball(0.25, p.belly, 1.2, 0.72, 0.62, { roughness: 0.95 }), 0, -0.14, 0.44)
  addAt(headGroup, ball(0.055, p.dark, 1, 0.78, 0.7), 0, -0.06, 0.59)

  const mouth = new THREE.Mesh(
    new THREE.TorusGeometry(0.075, 0.013, 8, 20, Math.PI),
    makeMat(0x7a4f5e, { roughness: 0.9 })
  )
  mouth.rotation.z = Math.PI
  mouth.position.set(0, -0.2, 0.56)
  headGroup.add(mouth)

  /* 眼睛：眼白 + 高光瞳孔 + 眼神光，眨眼只缩放眼睛组 */
  const eyeL = new THREE.Group()
  const eyeR = new THREE.Group()
  eyeL.position.set(-0.2, 0.08, 0.43)
  eyeR.position.set(0.2, 0.08, 0.43)
  headGroup.add(eyeL, eyeR)
  for (const eye of [eyeL, eyeR]) {
    eye.add(ball(0.11, 0xffffff, 1, 1.08, 0.55, { roughness: 0.35 }))
    const pupil = shinyEye()
    pupil.position.z = 0.065
    eye.add(pupil)
    const glint = ball(0.024, 0xffffff, 1, 1, 1, { emissive: 0xffffff, emissiveIntensity: 0.5 })
    glint.position.set(-0.026, 0.038, 0.118)
    eye.add(glint)
  }

  const blushL = addAt(headGroup, ball(0.075, p.inner, 1, 0.55, 0.35, { transparent: true, opacity: 0.9 }), -0.34, -0.08, 0.41)
  const blushR = addAt(headGroup, ball(0.075, p.inner, 1, 0.55, 0.35, { transparent: true, opacity: 0.9 }), 0.34, -0.08, 0.41)

  let earL = null
  let earR = null
  let tail = null
  let wingL = null
  let wingR = null

  if (t === 'cat') {
    earL = addAt(headGroup, cone(0.15, 0.28, p.main, 4), -0.29, 0.5, -0.01)
    earR = addAt(headGroup, cone(0.15, 0.28, p.main, 4), 0.29, 0.5, -0.01)
    earL.rotation.z = 0.28
    earR.rotation.z = -0.28
    addAt(earL, cone(0.08, 0.16, p.inner, 4), 0, 0, 0.09)
    addAt(earR, cone(0.08, 0.16, p.inner, 4), 0, 0, 0.09)
    tail = addAt(bodyGroup, capsule(0.075, 0.42, p.accent), 0, 0.5, -0.55)
    tail.rotation.x = -0.5
    // 胡须
    for (const s of [-1, 1]) {
      for (let i = 0; i < 2; i++) {
        const w = new THREE.Mesh(new THREE.CylinderGeometry(0.006, 0.006, 0.3, 6), makeMat(0x8c6474))
        w.rotation.z = Math.PI / 2
        w.rotation.y = s * (i ? -0.22 : 0.22)
        w.position.set(s * 0.38, -0.1 - i * 0.07, 0.48)
        headGroup.add(w)
      }
    }
  } else if (t === 'dog') {
    earL = addAt(headGroup, ball(0.17, p.dark, 0.62, 1.5, 0.8), -0.46, 0.12, -0.02)
    earR = addAt(headGroup, ball(0.17, p.dark, 0.62, 1.5, 0.8), 0.46, 0.12, -0.02)
    earL.rotation.z = 0.22
    earR.rotation.z = -0.22
    tail = addAt(bodyGroup, capsule(0.08, 0.34, p.accent), 0, 0.85, -0.5)
    tail.rotation.x = -0.9
  } else if (t === 'rabbit') {
    earL = addAt(headGroup, capsule(0.095, 0.44, p.main), -0.18, 0.72, -0.02)
    earR = addAt(headGroup, capsule(0.095, 0.44, p.main), 0.18, 0.72, -0.02)
    earL.rotation.z = 0.12
    earR.rotation.z = -0.12
    addAt(earL, capsule(0.05, 0.36, p.inner), 0, 0, 0.07)
    addAt(earR, capsule(0.05, 0.36, p.inner), 0, 0, 0.07)
    tail = addAt(bodyGroup, ball(0.11, 0xffffff), 0, 0.38, -0.58)
  } else if (t === 'hamster') {
    earL = addAt(headGroup, ball(0.13, p.main, 1, 1, 0.6), -0.3, 0.46, -0.02)
    earR = addAt(headGroup, ball(0.13, p.main, 1, 1, 0.6), 0.3, 0.46, -0.02)
    addAt(earL, ball(0.075, p.inner, 1, 1, 0.4), 0, 0, 0.06)
    addAt(earR, ball(0.075, p.inner, 1, 1, 0.4), 0, 0, 0.06)
    tail = addAt(bodyGroup, ball(0.07, p.inner), 0, 0.35, -0.6)
    // 腮囊
    addAt(headGroup, ball(0.16, p.belly, 1.05, 0.75, 0.6), -0.3, -0.13, 0.39)
    addAt(headGroup, ball(0.16, p.belly, 1.05, 0.75, 0.6), 0.3, -0.13, 0.39)
  } else if (t === 'bird') {
    const beak = addAt(headGroup, cone(0.1, 0.22, p.inner, 4), 0, -0.1, 0.58)
    beak.rotation.x = Math.PI / 2
    wingL = addAt(bodyGroup, ball(0.2, p.accent, 0.5, 1.4, 0.85), -0.58, 0.7, -0.02)
    wingR = addAt(bodyGroup, ball(0.2, p.accent, 0.5, 1.4, 0.85), 0.58, 0.7, -0.02)
    addAt(headGroup, ball(0.06, p.accent, 1, 2.2, 1), 0, 0.54, 0.06)
  } else {
    earL = addAt(headGroup, ball(0.12, p.main), -0.3, 0.46, -0.02)
    earR = addAt(headGroup, ball(0.12, p.main), 0.3, 0.46, -0.02)
    tail = addAt(bodyGroup, ball(0.09, p.accent), 0, 0.4, -0.58)
  }

  parts = {
    bodyGroup, headGroup, body, eyeL, eyeR,
    earL, earR, tail, wingL, wingR,
    footL, footR, armL, armR, tag, blushL, blushR
  }
  return group
}

/* ---------------- 舞台 ---------------- */
function buildStage() {
  const p = palette()

  const base = new THREE.Mesh(
    new THREE.CylinderGeometry(2.45, 2.58, 0.2, 64),
    makeMat(0xfff4f7, { roughness: 0.9 })
  )
  base.position.y = -0.3
  base.receiveShadow = true
  scene.add(base)

  const top = new THREE.Mesh(new THREE.CircleGeometry(2.4, 64), makeMat(0xfffaf8, { roughness: 0.95 }))
  top.rotation.x = -Math.PI / 2
  top.position.y = -0.19
  top.receiveShadow = true
  scene.add(top)

  const rim = new THREE.Mesh(
    new THREE.TorusGeometry(2.48, 0.05, 12, 80),
    makeMat(p.accent, { roughness: 0.45, metalness: 0.18 })
  )
  rim.rotation.x = Math.PI / 2
  rim.position.y = -0.19
  scene.add(rim)

  const ring = new THREE.Mesh(
    new THREE.RingGeometry(1.35, 1.39, 64),
    new THREE.MeshBasicMaterial({ color: p.accent, transparent: true, opacity: 0.22, side: THREE.DoubleSide })
  )
  ring.rotation.x = -Math.PI / 2
  ring.position.y = -0.185
  scene.add(ring)

  const shadow = new THREE.Mesh(
    new THREE.PlaneGeometry(1.9, 1.9),
    new THREE.MeshBasicMaterial({ map: shadowTexture(), transparent: true, depthWrite: false })
  )
  shadow.rotation.x = -Math.PI / 2
  shadow.position.y = -0.175
  scene.add(shadow)

  // 漂浮星光
  for (let i = 0; i < 9; i++) {
    const s = new THREE.Mesh(
      new THREE.SphereGeometry(0.025 + Math.random() * 0.02, 8, 8),
      new THREE.MeshBasicMaterial({ color: i % 2 ? p.accent : 0xffffff, transparent: true, opacity: 0.65 })
    )
    s.position.set((Math.random() - 0.5) * 3.6, Math.random() * 2.2, (Math.random() - 0.5) * 2.4)
    s.userData = { baseY: s.position.y, speed: 0.3 + Math.random() * 0.5, phase: Math.random() * Math.PI * 2 }
    scene.add(s)
    sparkles.push(s)
  }

  // 灯光：环境 + 主光 + 补光 + 轮廓光
  scene.add(new THREE.AmbientLight(0xffffff, 0.5))
  scene.add(new THREE.HemisphereLight(0xffffff, p.main, 1.15))

  const key = new THREE.DirectionalLight(0xffffff, 2.1)
  key.position.set(3.5, 5.5, 4)
  key.castShadow = true
  key.shadow.mapSize.set(1024, 1024)
  key.shadow.camera.left = -3
  key.shadow.camera.right = 3
  key.shadow.camera.top = 3
  key.shadow.camera.bottom = -3
  scene.add(key)

  const fill = new THREE.DirectionalLight(0xffd6ec, 0.85)
  fill.position.set(-4, 2.5, 2)
  scene.add(fill)

  const rimLight = new THREE.DirectionalLight(0xcdbcff, 1.1)
  rimLight.position.set(-2, 3.5, -4)
  scene.add(rimLight)
}

/* ---------------- 初始化 ---------------- */
function initThree() {
  const el = mountEl.value
  if (!el) return
  stageEl = el
  const w = Math.max(1, el.clientWidth)
  const h = Math.max(1, el.clientHeight)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(w, h)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.18
  el.appendChild(renderer.domElement)

  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(38, w / h, 0.1, 60)
  camera.position.set(0, 1.55, 5.3)
  camera.lookAt(0, 0.85, 0)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0, 0.85, 0)
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.enablePan = false
  controls.minDistance = 2.8
  controls.maxDistance = 7.5
  controls.minPolarAngle = Math.PI * 0.18
  controls.maxPolarAngle = Math.PI * 0.52
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.55
  controls.addEventListener('start', () => { controls.autoRotate = false })

  buildStage()
  petGroup = buildPet()
  scene.add(petGroup)

  clock = new THREE.Clock()
  nextBlink = 1.5 + Math.random() * 2

  const ray = new THREE.Raycaster()
  const ndc = new THREE.Vector2()
  onPointerDown = (ev) => { pointerStart = { x: ev.clientX, y: ev.clientY } }
  onPointerUp = (ev) => {
    if (!pointerStart || !petGroup) return
    const moved = Math.hypot(ev.clientX - pointerStart.x, ev.clientY - pointerStart.y)
    pointerStart = null
    if (moved > 6) return // 拖动旋转，不算摸摸
    const rect = el.getBoundingClientRect()
    ndc.x = ((ev.clientX - rect.left) / rect.width) * 2 - 1
    ndc.y = -((ev.clientY - rect.top) / rect.height) * 2 + 1
    ray.setFromCamera(ndc, camera)
    if (ray.intersectObject(petGroup, true).length) doTouch()
  }
  el.addEventListener('pointerdown', onPointerDown)
  el.addEventListener('pointerup', onPointerUp)

  resizeObserver = new ResizeObserver(() => {
    if (!el || !renderer || !camera) return
    const cw = Math.max(1, el.clientWidth)
    const ch = Math.max(1, el.clientHeight)
    camera.aspect = cw / ch
    camera.updateProjectionMatrix()
    renderer.setSize(cw, ch)
  })
  resizeObserver.observe(el)

  animate()
}

let onPointerDown = null
let onPointerUp = null

function resetView() {
  if (!camera || !controls) return
  camera.position.set(0, 1.55, 5.3)
  controls.target.set(0, 0.85, 0)
  controls.autoRotate = true
  controls.update()
}

/* ---------------- 粒子 ---------------- */
function spawnHearts() {
  if (!scene) return
  const map = heartTexture()
  for (let i = 0; i < 7; i++) {
    const s = new THREE.Sprite(new THREE.SpriteMaterial({ map, color: 0xff6fa8, transparent: true, opacity: 0.95, depthWrite: false }))
    s.position.set((Math.random() - 0.5) * 0.7, 1.35 + Math.random() * 0.25, 0.35 + (Math.random() - 0.5) * 0.3)
    s.scale.setScalar(0.16 + Math.random() * 0.08)
    s.userData = { life: 0, vx: (Math.random() - 0.5) * 0.5, vy: 0.7 + Math.random() * 0.5 }
    scene.add(s)
    hearts.push(s)
  }
}

function spawnCrumbs() {
  if (!scene) return
  const p = palette()
  for (let i = 0; i < 8; i++) {
    const m = new THREE.Mesh(
      new THREE.SphereGeometry(0.025 + Math.random() * 0.02, 8, 8),
      new THREE.MeshBasicMaterial({ color: i % 2 ? p.accent : 0xffd166 })
    )
    m.position.set((Math.random() - 0.5) * 0.25, 1.35, 0.62)
    m.userData = { life: 0, vx: (Math.random() - 0.5) * 1.1, vy: 0.3 + Math.random() * 0.5 }
    scene.add(m)
    crumbs.push(m)
  }
}

function spawnBubble() {
  if (!scene) return
  const b = new THREE.Mesh(
    new THREE.SphereGeometry(0.045 + Math.random() * 0.03, 14, 12),
    new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      roughness: 0.05,
      metalness: 0,
      transmission: 0.6,
      transparent: true,
      opacity: 0.62,
      depthWrite: false
    })
  )
  b.position.set(1.12, 0.76, 0.16)
  b.userData = { life: 0, vx: 0.12 + Math.random() * 0.12, vy: 0.42 + Math.random() * 0.3 }
  scene.add(b)
  bubbles.push(b)
}

function updateParticles(dt) {
  for (let i = bubbles.length - 1; i >= 0; i--) {
    const b = bubbles[i]
    b.userData.life += dt
    b.position.x += b.userData.vx * dt
    b.position.y += b.userData.vy * dt
    b.material.opacity = Math.max(0, 0.62 - b.userData.life / 2.4)
    if (b.userData.life > 2.4) {
      scene.remove(b)
      b.geometry.dispose()
      b.material.dispose()
      bubbles.splice(i, 1)
    }
  }
  for (let i = hearts.length - 1; i >= 0; i--) {
    const s = hearts[i]
    s.userData.life += dt
    s.position.x += s.userData.vx * dt
    s.position.y += s.userData.vy * dt
    s.material.opacity = Math.max(0, 0.95 - s.userData.life / 1.25)
    s.scale.multiplyScalar(1 + dt * 0.35)
    if (s.userData.life > 1.25) {
      scene.remove(s)
      s.material.dispose()
      hearts.splice(i, 1)
    }
  }
  for (let i = crumbs.length - 1; i >= 0; i--) {
    const m = crumbs[i]
    m.userData.life += dt
    m.position.x += m.userData.vx * dt
    m.position.y -= m.userData.vy * dt
    if (m.userData.life > 1.1) {
      scene.remove(m)
      m.geometry.dispose()
      m.material.dispose()
      crumbs.splice(i, 1)
    }
  }
  for (const s of sparkles) {
    const d = s.userData
    s.position.y = d.baseY + Math.sin(clock.elapsedTime * d.speed + d.phase) * 0.18
    s.material.opacity = 0.35 + (Math.sin(clock.elapsedTime * 2 + d.phase) + 1) * 0.25
  }
}

/* ---------------- 每帧动画 ---------------- */
function animate() {
  rafId = requestAnimationFrame(animate)
  if (!renderer || !scene || !camera || !clock) return

  const dt = Math.min(clock.getDelta(), 0.05)
  const t = clock.elapsedTime

  if (petGroup && parts.bodyGroup) {
    // 呼吸 + 轻微左右摇摆
    parts.bodyGroup.scale.y = 1 + Math.sin(t * 2.1) * 0.022
    parts.bodyGroup.rotation.z = Math.sin(t * 0.85) * 0.018

    // 头部点头 / 歪头（咀嚼时叠加点头）；鱼是侧身泳姿，改为身体左右轻摆
    let nod = 0
    if (pelletT >= 1 && pelletT < 2.4) {
      nod = Math.abs(Math.sin((pelletT - 1) * 12)) * 0.22
    }
    if (parts.isFish) {
      parts.bodyGroup.position.x = Math.sin(t * 0.85) * 0.045
      parts.bodyGroup.rotation.y = Math.sin(t * 0.9) * 0.035
      if (t >= nextBubble) {
        spawnBubble()
        nextBubble = t + 1.8 + Math.random() * 1.6
      }
    } else {
      parts.headGroup.position.y = 1.4 + Math.sin(t * 1.7) * 0.025
      parts.headGroup.rotation.z = Math.sin(t * 0.9) * 0.05
      parts.headGroup.rotation.x = nod
    }

    // 随机眨眼
    if (t >= nextBlink && blinkT < 0) {
      blinkT = 0
      nextBlink = t + 2.4 + Math.random() * 2.8
    }
    if (blinkT >= 0) {
      blinkT += dt
      const p = blinkT / 0.17
      const s = p < 0.5 ? 1 - p * 1.8 : 0.1 + (p - 0.5) * 1.8
      parts.eyeL.scale.y = Math.max(0.08, s)
      parts.eyeR.scale.y = Math.max(0.08, s)
      if (p >= 1) {
        blinkT = -1
        parts.eyeL.scale.y = 1
        parts.eyeR.scale.y = 1
      }
    }

    // 耳朵轻抖 / 尾巴 / 翅膀 / 爪子
    if (parts.earL && parts.earR) {
      parts.earL.rotation.z += Math.sin(t * 2.7) * 0.0015
      parts.earR.rotation.z -= Math.sin(t * 2.7) * 0.0015
    }
    if (parts.tail) parts.tail.rotation.y = Math.sin(t * 3.4) * 0.55
    if (parts.wingL && parts.wingR) {
      parts.wingL.rotation.z = Math.sin(t * 4.2) * 0.28
      parts.wingR.rotation.z = -Math.sin(t * 4.2) * 0.28
    }
    if (parts.footL && parts.footR) {
      parts.footL.position.y = 0.08 + Math.max(0, Math.sin(t * 2.1)) * 0.02
      parts.footR.position.y = 0.08 + Math.max(0, Math.sin(t * 2.1 + Math.PI)) * 0.02
    }

    // 跳跃：起跳拉伸 + 落地回弹
    if (jumpT >= 0) {
      jumpT += dt
      const p = jumpT / 0.92
      if (p >= 1) {
        jumpT = -1
        petGroup.position.y = 0
        petGroup.rotation.x = 0
        petGroup.scale.set(1, 1, 1)
      } else {
        const arc = Math.sin(p * Math.PI)
        petGroup.position.y = arc * 0.9
        petGroup.rotation.x = -arc * 0.1
        const stretch = 1 + arc * 0.09
        petGroup.scale.set(1 / Math.sqrt(stretch), stretch, 1 / Math.sqrt(stretch))
      }
    }

    // 摸摸 Q 弹（与跳跃互不覆盖：只在未跳时生效）
    if (squishT >= 0 && jumpT < 0) {
      squishT += dt
      const p = squishT / 0.38
      if (p >= 1) {
        squishT = -1
        petGroup.scale.set(1, 1, 1)
      } else {
        const s = 1 + Math.sin(p * Math.PI) * 0.13
        petGroup.scale.set(s, 1 / s, s)
      }
    }

    // 食物下落 -> 咀嚼 -> 碎屑
    if (pelletT >= 0 && foodPellet) {
      pelletT += dt
      if (pelletT < 1) {
        const p = pelletT
        foodPellet.position.set(Math.sin(p * Math.PI) * 0.12, 2.25 - p * 1.42, 0.62 - p * 0.08)
        foodPellet.rotation.x += dt * 6
        foodPellet.rotation.z += dt * 4
      } else if (pelletT < 1.12) {
        if (foodPellet.visible) {
          foodPellet.visible = false
          spawnCrumbs()
        }
      } else if (pelletT >= 2.4) {
        scene.remove(foodPellet)
        foodPellet.geometry.dispose()
        foodPellet.material.dispose()
        foodPellet = null
        pelletT = -1
        feeding.value = false
      }
    }
  }

  updateParticles(dt)
  controls && controls.update()
  renderer.render(scene, camera)
}

function startPellet() {
  if (foodPellet) {
    scene.remove(foodPellet)
    foodPellet.geometry.dispose()
    foodPellet.material.dispose()
  }
  foodPellet = ball(0.105, 0xd79a5c, 1, 0.85, 1, { roughness: 0.85 })
  foodPellet.position.set(0, 2.25, 0.62)
  scene.add(foodPellet)
  pelletT = 0
}

/* ---------------- 交互动作 ---------------- */
function doTouch() {
  if (!props.canTouch) {
    showToast('今天摸得够多啦，明天再来吧 🥺')
    return
  }
  squishT = 0
  spawnHearts()
  emit('interact')
  showToast('摸摸 +1 🧡')
}

function doFeed() {
  if (!props.canFeed || feeding.value || !scene) return
  feeding.value = true
  startPellet()
  emit('feed')
  showToast('干饭最积极了 🍖')
}

function doJump() {
  if (jumpT < 0) jumpT = 0
}

/* ---------------- 资源清理 ---------------- */
function disposeObject(obj) {
  if (!obj) return
  obj.traverse((o) => {
    if (o.geometry && !disposables.has(o.geometry)) {
      o.geometry.dispose()
      disposables.add(o.geometry)
    }
    const mats = Array.isArray(o.material) ? o.material : (o.material ? [o.material] : [])
    for (const m of mats) {
      if (!disposables.has(m)) {
        m.dispose()
        disposables.add(m)
      }
    }
  })
}

function rebuildPet() {
  if (!scene || !petGroup) return
  scene.remove(petGroup)
  disposeObject(petGroup)
  petGroup = buildPet()
  scene.add(petGroup)
  jumpT = -1
  squishT = -1
}

onMounted(initThree)

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  clearTimeout(toastTimer)
  if (stageEl && onPointerDown) stageEl.removeEventListener('pointerdown', onPointerDown)
  if (stageEl && onPointerUp) stageEl.removeEventListener('pointerup', onPointerUp)
  resizeObserver && resizeObserver.disconnect()
  controls && controls.dispose()
  for (const h of hearts) { if (scene) scene.remove(h); h.material.dispose() }
  hearts.length = 0
  for (const c of crumbs) { if (scene) scene.remove(c); c.geometry.dispose(); c.material.dispose() }
  crumbs.length = 0
  for (const b of bubbles) { if (scene) scene.remove(b); b.geometry.dispose(); b.material.dispose() }
  bubbles.length = 0
  sparkles.length = 0
  if (foodPellet) { foodPellet.geometry.dispose(); foodPellet.material.dispose(); foodPellet = null }
  disposeObject(petGroup)
  if (scene) disposeObject(scene)
  renderer && renderer.dispose()
  renderer = null
  scene = null
  petGroup = null
  stageEl = null
})

watch(
  () => props.pet && props.pet.id,
  () => rebuildPet()
)
</script>

<style scoped>
.p3d-mask {
  position: fixed;
  inset: 0;
  z-index: 120;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(74, 59, 82, 0.46);
  backdrop-filter: blur(5px);
}
.p3d-card {
  position: relative;
  width: 100%;
  max-width: 600px;
  background: linear-gradient(180deg, #fffdfd 0%, #fff7fb 100%);
  border: 3px solid #ffd9e8;
  border-radius: 24px;
  box-shadow: 0 24px 70px rgba(120, 72, 108, 0.28), inset 0 1px 0 #fff;
  overflow: hidden;
}
.p3d-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px 10px;
  border-bottom: 1px dashed #f3d6e4;
}
.p3d-heading { display: flex; align-items: center; gap: 11px; }
.p3d-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff7fb0, #b98bff);
  color: #fff;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.5px;
  box-shadow: 0 6px 14px rgba(255, 127, 176, 0.35);
}
.p3d-title { margin: 0; font-size: 17px; font-weight: 900; color: #4a3548; }
.p3d-subtitle { margin: 2px 0 0; font-size: 12px; color: #b2869e; }
.p3d-close {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: #fbeef5;
  color: #a87b95;
  font-size: 19px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}
.p3d-close:hover { background: #ffdcec; transform: rotate(90deg); }

.p3d-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 12px 18px 4px;
}
.p3d-stat {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px 6px;
  padding: 9px 10px;
  border: 1px solid #f6dfea;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.75);
}
.p3d-stat-label { font-size: 11px; color: #a37f94; font-weight: 700; }
.p3d-stat-value { font-size: 12px; font-weight: 900; color: #6b4a61; }
.p3d-bar {
  grid-column: 1 / -1;
  height: 5px;
  border-radius: 99px;
  background: #f6e8ef;
  overflow: hidden;
}
.p3d-bar b {
  display: block;
  height: 100%;
  border-radius: inherit;
  transition: width 0.4s ease;
}
.bar-love { background: linear-gradient(90deg, #ff8fb8, #ff5f9e); }
.bar-food { background: linear-gradient(90deg, #ffc57a, #ff9a4d); }
.bar-mood { background: linear-gradient(90deg, #b8a0ff, #8b6ff0); }

.p3d-stage-wrap { position: relative; margin: 8px 18px 12px; }
.p3d-stage {
  position: relative;
  height: 390px;
  border-radius: 18px;
  overflow: hidden;
  border: 2px solid #f4dbe8;
  background: radial-gradient(circle at 50% 30%, #fff9fd 0%, #ffeaf4 52%, #f3dcf2 100%);
  box-shadow: inset 0 2px 16px rgba(255, 255, 255, 0.9), 0 8px 22px rgba(196, 140, 180, 0.16);
  touch-action: none;
}
.p3d-stage::before,
.p3d-stage::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
}
.p3d-stage::before {
  width: 150px;
  height: 150px;
  left: -40px;
  top: -40px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.85), rgba(255, 255, 255, 0));
}
.p3d-stage::after {
  width: 200px;
  height: 200px;
  right: -60px;
  bottom: -70px;
  background: radial-gradient(circle, rgba(255, 197, 224, 0.5), rgba(255, 197, 224, 0));
}
.p3d-stage canvas { position: relative; z-index: 1; display: block; }
.p3d-hint {
  position: absolute;
  z-index: 2;
  left: 12px;
  top: 10px;
  padding: 5px 10px;
  border-radius: 99px;
  background: rgba(255, 255, 255, 0.78);
  color: #a87b95;
  font-size: 11px;
  font-weight: 700;
  pointer-events: none;
}
.p3d-reset {
  position: absolute;
  z-index: 2;
  right: 12px;
  top: 10px;
  padding: 5px 11px;
  border: 1px solid #f0cfe0;
  border-radius: 99px;
  background: rgba(255, 255, 255, 0.86);
  color: #a06a8c;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}
.p3d-reset:hover { background: #fff; transform: translateY(-1px); }
.p3d-deco {
  position: absolute;
  z-index: 2;
  color: #ff9ec4;
  font-style: normal;
  pointer-events: none;
  animation: p3d-float 3.2s ease-in-out infinite;
}
.deco-a { left: 9%; top: 26%; font-size: 17px; }
.deco-b { right: 11%; top: 40%; font-size: 14px; animation-delay: 0.8s; }
.deco-c { right: 18%; bottom: 18%; font-size: 12px; animation-delay: 1.6s; }
@keyframes p3d-float {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.5; }
  50% { transform: translateY(-8px) rotate(25deg); opacity: 1; }
}

.p3d-actions {
  display: flex;
  gap: 10px;
  padding: 0 18px 16px;
}
.p3d-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 2px solid #ffd9e8;
  background: #fff;
  color: #7b536d;
  font-family: inherit;
  font-size: 13px;
  font-weight: 900;
  padding: 11px 0;
  border-radius: 14px;
  cursor: pointer;
  box-shadow: 0 5px 14px rgba(214, 150, 190, 0.14);
  transition: transform 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
}
.p3d-btn span { font-size: 16px; }
.p3d-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 9px 20px rgba(214, 150, 190, 0.26); }
.p3d-btn:active:not(:disabled) { transform: translateY(1px) scale(0.98); }
.p3d-btn:disabled { opacity: 0.5; cursor: default; box-shadow: none; }
.p3d-btn.touch { border-color: #ffc0da; background: linear-gradient(135deg, #fff2f8, #ffe3f0); }
.p3d-btn.feed { border-color: #ffd7b0; background: linear-gradient(135deg, #fff8ee, #ffeccf); color: #956238; }
.p3d-btn.jump { border-color: #d8ccff; background: linear-gradient(135deg, #f8f5ff, #ece6ff); color: #6f5aa8; }

.p3d-toast {
  margin: 0 18px 16px;
  padding: 9px 12px;
  text-align: center;
  font-size: 12.5px;
  font-weight: 800;
  color: #a84479;
  border-radius: 12px;
  background: #fff0f7;
  border: 1px solid #ffd9e8;
}
.p3d-toast-enter-active, .p3d-toast-leave-active { transition: opacity 0.22s ease, transform 0.22s ease; }
.p3d-toast-enter-from, .p3d-toast-leave-to { opacity: 0; transform: translateY(8px); }

@media (max-width: 640px) {
  .p3d-mask { padding: 12px; }
  .p3d-stage { height: 310px; }
  .p3d-stats { gap: 6px; padding: 10px 12px 4px; }
  .p3d-stat { padding: 7px; }
  .p3d-actions { gap: 6px; padding: 0 12px 12px; }
  .p3d-btn { font-size: 12px; }
}
</style>
