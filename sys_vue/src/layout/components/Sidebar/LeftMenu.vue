<script lang="ts" setup>
import {useRoute} from "vue-router";
import SidebarItem from "./SidebarItem.vue";
import {useSettingsStore} from "@/store/modules/settings";
import {useAppStore} from "@/store/modules/app";
import variables from "@/styles/variables.module.scss";

import {isExternal} from "@/utils/index";
import {useTagsViewStore} from "@/store/modules/tagsView";

const settingsStore = useSettingsStore();
const appStore = useAppStore();
const currRoute = useRoute();
const layout = computed(() => settingsStore.layout);
const props = defineProps({
  menuList: {
    required: true,
    default: () => {
      return [];
    },
    type: Array<any>,
  },
  basePath: {
    type: String,
    required: true,
  },
});

/**
 * 解析路径
 *
 * @param routePath 路由路径
 */
function resolvePath(routePath: string) {
  if (isExternal(routePath)) {
    return routePath;
  }
  if (isExternal(props.basePath)) {
    return props.basePath;
  }

  // 完整路径 = 父级路径(/level/level_3) + 路由路径
  const fullPath = (props.basePath.replace(/\/$/, "") + "/" + routePath).replace(/\/+/g, "/"); // 相对路径 → 绝对路径
  return fullPath;
}

const activeMenu = computed(() => {
  if (currRoute.name == 'devDetail') {//设备详情
    return '/deviceManger/deviceManage'
  }
  if (currRoute.name == 'realSealdetail') {
    const tagsViewStore = useTagsViewStore();
    let activeMenu = tagsViewStore.activeMenu
    if (activeMenu == 'myRealSeal') {
      return '/deviceManger/myRealSeal'
    } else if (activeMenu == 'realSealManage') {
      return '/deviceManger/realSealManage'
    }
    return currRoute.path;
  }
  return currRoute.path;
});
</script>
<template>

  <el-menu
    :default-active="layout === 'top' ? '-' :activeMenu"
    :collapse="!appStore.sidebar.opened"
    :background-color="variables.menuBg"
    :text-color="variables.menuText"
    :active-text-color="variables.menuActiveText"
    :unique-opened="false"
    :collapse-transition="false"
    :mode="layout === 'top' ? 'horizontal' : 'vertical'"
  >
    <sidebar-item
      v-for="route in menuList"
      :key="route.path"
      :item="route"
      :base-path="resolvePath(route.path)"
      :is-collapse="!appStore.sidebar.opened"
    />
  </el-menu>
</template>
