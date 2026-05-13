<template>
  <div class="app">
    <router-view v-slot="{ Component, route }">
      <transition name="page">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>

</script>

<style scoped>
.app {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--surface-base);
  position: relative;
}
</style>

<style>
/* 页面过渡 — 同时进行 + 绝对定位重叠，不依赖 mode="out-in" */
.page-enter-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
