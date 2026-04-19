<template>
  <div
    class="px-[15px] flex items-center justify-center color-[var(--el-text-color-regular)]"
    role="button"
    :aria-expanded="isActive"
    aria-label="Toggle sidebar"
    tabindex="0"
    @click="toggleClick"
    @keydown.enter="toggleClick"
    @keydown.space.prevent="toggleClick"
  >
    <svg-icon
      class="hamburger"
      :class="{ 'is-active': isActive }"
      icon-class="shrink"
    />
  </div>
</template>
<script setup lang="ts">
defineProps({
  isActive: {
    required: true,
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["toggleClick"]);

function toggleClick() {
  emit("toggleClick");
}
</script>

<style scoped lang="scss">
.hamburger {
  vertical-align: middle;
  cursor: pointer;
  transform: scaleX(-1);
}

.hamburger.is-active {
  transform: scaleX(1);
}
</style>
