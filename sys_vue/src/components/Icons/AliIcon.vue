<template>
  <i class="iconfont" :class="icon"></i>
</template>

<script lang="ts" setup>
defineProps({
  icon: {
    type: String,
  },
});
</script>
