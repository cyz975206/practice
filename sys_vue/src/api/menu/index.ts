import request from "@/utils/request";
import type { MenuForm, MenuQuery } from "./types";

/** 创建菜单 */
export function createMenu(data: MenuForm) {
  return request({ url: "/api/menu", method: "post", data });
}

/** 更新菜单 */
export function updateMenu(id: number, data: MenuForm) {
  return request({ url: `/api/menu/${id}`, method: "put", data });
}

/** 删除菜单 */
export function deleteMenu(id: number) {
  return request({ url: `/api/menu/${id}`, method: "delete" });
}

/** 获取菜单详情 */
export function getMenuDetail(id: number) {
  return request({ url: `/api/menu/${id}`, method: "get" });
}

/** 分页查询菜单 */
export function getMenuPage(params: MenuQuery) {
  return request({ url: "/api/menu", method: "get", params });
}

/** 获取菜单树 */
export function getMenuTree() {
  return request({ url: "/api/menu/tree", method: "get" });
}
