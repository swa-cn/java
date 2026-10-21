<template>
  <!-- 彩带庆祝画布 -->
  <canvas id="fx" ref="canvasEl" aria-hidden="true"></canvas>

  <!-- 背景装饰：云朵 / 星星 / 爱心 -->
  <div class="scene" aria-hidden="true">
    <div class="cloud c1"><i></i><i></i><i></i></div>
    <div class="cloud c2"><i></i><i></i><i></i></div>
    <div class="cloud c3"><i></i><i></i><i></i></div>
    <span class="star s1"></span>
    <span class="star s2"></span>
    <span class="star s3"></span>
    <span class="star s4"></span>
    <span class="star s5"></span>
    <span class="heart h1">♥</span>
    <span class="heart h2">♥</span>
    <span class="heart h3">♥</span>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { initConfetti } from '../composables/useConfetti'

const canvasEl = ref(null)

onMounted(() => {
  initConfetti(canvasEl.value)
})
</script>
