/**
 * Check if an element has a class
 */
export function hasClass(ele: HTMLElement, cls: string) {
  return !!ele.className.match(new RegExp("(\\s|^)" + cls + "(\\s|$)"));
}

/**
 * Add class to element
 */
export function addClass(ele: HTMLElement, cls: string) {
  if (!hasClass(ele, cls)) ele.className += " " + cls;
}

/**
 * Remove class from element
 */
export function removeClass(ele: HTMLElement, cls: string) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp("(\\s|^)" + cls + "(\\s|$)");
    ele.className = ele.className.replace(reg, " ");
  }
}

/**
 * Check if path is external
 */
export function isExternal(path: string) {
  return /^(https?:|mailto:|tel:)/.test(path);
}

import dayjs from 'dayjs'

/** 格式化日期时间 */
export function formatDate(date?: string, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/** 数组去重 */
export function uniqueArray<T>(array: T[]): T[] {
  return [...new Set(array)];
}

/** 按 key 去重 */
export function uniqueData<T>(data: T[], key: string): T[] {
  return data.reduce((acc, cur) => {
    if (!acc.find((item: any) => item[key] === (cur as any)[key])) {
      return acc.concat([cur]);
    }
    return acc;
  }, [] as T[]);
}

/** 在树形数据中按 ID 查找节点 */
export function findNodeById(data: any[], targetId: any, idKey = "id"): any {
  for (const item of data) {
    if (item[idKey] === targetId) return item;
    if (item.children?.length > 0) {
      const found = findNodeById(item.children, targetId, idKey);
      if (found) return found;
    }
  }
  return null;
}

/** 在机构树中按 orgCode 查找机构名称 */
export function findOrgNameById(orgTree: any[], targetOrgCode: string): string | undefined {
  function search(list: any[]): string | undefined {
    for (const org of list) {
      if (org.orgCode === targetOrgCode) return org.orgShortName;
      if (org.children?.length > 0) {
        const found = search(org.children);
        if (found) return found;
      }
    }
    return undefined;
  }
  return search(orgTree);
}

/** 获取当前日期 YYYY-MM-DD */
export function getCurrentDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/** 获取数组中出现次数最多的元素的次数 */
export function getMaxDuplicateCount(arr: any[]): number {
  const counts: Record<string, number> = {};
  arr.forEach((item) => {
    counts[item] = (counts[item] || 0) + 1;
  });
  return Math.max(...Object.values(counts));
}

/** 清除登录态 */
export function clear() {
  localStorage.removeItem("access_token");
}
