<template>
  <Teleport to="body">
    <div class="p3d-mask" @click.self="$emit('close')">
      <div class="p3d-card" role="dialog" aria-label="宠物3D互动">
        <header class="p3d-head">
          <h2 class="p3d-title">🧊 3D 互动 · {{ pet?.name || '宠物' }}</h2>
          <button type="button" class="p3d-close" aria-label="关闭" @click="$emit('close')">×</button>
        </header>

        <p class="p3d-tip">拖动旋转 · 滚轮缩放 · 点击宠物摸摸它（+亲密度）</p>

        <div ref="mountEl" class="p3d-stage"></div>

        <div class="p3d-actions">
          <button type="button" class="p3d-btn touch" :disabled="!canTouch" @click="doTouch">
            {{ canTouch ? '👋 摸摸' : '今日摸摸已满' }}
          </button>
          <button type="button" class="p3d-btn feed" :disabled="!canFeed || feeding" @click="doFeed">
            {{ feeding ? '干饭中…' : (canFeed ? '🍖 喂食' : '没有食物了') }}
          </button>
          <button type="button" class="p3d-btn jump" @click="doJump">🦘 跳一下</button>
        </div>

        <p v-if="toast" class="p3d-toast">{{ toast }}</p>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
// =====================================================================
// Pet3DPlayground —— 宠物 3D 互动弹窗（Three.js 纯代码建模，无需模型文件）
//
// 用基础几何体（球/圆锥/胶囊）拼出 Q 版小宠物：大头小身，奶乎乎。
// 交互：
//  - OrbitControls 拖拽旋转 / 滚轮缩放
//  - 点击宠物身体 = 摸摸（触发父组件真实 interact 接口，+亲密度）
//  - 喂食：食物小球落进嘴里，宠物低头咀嚼（触发父组件真实 feed 接口）
//  - 跳一下：起跳落地的小动画
//  - 待机动画：呼吸起伏、眼睛眨、尾巴/翅膀摆动
// =====================================================================

import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

