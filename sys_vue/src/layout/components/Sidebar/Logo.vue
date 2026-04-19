<script lang="ts" setup>
import { useSettingsStore } from "@/store/modules/settings";

const settingsStore = useSettingsStore();

defineProps({
  collapse: {
    type: Boolean,
    required: true,
  },
});


</script>

<template>
  <div
    class="w-full h-[50px] bg-gray-800 dark:bg-[var(--el-bg-color-overlay)] logo-wrap"
  >
    <transition name="sidebarLogoFade">
      <router-link
        v-if="collapse"
        key="collapse"
        class="h-full w-full flex items-center justify-center"
        to="/"
      >
        <img width="35" v-if="settingsStore.sidebarLogo" src="/logo/logo.png"  />
      </router-link>

      <router-link
        v-else
        key="expand"
        class="h-full w-full flex items-center pl-5"
        to="/"
      >
        <img width="35" v-if="settingsStore.sidebarLogo" src="/logo/logo.png"  />
        <span class="ml-3 text-white text-sm font-bold">
          {{ settingsStore.title }}</span
        >
      </router-link>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
// https://cn.vuejs.org/guide/built-ins/transition.html#the-transition-component
.sidebarLogoFade-enter-active {
  transition: opacity 2s;
}

.sidebarLogoFade-leave-active,
.sidebarLogoFade-enter-from,
.sidebarLogoFade-leave-to {
  opacity: 0;
}
</style>
