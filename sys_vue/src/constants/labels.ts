/**
 * 业务标签映射常量
 * 集中管理各枚举字段的 label 和 tagType 映射
 */

import type { StatusMap } from './status'

/** 菜单类型 */
export const MENU_TYPE_MAP: StatusMap = {
  D: { label: '目录', tagType: '' },
  M: { label: '菜单', tagType: 'success' },
  B: { label: '按钮', tagType: 'warning' },
}

/** 日志级别标签类型 */
export const LOG_LEVEL_TAG_MAP: Record<string, string> = {
  INFO: '',
  WARN: 'warning',
  ERROR: 'danger',
}