const props = defineProps({
  /** 宠物对象：{ id, name, typeId, status, intimacy, satiety, mood } */
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

/* ---------------- Three.js 场景 ---------------- */
let renderer = null
let scene = null
let camera = null
let controls = null
let rafId = 0
let petGroup = null          // 宠物模型（Group）
let parts = {}               // 需要动画的部件引用 { body, head, eyeL, eyeR, tail, wingL, wingR }
let clock = null
let jumpT = -1               // 跳跃动画进度（-1 = 未在跳）
let chewT = -1               // 咀嚼动画进度
let squishT = -1             // 摸摸挤压动画进度
let foodPellet = null        // 喂食的小食物球
let pelletT = -1             // 食物下落进度

const SPECIES_COLOR = {
  cat: 0xff6fa8, dog: 0xffa26b, rabbit: 0xc9a6ff,
  hamster: 0xffb35c, bird: 0x6fd7c2, fish: 0x63b8ec
}
const DEFAULT_COLOR = 0xff9fb8

function colorOf() {
  const t = String(props.pet?.typeId || '').toLowerCase()
  return SPECIES_COLOR[t] || DEFAULT_COLOR
}

/** 建一个小球体（快捷方法） */
function ball(r, color, sx = 1, sy = 1, sz = 1) {
  const m = new THREE.Mesh(
    new THREE.SphereGeometry(r, 24, 18),
    new THREE.MeshStandardMaterial({ color, roughness: 0.85, metalness: 0.05 })
  )
  m.scale.set(sx, sy, sz)
  return m
}

/** 用基础几何体拼 Q 版宠物：大头 + 小身 + 器官按物种微调 */
function buildPet(typeId) {
  const group = new THREE.Group()
  const color = colorOf()
  const dark = new THREE.Color(color).offsetHSL(0, 0.05, -0.18).getHex()
  const t = String(typeId || '').toLowerCase()

  // 身体：竖椭圆
  const body = ball(0.62, color, 1, 1.06, 0.94)
  body.position.y = 0.52
  group.add(body)

  // 头：大球（Q 版关键：头比身体显眼）
  const head = ball(0.5, color)
  head.position.y = 1.34
  group.add(head)

  // 眼睛（眨眼动画缩放 y）
  const eyeGeo = new THREE.SphereGeometry(0.07, 12, 10)
  const eyeMat = new THREE.MeshStandardMaterial({ color: 0x2c2433, roughness: 0.4 })
  const eyeL = new THREE.Mesh(eyeGeo, eyeMat)
  eyeL.position.set(-0.18, 1.44, 0.44)
  const eyeR = eyeL.clone()
  eyeR.position.x = 0.18
  group.add(eyeL, eyeR)

  // 腮红
  const blushMat = new THREE.MeshStandardMaterial({ color: 0xff8fab, roughness: 1 })
  const blushGeo = new THREE.SphereGeometry(0.06, 10, 8)
  const blushL = new THREE.Mesh(blushGeo, blushMat)
  blushL.position.set(-0.3, 1.3, 0.4)
  blushL.scale.set(1, 0.6, 0.5)
  const blushR = blushL.clone()
  blushR.position.x = 0.3
  group.add(blushL, blushR)

  // 物种特征
  let tail = null
  let wingL = null
  let wingR = null

  if (t === 'cat') {
    // 三角耳朵
    const earGeo = new THREE.ConeGeometry(0.12, 0.22, 4)
    const earMat = new THREE.MeshStandardMaterial({ color: dark, roughness: 0.85 })
    const earL = new THREE.Mesh(earGeo, earMat)
    earL.position.set(-0.26, 1.78, 0)
    earL.rotation.z = 0.3
    const earR = earL.clone()
    earR.position.x = 0.26
    earR.rotation.z = -0.3
    group.add(earL, earR)
    tail = ball(0.1, dark, 1, 1, 1)
    tail.position.set(0, 0.42, -0.62)
    group.add(tail)
  } else if (t === 'dog') {
    // 垂耳（扁球）
    const earL = ball(0.13, dark, 0.6, 1.3, 0.8)
    earL.position.set(-0.44, 1.4, 0.05)
    const earR = earL.clone()
    earR.position.x = 0.44
    group.add(earL, earR)
    // 尾巴：斜上小球链（简化为一节）
    tail = ball(0.11, dark)
    tail.position.set(0, 0.78, -0.56)
    group.add(tail)
  } else if (t === 'rabbit') {
    // 长耳朵（拉伸胶囊感）
    const earL = ball(0.1, color, 1, 3.2, 0.7)
    earL.position.set(-0.18, 1.92, 0)
    earL.rotation.z = 0.12
    const earR = earL.clone()
    earR.position.x = 0.18
    earR.rotation.z = -0.12
    group.add(earL, earR)
    tail = ball(0.09, 0xffffff)
    tail.position.set(0, 0.4, -0.6)
    group.add(tail)
  } else if (t === 'hamster') {
    // 圆耳
    const earL = ball(0.1, dark)
    earL.position.set(-0.24, 1.76, 0)
    const earR = earL.clone()
    earR.position.x = 0.24
    group.add(earL, earR)
    tail = ball(0.05, 0xffc9d6)
    tail.position.set(0, 0.38, -0.62)
    group.add(tail)
  } else if (t === 'bird') {
    // 鸟：喙 + 翅膀 + 头顶呆毛
    const beak = new THREE.Mesh(
      new THREE.ConeGeometry(0.08, 0.18, 4),
      new THREE.MeshStandardMaterial({ color: 0xffb741, roughness: 0.8 })
    )
    beak.rotation.x = Math.PI / 2
    beak.position.set(0, 1.34, 0.52)
    group.add(beak)
    wingL = ball(0.16, dark, 0.5, 1.4, 0.8)
    wingL.position.set(-0.58, 0.72, 0)
    wingR = wingL.clone()
    wingR.position.x = 0.58
    group.add(wingL, wingR)
    const crest = ball(0.05, dark, 1, 2.2, 1)
    crest.position.set(0, 1.86, 0.1)
    group.add(crest)
  } else if (t === 'fish') {
    // 鱼：尾鳍（扁锥）+ 背鳍
    tail = new THREE.Mesh(
      new THREE.ConeGeometry(0.2, 0.34, 4),
      new THREE.MeshStandardMaterial({ color: dark, roughness: 0.85 })
    )
    tail.rotation.x = Math.PI / 2
    tail.rotation.z = Math.PI / 2
    tail.position.set(0, 0.62, -0.72)
    group.add(tail)
    const fin = new THREE.Mesh(
      new THREE.ConeGeometry(0.14, 0.26, 4),
      new THREE.MeshStandardMaterial({ color: dark, roughness: 0.85 })
    )
    fin.position.set(0, 1.05, 0)
    group.add(fin)
    // 鱼身放平一点
    body.scale.set(1.1, 0.9, 1.15)
  } else {
    // 兜底：圆耳
    const earL = ball(0.1, dark)
    earL.position.set(-0.24, 1.76, 0)
    const earR = earL.clone()
    earR.position.x = 0.24
    group.add(earL, earR)
    tail = ball(0.1, dark)
    tail.position.set(0, 0.42, -0.62)
    group.add(tail)
  }

  parts = { body, head, eyeL, eyeR, tail, wingL, wingR }
  return group
}

/** 地面小舞台 + 软光照 */
function buildStage() {
  // 地面：奶白圆盘
  const ground = new THREE.Mesh(
    new THREE.CylinderGeometry(2.6, 2.6, 0.16, 40),
    new THREE.MeshStandardMaterial({ color: 0xfff1e6, roughness: 1 })
  )
  ground.position.y = -0.28
  scene.add(ground)

  // 环形描边（用略大的扁圆环模拟）
  const rim = new THREE.Mesh(
    new THREE.TorusGeometry(2.6, 0.05, 10, 60),
    new THREE.MeshStandardMaterial({ color: colorOf(), roughness: 0.9 })
  )
  rim.rotation.x = Math.PI / 2
  rim.position.y = -0.2
  scene.add(rim)

  scene.add(new THREE.HemisphereLight(0xffffff, 0xffe3d0, 1.0))
  const dir = new THREE.DirectionalLight(0xffffff, 1.4)
  dir.position.set(3, 5, 4)
  scene.add(dir)
}

function initThree() {
  const el = mountEl.value
  const w = el.clientWidth
  const h = el.clientHeight

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(w, h)
  el.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xfdf3f7)

  camera = new THREE.PerspectiveCamera(42, w / h, 0.1, 50)
  camera.position.set(0, 1.5, 4.6)
  camera.lookAt(0, 0.8, 0)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0, 0.8, 0)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 2.4
  controls.maxDistance = 8
  controls.maxPolarAngle = Math.PI * 0.55
  controls.enablePan = false

  buildStage()
  petGroup = buildPet(props.pet?.typeId)
  scene.add(petGroup)

  clock = new THREE.Clock()

  // 点击宠物 = 摸摸
  const ray = new THREE.Raycaster()
  const ndc = new THREE.Vector2()
  el.addEventListener('pointerdown', onPointerDown)
  function onPointerDown(ev) {
    const rect = el.getBoundingClientRect()
    ndc.x = ((ev.clientX - rect.left) / rect.width) * 2 - 1
    ndc.y = -((ev.clientY - rect.top) / rect.height) * 2 + 1
    ray.setFromCamera(ndc, camera)
    const hit = ray.intersectObject(petGroup, true)
    if (hit.length) doTouch()
  }

  animate()
}

