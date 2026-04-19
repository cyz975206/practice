import request from "@/utils/request";
import type { DictTypeForm, DictTypeQuery } from "./types";

/** 创建字典类型 */
export function createDictType(data: DictTypeForm) {
  return request({ url: "/api/dict/type", method: "post", data });
}

/** 更新字典类型 */
export function updateDictType(id: number, data: DictTypeForm) {
  return request({ url: `/api/dict/type/${id}`, method: "put", data });
}

/** 删除字典类型 */
export function deleteDictType(id: number) {
  return request({ url: `/api/dict/type/${id}`, method: "delete" });
}

/** 获取字典类型详情 */
export function getDictTypeDetail(id: number) {
  return request({ url: `/api/dict/type/${id}`, method: "get" });
}

/** 分页查询字典类型 */
export function getDictTypePage(params: DictTypeQuery) {
  return request({ url: "/api/dict/type", method: "get", params });
}

/** 启用/禁用字典类型 */
export function updateDictTypeStatus(id: number, status: string) {
  return request({ url: `/api/dict/type/${id}/status`, method: "put", data: { status } });
}
