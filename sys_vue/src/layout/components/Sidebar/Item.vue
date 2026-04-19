<template>
  <el-icon v-if="icon && icon.includes('el-icon')" class="sub-el-icon" />
  <SvgIcon v-else-if="icon" :icon-class="icon" />
  <span v-if="title">{{ title }}</span>
</template>

<script setup lang="ts">
defineProps({
  icon: {
    type: String,
    default: "",
  },
  title: {
    type: String,
    default: "",
  },
});
</script>

<style scoped>
.sub-el-icon {
  width: 1em;
  height: 1em;
  color: currentcolor;
}
</style>
