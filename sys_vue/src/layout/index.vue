<script setup lang="ts">
import Main from "./main.vue";
import { computed, watchEffect } from "vue";
import { useWindowSize } from "@vueuse/core";
import Sidebar from "./components/Sidebar/index.vue";
import LeftMenu from "./components/Sidebar/LeftMenu.vue";
import EditPassword from "@/components/EditPassword/index.vue";
import { useRoute } from "vue-router";

import { useAppStore } from "@/store/modules/app";
import { useUserStore } from "@/store/modules/user";
import { useSettingsStore } from "@/store/modules/settings";
import { usePermissionStore } from "@/store/modules/permission";

const permissionStore = usePermissionStore();
const { width } = useWindowSize();
const route = useRoute();

const WIDTH = 992;
const appStore = useAppStore();
const userStore = useUserStore();
const settingsStore = useSettingsStore();

const activeTopMenu = computed(() => appStore.activeTopMenu);
const mixLeftMenu = computed(() => permissionStore.mixLeftMenu);
const layout = computed(() => settingsStore.layout);

watch(
  () => activeTopMenu.value,
  (newVal) => {
    if (layout.value !== "mix") return;
    permissionStore.getMixLeftMenu(newVal);
  },
  { deep: true, immediate: true }
);

const classObj = computed(() => ({
  hideSidebar: !appStore.sidebar.opened,
  openSidebar: appStore.sidebar.opened,
  withoutAnimation: appStore.sidebar.withoutAnimation,
  mobile: appStore.device === "mobile",
  isTop: layout.value === "top",
  isMix: layout.value === "mix",
}));

watchEffect(() => {
  if (width.value < WIDTH) {
    appStore.toggleDevice("mobile");
    appStore.closeSideBar(true);
  } else {
    appStore.toggleDevice("desktop");
    if (width.value >= 1200) {
      appStore.openSideBar(true);
    } else {
      appStore.closeSideBar(true);
    }
  }
});

function handleOutsideClick() {
  appStore.closeSideBar(false);
}

function toggleSideBar() {
  appStore.toggleSidebar();
}
</script>

<template>
  <div :class="classObj" class="app-wrapper">
    <!-- 手机设备侧边栏打开遮罩层 -->
    <div
      v-if="classObj.mobile && classObj.openSidebar"
      class="drawer__background"
      @click="handleOutsideClick"
    ></div>

    <Sidebar class="sidebar-container" />

    <div v-if="layout === 'mix'" class="mix-wrapper">
      <div class="mix-wrapper__left">
        <LeftMenu :menu-list="mixLeftMenu" :base-path="activeTopMenu" />
        <div class="toggle-sidebar">
          <hamburger
            :is-active="appStore.sidebar.opened"
            @toggle-click="toggleSideBar"
          />
        </div>
      </div>
      <Main />
    </div>

    <Main v-else />

    <EditPassword v-model="userStore.editPassword" />
  </div>
</template>

<style lang="scss" scoped>
.app-wrapper {
  &::after {
    display: table;
    clear: both;
    content: "";
  }

  position: relative;
  width: 100%;
  height: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer__background {
  position: absolute;
  top: 0;
  z-index: 999;
  width: 100%;
  height: 100%;
  background: #000;
  opacity: 0.3;
}

.isTop {
  .sidebar-container {
    z-index: 800;
    display: flex;
    width: 100% !important;
    height: 50px;

    :deep(.logo-wrap) {
      width: $sideBarWidth;
    }

    :deep(.el-scrollbar) {
      flex: 1;
      min-width: 0;
      height: 50px;
    }
  }

  .main-container {
    padding-top: 50px;
    margin-left: 0;
    overflow: hidden;
  }

  --el-menu-item-height: 50px;
}

.mobile.isTop {
  :deep(.logo-wrap) {
    width: 63px;
  }
}

.isMix {
  :deep(.main-container) {
    display: inline-block;
    width: calc(100% - #{$sideBarWidth});
    margin-left: 0;
  }

  .mix-wrapper {
    display: flex;
    height: 100%;
    padding-top: 50px;

    .mix-wrapper__left {
      position: relative;
      height: 100%;

      .el-menu {
        height: 100%;
      }

      .toggle-sidebar {
        position: absolute;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 50px;
        line-height: 50px;
        box-shadow: 0 0 6px -2px var(--el-color-primary);

        div:hover {
          background-color: var(--menuBg);
        }

        :deep(svg) {
          color: #409eff !important;
        }
      }
    }

    .main-container {
      flex: 1;
      min-width: 0;
    }
  }
}

.openSidebar {
  .mix-wrapper {
    .mix-wrapper__left {
      width: $sideBarWidth;
    }

    :deep(.svg-icon) {
      margin-top: -1px;
      margin-right: 5px;
    }

    .el-menu {
      border: none;
    }
  }
}
</style>
