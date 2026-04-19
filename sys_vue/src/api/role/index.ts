import request from "@/utils/request";
import type { RoleForm, RoleQuery } from "./types";

/** 创建角色 */
export function createRole(data: RoleForm) {
  return request({ url: "/api/role", method: "post", data });
}

/** 更新角色 */
export function updateRole(id: number, data: RoleForm) {
  return request({ url: `/api/role/${id}`, method: "put", data });
}

/** 删除角色 */
export function deleteRole(id: number) {
  return request({ url: `/api/role/${id}`, method: "delete" });
}

/** 获取角色详情 */
export function getRoleDetail(id: number) {
  return request({ url: `/api/role/${id}`, method: "get" });
}

/** 分页查询角色 */
export function getRolePage(params: RoleQuery) {
  return request({ url: "/api/role", method: "get", params });
}

/** 分配菜单权限 */
export function assignRoleMenus(id: number, menuIds: number[]) {
  return request({ url: `/api/role/${id}/menus`, method: "put", data: menuIds });
}

/** 查询角色已分配菜单ID列表 */
export function getRoleMenuIds(id: number) {
  return request({ url: `/api/role/${id}/menus`, method: "get" });
}
