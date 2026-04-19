/**
 * 状态映射常量与工具函数
 * 集中管理各模块状态的 label 和 tagType 映射
 */

export interface StatusMapEntry {
  label: string
  tagType: string
}

export type StatusMap = Record<string, StatusMapEntry>

/** 获取状态显示名 */
export function getStatusLabel(map: StatusMap, value: string): string {
  return map[value]?.label ?? value
}

/** 获取状态标签类型 */
export function getStatusTagType(map: StatusMap, value: string): string {
  return map[value]?.tagType ?? ''
}

/** 菜单/角色状态 */
export const MENU_ROLE_STATUS_MAP: StatusMap = {
  '1': { label: '启用', tagType: 'success' },
  '0': { label: '禁用', tagType: 'danger' },
}