/** 每帧动画：呼吸 / 眨眼 / 尾巴摆 / 跳 / 咀嚼 / 挤压 / 食物下落 */
function animate() {
  rafId = requestAnimationFrame(animate)
  const dt = clock.getDelta()
  const t = clock.elapsedTime

  if (petGroup) {
    // 呼吸：身体 y 轻微起伏
    const breath = 1 + Math.sin(t * 2.2) * 0.025
    parts.body.scale.y = (parts.baseBodyY || (parts.baseBodyY = parts.body.scale.y)) * breath
    // 头轻微左右晃
    parts.head.rotation.z = Math.sin(t * 1.1) * 0.05

    // 眨眼：每 ~3.4s 闭一次
    const blinkCycle = t % 3.4
    const eyeS = blinkCycle < 0.12 ? Math.max(0.12, 1 - blinkCycle / 0.06) : 1
    parts.eyeL.scale.y = eyeS
    parts.eyeR.scale.y = eyeS

    // 尾巴 / 翅膀摆动
    if (parts.tail) parts.tail.rotation.y = Math.sin(t * 3.2) * 0.5
    if (parts.wingL && parts.wingR) {
      parts.wingL.rotation.z = Math.sin(t * 4) * 0.25
      parts.wingR.rotation.z = -Math.sin(t * 4) * 0.25
    }

    // 跳跃：0.9 秒抛物线
    if (jumpT >= 0) {
      jumpT += dt
      const p = jumpT / 0.9
      if (p >= 1) {
        jumpT = -1
        petGroup.position.y = 0
      } else {
        petGroup.position.y = Math.sin(p * Math.PI) * 0.9
        // 空中腿收起感：整体轻微后仰
        petGroup.rotation.x = -Math.sin(p * Math.PI) * 0.12
      }
    }

    // 摸摸挤压：0.35 秒 Q 弹
    if (squishT >= 0) {
      squishT += dt
      const p = squishT / 0.35
      if (p >= 1) {
        squishT = -1
        petGroup.scale.set(1, 1, 1)
      } else {
        const s = 1 + Math.sin(p * Math.PI) * 0.12
        petGroup.scale.set(s, 1 / s, s)
      }
    }

    // 喂食：食物球下落 + 低头咀嚼
    if (pelletT >= 0 && foodPellet) {
      pelletT += dt
      const p = pelletT / 0.6
      if (p < 1) {
        foodPellet.position.set(0, 2.2 - p * 1.5, 0.55)
      } else if (p < 2.2) {
        foodPellet.visible = false
        chewT = pelletT
        parts.head.rotation.x = Math.abs(Math.sin((pelletT - 0.6) * 12)) * 0.25
      } else {
        scene.remove(foodPellet)
        foodPellet = null
        pelletT = -1
        chewT = -1
        parts.head.rotation.x = 0
        feeding.value = false
      }
    }
  }

  controls && controls.update()
  renderer && renderer.render(scene, camera)
}

