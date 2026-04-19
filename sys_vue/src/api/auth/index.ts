import request from "@/utils/request";
import type { LoginData } from "./types";

/** 登录 */
export function loginApi(data: LoginData) {
  return request({
    url: "/api/auth/login",
    method: "post",
    data,
  });
}

/** 登出 */
export function logoutApi() {
  return request({
    url: "/api/auth/logout",
    method: "post",
  });
}

/** 获取当前登录用户信息 */
export function getUserInfoApi() {
  return request({
    url: "/api/auth/info",
    method: "get",
  });
}
