import request from "@/utils/request";
import type { OperationLogQuery } from "./types";

/** 分页查询操作日志 */
export function getOperationLogPage(params: OperationLogQuery) {
  return request({ url: "/api/log/operation", method: "get", params });
}
