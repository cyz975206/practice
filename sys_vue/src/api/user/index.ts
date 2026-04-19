import request from "@/utils/request";
import type { UserForm, UserQuery } from "./types";

/** 创建用户 */
export function createUser(data: UserForm) {
  return request({ url: "/api/user", method: "post", data });
}

/** 更新用户 */
export function updateUser(id: number, data: UserForm) {
  return request({ url: `/api/user/${id}`, method: "put", data });
}

/** 删除用户 */
export function deleteUser(id: number) {
  return request({ url: `/api/user/${id}`, method: "delete" });
}

/** 获取用户详情 */
export function getUserDetail(id: number) {
  return request({ url: `/api/user/${id}`, method: "get" });
}

/** 分页查询用户 */
export function getUserPage(params: UserQuery) {
  return request({ url: "/api/user", method: "get", params });
}

/** 重置密码（不传 newPassword 则自动生成） */
export function resetPassword(id: number, data?: { newPassword: string }) {
  return request({ url: `/api/user/${id}/reset-password`, method: "put", data });
}

/** 修改密码 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request({ url: "/api/user/change-password", method: "put", data });
}

/** 解锁用户 */
export function unlockUser(id: number) {
  return request({ url: `/api/user/${id}/unlock`, method: "put" });
}

/** 分配角色 */
export function assignUserRoles(id: number, roleIds: number[]) {
  return request({ url: `/api/user/${id}/roles`, method: "put", data: roleIds });
}
