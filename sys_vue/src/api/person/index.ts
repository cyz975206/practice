import request from "@/utils/request";
import type { PersonForm, PersonQuery } from "./types";

/** 创建人员 */
export function createPerson(data: PersonForm) {
  return request({ url: "/api/person", method: "post", data });
}

/** 更新人员 */
export function updatePerson(id: number, data: PersonForm) {
  return request({ url: `/api/person/${id}`, method: "put", data });
}

/** 删除人员 */
export function deletePerson(id: number) {
  return request({ url: `/api/person/${id}`, method: "delete" });
}

/** 获取人员详情 */
export function getPersonDetail(id: number) {
  return request({ url: `/api/person/${id}`, method: "get" });
}

/** 分页查询人员 */
export function getPersonPage(params: PersonQuery) {
  return request({ url: "/api/person", method: "get", params });
}

/** 导出人员 */
export function exportPerson(params?: PersonQuery) {
  return request({ url: "/api/person/export", method: "get", params, responseType: "blob" });
}

/** 导入人员 */
export function importPerson(file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/api/person/import/excel",
    method: "post",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  });
}
