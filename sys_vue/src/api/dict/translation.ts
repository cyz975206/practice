import request from "@/utils/request";
import type { DictTranslationItem } from "./types";

/** 获取类型下所有翻译项 */
export function getDictTranslation(dictType: string) {
  return request({ url: `/api/dict/translation/${dictType}`, method: "get" });
}

/** 按编码翻译 */
export function translateByCode(dictType: string, dictCode: string) {
  return request({ url: `/api/dict/translation/${dictType}/code/${dictCode}`, method: "get" });
}

/** 按值翻译 */
export function translateByValue(dictType: string, dictValue: string) {
  return request({ url: `/api/dict/translation/${dictType}/value/${dictValue}`, method: "get" });
}
