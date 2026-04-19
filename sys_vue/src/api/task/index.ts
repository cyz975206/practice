import request from "@/utils/request";
import type { TaskForm, TaskQuery } from "./types";

/** 创建定时任务 */
export function createTask(data: TaskForm) {
  return request({ url: "/api/task", method: "post", data });
}

/** 更新定时任务 */
export function updateTask(id: number, data: TaskForm) {
  return request({ url: `/api/task/${id}`, method: "put", data });
}

/** 删除定时任务 */
export function deleteTask(id: number) {
  return request({ url: `/api/task/${id}`, method: "delete" });
}

/** 查询定时任务详情 */
export function getTaskDetail(id: number) {
  return request({ url: `/api/task/${id}`, method: "get" });
}

/** 分页查询定时任务 */
export function getTaskPage(params: TaskQuery) {
  return request({ url: "/api/task", method: "get", params });
}

/** 启用定时任务 */
export function startTask(id: number) {
  return request({ url: `/api/task/${id}/start`, method: "put" });
}

/** 停用定时任务 */
export function stopTask(id: number) {
  return request({ url: `/api/task/${id}/stop`, method: "put" });
}

/** 手动触发定时任务 */
export function triggerTask(id: number) {
  return request({ url: `/api/task/${id}/trigger`, method: "post" });
}
