<template>
  <div class="mascot" :class="{ covering }" aria-hidden="true">
    <svg viewBox="0 0 120 112">
      <!-- 耳朵 -->
      <path d="M24 40 Q16 10 44 24 Z" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" stroke-linejoin="round" />
      <path d="M96 40 Q104 10 76 24 Z" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" stroke-linejoin="round" />
      <path d="M28 34 Q24 20 38 26 Z" fill="#ffb3c8" />
      <path d="M92 34 Q96 20 82 26 Z" fill="#ffb3c8" />
      <!-- 脸 -->
      <ellipse cx="60" cy="64" rx="42" ry="38" fill="#ffe3c2" stroke="#4a3540" stroke-width="4" />
      <!-- 眼睛 -->
      <g class="eyes">
        <ellipse cx="44" cy="60" rx="6.5" ry="9" fill="#4a3540" />
        <circle cx="46.5" cy="56.5" r="2.4" fill="#fff" />
        <ellipse cx="76" cy="60" rx="6.5" ry="9" fill="#4a3540" />
        <circle cx="78.5" cy="56.5" r="2.4" fill="#fff" />
      </g>
      <!-- 腮红 -->
      <ellipse cx="28" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
      <ellipse cx="92" cy="74" rx="8" ry="5" fill="#ffb3c8" opacity="0.85" />
      <!-- 嘴 -->
      <path d="M53 76 q3.5 5 7 0 q3.5 5 7 0" fill="none" stroke="#4a3540" stroke-width="3" stroke-linecap="round" />
      <!-- 胡须 -->
      <path d="M14 62 h12 M14 70 h11" stroke="#4a3540" stroke-width="2.4" stroke-linecap="round" />
      <path d="M106 62 h-12 M106 70 h-11" stroke="#4a3540" stroke-width="2.4" stroke-linecap="round" />
      <!-- 小手（捂眼时出现） -->
      <g class="paws">
        <ellipse cx="44" cy="62" rx="11" ry="9.5" fill="#ffe9cf" stroke="#4a3540" stroke-width="3.5" />
        <ellipse cx="76" cy="62" rx="11" ry="9.5" fill="#ffe9cf" stroke="#4a3540" stroke-width="3.5" />
        <ellipse cx="44" cy="64.5" rx="3.4" ry="2.4" fill="#ffb3c8" />
        <ellipse cx="76" cy="64.5" rx="3.4" ry="2.4" fill="#ffb3c8" />
      </g>
    </svg>
  </div>
</template>

<script setup>
// covering=true 时，小猫会抬起小手捂住眼睛
defineProps({
  covering: { type: Boolean, default: false }
})
</script>
