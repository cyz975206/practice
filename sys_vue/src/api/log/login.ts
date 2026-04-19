import request from "@/utils/request";
import type { LoginLogQuery } from "./types";

/** 分页查询登录日志 */
export function getLoginLogPage(params: LoginLogQuery) {
  return request({ url: "/api/log/login", method: "get", params });
}
