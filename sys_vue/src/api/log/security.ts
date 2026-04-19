import request from "@/utils/request";
import type { SecurityLogQuery, SecurityLogHandleForm } from "./types";

/** 分页查询安全日志 */
export function getSecurityLogPage(params: SecurityLogQuery) {
  return request({ url: "/api/log/security", method: "get", params });
}

/** 处理安全日志 */
export function handleSecurityLog(id: number, data: SecurityLogHandleForm) {
  return request({ url: `/api/log/security/${id}/handle`, method: "put", data });
}
