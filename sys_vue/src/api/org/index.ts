import request from "@/utils/request";
import type { OrgForm, OrgQuery, OrgSortForm } from "./types";

/** 创建机构 */
export function createOrg(data: OrgForm) {
  return request({ url: "/api/org", method: "post", data });
}

/** 更新机构 */
export function updateOrg(id: number, data: OrgForm) {
  return request({ url: `/api/org/${id}`, method: "put", data });
}

/** 删除机构 */
export function deleteOrg(id: number) {
  return request({ url: `/api/org/${id}`, method: "delete" });
}

/** 获取机构详情 */
export function getOrgDetail(id: number) {
  return request({ url: `/api/org/${id}`, method: "get" });
}

/** 分页查询机构 */
export function getOrgPage(params: OrgQuery) {
  return request({ url: "/api/org", method: "get", params });
}

/** 获取机构树 */
export function getOrgTree() {
  return request({ url: "/api/org/tree", method: "get" });
}

/** 启用/停用机构 */
export function updateOrgStatus(id: number, status: string) {
  return request({ url: `/api/org/${id}/status`, method: "put", data: { status } });
}

/** 批量调整排序 */
export function sortOrg(data: OrgSortForm) {
  return request({ url: "/api/org/sort", method: "put", data });
}

/** 导出机构 */
export function exportOrg(params?: OrgQuery) {
  return request({ url: "/api/org/export", method: "get", params, responseType: "blob" });
}

/** 导入机构 */
export function importOrg(file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/api/org/import/excel",
    method: "post",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  });
}
