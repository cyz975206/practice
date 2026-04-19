import { defineStore } from "pinia";
import { loginApi, logoutApi, getUserInfoApi } from "@/api/auth";
import { store } from "@/store";
import { usePermissionStoreHook } from "./permission";
import { useMenuStoreHook } from "./menu";
import type { LoginData, UserInfo } from "@/api/auth/types";
import type { R } from "@/api/types";

interface LoginResult {
  token: string;
  userInfo: UserInfo;
}

export const useUserStore = defineStore("user", () => {
  const user: UserInfo = {
    id: 0,
    username: "",
    nickname: "",
    roles: [],
    perms: [],
  };

  // 修改密码弹窗
  const editPassword = ref(false);

  /** 登录 */
  async function login(loginData: LoginData) {
    const response = (await loginApi(loginData)) as R<LoginResult>;
    const { token } = response.data;
    localStorage.setItem("access_token", token);
    // 清除旧权限，获取新权限和菜单
    user.roles = [];
    user.perms = [];
    await getUserInfo();
    await generateRoutes();
  }

  /** 生成动态路由和菜单 */
  async function generateRoutes() {
    const permissionStore = usePermissionStoreHook();
    const { routes: accessRoutes, menuTree } = await permissionStore.generateRoutes();
    if (menuTree.length > 0) {
      const menuStore = useMenuStoreHook();
      menuStore.setMenuTree(menuTree);
    }
    const router = (await import("@/router")).default;
    accessRoutes.forEach((route) => {
      router.addRoute(route);
    });
  }

  /** 获取当前用户信息 */
  async function getUserInfo() {
    const response = (await getUserInfoApi()) as R<UserInfo>;
    if (response.data) {
      const data = response.data;
      if (!data.roles || data.roles.length <= 0) {
        throw new Error("用户未分配角色");
      }
      Object.assign(user, data);
      return data;
    }
    throw new Error("获取用户信息失败");
  }

  /** 登出 */
  async function logout() {
    try {
      await logoutApi();
    } finally {
      localStorage.removeItem("access_token");
      window.location.href = import.meta.env.BASE_URL + "#/login";
    }
  }

  /** 重置 Token */
  function resetToken() {
    localStorage.removeItem("access_token");
    user.roles = [];
    user.perms = [];
  }

  return {
    editPassword,
    user,
    login,
    getUserInfo,
    logout,
    resetToken,
  };
});

// 非setup
export function useUserStoreHook() {
  return useUserStore(store);
}
