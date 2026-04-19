import request from "@/utils/request";
import type { ConfigForm, ConfigQuery } from "./types";

/** 创建系统配置 */
export function createConfig(data: ConfigForm) {
  return request({ url: "/api/config", method: "post", data });
}

/** 更新系统配置 */
export function updateConfig(id: number, data: ConfigForm) {
  return request({ url: `/api/config/${id}`, method: "put", data });
}

/** 删除系统配置 */
export function deleteConfig(id: number) {
  return request({ url: `/api/config/${id}`, method: "delete" });
}

/** 查询系统配置详情 */
export function getConfigDetail(id: number) {
  return request({ url: `/api/config/${id}`, method: "get" });
}

/** 分页查询系统配置 */
export function getConfigPage(params: ConfigQuery) {
  return request({ url: "/api/config", method: "get", params });
}

/** 按配置键查询配置值 */
export function getConfigByKey(configKey: string) {
  return request({ url: `/api/config/key/${configKey}`, method: "get" });
}
