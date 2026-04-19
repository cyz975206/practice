<script setup lang="ts">
import { useAppStore } from "@/store/modules/app";
import { useUserStore } from "@/store/modules/user";
import screenfull from "screenfull";

const appStore = useAppStore();
const userStore = useUserStore();

function toggleSideBar() {
  appStore.toggleSidebar();
}

function toggleFullscreen() {
  screenfull.toggle();
}

function handleChangePassword() {
  userStore.editPassword = true;
}

async function handleLogout() {
  await userStore.logout();
}
</script>

<template>
  <!-- 顶部导航栏 -->
  <div class="navbar">
    <!-- 左侧面包屑 -->
    <div class="flex">
      <hamburger
        :is-active="appStore.sidebar.opened"
        @toggle-click="toggleSideBar"
      />
      <breadcrumb />
    </div>

    <!-- 右侧导航 -->
    <div class="flex items-center">
      <el-tooltip content="全屏" placement="bottom">
        <svg-icon icon-class="fullscreen" class="nav-icon" @click="toggleFullscreen" />
      </el-tooltip>
      <el-dropdown trigger="click" class="ml-4">
        <span class="flex items-center cursor-pointer">
          <el-avatar :size="28" class="mr-2">
            {{ userStore.user.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ userStore.user.nickname || userStore.user.username }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleChangePassword">修改密码</el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 0 1px #0003;
}
.nav-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;

  &:hover {
    color: var(--el-color-primary);
  }
}
</style>
