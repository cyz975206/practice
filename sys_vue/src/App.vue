<script setup lang="ts">
import { useSettingsStore } from "@/store/modules/settings";
import { SizeEnum } from "@/enums/SizeEnum";
import router from "@/router";
import zhCn from "element-plus/es/locale/lang/zh-cn";

const settingsStore = useSettingsStore();

const size = computed(() => settingsStore.size || "default" as SizeEnum);
provide("useRouter", router);
</script>

<template>
  <el-config-provider :size="size" :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<style lang="scss">
.el-message {
  z-index: 9999!important;
}
:deep(.el-message){
  z-index: 9999!important;
}
.is-message-box {
  z-index: 2600!important;
}
.is-light {
  z-index: 2600!important;
}
</style>
