import request from "@/utils/request";
import type { TaskLogQuery } from "./types";

/** 分页查询任务日志 */
export function getTaskLogPage(params: TaskLogQuery) {
  return request({ url: "/api/log/task", method: "get", params });
}
