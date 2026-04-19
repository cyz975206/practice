import { defineStore } from "pinia";
import { loginApi, logoutApi, getUserInfoApi } from "@/api/auth";
import { store } from "@/store";
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
