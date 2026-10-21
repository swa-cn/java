// =====================================================================
// useConfetti —— 全屏 Canvas 彩带庆祝效果（模块级单例）
// 由原生 app.js 中的 Confetti 模块改造而来。
// 画布由 SceneBackground.vue 在挂载时通过 initConfetti(canvas) 注入。
// =====================================================================

let canvas = null
let ctx = null
let parts = []
let raf = 0
let reduced = false

const COLORS = ['#ff7eb3', '#ffcf5c', '#5fd4b0', '#7c6cff', '#8fd3ff', '#ff8a5c']

function resize() {
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

function drawStar(x, y, r, rot) {
  ctx.beginPath()
  for (let i = 0; i < 5; i++) {
    const a = rot + (i * 4 * Math.PI) / 5 - Math.PI / 2
    const px = x + Math.cos(a) * r
    const py = y + Math.sin(a) * r
    if (i === 0) ctx.moveTo(px, py)
    else ctx.lineTo(px, py)
  }
  ctx.closePath()
  ctx.fill()
}

function tick() {
  if (!ctx) return
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  for (let i = parts.length - 1; i >= 0; i--) {
    const p = parts[i]
    p.vy += 0.12
    p.vx *= 0.985
    p.x += p.vx
    p.y += p.vy
    p.rot += p.vr
    p.a -= 0.011
    if (p.a <= 0 || p.y > canvas.height + 30) {
      parts.splice(i, 1)
      continue
    }
    ctx.save()
    ctx.globalAlpha = Math.max(p.a, 0)
    ctx.fillStyle = p.color
    if (p.star) drawStar(p.x, p.y, p.r, p.rot)
    else {
      ctx.beginPath()
      ctx.ellipse(p.x, p.y, p.r, p.r * 0.55, p.rot, 0, Math.PI * 2)
      ctx.fill()
    }
    ctx.restore()
  }
  if (parts.length) raf = requestAnimationFrame(tick)
  else {
    raf = 0
    ctx.clearRect(0, 0, canvas.width, canvas.height)
  }
}

export function initConfetti(canvasEl) {
  reduced =
    window.matchMedia &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduced) return
  canvas = canvasEl
  if (!canvas) return
  ctx = canvas.getContext('2d')
  resize()
  window.addEventListener('resize', resize)
}

export function burstConfetti(x, y, n) {
  if (reduced || !canvas) return
  for (let i = 0; i < n; i++) {
    parts.push({
      x: x + (Math.random() - 0.5) * 60,
      y: y + (Math.random() - 0.5) * 30,
      vx: (Math.random() - 0.5) * 7,
      vy: -3 - Math.random() * 4,
      r: 4 + Math.random() * 6,
      rot: Math.random() * Math.PI,
      vr: (Math.random() - 0.5) * 0.25,
      a: 1,
      star: Math.random() < 0.45,
      color: COLORS[(Math.random() * COLORS.length) | 0]
    })
  }
  if (!raf) raf = requestAnimationFrame(tick)
}
