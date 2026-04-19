import request from "@/utils/request";
import type { SystemLogQuery } from "./types";

/** 分页查询系统日志 */
export function getSystemLogPage(params: SystemLogQuery) {
  return request({ url: "/api/log/system", method: "get", params });
}
