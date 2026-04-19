/**
 * Tag type (UI styling) mappings for dictionary items.
 * Labels come from the backend dict API via useDict; tag types are a frontend concern.
 */

/** 通用启用/禁用标签样式 */
export const COMMON_STATUS_TAG_MAP: Record<string, string> = {
  '1': 'success',
  '0': 'danger',
}

/** 机构状态（复用通用映射） */
export const ORG_STATUS_TAG_MAP = COMMON_STATUS_TAG_MAP

/** 用户账号状态 */
export const USER_STATUS_TAG_MAP: Record<string, string> = {
  '1': 'success',
  '2': 'danger',
  '3': 'info',
  '4': '',
}

/** 字典状态 */
export const DICT_STATUS_TAG_MAP = COMMON_STATUS_TAG_MAP

/** 角色状态（复用通用映射） */
export const ROLE_STATUS_TAG_MAP = COMMON_STATUS_TAG_MAP

/** 操作/登录结果 */
export const RESULT_TAG_MAP: Record<string, string> = {
  success: 'success',
  fail: 'danger',
}