function startPellet() {
  if (foodPellet) scene.remove(foodPellet)
  foodPellet = ball(0.09, 0xd9a066)
  foodPellet.position.set(0, 2.2, 0.55)
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
  emit('interact')
  showToast('摸摸 +1 🧡')
}

function doFeed() {
  if (!props.canFeed || feeding.value) return
  feeding.value = true
  startPellet()
  emit('feed')
  showToast('干饭最积极了 🍖')
}

function doJump() {
  if (jumpT < 0) jumpT = 0
}

/* ---------------- 生命周期 ---------------- */
onMounted(initThree)
onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  if (mountEl.value) {
    // 事件随 DOM 一起销毁，无需手动解绑
  }
  controls && controls.dispose()
  renderer && renderer.dispose()
  renderer = null
  scene = null
})

// 换宠物（父组件切换 3D 对象时）重建模型
watch(
  () => props.pet && props.pet.id,
  () => {
    if (!scene || !petGroup) return
    scene.remove(petGroup)
    petGroup = buildPet(props.pet?.typeId)
    scene.add(petGroup)
  }
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
  background: rgba(74, 59, 82, 0.42);
  backdrop-filter: blur(3px);
}
.p3d-card {
  width: 100%;
  max-width: 560px;
  background: var(--card);
  border: 3px solid var(--edge);
  border-radius: 22px;
  box-shadow: var(--shadow);
  overflow: hidden;
}
.p3d-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px dashed var(--line);
}
.p3d-title { margin: 0; font-size: 15px; }
.p3d-close {
  border: none;
  background: none;
  font-size: 20px;
  line-height: 1;
  color: var(--ink-soft);
  cursor: pointer;
}
.p3d-tip { margin: 10px 16px 0; font-size: 12px; color: var(--ink-soft); }
.p3d-stage {
  height: 380px;
  margin: 10px 16px;
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid var(--line);
  touch-action: none;
}
.p3d-actions {
  display: flex;
  gap: 10px;
  padding: 0 16px 14px;
}
.p3d-btn {
  flex: 1;
  border: 2px solid var(--line);
  background: var(--card);
  font-family: inherit;
  font-size: 13px;
  font-weight: 800;
  padding: 10px 0;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.16s ease;
}
.p3d-btn:hover:not(:disabled) { background: var(--soft); border-color: var(--pink); transform: translateY(-2px); }
.p3d-btn:disabled { opacity: 0.55; cursor: default; }
.p3d-toast {
  margin: 0 16px 14px;
  text-align: center;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--pink-deep);
}
@media (max-width: 640px) {
  .p3d-stage { height: 300px; }
}
</style>
