import request from "@/utils/request";
import type { DictItemForm, DictItemQuery } from "./types";

/** 创建字典项 */
export function createDictItem(data: DictItemForm) {
  return request({ url: "/api/dict/item", method: "post", data });
}

/** 更新字典项 */
export function updateDictItem(id: number, data: DictItemForm) {
  return request({ url: `/api/dict/item/${id}`, method: "put", data });
}

/** 删除字典项 */
export function deleteDictItem(id: number) {
  return request({ url: `/api/dict/item/${id}`, method: "delete" });
}

/** 获取字典项详情 */
export function getDictItemDetail(id: number) {
  return request({ url: `/api/dict/item/${id}`, method: "get" });
}

/** 分页查询字典项 */
export function getDictItemPage(params: DictItemQuery) {
  return request({ url: "/api/dict/item", method: "get", params });
}

/** 启用/禁用字典项 */
export function updateDictItemStatus(id: number, status: string) {
  return request({ url: `/api/dict/item/${id}/status`, method: "put", data: { status } });
}

/** 批量调整排序 */
export function sortDictItem(data: { ids: number[]; sorts: number[] }) {
  return request({ url: "/api/dict/item/sort", method: "put", data });
}
